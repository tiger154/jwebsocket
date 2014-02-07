//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBConnectorsManager (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
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
package org.jwebsocket.jms.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.HashMap;
import java.util.Map;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.jms.BaseConnectorsManager;
import org.jwebsocket.jms.ConnectorStatus;
import org.jwebsocket.jms.JMSConnector;
import org.springframework.util.Assert;

/**
 * MongoDB based implementation of connectors manager component.
 *
 * @author kyberneees
 */
public class MongoDBConnectorsManager extends BaseConnectorsManager {

	private DBCollection mConnectors;

	/**
	 *
	 */
	public MongoDBConnectorsManager() {
	}

	@Override
	public JMSConnector add(String aConnectionId, String aConsumerId, String aReplySelector) throws Exception {
		Assert.notNull(aConnectionId);
		Assert.notNull(aReplySelector);

		if (null == mConnectors.findOne(new BasicDBObject().append(Attributes.CONSUMER_ID, aConsumerId))) {
			mConnectors.save(new BasicDBObject()
					.append(Attributes.CONNECTION_ID, aConnectionId)
					.append(Attributes.CONSUMER_ID, aConsumerId)
					.append(Attributes.REPLY_SELECTOR, aReplySelector)
					.append(Attributes.STATUS, ConnectorStatus.UP));
		}

		return get(aConnectionId);
	}

	@Override
	public boolean exists(String aReplySelector) throws Exception {
		return null != mConnectors.findOne(new BasicDBObject()
				.append(Attributes.STATUS, ConnectorStatus.UP)
				.append(Attributes.REPLY_SELECTOR, aReplySelector));
	}

	private JMSConnector toConnector(DBObject aRecord) throws Exception {
		String lReplySelector = (String) aRecord.get(Attributes.REPLY_SELECTOR);

		JMSConnector lConnector = new JMSConnector(getEngine(),
				lReplySelector,
				(String) aRecord.get(Attributes.CONNECTION_ID),
				(String) aRecord.get(Attributes.CONSUMER_ID));

		// setting the session storage
		IBasicStorage<String, Object> lSessionStorage = getSessionManager().getStorageProvider()
				.getStorage(lReplySelector);
		lConnector.getSession().setSessionId(lReplySelector);
		lConnector.getSession().setStorage(lSessionStorage);
		// using the session storage as connector custom vars container
		lConnector.setCustomVarsContainer(lSessionStorage);

		return lConnector;
	}

	@Override
	public JMSConnector get(String aReplySelector) throws Exception {
		if (!exists(aReplySelector)) {
			return null;
		}

		// getting the connector entry on database
		DBObject lRecord = mConnectors.findOne(new BasicDBObject().append(Attributes.REPLY_SELECTOR, aReplySelector));

		// creating connector from database entry
		return toConnector(lRecord);
	}

	@Override
	public void remove(String aConsumerId) throws Exception {
		mConnectors.remove(new BasicDBObject().append(Attributes.CONSUMER_ID, aConsumerId));
	}

	/**
	 *
	 * @param aCollection
	 */
	public void setCollection(DBCollection aCollection) {
		mConnectors = aCollection;
	}

	/**
	 *
	 * @return
	 */
	public DBCollection getCollection() {
		return mConnectors;
	}

	@Override
	public void setStatus(String aReplySelector, int aStatus) throws Exception {
		Assert.isTrue(exists(aReplySelector), "The given 'replySelector' was not found!");
		mConnectors.update(new BasicDBObject().append(Attributes.REPLY_SELECTOR, aReplySelector),
				new BasicDBObject()
				.append("$set", new BasicDBObject()
						.append(Attributes.STATUS, aStatus)));
	}

	@Override
	public Map<String, WebSocketConnector> getAll() throws Exception {
		DBCursor lCursor = mConnectors.find(new BasicDBObject().append(Attributes.STATUS, ConnectorStatus.UP));

		Map<String, WebSocketConnector> lConnectors = new HashMap<String, WebSocketConnector>();
		while (lCursor.hasNext()) {
			WebSocketConnector lConnector = toConnector(lCursor.next());
			lConnectors.put(lConnector.getId(), lConnector);
		}

		return lConnectors;
	}

	@Override
	public String getReplySelectorByConsumerId(String aConsumerId) throws Exception {
		DBCursor lCursor = mConnectors.find(new BasicDBObject().append(Attributes.CONSUMER_ID, aConsumerId));

		if (lCursor.hasNext()) {
			return lCursor.next().get(Attributes.REPLY_SELECTOR).toString();
		}
		return null;
	}

	@Override
	public void initialize() throws Exception {
		super.initialize();

		mConnectors.ensureIndex(new BasicDBObject().append(Attributes.CONSUMER_ID, 1));
		mConnectors.ensureIndex(new BasicDBObject().append(Attributes.REPLY_SELECTOR, 1),
				new BasicDBObject().append("unique", true));
	}
}
