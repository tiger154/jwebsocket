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
import java.util.UUID;
import javax.jms.JMSException;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketSession;

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
	 * @param aRemoteHost
	 * @param aTargetId
	 */
	public JMSConnector(WebSocketEngine aEngine, JMSSender aJMSSender,
			String aRemoteHost, String aTargetId) {
		super(aEngine);

		mJMSSender = aJMSSender;

		WebSocketSession lSession = getSession();
		lSession.setSessionId(UUID.randomUUID().toString());

		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		Map<String, String> lCookiesMap = new FastMap<String, String>().shared();
		lCookiesMap.put(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME, lSession.getSessionId());
		lHeader.put(RequestHeader.WS_COOKIES, lCookiesMap);
		setHeader(lHeader);

		mRemoteHost = aRemoteHost;
		mTargetId = aTargetId;

		// specify the gateway per connector, 
		// we might have multiple jWebSocket instances connected to a queue
		setVar("$gatewayId", mJMSSender.getEndPointId());
		
		// specifying the client type by default
		setVar("jwsType", "java");
	}

	@Override
	public String getId() {
		return mTargetId;
	}

	@Override
	public boolean supportsTransactions() {
		return false;
	}

//	@Override
//	public InetAddress getRemoteHost() {
//		try {
//			return InetAddress.getByName(mRemoteHost);
//		} catch (UnknownHostException lEx) {
//			return null;
//		}
//	}
	@Override
	public void sendPacket(final WebSocketPacket aDataPacket) {
		try {
			mJMSSender.sendText(mTargetId, aDataPacket.getUTF8());
		} catch (JMSException lEx) {
			// TODO: Process exception
		}
	}
}
