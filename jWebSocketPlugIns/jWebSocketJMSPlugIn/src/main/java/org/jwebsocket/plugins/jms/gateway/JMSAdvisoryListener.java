//	---------------------------------------------------------------------------
//	jWebSocket - JMS Advisory Listener (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.gateway;

import java.util.Map;
import javax.jms.Message;
import javax.jms.MessageListener;
import javolution.util.FastMap;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author alexanderschulze
 */
public class JMSAdvisoryListener implements MessageListener {

	private static final Logger mLog = Logging.getLogger();
	private JMSEngine mEngine = null;
	private JMSSender mJMSSender = null;
	private final Map<String, String> mEndPoints = new FastMap<String, String>();

	/**
	 *
	 * @param aEngine
	 * @param aJMSSender
	 */
	public JMSAdvisoryListener(JMSEngine aEngine, JMSSender aJMSSender) {
		mEngine = aEngine;
		mJMSSender = aJMSSender;
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onMessage(Message aMessage) {

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
					String lEndPointId = lConsumer.getSelector();
					if (null == lEndPointId) {
						lEndPointId = lConnectionId;
					} else {
						int lStart = lEndPointId.indexOf("'");
						int lEnd = lEndPointId.indexOf("'", lStart + 1);
						lEndPointId = lEndPointId.substring(lStart + 1, lEnd);
					}

					// add connector if not event from the gateway itself.
					if (null != lEndPointId) {
						if (lEndPointId.equals(mJMSSender.getEndPointId())) {
							if (mLog.isInfoEnabled()) {
								mLog.info("JMS Gateway successfully connected to broker.");
							}
						} else {
							mEndPoints.put(lConnectionId, lEndPointId);
							WebSocketConnector lConnector = new JMSConnector(mEngine,
									mJMSSender, lConnectionId, lEndPointId);
							if (mLog.isDebugEnabled()) {
								mLog.debug("Adding connector to engine...");
							}
							mEngine.addConnector(lConnector);

							if (mLog.isInfoEnabled()) {
								mLog.info("JMS client connected"
										+ ", connector '" + lEndPointId
										+ "' added to JMSEngine"
										+ ", connection-id: '"
										+ lConnectionId + "'.");
							}
							Token lToken = TokenFactory.createToken(
									"org.jwebsocket.jms.gateway",
									"welcome");
							lConnector.sendPacket(JSONProcessor.tokenToPacket(lToken));
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
						String lEndPointId = mEndPoints.get(lConnectionId);
						WebSocketConnector lConnector = null;
						if (null != lEndPointId) {
							lConnector = mEngine.getConnectors().get(lEndPointId);
							mEndPoints.remove(lConnectionId);
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
						} else {
							mLog.error("Connector '" + lConnectionId
									+ "' could not be removed from JMSEngine!");
						}
					} else {
						mLog.warn("Unknown remove message: " + aMessage);
					}
				} else {
					mLog.warn("Unknown advisory message: " + aMessage);
				}
			} catch (Exception lEx) {
				mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage() + ", for " + aMessage);
			}
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
