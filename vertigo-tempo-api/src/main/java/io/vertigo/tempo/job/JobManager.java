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
package io.vertigo.tempo.job;

import io.vertigo.lang.Component;
import io.vertigo.tempo.job.metamodel.JobDefinition;

/**
 * Job scheduler.
 * Cette classe permet d'exécuter une "tâche" de manière indépendante d'une servlet :
 * - à une fréquence fixe (ex : toutes les 10 min)
 * - à une heure fixe chaque jour (ex : à 1h tous les jours)
 * - de suite (ex : dés que possible s'il n'y a pas déjà une tâche en cours de même nom)
 *
 * En général, les implémentations de cette interface utilisent un ou plusieurs threads séparés
 * du thread d'appel et sont transactionnelles dans leurs threads pour chaque exécution.
 *
 * @author evernat
 */
public interface JobManager extends Component {

	/**
	 * Exécution immédiate et synchrone d'un job.
	 */
	void execute(final JobDefinition jobDefinition);
}
