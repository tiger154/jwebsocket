/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.tests;

import java.util.Date;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author aschulze
 */
public class StressTests implements WebSocketClientTokenListener {

	private int MAX_CONNS = 50;
	private BaseTokenClient[] mClients = new BaseTokenClient[MAX_CONNS];
	private int PROT_VER = 8;
	private volatile int mFinished = 0;

	public interface LogListener {

		void log(String aMessage);
	}
	private LogListener mLogListener = null;

	private void mLog(String aMsg) {
		if (null != mLogListener) {
			mLogListener.log(aMsg + "\n");
		}
	}

	public StressTests(LogListener aLogListener) {
		mLogListener = aLogListener;
	}

	/**
	 * Opens all test connections to the server 
	 * identified by its URL.
	 * @param aURL
	 */
	public void init(String aURL) {
		BaseTokenClient lClient;
		for (int lIdx = 0; lIdx < MAX_CONNS; lIdx++) {
			mLog("Opening client #" + lIdx + "...");
			mClients[lIdx] = new BaseTokenClient();
			lClient = mClients[lIdx];
			lClient.setParam("idx", lIdx);
			lClient.addTokenClientListener(this);
			lClient.open(PROT_VER, aURL);
		}
	}

	/**
	 * Closes all test connections to the server.
	 */
	public void exit() {
		BaseTokenClient lClient;
		for (int lIdx = 0; lIdx < MAX_CONNS; lIdx++) {
			lClient = mClients[lIdx];
			lClient.removeTokenClientListener(this);
			try {
				mLog("Closing client #" + lIdx + "...");
				lClient.close();
				Thread.sleep(20);
			} catch (Exception lEx) {
			}
		}
	}

	public void runStressTest(String aURL) {
		init(aURL);
		long lTimeout = 10000;
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
		BaseTokenClient lClient = (BaseTokenClient) aEvent.getClient();

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
