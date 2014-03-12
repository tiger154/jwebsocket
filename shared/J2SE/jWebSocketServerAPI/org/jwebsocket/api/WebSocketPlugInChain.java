//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketPlugInChain (Community Edition, CE)
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

import java.util.List;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketSession;

/**
 * A plug-in chain maintains a map of plug-ins. A server in the jWebSocket model
 * usually does not process data packets directly but forwards them to a chain
 * of plug-ins. The plug-in chain then forwards the data packet to each plug-in
 * until the first plug-in aborts or breaks the chain by returning the
 * corresponding PlugInResponse.
 *
 * @author Alexander Schulze
 * @author Marcos Antonio Gonzalez Huerta
 * @author Rolando Santamaria Maso
 */
public interface WebSocketPlugInChain extends ISystemLifecycle {

	/**
	 * is called by the server when the engine has been started. Usually the
	 * implementations iterate through the chain of plug-ins and call their
	 * <tt>engineStarted</tt> method of each plug-in to notify them about the
	 * "engine started" event. This event is useful when a plug-in needs to be
	 * initialized before first usage.
	 *
	 * @param aEngine The jWebSocket engine that has just started.
	 */
	void engineStarted(WebSocketEngine aEngine);

	/**
	 * is called by the server when the engine has been stopped. Usually the
	 * implementations iterate through the chain of plug-ins and call their
	 * <tt>engineStopped</tt> method of each plug-in to notify them about the
	 * "engine stopped" event. This event is useful when a plug-in needs to be
	 * cleaned up after usage.
	 *
	 * @param aEngine The jWebSocket engine that has just stopped.
	 */
	void engineStopped(WebSocketEngine aEngine);

	/**
	 * is called by the server when a new connector has been started, i.e. a new
	 * client has connected. Usually the implementations iterate through the
	 * chain of plug-ins and call their <tt>connectorStarted</tt> method of each
	 * plug-in to notify them about the connect event.
	 *
	 * @param aConnector The connector that has just started.
	 */
	void connectorStarted(WebSocketConnector aConnector);

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
	 * is called when a data packet from a client was received and has to be
	 * processed. Usually the implementations iterate through the chain of
	 * plug-ins and call the <tt>processPacket</tt> method of each plug-in to
	 * notify them about the incoming packet.
	 *
	 * @param aConnector The connector from which the data packet was received.
	 * @param aDataPacket The data packet which was received.
	 * @return PlugInResponse specifies whether to continue or abort the
	 * processing of the plug-in chain.
	 */
	PlugInResponse processPacket(WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 * is called by the server when a connector has been stopped, i.e. a client
	 * has disconnected. Usually the implementations iterate through the chain
	 * of plug-ins and call the <tt>connectorStopped</tt> method of the plug-ins
	 * to notify them about the disconnect event.
	 *
	 * @param aConnector The connector that has just stopped.
	 * @param aCloseReason Specifies why a connection has closed. Please refer
	 * to CloseReason documentation.
	 */
	void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	/**
	 * returns the list of the plug-ins within this plug-in chain.
	 *
	 * @return List of plug-ins.
	 */
	List<WebSocketPlugIn> getPlugIns();

	/**
	 * appends a plug-in to the plug-in chain. All subsequent incoming data
	 * packet will be forwarded to that plug-in too.
	 *
	 * @param aPlugIn Plug-in to be added from the plug-in chain.
	 */
	void addPlugIn(WebSocketPlugIn aPlugIn);

	/**
	 * appends a plug-in to the plug-in chain. All subsequent incoming data
	 * packet will be forwarded to that plug-in too.
	 *
	 * @param aPosition Position of the Plug-in to be added from the plug-in
	 * chain.
	 * @param aPlugIn Plug-in to be added from the plug-in chain.
	 */
	void addPlugIn(Integer aPosition, WebSocketPlugIn aPlugIn);

	/**
	 * removes a plug-in from the plug-in chain. All subsequent incoming data
	 * packet will not be forwarded to that plug-in any more.
	 *
	 * @param aPlugIn Plug-in to be removed from the plug-in chain.
	 */
	void removePlugIn(WebSocketPlugIn aPlugIn);

	/**
	 * returns the plug-in from the plug-in chain that matches the given plug-in
	 * id.
	 *
	 * @param aId
	 * @return plug-in from the plug-in chain that matches the given plug-in id.
	 */
	public WebSocketPlugIn getPlugIn(String aId);

	/**
	 *
	 * @return
	 */
	WebSocketServer getServer();
}
