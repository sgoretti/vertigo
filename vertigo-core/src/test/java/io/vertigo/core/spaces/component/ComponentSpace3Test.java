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
package io.vertigo.core.spaces.component;

import io.vertigo.core.App;
import io.vertigo.core.Home;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.spaces.component.data.FunctionManager;
import io.vertigo.core.spaces.component.data.FunctionManager1Impl;
import io.vertigo.core.spaces.component.data.FunctionManager2Impl;
import io.vertigo.core.spaces.component.data.FunctionPlugin;

import org.junit.Assert;
import org.junit.Test;

public final class ComponentSpace3Test {

	@Test
	public void testInjectPluginsAttribute() {
		final AppConfig appConfig = createHomeWithInjectPluginsAttribute(true);
		try (App app = new App(appConfig)) {
			final FunctionManager functionManager = Home.getComponentSpace().resolve(FunctionManager.class);
			Assert.assertEquals(4, functionManager.compute("x+1", 3));
			Assert.assertEquals(6, functionManager.compute("2x", 3));
			Assert.assertEquals(15, functionManager.compute("4x+3", 3));
			Assert.assertEquals(1, functionManager.compute("0x+1", 3));
			Assert.assertEquals(-7, functionManager.compute("x-10", 3));
			Assert.assertEquals(-9, functionManager.computeAll(3));
		}
	}

	@Test
	public void testInjectPluginsAttributeOrder() {
		final AppConfig appConfig = createHomeWithInjectPluginsAttribute(false);
		try (App app = new App(appConfig)) {
			final FunctionManager functionManager = Home.getComponentSpace().resolve(FunctionManager.class);
			Assert.assertEquals(26, functionManager.computeAll(3));
		}
	}

	@Test
	public void testInjectPluginsConstructor() {
		final AppConfig appConfig = createHomeWithInjectPluginsConstructor(true);
		try (App app = new App(appConfig)) {
			final FunctionManager functionManager = Home.getComponentSpace().resolve(FunctionManager.class);
			Assert.assertEquals(4, functionManager.compute("x+1", 3));
			Assert.assertEquals(6, functionManager.compute("2x", 3));
			Assert.assertEquals(15, functionManager.compute("4x+3", 3));
			Assert.assertEquals(1, functionManager.compute("0x+1", 3));
			Assert.assertEquals(-7, functionManager.compute("x-10", 3));
			Assert.assertEquals(-9, functionManager.computeAll(3));
		}
	}

	@Test
	public void testInjectPluginsConstructorOrder() {
		final AppConfig appConfig = createHomeWithInjectPluginsConstructor(false);
		try (App app = new App(appConfig)) {
			final FunctionManager functionManager = Home.getComponentSpace().resolve(FunctionManager.class);
			Assert.assertEquals(26, functionManager.computeAll(3));
		}
	}

	private static AppConfig createHomeWithInjectPluginsAttribute(final boolean withNullMult) {
		return startHomeWithFunctionManager(FunctionManager1Impl.class, withNullMult);
	}

	private static AppConfig createHomeWithInjectPluginsConstructor(final boolean withNullMult) {
		return startHomeWithFunctionManager(FunctionManager2Impl.class, withNullMult);
	}

	private static AppConfig startHomeWithFunctionManager(final Class<? extends FunctionManager> implClass, final boolean withNullMult) {
		// @formatter:off
		
		
		return  new AppConfigBuilder()
			.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
			.endBoot()	
			.beginModule("Function")
				.addComponent(FunctionManager.class, implClass)
				.beginPlugin(FunctionPlugin.class)
					.addParam("name", "x+1")
					.addParam("a", "1")
					.addParam("b", "1")
				.endPlugin()
				.beginPlugin(FunctionPlugin.class)
					.addParam("name", "2x")
					.addParam("a", "2")
					.addParam("b", "0")
				.endPlugin()
				.beginPlugin(FunctionPlugin.class)
					.addParam("name", "4x+3")
					.addParam("a", "4")
					.addParam("b", "3")
				.endPlugin()
				.beginPlugin(FunctionPlugin.class)
					.addParam("name", (withNullMult?"0":"1")+"x+1")
					.addParam("a", withNullMult?"0":"1")
					.addParam("b", "1")
				.endPlugin()
				.beginPlugin(FunctionPlugin.class)
					.addParam("name", "x-10")
					.addParam("a", "1")
					.addParam("b", "-10")
				.endPlugin()
			.endModule()
		.build();
		// @formatter:on
	}
}
