// ---------------------------------------------------------------------------
// jWebSocket - PluginConfig (Community Edition, CE)
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
package org.jwebsocket.config.xml;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Class that represents the plugin config
 *
 * @author puran
 * @version $Id: PluginConfig.java 596 2010-06-22 17:09:54Z fivefeetfurther $
 *
 */
public final class PluginConfig implements Config, PluginConfiguration {

	private final String mId;
	private final String mName;
	private final String mJar;
	private final String mPackageName;
	private final String mNamespace;
	private final List<String> mServers;
	private final List<String> mJars;
	private final Map<String, Object> mSettings;
	private final boolean mEnabled;

	/**
	 * default constructor
	 *
	 * @param aId the plug-in id
	 * @param aName the plug-in name
	 * @param aPackage
	 * @param aJar the plug-in jar
	 * @param aJars
	 * @param aNamespace the name-space
	 * @param aServers
	 * @param aSettings FastMap of settings key and value
	 * @param aEnabled
	 */
	public PluginConfig(String aId, String aName, String aPackage, String aJar, List<String> aJars,
			String aNamespace, List<String> aServers, Map<String, Object> aSettings, boolean aEnabled) {
		mId = aId;
		mName = aName;
		mPackageName = aPackage;
		mJar = aJar;
		mJars = aJars;
		mNamespace = aNamespace;
		mServers = aServers;
		mSettings = aSettings;
		mEnabled = aEnabled;
		validate();
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return mId;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * @return the package
	 */
	@Override
	public String getPackage() {
		return mPackageName;
	}

	/**
	 * @return the jar
	 */
	@Override
	public String getJar() {
		return mJar;
	}

	@Override
	public List<String> getJars() {
		return mJars;
	}

	/**
	 * @return the namespace
	 */
	@Override
	public String getNamespace() {
		return mNamespace;
	}

	/**
	 * @return the list of servers
	 */
	@Override
	public List<String> getServers() {
		return (null == mServers) ? null : Collections.unmodifiableList(mServers);
	}

	/**
	 * @return the settings
	 */
	@Override
	public Map<String, Object> getSettings() {
		return (null == mSettings) ? null : Collections.unmodifiableMap(mSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((mId != null && mId.length() > 0)
				&& (mName != null && mName.length() > 0)
				&& (mNamespace != null && mNamespace.length() > 0)) {
			return;
		}

		if ((mJar != null && mJar.length() > 0) || !mJars.isEmpty()) {
			return;
		}

		throw new WebSocketRuntimeException(
				"Missing one of the plugin configuration, "
				+ "please check your configuration file");
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public String getString(String aKey, String aDefault) {
		Object lValue = mSettings.get(aKey);
		String lRes = null;
		if (lValue != null && lValue instanceof String) {
			lRes = (String) lValue;
		}
		return (lRes != null ? lRes : aDefault);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public String getString(String aKey) {
		return (aKey != null ? getString(aKey, null) : null);
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public JSONObject getJSON(String aKey, JSONObject aDefault) {
		Object lValue = mSettings.get(aKey);
		JSONObject lRes = null;
		if (lValue != null && lValue instanceof JSONObject) {
			lRes = (JSONObject) lValue;
		}
		return (lRes != null ? lRes : aDefault);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public JSONObject getJSON(String aKey) {
		return (aKey != null ? getJSON(aKey, null) : null);
	}

	@Override
	public boolean getEnabled() {
		return mEnabled;
	}
}
