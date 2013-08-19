package org.jwebsocket.jms;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQTopic;
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

	private final String mDestination;
	private final Session mSession;
	private final INodesManager mNodesManager;
	private MessageConsumer mClientsMessagesConsumer;
	private MessageConsumer mClientsConnectionAdvisor;
	private MessageConsumer mNodesConnectionAdvisor;
	private MessageProducer mNodesMessagesProducer;
	private Logger mLog = Logging.getLogger();
	private final String mNodeId;

	public JMSLoadBalancer(String aNodeId, String aDestination, Session aSession, INodesManager aNodesManager) {
		mDestination = aDestination;
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
		Queue lClientsQueue = mSession.createQueue(mDestination);
		// nodes topic
		Topic lNodesTopic = mSession.createTopic(mDestination);

		mClientsMessagesConsumer = mSession.createConsumer(lClientsQueue);
		mNodesMessagesProducer = mSession.createProducer(lNodesTopic);

		// client messages listener
		mClientsMessagesConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message aMessage) {
				try {
					ActiveMQTextMessage lMessage = (ActiveMQTextMessage) aMessage;
					MessageType lType = MessageType.valueOf(aMessage.getStringProperty(Attributes.MESSAGE_TYPE));
					String lSessionId = String.valueOf(lMessage.getProducerId().getSessionId());
					// prefixing the session id to avoid conflicts if the session id is re-used 
					lSessionId = Tools.getMD5(mDestination + lSessionId);

					// getting optimum node id
					String lNodeId = mNodesManager.getOptimumNode();

					if (null == lNodeId) {
						return;
					}

					switch (lType) {
						case CONNECTION: {
							// payload
							Message lRequest = mSession.createMessage();
							// type
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							// ip address
							lRequest.setStringProperty(Attributes.IP_ADDRESS, lMessage.getConnection().getConnectionInfo().getClientIp());
							// session id
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);
							// response queue
							lRequest.setJMSReplyTo(lMessage.getJMSReplyTo());

							// redirecting message to optimum node
							lRequest.setStringProperty(Attributes.NODE, lNodeId);
							mNodesMessagesProducer.send(lRequest);

							mNodesManager.increaseRequests(lNodeId);
						}
						case MESSAGE: {
							TextMessage lRequest = mSession.createTextMessage(lMessage.getText());
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

							// redirecting message to optimum node
							lRequest.setStringProperty(Attributes.NODE, lNodeId);
							mNodesMessagesProducer.send(lRequest);

							mNodesManager.increaseRequests(lNodeId);
						}
						case DISCONNECTION: {
							Message lRequest = mSession.createMessage();
							lRequest.setStringProperty(Attributes.MESSAGE_TYPE, lType.name());
							lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

							// redirecting message to optimum node
							lRequest.setStringProperty(Attributes.NODE, lNodeId);
							mNodesMessagesProducer.send(lRequest);

							mNodesManager.increaseRequests(lNodeId);
						}
					}
				} catch (Exception ex) {
					mLog.error(Logging.getSimpleExceptionMessage(ex, "processing client message"));
				}
			}
		});

		// client connections listener 
		ActiveMQTopic lAdvisoryClientsQueue = AdvisorySupport.getProducerAdvisoryTopic(lClientsQueue);
		mClientsConnectionAdvisor = mSession.createConsumer(lAdvisoryClientsQueue);
		mClientsConnectionAdvisor.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message aMessage) {
				try {
					ActiveMQMessage lMessage = (ActiveMQMessage) aMessage;
					Object lDataStructure = lMessage.getDataStructure();
					if (lDataStructure instanceof RemoveInfo) {
						RemoveInfo lCommand = (RemoveInfo) lDataStructure;
						DataStructure lDS = lCommand.getObjectId();

						// getting optimum node id
						String lNodeId = mNodesManager.getOptimumNode();

						if (lCommand.isProducerRemove()) {
							if (lDS instanceof ConsumerId) {
								// getting the session id
								String lSessionId = String.valueOf(((ConsumerId) lDS).getSessionId());
								lSessionId = Tools.getMD5(mDestination + lSessionId);

								Message lRequest = mSession.createMessage();
								lRequest.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.DISCONNECTION.name());
								lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

								// redirecting message to optimum node
								lRequest.setStringProperty(Attributes.NODE, lNodeId);
								mNodesMessagesProducer.send(lRequest);

								mNodesManager.increaseRequests(lNodeId);
							}
						} else if (lCommand.isSessionRemove()) {
							if (lDS instanceof ConsumerId) {
								// getting the session id
								String lSessionId = String.valueOf(((ConsumerId) lDS).getSessionId());
								lSessionId = Tools.getMD5(mDestination + lSessionId);

								Message lRequest = mSession.createMessage();
								lRequest.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.SESSION_STOPPED.name());
								lRequest.setStringProperty(Attributes.SESSION_ID, lSessionId);

								// redirecting message to optimum node
								lRequest.setStringProperty(Attributes.NODE, lNodeId);
								mNodesMessagesProducer.send(lRequest);

								mNodesManager.increaseRequests(lNodeId);
							}
						}
					}
				} catch (Exception ex) {
					mLog.error(Logging.getSimpleExceptionMessage(ex, "processing client connection event"));
				}
			}
		});

		// nodes connection listener
		ActiveMQTopic lAdvisoryNodesTopic = AdvisorySupport.getProducerAdvisoryTopic(lNodesTopic);
		mNodesConnectionAdvisor = mSession.createConsumer(lAdvisoryNodesTopic);
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
							// getting the session id
							String lSessionId = String.valueOf(((ConsumerId) lDS).getSessionId());

							// setting the node status to offline
							mNodesManager.setStatus(mNodesManager.getNodeId(lSessionId), NodeStatus.OFFLINE);
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
		mNodesManager.setStatus(mNodeId, NodeStatus.OFFLINE);
		mNodesManager.shutdown();

		if (mLog.isDebugEnabled()) {
			mLog.info("Load balancer successfully stopped!");
		}
	}
}
