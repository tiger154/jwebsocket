//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketPlugIn (Community Edition, CE)
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
package org.jwebsocket.api;

import java.util.Map;
import org.json.JSONObject;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketSession;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public interface WebSocketPlugIn extends ISystemLifecycle {

	/**
	 * returns the id of the plug-in.
	 *
	 * @return
	 */
	String getId();

	/**
	 * returns the name of the plug-in.
	 *
	 * @return
	 */
	String getName();

	/**
	 * return the version of the plug-in.
	 *
	 * @return
	 */
	String getVersion();

	/**
	 * return the label of the plug-in.
	 *
	 * @return
	 */
	String getLabel();

	/**
	 * returns the description of the plug-in.
	 *
	 * @return
	 */
	String getDescription();

	/**
	 * returns the vendor of the plug-in.
	 *
	 * @return
	 */
	String getVendor();

	/**
	 * returns the copyright of the plug-in.
	 *
	 * @return
	 */
	String getCopyright();

	/**
	 * returns the license of the plug-in.
	 *
	 * @return
	 */
	String getLicense();

	/**
	 * returns the Namespace of the plug-in.
	 *
	 * @return
	 */
	String getNamespace();

	/**
	 * returns the enabled status of the plug-in.
	 *
	 * @return
	 */
	boolean getEnabled();

	/**
	 * set the enabled status of the plug-in.
	 *
	 * @param aEnabled
	 */
	void setEnabled(boolean aEnabled);

	/**
	 * notifies the plug-in about a change in enabled status.
	 *
	 * @param aEnabled
	 */
	void processEnabled(boolean aEnabled);

	/**
	 * is called by the server when the engine has been started.
	 *
	 * @param aEngine
	 */
	void engineStarted(WebSocketEngine aEngine);

	/**
	 * is called by the server when the engine has been stopped.
	 *
	 * @param aEngine
	 */
	void engineStopped(WebSocketEngine aEngine);

	/**
	 *
	 * @param aConnector
	 */
	void connectorStarted(WebSocketConnector aConnector);

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aDataPacket
	 */
	void processPacket(PlugInResponse aResponse, WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	/**
	 * Called when a WebSocketSession is created. Future enterprise applications
	 * will use this event instead of "connectorStarted", because the second
	 * does not guarantee a session storage creation.
	 *
	 * @param aConnector
	 * @param aSession
	 */
	void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession);

	/**
	 * Called when a WebSocketSession expired. This event represents the real
	 * client disconnection. The "connectorStopped" event should happen multiple
	 * times, but the session is kept. When a session is stopped (expired) it
	 * means: A client is finally disconnected.
	 *
	 * @param aSession
	 */
	void sessionStopped(WebSocketSession aSession);

	/**
	 *
	 * @param aPlugInChain
	 */
	void setPlugInChain(WebSocketPlugInChain aPlugInChain);

	/**
	 * @return the plugInChain
	 */
	WebSocketPlugInChain getPlugInChain();

	/**
	 * Set the plug-in configuration
	 *
	 * @param configuration the plug-in configuration object to set
	 */
	// void setPluginConfiguration(PluginConfiguration configuration);
	/**
	 * Returns the plug-in configuration object based on the configuration file
	 * values
	 *
	 * @return the plug-in configuration object
	 */
	PluginConfiguration getPluginConfiguration();

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void addString(String aKey, String aValue);

	/**
	 *
	 *
	 * @param aSettings
	 */
	// void addAllSettings(Map<String, String> aSettings);
	/**
	 *
	 * @param aKey
	 */
	void removeSetting(String aKey);

	/**
	 *
	 */
	void clearSettings();

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	String getString(String aKey, String aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	String getString(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	JSONObject getJSON(String aKey, JSONObject aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	JSONObject getJSON(String aKey);

	/**
	 *
	 * @return
	 */
	Map<String, Object> getSettings();

	/**
	 * Returns TRUE if the given plug-in version is compatible, FALSE otherwise.
	 *
	 * @param aVersion
	 * @return
	 */
	boolean isVersionCompatible(String aVersion);
}
