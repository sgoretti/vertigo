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
package io.vertigo.dynamox.commons.template.export;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.export.ExportBuilder;
import io.vertigo.dynamo.export.ExportDtParameters;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper pour les editions xls.
 * 
 * @author kleegroup
 * @param <R> Type d'objet pour la liste
 */
public class ExportXlsHelper<R extends DtObject> {
	private final ExportManager exportManager;
	private final ExportBuilder exportBuilder;

	/**
	 * Constructeur.
	 * 
	 * @param fileName nom du fichier r�sultat de l'export
	 * @param title titre de la feuille principale de l'export
	 */
	public ExportXlsHelper(final ExportManager exportManager, final String fileName, final String title) {
		Assertion.checkNotNull(exportManager);
		Assertion.checkNotNull(fileName);
		//---------------------------------------------------------------------
		this.exportManager = exportManager;
		exportBuilder = new ExportBuilder(ExportFormat.XLS, fileName)//
				.withTitle(title);
	}

	/**
	 * Prepare the export generation. If the screen allows 2 exports, then one must use 2 actions
	 * 
	 * @param dtcToExport the objects collection to be exported
	 * @param collectionColumnNameList list of the columns taht must be exported in the collection
	 * @param criterion search criterion if exists
	 * @param criterionExcludedColumnNameList list of the criteria that must be excluded for the export
	 * @param specificLabelMap map of the column names to be used instead of the default label associated with the field
	 */
	public final void prepareExport(final DtList<R> dtcToExport, final List<String> collectionColumnNameList, final DtObject criterion, final List<String> criterionExcludedColumnNameList, final Map<String, String> specificLabelMap) {

		addDtList(dtcToExport, collectionColumnNameList, specificLabelMap);

		// We add a criteria page if exists
		if (criterion != null) {
			addDtObject(criterion, criterionExcludedColumnNameList);
		}
	}

	/**
	 * Add a DTC to the export.
	 * 
	 * @param dtcToExport collection to be exported
	 * @param collectionColumnNameList names of the columns that must be exported
	 * @param specificLabelMap map of the column names to be used instead of the default label associated with the field
	 */
	public final void addDtList(final DtList<R> dtcToExport, final List<String> collectionColumnNameList, final Map<String, String> specificLabelMap) {
		Assertion.checkArgument(dtcToExport != null && dtcToExport.size() > 0, "The list of the objects to be exported must exist and not be empty");
		Assertion.checkArgument(collectionColumnNameList != null && !collectionColumnNameList.isEmpty(), "The list of the columns to be exported must exist and not be empty");

		// --------------------------------------------

		final ExportDtParameters exportListParameters = exportManager.createExportListParameters(

		dtcToExport);

		// exportListParameters.setMetaData(PublisherMetaData.TITLE, tabName);
		for (final DtField dtField : getExportColumnList(dtcToExport, collectionColumnNameList)) {
			if (specificLabelMap == null) {
				exportListParameters.addExportField(dtField);
			} else {
				// final String label = specificLabelMap.get(field.getName());
				// TODO exportListParameters.addExportField(field, label);
				exportListParameters.addExportField(dtField, null);
			}
		}
		exportBuilder.withExportDtParameters(exportListParameters);
	}

	/**
	 * Add a criterion to the export.
	 * 
	 * @param criterion criterion object to be exported
	 * @param criterionExcludedColumnNameList names of the columns to be excluded
	 */
	public final void addDtObject(final DtObject criterion, final List<String> criterionExcludedColumnNameList) {
		Assertion.checkNotNull(criterion);
		Assertion.checkArgument(criterionExcludedColumnNameList != null, "The list of the columns to be excluded must exist");

		// --------------------------------------------

		final ExportDtParameters exportObjectParameters = exportManager.createExportObjectParameters(criterion);

		// exportObjectParameters.setMetaData(PublisherMetaData.TITLE, tabName);
		for (final DtField dtField : getExportCriterionFieldList(criterion, criterionExcludedColumnNameList)) {
			exportObjectParameters.addExportField(dtField);
		}

		exportBuilder.withExportDtParameters(exportObjectParameters);
	}

	/**
	 * Traduit la liste des champs � exporter en liste de DtField.
	 * @param list Liste � exporter
	 * @param collectionColumnNameList Liste des noms de champs � exporter
	 * @return Liste des DtField correspondant
	 */
	private List<DtField> getExportColumnList(final DtList<R> list, final List<String> collectionColumnNameList) {
		final List<DtField> exportColumnList = new ArrayList<>();

		for (final String field : collectionColumnNameList) {
			exportColumnList.add(list.getDefinition().getField(field));
		}
		return exportColumnList;
	}

	/**
	 * D�termine la liste des champs du crit�re � exporter en liste de DtField.
	 * @param dto DtObject � exporter
	 * @param criterionExcludedColumnNameList Liste des noms de champs � NE PAS exporter
	 * @return Liste des DtField � exporter
	 */
	private List<DtField> getExportCriterionFieldList(final DtObject dto, final List<String> criterionExcludedColumnNameList) {
		final List<DtField> exportColumnList = new ArrayList<>();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		addFieldToExcludedExportColumnNameList(dtDefinition, criterionExcludedColumnNameList);

		for (final DtField dtField : dtDefinition.getFields()) {
			if (!criterionExcludedColumnNameList.contains(dtField.getName())) {
				exportColumnList.add(dtField);
			}
		}
		return exportColumnList;
	}

	private void addFieldToExcludedExportColumnNameList(final DtDefinition definition, final List<String> criterionExcludedColumnNameList) {
		if (definition.getIdField().isDefined()) {
			final DtField keyField = definition.getIdField().get();
			if ("DO_IDENTIFIER".equals(keyField.getDomain().getName())) {
				criterionExcludedColumnNameList.add(keyField.getName());
			}
		}
	}
}