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
package org.jwebsocket.plugins.jms.bridge;

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
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author alexanderschulze
 */
public class JMSAdvisoryListener implements MessageListener {

	private static Logger mLog = Logging.getLogger();
	private JMSEngine mEngine = null;
	private JmsTemplate mJMSTemplate = null;
	private Map<String, String> mCorrelations = new FastMap<String, String>();

	@Override
	public void onMessage(Message aMessage) {

		mLog.info(">>>> " + aMessage);

		if (aMessage instanceof ActiveMQMessage) {
			try {
				ActiveMQMessage lMessage = (ActiveMQMessage) aMessage;
				Object lDataStructure = lMessage.getDataStructure();
				if (lDataStructure instanceof ProducerInfo) {
					ProducerInfo lProd = (ProducerInfo) lMessage.getDataStructure();
					mLog.info(lProd);
				} else if (lDataStructure instanceof ConsumerInfo) {
					ConsumerInfo lConsumer = (ConsumerInfo) lMessage.getDataStructure();
					String lConnectionId = lConsumer.getConsumerId().getConnectionId();
					String lCorrelationId = lConsumer.getSelector();
					if (null == lCorrelationId) {
						lCorrelationId = lConnectionId;
					} else {
						int lStart = lCorrelationId.indexOf("'");
						int lEnd = lCorrelationId.indexOf("'", lStart + 1);
						lCorrelationId = lCorrelationId.substring(lStart + 1, lEnd);
					}

					mCorrelations.put(lConnectionId, lCorrelationId);
					WebSocketConnector lConnector = new JMSConnector(mEngine,
							mJMSTemplate, lCorrelationId, lCorrelationId);
					mEngine.addConnector(lConnector);

					mLog.info("JMS client connected, connector '"
							+ lConnectionId 
							+ "' added to JMSEngine, correlation-id: '" 
							+ lCorrelationId + "'.");
					Token lToken = TokenFactory.createToken(
							"org.jwebsocket.jms.bridge",
							"welcome");
					lConnector.sendPacket(JSONProcessor.tokenToPacket(lToken));

				} else if (lDataStructure instanceof RemoveInfo) {
					RemoveInfo lRemove = (RemoveInfo) lMessage.getDataStructure();

					DataStructure lDS = lRemove.getObjectId();
					if (lDS instanceof ConsumerId) {
						String lConnectionId = ((ConsumerId) lDS).getConnectionId();
						String lCorrelationId = mCorrelations.get(lConnectionId);
						WebSocketConnector lConnector = null;
						if (null != lCorrelationId) {
							lConnector = mEngine.getConnectors().get(lCorrelationId);
							mCorrelations.remove(lConnectionId);
						}
						if (null != lConnector) {
							mEngine.removeConnector(lConnector);
							mLog.info("JMS client disconnected, "
									+ "Connector '" + lConnectionId
									+ "' removed from JMSEngine, correlation-id: '" + lCorrelationId + "'.");
						} else {
							mLog.error("Connector '" + lConnectionId + "' could not be removed from JMSEngine!");
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
	 * @param aEngine the mJMSEngine to set
	 */
	public void setEngine(JMSEngine aEngine) {
		mEngine = aEngine;
	}

	/**
	 * @return the mJMSTemplate
	 */
	public JmsTemplate getJMSTemplate() {
		return mJMSTemplate;
	}

	/**
	 * @param aJMSTemplate the mJMSTemplate to set
	 */
	public void setJMSTemplate(JmsTemplate aJMSTemplate) {
		mJMSTemplate = aJMSTemplate;
	}
}
