//  ---------------------------------------------------------------------------
//  jWebSocket - AMQClusterFilter (Community Edition, CE)
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
package org.jwebsocket.plugins.jms;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.util.List;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ConsumerInfo;

/**
 *
 * @author kyberneees
 */
public class AMQClusterFilter extends BrokerFilter {

	private List<String> mTargetDestinations;
	private Mongo mMongo;
	public final static String COLLECTION_NAME = "consumerinfo_temp_storage";

	public AMQClusterFilter(Broker aBroker, List<String> aTargetDestinations, Mongo aMongo) {
		super(aBroker);
		mTargetDestinations = aTargetDestinations;

		if (null == aMongo) {
			throw new RuntimeException("MongoDB connection can't be null!");
		}
		mMongo = aMongo;
	}

	@Override
	public Subscription addConsumer(ConnectionContext aContext, ConsumerInfo aInfo) throws Exception {
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
		if (null != lDest) {
			for (String lClusterDest : mTargetDestinations) {
				if (lClusterDest.matches(lDest)) {
					String lDatabaseName = aInfo.getDestination().getPhysicalName();
					if (lDatabaseName.endsWith("_nodes")) {
						// the database name match the C2S topic name
						lDatabaseName = lDatabaseName.substring(0, lDatabaseName.length() - 6);
					}

					// getting the mongo db collection
					DB lDatabase = mMongo.getDB(lDatabaseName);
					DBCollection lCollection = lDatabase.getCollection(COLLECTION_NAME);
					DBObject lRecord = new BasicDBObject();
					lRecord.put("correlationId", lCorrelationId);
					lRecord.put("destination", lDest);
					lRecord.put("connectionId", aInfo.getConsumerId().getConnectionId());
					lRecord.put("consumerId", aInfo.getConsumerId().toString());
					// saving record
					lCollection.update(new BasicDBObject()
							.append("correlationId", lCorrelationId),
							lRecord, true, false);
				}
			}
		}

		return super.addConsumer(aContext, aInfo);
	}
}
