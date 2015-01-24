//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting;

import java.io.File;
import java.io.FileFilter;
import java.net.URLDecoder;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class Settings {

	/**
	 * Applications directory file
	 */
	private File mAppsDirectory;
	/**
	 * Global security permissions
	 */
	private final List<String> mGlobalSecurityPermissions = new LinkedList<String>();
	/**
	 * Global white listed beans
	 */
	private final List<String> mGlobalWhiteListedBeans = new LinkedList<String>();
	/**
	 * Applications security permissions
	 */
	private final Map<String, List<String>> mAppsSecurityPermissions = new LinkedHashMap<String, List<String>>();
	/**
	 * Applications while listed beans
	 */
	private final Map<String, List<String>> mAppsWhiteListedBeans = new LinkedHashMap<String, List<String>>();
	/**
	 * Local apps white listed beans
	 */
	private final Map<String, List<String>> mCachedWhiteListedBeans = new FastMap<String, List<String>>().shared();
	/**
	 * Cached permissions
	 */
	private final Map<String, Permissions> mCachedAppPermissions = new FastMap<String, Permissions>().shared();
	/**
	 * Script apps directory path
	 */
	private String mAppsDirectoryPath;
	/**
	 * Script apps shared extensions directory path
	 */
	private String mExtensionsDirectoryPath;

	/**
	 * Gets the map representation <app name, app absolute path> of the apps
	 * directory.
	 *
	 * @return
	 */
	public Map<String, String> getApps() {
		Map<String, String> lApps = new HashMap<String, String>();
		File[] lFiles = mAppsDirectory.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
		for (File lF : lFiles) {
			if (!lF.getName().equals(".svn")) {
				lApps.put(lF.getName(), lF.getAbsolutePath());
			}
		}

		return lApps;
	}

	/**
	 * Gets the applications directory path.
	 *
	 * @return
	 */
	public String getAppsDirectory() {
		return mAppsDirectoryPath;
	}

	/**
	 * Sets the applications directory path.
	 *
	 * @param aAppsDirectoryPath
	 */
	public void setAppsDirectory(String aAppsDirectoryPath) {
		this.mAppsDirectoryPath = JWebSocketConfig.expandEnvVarsAndProps(aAppsDirectoryPath);
	}

	/**
	 * Sets the script apps shared extensions directory path.
	 *
	 * @param aExtensionsDirectoryPath
	 */
	public void setExtensionsDirectory(String aExtensionsDirectoryPath) {
		mExtensionsDirectoryPath = JWebSocketConfig.expandEnvVarsAndProps(aExtensionsDirectoryPath);
	}

	/**
	 * Gets the script apps shared extensions directory path.
	 *
	 * @return
	 */
	public String getExtensionsDirectory() {
		return mExtensionsDirectoryPath;
	}

	/**
	 * Initialize plug-in
	 *
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		// fixing paths format
		mExtensionsDirectoryPath = new File(URLDecoder.decode(mExtensionsDirectoryPath, "utf-8")).getPath();
		mAppsDirectoryPath = new File(URLDecoder.decode(mAppsDirectoryPath, "utf-8")).getPath();

		File lAppsDirectory = new File(mAppsDirectoryPath);
		Assert.isTrue(lAppsDirectory.isDirectory(), "The applications directory path does not exists!"
				+ " Please check directory path or access permissions.");
		Assert.isTrue(lAppsDirectory.canWrite(), "The Scripting plug-in requires "
				+ "WRITE permissions into the applications directory!");
		mAppsDirectory = lAppsDirectory;

		File lExtensionsDirectory = new File(mExtensionsDirectoryPath);
		Assert.isTrue(lExtensionsDirectory.isDirectory(), "The extensions directory path does not exists!"
				+ " Please check directory path or access permissions.");
		Assert.isTrue(lExtensionsDirectory.canRead(), "The Scripting plug-in requires "
				+ "READ permissions into the extensions directory!");
	}

	/**
	 * Sets the global security permissions that apply to all script
	 * applications.
	 *
	 * @param aPermissions
	 */
	public void setGlobalSecurityPermissions(List<String> aPermissions) {
		if (null != aPermissions) {
			mGlobalSecurityPermissions.addAll(aPermissions);
		}
	}

	/**
	 * Gets the global security permissions.
	 *
	 * @return
	 */
	public List<String> getGlobalSecurityPermissions() {
		return mGlobalSecurityPermissions;
	}

	/**
	 * Sets apps security permissions. Global permissions are always considered.
	 *
	 * @param aPermissions
	 */
	public void setAppsSecurityPermissions(Map<String, List<String>> aPermissions) {
		if (null != aPermissions) {
			mAppsSecurityPermissions.putAll(aPermissions);
		}
	}

	/**
	 * Gets the apps white listed beans.
	 *
	 * @return
	 */
	public Map<String, List<String>> getAppsWhiteListedBeans() {
		return mAppsWhiteListedBeans;
	}

	/**
	 * Sets apps white listed beans. Global white listed beans are always
	 * considered.
	 *
	 * @param aAppsWhiteListedBeans
	 */
	public void setAppsWhiteListedBeans(Map<String, List<String>> aAppsWhiteListedBeans) {
		if (null != aAppsWhiteListedBeans) {
			mAppsWhiteListedBeans.putAll(aAppsWhiteListedBeans);
		}
	}

	/**
	 * Gets global white listed beans.
	 *
	 * @return
	 */
	public List<String> getGlobalWhiteListedBeans() {
		return mGlobalWhiteListedBeans;
	}

	/**
	 * Sets the global white listed beans.
	 *
	 * @param aGlobalWhiteListedBeans
	 */
	public void setGlobalWhiteListedBeans(List<String> aGlobalWhiteListedBeans) {
		if (null != aGlobalWhiteListedBeans) {
			mGlobalWhiteListedBeans.addAll(aGlobalWhiteListedBeans);
		}
	}

	/**
	 * Gets apps security permissions Map
	 *
	 * @return
	 */
	public Map<String, List<String>> getAppsSecurityPermissions() {
		return mAppsSecurityPermissions;
	}

	/**
	 * Gets application security permissions.
	 *
	 * @param aAppName
	 * @param aAppPath
	 * @return
	 */
	public Permissions getAppPermissions(String aAppName, String aAppPath) {
		if (mCachedAppPermissions.containsKey(aAppName)) {
			return mCachedAppPermissions.get(aAppName);
		}

		Permissions lPerms = new Permissions();
		Permission lPermission;

		// processing global permissions
		for (String lStrPerm : getGlobalSecurityPermissions()) {
			lPermission = Tools.stringToPermission(JWebSocketConfig.expandEnvVarsAndProps(
					lStrPerm
					.replace("${APP_HOME}", aAppPath)
					.replace("${EXT}", mExtensionsDirectoryPath)));

			if (null != lPermission) {
				lPerms.add(lPermission);
			}
		}

		// processing app permissions
		if (getAppsSecurityPermissions().containsKey(aAppName)) {
			for (String lStrPerm : getAppsSecurityPermissions().get(aAppName)) {
				lPermission = Tools.stringToPermission(JWebSocketConfig.expandEnvVarsAndProps(
						lStrPerm
						.replace("${APP_HOME}", aAppPath)
						.replace("${EXT}", mExtensionsDirectoryPath)));

				if (null != lPermission) {
					lPerms.add(lPermission);
				}
			}
		}

		mCachedAppPermissions.put(aAppName, lPerms);
		return lPerms;
	}

	/**
	 * Gets application white listed beans.
	 *
	 * @param aAppName
	 * @return
	 */
	public List<String> getAppWhiteListedBeans(String aAppName) {
		if (mCachedWhiteListedBeans.containsKey(aAppName)) {
			return mCachedWhiteListedBeans.get(aAppName);
		}

		List lWhiteList = new ArrayList();

		// adding global beans
		lWhiteList.addAll(mGlobalWhiteListedBeans);
		// adding app specific beans
		if (mAppsWhiteListedBeans.containsKey(aAppName)) {
			lWhiteList.addAll(mAppsWhiteListedBeans.get(aAppName));
		}

		// caching white listed beans
		mCachedWhiteListedBeans.put(aAppName, lWhiteList);

		return lWhiteList;
	}
}
