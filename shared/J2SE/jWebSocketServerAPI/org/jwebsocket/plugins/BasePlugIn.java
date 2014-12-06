//	---------------------------------------------------------------------------
//	jWebSocket - Basic Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins;

import java.util.Map;
import javolution.util.FastMap;
import org.json.JSONObject;
import org.jwebsocket.api.*;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.util.Tools;

/**
 * Abstract implementation of WebSocketPlugin
 *
 * @author Alexander Schulze
 * @version $Id:$
 */
public abstract class BasePlugIn implements WebSocketPlugIn {

	/**
	 * custom vendor string - NEEDS TO BE OVERWRITTEN BY CUSTOM PLUGIN!
	 */
	public static final String PLUGIN_CUSTOM_VENDOR = "<return vendor in getVendor() method of plug-in>";
	/**
	 * custom plug-in version string - NEEDS TO BE OVERWRITTEN BY CUSTOM PLUGIN!
	 */
	public static final String PLUGIN_CUSTOM_VERSION = "<return version in getVersion() method of plug-in>";
	/**
	 * custom plug-in description string - NEEDS TO BE OVERWRITTEN BY CUSTOM
	 * PLUGIN!
	 */
	public static final String PLUGIN_CUSTOM_DESCRIPTION = "<return description in getDescription() method of plug-in>";
	/**
	 * custom plug-in label string - NEEDS TO BE OVERWRITTEN BY CUSTOM PLUGIN!
	 */
	public static final String PLUGIN_CUSTOM_LABEL = "<return label in getLabel() method of plug-in>";
	/**
	 * custom copyright string - NEEDS TO BE OVERWRITTEN BY CUSTOM PLUGIN!
	 */
	public static final String PLUGIN_CUSTOM_COPYRIGHT = "<return copyright in getCopyright() method of plug-in>";
	/**
	 * custom license string - NEEDS TO BE OVERWRITTEN BY CUSTOM PLUGIN!
	 */
	public static final String PLUGIN_CUSTOM_LICENSE = "<return license in getLicense() method of plug-in>";
	private WebSocketPlugInChain mPlugInChain = null;
	private final Map<String, Object> mSettings = new FastMap<String, Object>();
	private PluginConfiguration mConfiguration;
	// authentication methods support
	private String mAuthenticationMethod = AUTHENTICATION_METHOD_SPRING;
	/**
	 *
	 */
	public static String AUTHENTICATION_METHOD_KEY = "authentication_method";
	/**
	 *
	 */
	public static String AUTHENTICATION_METHOD_SPRING = "spring";
	/**
	 *
	 */
	public static String AUTHENTICATION_METHOD_EMBEDDED = "embedded";

	/**
	 * Constructor
	 *
	 * @param aConfiguration the plug-in configuration
	 */
	public BasePlugIn(PluginConfiguration aConfiguration) {
		mConfiguration = aConfiguration;
		if (null != aConfiguration) {
			Map<String, Object> lSettings = aConfiguration.getSettings();
			addAllSettings(lSettings);
		}

		// setting the authentication method value
		mAuthenticationMethod = getString(AUTHENTICATION_METHOD_KEY, mAuthenticationMethod);
	}

	@Override
	public void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession) {
	}

	@Override
	public void sessionStopped(WebSocketSession aSession) {
	}

	/**
	 *
	 * @return
	 */
	public String getAuthenticationMethod() {
		return mAuthenticationMethod;
	}

	/**
	 *
	 * @param aAuthenticationMethod
	 */
	public void setAuthenticationMethod(String aAuthenticationMethod) {
		this.mAuthenticationMethod = aAuthenticationMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PluginConfiguration getPluginConfiguration() {
		return mConfiguration;
	}

	@Override
	public abstract void engineStarted(WebSocketEngine aEngine);

	@Override
	public abstract void engineStopped(WebSocketEngine aEngine);

	/**
	 *
	 * @param aConnector
	 */
	@Override
	public abstract void connectorStarted(WebSocketConnector aConnector);

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aDataPacket
	 */
	@Override
	public abstract void processPacket(PlugInResponse aResponse, WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	@Override
	public abstract void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	@Override
	public void processEnabled(boolean aEnabled) {
		// this is supposed to be overwritten by 
		// the plug-in implementations if required
	}

	/**
	 *
	 * @param aPlugInChain
	 */
	@Override
	public void setPlugInChain(WebSocketPlugInChain aPlugInChain) {
		mPlugInChain = aPlugInChain;
	}

	/**
	 * @return the plugInChain
	 */
	@Override
	public WebSocketPlugInChain getPlugInChain() {
		return mPlugInChain;
	}

	/**
	 *
	 * @return
	 */
	public WebSocketServer getServer() {
		WebSocketServer lServer = null;
		if (mPlugInChain != null) {
			lServer = mPlugInChain.getServer();
		}
		return lServer;
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getUsername</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 * @return
	 */
	public String getUsername(WebSocketConnector aConnector) {
		return getServer().getUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>setUsername</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 * @param aUsername
	 */
	public void setUsername(WebSocketConnector aConnector, String aUsername) {
		getServer().setUsername(aConnector, aUsername);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>removeUsername</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 */
	public void removeUsername(WebSocketConnector aConnector) {
		getServer().removeUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getNodeId</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 * @return
	 */
	public String getNodeId(WebSocketConnector aConnector) {
		return getServer().getNodeId(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>setNodeId</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 * @param aNodeId
	 */
	public void setNodeId(WebSocketConnector aConnector, String aNodeId) {
		getServer().setNodeId(aConnector, aNodeId);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>removeNodeId</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 */
	public void removeNodeId(WebSocketConnector aConnector) {
		getServer().removeNodeId(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getConnector</tt> to simplify token plug-in code.
	 *
	 * @param aId
	 * @return
	 */
	public WebSocketConnector getConnector(String aId) {
		return (aId != null ? getServer().getConnector(aId) : null);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getNode</tt> to simplify token plug-in code.
	 *
	 * @param aNodeId
	 * @return
	 */
	public WebSocketConnector getNode(String aNodeId) {
		return (aNodeId != null ? getServer().getNode(aNodeId) : null);
	}

	/**
	 * Convenience method to simplify token plug-in code. Method is deprecated,
	 * please use <tt>Long getConnectorsCount()</tt>
	 *
	 * @return
	 */
	@Deprecated
	public int getConnectorCount() {
		return getConnectorsCount().intValue();
	}

	/**
	 * Convenience method to simplify token plug-in code.
	 *
	 * @return
	 */
	public Long getConnectorsCount() {
		return getServer().getConnectorsCount();
	}

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	@Override
	public void addString(String aKey, String aValue) {
		if (null != aKey) {
			mSettings.put(aKey, aValue);
		}
	}

	/**
	 * @param aSettings
	 */
	// @Override
	private void addAllSettings(Map<String, Object> aSettings) {
		if (null != aSettings) {
			mSettings.putAll(aSettings);
		}
	}

	/**
	 *
	 * @param aKey
	 */
	@Override
	public void removeSetting(String aKey) {
		if (null != aKey) {
			mSettings.remove(aKey);
		}
	}

	/**
	 *
	 */
	@Override
	public void clearSettings() {
		mSettings.clear();
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
	public Map<String, Object> getSettings() {
		return mSettings;
	}

	/**
	 * @return the id of the plug-in
	 */
	@Override
	public String getId() {
		return mConfiguration.getId();
	}

	/**
	 * @return the name of the plug-in
	 */
	@Override
	public String getName() {
		return mConfiguration.getName();
	}

	@Override
	public boolean getEnabled() {
		return mConfiguration.getEnabled();
	}

	@Override
	public void setEnabled(boolean aEnabled) {
		Boolean lOldEnabled = mConfiguration.getEnabled();
		mConfiguration = new PluginConfig(mConfiguration.getId(),
				mConfiguration.getName(), mConfiguration.getPackage(),
				mConfiguration.getJar(), mConfiguration.getJars(), mConfiguration.getNamespace(),
				mConfiguration.getServers(), mSettings, aEnabled);
		// notify plug-in for change of enabled status
		if (aEnabled != lOldEnabled) {
			processEnabled(aEnabled);
		}
	}

	@Override
	public String getVersion() {
		return PLUGIN_CUSTOM_VERSION;
	}

	@Override
	public String getVendor() {
		return PLUGIN_CUSTOM_VENDOR;
	}

	@Override
	public String getLabel() {
		return PLUGIN_CUSTOM_LABEL;
	}

	@Override
	public String getDescription() {
		return PLUGIN_CUSTOM_DESCRIPTION;
	}

	@Override
	public String getCopyright() {
		return PLUGIN_CUSTOM_COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return PLUGIN_CUSTOM_LICENSE;
	}

	@Override
	public void systemStarting() throws Exception {
	}

	@Override
	public void systemStarted() throws Exception {
	}

	@Override
	public void systemStopping() throws Exception {
	}

	@Override
	public void systemStopped() throws Exception {
	}

	@Override
	public boolean isVersionCompatible(String aVersion) {
		return Tools.compareVersions(getVersion(), aVersion) == 0;
	}
}
