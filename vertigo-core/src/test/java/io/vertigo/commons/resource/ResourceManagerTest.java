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
package io.vertigo.commons.resource;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.impl.resource.ResourceManagerImpl;
import io.vertigo.core.resource.ResourceManager;

import java.net.URL;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public final class ResourceManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ResourceManager resourceManager;

	@Override
	protected AppConfig buildAppConfig() {
		//@formatter:off
		return new AppConfigBuilder()
			.beginModule("spaces")
				.addComponent(ResourceManager.class, ResourceManagerImpl.class)
				.addPlugin(ClassPathResourceResolverPlugin.class)
			.endModule()
			.build();
		// @formatter:on
	}

	@Test(expected = NullPointerException.class)
	public void testNullResource() {
		resourceManager.resolve(null);

	}

	@Test(expected = RuntimeException.class)
	public void testEmptyResource() {
		resourceManager.resolve("nothing");
	}

	@Test
	public void testResourceSelector() {
		final String expected = "io/vertigo/commons/resource/hello.properties";
		final URL url = resourceManager.resolve(expected);
		Assert.assertTrue(url.getPath().indexOf(expected) != -1);
	}
}
