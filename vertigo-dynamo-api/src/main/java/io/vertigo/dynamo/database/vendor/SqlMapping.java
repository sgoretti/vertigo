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
package io.vertigo.dynamo.database.vendor;

import io.vertigo.dynamo.domain.metamodel.DataType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface centralisant les mappings à la BDD.
 *
 * @author pchretien
 */
public interface SqlMapping {
	/**
	 * Retourne le type vertigo 'DataType' correspondant à un type sql.
	 *
	 * @param sqlType Type SQL
	 * @return Type Vertigo correspondant
	 */
	DataType getDataType(int sqlType);

	/**
	 * Retourne le type SQL correspondant à un type vertigo 'DataType'.
	 *
	 * @param dataType Type Vertigo primitif
	 * @return Type SQL correspondant à un type
	 */
	int getSqlType(DataType dataType);

	/**
	 * Affecte les valeurs sur un statement.
	 *
	 * @param statement Statement SQL à affecter
	 * @param index Index de la variable dans le statement
	 * @param dataType Type primitif
	 * @param value Valeur à affecter sur le statement à l'index indiqué
	 * @throws SQLException Exception sql
	 */
	void setValueOnStatement(PreparedStatement statement, int index, DataType dataType, Object value) throws SQLException;

	/**
	 * Retourne la valeur typée vertigo d'un callablestatement.
	 *
	 * @param callableStatement CallableStatement SQL à affecter
	 * @param index Indexe de la variable dans le statement
	 * @param dataType Type primitif vertigo
	 * @return Valeur obtenue par le CallableStatement à l'indexe indiqué
	 * @throws SQLException Exception sql
	 */
	Object getValueForCallableStatement(CallableStatement callableStatement, int index, DataType dataType) throws SQLException;

	/**
	 * Retourne la valeur typée vertigo d'un resultSet.
	 *
	 * @param rs ResultSet
	 * @param col Indexe de la colonne
	 * @param dataType Type primitif
	 * @return Valeur typée d'un resultSet
	 * @throws SQLException Exception sql
	 */
	Object getValueForResultSet(ResultSet rs, int col, DataType dataType) throws SQLException;
}
