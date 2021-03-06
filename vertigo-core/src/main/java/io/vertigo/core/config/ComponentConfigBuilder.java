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
package io.vertigo.core.config;

import io.vertigo.core.spaces.component.ComponentInitializer;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Option;

import java.util.HashMap;
import java.util.Map;

/**
 * Paramétrage de l'application.
 *
 * @author npiedeloup, pchretien
 */
public final class ComponentConfigBuilder implements Builder<ComponentConfig> {
	//Par convention l'id du composant manager est le simpleName de la classe de l'api ou de l'impl.
	private final ModuleConfigBuilder moduleConfigBuilder;
	private final Option<Class<?>> apiClass;
	private final Class<?> implClass;
	private final boolean elastic;
	private Class<? extends ComponentInitializer<?>> managerInitializerClass;
	private final Map<String, String> myParams = new HashMap<>();

	ComponentConfigBuilder(final ModuleConfigBuilder moduleConfigBuilder, final Option<Class<?>> apiClass, final Class<?> implClass, final boolean elastic) {
		Assertion.checkNotNull(moduleConfigBuilder);
		Assertion.checkNotNull(apiClass);
		Assertion.checkNotNull(implClass);
		//-----
		this.moduleConfigBuilder = moduleConfigBuilder;
		this.apiClass = apiClass;
		this.implClass = implClass;
		this.elastic = elastic;
	}

	public ComponentConfigBuilder withInitializer(final Class<? extends ComponentInitializer<?>> managerInitialierClass) {
		Assertion.checkNotNull(managerInitialierClass);
		//-----
		managerInitializerClass = managerInitialierClass;
		return this;
	}

	public ComponentConfigBuilder addParam(final String paramName, final String paramValue) {
		Assertion.checkArgNotEmpty(paramName);
		//paramValue can be null
		//-----
		if (paramValue != null) {
			myParams.put(paramName, paramValue);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ComponentConfig build() {
		return new ComponentConfig(apiClass, implClass, elastic, managerInitializerClass, myParams);
	}

	public ModuleConfigBuilder endComponent() {
		return moduleConfigBuilder;
	}
}
