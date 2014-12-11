//	---------------------------------------------------------------------------
//	jWebSocket - JMS Bridge JMSListener (Community Edition, CE)
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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class JMSLogger implements MessageListener {

	private static final Logger mLog = Logger.getLogger(JMSLogger.class);
	private Session mLoggerSession;
	private MessageConsumer[] mLoggerConsumer;

	/**
	 *
	 * @param aConnection
	 * @param aTopics
	 */
	public JMSLogger(Connection aConnection, Destination[] aTopics) {
		try {
			mLoggerSession = aConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			int lIdx = 0;
			mLoggerConsumer = new MessageConsumer[aTopics.length];
			for (Destination lDestination : aTopics) {
				mLoggerConsumer[lIdx] = mLoggerSession.createConsumer(lDestination);
				mLoggerConsumer[lIdx].setMessageListener(this);
				lIdx++;
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"instantiating JMS Logger."));
		}
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void onMessage(Message aMsg) {
		String lMsg = "[message could not be interpreted]", 
				lSourceId = "[sourceId unknown]", 
				lTargetId = "[targetId unknown]";
		try {
			if (aMsg instanceof ActiveMQTextMessage) {
				ActiveMQTextMessage lTextMsg = (ActiveMQTextMessage) aMsg;
				lMsg = lTextMsg.getText();
				lSourceId = (String) lTextMsg.getProperty("sourceId");
				lTargetId = (String) lTextMsg.getProperty("targetId");
				if (mLog.isInfoEnabled()) {
					mLog.info("Text: '" + lSourceId + "'->'" + lTargetId + "': " + lMsg);
				}
			} else if (aMsg instanceof ActiveMQBytesMessage) {
				ActiveMQBytesMessage lBytesMsg = (ActiveMQBytesMessage) aMsg;
				lMsg = new String(lBytesMsg.getContent().getData(), "UTF-8");
				lSourceId = (String) lBytesMsg.getProperty("sourceId");
				lTargetId = (String) lBytesMsg.getProperty("targetId");
				if (mLog.isInfoEnabled()) {
					mLog.info("Binary: '" + lSourceId + "'->'" + lTargetId + "': " + lMsg);
				}
			} else if (aMsg instanceof ActiveMQMessage) {
				try {
					ActiveMQMessage lAdvMsg = (ActiveMQMessage) aMsg;
					Object lDataStructure = lAdvMsg.getDataStructure();
					if (lDataStructure instanceof ProducerInfo) {
						ProducerInfo lProd = (ProducerInfo) lAdvMsg.getDataStructure();
						if (mLog.isInfoEnabled()) {
							mLog.info("Producer connected: " + lProd);
						}
					} else if (lDataStructure instanceof ConsumerInfo) {
						ConsumerInfo lConsumer = (ConsumerInfo) lAdvMsg.getDataStructure();
						if (mLog.isInfoEnabled()) {
							mLog.info("Endpoint connected: " + lConsumer);
						}
					} else if (lDataStructure instanceof RemoveInfo) {
						RemoveInfo lRemove = (RemoveInfo) lAdvMsg.getDataStructure();
						if (mLog.isInfoEnabled()) {
							mLog.info("Endpoint disconnected: " + lRemove);
						}
					} else {
						mLog.warn("Unknown advisory message: " + aMsg);
					}
				} catch (Exception lEx) {
					mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage() + ", for " + aMsg);
				}

			} else {
				lMsg = aMsg.getJMSType() + ": " + aMsg.toString();
				lSourceId = (String) aMsg.getStringProperty("sourceId");
				lTargetId = (String) aMsg.getStringProperty("targetId");
				if (mLog.isDebugEnabled()) {
					mLog.debug("Unknown: '" + lSourceId + "'->'" + lTargetId + "': " + lMsg);
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"getting " + aMsg.getClass().getSimpleName() + " message"));
		}
	}

	/**
	 *
	 */
	public void close() {
		try {
			if (null != mLoggerConsumer) {
				for (MessageConsumer lConsumer : mLoggerConsumer) {
					lConsumer.close();
				}
			}
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"closing JMS logger consumer"));
		}
		try {
			if (null != mLoggerSession) {
				mLoggerSession.close();
			}
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"closing JMS logger session"));
		}
	}

}
