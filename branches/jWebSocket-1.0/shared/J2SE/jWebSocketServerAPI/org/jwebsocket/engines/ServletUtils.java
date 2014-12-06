//	---------------------------------------------------------------------------
//	jWebSocket - Servlet utility (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.engines;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Contains utility methods and constants for servlet engines
 *
 * @author Rolando Santamaria Maso
 */
public class ServletUtils {

	/**
	 * The servlet engine configuration parameter name
	 */
	public static final String SERVLET_ENGINE_CONFIG_KEY = "jws_engine";
	/**
	 * The servlet engine configuration parameter name
	 */
	public static final String WEB_APP_HOME_PROP_KEY = "WEB_APP_HOME";

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
			lInitArgs[lIndex++] = "-home";
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

	/**
	 * Send a file using the HttpServletResponse object. The connection is
	 * closed once the file is sent.
	 *
	 * @param aResponse
	 * @param aFile
	 * @throws Exception
	 */
	public static void sendFile(HttpServletResponse aResponse, File aFile) throws Exception {
		// sending file
		String lMimeType = new MimetypesFileTypeMap().getContentType(aFile);
		aResponse.setContentType(lMimeType);
		aResponse.setHeader("Content-Disposition", "attachment; filename=" + aFile.getName());
		aResponse.setHeader("Content-Type", lMimeType);
		aResponse.setContentLength((int) aFile.length());

		ServletOutputStream lOS = null;
		BufferedInputStream lBuffer = null;
		try {
			int lByteRead;
			lOS = aResponse.getOutputStream();
			lBuffer = new BufferedInputStream(new FileInputStream(aFile));
			while ((lByteRead = lBuffer.read()) != -1) {
				lOS.write(lByteRead);
			}
		} catch (Exception lEx) {
			throw new ServletException(lEx.getMessage());
		} finally {
			if (null != lOS) {
				lOS.close();
			}
			if (null != lBuffer) {
				lBuffer.close();
			}
		}
	}
}
