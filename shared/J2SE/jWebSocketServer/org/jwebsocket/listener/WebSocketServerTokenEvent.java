//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketServerTokenEvent (Community Edition, CE)
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
package org.jwebsocket.listener;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class WebSocketServerTokenEvent extends WebSocketServerEvent {

	/**
	 *
	 * @param aConnector
	 * @param aServer
	 */
	public WebSocketServerTokenEvent(WebSocketConnector aConnector, WebSocketServer aServer) {
		super(aConnector, aServer);
	}

	/**
	 *
	 * @param aToken
	 */
	public void sendToken(Token aToken) {
		((TokenServer) getServer()).sendToken(getConnector(), aToken);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>createResponse</tt> to simplify token plug-in code.
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createResponse(Token aInToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			return lServer.createResponse(aInToken);
		} else {
			return null;
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>createAccessDenied</tt> to simplify token plug-in code.
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createAccessDenied(Token aInToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			return lServer.createAccessDenied(aInToken);
		} else {
			return null;
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void sendToken(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			lServer.sendToken(aSource, aTarget, aToken);
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	/*
	 public void sendToken(WebSocketConnector aTarget, Token aToken) {
	 TokenServer lServer = getServer();
	 if (lServer != null) {
	 lServer.sendToken(aTarget, aToken);
	 }
	 }
	 */
	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 *
	 * @param aSource
	 * @param aToken
	 */
	public void broadcastToken(WebSocketConnector aSource, Token aToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			lServer.broadcastToken(aSource, aToken);
		}
	}

	/**
	 *
	 * @param aSource
	 * @param aToken
	 * @param aBroadcastOptions
	 */
	public void broadcastToken(WebSocketConnector aSource, Token aToken,
			BroadcastOptions aBroadcastOptions) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			lServer.broadcastToken(aSource, aToken, aBroadcastOptions);
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getUsername</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 * @return
	 */
	public String getUsername(WebSocketConnector aConnector) {
		return ((TokenServer) getServer()).getUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>setUsername</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 * @param aUsername
	 */
	public void setUsername(WebSocketConnector aConnector, String aUsername) {
		((TokenServer) getServer()).setUsername(aConnector, aUsername);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>removeUsername</tt> to simplify token plug-in code.
	 *
	 * @param aConnector
	 */
	public void removeUsername(WebSocketConnector aConnector) {
		((TokenServer) getServer()).removeUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getConnector</tt> to simplify token plug-in code.
	 *
	 * @param aId
	 * @return
	 */
	public WebSocketConnector getConnector(String aId) {
		return ((TokenServer) getServer()).getConnector(aId);
	}

	/**
	 * Convenience method to simplify token plug-in code.
	 *
	 * @return
	 */
	public int getConnectorCount() {
		return ((TokenServer) getServer()).getConnectorsCount().intValue();
	}
}
