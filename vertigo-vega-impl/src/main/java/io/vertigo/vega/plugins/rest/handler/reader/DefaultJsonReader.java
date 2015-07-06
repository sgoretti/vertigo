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
package io.vertigo.vega.plugins.rest.handler.reader;

import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import spark.Request;

public final class DefaultJsonReader implements JsonReader<Request> {

	/** {@inheritDoc} */
	@Override
	public RestParamType[] getSupportedInput() {
		return RestParamType.values(); //default support all
	}

	/** {@inheritDoc} */
	@Override
	public Class<Request> getSupportedOutput() {
		return Request.class;
	}

	/** {@inheritDoc} */
	@Override
	public Request extractData(final Request request, final EndPointParam endPointParam, final RouteContext routeContext) {
		return request;
	}
}