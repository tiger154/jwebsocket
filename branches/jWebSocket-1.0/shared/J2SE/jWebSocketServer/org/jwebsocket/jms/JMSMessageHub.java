//	---------------------------------------------------------------------------
//	jWebSocket - JMSMessageHub (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.logging.Logging;

/**
 * JMS based message hub for inter-nodes communication.
 *
 * @author kyberneees
 */
public class JMSMessageHub implements IInitializable {

	private final static Logger mLog = Logging.getLogger();
	private final Map<String, List<MessageListener>> mListeners = new FastMap<String, List<MessageListener>>().shared();
	private MessageConsumer mConsumer;
	private MessageProducer mProducer;
	private final JMSEngine mEngine;

	public JMSMessageHub(JMSEngine aEngine) {
		mEngine = aEngine;
	}

	@Override
	public void initialize() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Initializing message hub...");
		}

		Destination lDestination = mEngine.getSession().createTopic(mEngine.getDestination() + "_messagehub");
		mConsumer = mEngine.getSession().createConsumer(lDestination, Attributes.NAMESPACE + " IS NOT NULL");
		mConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message aMessage) {
				try {
					String lNS = aMessage.getStringProperty(Attributes.NAMESPACE);
					String lNodeId = aMessage.getStringProperty(Attributes.NODE_ID);
					Boolean lSenderIncluded = aMessage.getBooleanProperty(Attributes.SENDER_INCLUDED);

					// sender does not wants to receive the messaege
					if (mEngine.getNodeId().equals(lNodeId) && !lSenderIncluded) {
						return;
					}

					if (mListeners.containsKey(lNS)) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Notifying message(ns: " + lNS + ", senderNodeId: " + lNodeId + ") listeners...");
						}
						Iterator<MessageListener> lIt = mListeners.get(lNS).iterator();
						while (lIt.hasNext()) {
							try {
								lIt.next().onMessage(aMessage);
							} catch (Exception lEx) {
								mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying message listener"));
							}
						}
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "processing message"));
				}
			}
		});

		mProducer = mEngine.getSession().createProducer(lDestination);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Message hub successfully instantiated!");
		}
	}

	@Override
	public void shutdown() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Shutting down message hub...");
		}

		mConsumer.close();
		mProducer.close();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Message hub successfully terminated!");
		}
	}

	/**
	 * Registers a message listener for a target namespace.
	 *
	 * @param aNamespace
	 * @param aListener
	 * @return
	 */
	public MessageListener registerListener(String aNamespace, MessageListener aListener) {
		if (!mListeners.containsKey(aNamespace)) {
			mListeners.put(aNamespace, new FastList<MessageListener>());
		}

		mListeners.get(aNamespace).add(aListener);

		return aListener;
	}

	/**
	 * Removes a message listener on a target namespace.
	 *
	 * @param aNamespace
	 * @param aListener
	 * @return
	 */
	public boolean removeListener(String aNamespace, MessageListener aListener) {
		if (mListeners.containsKey(aNamespace)) {
			return mListeners.get(aNamespace).remove(aListener);
		}

		return false;
	}

	/**
	 * Builds a new message instance.
	 *
	 * @param aNS
	 * @param aMsgType
	 * @return
	 * @throws Exception
	 */
	public MapMessage buildMessage(String aNS, String aMsgType) throws Exception {
		MapMessage lMessage = mEngine.getSession().createMapMessage();
		lMessage.setStringProperty(Attributes.NAMESPACE, aNS);
		lMessage.setStringProperty(Attributes.MESSAGE_ID, UUID.randomUUID().toString());
		lMessage.setStringProperty(Attributes.NODE_ID, mEngine.getNodeId());
		lMessage.setStringProperty(Attributes.MESSAGE_TYPE, aMsgType);

		return lMessage;
	}

	/**
	 * Sends a message through the message hub.
	 *
	 * @param aMessage
	 * @throws Exception
	 */
	public void send(Message aMessage) throws Exception {
		send(aMessage, false);
	}

	/**
	 * Sends a message through the message hub.
	 *
	 * @param aMessage
	 * @throws Exception
	 */
	public void send(Message aMessage, boolean aSenderIncluded) throws Exception {
		aMessage.setBooleanProperty(Attributes.SENDER_INCLUDED, aSenderIncluded);
		if (mLog.isDebugEnabled()) {
			mLog.info("Sending message '" + aMessage.toString() + "' to cluster mesage hub...");
		}

		mProducer.send(aMessage);
	}
}
