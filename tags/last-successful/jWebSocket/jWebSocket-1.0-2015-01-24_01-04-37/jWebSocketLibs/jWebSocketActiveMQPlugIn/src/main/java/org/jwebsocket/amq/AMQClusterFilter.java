//	---------------------------------------------------------------------------
//	jWebSocket - AMQClusterFilter (Community Edition, CE)
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
package org.jwebsocket.amq;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.MessageId;
import org.apache.activemq.command.ProducerId;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.state.ProducerState;
import org.apache.activemq.util.IdGenerator;
import org.apache.activemq.util.LongSequenceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store consumer connection data into a MongoDB database collection to be used by the jWebSocket
 * Cluster Load Balancer component.
 *
 * @author Rolando Santamaria Maso
 */
public class AMQClusterFilter extends BrokerFilter {

	private final List<String> mTargetDestinations;
	private Mongo mMongo;
	private final String mUsername, mPassword;
	private static final Logger mLog = LoggerFactory.getLogger(AMQClusterFilter.class);
	private static final IdGenerator mIdGenerator = new IdGenerator();
	private final LongSequenceGenerator mMessageIdGenerator = new LongSequenceGenerator();
	private final ProducerId mProducerId = new ProducerId();

	/**
	 *
	 */
	public final static String COLLECTION_NAME = "consumerinfo_temp_storage";

	/**
	 *
	 */
	private Map<String, DBCollection> mCachedCollections;

	/**
	 *
	 * @param aBroker
	 * @param aTargetDestinations
	 * @param aMongo
	 * @param aUsername
	 * @param aPassword
	 */
	public AMQClusterFilter(Broker aBroker, List<String> aTargetDestinations, Mongo aMongo,
			String aUsername, String aPassword) {
		super(aBroker);
		mProducerId.setConnectionId(mIdGenerator.generateId());

		mLog.info("Instantiating jWebSocket AMQClusterFilter...");

		mCachedCollections = new FastMap<String, DBCollection>().shared();
		mTargetDestinations = aTargetDestinations;
		mUsername = aUsername;
		mPassword = aPassword;

		if (null == aMongo) {
			throw new RuntimeException("MongoDB connection can't be null!");
		}
		mMongo = aMongo;
	}

	/**
	 *
	 * @param aContext
	 * @param aInfo
	 * @return
	 * @throws Exception
	 */
	@Override
	public Subscription addConsumer(ConnectionContext aContext, ConsumerInfo aInfo) throws Exception {
		if (!aContext.isNetworkConnection()) {
			String lDest = aInfo.getDestination().getQualifiedName();

			String lSelector = aInfo.getSelector();
			if (null == lSelector) {
				// do not process
				return super.addConsumer(aContext, aInfo);
			}
			int lAPos = lSelector.indexOf("'");
			if (-1 == lAPos) {
				// do not process
				return super.addConsumer(aContext, aInfo);
			}

			int lBPos = lSelector.indexOf("'", lAPos + 1);

			// the correlation id identifies a consumer
			String lCorrelationId = lSelector.substring(lAPos + 1, lBPos);
			for (String lClusterDest : mTargetDestinations) {
				if (lClusterDest.matches(lDest)) {
					String lDatabaseName = aInfo.getDestination().getPhysicalName();
					if (lDatabaseName.endsWith("_nodes")) {
						// supporting server nodes
						// the database name match the C2S topic name
						lDatabaseName = lDatabaseName.substring(0, lDatabaseName.length() - 6);
					}
					if (!mCachedCollections.containsKey(lDatabaseName)) {
						// getting the mongo db collection
						DB lDatabase = mMongo.getDB(lDatabaseName);
						// authenticating if required
						if (mUsername != null && mPassword != null) {
							lDatabase.authenticate(mUsername, mPassword.toCharArray());
						}
						// caching collection instance
						mCachedCollections.put(lDatabaseName, lDatabase.getCollection(COLLECTION_NAME));
					}

					// getting cached collection instance
					DBCollection lCollection = mCachedCollections.get(lDatabaseName);
					DBObject lRecord = new BasicDBObject();
					lRecord.put("correlationId", lCorrelationId);
					lRecord.put("destination", lDest);
					lRecord.put("connectionId", aInfo.getConsumerId().getConnectionId());
					String lConsumerId = aInfo.getConsumerId().toString();
					lRecord.put("consumerId", lConsumerId);
					// saving record
					lCollection.update(new BasicDBObject()
							.append("correlationId", lCorrelationId),
							lRecord, true, false);
				}
			}
		}
		return super.addConsumer(aContext, aInfo);
	}

	@Override
	public void removeConsumer(ConnectionContext aContext, ConsumerInfo aInfo) throws Exception {
		if (!aContext.isNetworkConnection()) {
			String lDest = aInfo.getDestination().getPhysicalName();

			for (String lClusterDest : mTargetDestinations) {
				if (lClusterDest.matches("topic://" + lDest)) {
					ActiveMQMapMessage lMsg = new ActiveMQMapMessage();
					if (lDest.endsWith("_nodes")) {
						lMsg.setDestination(new ActiveMQTopic(lDest));
					} else {
						lMsg.setDestination(new ActiveMQTopic(lDest + "_nodes"));
					}

					lMsg.setJMSTimestamp(System.currentTimeMillis());
					String lConsumerId = aInfo.getConsumerId().toString();
					lMsg.setStringProperty("consumerId", lConsumerId);
					lMsg.setStringProperty("msgType", "BROKER_EVENT");
					lMsg.setOriginalTransactionId(null);
					lMsg.setStringProperty("name", "DISCONNECTION");
					lMsg.setStringProperty("destination", lDest);
					lMsg.setMessageId(new MessageId(mProducerId, mMessageIdGenerator.getNextSequenceId()));
					lMsg.setResponseRequired(false);
					lMsg.setPersistent(false);

					boolean lOriginalFlowControl = aContext.isProducerFlowControl();

					try {
						ProducerBrokerExchange lPBE = new ProducerBrokerExchange();
						lPBE.setMutable(true);
						lPBE.setProducerState(new ProducerState(new ProducerInfo()));
						lPBE.setConnectionContext(aContext);

						send(lPBE, lMsg);
					} catch (Exception lEx) {
					} finally {
						aContext.setProducerFlowControl(lOriginalFlowControl);
					}
				}
			}
		}

		super.removeConsumer(aContext, aInfo);
	}

}
