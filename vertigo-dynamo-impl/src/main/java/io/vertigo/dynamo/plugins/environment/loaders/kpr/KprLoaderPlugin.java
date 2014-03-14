package io.vertigo.dynamo.plugins.environment.loaders.kpr;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.impl.environment.Loader;
import io.vertigo.dynamo.impl.environment.LoaderException;
import io.vertigo.dynamo.impl.environment.LoaderPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Parser d'un fichier KPR.
 * Un fichier KPR est un fichier qui liste l'ensemble des fichiers KSP du projet.
 *
 * @author  pchretien
 * @version $Id: KprLoaderPlugin.java,v 1.4 2013/10/22 10:45:05 pchretien Exp $
 */
public final class KprLoaderPlugin implements LoaderPlugin {
	private static final String KPR_EXTENSION = ".kpr";
	private static final String KSP_EXTENSION = ".ksp";

	private final URL kprURL;
	private final ResourceManager resourceManager;

	//revoir
	/**
	 * Constructeur.
	 * @param kprFileName Adresse du fichier KPR.
	 */
	@Inject
	public KprLoaderPlugin(@Named("kpr") final String kprFileName, final ResourceManager resourceManager) {
		Assertion.checkArgNotEmpty(kprFileName);
		Assertion.checkNotNull(resourceManager);
		//----------------------------------------------------------------------
		kprURL = resourceManager.resolve(kprFileName);
		this.resourceManager = resourceManager;
	}

	/** {@inheritDoc} */
	public void load(final DynamicDefinitionRepository dynamicModelrepository) throws LoaderException {
		Assertion.checkNotNull(dynamicModelrepository);
		Assertion.checkNotNull(kprURL);
		//----------------------------------------------------------------------
		for (final URL url : getKspFiles(kprURL, resourceManager)) {
			final Loader loader = new KspLoader(url);
			loader.load(dynamicModelrepository);
		}
	}

	/**
	 * récupère la liste des fichiers KSP correspondant à un KPR.
	 * @param kprURL fichier KPR
	 * @return List liste des fichiers KSP.
	 */
	private static List<URL> getKspFiles(final URL kprURL, final ResourceManager resourceManager) {
		try {
			return doGetKspFiles(kprURL, resourceManager);
		} catch (final Exception e) {
			throw new VRuntimeException("Echec de lecture du fichier KPR " + kprURL.getFile(), e);
		}

	}

	private static List<URL> doGetKspFiles(final URL kprURL, final ResourceManager resourceManager) throws Exception {
		final List<URL> kspFileList = new ArrayList<>();
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(kprURL.openStream()))) {
			String line = reader.readLine();
			//-----------------------------------
			String path = kprURL.getPath();
			path = path.substring(0, path.lastIndexOf('/'));
			while (line != null) {
				final String fileName = line.trim();
				if (fileName.length() > 0) {
					URL url;
					// voir http://commons.apache.org/vfs/filesystems.html
					if (fileName.indexOf('!') != -1) {
						// pour client riche JavaWebStart (Jar, Tar, Zip)
						final String archFileUri = fileName.substring(fileName.indexOf('!') + 1).replace('\\', '/');
						url = resourceManager.resolve(archFileUri + '/' + fileName);
					} else {
						// Protocol : vfszip pour jboss par exemple
						url = new URL(kprURL.getProtocol() + ':' + path + '/' + fileName);
					}
					if (fileName.endsWith(KPR_EXTENSION)) {
						//kpr
						kspFileList.addAll(getKspFiles(url, resourceManager));
					} else if (fileName.endsWith(KSP_EXTENSION)) {
						//ksp
						kspFileList.add(url);
					} else {
						throw new VRuntimeException("Type de fichier inconnu : {0}", null, fileName);
					}
				}
				line = reader.readLine();
			}
		}

		return kspFileList;
	}
}