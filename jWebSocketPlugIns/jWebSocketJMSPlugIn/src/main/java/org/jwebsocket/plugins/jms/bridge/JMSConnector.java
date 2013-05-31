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

import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.RequestHeader;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author alexanderschulze
 */
public class JMSConnector extends BaseConnector {

	JmsTemplate mJMSTemplate = null;

	public JMSConnector(WebSocketEngine aEngine, JmsTemplate aJMSTemplate) {
		super(aEngine);

		mJMSTemplate = aJMSTemplate;
		
		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		setHeader(lHeader);
	}

	@Override
	public String getNodeId() {
		return "JMS.TOP1.1";
	}

	@Override
	public String getId() {
		return "JMS.TOP1.1";
	}
	
	@Override
	public Boolean supportTokens() {
		return true;
	}

	@Override
	public void sendPacket(WebSocketPacket aDataPacket) {
		mJMSTemplate.convertAndSend(aDataPacket.getUTF8());
	}
}
