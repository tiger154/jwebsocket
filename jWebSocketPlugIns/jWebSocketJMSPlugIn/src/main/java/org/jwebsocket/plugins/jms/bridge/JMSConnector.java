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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.RequestHeader;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 *
 * @author alexanderschulze
 */
public class JMSConnector extends BaseConnector {

	JmsTemplate mJmsTemplate = null;
	private String mConnectionId = "-";
	private String mCorrelationId = "-";

	public JMSConnector(WebSocketEngine aEngine, JmsTemplate aJMSTemplate,
			String aConnectionId, String aCorrelationId) {
		super(aEngine);

		mJmsTemplate = aJMSTemplate;
		
		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		setHeader(lHeader);
		
		mConnectionId = aConnectionId;
		mCorrelationId = aCorrelationId;
	}

	@Override
	public String getId() {
		return mConnectionId;
	}

	@Override
	public void sendPacket(final WebSocketPacket aDataPacket) {
		mJmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session aSession) throws JMSException {
				Message lMsg = aSession.createTextMessage(aDataPacket.getUTF8());
				lMsg.setJMSCorrelationID(mCorrelationId);
				return lMsg;
			}
		});
			
	}
}
