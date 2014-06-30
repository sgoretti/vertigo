//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2007 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// http://www.gnu.org/copyleft/lesser.html
//
package io.vertigo.quarto.plugins.converter.openoffice;

/**
 * Impl�mentation de connexion � OpenOffice en mode Socket (mode le plus simple).
 * <p>
 * <b>Attention</b> Il faut configurer OpenOffice pour qu'il accepte cette connexion.
 * Soit en modifiant le fichier de conf :
 * <code>OOoBasePath\share\registry\data\org\openoffice\Setup.xcu</code>
 * Juste apr�s cette ligne-ci : <code><node oor:name=\"Office\"></code>
 * Il faut ajouter les lignes suivantes :
 * <code><prop oor:name=\"ooSetupConnectionURL\" oor:type=\"xs:string\">
 * <value>socket,host=localhost,port=8100;urp;</value>
 * </prop></code>
 * Ensuite, il faut relancer OpenOffice
 * <p>
 * Soit par ligne de commande (<b>a tester</b> : http://linuxfr.org/forums/15/16106.html) :
 * <code>/usr/bin/xvfb-run -a /usr/bin/openoffice -invisible "-accept=socket,host=localhost,port=8100;urp;StarOffice.Service.Manager" &</code>
 * <p>
 * Repris de JodConverter 2.2.0 (http://www.artofsolving.com/opensource/jodconverter)
 * @author npiedeloup
 * @version $Id: SocketOpenOfficeConnection.java,v 1.1 2013/07/10 15:45:43 npiedeloup Exp $
 */
final class SocketOpenOfficeConnection extends AbstractOpenOfficeConnection {
	/**
	 * Constructeur utilisant des param�tres de connexion sp�cifiques.
	 * @param host sp�cifique
	 * @param port sp�cifique
	 */
	SocketOpenOfficeConnection(final String host, final int port) {
		super("socket,host=" + host + ",port=" + port + ",tcpNoDelay=1");
	}
}