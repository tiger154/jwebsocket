package org.jwebsocket.jms;

import java.net.Inet4Address;
import java.util.TimerTask;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQSession;
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
 *
 * @author kyberneees
 */
public class JMSLoadBalancer implements IInitializable {

	private final String mServerDestination;
	private final Session mSession;
	private final INodesManager mNodesManager;
	private MessageConsumer mClientsMessagesConsumer;
	private MessageConsumer mClientsConnectionAdvisor;
	private MessageConsumer mNodesConnectionAdvisor;
	private MessageProducer mNodesMessagesProducer;
	private Logger mLog = Logging.getLogger();
	private final String mNodeId;

	public JMSLoadBalancer(String aNodeId, String aDestination, Session aSession, INodesManager aNodesManager) {
		mServerDestination = aDestination;
		mSession = aSession;
		mNodesManager = aNodesManager;
		mNodeId = aNodeId;
	}

	@Override
	public void initialize() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.info("Initializing node load balancer instance...");
		}

		// clients queue
		Topic lClientsTopic = mSession.createTopic(mServerDestination);
		// nodes topic
		Topic lNodesTopic = mSession.createTopic(mServerDestination + "_nodes");

		mClientsMessagesConsumer = mSession.createConsumer(lClientsTopic, Attributes.MESSAGE_TYPE + " IS NOT NULL");
		mNodesMessagesProducer = mSession.createProducer(lNodesTopic);

		// client messages listener
		mClientsMessagesConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message aMessage) {
				try {
					String lMsgId = aMessage.getStringProperty(Attributes.MESSAGE_ID);
					if (null == lMsgId || !mNodesManager.getSynchronizer().getLoadBalancerTurn(lMsgId)) {
						// LB not turn to work
						return;
					}

					if (mLog.isDebugEnabled()) {
						mLog.info("Processing client message...");
					}

					ActiveMQMessage lMessage = (ActiveMQMessage) aMessage;
					// getting the message type property
					MessageType lType = MessageType.valueOf(aMessage.getStringProperty(Attributes.MESSAGE_TYPE));
					// generating the session id
					String lSessionId = String.valueOf(lMessage.getProducerId().getParentId());
					// prefixing the session id to avoid conflicts
					lSessionId = Tools.getMD5(mServerDestination + lSessionId);

					String lNodeId;
					if (!lType.equals(MessageType.ACK)) {
						// getting optimum node id
						lNodeId = mNodesManager.getOptimumNode();
						if (mLog.isDebugEnabled()) {
							mLog.info("Node '" + lNodeId + "' selected as optimum from (" + mNodesManager.count() + ") nodes...");
						}
					} else {
						// message is ack and the node id comes as property value
						lNodeId = (String) lMessage.getProperty(Attributes.NODE_ID);
					}
					if (null == lNodeId) {
						return;
					}
					switch (lType) {
						case CONNECTION: {
							if (mLog.isDebugEnabled()) {
								mLog.info("Processing message(CONNECTION) from client...");
							}
							// payload
							Message lRequest = mSession.createMessage();
							// type
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							// session id
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);
							// setting the worker node selected by the LB
							lRequest.setStringProperty(Attributes.NODE_ID, lNodeId);
							// reply selector key
							lRequest.setStringProperty(Attributes.REPLY_SELECTOR, lMessage.getStringProperty(Attributes.REPLY_SELECTOR));
							// setting the connection id
							lRequest.setStringProperty(Attributes.CONNECTION_ID, lMessage.getProducerId().getConnectionId());

							mNodesMessagesProducer.send(lRequest);
							mNodesManager.increaseRequests(lNodeId);
							break;
						}
						case MESSAGE: {
							if (mLog.isDebugEnabled()) {
								mLog.info("Processing message(MESSAGE) from client...");
							}
							TextMessage lRequest = mSession.createTextMessage(lMessage.getStringProperty(Attributes.DATA));
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

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
							TextMessage lRequest = mSession.createTextMessage(lMessage.getStringProperty(Attributes.DATA));
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.MESSAGE.name());
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

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
							Message lRequest = mSession.createMessage();
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

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

		// client connections listener 
		// @note: the algorithm works because each client require to generate a unique
		// reply destination per connection
		mClientsConnectionAdvisor = mSession.createConsumer(AdvisorySupport.getConsumerAdvisoryTopic(lClientsTopic));
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
							String lConnectionId = ((ConsumerId) lDS).getConnectionId();
							if (!mNodesManager.getSynchronizer().getLoadBalancerTurn(lConnectionId)) {
								// LB not turn to work
								return;
							}

							String lNodeId = mNodesManager.getOptimumNode();

							Message lRequest = mSession.createMessage();
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.DISCONNECTION.name());
							lRequest.setStringProperty(Attributes.CONNECTION_ID, lConnectionId);

							// redirecting message to optimum node
							lRequest.setStringProperty(Attributes.NODE_ID, lNodeId);
							mNodesMessagesProducer.send(lRequest);

							mNodesManager.increaseRequests(lNodeId);
						}
					}
				} catch (Exception ex) {
					mLog.error(Logging.getSimpleExceptionMessage(ex, "processing client connection events"));
				}
			}
		});

		// nodes connection listener
		mNodesConnectionAdvisor = mSession.createConsumer(AdvisorySupport.getConsumerAdvisoryTopic(lNodesTopic));
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
							String lConnectionId = String.valueOf(((ConsumerId) lDS).getConnectionId());
							if (!mNodesManager.getSynchronizer().getLoadBalancerTurn(lConnectionId)) {
								// LB not turn to work
								return;
							}

							// setting the node status to offline
							String lNodeId = mNodesManager.getNodeId(lConnectionId);
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

		if (mLog.isDebugEnabled()) {
			mLog.info("Load balancer successfully initialized!");
		}

		String lConnectionId = ((ActiveMQSession)mSession).getConnection().getConnectionInfo().getConnectionId().getValue();
		// registering node
		mNodesManager.register(lConnectionId, mNodeId, mNodesManager.getNodeDescription(),
				Inet4Address.getLocalHost().getHostAddress(),
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
}
