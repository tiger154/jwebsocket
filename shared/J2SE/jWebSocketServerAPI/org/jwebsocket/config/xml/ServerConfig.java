// ---------------------------------------------------------------------------
// jWebSocket - ServerConfig (Community Edition, CE)
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
import java.util.Map;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Represents the server config
 *
 * @author puran
 * @version $Id: ServerConfig.java 616 2010-07-01 08:04:51Z fivefeetfurther $
 *
 */
public final class ServerConfig implements Config, ServerConfiguration {

	private final String mId;
	private final String mName;
	private final String mJar;
	private final ThreadPoolConfig mThreadPoolConfig;
	private Map<String, Object> mSettings;

	/**
	 *
	 * @param aId
	 * @param aName
	 * @param aJar
	 * @param aSettings
	 */
	public ServerConfig(String aId, String aName, String aJar, Map<String, Object> aSettings) {
		this(aId, aName, aJar, new ThreadPoolConfig(), aSettings);
	}

	/**
	 *
	 * @param aId
	 * @param aName
	 * @param aJar
	 * @param aThreadPoolConfig
	 * @param aSettings
	 */
	public ServerConfig(String aId, String aName, String aJar, ThreadPoolConfig aThreadPoolConfig,
			Map<String, Object> aSettings) {
		this.mId = aId;
		this.mName = aName;
		this.mJar = aJar;
		//If the threadpoolconfig is not set, we just create a default-one.
		if (aThreadPoolConfig == null) {
			this.mThreadPoolConfig = new ThreadPoolConfig();
		} else {
			this.mThreadPoolConfig = aThreadPoolConfig;
		}
		this.mSettings = aSettings;
		//validate the server configuration
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
	 * @return the jar
	 */
	@Override
	public String getJar() {
		return mJar;
	}

	/**
	 * @return the server thread pool configuration
	 */
	@Override
	public ThreadPoolConfig getThreadPoolConfig() {
		return mThreadPoolConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((mId != null && mId.length() > 0)
				&& (mName != null && mName.length() > 0)
				&& (mJar != null && mJar.length() > 0)) {
			mThreadPoolConfig.validate();
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the server configuration, please check your configuration file");
	}

	@Override
	public Map<String, Object> getSettings() {
		return Collections.unmodifiableMap(mSettings);
	}
}
