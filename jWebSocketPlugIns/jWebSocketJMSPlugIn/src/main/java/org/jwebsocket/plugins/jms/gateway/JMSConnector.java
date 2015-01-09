//	---------------------------------------------------------------------------
//	jWebSocket - JMS Connector (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.gateway;

import java.util.Map;
import javax.jms.JMSException;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.util.Tools;

/**
 * JMS Gateway Connector, the WebSocket Connector Pendant
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class JMSConnector extends BaseConnector {

	JMSSender mJMSSender = null;
	private String mTargetId = "-";
	private String mConnectionId;

	/**
	 *
	 * @param aEngine
	 * @param aJMSSender
	 * @param aConnectionId
	 * @param aTargetId
	 */
	public JMSConnector(WebSocketEngine aEngine, JMSSender aJMSSender,
			String aConnectionId, String aTargetId) {
		super(aEngine);

		mJMSSender = aJMSSender;

		WebSocketSession lSession = getSession();
		lSession.setSessionId(Tools.getMD5(aConnectionId));

		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		Map<String, String> lCookiesMap = new FastMap<String, String>().shared();
		lHeader.put(RequestHeader.WS_COOKIES, lCookiesMap);
		setHeader(lHeader);

		mConnectionId = aConnectionId;
		mTargetId = aTargetId;

		// specify the gateway per connector, 
		// we might have multiple jWebSocket instances connected to a queue
		setVar("$gatewayId", mJMSSender.getEndPointId());

		// specifying the client type by default
		setVar("jwsType", "jms-gateway");
	}

	@Override
	public String getId() {
		return mTargetId;
	}

	@Override
	public boolean supportsTransactions() {
		return false;
	}

	/**
	 * Set the remote client connection id value.
	 *
	 * @param aConnectionId
	 */
	public void setConnectionId(String aConnectionId) {
		mConnectionId = aConnectionId;
	}

	/**
	 * Get the remote client connection id value.
	 *
	 * @return
	 */
	public String getConnectionId() {
		return mConnectionId;
	}

	@Override
	public void sendPacket(final WebSocketPacket aDataPacket) {
		try {
			mJMSSender.sendText(mTargetId, aDataPacket.getUTF8());
		} catch (JMSException lEx) {
			// TODO: Process exception
		}
	}
}
