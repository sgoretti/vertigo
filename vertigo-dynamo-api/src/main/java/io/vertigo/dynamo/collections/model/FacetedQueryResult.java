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
package io.vertigo.dynamo.collections.model;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Résultat de la recherche.
 * Tout résultat est facetté.
 * Eventuellement il n'y a aucune facette.
 * @author pchretien, dchallas
 * @param <R> Type de l'objet resultant de la recherche
 * @param <S> Type de l'objet source
 */
public final class FacetedQueryResult<R extends DtObject, S> implements Serializable {
	private static final long serialVersionUID = 1248453191954177054L;

	private final DtList<R> dtc;
	private final List<Facet> facets;
	private final Map<R, Map<DtField, String>> highlights;
	private final Map<FacetValue, DtList<R>> clusteredDtc;
	private final long count;
	private final S source;
	private final Option<FacetedQuery> query;

	/**
	 * Constructeur.
	 * @param query Facettage de la requète
	 * @param count  Nombre total de résultats
	 * @param dtc DTC résultat, éventuellement tronquée à n (ex 500) si trop d'éléments.
	 * @param facets Liste des facettes. (Peut être vide jamais null)
	 * @param clusteredDtc Cluster des documents. (Peut être vide jamais null)
	 * @param highlights Liste des extraits avec mise en valeur par objet et par champs
	 * @param source Object source permettant rerentrer dans le mechanisme de filtrage
	 */
	public FacetedQueryResult(final Option<FacetedQuery> query, final long count, final DtList<R> dtc, final List<Facet> facets, final Map<FacetValue, DtList<R>> clusteredDtc, final Map<R, Map<DtField, String>> highlights, final S source) {
		Assertion.checkNotNull(query);
		Assertion.checkNotNull(dtc);
		Assertion.checkNotNull(facets);
		Assertion.checkNotNull(source);
		Assertion.checkNotNull(clusteredDtc);
		Assertion.checkNotNull(highlights);
		//-----
		this.query = query;
		this.count = count;
		this.dtc = dtc;
		this.facets = facets;
		this.clusteredDtc = clusteredDtc;
		this.highlights = highlights;
		this.source = source;
	}

	/**
	 * @return Nombre total de résultats
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Rappel des facettes de la requête initiale.
	 * @return Facettes de requète
	 */
	public Option<FacetedQuery> getFacetedQuery() {
		return query;
	}

	/**
	 * @return DTC résultat, éventuellement tronquée à n (ex 500) si trop d'éléments.
	 */
	public DtList<R> getDtList() {
		return dtc;
	}

	/**
	 * @return Liste des facettes. (Peut être vide jamais null)
	 */
	public List<Facet> getFacets() {
		return facets;
	}

	/**
	 * @return Cluster des documents par valeur de facette, si demandé lors de la requête. (Peut être vide jamais null)
	 */
	public Map<FacetValue, DtList<R>> getClusters() {
		return clusteredDtc;
	}

	/**
	 * @param document Document dont on veut les highlights
	 * @return Extrait avec mise en valeur par champs. (Peut être vide jamais null)
	 */
	public Map<DtField, String> getHighlights(final R document) {
		final Map<DtField, String> documentHightlights = highlights.get(document);
		return documentHightlights != null ? documentHightlights : Collections.<DtField, String> emptyMap();
	}

	/**
	 * @return Object source permettant réentrer dans le mécanisme de filtrage.
	 */
	public S getSource() {
		return source;
	}
}
