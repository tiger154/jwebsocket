//	---------------------------------------------------------------------------
//	jWebSocket - JMS Advisory Listener (Community Edition, CE)
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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.jms.JMSPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author alexanderschulze
 */
public class JMSAdvisoryListener implements MessageListener {

	private static final Logger mLog = Logging.getLogger();
	private static JMSPlugIn mJMSPlugIn = null;
	private final boolean mBroadcastEvents;
	private final JMSEngine mEngine;
	private final JMSSender mJMSSender;

	/**
	 *
	 * @param aJMSPlugIn
	 * @param aEngine
	 * @param aJMSSender
	 * @param aBroadcastEvents
	 */
	public JMSAdvisoryListener(JMSPlugIn aJMSPlugIn, JMSEngine aEngine, JMSSender aJMSSender, boolean aBroadcastEvents) {
		mEngine = aEngine;
		mJMSSender = aJMSSender;
		if (null == mJMSPlugIn) {
			mJMSPlugIn = aJMSPlugIn;
		}
		mBroadcastEvents = aBroadcastEvents;
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onMessage(final Message aMessage) {
		Tools.getThreadPool().submit(new Runnable() {

			@Override
			public void run() {
				boolean lBroadcast = false;
				final Token lBroadcastToken;
				if (mBroadcastEvents) {
					lBroadcastToken = TokenFactory.createToken(mJMSPlugIn.getNamespace(), "event");
				} else {
					lBroadcastToken = null;
				}

				if (aMessage instanceof ActiveMQMessage) {
					try {
						ActiveMQMessage lMessage = (ActiveMQMessage) aMessage;
						Object lDataStructure = lMessage.getDataStructure();
						if (lDataStructure instanceof ProducerInfo) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Received producer info: " + aMessage);
							}
							ProducerInfo lProd = (ProducerInfo) lMessage.getDataStructure();
							mLog.info(lProd);
						} else if (lDataStructure instanceof ConsumerInfo) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Received consumer info: " + aMessage);
							}
							ConsumerInfo lConsumer = (ConsumerInfo) lMessage.getDataStructure();
							String lConnectionId = lConsumer.getConsumerId().getConnectionId();
							String lEndPointId = lConnectionId.split("-", 2)[0];

							// add connector if not event from the gateway itself.
							if (null != lEndPointId) {
								if (lEndPointId.equals(mJMSSender.getEndPointId())) {
									if (mLog.isInfoEnabled()) {
										mLog.info("JMS Gateway successfully connected to broker.");
									}

									broadcastIdentifyMessage();
								} else {
									JMSConnector lConnector = (JMSConnector) mEngine.getConnectorById(lEndPointId);
									if (null != lConnector) {
										// is a remote client reconnection
										// lConnector.setConnectionId(lConnectionId); //not necessary because the connection id remains equal
									} else {
										// registrating new remote client
										lConnector = new JMSConnector(mEngine,
												mJMSSender, lConnectionId, lEndPointId);

										if (mLog.isDebugEnabled()) {
											mLog.debug("Adding connector '"
													+ lEndPointId + "' to JMS engine...");
										}
										mEngine.addConnector(lConnector);
										lConnector.startConnector();

										if (mLog.isInfoEnabled()) {
											mLog.info("JMS client connected"
													+ ", connector '" + lEndPointId
													+ "' added to JMSEngine"
													+ ", connection-id: '"
													+ lConnectionId + "'.");
										}
										if (mBroadcastEvents) {
											lBroadcastToken.setString("endPointId", lEndPointId);
											lBroadcastToken.setString("name", "endPointConnected");
											lBroadcast = true;
										}
									}
								}
							}
						} else if (lDataStructure instanceof RemoveInfo) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Received remove info: " + aMessage);
							}
							RemoveInfo lRemove = (RemoveInfo) lMessage.getDataStructure();

							DataStructure lDS = lRemove.getObjectId();
							if (lDS instanceof ConsumerId) {
								String lConnectionId = ((ConsumerId) lDS).getConnectionId();
								String lEndPointId = lConnectionId.split("-", 2)[0];
								WebSocketConnector lConnector = null;
								if (null != lEndPointId) {
									lConnector = mEngine.getConnectors().get(lEndPointId);
								}
								if (null != lConnector) {
									mEngine.removeConnector(lConnector);
									if (mLog.isInfoEnabled()) {
										mLog.info("JMS client disconnected"
												+ ", connector '" + lEndPointId
												+ "' removed from JMSEngine"
												+ ", connection-id: '"
												+ lConnectionId + "'.");
									}
									lConnector.stopConnector(CloseReason.BROKEN);
									if (mBroadcastEvents) {
										lBroadcastToken.setString("endPointId", lEndPointId);
										lBroadcastToken.setString("name", "endPointDisconnected");
										lBroadcast = true;
									}
								} else {
									mLog.error("Connector '" + lConnectionId
											+ "' could not be removed from JMSEngine!");
								}
							} else {
								mLog.warn("Unknown remove message: " + aMessage);
							}
						} else if (lDataStructure instanceof ConnectionInfo) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Received connection info: " + aMessage);
							}
							ConnectionInfo lConnection = (ConnectionInfo) lMessage.getDataStructure();
							if (mLog.isInfoEnabled()) {
								mLog.info("JMS Connection Event (from IP: " + lConnection.getClientIp() + "):" + lConnection.toString());
							}
						} else {
							mLog.warn("Unknown advisory message: " + aMessage);
						}
					} catch (Exception lEx) {
						mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage() + ", for " + aMessage);
					}
				}
				if (mBroadcastEvents && lBroadcast) {
					mJMSPlugIn.broadcastToken(null, lBroadcastToken,
							new BroadcastOptions(
									false, // lIsSenderIncluded
									false // lIsResponseRequested
							));
				}
			}
		});
	}

	/**
	 * Send the "identify" command to all connected clients
	 */
	private void broadcastIdentifyMessage() {
		Token lIdentify = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE + ".jms.gateway", "identify");
		lIdentify.setString("sourceId", getSender().getEndPointId());

		try {
			getSender().sendText("*", JSONProcessor.tokenToPacket(lIdentify).getString());
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending identify broadcast to all connected endpoints"));
		}
	}

	/**
	 * @return the mJMSEngine
	 */
	public JMSEngine getEngine() {
		return mEngine;
	}

	/**
	 * @return the JMSSender
	 */
	public JMSSender getSender() {
		return mJMSSender;
	}
}
