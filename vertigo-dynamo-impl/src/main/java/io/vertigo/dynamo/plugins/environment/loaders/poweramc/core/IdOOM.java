package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

/**
 * Identifiant d'un objet powerAMC.
 *
 * @author pchretien
 * @version $Id: IdOOM.java,v 1.3 2013/10/22 12:30:19 pchretien Exp $
 */
final class IdOOM {
	private final String keyValue;

	/**
	 * Constructeur.
	 * @param keyValue Valeur de l'identiant
	 */
	IdOOM(final String keyValue) {
		Assertion.checkNotNull(keyValue);
		//------------------------------------------------------------------
		this.keyValue = keyValue;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return keyValue.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof IdOOM) {
			return ((IdOOM) o).keyValue.equals(this.keyValue);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "id(" + keyValue + ')';
	}
}