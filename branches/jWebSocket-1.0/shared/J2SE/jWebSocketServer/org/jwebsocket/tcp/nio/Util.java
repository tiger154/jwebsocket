//	---------------------------------------------------------------------------
//	jWebSocket - Util
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Author: Jan Gnezda
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
package org.jwebsocket.tcp.nio;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.jwebsocket.config.JWebSocketConfig;

/**
 *
 * @author kyberneees
 */
public class Util {

	/**
	 * Creates a SSLContext
	 *
	 * @param aKeyStore
	 * @param aKeyStorePassword
	 * @return
	 * @throws The SSLContext instance
	 */
	public static SSLContext createSSLContext(String aKeyStore, String aKeyStorePassword) throws Exception {
		SSLContext lSSLContext = SSLContext.getInstance("TLS");
		KeyManagerFactory lKMF = KeyManagerFactory.getInstance("SunX509");
		KeyStore lKeyStore = KeyStore.getInstance("JKS");

		String lKeyStorePath = JWebSocketConfig.expandEnvAndJWebSocketVars(aKeyStore);
		if (lKeyStorePath != null) {
			char[] lPassword = aKeyStorePassword.toCharArray();
			URL lURL = JWebSocketConfig.getURLFromPath(lKeyStorePath);
			lKeyStore.load(new FileInputStream(lURL.getPath()), lPassword);
			lKMF.init(lKeyStore, lPassword);
			SecureRandom lSecureRandom = new java.security.SecureRandom();
			lSecureRandom.nextInt();

			lSSLContext.init(lKMF.getKeyManagers(), null, lSecureRandom);
		}

		return lSSLContext;
	}

	/**
	 * Creates a ServerSocketChannel
	 *
	 * @param aPort
	 * @return The ServerSocketChannel instance
	 * @throws IOException
	 */
	public static ServerSocketChannel createServerSocketChannel(int aPort) throws IOException {
		ServerSocketChannel lServer = ServerSocketChannel.open();
		lServer.configureBlocking(false);
		lServer.socket().bind(new InetSocketAddress(aPort));

		return lServer;
	}
}
