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
/**
 *
 */
package io.vertigo.dynamo.store.datastore;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.util.Collection;
import java.util.Map;

/**
 * Interface pour permettre des opérations avec des collections.
 *
 * @param <D> Type d'objet métier.
 * @param <P> Type de la clef primaire.
 * @author jmforhan
 */
public interface BrokerBatch<D extends DtObject, P> {

	/**
	 * Récupère la liste des objets correspondant à des ids. On n'assure pas l'ordre de la liste par rapport à l'ordre des
	 * identifiants en entré. On peut avoir également une liste plus petites s'il y a des doublons dans la liste en entrée ou que
	 * des identifiants ne correspondant à rien en base.
	 *
	 * @param idList liste des identifiants
	 * @return Liste des objets correspondants.
	 */
	DtList<D> getList(Collection<P> idList);

	/**
	 * Récupère la liste des objets correspondant à des ids et retourne sous forme de map entre la clé primaire et l'objet
	 * correspondant. Cette méthode est se base sur getList(). Il est possible qu'un id passé en entrée ne se retrouve pas comme
	 * clé de la map s'il n'y a aucun object associé à cet identifiant en base.
	 *
	 * @param idList liste des identifiants
	 * @return map entre index et l'objet associé.
	 */
	Map<P, D> getMap(Collection<P> idList);

	/**
	 * Récupère la liste des objets associé à une collection de clé étrangère.
	 *
	 * @param fieldName champ de sélection des objets à récupérer
	 * @param value collection des valeurs à utiliser pour sélectionner les objets
	 * @param <O> type de la valeur de sélection
	 * @return Liste des objets correspondants.
	 */
	<O extends Object> DtList<D> getListByField(final String fieldName, final Collection<O> value);

	/**
	 * Récupère la liste des objets associé à une collection de clé étrangère et la retourne sous forme de Map dont la clé est
	 * l'objet de sélection.
	 *
	 * @param fieldName champ de sélection des objets à récupérer
	 * @param value collection des valeurs à utiliser pour sélectionner les objets
	 * @param <O> type de la valeur de sélection
	 * @return map entre valeur de sélection et objet associé.
	 */
	<O extends Object> Map<O, DtList<D>> getMapByField(final String fieldName, final Collection<O> value);
}
