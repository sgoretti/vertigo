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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DSLDefinitionBody;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DSLDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DSLPropertyEntry;

import java.util.ArrayList;
import java.util.List;

final class DSLInnerDefinitionRule extends AbstractRule<DSLDefinitionEntry, List<?>> {
	private final DynamicDefinitionRepository dynamicModelRepository;
	private final String entityName;
	private final Entity entity;

	DSLInnerDefinitionRule(final DynamicDefinitionRepository dynamicModelRepository, final String entityName, final Entity entity) {
		Assertion.checkNotNull(dynamicModelRepository);
		Assertion.checkArgNotEmpty(entityName);
		Assertion.checkNotNull(entity);
		//-----------------------------------------------------------------
		this.dynamicModelRepository = dynamicModelRepository;
		this.entityName = entityName;
		this.entity = entity;

	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final DSLDefinitionBodyRule definitionBodyRule = new DSLDefinitionBodyRule(dynamicModelRepository, entity);
		return new SequenceRule(//"InnerDefinition"
				new TermRule(entityName), //
				DSLSyntaxRules.SPACES,//
				DSLSyntaxRules.WORD,//2
				DSLSyntaxRules.SPACES,//
				definitionBodyRule,//4
				DSLSyntaxRules.SPACES,//
				new OptionRule<>(DSLSyntaxRules.OBJECT_SEPARATOR)//
		);
	}

	@Override
	protected DSLDefinitionEntry handle(final List<?> parsing) {
		//Dans le cas des sous définition :: field [PRD_XXX]

		final String definitionName = (String) parsing.get(2);
		final DSLDefinitionBody definitionBody = (DSLDefinitionBody) parsing.get(4);

		final DynamicDefinitionBuilder dynamicDefinitionBuilder = dynamicModelRepository.createDynamicDefinitionBuilder(definitionName, entity, null);
		populateDefinition(definitionBody, dynamicDefinitionBuilder);

		//---
		return new DSLDefinitionEntry(entityName, dynamicDefinitionBuilder.build());
	}

	/**
	 * Peuple la définition à partir des éléments trouvés.
	 */
	private static void populateDefinition(final DSLDefinitionBody definitionBody, final DynamicDefinitionBuilder dynamicDefinitionBuilder) {
		for (final DSLDefinitionEntry fieldDefinitionEntry : definitionBody.getDefinitionEntries()) {
			// ------------------------------------------------------------------
			// 1.On vérifie que le champ existe pour la metaDefinition
			// et qu'elle n'est pas déjà enregistrée sur l'objet.
			// ------------------------------------------------------------------
			if (fieldDefinitionEntry.containsDefinition()) {
				// On ajoute la définition par sa valeur.
				dynamicDefinitionBuilder.withChildDefinition(fieldDefinitionEntry.getFieldName(), fieldDefinitionEntry.getDefinition());
			} else {
				// On ajoute les définitions par leur clé.
				dynamicDefinitionBuilder.withDefinitions(fieldDefinitionEntry.getFieldName(), toDefinitionKeys(fieldDefinitionEntry.getDefinitionKeys()));
			}
		}
		for (final DSLPropertyEntry fieldPropertyEntry : definitionBody.getPropertyEntries()) {
			//			// On vérifie que la propriété est enregistrée sur la metaDefinition
			//			Assertion.precondition(definition.getEntity().getPropertySet().contains(fieldPropertyEntry.getProperty()), "Propriété {0} non enregistré sur {1}",
			//					fieldPropertyEntry.getProperty(), definition.getEntity().getName());
			//			// ------------------------------------------------------------------
			final Object value = readProperty(fieldPropertyEntry.getProperty(), fieldPropertyEntry.getPropertyValueAsString());
			dynamicDefinitionBuilder.withPropertyValue(fieldPropertyEntry.getProperty(), value);
		}
	}

	//=========================================================================
	//=================================STATIC==================================
	//=========================================================================

	/**
	 * Retourne la valeur typée en fonction de son expression sous forme de String
	 * L'expression est celle utilisée dans le fichier xml/ksp.
	 * Cette méthode n'a pas besoin d'être optimisée elle est appelée au démarrage uniquement.
	 * @param property Propriété à lire.
	 * @param stringValue Valeur de la propriété sous forme String
	 * @return J Valeur typée de la propriété
	 */
	private static Object readProperty(final EntityProperty property, final String stringValue) {
		Assertion.checkNotNull(property);
		//---------------------------------------------------------------------
		return property.getPrimitiveType().cast(stringValue);
	}

	private static List<DynamicDefinitionKey> toDefinitionKeys(final List<String> list) {
		final List<DynamicDefinitionKey> definitionKeyList = new ArrayList<>();
		for (final String item : list) {
			definitionKeyList.add(new DynamicDefinitionKey(item));
		}
		return definitionKeyList;
	}

}