//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JQuery Demo Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
/**
 * Sends the server time each second to a set of registered clients. The logic
 * of this plugIn is implemented in the StreamingPlugIn in the following class:
 * <tt>org.jwebsocket.plugins.streaming.StreamingPlugIn</tt>, this plugIn is not
 * used anymore by jWebSocket, will be removed very soon.
 */
package org.jwebsocket.plugins.jquery;

import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * @author Victor Antonio Barzana Crespo
 */
public class JQueryPlugIn extends TokenPlugIn {

	private static Collection<WebSocketConnector> mClients = new FastList<WebSocketConnector>().shared();
	private static Thread mServerTimeThread;
	private static boolean mIsServerTimeRunning = false;

	public JQueryPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		// jquery.clock.demo
		this.setNamespace(aConfiguration.getNamespace());
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		try {
			mIsServerTimeRunning = false;
			mServerTimeThread.join(100);
			mServerTimeThread.stop();
		} catch (InterruptedException ex) {
			Logger.getLogger(JQueryPlugIn.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (!mClients.isEmpty()) {
			mClients.remove(aConnector);
			try {
				mIsServerTimeRunning = false;
				mServerTimeThread.join();
				mServerTimeThread.stop();
			} catch (InterruptedException ex) {
				Logger.getLogger(JQueryPlugIn.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if ("register".equals(aToken.getType())) {
				if (mClients.isEmpty()) {
					mIsServerTimeRunning = true;
					mServerTimeThread = new Thread(new getServerTime(), "jWebSocket Monitoring Plug-in jQuery time");
					mServerTimeThread.start();
				}
				mClients.add(aConnector);
			} else if ("unregister".equals(aToken.getType())) {
				if (mClients.contains(aConnector)) {
					mClients.remove(aConnector);
				}
				if (mClients.isEmpty()) {
					try {
						mIsServerTimeRunning = false;
						mServerTimeThread.join();
						mServerTimeThread.stop();
					} catch (InterruptedException ex) {
						Logger.getLogger(JQueryPlugIn.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}
	}

	class getServerTime implements Runnable {

		@Override
		public void run() {
			Token lToken = TokenFactory.createToken(getNamespace(), "datetime");
			Date d;
			while (mIsServerTimeRunning) {
				if (mClients != null && !mClients.isEmpty()) {
					d = new Date();
					lToken.setInteger("hours", d.getHours());
					lToken.setInteger("minutes", d.getMinutes());
					lToken.setInteger("seconds", d.getSeconds());

					for (WebSocketConnector lConnector : mClients) {
						getServer().sendToken(lConnector, lToken);
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
					}
				}
			}
		}
	}
}
