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
import org.jwebsocket.token.Token;
import java.util.Date;
import java.util.Map;
import java.util.TimerTask;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.TokenFactory;

public class UpdateTimeTask extends TimerTask {

	private TokenServer server;
	private Token t;

	public UpdateTimeTask(TokenServer server, String namespace) {
		this.server = server;
		this.t = TokenFactory.createToken(namespace, "datetime");
	}

	@Override
	public void run() {
		while (true) {
			Date d = new Date();
			t.setInteger("hours", d.getHours());
			t.setInteger("minutes", d.getMinutes());
			t.setInteger("seconds", d.getSeconds());

			if (server.getAllConnectors() != null) {
				Map col = server.getAllConnectors();
				if (col != null) {
					//sending the time each one second to all connected clients
					for (WebSocketConnector c : (Collection<WebSocketConnector>) col.values()) {
						server.sendToken(c, t);
					}
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ex) {
				System.out.println("An exception has been detected: " + ex.getMessage());
			}
		}
	}
}
