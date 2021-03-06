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
package io.vertigo.dynamo.impl.database.vendor.h2;

import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.impl.database.vendor.core.AbstractSqlExceptionHandler;

import java.sql.SQLException;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour H2.
 *
 * @author jmainaud
 */
final class H2SqlExceptionHandler extends AbstractSqlExceptionHandler {

	/** Champ DUPLICATE_KEY_1. */
	private static final int DUPLICATE_KEY_1 = 23001;
	/** Champ REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1. */
	private static final int REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1 = 23003;
	/** Champ REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1. */
	private static final int REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1 = 23002;
	/** Champ VALUE_TOO_LARGE_FOR_PRECISION_1. */
	private static final int VALUE_TOO_LARGE_FOR_PRECISION_1 = 90039;
	/** Champ VALUE_TOO_LONG_2. */
	private static final int VALUE_TOO_LONG_2 = 90005;

	/**
	 * Crée une nouvelle instance de H2SqlExceptionHandler.
	 */
	H2SqlExceptionHandler() {
		super();
	}

	/**
	 * Gestionnaire des erreurs de la base de données pour PostgreSql.
	 *
	 * @param sqle Exception Sql
	 * @param statement Requête en erreur.
	 */
	@Override
	public void handleSQLException(final SQLException sqle, final SqlPreparedStatement statement) {
		final int errCode = sqle.getErrorCode();
		switch (errCode) {
			case VALUE_TOO_LONG_2:
			case VALUE_TOO_LARGE_FOR_PRECISION_1:
				// Valeur trop grande pour ce champs
				handleTooLargeValueSqlException(sqle);
				break;
			case REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1:
			case REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1:
				// Violation de contrainte d'intégrité référentielle
				handleForeignConstraintSQLException(sqle);
				break;
			case DUPLICATE_KEY_1:
				// Violation de contrainte d'unicité
				handleUniqueConstraintSQLException(sqle);
				break;
			default:
				// Message d'erreur par défaut
				handleOtherSQLException(sqle, statement);
				break;
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void handleOtherSQLException(final SQLException sqle, final SqlPreparedStatement statement) {
		final int errCode = sqle.getErrorCode();
		throw new RuntimeException("[Erreur SQL](" + errCode + ") : " + sqle.getMessage() + '\n' + statement, sqle);
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String msg) {
		return "";
	}

	/*
	 * Exemple de messages d'erreur.
	 *
	 * VALUE_TOO_LONG_2: Value too long for column "NAME VARCHAR(2)": "'Hello' (5)"; SQL statement: INSERT INTO TEST VALUES(1, 'Hello')
	 * [90005-147]
	 *
	 * VALUE_TOO_LARGE_FOR_PRECISION_1: The value is too large for the precision "2"; SQL statement: SELECT * FROM TABLE(X DECIMAL(2, 2) =
	 * (123.34)) [90039-147]
	 *
	 * REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1: Referential integrity constraint violation:
	 * "CONSTRAINT_3: PUBLIC.CHILD FOREIGN KEY(P_ID) REFERENCES PUBLIC.PARENT(ID)"; SQL statement: INSERT INTO CHILD VALUES(1) [23002-147]
	 *
	 * REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1: Referential integrity constraint violation:
	 * "CONSTRAINT_3: PUBLIC.CHILD FOREIGN KEY(P_ID) REFERENCES PUBLIC.PARENT(ID)"; SQL statement: DELETE FROM PARENT [23003-147]
	 *
	 * DUPLICATE_KEY_1: Unique index or primary key violation: "PRIMARY KEY ON PUBLIC.TEST(ID)"; SQL statement: INSERT INTO TEST VALUES(1)
	 * [23001-147]
	 */

}
