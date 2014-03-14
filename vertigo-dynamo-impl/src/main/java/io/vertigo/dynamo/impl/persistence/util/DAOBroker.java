package io.vertigo.dynamo.impl.persistence.util;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.persistence.Broker;
import io.vertigo.dynamo.persistence.BrokerNN;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.dynamo.persistence.FilterCriteria;
import io.vertigo.dynamo.persistence.FilterCriteriaBuilder;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour accéder au Broker.
 * 
 * @author cgodard
 * @version $Id: DAOBroker.java,v 1.6 2014/01/20 17:49:32 pchretien Exp $
 * @param <D> Type d'objet métier.
 * @param <P> Type de la clef primaire.
 */
public class DAOBroker<D extends DtObject, P> implements BrokerNN {
	/** DT de l'objet dont on gére le CRUD. */
	private final DtDefinition dtDefinition;
	private final Broker broker;
	private final BrokerNN brokerNN;

	/**
	 * Contructeur.
	 * 
	 * @param dtObjectClass Définition du DtObject associé à ce DAOBroker
	 * @param persistenceManager Manager de gestion de la persistance
	 */
	public DAOBroker(final Class<? extends DtObject> dtObjectClass, final PersistenceManager persistenceManager) {
		this(DtObjectUtil.findDtDefinition(dtObjectClass), persistenceManager);
	}

	/**
	 * Contructeur.
	 * 
	 * @param dtDefinition Définition du DtObject associé à ce DAOBroker
	 * @param persistenceManager Manager de gestion de la persistance
	 */
	public DAOBroker(final DtDefinition dtDefinition, final PersistenceManager persistenceManager) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(persistenceManager);
		// ---------------------------------------------------------------------
		broker = persistenceManager.getBroker();
		brokerNN = persistenceManager.getBrokerNN();
		this.dtDefinition = dtDefinition;
	}

	/**
	 * Sauvegarde d'un objet. Création (insert) ou mise à jour (update) en fonction du fait que l'objet existe ou pas
	 * déjà en base.
	 * 
	 * @param dto Objet à sauvegarder
	 */
	public final void save(final D dto) {
		broker.save(dto);
	}

	/**
	 * Suppression d'un objet persistant par son URI.
	 * 
	 * @param uri URI de l'objet à supprimer
	 */
	public final void delete(final URI<D> uri) {
		broker.delete(uri);
	}

	/**
	 * Suppression d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 * 
	 * @param id identifiant de l'objet persistant à supprimer
	 */
	public final void delete(final P id) {
		delete(createDtObjectURI(id));
	}

	/**
	 * Récupération d'un objet persistant par son URI. L'objet doit exister.
	 * 
	 * @param uri URI de l'objet à récupérer
	 * @return D Object recherché
	 */
	public final D get(final URI<D> uri) {
		return broker.<D> get(uri);
	}

	/**
	 * Récupération d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 * 
	 * @param id identifiant de l'objet persistant recherché
	 * @return D Object objet recherché
	 */
	public final D get(final P id) {
		return get(createDtObjectURI(id));
	}

	/**
	 * Retourne l'URI de DtObject correspondant à une URN de définition et une valeur d'URI donnés.
	 * 
	 * @param id identifiant de l'objet persistant recherché
	 * @return URI recherchée
	 */
	private URI<D> createDtObjectURI(final P id) {
		return new URI<>(dtDefinition, id);
	}

	/**
	 * @param fieldName de l'object à récupérer NOT NULL
	 * @param value de l'object à récupérer NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<D> getListByDtField(final String fieldName, final Object value, final int maxRows) {
		final FilterCriteria<D> criteria = new FilterCriteriaBuilder<D>().withFilter(fieldName, value).build();
		// Verification de la valeur est du type du champ
		dtDefinition.getField(fieldName).getDomain().getDataType().checkValue(value);
		return broker.<D> getList(dtDefinition, criteria, maxRows);
	}

	/**
	 * @param criteria Critére de recherche NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<D> getList(final Criteria<D> criteria, final int maxRows) {
		return broker.<D> getList(dtDefinition, criteria, maxRows);
	}

	/** {@inheritDoc} */
	public final void removeAllNN(final DtListURIForAssociation dtListURI) {
		brokerNN.removeAllNN(dtListURI);
	}

	/** {@inheritDoc} */
	public final void removeNN(final DtListURIForAssociation dtListURI, final URI<DtObject> uriToDelete) {
		brokerNN.removeNN(dtListURI, uriToDelete);
	}

	/**
	 * Mise à jour des associations n-n.
	 * 
	 * @param <FK> <FK extends DtObject>
	 * @param dtListURI DtList de référence
	 * @param newDtc DtList modifiée
	 */
	public final <FK extends DtObject> void updateNN(final DtListURIForAssociation dtListURI, final DtList<FK> newDtc) {
		Assertion.checkNotNull(newDtc);
		// ---------------------------------------------------------------------
		final List<URI<? extends DtObject>> objectURIs = new ArrayList<>();
		for (final FK dto : newDtc) {
			objectURIs.add(createURI(dto));
		}
		updateNN(dtListURI, objectURIs);
	}

	/** {@inheritDoc} */
	public final void updateNN(final DtListURIForAssociation dtListURI, final List<URI<? extends DtObject>> newUriList) {
		brokerNN.updateNN(dtListURI, newUriList);
	}

	/** {@inheritDoc} */
	public final void appendNN(final DtListURIForAssociation dtListURI, final URI<DtObject> uriToAppend) {
		brokerNN.appendNN(dtListURI, uriToAppend);
	}

	/**
	 * Ajout un objet à la collection existante.
	 * 
	 * @param dtListURI DtList de référence
	 * @param dtoToAppend Objet à ajout à la NN
	 */
	public final void appendNN(final DtListURIForAssociation dtListURI, final DtObject dtoToAppend) {
		brokerNN.appendNN(dtListURI, createURI(dtoToAppend));
	}

	private static <D extends DtObject> URI<D> createURI(final D dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		return new URI<>(dtDefinition, DtObjectUtil.getId(dto));
	}

	/**
	 * Sauvegarde des associations n-n.
	 * 
	 * @param dtc DtList initiale chargée à partir du DAO pour obtenir les méta-données qui indiquent sur quel
	 *        objet on a la relation n-n (utiliser un "getCollection()").
	 * @param newDtc DtList
	 * @param <FK> Objet en Foreign Key
	 * @deprecated utiliser updateNN() # l'URI de la collection (getXXXCollection -> getXXXCollectionURI())
	 */
	@Deprecated
	public final <FK extends DtObject> void putNN(final DtList<FK> dtc, final DtList<FK> newDtc) {
		updateNN(DtListURIForAssociation.class.cast(dtc.getURI()), newDtc);
	}

	/**
	 * Récupération une liste filtrée par le champ saisie dans le dtoCritère.
	 * @param dtoCritere les criteres
	 * @param maxRows Nombre maximum de ligne
	 * @return Collection de DtObject
	 * @deprecated utiliser getList(Criteria criteria)
	 */
	@Deprecated
	public final DtList<D> getList(final DtObject dtoCritere, final int maxRows) {
		final DtListURI collectionURI = new DtListURIForCriteria<D>(dtDefinition, dtoCritere, maxRows);
		Assertion.checkNotNull(collectionURI);
		return broker.getList(collectionURI);
	}
}