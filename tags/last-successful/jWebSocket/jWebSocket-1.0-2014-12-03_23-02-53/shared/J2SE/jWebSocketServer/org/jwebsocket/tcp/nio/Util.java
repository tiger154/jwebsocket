//	---------------------------------------------------------------------------
//	jWebSocket - Util (Community Edition, CE)
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
package org.jwebsocket.tcp.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.jwebsocket.config.JWebSocketConfig;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class Util {

	/**
	 * Creates a SSLContext
	 *
	 * @param aKeyStore
	 * @param aKeyStorePassword
	 * @return
	 * @throws Exception
	 */
	public static SSLContext createSSLContext(String aKeyStore, String aKeyStorePassword) throws Exception {
		SSLContext lSSLContext = SSLContext.getInstance("TLS");

		KeyManagerFactory lKMF = KeyManagerFactory.getInstance("SunX509");

		KeyStore lKeyStore = KeyStore.getInstance("JKS");

		TrustManagerFactory lTM = TrustManagerFactory.getInstance("SunX509");
		lTM.init(lKeyStore);

		String lKeyStorePath = JWebSocketConfig.expandEnvVarsAndProps(aKeyStore);
		if (new File(lKeyStorePath).exists()) {
			char[] lPassword = aKeyStorePassword.toCharArray();
			URL lURL = JWebSocketConfig.getURLFromPath(lKeyStorePath);
			lKeyStore.load(new FileInputStream(lURL.getPath()), lPassword);
			lKMF.init(lKeyStore, lPassword);
			SecureRandom lSecureRandom = new java.security.SecureRandom();
			lSecureRandom.nextInt();

			lSSLContext.init(lKMF.getKeyManagers(), lTM.getTrustManagers(), lSecureRandom);
		} else {
			throw new Exception("KeyStore file not found!");
		}

		return lSSLContext;
	}

	/**
	 * Creates a ServerSocketChannel
	 *
	 * @param aPort
	 * @param aHostname
	 * @return The ServerSocketChannel instance
	 * @throws IOException
	 */
	public static ServerSocketChannel createServerSocketChannel(int aPort, String aHostname) throws IOException {
		ServerSocketChannel lServer = ServerSocketChannel.open();
		lServer.configureBlocking(false);

		if (null == aHostname) {
			lServer.socket().bind(new InetSocketAddress(aPort));
		} else {
			lServer.socket().bind(new InetSocketAddress(InetAddress.getByName(aHostname), aPort));
		}
		return lServer;
	}
}
