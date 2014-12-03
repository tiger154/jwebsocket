//	---------------------------------------------------------------------------
//	jWebSocket - Messaging (Community Edition, CE)
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
package org.jwebsocket.rpc.sample;

import java.util.ArrayList;
import java.util.List;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.rpc.BaseConnectorRPCCallable;
import org.jwebsocket.plugins.rpc.rrpc.Rrpc;

/**
 *
 * @author Alexander Schulze
 */
public class Messaging extends BaseConnectorRPCCallable {

	private List<String> mMessages = new ArrayList<String>();

	/**
	 *
	 * @param aConnector
	 */
	public Messaging(WebSocketConnector aConnector) {
		super(aConnector);
		mMessages.add("Hello Quentin");
		mMessages.add("How Are You ?");
	}

	/**
	 * Send the list of messages to the connector throw a RRPC. We will supposed
	 * this task can be long (access to the messages from a hudge database, for
	 * instance)
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
