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
package io.vertigo.vega.plugins.webservice.instrospector.annotations;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.vega.impl.webservice.WebServiceIntrospectorPlugin;
import io.vertigo.vega.webservice.WebServiceTypeUtil;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinitionBuilder;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParamBuilder;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition.Verb;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.ImplicitParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;
import io.vertigo.vega.webservice.model.UiListState;
import io.vertigo.vega.webservice.stereotype.AccessTokenConsume;
import io.vertigo.vega.webservice.stereotype.AccessTokenMandatory;
import io.vertigo.vega.webservice.stereotype.AccessTokenPublish;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.AutoSortAndPagination;
import io.vertigo.vega.webservice.stereotype.DELETE;
import io.vertigo.vega.webservice.stereotype.Doc;
import io.vertigo.vega.webservice.stereotype.ExcludedFields;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.HeaderParam;
import io.vertigo.vega.webservice.stereotype.IncludedFields;
import io.vertigo.vega.webservice.stereotype.InnerBodyParam;
import io.vertigo.vega.webservice.stereotype.PATCH;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PUT;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;
import io.vertigo.vega.webservice.stereotype.ServerSideConsume;
import io.vertigo.vega.webservice.stereotype.ServerSideRead;
import io.vertigo.vega.webservice.stereotype.ServerSideSave;
import io.vertigo.vega.webservice.stereotype.SessionInvalidate;
import io.vertigo.vega.webservice.stereotype.SessionLess;
import io.vertigo.vega.webservice.stereotype.Validate;
import io.vertigo.vega.webservice.validation.DefaultDtObjectValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author npiedeloup
 */
public final class AnnotationsWebServiceIntrospectorPlugin implements WebServiceIntrospectorPlugin {

	/** {@inheritDoc} */
	@Override
	public List<WebServiceDefinition> instrospectWebService(final Class<? extends WebServices> webServicesClass) {
		Assertion.checkNotNull(webServicesClass);
		//-----
		final List<WebServiceDefinition> webServiceDefinitions = new ArrayList<>();
		for (final Method method : webServicesClass.getMethods()) {
			final Option<WebServiceDefinition> webServiceDefinition = buildWebServiceDefinition(method, webServicesClass);
			if (webServiceDefinition.isDefined()) {
				webServiceDefinitions.add(webServiceDefinition.get());
			}
		}
		return webServiceDefinitions;
	}

	private static <C extends WebServices> Option<WebServiceDefinition> buildWebServiceDefinition(final Method method, final Class<C> webServicesClass) {
		final WebServiceDefinitionBuilder builder = new WebServiceDefinitionBuilder(method);
		final PathPrefix pathPrefix = method.getDeclaringClass().getAnnotation(PathPrefix.class);
		if (pathPrefix != null) {
			builder.withPathPrefix(pathPrefix.value());
		}
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof GET) {
				builder.with(Verb.GET, ((GET) annotation).value());
			} else if (annotation instanceof POST) {
				builder.with(Verb.POST, ((POST) annotation).value());
			} else if (annotation instanceof PUT) {
				builder.with(Verb.PUT, ((PUT) annotation).value());
			} else if (annotation instanceof PATCH) {
				builder.with(Verb.PATCH, ((PATCH) annotation).value());
			} else if (annotation instanceof DELETE) {
				builder.with(Verb.DELETE, ((DELETE) annotation).value());
			} else if (annotation instanceof AnonymousAccessAllowed) {
				builder.withNeedAuthentication(false);
			} else if (annotation instanceof SessionLess) {
				builder.withNeedSession(false);
			} else if (annotation instanceof SessionInvalidate) {
				builder.withSessionInvalidate(true);
			} else if (annotation instanceof ExcludedFields) {
				builder.addExcludedFields(((ExcludedFields) annotation).value());
			} else if (annotation instanceof IncludedFields) {
				builder.addIncludedFields(((IncludedFields) annotation).value());
			} else if (annotation instanceof AccessTokenPublish) {
				builder.withAccessTokenPublish(true);
			} else if (annotation instanceof AccessTokenMandatory) {
				builder.withAccessTokenMandatory(true);
			} else if (annotation instanceof AccessTokenConsume) {
				builder.withAccessTokenMandatory(true);
				builder.withAccessTokenConsume(true);
			} else if (annotation instanceof ServerSideSave) {
				builder.withServerSideSave(true);
			} else if (annotation instanceof AutoSortAndPagination) {
				builder.withAutoSortAndPagination(true);
			} else if (annotation instanceof Doc) {
				builder.withDoc(((Doc) annotation).value());
			}
		}
		if (builder.hasVerb()) {
			final Type[] paramType = method.getGenericParameterTypes();
			final Annotation[][] parameterAnnotation = method.getParameterAnnotations();

			for (int i = 0; i < paramType.length; i++) {
				final WebServiceParam webServiceParam = buildWebServiceParam(parameterAnnotation[i], paramType[i]);
				builder.addWebServiceParam(webServiceParam);
			}
			//---
			return Option.some(builder.build());
		}
		return Option.none();
	}

	private static WebServiceParam buildWebServiceParam(final Annotation[] annotations, final Type paramType) {
		final WebServiceParamBuilder builder = new WebServiceParamBuilder(paramType);
		if (WebServiceTypeUtil.isAssignableFrom(DtObject.class, paramType)) {
			builder.addValidatorClasses(DefaultDtObjectValidator.class);
		} else if (WebServiceTypeUtil.isParameterizedBy(DtObject.class, paramType)) {
			builder.addValidatorClasses(DefaultDtObjectValidator.class);
		} else if (isImplicitParam(paramType)) {
			builder.with(WebServiceParamType.Implicit, getImplicitParam(paramType).name());
		} else if (UiListState.class.equals(paramType)) {
			builder.with(WebServiceParamType.Query, "listState"); //UiListState don't need to be named, it will be retrieve from query
		}
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				builder.with(WebServiceParamType.Path, ((PathParam) annotation).value());
			} else if (annotation instanceof QueryParam) {
				builder.with(WebServiceParamType.Query, ((QueryParam) annotation).value());
			} else if (annotation instanceof HeaderParam) {
				builder.with(WebServiceParamType.Header, ((HeaderParam) annotation).value());
			} else if (annotation instanceof InnerBodyParam) {
				builder.with(WebServiceParamType.InnerBody, ((InnerBodyParam) annotation).value());
			} else if (annotation instanceof Validate) {
				builder.addValidatorClasses(((Validate) annotation).value());
			} else if (annotation instanceof ExcludedFields) {
				builder.addExcludedFields(((ExcludedFields) annotation).value());
			} else if (annotation instanceof IncludedFields) {
				builder.addIncludedFields(((IncludedFields) annotation).value());
			} else if (annotation instanceof ServerSideRead) {
				builder.needServerSideToken();
			} else if (annotation instanceof ServerSideConsume) {
				builder.needServerSideToken()
						.consumeServerSideToken();
			}
		}
		return builder.build();
	}

	private static ImplicitParam getImplicitParam(final Type paramType) {
		for (final ImplicitParam implicitParam : ImplicitParam.values()) {
			if (implicitParam.getImplicitType().equals(paramType)) {
				return implicitParam;
			}
		}
		return null;
	}

	private static final boolean isImplicitParam(final Type paramType) {
		for (final ImplicitParam implicitParam : ImplicitParam.values()) {
			if (implicitParam.getImplicitType().equals(paramType)) {
				return true;
			}
		}
		return false;
	}
}
