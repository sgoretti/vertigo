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
package io.vertigo.commons.config.properties;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.config.ConfigManager;
import io.vertigo.commons.config.ServerConfig;
import io.vertigo.commons.impl.config.ConfigManagerImpl;
import io.vertigo.commons.plugins.config.properties.PropertiesConfigPlugin;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.impl.resource.ResourceManagerImpl;
import io.vertigo.core.resource.ResourceManager;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public final class PropertiesConfigManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ConfigManager configManager;

	@Override
	protected AppConfig buildAppConfig() {
		// @formatter:off
		return new AppConfigBuilder()
			.beginModule("spaces")
				.addComponent(ResourceManager.class, ResourceManagerImpl.class)
				.addPlugin( ClassPathResourceResolverPlugin.class)
				.addComponent(ConfigManager.class, ConfigManagerImpl.class)
				.beginPlugin( PropertiesConfigPlugin.class)
					.addParam("url", "io/vertigo/commons/config/properties/app-config.properties")
					.addParam("configPath", "server")
				.endPlugin()
			.endModule()
			.build();
		// @formatter:on
	}

	@Test(expected = Exception.class)
	public void testFail0() {
		configManager.getStringValue("completely", "wrong");
	}

	@Test
	public void testString() {
		final String value = configManager.getStringValue("server", "mail");
		Assert.assertEquals("john.doe@free.fr", value);
	}

	@Test
	public void testInt() {
		final int value = configManager.getIntValue("server", "weight");
		Assert.assertEquals(55, value);
	}

	@Test
	public void testBoolean1() {
		final boolean value = configManager.getBooleanValue("server", "active");
		Assert.assertTrue(value);
	}

	@Test
	public void testBoolean2() {
		final boolean value = configManager.getBooleanValue("server", "closed");
		Assert.assertFalse(value);
	}

	@Test
	public void testResolve() {
		final ServerConfig serverConfig = configManager.resolve("server", ServerConfig.class);
		Assert.assertEquals("monBeauServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}
}
