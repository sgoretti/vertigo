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
package io.vertigo.studio.plugins.mda.file;

import io.vertigo.core.Home;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileConfig;
import io.vertigo.util.MapBuilder;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Génération des objets relatifs au module File.
 *
 * @author npiedeloup
 */
public final class FileInfoGeneratorPlugin extends AbstractGeneratorPlugin {
	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public FileInfoGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir) {
		//-----
		this.targetSubDir = targetSubDir;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileConfig fileInfoConfiguration, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(fileInfoConfiguration);
		Assertion.checkNotNull(resultBuilder);
		//-----
		/* Générations des FI. */
		generateFileInfos(targetSubDir, fileInfoConfiguration, resultBuilder);
	}

	private static void generateFileInfos(final String targetSubDir, final FileConfig fileInfoConfig, final ResultBuilder resultBuilder) {
		final Collection<FileInfoDefinition> fileInfoDefinitions = Home.getDefinitionSpace().getAll(FileInfoDefinition.class);
		for (final FileInfoDefinition fileInfoDefinition : fileInfoDefinitions) {
			generateFileInfo(targetSubDir, fileInfoConfig, resultBuilder, fileInfoDefinition);
		}
	}

	private static void generateFileInfo(final String targetSubDir, final FileConfig fileInfoConfiguration, final ResultBuilder resultBuilder, final FileInfoDefinition fileInfoDefinition) {
		final TemplateFileInfoDefinition definition = new TemplateFileInfoDefinition(fileInfoDefinition);

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("fiDefinition", definition)
				.put("packageName", fileInfoConfiguration.getProjectPackageName() + ".fileinfo")
				.build();

		createFileGenerator(fileInfoConfiguration, mapRoot, definition.getClassSimpleName(), targetSubDir, fileInfoConfiguration.getProjectPackageName() + ".fileinfo", ".java", "file/fileInfo.ftl")
				.generateFile(resultBuilder);
	}
}
