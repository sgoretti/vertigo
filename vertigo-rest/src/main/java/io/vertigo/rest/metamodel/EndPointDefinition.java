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
package io.vertigo.rest.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * End point definition.
 * @author npiedeloup
 */
@Prefix("EP_")
public final class EndPointDefinition implements Definition {

	public enum Verb {
		GET, POST, PUT, DELETE,
	}

	private final String name;
	private final String path;
	private final Verb verb;

	private final Method method; //Function gérant l'exectution du EndPoint	
	private final boolean needSession;
	private final boolean needAuthentification;

	private final boolean accessTokenPublish;
	private final boolean accessTokenMandatory;
	private final boolean accessTokenConsume;
	private final boolean serverSideSave;
	private final boolean autoSortAndPagination;

	private final List<String> includedFields;
	private final List<String> excludedFields;

	private final List<EndPointParam> endPointParams;
	private final String doc;

	public EndPointDefinition(final String name, final Verb verb, final String path, final Method method, final boolean needSession, final boolean needAuthentification, final boolean accessTokenPublish, final boolean accessTokenMandatory, final boolean accessTokenConsume, final boolean serverSideSave, final boolean autoSortAndPagination, final List<String> includedFields, final List<String> excludedFields, final List<EndPointParam> endPointParams, final String doc) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(verb);
		Assertion.checkArgNotEmpty(path);
		Assertion.checkNotNull(method);
		Assertion.checkNotNull(includedFields);
		Assertion.checkNotNull(excludedFields);
		Assertion.checkNotNull(endPointParams);
		Assertion.checkNotNull(doc); //doc can be empty
		Assertion.checkArgument(!accessTokenConsume || accessTokenMandatory, "AccessToken mandatory for accessTokenConsume ({0})", name);
		Assertion.checkArgument(!serverSideSave || needSession, "Session mandatory for serverSideState ({0})", name);
		Assertion.checkArgument(!serverSideSave || !Void.TYPE.equals(method.getReturnType()), "Return object mandatory for serverSideState ({0})", name);
		//---------------------------------------------------------------------
		this.name = name;
		this.verb = verb;
		this.path = path;

		this.method = method;
		this.needSession = needSession;
		this.needAuthentification = needAuthentification;

		this.accessTokenPublish = accessTokenPublish;
		this.accessTokenMandatory = accessTokenMandatory;
		this.accessTokenConsume = accessTokenConsume;
		this.serverSideSave = serverSideSave;
		this.autoSortAndPagination = autoSortAndPagination;

		this.includedFields = Collections.unmodifiableList(new ArrayList<>(includedFields));
		this.excludedFields = Collections.unmodifiableList(new ArrayList<>(excludedFields));
		this.endPointParams = Collections.unmodifiableList(new ArrayList<>(endPointParams));

		this.doc = doc;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public Verb getVerb() {
		return verb;
	}

	public Method getMethod() {
		return method;
	}

	public List<EndPointParam> getEndPointParams() {
		return endPointParams;
	}

	public boolean isNeedSession() {
		return needSession;
	}

	public boolean isNeedAuthentification() {
		return needAuthentification;
	}

	public boolean isAccessTokenPublish() {
		return accessTokenPublish;
	}

	public boolean isAccessTokenMandatory() {
		return accessTokenMandatory;
	}

	public boolean isAccessTokenConsume() {
		return accessTokenConsume;
	}

	public boolean isServerSideSave() {
		return serverSideSave;
	}

	public boolean isAutoSortAndPagination() {
		return autoSortAndPagination;
	}

	public List<String> getIncludedFields() {
		return includedFields;
	}

	public List<String> getExcludedFields() {
		return excludedFields;
	}

	public String getDoc() {
		return doc;
	}
}