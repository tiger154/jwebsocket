//	---------------------------------------------------------------------------
//	jWebSocket - JMS Cluster Advisory Listener (Community Edition, CE)
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
package org.jwebsocket.plugins.cluster;

import java.util.List;
import javax.jms.Message;
import javax.jms.MessageListener;
import javolution.util.FastList;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author alexanderschulze
 */
public class ClusterAdvisoryListener implements MessageListener {

	private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onMessage(Message aMessage) {

		if (mLog.isDebugEnabled()) {
			mLog.debug("Message received: " + aMessage);
		}

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
					mLog.info("Cluster node connected, id: '"
							+ lConnectionId + "'.");

				} else if (lDataStructure instanceof RemoveInfo) {
					RemoveInfo lRemove = (RemoveInfo) lMessage.getDataStructure();
					DataStructure lDS = lRemove.getObjectId();
					if (lDS instanceof ConsumerId) {
						String lConnectionId = ((ConsumerId) lDS).getConnectionId();
						mLog.info("Cluster node disconnected, "
								+ "id: '" + lConnectionId
								+ "'.");
					} else {
						mLog.warn("Unknown remove message: " + aMessage);
					}
				} else {
					mLog.warn("Unknown advisory message: " + aMessage);
				}
			} catch (Exception lEx) {
				mLog.error(lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ", for message '" + aMessage + "'.");
			}
		}
	}
}
