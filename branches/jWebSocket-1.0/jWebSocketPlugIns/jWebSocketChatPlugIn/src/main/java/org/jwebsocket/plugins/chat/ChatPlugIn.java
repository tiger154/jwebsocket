//	---------------------------------------------------------------------------
//	jWebSocket - Chat Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.chat;

import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * This plug-in provides all the chat functionality.
 *
 * @author vbarzana
 */
public class ChatPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private static FastMap<String, WebSocketConnector> mClients =
			new FastMap<String, WebSocketConnector>().shared();

	public ChatPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating chat plug-in...");
		}
		// specify default name space for chat plugin
		this.setNamespace(aConfiguration.getNamespace());

		if (mLog.isInfoEnabled()) {
			mLog.info("Chat plug-in successfully loaded.");
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		if (!mClients.isEmpty()) {
			mClients.remove(aConnector.getId());
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if ("register".equals(aToken.getType())) {
				mClients.put(aConnector.getId(), aConnector);
				getClients(aConnector);
				notifyNewClientConnected(aConnector);
			} else if ("unregister".equals(aToken.getType())) {
				if (mClients.containsKey(aConnector.getId())) {
					mClients.remove(aConnector.getId());
				}
			} else if ("getChatClients".equals(aToken.getType())) {
				getClients(aConnector);
			} else if ("broadcast".equals(aToken.getType())) {
				broadcast(aConnector, aToken);
			} else if ("messageTo".equals(aToken.getType())) {
				sendMessage(aConnector, aToken);
			}
		}
	}

	private void broadcast(WebSocketConnector aConnector, Token aToken) {
		String lMessage = aToken.getString("msg");
		String lUsername = aConnector.getUsername() + "@" + aConnector.getId();
		if (lMessage != null) {
			if (!lMessage.isEmpty()) {
				Token lToken = TokenFactory.createToken(getNamespace(), "broadcast");
				lToken.setString("msg", lMessage);
				lToken.setString("sourceId", lUsername);

				for (Map.Entry<String, WebSocketConnector> lEntry : mClients.entrySet()) {
					getServer().sendToken(lEntry.getValue(), lToken);
				}
			} else {
				getServer().sendErrorToken(aConnector, aToken, -1,
						"You must specify a message");
			}
		} else {
			getServer().sendErrorToken(aConnector, aToken, -1,
					"You must specify a message");
		}
	}

	private void notifyNewClientConnected(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(),
				"newClientConnected");
		lToken.setString("sourceId", aConnector.getUsername() + "@"
				+ aConnector.getId());

		lToken.setString("msg", "New client connected");

		for (Map.Entry<String, WebSocketConnector> lEntry : mClients.entrySet()) {
			WebSocketConnector lConnector = lEntry.getValue();
			// Notify all clients but who is online
			if (!aConnector.getId().equals(lConnector.getId())) {
				getServer().sendToken(lConnector, lToken);
			}
		}
	}

	private void sendMessage(WebSocketConnector aSourceConnector, Token aToken) {
		String lTargetId = aToken.getString("targetId");
		String lMessage = aToken.getString("msg");
		WebSocketConnector lTargetConnector = mClients.get(lTargetId);

		if (lTargetConnector != null && lMessage != null) {
			Token lToken = TokenFactory.createToken(getNamespace(), "messageTo");
			lToken.setString("msg", lMessage);
			lToken.setString("sourceId", aSourceConnector.getUsername() + "@" + aSourceConnector.getId());
			lToken.setString("targetId", lTargetConnector.getUsername() + "@" + lTargetConnector.getId());

			getServer().sendToken(lTargetConnector, lToken);
		} else {
			getServer().sendErrorToken(aSourceConnector, aToken, -1,
					"The message couldn't be sent, please, check the outgoing data");
		}
	}

	private void getClients(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(), "getChatClients");
		FastList<String> lClients = new FastList<String>();

		// Setting myself in the top of the list
//		lClients.add(aConnector.getUsername() + "@" + aConnector.getId());

		for (Map.Entry<String, WebSocketConnector> lEntry : mClients.entrySet()) {
			WebSocketConnector lConnector = lEntry.getValue();
			if (!lConnector.getId().equals(aConnector.getId())) {
				lClients.add(lConnector.getUsername() + "@" + lConnector.getId());
			}
		}

		lToken.setList("clients", lClients);

		getServer().sendToken(aConnector, lToken);
	}
}
