// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
// for more details.
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.android.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import java.util.List;
import java.util.Properties;
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.rpc.Rpc;
import org.jwebsocket.client.plugins.rpc.RpcListener;
import org.jwebsocket.client.plugins.rpc.Rrpc;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class JWC {

	private final static int MT_OPENED = 0;
	private final static int MT_PACKET = 1;
	private final static int MT_CLOSED = 2;
	private final static int MT_TOKEN = 3;
	private final static String CONFIG_FILE = "jWebSocket";
	private static String mURL = "ws://jwebsocket.org:8787";
	private static BaseTokenClient mJWC;
	private static List<WebSocketClientTokenListener> mListeners = new FastList<WebSocketClientTokenListener>();
	private static String DEF_ENCODING = "UTF-8";

	public static void init() {
		mJWC = new BaseTokenClient();
		mJWC.addListener(new Listener());
		mJWC.addListener(new RpcListener());
		//TODO: this could be improve if we use client plugins.
		Rpc.setDefaultBaseTokenClient(mJWC);
		Rrpc.setDefaultBaseTokenClient(mJWC);
	}

	public static void loadSettings(Activity aActivity) {
		Properties lProps = new Properties();
		try {
			lProps.load(aActivity.openFileInput(CONFIG_FILE));
		} catch (Exception ex) {
			Toast.makeText(aActivity.getApplicationContext(),
					ex.getClass().getSimpleName() + ":" + ex.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
		mURL = (String) lProps.getProperty("url", "ws://jwebsocket.org:8787/");
	}

	public static void saveSettings(Activity aActivity) {
		Properties lProps = new Properties();
		try {
			lProps.put("url", mURL);
			lProps.save(aActivity.openFileOutput(CONFIG_FILE, Context.MODE_PRIVATE), "jWebSocketClient Configuration");
		} catch (Exception ex) {
			Toast.makeText(aActivity.getApplicationContext(),
					ex.getClass().getSimpleName() + ":" + ex.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	public static void open() throws WebSocketException {
		mJWC.open(mURL);
	}

	public static void close() throws WebSocketException {
		mJWC.close();
	}

	public static void send(String aString) throws WebSocketException {
		mJWC.send(mURL, DEF_ENCODING);
	}

	public static void sendToken(Token aToken) throws WebSocketException {
		mJWC.sendToken(aToken);
	}

	public static void sendText(String aTarget, String aData) throws WebSocketException {
		mJWC.sendText(aTarget, aData);

	}

	public static void broadcastText(String aData) throws WebSocketException {
		mJWC.broadcastText(aData);
	}

	public static void saveFile(byte[] aData, String aFilename, String aScope,
			Boolean aNotify) throws WebSocketException {
		mJWC.saveFile(aData, aFilename, aScope, aNotify);
	}

	public static void sendFile(String aHeader, byte[] aData, String aFilename, String aTarget)
			throws WebSocketException {
		mJWC.sendFile(aHeader, aData, aFilename, aTarget);
	}

	public static void addListener(WebSocketClientTokenListener aListener) {
		mListeners.add(aListener);
	}

	public static void removeListener(WebSocketClientTokenListener aListener) {
		mListeners.remove(aListener);
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

	public static void notifyOpened(WebSocketClientEvent aEvent) {
		for (WebSocketClientTokenListener lListener : mListeners) {
			lListener.processOpened(aEvent);
		}
	}

	public static void notifyPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		for (WebSocketClientTokenListener lListener : mListeners) {
			lListener.processPacket(aEvent, aPacket);
		}
	}

	public static void notifyToken(WebSocketClientEvent aEvent, Token aToken) {
		for (WebSocketClientTokenListener lListener : mListeners) {
			lListener.processToken(aEvent, aToken);
		}
	}

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

	static class Listener implements WebSocketClientTokenListener {

		@Override
		public void processOpened(WebSocketClientEvent aEvent) {
			Message lMsg = new Message();
			lMsg.what = MT_OPENED;
			messageHandler.sendMessage(lMsg);
		}

		@Override
		public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
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
