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
package io.vertigo.vega.plugins.webservice.servlet;

import io.vertigo.commons.impl.config.ConfigPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Plugin d'accès à la configuration de la WebApp.
 * @author npiedeloup
 */
public final class WebAppContextConfigPlugin implements ConfigPlugin {
	private static Properties properties;

	/**
	 * @param initConf Configuration initiale
	 */
	public static void setInitConfig(final Properties initConf) {
		Assertion.checkNotNull(initConf);
		//-----
		WebAppContextConfigPlugin.properties = initConf;
	}

	private final String managedConfigPath;

	/**
	 * Constructeur.
	 * @param configPath Nom de la config initial
	 */
	@Inject
	public WebAppContextConfigPlugin(@Named("configPath") final String configPath) {
		Assertion.checkArgNotEmpty(configPath);
		//-----
		managedConfigPath = configPath;
	}

	/** {@inheritDoc} */
	@Override
	public Option<String> getValue(final String configPath, final String property) {
		Assertion.checkArgNotEmpty(configPath);
		Assertion.checkArgNotEmpty(property);
		//-----
		return managedConfigPath.equals(configPath) ? Option.<String> option(properties.getProperty(property)) : Option.<String> none();
	}
}
