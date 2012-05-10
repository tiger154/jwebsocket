//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JQuery Demo Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//	This is a timetask class, see the run method implementation
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
 *
 * @author Victor and Carlos
 */
package org.jwebsocket.plugins.jquery;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TimerTask;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

public class UpdateTimeTask extends TimerTask {

	private TokenServer mServer;
	private Token mToken;

	public UpdateTimeTask(TokenServer aServer, String aNamespace) {
		mServer = aServer;
		mToken = TokenFactory.createToken(aNamespace, "datetime");
	}

	@Override
	public void run() {
		while (true) {
			Date lDate = new Date();
			mToken.setInteger("hours", lDate.getHours());
			mToken.setInteger("minutes", lDate.getMinutes());
			mToken.setInteger("seconds", lDate.getSeconds());

			if (mServer.getAllConnectors() != null) {
				Map lConnectors = mServer.getAllConnectors();
				if (null != lConnectors) {
					//sending the time each one second to all connected clients
					for (WebSocketConnector lConnector : (Collection<WebSocketConnector>) lConnectors.values()) {
						mServer.sendToken(lConnector, mToken);
					}
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception lEx) {
				System.out.println("An exception has been detected: " + lEx.getMessage());
			}
		}
	}
}
