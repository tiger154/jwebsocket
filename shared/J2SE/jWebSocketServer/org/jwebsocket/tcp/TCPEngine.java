//	---------------------------------------------------------------------------
//	jWebSocket - TCP Engine
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.tcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import javax.net.ssl.SSLSocket;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketHandshake;

/**
 * Implementation of the jWebSocket TCP engine. The TCP engine provide a Java
 * Socket implementation of the WebSocket protocol. It contains the handshake
 * @author aschulze
 * @author jang
 */
public class TCPEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger(TCPEngine.class);
	private ServerSocket mTCPServerSocket = null;
	private SSLServerSocket mSSLServerSocket = null;
	private int mTCPListenerPort = JWebSocketCommonConstants.DEFAULT_PORT;
	private int mSSLListenerPort = JWebSocketCommonConstants.DEFAULT_SSLPORT;
	private int mSessionTimeout = JWebSocketCommonConstants.DEFAULT_TIMEOUT;
	private String mKeyStore = JWebSocketServerConstants.JWEBSOCKET_KEYSTORE;
	private String mKeyStorePassword = JWebSocketServerConstants.JWEBSOCKET_KS_DEF_PWD;
	private boolean mIsRunning = false;
	private boolean mEventsFired = false;
	private Thread mTCPEngineThread = null;
	private Thread mSSLEngineThread = null;

	public TCPEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
		mTCPListenerPort = aConfiguration.getPort();
		mSSLListenerPort = aConfiguration.getSSLPort();
		mSessionTimeout = aConfiguration.getTimeout();
		mKeyStore = aConfiguration.getKeyStore();
		mKeyStorePassword = aConfiguration.getKeyStorePassword();
	}

	@Override
	public void startEngine()
			throws WebSocketException {
		setSessionTimeout(mSessionTimeout);

		// create unencrypted server socket for ws:// protocol
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting TCP engine '"
					+ getId()
					+ "' at port " + mTCPListenerPort
					+ " with default timeout "
					+ (mSessionTimeout > 0 ? mSessionTimeout + "ms" : "infinite")
					+ "...");
		}
		try {
			mTCPServerSocket = new ServerSocket(mTCPListenerPort);

			EngineListener lListener = new EngineListener(this, mTCPServerSocket);
			mTCPEngineThread = new Thread(lListener);
			mTCPEngineThread.start();

		} catch (IOException lEx) {
			throw new WebSocketException(lEx.getMessage());
		}

		// TODO: results in firing started event twice! make more clean!
		// super.startEngine();
		if (mLog.isInfoEnabled()) {
			mLog.info("TCP engine '"
					+ getId() + "' started' at port "
					+ mTCPListenerPort + " with default timeout "
					+ (mSessionTimeout > 0 ? mSessionTimeout + "ms" : "infinite")
					+ ".");
		}

		// create encrypted (SSL) server socket for wss:// protocol
		if (mSSLListenerPort > 0) {
			if (mKeyStore != null && !mKeyStore.isEmpty()
					&& mKeyStorePassword != null && !mKeyStorePassword.isEmpty()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Starting SSL engine '"
							+ getId()
							+ "' at port " + mSSLListenerPort
							+ " with default timeout "
							+ (mSessionTimeout > 0 ? mSessionTimeout + "ms" : "infinite")
							+ "...");
				}
				try {
					SSLContext lSSLContext = SSLContext.getInstance("SSL");
					KeyManagerFactory lKMF = KeyManagerFactory.getInstance("SunX509");
					KeyStore lKeyStore = KeyStore.getInstance("JKS");

					String lKeyStorePath = JWebSocketConfig.getConfigFolder(mKeyStore);
					if (lKeyStorePath != null) {
						char[] lPassword = mKeyStorePassword.toCharArray();
						lKeyStore.load(new FileInputStream(lKeyStorePath), lPassword);
						lKMF.init(lKeyStore, lPassword);

						lSSLContext.init(lKMF.getKeyManagers(), null, null);
						SSLServerSocketFactory lSSLFactory = lSSLContext.getServerSocketFactory();
						mSSLServerSocket = (SSLServerSocket) lSSLFactory.createServerSocket(
								mSSLListenerPort);
						EngineListener lSSLListener = new EngineListener(this, mSSLServerSocket);
						mSSLEngineThread = new Thread(lSSLListener);
						mSSLEngineThread.start();

						if (mLog.isInfoEnabled()) {
							mLog.info("SSL engine '"
									+ getId() + "' started' at port "
									+ mSSLListenerPort + " with default timeout "
									+ (mSessionTimeout > 0
									? mSessionTimeout + "ms" : "infinite")
									+ ".");
						}
					} else {
						mLog.error("SSL engine could not be instantiated: "
								+ "KeyStore '" + mKeyStore + "' not found.");
					}
				} catch (Exception lEx) {
					mLog.error("SSL engine could not be instantiated: "
							+ lEx.getMessage());
				}
			} else {
				mLog.error("SSL engine could not be instantiated due to missing configuration,"
						+ " please set sslport, keystore and password options.");
			}
		} else {
			mLog.info("No SSL engine configured,"
					+ " set sslport, keystore and password options if desired.");
		}
	}

	@Override
	public void stopEngine(CloseReason aCloseReason)
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping TCP engine '" + getId()
					+ "' at port " + mTCPListenerPort + "...");
		}
		// resetting "isRunning" causes engine listener to terminate
		mIsRunning = false;
		long lStarted = new Date().getTime();

		// close unencrypted TCP server socket
		try {
			// when done, close server socket
			// closing the server socket should lead to an IOExeption
			// at accept in the listener thread which terminates the listener
			if (mTCPServerSocket != null && !mTCPServerSocket.isClosed()) {
				mTCPServerSocket.close();
				if (mLog.isInfoEnabled()) {
					mLog.info("TCP engine '" + getId()
							+ "' stopped at port " + mTCPListenerPort
							+ " (closed=" + mTCPServerSocket.isClosed() + ").");
				}
				mTCPServerSocket = null;
			} else {
				mLog.warn("Stopping TCP engine '" + getId()
						+ "': no server socket or server socket closed.");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on stopping TCP engine '" + getId()
					+ "': " + lEx.getMessage());
		}

		// close encrypted SSL server socket
		try {
			// when done, close server socket
			// closing the server socket should lead to an IOExeption
			// at accept in the listener thread which terminates the listener
			if (mSSLServerSocket != null && !mSSLServerSocket.isClosed()) {
				mSSLServerSocket.close();
				if (mLog.isInfoEnabled()) {
					mLog.info("SSL engine '" + getId()
							+ "' stopped at port " + mSSLListenerPort
							+ " (closed=" + mSSLServerSocket.isClosed() + ").");
				}
				mSSLServerSocket = null;
			} else {
				mLog.warn("Stopping SSL engine '" + getId()
						+ "': no server socket or server socket closed.");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on stopping SSL engine '" + getId()
					+ "': " + lEx.getMessage());
		}

		// stop TCP listener thread
		if (mTCPEngineThread != null) {
			try {
				// TODO: Make this timeout configurable one day
				mTCPEngineThread.join(10000);
			} catch (Exception lEx) {
				mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
			}
			if (mLog.isDebugEnabled()) {
				long lDuration = new Date().getTime() - lStarted;
				if (mTCPEngineThread.isAlive()) {
					mLog.warn("TCP engine '" + getId()
							+ "' did not stop after " + lDuration + "ms.");
				} else {
					if (mLog.isDebugEnabled()) {
						mLog.debug("TCP engine '" + getId()
								+ "' stopped after " + lDuration + "ms.");
					}
				}
			}
		}

		// stop SSL listener thread
		if (mSSLEngineThread != null) {
			try {
				// TODO: Make this timeout configurable one day
				mSSLEngineThread.join(10000);
			} catch (Exception lEx) {
				mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
			}
			if (mLog.isDebugEnabled()) {
				long lDuration = new Date().getTime() - lStarted;
				if (mSSLEngineThread.isAlive()) {
					mLog.warn("SSL engine '" + getId()
							+ "' did not stop after " + lDuration + "ms.");
				} else {
					if (mLog.isDebugEnabled()) {
						mLog.debug("SSL engine '" + getId()
								+ "' stopped after " + lDuration + "ms.");
					}
				}
			}
		}

		// inherited method stops all connectors
		lStarted = new Date().getTime();
		int lNumConns = getConnectors().size();
		super.stopEngine(aCloseReason);

		// now wait until all connectors have been closed properly
		// or timeout exceeds...
		try {
			while (getConnectors().size() > 0
					&& new Date().getTime() - lStarted < 10000) {
				Thread.sleep(250);
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			long lDuration = new Date().getTime() - lStarted;
			int lRemConns = getConnectors().size();
			if (lRemConns > 0) {
				mLog.warn(lRemConns + " of " + lNumConns
						+ " TCP connectors '" + getId()
						+ "' did not stop after " + lDuration + "ms.");
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug(lNumConns
							+ " TCP connectors '" + getId()
							+ "' stopped after " + lDuration + "ms.");
				}
			}
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected new connector at port " + aConnector.getRemotePort() + ".");
		}
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected stopped connector at port " + aConnector.getRemotePort() + ".");
		}
		super.connectorStopped(aConnector, aCloseReason);
	}

	private RequestHeader processHandshake(Socket aClientSocket)
			throws UnsupportedEncodingException, IOException {

		InputStream lIn = aClientSocket.getInputStream();
		OutputStream lOut = aClientSocket.getOutputStream();

		// TODO: Replace this structure by more dynamic ByteArrayOutputStream?
		byte[] lBuff = new byte[8192];
		int lRead = lIn.read(lBuff);
		if (lRead <= 0) {
			mLog.warn("Connection "
					+ aClientSocket.getInetAddress() + ":"
					+ aClientSocket.getPort()
					+ " did not detect initial handshake (" + lRead + ").");
			return null;
		}
		byte[] lReq = new byte[lRead];
		System.arraycopy(lBuff, 0, lReq, 0, lRead);

		/* please keep comment for debugging purposes! */
		if (mLog.isDebugEnabled()) {
			mLog.debug("Parsing handshake request: " + new String(lReq).replace("\r\n", "\\n"));
			// mLog.debug("Parsing initial WebSocket handshake...");
		}
		Map lRespMap = WebSocketHandshake.parseC2SRequest(
				lReq, aClientSocket instanceof SSLSocket);
		RequestHeader lHeader = EngineUtils.validateC2SRequest(
				getConfiguration().getDomains(), lRespMap, mLog);
		if (lHeader == null) {
			return null;
		}

		// generate the websocket handshake
		// if policy-file-request is found answer it
		byte[] lBA = WebSocketHandshake.generateS2CResponse(lRespMap);
		if (lBA == null) {
			if (mLog.isDebugEnabled()) {
				mLog.warn("TCPEngine detected illegal handshake.");
			}
			return null;
		}

		/* please keep comment for debugging purposes!*/
		if (mLog.isDebugEnabled()) {
			mLog.debug("Flushing handshake response: " + new String(lBA).replace("\r\n", "\\n"));
			// mLog.debug("Flushing initial WebSocket handshake...");
		}

		lOut.write(lBA);
		lOut.flush();

		// maybe the request is a flash policy-file-request
		String lFlashBridgeReq = (String) lRespMap.get("policy-file-request");
		if (lFlashBridgeReq != null) {
			mLog.warn("TCPEngine returned policy file request ('"
					+ lFlashBridgeReq
					+ "'), check for FlashBridge plug-in.");
		}


		// if we detected a flash policy-file-request return "null"
		// (no websocket header detected)
		if (lFlashBridgeReq != null) {
			mLog.warn("TCP Engine returned policy file response ('"
					+ new String(lBA, "US-ASCII")
					+ "'), check for FlashBridge plug-in.");
			return null;
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Handshake flushed.");
		}

		return lHeader;
	}

	@Override
	/*
	 * Returns {@code true} if the TCP engine is running or {@code false}
	 * otherwise. The alive status represents the state of the TCP engine
	 * listener thread.
	 */
	public boolean isAlive() {
		return (mTCPEngineThread != null && mTCPEngineThread.isAlive());
	}

	private class EngineListener implements Runnable {

		private WebSocketEngine mEngine = null;
		private ServerSocket mServer = null;

		/**
		 * Creates the server socket listener for new
		 * incoming socket connections.
		 * @param aEngine
		 */
		public EngineListener(WebSocketEngine aEngine, ServerSocket aServerSocket) {
			mEngine = aEngine;
			mServer = aServerSocket;
		}

		@Override
		public void run() {
			Thread.currentThread().setName(
					"jWebSocket TCP-Engine (" + mServer.getLocalPort() + ", "
					+ (mServer instanceof SSLServerSocket
					? "SSL secured)"
					: "non secured)"));

			// notify server that engine has started
			if (!mEventsFired) {
				mEventsFired = true;
				engineStarted();
			}

			mIsRunning = true;
			while (mIsRunning) {
				try {
					// accept is blocking so here is no need
					// to put any sleeps into this loop
					// if (log.isDebugEnabled()) {
					//	log.debug("Waiting for client...");
					// }
					Socket lClientSocket = mServer.accept();
					if (mLog.isDebugEnabled()) {
						mLog.debug("Client trying to connect on port #"
								+ lClientSocket.getPort() + "...");
					}

					// boolean lTCPNoDelay = lClientSocket.getTcpNoDelay();
					// ensure that all packets are sent immediately w/o delay
					// to achieve better latency, no waiting and packaging.
					lClientSocket.setTcpNoDelay(true);

					try {
						// process handshake to parse header data
						RequestHeader lHeader = processHandshake(lClientSocket);
						if (lHeader != null) {
							// set socket timeout to given amount of milliseconds
							// use tcp engine's timeout as default and
							// check system's min and max timeout ranges
							int lSessionTimeout = lHeader.getTimeout(getSessionTimeout());
							/* min and max range removed since 0.9.0.0602, see config documentation
							if (lSessionTimeout > JWebSocketServerConstants.MAX_TIMEOUT) {
							lSessionTimeout = JWebSocketServerConstants.MAX_TIMEOUT;
							} else if (lSessionTimeout < JWebSocketServerConstants.MIN_TIMEOUT) {
							lSessionTimeout = JWebSocketServerConstants.MIN_TIMEOUT;
							}
							 */
							if (lSessionTimeout > 0) {
								lClientSocket.setSoTimeout(lSessionTimeout);
							}
							// create connector and pass header
							// log.debug("Instantiating connector...");
							WebSocketConnector lConnector = new TCPConnector(mEngine, lClientSocket);
							lConnector.setVersion(lHeader.getVersion());

							String lLogInfo = lConnector.isSSL() ? "SSL" : "TCP";
							if (mLog.isDebugEnabled()) {
								mLog.debug(lLogInfo + " client accepted on port "
										+ lClientSocket.getPort()
										+ " with timeout "
										+ (lSessionTimeout > 0 ? lSessionTimeout + "ms" : "infinite")
										// + " (TCPNoDelay was: " + lTCPNoDelay + ")"
										+ "...");
							}

							// log.debug("Setting header to engine...");
							lConnector.setHeader(lHeader);
							// log.debug("Adding connector to engine...");
							getConnectors().put(lConnector.getId(), lConnector);
							if (mLog.isDebugEnabled()) {
								mLog.debug("Starting " + lLogInfo + " connector...");
							}
							lConnector.startConnector();
						} else {
							// if header could not be parsed properly
							// immediately disconnect the client.
							lClientSocket.close();
						}
					} catch (Exception lEx) {
						mLog.error(
								(mServer instanceof SSLServerSocket
								? "SSL" : "TCP") + " engine: "
								+ lEx.getClass().getSimpleName()
								+ ": " + lEx.getMessage());
					}
				} catch (Exception lEx) {
					if (mIsRunning) {
						mIsRunning = false;
						mLog.error(
								(mServer instanceof SSLServerSocket ? "SSL" : "TCP")
								+ " engine: "
								+ lEx.getClass().getSimpleName()
								+ ": " + lEx.getMessage());
					} else {
						if (mLog.isInfoEnabled()) {
							mLog.info(
									(mServer instanceof SSLServerSocket ? "SSL" : "TCP")
									+ " engine: "
									+ "Server listener thread stopped.");
						}
					}
				}
			}

			// notify server that engine has stopped
			// this closes all connections
			if (mEventsFired) {
				mEventsFired = false;
				engineStopped();
			}
		}
	}
}
