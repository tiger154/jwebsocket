//	---------------------------------------------------------------------------
//	jWebSocket - Chat Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.chat;

import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * This plug-in provides all the chat functionality.
 *
 * @author Victor Antonio Barzana Crespo
 */
public class ChatPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private static final FastMap<String, WebSocketConnector> mClients
			= new FastMap<String, WebSocketConnector>().shared();

	/**
	 *
	 */
	public static final String NS_CHAT
			= JWebSocketServerConstants.NS_BASE + ".plugins.chat";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ChatPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ChatPlugIn - Community Edition";
	private final static String TT_REGISTER = "register";
	private final static String TT_UNREGISTER = "unregister";
	private final static String TT_GET_CLIENTS = "getChatClients";
	private final static String TT_BROADCAST = "broadcast";
	private final static String TT_MESSAGE_TO = "messageTo";
	private final static String TT_EVENT = "event";
	private final static String TT_NEW_CLIENT = "newClientConnected";

	/**
	 * This PlugIn shows how to easily set up a simple jWebSocket based chat
	 * system.
	 *
	 * @param aConfiguration
	 */
	public ChatPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating chat plug-in...");
		}
		// specify default name space for chat plugin
		this.setNamespace(NS_CHAT);

		if (mLog.isInfoEnabled()) {
			mLog.info("Chat plug-in successfully instantiated.");
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_CHAT;
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		unregister(aConnector);
	}

	@Override
	public void processLogoff(WebSocketConnector aConnector) {
		unregister(aConnector);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if (TT_REGISTER.equals(aToken.getType())) {
				mClients.put(aConnector.getId(), aConnector);
				getClients(aConnector);
				notifyNewClientConnected(aConnector);
			} else if (TT_UNREGISTER.equals(aToken.getType())) {
				unregister(aConnector);
			} else if (TT_GET_CLIENTS.equals(aToken.getType())) {
				getClients(aConnector);
			} else if (TT_BROADCAST.equals(aToken.getType())) {
				broadcast(aConnector, aToken);
			} else if (TT_MESSAGE_TO.equals(aToken.getType())) {
				sendMessage(aConnector, aToken);
			}
		}
	}

	private void broadcast(WebSocketConnector aConnector, Token aToken) {
		String lMessage = aToken.getString("msg");
		String lUsername = aConnector.getUsername() + "@" + aConnector.getId();
		if (lMessage != null) {
			if (!lMessage.isEmpty()) {
				Token lToken = TokenFactory.createToken(getNamespace(), TT_BROADCAST);
				lToken.setString("msg", lMessage);
				lToken.setString("sourceId", lUsername);

				for (Map.Entry<String, WebSocketConnector> lEntry : mClients.entrySet()) {
					getServer().sendToken(lEntry.getValue(), lToken);
				}
			} else {
				getServer().sendErrorToken(aConnector, aToken, -1,
						"No message given. Please check!");
			}
		} else {
			getServer().sendErrorToken(aConnector, aToken, -1,
					"No message given. Please check!");
		}
	}

	private void notifyNewClientConnected(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(), TT_NEW_CLIENT);
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
			Token lToken = TokenFactory.createToken(getNamespace(), TT_MESSAGE_TO);
			lToken.setString("msg", lMessage);
			lToken.setString("sourceId", aSourceConnector.getUsername() + "@" + aSourceConnector.getId());
			lToken.setString("targetId", lTargetConnector.getUsername() + "@" + lTargetConnector.getId());

			getServer().sendToken(lTargetConnector, lToken);
		} else {
			getServer().sendErrorToken(aSourceConnector, aToken, -1,
					"The message couldn't be sent, please, check the outgoing data.");
		}
	}

	private void getClients(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(), TT_GET_CLIENTS);
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

	private void unregister(WebSocketConnector aConnector) {
		if (!mClients.isEmpty() && mClients.containsKey(aConnector.getId())) {
			mClients.remove(aConnector.getId());
			if (mClients.size() > 0) {
				for (Map.Entry<String, WebSocketConnector> lEntry : mClients.entrySet()) {
					WebSocketConnector lConnector = lEntry.getValue();
					Token lTk = TokenFactory.createToken(getNamespace(), TT_EVENT);
					lTk.setString("name", "disconnect");
					lTk.setString("sourceId", aConnector.getId());
					getServer().sendToken(lConnector, lTk);
				}
			}
		}
	}
}
