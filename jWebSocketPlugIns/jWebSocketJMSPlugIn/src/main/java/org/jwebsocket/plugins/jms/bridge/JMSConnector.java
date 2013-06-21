//	---------------------------------------------------------------------------
//	jWebSocket - JMS Connector (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.jms.bridge;

import java.net.Inet4Address;
import java.net.InetAddress;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.RequestHeader;

/**
 * JMS Gateway Connector, the WebSocket Connector Pendant
 *
 * @author Alexander Schulze
 */
public class JMSConnector extends BaseConnector {

	JMSSender mJMSSender = null;
	private String mTargetId = "-";
	private String mRemoteHost = "-";

	/**
	 *
	 * @param aEngine
	 * @param aJMSSender
	 * @param aTargetId
	 */
	public JMSConnector(WebSocketEngine aEngine, JMSSender aJMSSender,
			String aRemoteHost, String aTargetId) {
		super(aEngine);

		mJMSSender = aJMSSender;

		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		setHeader(lHeader);

		mRemoteHost = aRemoteHost;
		mTargetId = aTargetId;
	}

	@Override
	public String getId() {
		return mTargetId;
	}

	@Override
	public void sendPacket(final WebSocketPacket aDataPacket) {
		mJMSSender.sendText(mTargetId, aDataPacket.getUTF8());
	}
}
