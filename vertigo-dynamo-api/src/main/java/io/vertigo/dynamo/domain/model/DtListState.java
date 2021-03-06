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
package io.vertigo.dynamo.domain.model;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * DtList state : sorting and paging informations
 *
 * @author npiedeloup
 */
public final class DtListState {

	private final Option<String> sortFieldName;
	private final Option<Boolean> sortDesc;
	private final int skipRows;
	private final Option<Integer> maxRows;

	/**
	 * @param maxRows max returning elements (null if not use)
	 * @param skipRows elements to skip (mandatory, 0 by default)
	 * @param sortFieldName sort fieldName (null if not use)
	 * @param sortDesc desc or asc order (null if not use)
	 */
	public DtListState(final Integer maxRows, final int skipRows, final String sortFieldName, final Boolean sortDesc) {
		Assertion.checkArgument(maxRows == null || maxRows > 0, "maxRows must be positive ({0})", maxRows);
		Assertion.checkArgument(skipRows >= 0, "SkipRows must be positive ({0})", skipRows);
		Assertion.checkArgument(sortFieldName == null || sortDesc != null, "When sorting, sortFieldName and sortDesc are both mandatory.");
		//-----
		this.maxRows = Option.option(maxRows);
		this.skipRows = skipRows;
		this.sortFieldName = Option.option(sortFieldName);
		this.sortDesc = Option.option(sortDesc);
	}

	/**
	 * @return max returning elements
	 */
	public Option<Integer> getMaxRows() {
		return maxRows;
	}

	/**
	 * @return elements to skip
	 */
	public int getSkipRows() {
		return skipRows;
	}

	/**
	 * @return sort fieldName
	 */
	public Option<String> getSortFieldName() {
		return sortFieldName;
	}

	/**
	 * @return  desc or asc order
	 */
	public Option<Boolean> isSortDesc() {
		return sortDesc;
	}
}
