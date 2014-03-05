// ---------------------------------------------------------------------------
// jWebSocket - FilterConfig (Community Edition, CE)
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

import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Class that represents the filter config
 *
 * @author puran
 * @version $Id: FilterConfig.java 596 2010-06-22 17:09:54Z fivefeetfurther $
 *
 */
public final class FilterConfig implements Config, FilterConfiguration {

	private final String mId;
	private final String mName;
	private final String mJar;
	private final String mPackageName;
	private final String mNamespace;
	private final List<String> mServers;
	private final Map<String, String> mSettings;
	private final boolean mEnabled;

	/**
	 * default constructor
	 *
	 * @param aId
	 * @param aPackageName
	 * @param aName
	 * @param aJar
	 * @param aServers
	 * @param aNamespace
	 * @param aSettings
	 * @param aEnabled
	 */
	public FilterConfig(String aId, String aName, String aPackageName, String aJar, String aNamespace,
			List<String> aServers, Map<String, String> aSettings, boolean aEnabled) {
		mId = aId;
		mName = aName;
		mPackageName = aPackageName;
		mJar = aJar;
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
	public Map<String, String> getSettings() {
		return (null == mSettings) ? null : Collections.unmodifiableMap(mSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((mId != null && mId.length() > 0)
				&& (mName != null && mName.length() > 0)
				&& (mJar != null && mJar.length() > 0)
				&& (mNamespace != null && mNamespace.length() > 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the filter configuration, please check your configuration file");
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getPackageName() {
		return mPackageName;
	}

	@Override
	public boolean getEnabled() {
		return mEnabled;
	}
}
