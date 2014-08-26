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
package io.vertigo.dynamo.plugins.export.csv;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportDtParametersReadable;
import io.vertigo.dynamo.export.ExportField;
import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.kernel.lang.Assertion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Export avec ETAT.
 * 
 * @author pchretien, npiedeloup
 */
final class CSVExporter {
	/**
	 * Séparateur csv : par défaut ";".
	 */
	private static final String SEPARATOR = ";";

	/**
	 * Caractère de fin de ligne
	 */
	private static final String END_LINE = "" + (char) 13 + (char) 10;

	/**
	 * Encoder CSV
	 */
	private final Encoder<String, String> csvEncoder;

	private final Map<DtField, Map<Object, String>> referenceCache = new HashMap<>();
	private final Map<DtField, Map<Object, String>> denormCache = new HashMap<>();
	private final ExportHelper exportHelper;

	/**
	 * Constructeur.
	 * @param codecManager Manager des codecs
	 * @param exportHelper Helper d'export.
	 */
	CSVExporter(final CodecManager codecManager, final ExportHelper exportHelper) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkNotNull(exportHelper);
		//---------------------------------------------------------------------
		csvEncoder = codecManager.getCsvEncoder();
		this.exportHelper = exportHelper;
	}

	/**
	 * Méthode principale qui gère l'export d'un tableau vers un fichier CVS.
	 * On ajoute le BOM UTF8 si le fichier est généré en UTF-8 pour une bonne ouverture dans Excel.
	 * @param documentParameters Paramètres du document à exporter
	 * @param out Flux de sortie
	 * @throws IOException Exception d'ecriture
	 */
	void exportData(final Export documentParameters, final OutputStream out) throws IOException {
		final Charset charset = Charset.forName("UTF-8");
		try (final Writer writer = new OutputStreamWriter(out, charset.name())) {
			// on met le BOM UTF-8 afin d'avoir des ouvertures correctes avec excel
			writer.append('\uFEFF');
			final boolean isMultiData = documentParameters.getReportDataParameters().size() > 1;
			for (final ExportDtParametersReadable resourceParams : documentParameters.getReportDataParameters()) {
				exportHeader(resourceParams, writer);
				exportData(resourceParams, writer);
				if (isMultiData) {
					writer.write("\"\"");
					writer.write(END_LINE);
				}
			}
		}
	}

	/**
	 * Réalise l'export des données d'en-tête.
	 * @param parameters de cet export
	 * @param out Le flux d'écriture des données exportées.
	 * @throws IOException Exception lors de l'écriture dans le flux.
	 */
	private void exportHeader(final ExportDtParametersReadable parameters, final Writer out) throws IOException {
		final String title = parameters.getTitle();
		if (title != null) {
			out.write(encodeString(title));
			out.write(END_LINE);
		}

		String sep = "";
		for (final ExportField exportColumn : parameters.getExportFields()) {
			out.write(sep);
			out.write(encodeString(exportColumn.getLabel().getDisplay()));
			sep = SEPARATOR;
		}
		out.write(END_LINE);
	}

	/**
	 * Réalise l'export des données de contenu.
	 * @param parameters de cet export
	 * @param out Le flux d'écriture des données exportées.
	 * @throws IOException Exception lors de l'écriture dans le flux.
	 */
	private void exportData(final ExportDtParametersReadable parameters, final Writer out) throws IOException {
		// Parcours des DTO de la DTC
		if (parameters.hasDtObject()) {
			exportLine(parameters.getDtObject(), parameters, out);
		} else {
			for (final DtObject dto : parameters.getDtList()) {
				exportLine(dto, parameters, out);
			}
		}
	}

	private void exportLine(final DtObject dto, final ExportDtParametersReadable parameters, final Writer out) throws IOException {
		String sep = "";
		String sValue;
		for (final ExportField exportColumn : parameters.getExportFields()) {
			final DtField dtField = exportColumn.getDtField();
			out.write(sep);
			sValue = exportHelper.getText(referenceCache, denormCache, dto, exportColumn);
			// si toutes les colonnes de cette ligne sont vides,
			// on n'obtient pas une ligne correctement formatée ...
			if ("".equals(sValue)) {
				sValue = " ";
			}
			if (dtField.getDomain().getDataType() == DataType.BigDecimal) {
				out.write(encodeNumber(sValue));
			} else {
				out.write(encodeString(sValue));
			}
			sep = SEPARATOR;
		}
		out.write(END_LINE);
	}

	/**
	 * Encode la chaîne exportée en csv.
	 *
	 * @param str La chaîne à encoder.
	 * @return La chaîne encodée.
	 */
	private String encodeString(final String str) {
		return '\"' + csvEncoder.encode(str) + '\"';

	}

	/**
	 * Encode la chaîne exportée en csv.
	 *
	 * @param str La chaîne à encoder.
	 * @return La chaîne encodée.
	 */
	private String encodeNumber(final String str) {
		return encodeString(str).replace('.', ',');
	}

}
