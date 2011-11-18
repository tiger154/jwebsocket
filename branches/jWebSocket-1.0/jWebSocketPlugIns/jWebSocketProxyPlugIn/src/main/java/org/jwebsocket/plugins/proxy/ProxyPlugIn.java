//	---------------------------------------------------------------------------
//	jWebSocket - Proxy Plug-In
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.plugins.proxy;

import jlibs.core.nio.ClientChannel;
import jlibs.core.nio.NIOSelector;
import jlibs.core.nio.ServerChannel;
import jlibs.core.nio.handlers.NIOThread;
import jlibs.core.nio.handlers.ServerHandler;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.util.Tools;

/**
 * @author aschulze
 */
public class ProxyPlugIn extends TokenPlugIn implements ServerHandler {

	private static Logger mLog = Logging.getLogger(ProxyPlugIn.class);
	// specify name space for system plug-in
	private static final String NS_PROXY = JWebSocketServerConstants.NS_BASE + ".plugins.system";
	// specify token types processed by system plug-in
	private Endpoint mInEndpoint, mHttpEndpoint, mWebSocketEndpoint;
	private ServerChannel mServer;

	public ProxyPlugIn(PluginConfiguration aConfiguration) throws IOException {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating proxy plug-in...");
		}

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
	
	private int mCount;

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

