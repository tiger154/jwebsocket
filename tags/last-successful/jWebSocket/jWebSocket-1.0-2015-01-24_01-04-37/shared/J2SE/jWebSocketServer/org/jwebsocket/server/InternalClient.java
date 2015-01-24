//	---------------------------------------------------------------------------
//	jWebSocket - InternalClient (Community Edition, CE)
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
package org.jwebsocket.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IInternalConnectorListener;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.connectors.InternalConnector;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 * This class represents an internal jWebSocket server client
 *
 * @author Rolando Santamaria Maso
 */
public class InternalClient {

	private Logger mLog = Logging.getLogger();
	private final InternalConnector mConnector;
	private static Map<Integer, WebSocketResponseTokenListener> mResponseListenersQueue
			= new FastMap<Integer, WebSocketResponseTokenListener>().shared();
	static int CURRENT_TOKEN_UID = 0;
	private final ExecutorService mThreadPool;
	private final Timer mTimer;

	static int getUTID() {
		if (Integer.MAX_VALUE == CURRENT_TOKEN_UID) {
			CURRENT_TOKEN_UID = 0;
		}

		return ++CURRENT_TOKEN_UID;
	}

	/**
	 * Constructor
	 *
	 * @param aConnector
	 */
	public InternalClient(InternalConnector aConnector) {
		mConnector = aConnector;
		mThreadPool = Tools.getThreadPool();
		mTimer = Tools.getTimer();

		mConnector.addListener(new IInternalConnectorListener() {
			@Override
			public void processPacket(WebSocketPacket aPacket) {
			}

			@Override
			public void processToken(Token aToken) {
				Integer lUTID = aToken.getInteger("utid");
				if (null != lUTID) {
					try {
						WebSocketResponseTokenListener lListener = mResponseListenersQueue.remove(lUTID);
						if (null != lListener) {
							lListener.OnResponse(aToken);
							if (0 == aToken.getCode()) {
								lListener.OnSuccess(aToken);
							} else {
								lListener.OnFailure(aToken);
							}
						}
					} catch (Exception lEx) {
						mLog.error("processing response listener callback", lEx);
					}
				}
			}

			@Override
			public void processWelcome(Token aToken) {
			}

			@Override
			public void processClosed(CloseReason aReason) {
			}

			@Override
			public void processOpened() {
			}
		});
	}

	/**
	 * Return TRUE if the client is connected, FALSE otherwise.
	 *
	 * @return
	 */
	public boolean isConnected() {
		return WebSocketConnectorStatus.UP.equals(mConnector.getStatus());
	}

	/**
	 * Get the client connector identifier.
	 *
	 * @return
	 */
	public String getId() {
		return mConnector.getId();
	}

	/**
	 * Get the client username if logon.
	 *
	 * @return
	 */
	public String getUsername() {
		return mConnector.getUsername();
	}

	/**
	 * Get the client connector instance.
	 *
	 * @return
	 */
	public InternalConnector getConnector() {
		return mConnector;
	}

	/**
	 * Constructor.
	 */
	public InternalClient() {
		this(new InternalConnector(JWebSocketFactory.getEngine()));
	}

	/**
	 * Open the client connection.
	 */
	public void open() {
		mConnector.startConnector();
	}

	/**
	 * Send a packet to the server.
	 *
	 * @param aPacket
	 */
	public void sendPacket(final WebSocketPacket aPacket) {
		mThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				mConnector.getEngine().processPacket(mConnector, aPacket);
			}
		});
	}

	/**
	 * Send a token to the server.
	 *
	 * @param aToken
	 */
	public void sendToken(Token aToken) {
		sendToken(aToken, null);
	}

	/**
	 * Send a token to the server allowing to pass response listener callbacks.
	 *
	 * @param aToken
	 * @param aResponseListener
	 */
	public void sendToken(final Token aToken, WebSocketResponseTokenListener aResponseListener) {
		setUTID(aToken);

		if (null != aResponseListener) {
			processResponseListener(aToken, aResponseListener);
		}

		mThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				Iterator<WebSocketServer> lServers = JWebSocketFactory.getServers().iterator();
				while (lServers.hasNext()) {
					WebSocketServer lServer = lServers.next();
					if (lServer instanceof TokenServer) {
						((TokenServer) lServer).processToken(mConnector, aToken);
					}
				}
			}
		});
	}

	private void setUTID(Token aToken) {
		// adding the utid attribute
		aToken.setInteger("utid", getUTID());
	}

	/**
	 * Add a listener to the server client connection instance.
	 *
	 * @param aListener
	 * @return
	 */
	public IInternalConnectorListener addListener(IInternalConnectorListener aListener) {
		mConnector.addListener(aListener);

		return aListener;
	}

	/**
	 * Remove a server client listener.
	 *
	 * @param aListener
	 */
	public void removeListener(IInternalConnectorListener aListener) {
		mConnector.removeListener(aListener);
	}

	/**
	 * Close the client connection.
	 */
	public void close() {
		mConnector.stopConnector(CloseReason.CLIENT);
	}

	private void processResponseListener(final Token aToken, WebSocketResponseTokenListener aListener) {
		mResponseListenersQueue.put(aToken.getInteger("utid"), aListener);

		final Integer lUTID = aToken.getInteger("utid");
		mTimer.schedule(new JWSTimerTask() {
			@Override
			public void runTask() {
				mThreadPool.submit(new Runnable() {
					@Override
					public void run() {
						WebSocketResponseTokenListener lListener = mResponseListenersQueue.remove(lUTID);
						if (null != lListener) {
							try {
								lListener.OnTimeout(aToken);
							} catch (Exception lEx) {
								mLog.error("processing response listener callback", lEx);
							}
						}
					}
				});
			}
		}, aListener.getTimeout());
	}

	/**
	 * Logon in the jWebSocket server.
	 *
	 * @param aUsername The username value.
	 * @param aPassword The password value.
	 * @param aListener The response listener.
	 */
	public void logon(String aUsername, String aPassword, WebSocketResponseTokenListener aListener) {
		Token lToken = TokenFactory.createToken(SystemPlugIn.NS_SYSTEM, "logon");
		lToken.setString("username", aUsername);
		lToken.setString("password", aPassword);

		sendToken(lToken, aListener);
	}

	/**
	 * Logoff in the jWebSocket server.
	 *
	 * @param aListener
	 */
	public void logoff(WebSocketResponseTokenListener aListener) {
		Token lToken = TokenFactory.createToken(SystemPlugIn.NS_SYSTEM, "logoff");

		sendToken(lToken, aListener);
	}
}
