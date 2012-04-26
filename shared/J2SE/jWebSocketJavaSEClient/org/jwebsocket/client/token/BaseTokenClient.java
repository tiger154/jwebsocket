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
package org.jwebsocket.client.token;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javolution.util.FastMap;
import org.apache.commons.codec.binary.Base64;
import org.jwebsocket.api.*;
import org.jwebsocket.client.java.BaseWebSocketClient;
import org.jwebsocket.client.java.ReliabilityOptions;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.WebSocketEncoding;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketSubProtocol;
import org.jwebsocket.packetProcessors.CSVProcessor;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.packetProcessors.XMLProcessor;
import org.jwebsocket.token.PendingResponseQueueItem;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.util.Tools;

/**
 * Token based implementation of {@code JWebSocketClient}
 *
 * @author aschulze
 * @author puran
 * @author jang
 * @version $Id:$
 */
public class BaseTokenClient extends BaseWebSocketClient implements WebSocketTokenClient {

	/**
	 * base name space for jWebSocket
	 */
	private final static String NS_BASE = "org.jwebsocket";
	/**
	 * token client protocols
	 */
	private final static String WELCOME = "welcome";
	private final static String LOGIN = "login";
	private final static String SPRING_LOGON = "logon";
	private final static String SPRING_LOGOFF = "logoff";
	private final static String GOODBYE = "goodBye";
	private final static String LOGOUT = "logout";
	/**
	 * token id
	 */
	private int CUR_TOKEN_ID = 0;
	/**
	 * sub protocol value
	 */
	private WebSocketSubProtocol mSubProt = null;
	// private String mSubProt;
	// private WebSocketEncoding mEncoding;
	private String mUsername = null;
	private String fClientId = null;
	private final Map<Integer, PendingResponseQueueItem> mPendingResponseQueue =
			new FastMap<Integer, PendingResponseQueueItem>().shared();
	private final ScheduledThreadPoolExecutor mResponseQueueExecutor =
			new ScheduledThreadPoolExecutor(1);

	/**
	 * Default constructor
	 */
	public BaseTokenClient() {
		this(JWebSocketCommonConstants.WS_SUBPROT_DEFAULT, JWebSocketCommonConstants.WS_ENCODING_DEFAULT);
	}

	/**
	 *
	 * @param aReliabilityOptions
	 */
	public BaseTokenClient(ReliabilityOptions aReliabilityOptions) {
		this(JWebSocketCommonConstants.WS_SUBPROT_DEFAULT, JWebSocketCommonConstants.WS_ENCODING_DEFAULT);
		setReliabilityOptions(aReliabilityOptions);
	}

	/**
	 *
	 * @param aSubProt
	 * @param aEncoding
	 */
	public BaseTokenClient(String aSubProt, WebSocketEncoding aEncoding) {
		mSubProt = new WebSocketSubProtocol(aSubProt, aEncoding);
		addSubProtocol(mSubProt);
		addListener(new TokenClientListener());
	}

	/**
	 *
	 * @param aSubProt
	 */
	public BaseTokenClient(WebSocketSubProtocol aSubProt) {
		mSubProt = aSubProt;
		addSubProtocol(mSubProt);
		addListener(new TokenClientListener());
	}

	/**
	 * WebSocketClient listener implementation that receives the data packet and
	 * creates <tt>token</tt> objects
	 *
	 * @author aschulze
	 */
	class TokenClientListener implements WebSocketClientListener {

		/**
		 * {@inheritDoc} Initialize all the variables when the process starts
		 */
		@Override
		public void processOpening(WebSocketClientEvent aEvent) {
		}

		/**
		 * {@inheritDoc} Initialize all the variables when the process starts
		 */
		@Override
		public void processOpened(WebSocketClientEvent aEvent) {
			mUsername = null;
			fClientId = null;
		}

		/**
		 * {@inheritDoc} This callback method is invoked by jWebSocket client
		 * after the data is received from low-level <tt>WebSocket</tt>
		 * connection. This method then generates the <tt>token</tt> objects
		 * using the data packets.
		 */
		@Override
		public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {

			Token lToken = packetToToken(aPacket);

			String lType = lToken.getType();
			String lReqType = lToken.getString("reqType");

			if (lType != null) {
				if (WELCOME.equals(lType)) {
					fClientId = lToken.getString("sourceId");
					String lUsername = lToken.getString("username");
					if (null != lUsername && !lUsername.equals("anonymous")) {
						mUsername = lUsername;
						mStatus = WebSocketStatus.AUTHENTICATED;
					}
				} else if (GOODBYE.equals(lType)) {
					mUsername = null;
				}
			}
			if (lReqType != null) {
				if (LOGIN.equals(lReqType) || SPRING_LOGON.equals(lReqType)) {
					mUsername = lToken.getString("username");
					mStatus = WebSocketStatus.AUTHENTICATED;
				} else if (LOGOUT.equals(lReqType) || SPRING_LOGOFF.equals(lReqType)) {
					mStatus = WebSocketStatus.OPEN;
					mUsername = null;
				}
			}

			//Notifying pending OnResponse callbacks if exists
			synchronized (mPendingResponseQueue) {
				// check if the response token is part of the pending responses queue
				Integer lUTID = lToken.getInteger("utid");
				Integer lCode = lToken.getInteger("code");
				// is there unique token id available in the response
				// and is there a matching pending response at all?
				PendingResponseQueueItem lPRQI =
						(lUTID != null ? mPendingResponseQueue.get(lUTID) : null);
				if (lPRQI != null) {
					// if so start analyzing
					WebSocketResponseTokenListener lWSRTL = lPRQI.getListener();
					if (lWSRTL != null) {
						// fire on response
						lWSRTL.OnResponse(lToken);
						// usable response code available?
						if (lCode != null) {
							if (lCode == 0) {
								lWSRTL.OnSuccess(lToken);
							} else {
								lWSRTL.OnFailure(lToken);
							}
						}
					}
					// and drop the pending queue item
					mPendingResponseQueue.remove(lUTID);
				}
			}

			//Notifying listeners
			for (WebSocketClientListener lListener : getListeners()) {
				if (lListener instanceof WebSocketClientTokenListener) {
					((WebSocketClientTokenListener) lListener).processToken(aEvent, lToken);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void processClosed(WebSocketClientEvent aEvent) {
			// clean up resources
			mUsername = null;
			fClientId = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void processReconnecting(WebSocketClientEvent aEvent) {
		}
	}

	@Override
	public void addTokenClientListener(WebSocketClientTokenListener aTokenListener) {
		super.addListener(aTokenListener);
	}

	@Override
	public void removeTokenClientListener(WebSocketClientTokenListener aTokenListener) {
		super.removeListener(aTokenListener);
	}

	/**
	 * {@
	 */
	@Override
	public void close() {
		super.close();
		mUsername = null;
		fClientId = null;
	}

	/**
	 * @return the fUsername
	 */
	@Override
	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String aUsername) {
		this.mUsername = aUsername;
	}

	@Override
	public boolean isAuthenticated() {
		return (mUsername != null);
	}

	/**
	 * @return the fClientId
	 */
	public String getClientId() {
		return fClientId;
	}

	/**
	 *
	 * @param aPacket
	 * @return
	 */
	public Token packetToToken(WebSocketPacket aPacket) {
		Token lToken = null;
		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(mSubProt.getFormat())) {
			lToken = JSONProcessor.packetToToken(aPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(mSubProt.getFormat())) {
			lToken = CSVProcessor.packetToToken(aPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(mSubProt.getFormat())) {
			lToken = XMLProcessor.packetToToken(aPacket);
		}

		return lToken;
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	public WebSocketPacket tokenToPacket(Token aToken) {
		WebSocketPacket lPacket = null;

		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(mSubProt.getFormat())) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(mSubProt.getFormat())) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(mSubProt.getFormat())) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}

		return lPacket;
	}

	/**
	 *
	 * @param aToken
	 * @throws WebSocketException
	 */
	public void sendToken(Token aToken) throws WebSocketException {
		CUR_TOKEN_ID++;
		aToken.setInteger("utid", CUR_TOKEN_ID);
		super.send(tokenToPacket(aToken));
	}

	private class ResponseTimeoutTimer implements Runnable {

		private Integer mUTID = 0;

		public ResponseTimeoutTimer(Integer aUTID) {
			mUTID = aUTID;
		}

		@Override
		public void run() {
			synchronized (mPendingResponseQueue) {
				PendingResponseQueueItem lPRQI =
						(mUTID != null ? mPendingResponseQueue.get(mUTID) : null);
				if (lPRQI != null) {
					// if so start analyzing
					WebSocketResponseTokenListener lWSRTL = lPRQI.getListener();
					if (lWSRTL != null) {
						// fire on response
						lWSRTL.OnTimeout(lPRQI.getToken());
					}
					// and drop the pending queue item
					mPendingResponseQueue.remove(mUTID);
				}
			}
		}
	}

	/**
	 *
	 * @param aToken
	 * @param aResponseListener
	 * @throws WebSocketException
	 */
	public void sendToken(Token aToken, WebSocketResponseTokenListener aResponseListener) throws WebSocketException {
		PendingResponseQueueItem lPRQI = new PendingResponseQueueItem(aToken, aResponseListener);
		int lUTID = CUR_TOKEN_ID + 1;
		mPendingResponseQueue.put(lUTID, lPRQI);
		ResponseTimeoutTimer lRTT = new ResponseTimeoutTimer(lUTID);
		mResponseQueueExecutor.schedule(lRTT, aResponseListener.getTimeout(), TimeUnit.MILLISECONDS);
		sendToken(aToken);
	}
	private final static String NS_SYSTEM_PLUGIN = NS_BASE + ".plugins.system";

	@Override
	public void login(String aUsername, String aPassword) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "login");
		lToken.setString("username", aUsername);
		lToken.setString("password", aPassword);
		sendToken(lToken);
	}

	@Override
	public void logout() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "logout");
		sendToken(lToken);
	}

	@Override
	public void ping(boolean aEcho) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "ping");
		lToken.setBoolean("echo", aEcho);
		sendToken(lToken);
	}

	@Override
	public void sendText(String aTarget, String aData) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "send");
		lToken.setString("targetId", aTarget);
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("data", aData);
		sendToken(lToken);
	}

	@Override
	public void broadcastText(String aData) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "broadcast");
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("data", aData);
		lToken.setBoolean("senderIncluded", false);
		lToken.setBoolean("responseRequested", true);
		sendToken(lToken);
	}
	private final static String NS_FILESYSTEM_PLUGIN = NS_BASE + ".plugins.filesystem";

	// @Override
	/**
	 *
	 * @param aData
	 * @param aFilename
	 * @param aScope
	 * @param aNotify
	 * @throws WebSocketException
	 */
	public void saveFile(byte[] aData, String aFilename, String aScope, Boolean aNotify) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_FILESYSTEM_PLUGIN, "save");
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("filename", aFilename);
		// TODO: set mimetype correctly according to file extension based on configuration in jWebSocket.xml
		lToken.setString("mimetype", "image/jpeg");
		lToken.setString("scope", aScope);
		lToken.setBoolean("notify", aNotify);

		// lToken.setString("data", Base64.encodeBase64String(aData));
		lToken.setString("data", Tools.base64Encode(aData));
		sendToken(lToken);
	}

	/**
	 *
	 * @param aHeader
	 * @param aData
	 * @param aFilename
	 * @param aTarget
	 * @throws WebSocketException
	 */
	public void sendFile(String aHeader, byte[] aData, String aFilename, String aTarget) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_FILESYSTEM_PLUGIN, "send");
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("filename", aFilename);
		// TODO: set mimetype correctly according to file extension based on configuration in jWebSocket.xml
		lToken.setString("mimetype", "image/jpeg");
		lToken.setString("unid", aTarget);

		lToken.setString("data", aHeader + new String(Base64.encodeBase64(aData)));
		sendToken(lToken);
	}

	/*
	 * functions of the Admin Plug-in
	 */
	private final static String NS_ADMIN_PLUGIN = NS_BASE + ".plugins.admin";

	@Override
	public void disconnect() throws WebSocketException {
	}

	/**
	 *
	 * @throws WebSocketException
	 */
	public void shutdown() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "shutdown");
		sendToken(lToken);
	}

	@Override
	public void getConnections() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "getConnections");
		sendToken(lToken);
	}

	/**
	 *
	 * @param aUsername
	 * @throws WebSocketException
	 */
	public void getUserRights(String aUsername) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "getUserRights");
		lToken.setString("username", aUsername);
		sendToken(lToken);
	}

	/**
	 *
	 * @param aUsername
	 * @throws WebSocketException
	 */
	public void getUserRoles(String aUsername) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "getUserRoles");
		lToken.setString("username", aUsername);
		sendToken(lToken);
	}
}
