//	---------------------------------------------------------------------------
//	jWebSocket - TCP Engine (Community Edition, CE)
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
package org.jwebsocket.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.nio.NioTcpEngine;
import org.jwebsocket.tcp.nio.Util;

/**
 * Implementation of the jWebSocket TCP engine. The TCP engine provide a Java
 * Socket implementation of the WebSocket protocol. It contains the handshake
 *
 * @author Alexander Schulze
 * @author jang
 * @author Rolando Santamaria Maso
 */
public class TCPEngine extends BaseEngine {

	private static final Logger mLog = Logging.getLogger();
	private ServerSocket mTCPServerSocket = null;
	private SSLServerSocket mSSLServerSocket = null;
	private int mTCPListenerPort = JWebSocketCommonConstants.DEFAULT_PORT;
	private int mSSLListenerPort = JWebSocketCommonConstants.DEFAULT_SSLPORT;
	private int mSessionTimeout = JWebSocketCommonConstants.DEFAULT_TIMEOUT;
	private String mHostname;
	private String mKeyStore = JWebSocketServerConstants.JWEBSOCKET_KEYSTORE;
	private String mKeyStorePassword = JWebSocketServerConstants.JWEBSOCKET_KS_DEF_PWD;
	private boolean mIsRunning = false;
	private boolean mEventsFired = false;
	private Thread mTCPEngineThread = null;
	private Thread mSSLEngineThread = null;
	// variables for the OutputStreamNIOWriter mechanism
	private static final int DEFAULT_NUM_WORKERS = NioTcpEngine.DEFAULT_NUM_WORKERS;
	private static final int DEFAULT_WRITER_TIMEOUT = 3000; // increase the time for slow networks
	private static final String NUM_WORKERS_CONFIG_KEY = NioTcpEngine.NUM_WORKERS_CONFIG_KEY;
	/**
	 *
	 */
	public static String WRITER_TIMEOUT_CONFIG_KEY = "writer_timeout";
	/**
	 *
	 */
	public static String TCP_NODELAY_CONFIG_KEY = "tcpNoDelay";
	/**
	 *
	 */
	public static Boolean DEFAULT_TCP_NODELAY = true;
	private int mNumWorkers = DEFAULT_NUM_WORKERS;
	private int mWriterTimeout = DEFAULT_WRITER_TIMEOUT;
	private boolean mTcpNoDelay = DEFAULT_TCP_NODELAY;

	/**
	 *
	 * @return
	 */
	public int getWriterTimeout() {
		return mWriterTimeout;
	}

	/**
	 *
	 * @return
	 */
	public int getWorkers() {
		return mNumWorkers;
	}

	/**
	 *
	 * @param aConfiguration
	 */
	public TCPEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
		mTCPListenerPort = aConfiguration.getPort();
		mSSLListenerPort = aConfiguration.getSSLPort();
		mSessionTimeout = aConfiguration.getTimeout();
		mKeyStore = aConfiguration.getKeyStore();
		mKeyStorePassword = aConfiguration.getKeyStorePassword();
		mHostname = aConfiguration.getHostname();

		// settings for the OutputStreamNIOWriter mechanism
		if (getConfiguration().getSettings().containsKey(NUM_WORKERS_CONFIG_KEY)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Setting '" + NUM_WORKERS_CONFIG_KEY + "' configuration "
						+ "from engine configuration...");
			}
			try {
				mNumWorkers = Integer.parseInt(getConfiguration().
						getSettings().
						get(NUM_WORKERS_CONFIG_KEY).
						toString());
			} catch (NumberFormatException lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx,
						"setting '" + NUM_WORKERS_CONFIG_KEY + "' configuration"));
			}

		}

		if (getConfiguration().getSettings().containsKey(WRITER_TIMEOUT_CONFIG_KEY)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Setting '" + WRITER_TIMEOUT_CONFIG_KEY + "' configuration "
						+ "from engine configuration...");
			}
			try {
				mWriterTimeout = Integer.parseInt(getConfiguration().
						getSettings().
						get(WRITER_TIMEOUT_CONFIG_KEY).
						toString());
			} catch (NumberFormatException lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx,
						"setting '" + WRITER_TIMEOUT_CONFIG_KEY + "' configuration"));
			}
		}

		if (getConfiguration().getSettings().containsKey(TCP_NODELAY_CONFIG_KEY)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Setting '" + TCP_NODELAY_CONFIG_KEY + "' configuration "
						+ "from engine configuration...");
			}
			try {
				mTcpNoDelay = Boolean.parseBoolean(getConfiguration().
						getSettings().
						get(TCP_NODELAY_CONFIG_KEY).
						toString());
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx,
						"setting '" + TCP_NODELAY_CONFIG_KEY + "' configuration"));
			}
		}
	}

	@Override
	public void startEngine() throws WebSocketException {
		// start output NIO writer mechanism
		TimeoutOutputStreamNIOWriter.start(mNumWorkers, mWriterTimeout);

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
			if (null != mHostname) {
				mTCPServerSocket = new ServerSocket(mTCPListenerPort, 0, InetAddress.getByName(mHostname));
			} else {
				mTCPServerSocket = new ServerSocket(mTCPListenerPort, 0);
			}

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
					+ "...");
		}

		// tutorial see: http://javaboutique.internet.com/tutorials/jkey/index.html
		// create encrypted (SSL) server socket for wss:// protocol
		if (mSSLListenerPort > 0) {
			// create unencrypted server socket for ws:// protocol
			if (mLog.isDebugEnabled()) {
				mLog.debug("Trying to initiate SSL on port " + mSSLListenerPort + "...");
			}
			if (mKeyStore != null && !mKeyStore.isEmpty()
					&& mKeyStorePassword != null && !mKeyStorePassword.isEmpty()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Using keystore " + mKeyStore + "...");
					mLog.debug("Starting SSL engine '"
							+ getId()
							+ "' at port " + mSSLListenerPort + ","
							+ " with default timeout "
							+ (mSessionTimeout > 0 ? mSessionTimeout + "ms" : "infinite")
							+ "...");
				}
				try {
					String lKeyStorePath = JWebSocketConfig.expandEnvVarsAndProps(mKeyStore);
					SSLContext lSSLContext = Util.createSSLContext(lKeyStorePath, mKeyStorePassword);

					SSLServerSocketFactory lSSLFactory = lSSLContext.getServerSocketFactory();
					mSSLServerSocket = (SSLServerSocket) lSSLFactory.createServerSocket(
							mSSLListenerPort);
					// enable all protocols
					mSSLServerSocket.setEnabledProtocols(mSSLServerSocket.getEnabledProtocols());
					// enable all cipher suites
					mSSLServerSocket.setEnabledCipherSuites(mSSLServerSocket.getSupportedCipherSuites());
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

				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating SSL engine"));
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
	@SuppressWarnings("SleepWhileInLoop")
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
		} catch (IOException lEx) {
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
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on stopping SSL engine '" + getId()
					+ "': " + lEx.getMessage());
		}

		// stop TCP listener thread
		if (mTCPEngineThread != null) {
			try {
				// TODO: Make this timeout configurable one day
				mTCPEngineThread.join(10000);
			} catch (InterruptedException lEx) {
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
			} catch (InterruptedException lEx) {
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
		} catch (InterruptedException lEx) {
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

		// stop timeout surveillance timer
		TimeoutOutputStreamNIOWriter.stop();
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
		 * Creates the server socket listener for new incoming socket
		 * connections.
		 *
		 * @param aEngine
		 */
		public EngineListener(WebSocketEngine aEngine, ServerSocket aServerSocket) {
			mEngine = aEngine;
			mServer = aServerSocket;
		}

		@Override
		@SuppressWarnings({"SleepWhileInLoop", "UseSpecificCatch", "null"})
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
					Socket lClientSocket = null;
					boolean lReject = false;
					boolean lRedirect = false;
					String lOnMaxConnectionStrategy = mEngine.getConfiguration().getOnMaxConnectionStrategy();

					// Accept new connections only if the maximun number of connections
					// has not been reached
					if ("wait".equals(lOnMaxConnectionStrategy)) {
						if (mEngine.getConnectors().size() >= mEngine.getMaxConnections()) {
							Thread.sleep(1000);
							continue;
						} else {
							lClientSocket = mServer.accept();
						}
					} else if ("close".equals(lOnMaxConnectionStrategy)) {
						lClientSocket = mServer.accept();
						if (mEngine.getConnectors().size() >= mEngine.getMaxConnections()) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Closing incoming socket client on  port '" + lClientSocket.getPort() + "' "
										+ "because the maximum number of supported connections "
										+ "has been reached...");
							}

							lClientSocket.close();
							continue;
						}
					} else if ("reject".equals(lOnMaxConnectionStrategy)) {
						lClientSocket = mServer.accept();
						if (mEngine.getConnectors().size() >= mEngine.getMaxConnections()) {
							lReject = true;
						}
					} else if ("redirect".equals(lOnMaxConnectionStrategy)) {
						lClientSocket = mServer.accept();
						if (mEngine.getConnectors().size() >= mEngine.getMaxConnections()) {
							lRedirect = true;
						}
					}

					if (mLog.isDebugEnabled()) {
						mLog.debug("Client from '"
								+ (lClientSocket != null
								? lClientSocket.getInetAddress()
								: "[no socket]")
								+ "' connecting to port "
								+ (lClientSocket != null
								? lClientSocket.getPort()
								: "[no socket]")
								+ "...");
					}

					try {
						// closing if server is not ready
						if (JWebSocketInstance.STARTED != JWebSocketInstance.getStatus()) {
							lClientSocket.close();
							continue;
						}

						WebSocketConnector lConnector = new TCPConnector(mEngine, lClientSocket);
						// setting the TcpNoDelay property
						if (lClientSocket != null) {
							lClientSocket.setTcpNoDelay(mTcpNoDelay);
							// restricting connection handshake timeout
							// lClientSocket.setSoTimeout(10 * 1000);
						}

						// Check for maximum connections reached strategies
						if (lReject) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Rejecting incoming connector '"
										+ lConnector.getId() + "' "
										+ "because the maximum number of supported connections "
										+ "has been reached.");
							}
							lConnector.stopConnector(CloseReason.SERVER_REJECT_CONNECTION);
						} else if (lRedirect) {
							// TODO: Pending for implementation to discover the redirection
							// server URL

							if (mLog.isDebugEnabled()) {
								mLog.debug("Redirecting incoming connector '" + lConnector.getId() + "' "
										+ "because the maximum number of supported connections "
										+ "has been reached.");
							}
							lConnector.stopConnector(CloseReason.SERVER_REDIRECT_CONNECTION);
						} else {
							// initiating connector
							((TCPConnector) lConnector).init();
						}
					} catch (SocketException lEx) {
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
