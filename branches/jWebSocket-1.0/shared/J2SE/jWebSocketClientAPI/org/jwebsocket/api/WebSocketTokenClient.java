//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketTokenClient (Community Edition, CE)
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

import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.WebSocketResponseTokenListener;

/**
 * Base interface that represents the <tt>Token</tt> based jWebSocket client.
 * This interface defines all the methods that handles the basic jWebSocket
 * specific protocol.
 *
 * @author puran
 * @version $Id: WebSocketTokenClient.java 697 2010-07-17 21:43:50Z
 * mailtopuran@gmail.com $
 */
public interface WebSocketTokenClient extends WebSocketClient {

	/**
	 * @return the loggedIn user name
	 */
	String getUsername();

	/**
	 * Login the client based on given username and password to the jWebSocket
	 * server
	 *
	 * @param aUsername
	 * @param aPassword
	 * @throws WebSocketException if there's any exception while login
	 */
	void login(String aUsername, String aPassword) throws WebSocketException;

	/**
	 * Logout the user
	 *
	 * @throws WebSocketException if exception while logging out
	 */
	void logout() throws WebSocketException;

	/**
	 * Checks if for this client a user already is authenticated.
	 *
	 * @return
	 */
	boolean isAuthenticated();

	/**
	 * Broadcast the text to all the connected clients to jWebSocket server
	 *
	 * @param aText
	 * @throws WebSocketException if exception while broadcasting
	 */
	void broadcastText(String aText) throws WebSocketException;

	/**
	 * Ping the jWebSocket server
	 *
	 * @param aEcho
	 * @throws WebSocketException if exception while doing a ping
	 */
	void ping(boolean aEcho) throws WebSocketException;

	/**
	 * Send the text data
	 *
	 * @param aTargetId
	 * @param aText
	 * @throws WebSocketException if exception while sending text
	 */
	void sendText(String aTargetId, String aText) throws WebSocketException;

	/**
	 * Disconnect from the jWebSocket server
	 *
	 * @throws WebSocketException if error while disconnecting
	 */
	void disconnect() throws WebSocketException;

	/**
	 * Send the token to get the number of connected clients
	 *
	 * @throws WebSocketException
	 */
	void getConnections() throws WebSocketException;

	/**
	 * Add the token client listener which are interested in receiving only
	 * token based data.
	 *
	 * @param aTokenListener
	 */
	void addTokenClientListener(WebSocketClientTokenListener aTokenListener);

	/**
	 * Remove the token client listener
	 *
	 * @param aTokenListener
	 */
	void removeTokenClientListener(WebSocketClientTokenListener aTokenListener);

	/**
	 * Send a token to the server
	 *
	 * @param aToken
	 * @throws WebSocketException
	 */
	void sendToken(Token aToken) throws WebSocketException;

	/**
	 * Send a token to the server
	 *
	 * @param aToken
	 * @param aResponseListener
	 * @throws WebSocketException
	 */
	void sendToken(Token aToken, WebSocketResponseTokenListener aResponseListener) throws WebSocketException;

	/**
	 * Send a token in transaction to the server
	 *
	 * @param aToken
	 * @param aResponseListener
	 * @param aDeliveryListener
	 * @throws WebSocketException
	 */
	void sendTokenInTransaction(Token aToken, WebSocketResponseTokenListener aResponseListener,
			IPacketDeliveryListener aDeliveryListener) throws WebSocketException;

	/**
	 * Send a token in transaction to the server using high level fragmentation
	 *
	 * @param aToken
	 * @param aFragmentSize
	 * @param aResponseListener
	 * @throws WebSocketException
	 */
	void sendTokenInTransaction(Token aToken, int aFragmentSize,
			final WebSocketResponseTokenListener aResponseListener) throws WebSocketException;

	/**
	 * Send a token in transaction to the server using high level fragmentation
	 *
	 * @param aToken
	 * @param aFragmentSize
	 * @param aResponseListener
	 * @param aDeliveryListener
	 * @throws WebSocketException
	 */
	void sendTokenInTransaction(Token aToken, int aFragmentSize, WebSocketResponseTokenListener aResponseListener,
			IPacketDeliveryListener aDeliveryListener) throws WebSocketException;

	/**
	 * Send an IChunkable object to the server
	 *
	 * @param aChunkable
	 * @param aResponseListener
	 */
	void sendChunkable(IChunkable aChunkable, final WebSocketResponseTokenListener aResponseListener);

	/**
	 * Send an IChunkable object to the server
	 *
	 * @param aChunkable
	 * @param aResponseListener
	 * @param aDeliveryListener
	 */
	void sendChunkable(IChunkable aChunkable, WebSocketResponseTokenListener aResponseListener,
			IChunkableDeliveryListener aDeliveryListener);

	/**
	 * Broadcast a token to other clients that share the current session id
	 *
	 * @param aToken
	 * @param aSenderIncluded
	 * @param aListener
	 * @throws WebSocketException
	 */
	void broadcastToSharedSession(Token aToken, boolean aSenderIncluded,
			WebSocketResponseTokenListener aListener) throws WebSocketException;
	
	/**
	 * Broadcast a token to other clients that share the current session id
	 * 
	 * @param aToken
	 * @throws WebSocketException 
	 */
	void broadcastToSharedSession(Token aToken) throws WebSocketException;
}
