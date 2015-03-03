//	---------------------------------------------------------------------------
//	jWebSocket - StressTests (Community Edition, CE)
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
package org.jwebsocket.tests;

import java.util.Date;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.java.JWebSocketJMSClient;
import org.jwebsocket.client.token.JWebSocketTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Alexander Schulze
 */
public class StressTests implements WebSocketClientTokenListener {

	private int MAX_CONNS = 200;
	private JWebSocketTokenClient[] mClients = new JWebSocketTokenClient[MAX_CONNS];
	private volatile int mFinished = 0;

	/**
	 *
	 */
	public interface LogListener {

		/**
		 *
		 * @param aMessage
		 */
		void log(String aMessage);
	}
	private LogListener mLogListener = null;

	private void mLog(String aMsg) {
		if (null != mLogListener) {
			mLogListener.log(aMsg + "\n");
		}
	}

	/**
	 *
	 * @param aLogListener
	 */
	public StressTests(LogListener aLogListener) {
		mLogListener = aLogListener;
	}

	/**
	 * Opens all test connections to the server identified by its URL.
	 *
	 * @param aURL
	 * @param aJMSCluster
	 * @param aClusterName
	 */
	public void init(String aURL, boolean aJMSCluster, String aClusterName) {
		JWebSocketTokenClient lClient;
		for (int lIdx = 0; lIdx < MAX_CONNS; lIdx++) {
			mLog("Opening client #" + lIdx + " on thread: " + Thread.currentThread().hashCode() + "...");
			if (!aJMSCluster) {
				mClients[lIdx] = new JWebSocketTokenClient();
			} else {
				mClients[lIdx] = new JWebSocketTokenClient(new JWebSocketJMSClient(aClusterName));
			}
			lClient = mClients[lIdx];
			lClient.setParam("idx", lIdx);
			lClient.addTokenClientListener(this);
			try {
				lClient.open(aURL);
			} catch (Exception lEx) {
				mLog("Exception: " + lEx.getMessage());
			}
		}
	}

	/**
	 * Closes all test connections to the server.
	 */
	public void exit() {
		JWebSocketTokenClient lClient;
		for (int lIdx = 0; lIdx < mFinished; lIdx++) {
			lClient = mClients[lIdx];
			lClient.removeTokenClientListener(this);
			try {
				mLog("Closing client #" + lIdx + " on thread: " + Thread.currentThread().hashCode() + "...");
				lClient.close();
				Thread.sleep(20);
			} catch (Exception lEx) {
				mLog("Exception: " + lEx.getMessage() + ". Closing client #" + lIdx + "...");
			}
		}
	}

	/**
	 *
	 * @param aURL
	 * @param aJMSCluster
	 * @param aClusterName
	 */
	public void runStressTest(String aURL, boolean aJMSCluster, String aClusterName) {
		init(aURL, aJMSCluster, aClusterName);
		long lTimeout = 5000 * MAX_CONNS;
		long lStart = new Date().getTime();
		while (new Date().getTime() - lStart < lTimeout && mFinished < MAX_CONNS) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ex) {
				mLog(ex.getClass().getSimpleName() + " at stressTest: " + ex.getMessage());
			}
		}
		exit();
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		JWebSocketTokenClient lClient = (JWebSocketTokenClient) aEvent.getClient();

		String lNS = aToken.getNS();
		String lType = aToken.getType();
		String lName = aToken.getString("name");
		String lSourceId = aToken.getString("sourceId");
		String lReqType = aToken.getString("reqType");

		if ("org.jwebsocket.plugins.system".equals(lNS)) {
			if ("welcome".equals(lType)) {
				try {
					mLog("Logging in client #" + lClient.getParam("idx") + "...");
					lClient.login("guest", "guest");
				} catch (WebSocketException ex) {
					mLog(ex.getClass().getSimpleName() + " at login: " + ex.getMessage());
				}
			} else if ("login".equals(lReqType)) {
				mLog("Sending echo request for client #" + lClient.getParam("idx") + "...");
				Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "echo");
				lToken.setString("data", "test");
				try {
					lClient.sendToken(lToken);
				} catch (WebSocketException ex) {
					mLog(ex.getClass().getSimpleName() + " at sendToken: " + ex.getMessage());
				}
			} else if ("echo".equals(lReqType)) {
				mLog("Received echo response for client #" + lClient.getParam("idx") + ".");
				mFinished++;
			} else if ("connect".equals(lName)) {
				mLog("Received client (" + lSourceId + ") connect.");
			} else if ("disconnect".equals(lName)) {
				mLog("Received client (" + lSourceId + ") disconnect.");
			} else if ("login".equals(lName)) {
				mLog("Received client (" + lSourceId + ") login.");
			} else if ("logout".equals(lName)) {
				mLog("Received client (" + lSourceId + ") logout.");
			} else {
				mLog("Received other system plug-in token from network.");
			}
		} else {
			mLog("Received non system plug-in token from network.");
		}
	}

	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
	}
}
