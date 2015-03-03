//	---------------------------------------------------------------------------
//	jWebSocket - JWC (Community Edition, CE)
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
package org.jwebsocket.android.demo;

import java.util.List;
import java.util.Properties;

import javolution.util.FastList;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.rpc.Rpc;
import org.jwebsocket.client.plugins.rpc.RpcListener;
import org.jwebsocket.client.plugins.rpc.Rrpc;
import org.jwebsocket.client.token.JWebSocketTokenClient;
import org.jwebsocket.config.ReliabilityOptions;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 *
 * @author Alexander Schulze
 */
// TODO: Remove deprecation annotation from here
@SuppressWarnings("deprecation")
public class JWC {

	private final static int MT_OPENED = 0;
	private final static int MT_PACKET = 1;
	private final static int MT_CLOSED = 2;
	private final static int MT_TOKEN = 3;
	private final static String CONFIG_FILE = "jWebSocket";
	public final static String DEFAULT_URL = "ws://jwebsocket.org:8787";
	private static String mURL;
	private static JWebSocketTokenClient mTokenClient;
	private static List<WebSocketClientTokenListener> mListeners = new FastList<WebSocketClientTokenListener>();
	private static String DEF_ENCODING = "UTF-8";
	private static ReliabilityOptions mReliabilityOptions = null;

	public static void init() {
		// Starting reliability options with true as first parameter to
		// auto-reconnect
		mReliabilityOptions = new ReliabilityOptions(true, 1500, 3000, 1, -1);
		mTokenClient = new JWebSocketTokenClient(mReliabilityOptions);
		mTokenClient.addListener(new Listener());
		mTokenClient.addListener(new RpcListener());
		// TODO: this could be improved if we use client plugins.
		Rpc.setDefaultBaseTokenClient(mTokenClient);
		Rrpc.setDefaultBaseTokenClient(mTokenClient);
	}

	/**
	 *
	 * @param aActivity
	 */
	public static void loadSettings(Activity aActivity) {
		Toast.makeText(aActivity.getApplicationContext(),
				"NOTE: We apologize, but some functionalities of this demo may not work " +
				"as expected, however, it may serve you as example for further projects!",
				Toast.LENGTH_LONG).show();
		
		Properties lProps = new Properties();
		try {
			lProps.load(aActivity.openFileInput(CONFIG_FILE));
		} catch (Exception ex) {
			Toast.makeText(aActivity.getApplicationContext(),
					ex.getClass().getSimpleName() + ":" + ex.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		mURL = lProps.getProperty("url", DEFAULT_URL);
	}

	/**
	 *
	 * @param aActivity
	 */
	public static void saveSettings(Activity aActivity) {
		Properties lProps = new Properties();
		try {
			lProps.put("url", mURL);
			lProps.store(
					aActivity.openFileOutput(CONFIG_FILE, Context.MODE_PRIVATE),
					"jWebSocketClient Configuration");
		} catch (Exception ex) {
			Toast.makeText(aActivity.getApplicationContext(),
					ex.getClass().getSimpleName() + ":" + ex.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 *
	 * @throws WebSocketException
	 */
	public static void open() throws WebSocketException {
		mTokenClient.open(mURL);
	}

	/**
	 *
	 * @throws WebSocketException
	 */
	public static void close() throws WebSocketException {
		mTokenClient.close();
	}

	/**
	 *
	 * @param aString
	 * @throws WebSocketException
	 */
	public static void send(String aString) throws WebSocketException {
		mTokenClient.send(aString, DEF_ENCODING);
	}

	/**
	 *
	 * @param aToken
	 * @throws WebSocketException
	 */
	public static void sendToken(Token aToken) throws WebSocketException {
		mTokenClient.sendToken(aToken);
	}

	/**
	 *
	 * @param aTarget
	 * @param aData
	 * @throws WebSocketException
	 */
	public static void sendText(String aTarget, String aData)
			throws WebSocketException {
		mTokenClient.sendText(aTarget, aData);

	}

	/**
	 *
	 * @param aData
	 * @throws WebSocketException
	 */
	public static void broadcastText(String aData) throws WebSocketException {
		mTokenClient.broadcastText(aData);
	}

	/**
	 *
	 * @param aData
	 * @param aFilename
	 * @param aScope
	 * @param aNotify
	 * @throws WebSocketException
	 */
	public static void saveFile(byte[] aData, String aFilename, String aScope,
			Boolean aNotify) throws WebSocketException {
		mTokenClient.saveFile(aData, aFilename, aScope, aNotify);
	}

	/**
	 *
	 * @param aHeader
	 * @param aData
	 * @param aFilename
	 * @param aTarget
	 * @throws WebSocketException
	 */
	public static void sendFile(String aHeader, byte[] aData, String aFilename,
			String aTarget) throws WebSocketException {
		mTokenClient.sendFile(aHeader, aData, aFilename, aTarget);
	}

	/**
	 *
	 * @param aListener
	 */
	public static void addListener(WebSocketClientTokenListener aListener) {
		if (!mListeners.contains(aListener)) {
			mListeners.add(aListener);
		}
	}

	/**
	 *
	 * @param aListener
	 */
	public static void removeListener(WebSocketClientTokenListener aListener) {
		mListeners.remove(aListener);
	}

	public static boolean isConnected() {
		return null != mTokenClient && mTokenClient.isConnected();
	}

	private static Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message aMessage) {
			switch (aMessage.what) {
				case MT_OPENED:
					notifyOpened(null);
					break;
				case MT_PACKET:
					notifyPacket(null, (RawPacket) aMessage.obj);
					break;
				case MT_TOKEN:
					notifyToken(null, (Token) aMessage.obj);
					break;
				case MT_CLOSED:
					notifyClosed(null);
					break;
			}
		}
	};

	/**
	 *
	 * @param aEvent
	 */
	public static void notifyOpened(WebSocketClientEvent aEvent) {
		for (WebSocketClientTokenListener lListener : mListeners) {
			lListener.processOpened(aEvent);
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aPacket
	 */
	public static void notifyPacket(WebSocketClientEvent aEvent,
			WebSocketPacket aPacket) {
		for (WebSocketClientTokenListener lListener : mListeners) {
			lListener.processPacket(aEvent, aPacket);
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aToken
	 */
	public static void notifyToken(WebSocketClientEvent aEvent, Token aToken) {
		for (WebSocketClientTokenListener lListener : mListeners) {
			lListener.processToken(aEvent, aToken);
		}
	}

	/**
	 *
	 * @param aEvent
	 */
	public static void notifyClosed(WebSocketClientEvent aEvent) {
		for (WebSocketClientTokenListener lListener : mListeners) {
			lListener.processClosed(aEvent);
		}
	}

	/**
	 * @return the URL
	 */
	public static String getURL() {
		return mURL;
	}

	/**
	 * @param aURL the URL to set
	 */
	public static void setURL(String aURL) {
		mURL = aURL;
	}

	/**
	 * @return the mJWC
	 */
	public static JWebSocketTokenClient getClient() {
		return mTokenClient;
	}

	static class Listener implements WebSocketClientTokenListener {

		@Override
		public void processOpened(WebSocketClientEvent aEvent) {
			Message lMsg = new Message();
			lMsg.what = MT_OPENED;
			messageHandler.sendMessage(lMsg);
		}

		@Override
		public void processPacket(WebSocketClientEvent aEvent,
				WebSocketPacket aPacket) {
			Message lMsg = new Message();
			lMsg.what = MT_PACKET;
			lMsg.obj = aPacket;
			messageHandler.sendMessage(lMsg);
		}

		@Override
		public void processToken(WebSocketClientEvent aEvent, Token aToken) {
			Message lMsg = new Message();
			lMsg.what = MT_TOKEN;
			lMsg.obj = aToken;
			messageHandler.sendMessage(lMsg);
		}

		@Override
		public void processClosed(WebSocketClientEvent aEvent) {
			Message lMsg = new Message();
			lMsg.what = MT_CLOSED;
			messageHandler.sendMessage(lMsg);
		}

		@Override
		public void processOpening(WebSocketClientEvent aEvent) {
		}

		@Override
		public void processReconnecting(WebSocketClientEvent aEvent) {
		}

	}
}
