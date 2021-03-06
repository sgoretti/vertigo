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
package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.lang.Assertion;

/**
* Class de statistique pour le suivi des traitements SQL.
*
* @author npiedeloup
*/
final class SqlStatementStatsImpl implements SqlStatementStats {
	private final SqlPreparedStatement statement;
	private long elapsedTime = -1; //non renseigné
	private boolean success; //false par défaut
	private Long nbModifiedRow;
	private Long nbSelectedRow;

	SqlStatementStatsImpl(final SqlPreparedStatement statement) {
		Assertion.checkNotNull(statement);
		//-----
		this.statement = statement;
	}

	/** {@inheritDoc} */
	@Override
	public long getElapsedTime() {
		return elapsedTime;
	}

	void setElapsedTime(final long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/** {@inheritDoc} */
	@Override
	public Long getNbModifiedRow() {
		return nbModifiedRow;
	}

	void setNbModifiedRow(final long nbModifiedRow) {
		this.nbModifiedRow = nbModifiedRow;
	}

	/** {@inheritDoc} */
	@Override
	public Long getNbSelectedRow() {
		return nbSelectedRow;
	}

	void setNbSelectedRow(final long nbSelectedRow) {
		this.nbSelectedRow = nbSelectedRow;
	}

	/** {@inheritDoc} */
	@Override
	public SqlPreparedStatement getPreparedStatement() {
		return statement;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSuccess() {
		return success;
	}

	void setSuccess(final boolean success) {
		this.success = success;
	}
}
