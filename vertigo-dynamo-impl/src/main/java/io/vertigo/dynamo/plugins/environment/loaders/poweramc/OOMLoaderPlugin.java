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
package io.vertigo.dynamo.plugins.environment.loaders.poweramc;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMLoader;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlLoader;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlLoaderPlugin;

import java.net.URL;

import javax.inject.Inject;

/**
 * Parser d'un fichier powerAMC, OOM.
 *
 * @author pchretien
 */
public final class OOMLoaderPlugin extends XmlLoaderPlugin {

	@Inject
	public OOMLoaderPlugin(final ResourceManager resourceManager) {
		super(resourceManager);
	}

	@Override
	protected XmlLoader createLoader(final URL url) {
		return new OOMLoader(url);
	}

	@Override
	public String getType() {
		return "oom";
	}
}
