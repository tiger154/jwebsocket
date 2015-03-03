//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Filesystem Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.filesystem;

import java.io.File;
import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.config.JWebSocketConfig;

/**
 * FileSystemPlugIn(FSP) configurator class
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class Settings {

	private Map<String, String> mAliases = new FastMap<String, String>();
	private boolean mMonitoringActive = false;
	private long mMonitoringInterval = 5000;

	/**
	 * Gets the FSP aliases information (Map). Includes alias name and alias
	 * directory path.
	 *
	 * @return the aliases
	 */
	public Map<String, String> getAliases() {
		return mAliases;
	}

	/**
	 * Sets the FSP aliases information.
	 *
	 * @param aAliases the aliases to set
	 */
	public void setAliases(Map<String, String> aAliases) {
		mAliases = aAliases;
	}

	/**
	 * Gets the directory path of a given alias name.
	 *
	 * @param aAliasName
	 * @return
	 */
	public String getAliasPath(String aAliasName) {
		return JWebSocketConfig.expandEnvVarsAndProps(mAliases.get(aAliasName));
	}

	/**
	 * Gets the alias name of a given directory path.
	 *
	 * @param aPath
	 * @return
	 */
	public String getAliasName(String aPath) {
		for (Map.Entry<String, String> lEntry : mAliases.entrySet()) {
			String lAlias = lEntry.getValue().replace("/", File.separator);
			String lExpandedAlias = JWebSocketConfig.expandEnvVarsAndProps(lAlias).replace("/", File.separator);
			if (aPath.startsWith(lAlias)
					|| aPath.startsWith(lExpandedAlias)) {
				return lEntry.getKey();
			}
		}

		return null;
	}

	/**
	 * Gets the directory path of a given alias allowing to pass a default value
	 * if data is not present.
	 *
	 * @param aAliasName
	 * @param aDefaultValue
	 * @return
	 */
	public String getAliasPath(String aAliasName, String aDefaultValue) {
		String lValue = mAliases.get(aAliasName);
		if (null == lValue) {
			return aDefaultValue;
		}
		return lValue;
	}

	/**
	 * @return the MonitoringActive
	 */
	public boolean isMonitoringActive() {
		return mMonitoringActive;
	}

	/**
	 * @param aMonitoringActive the MonitoringActive to set
	 */
	public void setMonitoringActive(boolean aMonitoringActive) {
		mMonitoringActive = aMonitoringActive;
	}

	/**
	 * @return the MonitoringInterval
	 */
	public long getMonitoringInterval() {
		return mMonitoringInterval;
	}

	/**
	 * @param aMonitoringInterval
	 */
	public void setMonitoringInterval(long aMonitoringInterval) {
		mMonitoringInterval = aMonitoringInterval;
	}
}
