//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBClusterSynchronizer (Community Edition, CE)
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
import com.mongodb.WriteConcern;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.jms.api.IClusterSynchronizer;

/**
 * Implementation based on MongoDB primary key duplication restriction exception
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBClusterSynchronizer implements IClusterSynchronizer {

	private DBCollection mCollection;

	/**
	 *
	 * @param aCollection
	 */
	public void setCollection(DBCollection aCollection) {
		this.mCollection = aCollection;
	}

	/**
	 *
	 * @return
	 */
	public DBCollection getCollection() {
		return mCollection;
	}

	@Override
	public boolean getWorkerTurn(String aMessageId) {
		try {
			mCollection.insert(new BasicDBObject().append(Attributes.MESSAGE_ID, aMessageId), WriteConcern.SAFE);
			return true;
		} catch (Exception lEx) {
			return false;
		}
	}

	@Override
	public void initialize() throws Exception {
		mCollection.createIndex(new BasicDBObject().append(Attributes.MESSAGE_ID, 1),
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
