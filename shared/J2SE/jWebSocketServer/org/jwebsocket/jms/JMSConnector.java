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

import java.net.InetAddress;
import java.util.Map;
import javax.jms.InvalidDestinationException;
import javax.jms.MessageProducer;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IPacketDeliveryListener;
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
	private final String mRemoteHost;
	private final MessageProducer mReplyProducer;
	private final String mConnectionId;
	private Logger mLog = Logging.getLogger();

	/**
	 * jWebSocket connector implementation for JMS connections
	 *
	 * @param aEngine
	 * @param aJMSSender
	 * @param aRemoteHost
	 * @param aSessionId
	 */
	public JMSConnector(WebSocketEngine aEngine,
			String aRemoteHost, String aSessionId, String aConnectionId) {
		super(aEngine);

		RequestHeader lHeader = new RequestHeader();
		lHeader.put(RequestHeader.WS_PROTOCOL, "org.jwebsocket.json");
		setHeader(lHeader);

		mReplyProducer = ((JMSEngine) aEngine).getReplyProducer();
		mRemoteHost = aRemoteHost;
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

		if (!aCloseReason.equals(CloseReason.CLIENT)) {
			sendPacket(new RawPacket(WebSocketFrameType.CLOSE,
					WebSocketProtocolAbstraction.calcCloseData(1000, aCloseReason.name())));
		}
		super.stopConnector(aCloseReason);
	}

	@Override
	public InetAddress getRemoteHost() {
		try {
			return InetAddress.getByName(mRemoteHost);
		} catch (Exception lEx) {
			return null;
		}
	}

	@Override
	public void sendPacket(final WebSocketPacket aDataPacket) {
		try {
			ActiveMQTextMessage lMessage = new ActiveMQTextMessage();
			lMessage.setText(aDataPacket.getString());
			// securing the reply to owner connector
			lMessage.setStringProperty(Attributes.CONNECTION_ID, mConnectionId);
			mReplyProducer.send(lMessage);
		} catch (InvalidDestinationException lEx) {
			// exception could happen if there is only one node and it gets incorrectly closed
			// connectors information could remains on database
			// what we do here is to keep clean the connectors database
			try {
				((JMSEngine) getEngine()).getConnectorsManager().removeConnector(getSession().getSessionId());
			} catch (Exception lEx2) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "cleaning connectors database"));
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending packet to '" + getId() + "' connector"));
		}
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener) {
		throw new UnsupportedOperationException("Not supported operation on JMS connectors!");
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket, Integer aFragmentSize, IPacketDeliveryListener aListener) {
		throw new UnsupportedOperationException("Not supported operation on JMS connectors!");
	}
}