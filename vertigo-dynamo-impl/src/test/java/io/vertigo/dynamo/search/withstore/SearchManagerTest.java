/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.search.withstore;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.search.model.SearchQueryBuilder;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard couplé au store.
 *
 * @author npiedeloup
 */
public final class SearchManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private SqlDataBaseManager dataBaseManager;
	@Inject
	private StoreManager storeManager;
	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private SearchManager searchManager;
	//Index
	private static final String IDX_CAR = "IDX_CAR";

	private SearchIndexDefinition carIndexDefinition;

	private long initialDbCarSize = 0;

	@Override
	protected void doSetUp() throws Exception {
		carIndexDefinition = Home.getDefinitionSpace().resolve(IDX_CAR, SearchIndexDefinition.class);

		//A chaque test on recrée la table famille
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
			execCallableStatement(connection, "create table famille(fam_id BIGINT , LIBELLE varchar(255));");
			execCallableStatement(connection, "create sequence SEQ_FAMILLE start with 10001 increment by 1");

			execCallableStatement(connection, "create table fam_car_location(fam_id BIGINT , ID BIGINT);");

			execCallableStatement(connection, "create table car(ID BIGINT, FAM_ID BIGINT, MAKE varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MOTOR_TYPE varchar(50) );");
			execCallableStatement(connection, "create sequence SEQ_CAR start with 10001 increment by 1");

			execCallableStatement(connection, "create table VX_FILE_INFO(FIL_ID BIGINT , FILE_NAME varchar(255), MIME_TYPE varchar(255), LENGTH BIGINT, LAST_MODIFIED date, FILE_DATA BLOB);");
			execCallableStatement(connection, "create sequence SEQ_VX_FILE_INFO start with 10001 increment by 1");
			transaction.commit();
		}

		//On supprime tout
		doRemove("*:*");

		final CarDataBase carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		initialDbCarSize = carDataBase.size();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Car car : carDataBase.createList()) {
				car.setId(null);
				storeManager.getDataStore().create(car);
			}
			transaction.commit();
		}
		waitIndexation();

	}

	/** {@inheritDoc} */
	@Override
	protected void doTearDown() throws Exception {
		if (dataBaseManager != null) {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				//A chaque fin de test on arréte la base.
				final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
				execCallableStatement(connection, "shutdown;");
				transaction.commit();
			}
		}
	}

	private void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		try (final SqlCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql)) {
			callableStatement.init();
			callableStatement.executeUpdate();
		}
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexAllQuery() {
		final long size = query("*:*");
		Assert.assertEquals(initialDbCarSize, size);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexNewData() {
		testIndexAllQuery();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(initialDbCarSize + 1, query("*:*"));
		Assert.assertEquals(1, query("DESCRIPTION:légende"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexDeleteData() {
		testIndexAllQuery();
		Assert.assertEquals(1, query("ID:10001"));

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().delete(createURI(10001L));
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(0, query("ID:10001"));
		Assert.assertEquals(initialDbCarSize - 1, query("*:*"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexUpdateData() {
		testIndexAllQuery();
		final Car car = createNewCar();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(car);
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(initialDbCarSize + 1, query("*:*"));
		Assert.assertEquals(1, query("DESCRIPTION:légende"));

		car.setDescription("Vendue");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().update(car);
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(initialDbCarSize + 1, query("*:*"));
		Assert.assertEquals(0, query("DESCRIPTION:légende"));
		Assert.assertEquals(1, query("DESCRIPTION:vendue"));
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveAll() {
		//On supprime tout
		doRemove("*:*");
		final long resize = query("*:*");
		Assert.assertEquals(0L, resize);
	}

	private static Car createNewCar() {
		final Car car = new Car();
		car.setId(null);
		car.setPrice(12000);
		car.setMake("Acme");
		car.setModel("Martin");
		car.setYear(1978);
		car.setKilo(1500);
		final BigDecimal conso = new BigDecimal(7.6);
		conso.setScale(2, RoundingMode.HALF_UP);
		car.setConsommation(conso);
		car.setMotorType("essence");
		car.setDescription("Voiture de légende assurant une reindexation dès son insertion");
		return car;
	}

	private long query(final String query) {
		//recherche
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private FacetedQueryResult<Car, SearchQuery> doQuery(final SearchQuery searchQuery, final DtListState listState) {
		return searchManager.loadList(carIndexDefinition, searchQuery, listState);
	}

	private void doRemove(final String query) {
		final ListFilter removeQuery = new ListFilter(query);
		searchManager.removeAll(carIndexDefinition, removeQuery);
	}

	private static URI createURI(final long carId) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
		return new URI(dtDefinition, carId);
	}

	private static void waitIndexation() {
		try {
			Thread.sleep(5000 + 1500); //wait index was done
		} catch (final InterruptedException e) {
			//rien
		}
	}

}
