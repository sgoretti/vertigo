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
package io.vertigo.core;

import io.vertigo.core.config.BootConfig;
import io.vertigo.core.config.LogConfig;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Engine;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author pchretien
 */
final class Boot implements Activeable {
	private final BootConfig bootConfig;

	Boot(final BootConfig bootConfig) {
		Assertion.checkNotNull(bootConfig);
		//-----
		this.bootConfig = bootConfig;
		//-----
		if (bootConfig.getLogConfig().isDefined()) {
			initLog(bootConfig.getLogConfig().get());
		}
	}

	@Override
	public void start() {
		startEngines();
	}

	private void startEngines() {
		for (final Engine engine : bootConfig.getEngines()) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).start();
			}
		}
	}

	@Override
	public void stop() {
		stopEngines();
	}

	private void stopEngines() {
		final List<Engine> reverseEngines = new ArrayList<>(bootConfig.getEngines());
		java.util.Collections.reverse(reverseEngines);

		for (final Engine engine : reverseEngines) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).stop();
			}
		}
	}

	private static void initLog(final LogConfig log4Config) {
		Assertion.checkNotNull(log4Config);
		//-----
		final String log4jFileName = log4Config.getFileName();
		Assertion.checkArgument(log4jFileName.endsWith(".xml"), "Use the XML format for log4j configurations (instead of : {0}).", log4jFileName);
		final URL url = Home.class.getResource(log4jFileName);
		if (url != null) {
			DOMConfigurator.configure(url);
			Logger.getRootLogger().info("Log4J configuration chargée (resource) : " + url.getFile());
		} else {
			Assertion.checkArgument(new File(log4jFileName).exists(), "Fichier de configuration log4j : {0} est introuvable", log4jFileName);
			// Avec configureAndWatch (utilise un anonymous thread)
			// on peut modifier à chaud le fichier de conf log4j
			// mais en cas de hot-deploy, le thread reste présent ce qui peut-entrainer des problèmes.
			DOMConfigurator.configureAndWatch(log4jFileName);
		}
		Logger.getRootLogger().info("Log4J configuration chargée (fichier) : " + log4jFileName);
	}

}
