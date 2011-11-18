//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Sample RPC-Library
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.rpc.sample;

import java.util.ArrayList;
import java.util.List;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.rpc.BaseConnectorRPCCallable;
import org.jwebsocket.plugins.rpc.rrpc.Rrpc;

public class Messaging extends BaseConnectorRPCCallable {

	private List<String> mMessages = new ArrayList<String>();

	public Messaging(WebSocketConnector aConnector) {
		super(aConnector);
		mMessages.add("Hello Quentin");
		mMessages.add("How Are You ?");
	}

	/**
	 * Send the list of messages to the connector throw a RRPC. We will supposed this
	 * task can be long (access to the messages from a hudge database, for instance)
	 */
	public void getMyMessages() {
		//We get the current connector:
		WebSocketConnector connector = getConnector();
		for (int i = 0; i < mMessages.size(); i++) {
			String message = mMessages.get(i);
			new Rrpc("org.jwebsocket.android.demo.RPCDemoActivity", "receiveMessage").to(connector).send(message).call();
		}
	}
}
