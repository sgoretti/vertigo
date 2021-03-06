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
package io.vertigo.dynamo.impl.work.worker.local;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.Coordinator;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Implémentation d'un pool local de {@link Coordinator}.
 *
 * @author pchretien
 */
public final class LocalCoordinator implements Coordinator, Closeable {
	/** Pool de workers qui wrappent sur l'implémentation générique.*/
	private final ExecutorService workers;

	/**
	 * Constructeur.
	 *
	 * @param workerCount paramètres d'initialisation du pool
	 */
	public LocalCoordinator(final int workerCount) {
		Assertion.checkArgument(workerCount >= 1, "At least one thread must be allowed to process asynchronous works.");
		//-----
		workers = Executors.newFixedThreadPool(workerCount);
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//Shutdown in two phases (see doc)
		workers.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!workers.awaitTermination(60, TimeUnit.SECONDS)) {
				workers.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!workers.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (final InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			workers.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Work devant être exécuté
	 * WorkItem contient à la fois le Work et le callback.
	 * @param workItem WorkItem
	 */
	@Override
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		Assertion.checkNotNull(workItem);
		//-----
		return workers.submit(new LocalWorker<>(workItem, workResultHandler));
	}
}
