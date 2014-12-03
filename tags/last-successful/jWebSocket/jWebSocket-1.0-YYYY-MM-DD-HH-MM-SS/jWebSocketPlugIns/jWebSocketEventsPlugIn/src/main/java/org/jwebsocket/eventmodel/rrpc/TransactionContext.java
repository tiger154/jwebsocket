//	---------------------------------------------------------------------------
//	jWebSocket - TransactionContext (Community Edition, CE)
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
package org.jwebsocket.eventmodel.rrpc;

import java.util.Map;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 * The Transaction context is a collection of resources used to success back to
 * the target client the response on a S2C call
 *
 * @author Rolando Santamaria Maso
 */
public class TransactionContext {

	private TokenServer server;
	private Token request;
	private Map<String, Object> resources;
	private double processingTime;
	private double elapsedTime;
	private WebSocketConnector senderConnector;

	/**
	 *
	 * @param aServer
	 * @param aSenderConnector
	 * @param aRequest
	 * @param aResources
	 */
	public TransactionContext(TokenServer aServer, WebSocketConnector aSenderConnector,
			Token aRequest, Map<String, Object> aResources) {
		server = aServer;
		request = aRequest;
		resources = aResources;
		senderConnector = aSenderConnector;
	}

	/**
	 *
	 * @return
	 */
	public Token getRequest() {
		return request;
	}

	/**
	 *
	 * @param request
	 */
	public void setRequest(Token request) {
		this.request = request;
	}

	/**
	 *
	 * @return
	 */
	public Map<String, Object> getResources() {
		return resources;
	}

	/**
	 *
	 * @param resources
	 */
	public void setResources(Map<String, Object> resources) {
		this.resources = resources;
	}

	/**
	 *
	 * @return
	 */
	public WebSocketServer getServer() {
		return server;
	}

	/**
	 *
	 * @param server
	 */
	public void setServer(TokenServer server) {
		this.server = server;
	}

	/**
	 *
	 * @return
	 */
	public WebSocketConnector getSenderConnector() {
		return senderConnector;
	}

	/**
	 *
	 * @param senderConnector
	 */
	public void setSenderConnector(WebSocketConnector senderConnector) {
		this.senderConnector = senderConnector;
	}

	/**
	 * Notify the sender connector about the success transaction
	 *
	 * @param response The response from the target client
	 */
	public void success(Object response) {
		Token r = server.createResponse(request);
		if (null != response) {
			r.getMap().put("response", response);
		}
		r.setDouble("processingTime", getProcessingTime());
		r.setDouble("elapsedTime", getElapsedTime());

		server.sendToken(senderConnector, r);
	}

	/**
	 * Notify the sender client about the success transaction
	 */
	public void success() {
		success(null);
	}

	/**
	 * Notify the sender client about the failure transaction
	 *
	 * @param reason Failure reason
	 * @param message Custom failure message
	 */
	public void failure(FailureReason reason, String message) {
		Token r = server.createErrorToken(request, -1, message);
		r.setString("reason", reason.name());
		r.setDouble("elapsedTime", getElapsedTime());

		server.sendToken(senderConnector, r);
	}

	/**
	 * @return Time required by the client to process the event
	 * <p>
	 * Time unit in nanoseconds or milliseconds depending of the client
	 */
	public double getProcessingTime() {
		return processingTime;
	}

	/**
	 * @param processingTime Time required by the client to process the event
	 */
	public void setProcessingTime(double processingTime) {
		this.processingTime = processingTime;
	}

	/**
	 * @return The complete time in nanoseconds passed from the "sent" time mark
	 * to the "response received" time mark
	 */
	public double getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @param elapsedTime The complete time in nanoseconds passed from the
	 * "sent" time mark to the "response received" time mark
	 */
	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
}
