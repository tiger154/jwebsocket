//	---------------------------------------------------------------------------
//	jWebSocket - JWebSocketWSClient (Community Edition, CE)
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
package org.jwebsocket.client.token;

import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.config.ReliabilityOptions;
import org.jwebsocket.kit.WebSocketEncoding;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class JWebSocketTokenClient extends BaseTokenClient {

	/**
	 *
	 * @param aReliabilityOptions
	 */
	public JWebSocketTokenClient(ReliabilityOptions aReliabilityOptions) {
		super(aReliabilityOptions);
	}

	/**
	 *
	 * @param aSubProt
	 * @param aEncoding
	 */
	public JWebSocketTokenClient(String aSubProt, WebSocketEncoding aEncoding) {
		super(aSubProt, aEncoding);
	}

	/**
	 *
	 * @param aClient
	 */
	public JWebSocketTokenClient(WebSocketClient aClient) {
		super(aClient);
	}

	/**
	 *
	 * @param aSubProt
	 * @param aEncoding
	 * @param aClient
	 */
	public JWebSocketTokenClient(String aSubProt, WebSocketEncoding aEncoding, WebSocketClient aClient) {
		super(aSubProt, aEncoding, aClient);
	}

	/**
	 *
	 */
	public JWebSocketTokenClient() {
		super();
	}
}
