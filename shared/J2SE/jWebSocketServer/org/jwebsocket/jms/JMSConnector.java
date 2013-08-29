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

import java.util.Arrays;
import java.util.Map;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javolution.util.FastMap;
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
import org.springframework.util.Assert;

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
	private static Map<String, IPacketDeliveryListener> mPacketDeliveryListeners =
			new FastMap<String, IPacketDeliveryListener>().shared();

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

		if (!aCloseReason.equals(CloseReason.CLIENT)) {
			sendPacket(new RawPacket(WebSocketFrameType.CLOSE,
					WebSocketProtocolAbstraction.calcCloseData(1000, aCloseReason.name())));
		}
		super.stopConnector(aCloseReason);
	}

	@Override
	public void sendPacket(final WebSocketPacket aDataPacket) {
		sendPacketInTransaction(aDataPacket, getMaxFrameSize(), null);
	}

	@Override
	public Integer getMaxFrameSize() {
		return getEngine().getConfiguration().getMaxFramesize();
	}

	@Override
	public void processPacket(WebSocketPacket aDataPacket) {
		// notifying engine
		if (getEngine() != null) {
			getEngine().processPacket(this, aDataPacket);
		}
	}

	protected ActiveMQTextMessage buildMessage() throws JMSException {
		ActiveMQTextMessage lMessage = new ActiveMQTextMessage();
		lMessage.setStringProperty(Attributes.CONNECTION_ID, mConnectionId);
		lMessage.setStringProperty(Attributes.REPLY_SELECTOR, mReplySelector);

		return lMessage;
	}

	@Override
	public void sendPacketInTransaction(final WebSocketPacket aDataPacket, final Integer aFragmentSize,
			final IPacketDeliveryListener aListener) {
		try {
			ActiveMQTextMessage lMessage = buildMessage();

			Assert.isTrue(aDataPacket.size() <= getMaxFrameSize(),
					"The packet size exceeds the max frame size supported by the client!");

			if (aDataPacket.size() > aFragmentSize) {
				final RawPacket lFragment = new RawPacket(Arrays.copyOfRange(aDataPacket.getByteArray(), 0, aFragmentSize));

				final long lSentTime = System.currentTimeMillis();
				final long lOriginTimeout = (null != aListener) ? aListener.getTimeout() : 1000 * 10;

				// setting the message content
				lMessage.setText(lFragment.getString());
				// setting the expiration timeout
				lMessage.setExpiration(lOriginTimeout);
				// sending the message
				mReplyProducer.send(lMessage);

				mPacketDeliveryListeners.put(lMessage.getJMSMessageID(), new IPacketDeliveryListener() {
					private int mBytesSent = 0;

					@Override
					public long getTimeout() {
						long lTimeout = lSentTime + lOriginTimeout - System.currentTimeMillis();
						if (lTimeout < 0) {
							lTimeout = 0;
						}

						return lTimeout;
					}

					@Override
					public void OnTimeout() {
						if (aListener != null) {
							aListener.OnTimeout();
						}
					}

					@Override
					public void OnSuccess() {
						// updating bytes sent
						mBytesSent += aFragmentSize;
						if (mBytesSent >= aDataPacket.size()) {
							// calling success if the packet was transmitted complete
							if (null != aListener) {
								aListener.OnSuccess();
							}
						} else {
							// prepare to sent a next fragment
							int lLength = (aFragmentSize + mBytesSent <= aDataPacket.size())
									? aFragmentSize
									: aDataPacket.size() - mBytesSent;

							byte[] lBytes = Arrays.copyOfRange(aDataPacket.getByteArray(), mBytesSent, mBytesSent + lLength);

							// sending next fragment
							sendPacketInTransaction(new RawPacket(lBytes), this);
						}
					}

					@Override
					public void OnFailure(Exception lEx) {
						if (aListener != null) {
							aListener.OnFailure(lEx);
						}
					}
				});
			} else {
				// setting the message content
				lMessage.setText(aDataPacket.getString());
				// storing the listener
				if (null != aListener) {
					// setting the expiration timeout
					lMessage.setExpiration(aListener.getTimeout());
					// sending the message
					mReplyProducer.send(lMessage);
					// storing the listener
					mPacketDeliveryListeners.put(lMessage.getJMSMessageID(), aListener);
				} else {
					// sending the message
					mReplyProducer.send(lMessage);
				}
			}
		} catch (InvalidDestinationException lEx) {
			aListener.OnFailure(lEx);
			// exception could happen if there is only one node and it gets incorrectly closed
			// connectors information could remains on database
			// what we do here is to keep clean the connectors database
			try {
				((JMSEngine) getEngine()).getConnectorsManager().removeConnector(getSession().getSessionId());
			} catch (Exception lEx2) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "cleaning connectors database"));
			}
		} catch (Exception lEx) {
			aListener.OnFailure(lEx);
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending packet to '" + getId() + "' connector"));
		}
	}

	public static void processMessageDelivered(String aMessageId) {
		if (mPacketDeliveryListeners.containsKey(aMessageId)) {
			mPacketDeliveryListeners.remove(aMessageId).OnSuccess();
		}
	}

	public static void processMessageExpired(String aMessageId) {
		if (mPacketDeliveryListeners.containsKey(aMessageId)) {
			mPacketDeliveryListeners.remove(aMessageId).OnTimeout();
		}
	}
}
