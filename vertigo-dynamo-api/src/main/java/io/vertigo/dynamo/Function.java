package io.vertigo.dynamo;

/**
 * Approche fonctionnelle.
 * On définit un objet fonction.
 * @author pchretien
 * @version $Id: Function.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 * @param <IN> Type de paramètre d'entrée de la fonction
 * @param <OUT> Type de paaramètre de sortie de la fonction
 */
public interface Function<IN, OUT> {
	/**
	 * @param input Paramètre d'entrée
	 * @return Calcul de la fonction
	 */
	OUT apply(final IN input);
}