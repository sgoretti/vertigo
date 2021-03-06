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
package io.vertigo.core;

import java.io.PrintStream;

/**
 * Displays logo.
 *
 * @author pchretien
 */
public final class Logo {
	private Logo() {
		//constructor is proteceted
	}

	/**
	 * Displays logo in console.
	 */
	public static void printCredits(final PrintStream out) {
		out.println();
		out.println("##########################################");
		out.println("#  _____________                         #");
		out.println("# |     _     / | ---------------------- #");
		out.println("# |#   / \\   / /|  Vertigo v0.8.2 - 2015 #"); //add one char for \\
		out.println("# |  __\\ /__/ / |                        #"); //add one char for \\
		out.println("# | / _      /  |                        #");
		out.println("# |/ / \\  ()/  *|                        #"); //add one char for \\
		out.println("# | /  |   |    |  www.kleegroup.com     #");
		out.println("# |/___|____\\___| ---------------------- #"); //add one char for \\
		out.println("#                                        #");
		out.println("##########################################");
		out.println();
	}
}
