//	---------------------------------------------------------------------------
//	jWebSocket - Basic PlugIn Class
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.api;

import java.util.Map;
import org.json.JSONObject;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketSession;

/**
 *
 * @author aschulze
 */
public interface WebSocketPlugIn {

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
	 * returns the enabled status of the plug-in.
	 *
	 * @return
	 */
	boolean getEnabled();

	/**
	 * set the enabled status of the plug-in.
	 */
	void setEnabled(boolean aEnabled);

	/**
	 * notifies the plug-in about a change in enabled status.
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
}
