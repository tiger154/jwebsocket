//	---------------------------------------------------------------------------
//	jWebSocket - Proxy Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import jlibs.core.nio.ClientChannel;
import jlibs.core.nio.NIOSelector;
import jlibs.core.nio.ServerChannel;
import jlibs.core.nio.handlers.NIOThread;
import jlibs.core.nio.handlers.ServerHandler;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.util.Tools;

/**
 * @author Alexander Schulze
 */
public class ProxyPlugIn extends TokenPlugIn implements ServerHandler {

	private static final Logger mLog = Logging.getLogger();
	// specify token types processed by system plug-in
	private final Endpoint mInEndpoint, mHttpEndpoint, mWebSocketEndpoint;
	private final ServerChannel mServer;
	private int mCount;
	// specify name space for system plug-in
	private static final String NS_PROXY =
			JWebSocketServerConstants.NS_BASE + ".plugins.system";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ProxyPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ProxyPlugIn - Community Edition";

	/**
	 *
	 * @param aConfiguration
	 * @throws IOException
	 */
	public ProxyPlugIn(PluginConfiguration aConfiguration) throws IOException {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating proxy plug-in...");
		}
		this.setNamespace(NS_PROXY);
		String lFromHost = getString("from_host", "localhost");
		int lFromPort = Tools.stringToInt(getString("from_port"), 80);
		String lHttpHost = getString("http_host", "localhost");
		int lHttpPort = Tools.stringToInt(getString("http_port"), 8080);
		String lWebSocketHost = getString("ws_host", "localhost");
		int lWebSocketPort = Tools.stringToInt(getString("ws_port"), 8787);

		mInEndpoint = new Endpoint(lFromHost, lFromPort, false);
		mHttpEndpoint = new Endpoint(lHttpHost, lHttpPort, false);
		mWebSocketEndpoint = new Endpoint(lWebSocketHost, lWebSocketPort, false);

		ClientChannel.defaults().SO_TIMEOUT = 10 * 1000L;
		final NIOSelector lNIOSelector = new NIOSelector(1000);

		mServer = new ServerChannel();
		mServer.bind(mInEndpoint.getAddress());
		mServer.attach(this);

		mServer.register(lNIOSelector);

		lNIOSelector.shutdownOnExit(false);
		new NIOThread(lNIOSelector).start();

		if (mLog.isInfoEnabled()) {
			mLog.info("Proxy successfully setup "
					+ mInEndpoint.getAddress().toString()
					+ " <-> "
					+ mWebSocketEndpoint.getAddress().toString());
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_PROXY;
	}

	/**
	 *
	 * @param aServer
	 * @param aInClient
	 */
	@Override
	public void onAccept(ServerChannel aServer, ClientChannel aInClient) {
		if (mLog.isInfoEnabled()) {
			mLog.info("Incoming connection " + mCount + ": " + aInClient.acceptedFrom().toString() + " accepted");
		}
		mCount++;
		ClientChannel lOutClient = null;
		try {
			if (mInEndpoint.isSSLEnabled()) {
				aInClient.enableSSL();
			}
			// in client in the client for the incoming connection on the proxy
			// out client is the client for the target host connection
			lOutClient = aInClient.selector().newClient();

			ByteBuffer lBuffer1 = ByteBuffer.allocate(9000);
			ByteBuffer lBuffer2 = ByteBuffer.allocate(9000);

			ClientListener lInListener = new ClientListener("In" + mCount, lBuffer1, lBuffer2, false, lOutClient);
			ClientListener lOutListener = new ClientListener("Out" + mCount, lBuffer2, lBuffer1, mWebSocketEndpoint.isSSLEnabled(), aInClient);

			aInClient.attach(lInListener);
			lOutClient.attach(lOutListener);

			NIOThread.connect(lOutClient, mWebSocketEndpoint.getAddress());
			aInClient.addInterest(ClientChannel.OP_READ);

		} catch (IOException ex) {
			mLog.error("onAccept: "
					+ ex.getClass().getSimpleName() + ": "
					+ ex.getMessage());
			try {
				if (lOutClient != null) {
					lOutClient.close();
				}
			} catch (IOException ignore) {
				mLog.error("onAccept: "
						+ ignore.getClass().getSimpleName() + ": "
						+ ignore.getMessage());
			}
		}
	}

	/**
	 *
	 * @param aServer
	 * @param aException
	 */
	@Override
	public void onAcceptFailure(ServerChannel aServer, IOException aException) {
		mLog.error("onAcceptFailure: "
				+ aException.getClass().getSimpleName() + ": "
				+ aException.getMessage());
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		// here the server must be terminated and
		// all client connections must be closed as well.
	}
}
