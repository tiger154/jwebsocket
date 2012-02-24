//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.factory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.xeustechnologies.jcl.JarClassLoader;

/**
 * ClassLoader that loads the classes from the jars. Engine, Servers, Plugins
 * all configured via jWebSocket.xml file is loaded using this class.
 * 
 * @author puran
 * @author kyberneees
 * @author Marcos Antonio González Huerta (markos0886, UCI)
 */
public class JWebSocketJarClassLoader extends JarClassLoader {

	/**
	 * 
	 */
	public JWebSocketJarClassLoader() {
		// init with empty list of URLs
		super();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aPath 
	 * @throws MalformedURLException
	 */
	public void addFile(String aPath) throws MalformedURLException {
		File lFile = new File(aPath);
		URL lURL = lFile.toURI().toURL();
		add(lURL);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aURL 
	 * @throws MalformedURLException
	 */
	public void addURL(URL aURL) throws MalformedURLException {
		add(aURL);
	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	public Class reloadClass(String className) {
		return getLocalLoader().loadClass(className, true);
	}
}
