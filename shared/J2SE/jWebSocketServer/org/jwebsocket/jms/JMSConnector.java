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
package org.jwebsocket.jms;

import java.util.Map;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.kit.WebSocketProtocolAbstraction;
import org.jwebsocket.logging.Logging;

/**
 * JMS Connector
 *
 * @author Alexander Schulze
 * @author kyberneees
 */
public class JMSConnector extends BaseConnector {

	private final String mSessionId;
	private final MessageProducer mReplyProducer;
	private final String mConnectionId;
	private final String mReplySelector;
	private Logger mLog = Logging.getLogger();

	/**
	 * jWebSocket connector implementation for JMS connections
	 *
	 * @param aEngine
	 * @param aJMSSender
	 * @param aRemoteHost
	 * @param aSessionId
	 */
	public JMSConnector(WebSocketEngine aEngine, String aSessionId, String aReplySelector, String aConnectionId) {
		super(aEngine);

		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		setHeader(lHeader);

		mReplySelector = aReplySelector;
		mReplyProducer = ((JMSEngine) aEngine).getReplyProducer();
		mSessionId = aSessionId;
		mConnectionId = aConnectionId;
	}

	public void setCustomVarsContainer(Map<String, Object> aMap) {
		mCustomVars = aMap;
	}

	@Override
	public String getId() {
		return mSessionId;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		setStatus(WebSocketConnectorStatus.DOWN);

		try {
			ActiveMQTextMessage lMessage = buildMessage(aCloseReason.name());
			lMessage.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.DISCONNECTION.name());
			mReplyProducer.send(lMessage);
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending close message to client: '" + getId() + "'"));
		}
		super.stopConnector(aCloseReason);
	}

	@Override
	public void sendPacket(WebSocketPacket aDataPacket) {
		try {
			mReplyProducer.send(buildMessage(aDataPacket.getString()));
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending message to client: '" + getId() + "'"));
		}
	}

	@Override
	public Integer getMaxFrameSize() {
		return getEngine().getConfiguration().getMaxFramesize();
	}

	protected ActiveMQTextMessage buildMessage(String aData) throws JMSException {
		ActiveMQTextMessage lMessage = new ActiveMQTextMessage();
		lMessage.setStringProperty(Attributes.CONNECTION_ID, mConnectionId);
		lMessage.setStringProperty(Attributes.REPLY_SELECTOR, mReplySelector);
		lMessage.setText(aData);

		return lMessage;
	}
}
