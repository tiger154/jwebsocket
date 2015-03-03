//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketClient (Community Edition, CE)
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

import java.net.URI;
import java.util.List;
import org.jwebsocket.config.ReliabilityOptions;
import org.jwebsocket.kit.WebSocketEncoding;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.kit.WebSocketSubProtocol;

/**
 * Base interface that represents the <tt>jWebSocket</tt> java client and it
 * defines all the methods and operations to allow the implementation of
 * jWebSocket specific client protocols and allows to register/deregister
 * different types of listeners for different types of communication and data
 * format.The implementation of this interface handles all the data formats and
 * client protocols and delegates the events and the data to different listeners
 * for further processing.
 *
 * @author Alexander Schulze
 * @author puran
 * @author Rolando Santamaria Maso
 * @author Rolando Betancourt Toucet
 * @version $Id: WebSocketClient.java 701 2010-07-18 17:53:06Z
 * mailtopuran@gmail.com $
 */
public interface WebSocketClient {

	/**
	 * Opens the jWebSocket connection
	 *
	 * @param aURL the websocket connection url
	 * @throws WebSocketException if the connection is opened and the client try
	 * to connect again.
	 */
	void open(String aURL) throws WebSocketException;

	/**
	 *
	 * @param aVersion
	 * @param aURI
	 * @throws WebSocketException
	 */
	void open(int aVersion, String aURI) throws WebSocketException;

	/**
	 *
	 * @param aVersion
	 * @param aURI
	 * @param aSubProtocols
	 * @throws WebSocketException
	 */
	void open(int aVersion, String aURI, String aSubProtocols) throws WebSocketException;

	/**
	 * Adds a new filter
	 *
	 * @param aFilter
	 */
	void addFilter(WebSocketClientFilter aFilter);

	/**
	 * Removes a filter
	 *
	 * @param aFilter
	 */
	void removeFilter(WebSocketClientFilter aFilter);

	/**
	 * Returns the registered client filters
	 *
	 * @return
	 */
	List<WebSocketClientFilter> getFilters();

	/**
	 * Send the given byte data to the server
	 *
	 * @param aData the byte data
	 * @throws WebSocketException if exception occurs while sending the data
	 */
	void send(byte[] aData) throws WebSocketException;

	/**
	 * Send the given byte data to the server
	 *
	 * @param aData the byte data
	 * @param aFrameType specify frame type
	 * @throws WebSocketException if exception occurs while sending the data
	 */
	void send(byte[] aData, WebSocketFrameType aFrameType) throws WebSocketException;

	/**
	 * Sends the data to the jWebSocket server, data has to be UTF-8 encoded.
	 *
	 * @param aData the data to send
	 * @param aEncoding the encoding type
	 * @throws WebSocketException if there's any exception while sending the
	 * data
	 */
	void send(String aData, String aEncoding) throws WebSocketException;

	/**
	 * Sends the websocket data packet to the <tt>WebSocket</tt> client
	 *
	 * @param aPacket the data packet to send
	 * @throws WebSocketException if there's any exception while sending
	 */
	void send(WebSocketPacket aPacket) throws WebSocketException;

	/**
	 * Close the jWebSocket connection. This method should perform all the
	 * cleanup operation to release the jWebSocket resources. Closing and
	 * connection which is already closed should not lead to any error.
	 *
	 * @throws WebSocketException if exception while close operation
	 */
	void close() throws WebSocketException;

	/**
	 * Method to check if the jWebSocketClient is still connected to the
	 * jWebSocketServer
	 *
	 * @return {@code true} if there's a persistent connection {@code false}
	 * otherwise
	 */
	boolean isConnected();

	/**
	 * Method to return if the status of the jWebSocketClient
	 *
	 * @return Value of the WebSocketStatus enumeration.
	 */
	WebSocketStatus getStatus();

	/**
	 * Notifies the <tt>jWebSocket</tt> client implementation about the
	 * connection being opened to the jWebSocket server via <tt>WebSocket</tt>
	 *
	 * @param aEvent the websocket client event object
	 */
	void notifyOpened(WebSocketClientEvent aEvent);

	/**
	 * Notifies the <tt>jWebSocket</tt> client implementation about the packet
	 * being received from the <tt>WebSocket</tt> client.
	 *
	 * @param aEvent the websocket client event object
	 * @param aPacket the data packet received
	 */
	void notifyPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket);

	/**
	 * Notifies the <tt>jWebSocket</tt> client implementation about the
	 * connection being closed
	 *
	 * @param aEvent the websocket client event object
	 */
	void notifyClosed(WebSocketClientEvent aEvent);

	/**
	 * Notifies the <tt>jWebSocket</tt> client implementation about the
	 * connection being re-established
	 *
	 * @param aEvent the websocket client event object
	 */
	void notifyReconnecting(WebSocketClientEvent aEvent);

	/**
	 * Adds the client listener to the lists of listener which are interested in
	 * receiving the <tt>jWebSocket</tt> connection and data events. Listeners
	 * are good way to handle all the <tt>jWebSocket</tt> specific protocol and
	 * data format
	 *
	 * @param aListener the event listener object
	 */
	void addListener(WebSocketClientListener aListener);

	/**
	 * Remove the listener from the list of listeners, once the listener is
	 * removed it won't be notified of any <tt>jWebSocket</tt> events.
	 *
	 * @param aListener the listener object to remove
	 */
	void removeListener(WebSocketClientListener aListener);

	/**
	 * Returns the list of listeners registered.
	 *
	 * @return the list of all listeners.
	 */
	List<WebSocketClientListener> getListeners();

	/**
	 * Adds subprotocol to the list of supported protocols which are negotiated
	 * during handshake with web socket server.
	 *
	 * @param aSubProt sub protocol
	 */
	void addSubProtocol(WebSocketSubProtocol aSubProt);

	/**
	 * If subprotocol was negotiated, then this method returns sub protocol
	 * name.
	 *
	 * @return name of the subprotocol or null if no subprotocol was negotiated
	 */
	String getNegotiatedSubProtocol();

	/**
	 * If subprotocol was negotiated, then this method returns sub protocol
	 * format (json, csv, binary, custom)
	 *
	 * @return format of the subprotocol or null if no subprotocol was
	 * negotiated
	 */
	WebSocketEncoding getNegotiatedEncoding();

	/**
	 * Web socket protocol draft. Specification recommends using only draft
	 * number in relevant header, but anything may be used as long as server
	 * understands it. JWebSocket adheres to the specification and therefore
	 * understands only draft number as valid value, e.g. "Sec-WebSocket-Draft:
	 * 3". Anything else will probably fail on server side.
	 *
	 * @param aVersion which draft to use for web socket protocol communication
	 */
	void setVersion(int aVersion);

	/**
	 * The "max frame size" variable indicates the "maximum packet size"
	 * supported by a client. The clients are able to choose the variable
	 * according to their scenarios. The client value is transmitted in the
	 * connection handshake. If the value exceeds the engine max frame size
	 * value, then the client value is ignored. The server transmit the selected
	 * value in the welcome message.
	 *
	 * @return The connector specific max frame size
	 */
	Integer getMaxFrameSize();

	/**
	 * Send a fragmented data packet to the server and control the delivery by
	 * using a listener.
	 *
	 * @param aDataPacket
	 * @param aFragmentSize
	 * @param aListener
	 */
	void sendPacketInTransaction(WebSocketPacket aDataPacket,
			Integer aFragmentSize, IPacketDeliveryListener aListener);

	/**
	 * Send a fragmented data packet to the server and control the delivery by
	 * using a listener.
	 *
	 * @param aDataPacket
	 * @param aListener
	 */
	void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener);

	/**
	 * Sets the client status.
	 *
	 * @param aStatus
	 * @throws Exception
	 */
	void setStatus(WebSocketStatus aStatus);

	/**
	 * Gets the realibility options.
	 *
	 * @return
	 */
	ReliabilityOptions getReliabilityOptions();

	/**
	 * Sets the reliability options
	 *
	 * @param aReliabilityOptions
	 */
	void setReliabilityOptions(ReliabilityOptions aReliabilityOptions);

	/**
	 * Sets the ping internal.
	 *
	 * @param aInterval
	 */
	void setPingInterval(int aInterval);

	/**
	 * Gets the ping internal
	 *
	 * @return
	 */
	int getPingInterval();

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Object getParam(String aKey, Object aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Object getParam(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setParam(String aKey, Object aValue);

	/**
	 * Get the WebSocketClient connection URI
	 *
	 * @return
	 */
	URI getURI();
}
