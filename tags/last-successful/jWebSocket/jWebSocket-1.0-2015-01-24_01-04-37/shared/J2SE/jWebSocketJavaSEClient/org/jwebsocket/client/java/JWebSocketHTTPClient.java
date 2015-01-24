//	---------------------------------------------------------------------------
//	jWebSocket - JWebSocketHTTPClient (Community Edition, CE)
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
package org.jwebsocket.client.java;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.jwebsocket.api.WebSocketBaseClientEvent;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.IsAlreadyConnectedException;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketEncoding;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.kit.WebSocketSubProtocol;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * HTTP WebSocket client implementation for the server-side HTTP Engine
 *
 * @author Rolando Santamaria Maso
 */
public class JWebSocketHTTPClient extends BaseClient {

	private boolean mConnected = false;
	private long mSyncInterval;
	public final static long DEFAULT_SYNC_INTERVAL = 1000 * 3;
	private Proxy mHttpProxy;

	public JWebSocketHTTPClient() {
		this(DEFAULT_SYNC_INTERVAL);
	}

	/**
	 *
	 * @param aSyncInterval The synchronization time interval
	 */
	public JWebSocketHTTPClient(long aSyncInterval) {
		this(null, aSyncInterval);
	}

	public JWebSocketHTTPClient(Proxy aHttpProxy) {
		this(aHttpProxy, DEFAULT_SYNC_INTERVAL);
	}

	public JWebSocketHTTPClient(Proxy aHttpProxy, long aSyncInterval) {
		mHttpProxy = aHttpProxy;
		mSyncInterval = aSyncInterval;
		Assert.isTrue(mSyncInterval > 200, "The synchronization interval value should be higher than 200!");
	}

	public long getSyncInterval() {
		return mSyncInterval;
	}

	public void setSyncInterval(long aSyncInterval) {
		mSyncInterval = aSyncInterval;
	}

	HttpURLConnection getHttpConnection(String aQuery) throws IOException {
		HttpURLConnection lConnection;
		if (null == mHttpProxy) {
			lConnection = (HttpURLConnection) new URL(mURI.toString() + aQuery).openConnection();
		} else {
			lConnection = (HttpURLConnection) new URL(mURI.toString() + aQuery).openConnection(mHttpProxy);
		}

		lConnection.setRequestMethod("GET");
		return lConnection;
	}

	String getResponseText(HttpURLConnection aReq) throws IOException {
		InputStream lIS = new BufferedInputStream(aReq.getInputStream());
		// chain the InputStream to a Reader
		Reader r = new InputStreamReader(lIS);
		StringBuilder lStringB = new StringBuilder();

		int lCh;
		while ((lCh = r.read()) != -1) {
			lStringB.append((char) lCh);
		}

		return lStringB.toString();
	}

	void sync() throws Exception {
		if (isConnected()) {
			HttpURLConnection lReq = getHttpConnection("&action=sync");
			String lResponseText = getResponseText(lReq);
			int lResponseCode = lReq.getResponseCode();

			if (lResponseCode == 200) {
				Token lResponse = JSONProcessor.JSONStringToToken("{\"data\":" + lResponseText + "}");
				List<String> lPackets = (List<String>) lResponse.getList("data");

				for (String lPacket : lPackets) {
					if ("http.command.close".equals(lPacket)) {
						mConnected = false;
						setStatus(WebSocketStatus.CLOSED);
						// notifying close event
						WebSocketBaseClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_CLOSE, "SERVER");
						notifyClosed(lEvent);

						return;
					} else {
						notifyPacket(new WebSocketBaseClientEvent(this, "message", lResponseText),
								new RawPacket(lPacket));
					}
				}
			} else {
				throw new WebSocketException(lReq.getResponseMessage());
			}

			Tools.getTimer().schedule(new JWSTimerTask() {

				@Override
				protected void runTask() {
					try {
						sync();
					} catch (Exception lEx) {

					}
				}
			}, mSyncInterval);
		}
	}

	@Override
	public void open(String aURL) throws WebSocketException {
		if (isConnected()) {
			throw new IsAlreadyConnectedException("HTTP connection already started!");
		}

		try {
			mURI = new URI(aURL);
			HttpURLConnection lReq = getHttpConnection("&action=open");

			// notifying logic "opening" listeners notification
			// we consider that a client has finally openned when 
			// the "max frame size" handshake has completed 
			final WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_OPENING, null);
			for (final WebSocketClientListener lListener : getListeners()) {
				getListenersExecutor().submit(new Runnable() {
					@Override
					public void run() {
						try {
							lListener.processOpening(lEvent);
						} catch (Exception lEx) {
							// nothing, soppose to be catched internally
						}
					}
				});
			}

			String lResponseText = getResponseText(lReq);
			int lResponseCode = lReq.getResponseCode();

			if (lResponseCode == 200) {
				mConnected = true;
				notifyPacket(new WebSocketBaseClientEvent(this, "message", lResponseText),
						new RawPacket(lResponseText));
				// call sync
				sync();
			} else {
				throw new WebSocketException(lResponseText);
			}

		} catch (Exception lEx) {
			throw new WebSocketException(lEx);
		}
	}

	@Override
	public boolean isConnected() {
		return mConnected;
	}

	@Override
	public void open(int aVersion, String aURI) throws WebSocketException {
		open(aURI);
	}

	@Override
	public void open(int aVersion, String aURI, String aSubProtocols) throws WebSocketException {
		open(aURI);
	}

	@Override
	public void send(WebSocketPacket aPacket) throws WebSocketException {
		send(aPacket.getByteArray());
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		String lData = new String(aData);
		try {
			HttpURLConnection lReq = getHttpConnection("&action=send&data=" + lData);

			String lResponseText = getResponseText(lReq);
			int lResponseCode = lReq.getResponseCode();

			if (lResponseCode == 200) {
				mConnected = true;
				notifyPacket(new WebSocketBaseClientEvent(this, "message", lResponseText),
						new RawPacket(lResponseText));
			} else {
				throw new WebSocketException(lResponseText);
			}

		} catch (IOException lEx) {
			throw new WebSocketException(lEx);
		}
	}

	@Override
	public void send(byte[] aData, WebSocketFrameType aFrameType) throws WebSocketException {
		send(aData);
	}

	@Override
	public void send(String aData, String aEncoding) throws WebSocketException {
		send(aData.getBytes());
	}

	void close(CloseReason aCloseReason) throws WebSocketException {
		try {
			setStatus(WebSocketStatus.CLOSING);
			mConnected = false;

			HttpURLConnection lReq = getHttpConnection("&action=close");
			String lResponseText = getResponseText(lReq);
			int lResponseCode = lReq.getResponseCode();

			if (lResponseCode == 200) {
				setStatus(WebSocketStatus.CLOSED);
				// notifying close event
				WebSocketBaseClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_CLOSE, aCloseReason.name());
				notifyClosed(lEvent);
				if (CloseReason.BROKEN.equals(aCloseReason)) {
					checkReconnect(lEvent);
				}
			} else {
				throw new WebSocketException(lResponseText);
			}
		} catch (Exception lEx) {
			throw new WebSocketException(lEx);
		}
	}

	@Override
	public void close() throws WebSocketException {
		close(CloseReason.CLIENT);
	}

	@Override
	public void addSubProtocol(WebSocketSubProtocol aSubProt) {
	}

	@Override
	public String getNegotiatedSubProtocol() {
		return null;
	}

	@Override
	public WebSocketEncoding getNegotiatedEncoding() {
		return null;
	}
}
