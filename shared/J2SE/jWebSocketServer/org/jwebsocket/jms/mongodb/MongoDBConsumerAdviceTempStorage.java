//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBConsumerAdviceTempStorage (Community Edition, CE)
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
package org.jwebsocket.jms.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.Map;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.jms.api.IConsumerAdviceTempStorage;
import org.springframework.util.Assert;

/**
 * MongoDB based implementation for IConsumerAdviceTempStorage interface.
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBConsumerAdviceTempStorage implements IConsumerAdviceTempStorage, IInitializable {

	private DBCollection mCollection;

	@Override
	public String getConsumerId(String aCorrelationId) throws Exception {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append(Attributes.CORRELATION_ID, aCorrelationId));
		if (null == lRecord) {
			return null;
		}
		mCollection.remove(lRecord);

		return lRecord.get(Attributes.CONSUMER_ID).toString();
	}

	@Override
	public Map<String, String> getData(String aCorrelationId) throws Exception {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append(Attributes.CORRELATION_ID, aCorrelationId));
		if (null == lRecord) {
			return null;
		}
		mCollection.remove(lRecord);

		return lRecord.toMap();
	}

	/**
	 *
	 * @return
	 */
	public DBCollection getCollection() {
		return mCollection;
	}

	/**
	 *
	 * @param aCollection
	 */
	public void setCollection(DBCollection aCollection) {
		mCollection = aCollection;
	}

	@Override
	public void initialize() throws Exception {
		Assert.notNull(mCollection, "The 'collection' argument cannot be null!");

		mCollection.createIndex(new BasicDBObject()
				.append(Attributes.CORRELATION_ID, 1)
				.append(Attributes.CONSUMER_ID, 1),
				new BasicDBObject().append("unique", true));

		if (!mCollection.isCapped()) {
			// converting collection to capped (limiting collection size)
			mCollection.getDB().command(new BasicDBObject()
					.append("convertToCapped", mCollection.getName())
					.append("max", 1000000));
		}
	}

	@Override
	public void shutdown() throws Exception {
	}
}
