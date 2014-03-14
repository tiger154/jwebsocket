//	---------------------------------------------------------------------------
//	jWebSocket - JMSLoadBalancer (Community Edition, CE)
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
package org.jwebsocket.jms;

import java.net.InetAddress;
import java.util.Map;
import java.util.TimerTask;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.RemoveInfo;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.jms.api.INodesManager;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.Tools;

/**
 * jWebSocket cluster server node load balancer component. Redirect the requests
 * to the less loaded node in the cluster.
 *
 * @author Rolando Santamaria Maso
 */
public class JMSLoadBalancer implements IInitializable {

	private final String mServerDestination, mHostname;
	private final Session mClientsSession, mServerSession;
	private final INodesManager mNodesManager;
	private MessageConsumer mClientsMessagesConsumer;
	private MessageConsumer mClientsConnectionAdvisor;
	private MessageConsumer mNodesConnectionAdvisor;
	private MessageProducer mNodesMessagesProducer;
	private final static Logger mLog = Logging.getLogger();
	private final String mNodeId;

	/**
	 *
	 * @param aNodeId
	 * @param aDestination
	 * @param aClientSession
	 * @param aNodeSession
	 * @param aNodesManager
	 * @param aHostname
	 */
	public JMSLoadBalancer(String aNodeId, String aDestination, Session aClientSession,
			Session aNodeSession, INodesManager aNodesManager, String aHostname) {
		mServerDestination = aDestination;
		mClientsSession = aClientSession;
		mServerSession = aNodeSession;
		mNodesManager = aNodesManager;
		mNodeId = aNodeId;
		mHostname = aHostname;
	}

	@Override
	public void initialize() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.info("Initializing node load balancer instance...");
		}
		// initializing nodes manager
		mNodesManager.initialize();

		// clients topic
		final Topic lClientsTopic = mClientsSession.createTopic(mServerDestination);
		// nodes topic
		final Topic lNodesTopic = mClientsSession.createTopic(mServerDestination + "_nodes");

		mClientsMessagesConsumer = mClientsSession.createConsumer(lClientsTopic,
				Attributes.MESSAGE_TYPE + " IS NOT NULL" + " AND " + Attributes.MESSAGE_ID + " IS NOT NULL");
		mNodesMessagesProducer = mServerSession.createProducer(lNodesTopic);

		// client messages listener
		mClientsMessagesConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message aMessage) {
				try {
					String lMsgId = aMessage.getStringProperty(Attributes.MESSAGE_ID);
					String lReplySelector = aMessage.getStringProperty(Attributes.REPLY_SELECTOR);
					String lSessionId = aMessage.getStringProperty(Attributes.SESSION_ID);

					if (null == lMsgId || null == lReplySelector
							|| !mNodesManager.getSynchronizer().getWorkerTurn(lMsgId)) {
						// LB not turn to work
						return;
					}

					if (mLog.isDebugEnabled()) {
						mLog.info("Processing client message...");
					}

					// getting the message type property
					MessageType lType = MessageType.valueOf(aMessage.getStringProperty(Attributes.MESSAGE_TYPE));

					String lNodeId;
					if (!lType.equals(MessageType.ACK)) {
						// getting optimum node id
						lNodeId = mNodesManager.getOptimumNode();
						if (mLog.isDebugEnabled()) {
							mLog.info("Node '" + lNodeId + "' selected as optimum from (" + mNodesManager.count() + ") nodes...");
						}
					} else {
						// message is ack and the node id comes as property value
						lNodeId = (String) aMessage.getStringProperty(Attributes.NODE_ID);
					}
					if (null == lNodeId) {
						return;
					}
					switch (lType) {
						case CONNECTION: {
							if (mLog.isDebugEnabled()) {
								mLog.info("Processing message(CONNECTION) from client...");
							}
							Message lRequest = mClientsSession.createMessage();
							// the message type
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							// setting the worker node selected by the LB
							lRequest.setStringProperty(Attributes.NODE_ID, lNodeId);
							// reply selector value
							lRequest.setStringProperty(Attributes.REPLY_SELECTOR, lReplySelector);
							// sessionId value
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

							// getting the consumer info data
							Map<String, String> lConsumerData = mNodesManager.getConsumerAdviceTempStorage().getData(lReplySelector);
							lRequest.setStringProperty(Attributes.CONSUMER_ID, Tools.getMD5(lConsumerData.get(Attributes.CONSUMER_ID)));
							lRequest.setStringProperty(Attributes.CONNECTION_ID, lConsumerData.get(Attributes.CONNECTION_ID));

							mNodesMessagesProducer.send(lRequest);
							mNodesManager.increaseRequests(lNodeId);
							break;
						}
						case MESSAGE: {
							if (mLog.isDebugEnabled()) {
								mLog.info("Processing message(MESSAGE) from client...");
							}
							TextMessage lRequest = mClientsSession.createTextMessage(aMessage.getStringProperty(Attributes.DATA));
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							lRequest.setStringProperty(Attributes.REPLY_SELECTOR, lReplySelector);

							// redirecting message to optimum node
							lRequest.setStringProperty(Attributes.NODE_ID, lNodeId);
							mNodesMessagesProducer.send(lRequest);

							mNodesManager.increaseRequests(lNodeId);
							break;
						}
						case ACK: {
							if (mLog.isDebugEnabled()) {
								mLog.info("Processing message(ACK) from client...");
							}
							TextMessage lRequest = mClientsSession.createTextMessage(aMessage.getStringProperty(Attributes.DATA));
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.MESSAGE.name());
							lRequest.setStringProperty(Attributes.REPLY_SELECTOR, lReplySelector);

							// redirecting origin node
							lRequest.setStringProperty(Attributes.NODE_ID, lNodeId);
							mNodesMessagesProducer.send(lRequest);

							mNodesManager.increaseRequests(lNodeId);
							break;
						}
						case DISCONNECTION: {
							if (mLog.isDebugEnabled()) {
								mLog.info("Processing message(DISCONNECTION) from client...");
							}
							Message lRequest = mClientsSession.createMessage();
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							lRequest.setStringProperty(Attributes.REPLY_SELECTOR, lReplySelector);

							// redirecting message to optimum node
							lRequest.setStringProperty(Attributes.NODE_ID, lNodeId);
							mNodesMessagesProducer.send(lRequest);

							mNodesManager.increaseRequests(lNodeId);
							break;
						}
					}
				} catch (Exception ex) {
					mLog.error(Logging.getSimpleExceptionMessage(ex, "processing client message"));
				}
			}
		});

		mClientsConnectionAdvisor = mServerSession.createConsumer(AdvisorySupport.getConsumerAdvisoryTopic(lClientsTopic));
		mClientsConnectionAdvisor.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message aMessage) {
				try {
					ActiveMQMessage lMessage = (ActiveMQMessage) aMessage;
					Object lDataStructure = lMessage.getDataStructure();

					if (lDataStructure instanceof RemoveInfo) {
						RemoveInfo lCommand = (RemoveInfo) lDataStructure;
						DataStructure lDS = lCommand.getObjectId();

						if (lDS instanceof ConsumerId) {
							String lConsumerId = ((ConsumerId) lDS).toString();
							if (!mNodesManager.getSynchronizer().getWorkerTurn(lConsumerId + MessageType.DISCONNECTION.name())) {
								// LB not turn to work
								return;
							}

							String lNodeId = mNodesManager.getOptimumNode();
							Message lRequest = mClientsSession.createMessage();
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.DISCONNECTION.name());
							lRequest.setStringProperty(Attributes.CONSUMER_ID, Tools.getMD5(lConsumerId));

							// redirecting message to optimum node
							lRequest.setStringProperty(Attributes.NODE_ID, lNodeId);
							mNodesMessagesProducer.send(lRequest);
							// increasing node requests
							mNodesManager.increaseRequests(lNodeId);
						}
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "processing client connection events"));
				}
			}
		});

		// nodes connection listener
		mNodesConnectionAdvisor = mServerSession.createConsumer(AdvisorySupport.getConsumerAdvisoryTopic(lNodesTopic));
		mNodesConnectionAdvisor.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message aMessage) {
				try {
					ActiveMQMessage lMessage = (ActiveMQMessage) aMessage;
					Object lDataStructure = lMessage.getDataStructure();
					if (lDataStructure instanceof RemoveInfo) {
						RemoveInfo lCommand = (RemoveInfo) lDataStructure;
						DataStructure lDS = lCommand.getObjectId();
						if (lDS instanceof ConsumerId) {
							// getting the connection id
							String lConsumerId = String.valueOf(((ConsumerId) lDS).toString());
							if (!mNodesManager.getSynchronizer().getWorkerTurn(lConsumerId)) {
								// LB not turn to work
								return;
							}

							// setting the node status to offline
							String lNodeId = mNodesManager.getNodeId(lConsumerId);
							if (null != lNodeId) {
								mNodesManager.setStatus(lNodeId, NodeStatus.DOWN);
							}
						}
					}
				} catch (Exception ex) {
					mLog.error(Logging.getSimpleExceptionMessage(ex, "processing node connection event"));
				}
			}
		});

		register();
	}

	@Override
	public void shutdown() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.info("Stopping load balancer...");
		}

		// closing consumers and producers
		mClientsConnectionAdvisor.close();
		mClientsMessagesConsumer.close();
		mNodesConnectionAdvisor.close();
		mNodesMessagesProducer.close();

		// setting status to offline
		mNodesManager.setStatus(mNodeId, NodeStatus.DOWN);

		// shutting down nodes manager
		mNodesManager.shutdown();

		if (mLog.isDebugEnabled()) {
			mLog.info("Load balancer successfully stopped!");
		}
	}

	private void register() throws Exception {
		Map<String, String> lConsumerData = mNodesManager.getConsumerAdviceTempStorage().getData(mNodeId);
		// registering node

		mNodesManager.register(lConsumerData.get(Attributes.CONSUMER_ID),
				mNodeId, mNodesManager.getNodeDescription(),
				InetAddress.getByName(mHostname).getHostAddress(),
				Tools.getCpuUsage());

		// registering node CPU usage updater
		Tools.getTimer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Tools.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {
						try {
							mNodesManager.updateCPU(mNodeId, Tools.getCpuUsage());
						} catch (Exception lEx) {
							mLog.error(Logging.getSimpleExceptionMessage(lEx,
									"updating node '" + mNodeId + "' CPU usage"));
						}
					}
				});
			}
		}, 1500, 1500);

		if (mLog.isDebugEnabled()) {
			mLog.info("Load balancer successfully initialized!");
		}
	}
}
