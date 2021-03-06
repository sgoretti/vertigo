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
package io.vertigo.vega.webservice.metamodel;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.webservice.WebServiceTypeUtil;
import io.vertigo.vega.webservice.model.UiListState;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WebService param infos :
 * - source type (query, path, body, innerBody or implicit)
 * - name
 * - type (class)
 * - includedField (for DtObjet or DtList)
 * - excludedField (for DtObjet or DtList)
 * - if object kept serverSide
 * - if one time token
 * - specific validators
 *
 * @author npiedeloup
 */
public final class WebServiceParam {

	/**
	 * Parameter's source types.
	 */
	public enum WebServiceParamType {
		Query, Path, Header, Body, InnerBody, Implicit
	}

	public enum ImplicitParam {
		UiMessageStack(UiMessageStack.class),
		//UiListState(UiListState.class),
		Request(HttpServletRequest.class),
		Response(HttpServletResponse.class), ;

		private Class<?> implicitType;

		ImplicitParam(final Class<?> implicitType) {
			this.implicitType = implicitType;
		}

		public Class getImplicitType() {
			return implicitType;
		}

	}

	private final WebServiceParamType paramType;
	private final String name;
	private final Type type;
	private final boolean optional;
	private final String fullName;
	private final Set<String> includedFields;
	private final Set<String> excludedFields;
	private final boolean needServerSideToken;
	private final boolean consumeServerSideToken;
	private final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses;

	/**
	 * @param paramType Parameter type
	 * @param name Parameter name
	 * @param type Parameter class
	 * @param excludedFields List of excluded fieldNames
	 * @param needServerSideToken if access token mandatory
	 * @param consumeServerSideToken if access token is consume (one time token)
	 * @param dtObjectValidatorClasses List of validator classes (order is keep)
	 */
	WebServiceParam(final WebServiceParamType paramType, final String name, final Type type, final boolean optional, final Set<String> includedFields, final Set<String> excludedFields, final boolean needServerSideToken, final boolean consumeServerSideToken, final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses) {
		this(":" + paramType.name() + ":" + name, paramType, name, type, optional, includedFields, excludedFields, needServerSideToken, consumeServerSideToken, dtObjectValidatorClasses);
		Assertion.checkArgument(paramType != WebServiceParamType.Implicit || isImplicitParam(name), "When ImplicitParam, name ({1}) must be one of {0}", ImplicitParam.values(), name);
		Assertion.checkNotNull(name);
		Assertion.checkArgument(!name.isEmpty() || (WebServiceTypeUtil.isAssignableFrom(UiListState.class, type) || WebServiceTypeUtil.isAssignableFrom(DtObject.class, type)), "Only DtObject and UiListState can be map from Query parameters");
	}

	private static boolean isImplicitParam(final String testedName) {
		for (final ImplicitParam existingParam : ImplicitParam.values()) {
			if (existingParam.name().equals(testedName)) {
				return true;
			}
		}
		return false;
	}

	private WebServiceParam(final String fullName, final WebServiceParamType paramType, final String name, final Type type, final boolean optional, final Set<String> includedFields, final Set<String> excludedFields, final boolean needServerSideToken, final boolean consumeServerSideToken, final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses) {
		Assertion.checkNotNull(paramType);
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(includedFields);
		Assertion.checkNotNull(excludedFields);
		Assertion.checkNotNull(dtObjectValidatorClasses);
		Assertion.checkArgument(dtObjectValidatorClasses.isEmpty()
				|| WebServiceTypeUtil.isAssignableFrom(DtObject.class, type)
				|| WebServiceTypeUtil.isParameterizedBy(DtObject.class, type), "Validators aren't supported for {0}", type);
		//-----
		this.paramType = paramType;
		this.type = type;
		this.optional = optional;
		this.name = name;
		this.fullName = fullName;
		this.includedFields = Collections.unmodifiableSet(new LinkedHashSet<>(includedFields));
		this.excludedFields = Collections.unmodifiableSet(new LinkedHashSet<>(excludedFields));
		this.needServerSideToken = needServerSideToken;
		this.consumeServerSideToken = consumeServerSideToken;
		this.dtObjectValidatorClasses = Collections.unmodifiableList(new ArrayList<>(dtObjectValidatorClasses));
	}

	/**
	 * @return Parameter's source type
	 */
	public WebServiceParamType getParamType() {
		return paramType;
	}

	/**
	 * @return Full name of this param.
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @return Parameter name in source
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Parameter class
	 */
	public Class<?> getType() {
		if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		return (Class<?>) type;
	}

	/**
	 * @return generics Type
	 */
	public Type getGenericType() {
		return type;
	}

	/**
	 * @return is optional
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @return List of included fieldNames
	 */
	public Set<String> getIncludedFields() {
		return includedFields;
	}

	/**
	 * @return List of excluded fieldNames
	 */
	public Set<String> getExcludedFields() {
		return excludedFields;
	}

	/**
	 * @return if access token mandatory
	 */
	public boolean isNeedServerSideToken() {
		return needServerSideToken;
	}

	/**
	 * @return if access token is consume (one time token)
	 */
	public boolean isConsumeServerSideToken() {
		return consumeServerSideToken;
	}

	/**
	 * @return List of validator classes (order is keep)
	 */
	public List<Class<? extends DtObjectValidator>> getDtObjectValidatorClasses() {
		return dtObjectValidatorClasses;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(type)
				.append(" ")
				.append(fullName)
				.toString();
	}
}
