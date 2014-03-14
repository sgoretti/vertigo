package io.vertigo.dynamo.impl.environment.kernel.impl.model;

import io.vertigo.dynamo.impl.environment.DynamicRegistry;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Espace de nommage.
 * Les éléments de la grammaire à savoir les définitions sont ajoutées à la volée.
 * Les définitions doivent respecter une métagrammaire définie préalablement dans DynamicNameSpace.
 *
 * @author  pchretien
 * @version $Id: DynamicDefinitionRepository.java,v 1.11 2013/10/22 12:29:17 pchretien Exp $
 */
public final class DynamicDefinitionRepository {

	/***
	 * On retient les définitions dans l'ordre pour
	 * créer les fichiers toujours de la même façon.
	 */
	private final Map<DynamicDefinitionKey, DynamicDefinition> definitionMap = new LinkedHashMap<>();
	private final List<DynamicDefinition> templates = new ArrayList<>();

	private final DynamicRegistry dynamicRegistry;
	private final Grammar grammar;

	/**
	 * Constructeur.
	 * @param dynamicRegistry DynamicDefinitionHandler
	 */
	public DynamicDefinitionRepository(final DynamicRegistry dynamicRegistry) {
		Assertion.checkNotNull(dynamicRegistry);
		//------------------------------------------------------------------------
		this.dynamicRegistry = dynamicRegistry;
		grammar = dynamicRegistry.getGrammar();
	}

	/**
	 * @return Grammaire
	 */
	public Grammar getGrammar() {
		return grammar;
	}

	/**
	 * Teste si une définition a déjà été ajoutée.
	 * @param definitionKey Clé de la définition
	 * @return Si la définition a déjà été enregistrée
	 */
	public boolean containsDefinitionKey(final DynamicDefinitionKey definitionKey) {
		return definitionMap.containsKey(definitionKey);
	}

	/**
	 * Récupération d'une définition par sa clé
	 *  -Soit la clé n'existe pas
	 *  -Soit la clé existe mais sans aucune définition
	 *  -Soit la clé raméne une définition.
	 *
	 * @param definitionKey Clé de la définition
	 * @return DynamicDefinition Définition correspondante ou null.
	 */
	public DynamicDefinition getDefinition(final DynamicDefinitionKey definitionKey) {
		Assertion.checkArgument(definitionMap.containsKey(definitionKey), "Aucune clé enregistrée pour :{0} parmi {1}", definitionKey, definitionMap.keySet());
		//---------------------------------------------------------------------
		final DynamicDefinition definition = definitionMap.get(definitionKey);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(definition, "Clé trouvée mais pas de définition enregistrée trouvée pour {0}", definitionKey);
		return definition;
	}

	/**
	 * Résolution des références de définitions.
	 */
	public void solve() {
		final DynamicSolver solver = new DynamicSolver();

		solveTemplates();
		final List<DynamicDefinition> orderedDefinitionList = solver.solve(this);
		registerAllDefinitions(orderedDefinitionList);
	}

	private void solveTemplates() {
		for (final DynamicDefinition template : templates) {
			((DynamicDefinitionBuilder) getDefinition(template.getDefinitionKey())).withBody(template);
		}
	}

	private void registerAllDefinitions(final List<DynamicDefinition> orderedDefinitionList) {
		for (final DynamicDefinition xdefinition : orderedDefinitionList) {
			dynamicRegistry.onDefinition(xdefinition);
			xdefinition.check();
		}
		//On sort sur la console le nombre de définitions trouvées
		//System.out.println(MessageFormat.format(">> {0} definitions trouvees.", orderedDefinitionList.size()));
	}

	/**
	 * Ajoute une définition.
	 * @param definition DynamicDefinition
	 */
	public void addDefinition(final DynamicDefinition definition) {
		Assertion.checkNotNull(definition);
		//----------------------------------------------------------------------
		put(definition.getDefinitionKey(), definition);
		dynamicRegistry.onNewDefinition(definition, this);
	}

	/**
	 * Ajoute un template.
	 * @param template DynamicDefinition
	 */
	public void addTemplate(final DynamicDefinition dynamicDefinition) {
		Assertion.checkNotNull(dynamicDefinition);
		//----------------------------------------------------------------------
		//definition.check();
		//On enregistre la définition qu'elle soit renseignée ou null.
		//---
		templates.add(dynamicDefinition);
	}

	/**
	 * Création d'une Definition (Non enregistrée !).
	 * @param packageName Nom du package
	 * @param keyName Nom de la Définition
	 * @param entity Entité
	 * @return Nouvelle Définition
	 */
	public DynamicDefinitionBuilder createDynamicDefinition(final String keyName, final Entity entity, final String packageName) {
		final DynamicDefinitionKey dynamicDefinitionKey = new DynamicDefinitionKey(keyName);
		return new DynamicDefinitionImpl(dynamicDefinitionKey, entity, packageName);
	}

	//-------------------------------------------------------------------------

	/**
	 * On ajoute une clé (non null) et sa définition (null)
	 * @param definitionKey Clé de la définition
	 * @param definition DynamicDefinition
	 */
	private void put(final DynamicDefinitionKey definitionKey, final DynamicDefinition definition) {
		//	Assertion.notNull(definition);
		Assertion.checkNotNull(definitionKey);
		if (definition != null) {
			Assertion.checkArgument(definition.getDefinitionKey().equals(definitionKey), "si la définition est renseignée la clé doit correspondre !");
		}
		//----------------------------------------------------------------------
		final DynamicDefinition previousDefinition = definitionMap.get(definitionKey);
		if (previousDefinition == null) {
			//On enregistre la définition qu'elle soit renseignée ou null.
			definitionMap.put(definitionKey, definition);
		} else {
			//On vérifie que l'on n'essaie pas d'écraser la définition déjà présente.
			Assertion.checkState(definition == null, "la définition {0} est déjà enregistrée", definitionKey);
		}
	}

	/**
	 *  @return Liste des clés orphelines.
	 */
	public Collection<DynamicDefinitionKey> getOrphanDefinitionKeys() {
		final Collection<DynamicDefinitionKey> collection = new ArrayList<>();
		for (final DynamicDefinitionKey definitionKey : definitionMap.keySet()) {
			if (definitionMap.get(definitionKey) == null) {
				collection.add(definitionKey);
			}
		}
		return Collections.unmodifiableCollection(collection);
	}

	/**
	 * @return Liste des définitions complétes
	 */
	public Collection<DynamicDefinition> getDefinitions() {
		return Collections.unmodifiableCollection(definitionMap.values());
	}
}