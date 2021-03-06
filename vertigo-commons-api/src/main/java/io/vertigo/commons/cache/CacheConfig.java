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
package io.vertigo.commons.cache;

import io.vertigo.lang.Assertion;

/**
 * Config of cache.
 *
 * @author pchretien
 */
public final class CacheConfig {
	private final String cacheType;
	private final int maxElementsInMemory;
	private final long timeToLiveSeconds;
	private final long timeToIdleSeconds;
	private final boolean eternal;

	public CacheConfig(final String cacheType, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds) {
		Assertion.checkArgNotEmpty(cacheType);
		//-----
		this.cacheType = cacheType;
		this.maxElementsInMemory = maxElementsInMemory;
		this.timeToLiveSeconds = timeToLiveSeconds;
		this.timeToIdleSeconds = timeToIdleSeconds;
		this.eternal = false;
	}

	public String getCacheType() {
		return cacheType;
	}

	public boolean isEternal() {
		return eternal;
	}

	public int getMaxElementsInMemory() {
		return maxElementsInMemory;
	}

	public long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	public long getTimeToIdleSeconds() {
		return timeToIdleSeconds;
	}
}
