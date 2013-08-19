//	---------------------------------------------------------------------------
//	jWebSocket - ConnectorsManager (Community Edition, CE)
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
package org.jwebsocket.jms.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import javax.jms.MessageProducer;
import org.jwebsocket.api.ISessionManager;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.jms.ConnectorStatus;
import org.jwebsocket.jms.JMSConnector;
import org.jwebsocket.jms.JMSEngine;
import org.jwebsocket.jms.api.IConnectorsManager;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.springframework.util.Assert;

/**
 * Database based connectors manager.
 *
 * @author kyberneees
 */
public class MongoDBConnectorsManager implements IConnectorsManager {

	private DBCollection mCollection;
	private ISessionManager mSessionManager;
	private MessageProducer mReplyProducer;
	private JMSEngine mEngine;

	public MongoDBConnectorsManager() {
	}

	@Override
	public void setReplyProducer(MessageProducer aReplyProducer) {
		mReplyProducer = aReplyProducer;
	}

	@Override
	public void setEngine(JMSEngine aEngine) {
		mEngine = aEngine;
	}

	@Override
	public WebSocketConnector addConnector(String aSessionId, String aIpAddress, String aReplyDestination) throws Exception {
		if (!sessionExists(aSessionId)) {
			mCollection.save(new BasicDBObject()
					.append(Attributes.IP_ADDRESS, aIpAddress)
					.append(Attributes.SESSION_ID, aSessionId)
					.append(Attributes.REPLY_DESTINATION, aReplyDestination)
					.append(Attributes.STATUS, ConnectorStatus.ONLINE));
		} else {
			mCollection.update(new BasicDBObject().append(Attributes.SESSION_ID, aSessionId),
					new BasicDBObject()
					.append("$set", new BasicDBObject()
					.append(Attributes.STATUS, ConnectorStatus.ONLINE)
					.append(Attributes.REPLY_DESTINATION, aReplyDestination)));
		}
		return getConnector(aSessionId);
	}

	@Override
	public boolean sessionExists(String aSessionId) throws Exception {
		return null != mCollection.findOne(new BasicDBObject()
				.append(Attributes.STATUS, ConnectorStatus.ONLINE)
				.append(Attributes.SESSION_ID, aSessionId));
	}

	@Override
	public WebSocketConnector getConnector(String aSessionId) throws Exception {
		if (sessionExists(aSessionId)) {
			return null;
		}

		DBObject lRecord = mCollection.findOne(new BasicDBObject().append(Attributes.SESSION_ID, aSessionId));
		JMSConnector lConnector = new JMSConnector(mEngine, mReplyProducer,
				(String) lRecord.get(Attributes.REPLY_DESTINATION),
				(String) lRecord.get(Attributes.IP_ADDRESS),
				(String) lRecord.get(Attributes.SESSION_ID));

		lConnector.getSession().setSessionId(aSessionId);
		lConnector.getSession().setStorage(mSessionManager.getSession(aSessionId));

		return lConnector;
	}

	@Override
	public void removeConnector(String aSessionId) throws Exception {
		mCollection.remove(new BasicDBObject().append(Attributes.SESSION_ID, aSessionId));
		mSessionManager.getSession(aSessionId).clear();
	}

	public void setCollection(DBCollection aCollection) {
		mCollection = aCollection;
	}

	public DBCollection getCollection() {
		return mCollection;
	}

	@Override
	public void setStatus(String aSessionId, int aStatus) throws Exception {
		Assert.isTrue(sessionExists(aSessionId), "The given session was not found!");
		mCollection.update(new BasicDBObject().append(Attributes.SESSION_ID, aSessionId),
				new BasicDBObject()
				.append("$set", new BasicDBObject()
				.append(Attributes.STATUS, aStatus)));
	}

	@Override
	public void initialize() throws Exception {
		SystemPlugIn lPlugIn = (SystemPlugIn) JWebSocketFactory.getTokenServer()
				.getPlugInById("jws.system");
		mSessionManager = lPlugIn.getSessionManager();

		Assert.notNull(mSessionManager, "The system plug-in 'sessionManager' is not properly configured!");
		Assert.notNull(mEngine, "The 'engine' reference cannot be null!");
		Assert.notNull(mReplyProducer, "The 'reply-producer' reference cannot be null!");

		mCollection.ensureIndex(new BasicDBObject().append(Attributes.SESSION_ID, 1),
				new BasicDBObject().append("unique", true));
	}

	@Override
	public void shutdown() throws Exception {
		mReplyProducer.close();
	}
}
