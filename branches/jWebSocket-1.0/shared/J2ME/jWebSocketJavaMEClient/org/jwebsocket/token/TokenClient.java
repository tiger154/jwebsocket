//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.token;

import j2me.util.Iterator;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.me.BaseClientJ2ME;
import org.jwebsocket.listener.WebSocketClientEvent;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.packetProcessors.JSONProcessor;

/**
 *
 * @author aschulze
 */
public class TokenClient {

	/**
	 *
	 */
	public final static int DISCONNECTED = 0;
	/**
	 *
	 */
	public final static int CONNECTED = 1;
	/**
	 *
	 */
	public final static int AUTHENTICATED = 2;
	private int CUR_TOKEN_ID = 0;
	private BaseClientJ2ME client = null;
	private String lSubProt = "json"; // is hardcoded for java me
	private final static String NS_BASE = "org.jwebsocket";
	private String fUsername = null;
	private String fClientId = null;
	private String fSessionId = null;
	private String fRestoreSessionId = null;
	public String DEMO = "DEMO";

	/**
	 *
	 * @param aClient
	 */
	public TokenClient(BaseClientJ2ME aClient) {
		client = aClient;
		client.addListener(new Listener());
	}

	/**
	 *
	 * @return
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * @return the fUsername
	 */
	public String getUsername() {
		return fUsername;
	}

	/**
	 * @return the fClientId
	 */
	public String getClientId() {
		return fClientId;
	}

	/**
	 * @return the fSessionId
	 */
	public String getfSessionId() {
		return fSessionId;
	}

	/**
	 *
	 * @param aListener
	 */
	public void addListener(WebSocketClientTokenListener aListener) {
		client.addListener(aListener);
	}

	/**
	 *
	 * @param aListener
	 */
	public void removeListener(WebSocketClientTokenListener aListener) {
		client.removeListener(aListener);
	}

	/**
	 *
	 * @param aURL
	 * @throws WebSocketException
	 */
	public void open(String aURL) throws WebSocketException {
		try {
			client.open(aURL);
		} catch (Exception ex) {
			throw new WebSocketException("I can't: " + ex.getMessage());
		}
	}

	/**
	 *
	 * @param aData
	 * @param aEncoding
	 * @throws WebSocketException
	 */
	public void send(String aData, String aEncoding) throws WebSocketException {
		client.send(aData, aEncoding);
	}

	/**
	 *
	 * @param aData
	 * @throws WebSocketException
	 */
	public void send(byte[] aData) throws WebSocketException {
		client.send(aData);
	}

	/**
	 *
	 * @param aPacket
	 * @throws WebSocketException
	 */
	public void send(WebSocketPacket aPacket) throws WebSocketException {
		client.send(aPacket.getByteArray());
	}

	/**
	 *
	 * @throws WebSocketException
	 */
	public void close() throws WebSocketException {
		fUsername = null;
		fClientId = null;
		fRestoreSessionId = fSessionId;
		fSessionId = null;
		client.close();
	}

	// TODO: Check if the following two methods packetToToken and tokenToPacket can be shared for server and client
	/**
	 *
	 * @param aPacket
	 * @return
	 */
	public Token packetToToken(WebSocketPacket aPacket) {
		Token lToken = JSONProcessor.packetToToken(aPacket);
		return lToken;
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	public WebSocketPacket tokenToPacket(Token aToken) {
		WebSocketPacket lPacket = JSONProcessor.tokenToPacket(aToken);
		return lPacket;
	}

	/**
	 *
	 * @param aToken
	 * @throws WebSocketException
	 */
	public void sendToken(Token aToken) throws WebSocketException {
		CUR_TOKEN_ID++;
		aToken.put("utid", new Integer(CUR_TOKEN_ID));
		send(tokenToPacket(aToken));
	}
	// TODO: put the following methods into client side plug-ins or separate them in a different way.

	/* functions of the System Plug-in */
	private final static String NS_SYSTEM_PLUGIN = NS_BASE + ".plugins.system";

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @throws WebSocketException
	 */
	public void login(String aUsername, String aPassword) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("type", "login");
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("username", aUsername);
		lToken.put("password", aPassword);
		sendToken(lToken);
	}

	/**
	 *
	 * @throws WebSocketException
	 */
	public void logout() throws WebSocketException {
		Token lToken = new Token();
		lToken.put("type", "logout");
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		sendToken(lToken);
	}

	/**
	 *
	 * @param aEcho
	 * @throws WebSocketException
	 */
	public void ping(boolean aEcho) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("type", "ping");
		lToken.put("echo", new Boolean(aEcho)); //
		sendToken(lToken);
	}

	/**
	 *
	 * @param aTarget
	 * @param aData
	 * @throws WebSocketException
	 */
	public void sendText(String aTarget, String aData) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("type", "send");
		lToken.put("targetId", aTarget);
		lToken.put("sourceId", getClientId());
		lToken.put("sender", getUsername());
		lToken.put("data", aData);
		sendToken(lToken);
	}

	/**
	 *
	 * @param aData
	 * @throws WebSocketException
	 */
	public void broadcastText(String aData) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("type", "broadcast");
		lToken.put("sourceId", getClientId());
		lToken.put("sender", getUsername());
		lToken.put("data", aData);
		lToken.put("senderIncluded", Boolean.FALSE);
		lToken.put("responseRequested", Boolean.TRUE);
		sendToken(lToken);
	}

	/* functions of the Admin Plug-in */
	private final static String NS_ADMIN_PLUGIN = NS_BASE + ".plugins.admin";

	/**
	 *
	 * @throws WebSocketException
	 */
	public void shutdownServer() throws WebSocketException {
		Token lToken = new Token();
		lToken.put("type", "shutdown");
		lToken.put("ns", NS_ADMIN_PLUGIN);
		sendToken(lToken);
	}

	class Listener implements WebSocketClientListener {

		public void processOpened(WebSocketClientEvent aEvent) {
			fUsername = null;
			fClientId = null;
			fSessionId = null;
		}

		public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
			Iterator lIterator = client.getListeners().iterator();
			while (lIterator.hasNext()) {
				WebSocketClientListener lListener = (WebSocketClientListener) lIterator.next();
				if (lListener instanceof WebSocketClientTokenListener) {
					Token lToken = packetToToken(aPacket);

					String lNS = lToken.getNS();
					String lType = lToken.getType();
					String lReqType = lToken.getString("reqType");

					if (lType != null) {
						if ("welcome".equals(lType)) {
							fClientId = lToken.getString("sourceId");
							fSessionId = lToken.getString("usid");
						} else if ("goodBye".equals(lType)) {
							fUsername = null;
						}
					}
					if (lReqType != null) {
						if ("login".equals(lReqType)) {
							fUsername = lToken.getString("username");
						} else if ("logout".equals(lReqType)) {
							fUsername = null;
						}
					}
					((WebSocketClientTokenListener) lListener).processToken(aEvent, lToken);
				}
			}
		}

		public void processClosed(WebSocketClientEvent aEvent) {
			fUsername = null;
			fClientId = null;
			fRestoreSessionId = fSessionId;
			fSessionId = null;
		}
	}
}
