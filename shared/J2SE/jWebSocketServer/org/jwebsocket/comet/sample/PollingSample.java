// ---------------------------------------------------------------------------
// jWebSocket
// Copyright (c) 2012 jWebSocket.org, Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.comet.sample;

import java.util.Iterator;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author naruto
 */
public class PollingSample extends TokenPlugIn {

	/**
	 * 
	 * @param aConfiguration
	 */
	public PollingSample(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		if (aToken.getNS().equals(getNamespace())) {
			if (aToken.getType().equals("chat")) {
				String name = aToken.getString("message");

				Token result = createResponse(aToken);

				result.setString("data", name);
				WebSocketConnector c;
				Iterator<WebSocketConnector> clients = getServer().getAllConnectors().values().iterator();
				while (clients.hasNext()) {
					c = clients.next();
					if (!c.equals(aConnector)) {
						try {
							getServer().sendToken(c, result);
						} catch (Exception e) {
							getServer().sendToken(c, result);
						}

					}
				}
			}
		}
	}
}
