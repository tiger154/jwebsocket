// ---------------------------------------------------------------------------
// jWebSocket - EngineConfig (Community Edition, CE)
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
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Class that represents the engine config
 *
 * @author puran
 * @version $Id: EngineConfig.java 616 2010-07-01 08:04:51Z fivefeetfurther $
 */
public final class EngineConfig implements Config, EngineConfiguration {

	private final String mId;
	private final String mName;
	private final String mJar;
	private final String mContext;
	private final String mServlet;
	private final Integer mPort;
	private final String mHostname;
	private final Integer mSSLPort;
	private final String mKeyStore;
	private final String mKeyStorePassword;
	private final int mTimeout;
	private final int mMaxframesize;
	private final List<String> mDomains;
	private final Integer mMaxConnections;
	private final String mOnMaxConnectionsStrategy;
	private final Map<String, Object> mSettings;
	private final boolean mNotifySystemStopping;
	private final boolean mKeepAliveConnectors;
	private final Integer mKeepConnectorsAliveInterval;
	private final Integer mKeepAliveConnectorsTimeout;

	/**
	 * Constructor for engine
	 *
	 * @param aId the engine id
	 * @param aName the name of the engine
	 * @param aJar the jar file name
	 * @param aPort the port number where engine runs
	 * @param aSSLPort
	 * @param aHostname
	 * @param aTimeout the timeout value
	 * @param aKeyStorePassword
	 * @param aKeyStore
	 * @param aMaxFrameSize the maximum frame size that engine will receive
	 * without closing the connection
	 * @param aServlet
	 * @param aDomains list of domain names
	 * @param aContext
	 * @param aSettigns
	 * @param aMaxConnections
	 * @param aOnMaxConnectionsStrategy
	 * @param aNotifySystemStopping
	 * @param aKeepConnectorsAlive
	 * @param aKeepAliveConnectorsInterval
	 * @param aKeepAliveConnectorsTimeout
	 *
	 */
	public EngineConfig(String aId, String aName, String aJar, Integer aPort,
			Integer aSSLPort, String aHostname, String aKeyStore, String aKeyStorePassword,
			String aContext, String aServlet, int aTimeout,
			int aMaxFrameSize, List<String> aDomains, Integer aMaxConnections,
			String aOnMaxConnectionsStrategy, boolean aNotifySystemStopping,
			Map<String, Object> aSettigns, boolean aKeepConnectorsAlive,
			Integer aKeepAliveConnectorsInterval,
			Integer aKeepAliveConnectorsTimeout) {
		this.mId = aId;
		this.mName = aName;
		this.mJar = aJar;
		this.mContext = aContext;
		this.mServlet = aServlet;
		this.mHostname = aHostname;
		this.mPort = aPort;
		this.mSSLPort = aSSLPort;
		this.mKeyStore = aKeyStore;
		this.mKeyStorePassword = aKeyStorePassword;
		this.mTimeout = aTimeout;
		this.mMaxframesize = aMaxFrameSize;
		this.mDomains = aDomains;
		this.mMaxConnections = aMaxConnections;
		this.mOnMaxConnectionsStrategy = aOnMaxConnectionsStrategy;
		this.mSettings = aSettigns;
		this.mNotifySystemStopping = aNotifySystemStopping;
		this.mKeepAliveConnectors = aKeepConnectorsAlive;
		this.mKeepConnectorsAliveInterval = aKeepAliveConnectorsInterval;
		this.mKeepAliveConnectorsTimeout = aKeepAliveConnectorsTimeout;

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

	@Override
	public String getHostname() {
		return mHostname;
	}

	/**
	 * @return the port
	 */
	@Override
	public Integer getPort() {
		return mPort;
	}

	/**
	 * @return the SSL port
	 */
	@Override
	public Integer getSSLPort() {
		return mSSLPort;
	}

	/**
	 * Returns the context for servlet based engines like Jetty
	 *
	 * @return the context for servlet based engines, null for native servers
	 */
	@Override
	public String getContext() {
		return mContext;
	}

	/**
	 * Returns the servlet for servlet based engines like Jetty
	 *
	 * @return the servlet for servlet based engines, null for native servers
	 */
	@Override
	public String getServlet() {
		return mServlet;
	}

	/**
	 * @return the timeout
	 */
	@Override
	public int getTimeout() {
		return mTimeout;
	}

	/**
	 * @return the max frame size
	 */
	@Override
	public int getMaxFramesize() {
		return mMaxframesize;
	}

	/**
	 * @return the domains
	 */
	@Override
	public List<String> getDomains() {
		return mDomains;
	}

	/**
	 * validate the engine configuration
	 *
	 * @throws WebSocketRuntimeException if any of the engine configuration is
	 * mising
	 */
	@Override
	public void validate() {
		if ((mId != null && mId.length() > 0)
				&& (mName != null && mName.length() > 0)
				&& (mJar != null && mJar.length() > 0)
				&& (mDomains != null && mDomains.size() > 0)
				// leaving port empty needs to be allowed eg. for Jetty
				// when using underlying WebSocket Servlets
				&& (mPort == null || (mPort >= 0 && mPort < 65536))
				&& (mSSLPort == null
				|| (mSSLPort >= 0 && mSSLPort < 65536
				&& mKeyStore != null && mKeyStore.length() > 0
				&& mKeyStorePassword != null && mKeyStorePassword.length() > 0))
				&& mTimeout >= 0
				&& mMaxConnections > 0
				&& null != mOnMaxConnectionsStrategy && mOnMaxConnectionsStrategy.length() > 0) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the engine configuration, "
				+ "please check your configuration file!");
	}

	/**
	 * @return the KeyStore
	 */
	@Override
	public String getKeyStore() {
		return mKeyStore;
	}

	/**
	 * @return the KeyStorePassword
	 */
	@Override
	public String getKeyStorePassword() {
		return mKeyStorePassword;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Integer getMaxConnections() {
		return mMaxConnections;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getOnMaxConnectionStrategy() {
		return mOnMaxConnectionsStrategy;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Map<String, Object> getSettings() {
		return Collections.unmodifiableMap(mSettings);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean isNotifySystemStopping() {
		return mNotifySystemStopping;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean getKeepAliveConnectors() {
		return mKeepAliveConnectors;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Integer getKeepAliveConnectorsInterval() {
		return mKeepConnectorsAliveInterval;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Integer getKeepAliveConnectorsTimeout() {
		return mKeepAliveConnectorsTimeout;
	}
}
