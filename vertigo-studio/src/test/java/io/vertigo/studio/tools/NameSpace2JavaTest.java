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
package io.vertigo.studio.tools;

import org.junit.Test;

/**
 * Test la génération à partir des oom et ksp.
 * @author dchallas
 */
public class NameSpace2JavaTest {
	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerate() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/test.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateFile() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/testFile.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateSql() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/testSql.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateDtDefinitions() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/testDtDefinitions.properties" });
	}
}
