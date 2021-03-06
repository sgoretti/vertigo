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
package io.vertigo.dynamo.store.kvstore;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.kvstore.KVStore;
import io.vertigo.dynamo.store.kvstore.data.Flower;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Option;
import io.vertigo.util.ListBuilder;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public final class KVStoreManagerTest extends AbstractTestCaseJU4 {
	private static final String DEFAULT_DATA_STORE_NAME = "default";
	@Inject
	private StoreManager storeManager;
	@Inject
	private VTransactionManager transactionManager;

	private static Flower buildFlower(final String name, final double price) {
		final Flower flower = new Flower();
		flower.setName(name);
		flower.setPrice(price);
		return flower;
	}

	@Test
	public void testFind() {
		final KVStore kvStore = storeManager.getKVStore();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Option<Flower> foundFlower = kvStore.find(DEFAULT_DATA_STORE_NAME, "1", Flower.class);
			Assert.assertTrue(foundFlower.isEmpty());
			final Flower tulip = buildFlower("tulip", 100);

			kvStore.put(DEFAULT_DATA_STORE_NAME, "1", tulip);
			final Option<Flower> foundFlower2 = kvStore.find(DEFAULT_DATA_STORE_NAME, "1", Flower.class);
			Assert.assertTrue(foundFlower2.isDefined());
			Assert.assertEquals("tulip", foundFlower2.get().getName());
			Assert.assertEquals(100d, foundFlower2.get().getPrice(), 0); //"Price must be excatly 100",
		}
	}

	@Test
	public void testFindAll() {
		final KVStore kvStore = storeManager.getKVStore();
		final List<Flower> flowers = new ListBuilder<Flower>()
				.add(buildFlower("daisy", 60))
				.add(buildFlower("tulip", 100))
				.add(buildFlower("rose", 110))
				.add(buildFlower("lily", 120))
				.add(buildFlower("orchid", 200))
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final List<Flower> foundFlowers = kvStore.findAll(DEFAULT_DATA_STORE_NAME, 0, null, Flower.class);
			Assert.assertTrue(foundFlowers.isEmpty());

			int i = 0;
			for (final Flower flower : flowers) {
				final String id = "" + i++;
				kvStore.put(DEFAULT_DATA_STORE_NAME, id, flower);

			}

			final List<Flower> foundFlowers2 = kvStore.findAll(DEFAULT_DATA_STORE_NAME, 0, 1000, Flower.class);
			Assert.assertEquals(flowers.size(), foundFlowers2.size());
			transaction.commit();
		}
	}

	@Test(expected = RuntimeException.class)
	public void testRemoveFail() {
		final KVStore kvStore = storeManager.getKVStore();
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			kvStore.remove(DEFAULT_DATA_STORE_NAME, "1");
		}
	}

	@Test
	public void testRemove() {
		final KVStore kvStore = storeManager.getKVStore();
		testFindAll();
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final List<Flower> foundFlowers = kvStore.findAll(DEFAULT_DATA_STORE_NAME, 0, 1000, Flower.class);
			//-----
			kvStore.remove(DEFAULT_DATA_STORE_NAME, "1");
			//-----
			final List<Flower> foundFlowers2 = kvStore.findAll(DEFAULT_DATA_STORE_NAME, 0, 1000, Flower.class);
			Assert.assertEquals(foundFlowers.size() - 1, foundFlowers2.size());
		}
	}

}
