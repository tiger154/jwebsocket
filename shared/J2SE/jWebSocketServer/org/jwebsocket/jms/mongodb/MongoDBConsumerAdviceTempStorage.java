/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author kyberneees
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

	public DBCollection getCollection() {
		return mCollection;
	}

	public void setCollection(DBCollection aCollection) {
		mCollection = aCollection;
	}

	@Override
	public void initialize() throws Exception {
		Assert.notNull(mCollection, "The 'collection' argument cannot be null!");

		mCollection.ensureIndex(new BasicDBObject()
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
