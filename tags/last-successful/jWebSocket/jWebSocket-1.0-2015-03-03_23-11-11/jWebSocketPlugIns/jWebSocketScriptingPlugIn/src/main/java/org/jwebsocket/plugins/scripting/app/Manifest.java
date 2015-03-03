//	---------------------------------------------------------------------------
//	jWebSocket - Manifest (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting.app;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * The class contains utility methods and properties to process the script app
 * manifest file "manifest.json".
 *
 * @author Rolando Santamaria Maso
 */
public class Manifest {

	/**
	 *
	 */
	public final static String LANGUAGE_EXT = "language_ext";

	/**
	 *
	 */
	public final static String JWEBSOCKET_PLUGINS_DEPENDENCIES = "jws_dependencies";

	/**
	 *
	 */
	public final static String JWEBSOCKET_VERSION = "jws_version";

	/**
	 *
	 */
	public final static String PERMISSIONS = "permissions";

	/**
	 * Checks the app jWebSocket version dependency.
	 *
	 * @param aVersion
	 * @throws Exception
	 */
	public static void checkJwsVersion(String aVersion) throws Exception {
		Assert.isTrue(JWebSocketInstance.isVersionCompatible(aVersion),
				"Unable to load application. jWebSocket version requirement '" + aVersion + "' not satisfied!");
	}

	/**
	 * Checks the app jWebSocket plug-ins dependencies.
	 *
	 * @param aPlugIns
	 * @throws Exception
	 */
	public static void checkJwsDependencies(List<String> aPlugIns) throws Exception {
		for (String lPlugInId : aPlugIns) {
			String[] lIdVersion = StringUtils.split(lPlugInId, ":");
			WebSocketPlugIn lPlugIn = JWebSocketFactory.getTokenServer().getPlugInById(lIdVersion[0]);
			Assert.notNull(lPlugIn,
					"Unable to load application. jWebSocket dependency plug-in '" + lPlugInId + "' not found!");

			if (lIdVersion.length == 2) {
				Assert.isTrue(lPlugIn.isVersionCompatible(lIdVersion[1]),
						"Unable to load application. jWebSocket plug-in '" + lPlugInId
						+ "' was found, but version number dependency not satisfied!");
			}
		}
	}

	/**
	 * Checks the app sandbox security permissions dependecy.
	 *
	 * @param aPerms
	 * @param aGrantedPerms
	 * @param aAppDirPath
	 * @throws Exception
	 */
	public static void checkPermissions(List<String> aPerms, Permissions aGrantedPerms,
			String aAppDirPath) throws Exception {
		for (String lPerm : aPerms) {
			final String lExpandedPerm = JWebSocketConfig.expandEnvVarsAndProps(lPerm.replace("${APP_HOME}", aAppDirPath));
			try {
				Tools.doPrivileged(aGrantedPerms,
						new PrivilegedAction<Boolean>() {
							@Override
							public Boolean run() {
								AccessController.checkPermission(Tools.stringToPermission(lExpandedPerm));
								return true;
							}
						});
			} catch (AccessControlException lEx) {
				throw new Exception("Unable to load application. Permission requirement '"
						+ lPerm + "' not satisfied!");
			}
		}
	}
}
