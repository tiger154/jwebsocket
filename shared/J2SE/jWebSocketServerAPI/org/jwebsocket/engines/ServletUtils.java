//	---------------------------------------------------------------------------
//	jWebSocket - Servlet utility class
//	Copyright (c) 2012 jWebSocket.org, Innotrade GmbH
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
package org.jwebsocket.engines;

import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;

/**
 * Contains utility methods and constants for servlet engines
 *
 * @author kyberneees
 */
public class ServletUtils {

	/**
	 * The servlet engine configuration parameter name
	 */
	public static final String SERVLET_ENGINE_CONFIG_KEY = "jws_engine";

	/**
	 * Extract the jWebSocket startup arguments from a servlet-context
	 * init-parameters
	 *
	 * @param aContext
	 * @return
	 */
	public static String[] extractStartupArguments(ServletContext aContext) {
		String[] lInitArgs = new String[3 * 2];

		String lConfig = aContext.getInitParameter(CONTEXT_CONFIG_PARAMETER);
		String lBootstrap = aContext.getInitParameter(CONTEXT_BOOTSTRAP_PARAMETER);
		String lHome = aContext.getInitParameter(CONTEXT_HOME_PARAMETER);
		int lIndex = 0;
		if (null != lConfig) {
			lInitArgs[lIndex++] = "-config";
			lInitArgs[lIndex++] = lConfig;
		}
		if (null != lBootstrap) {
			lInitArgs[lIndex++] = "-bootstrap";
			lInitArgs[lIndex++] = lBootstrap;
		}
		if (null != lHome) {
			lInitArgs[lIndex++] = "-home ";
			lInitArgs[lIndex++] = lHome;
		}

		return lInitArgs;
	}
	/**
	 * The startup jWebSocket.xml file path parameter name
	 */
	public static final String CONTEXT_CONFIG_PARAMETER = "jws_config";
	/**
	 * The jWebSocket server home directory path parameter name
	 */
	public static final String CONTEXT_HOME_PARAMETER = "jws_home";
	/**
	 * The bootstrap override path parameter name
	 */
	public static final String CONTEXT_BOOTSTRAP_PARAMETER = "jws_bootstrap";
}
