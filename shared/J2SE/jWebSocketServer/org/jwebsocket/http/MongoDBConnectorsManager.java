// ---------------------------------------------------------------------------
// jWebSocket - RESTConnectorsManager (Community Edition, CE)
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
package org.jwebsocket.http;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.IConnectorsPacketQueue;
import org.jwebsocket.api.ISessionManager;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBConnectorsManager implements IConnectorsManager {

	private DBCollection mConnectors;
	private ISessionManager mSessionManager;
	private WebSocketEngine mEngine;
	private IConnectorsPacketQueue mPacketsQueue;

	@Override
	public ISessionManager getSessionManager() {
		return mSessionManager;
	}

	@Override
	public IConnectorsPacketQueue getPacketsQueue() {
		return mPacketsQueue;
	}

	@Override
	public void setPacketsQueue(IConnectorsPacketQueue aPacketsQueue) {
		mPacketsQueue = aPacketsQueue;
	}

	@Override
	public WebSocketEngine getEngine() {
		return mEngine;
	}

	@Override
	public void setEngine(WebSocketEngine aEngine) {
		mEngine = aEngine;
	}

	/**
	 * Set the connectors collection
	 *
	 * @param aCollection
	 */
	public void setCollection(DBCollection aCollection) {
		mConnectors = aCollection;
	}

	/**
	 * Get the connectors collection
	 *
	 * @return
	 */
	public DBCollection getCollection() {
		return mConnectors;
	}

	@Override
	public void initialize() throws Exception {
		SystemPlugIn lPlugIn = (SystemPlugIn) JWebSocketFactory.getTokenServer()
				.getPlugInById("jws.system");
		mSessionManager = lPlugIn.getSessionManager();
		Assert.notNull(mSessionManager, "The system plug-in 'sessionManager' is not properly configured!");

		mConnectors.createIndex(new BasicDBObject().append(Attributes.CONNECTION_ID, 1),
				new BasicDBObject().append("unique", true));
		mConnectors.createIndex(new BasicDBObject().append(Attributes.SESSION_ID, 1));
	}

	@Override
	public void shutdown() throws Exception {
		mPacketsQueue.shutdown();
	}

	@Override
	public Long count() {
		return mConnectors.count();
	}

	@Override
	public boolean sessionExists(String aSessionId) {
		return null != mConnectors.findOne(new BasicDBObject()
				.append(Attributes.SESSION_ID, aSessionId));
	}

	@Override
	public boolean connectorExists(String aConnectorId) {
		return null != mConnectors.findOne(new BasicDBObject()
				.append(Attributes.CONNECTION_ID, aConnectorId));
	}

	@Override
	public HTTPConnector getConnectorById(String aConnectorId) throws Exception {
		return getConnectorById(aConnectorId, false);
	}

	@Override
	public HTTPConnector getConnectorById(String aConnectorId, boolean aStartupConnection) throws Exception {
		DBObject lConnector = mConnectors.findOne(new BasicDBObject()
				.append(Attributes.CONNECTION_ID, aConnectorId));

		return (null == lConnector) ? null : toConnector(lConnector, aStartupConnection);
	}

	@Override
	public HTTPConnector getConnectorBySessionId(String aSessionId) throws Exception {
		DBObject lConnector = mConnectors.findOne(new BasicDBObject()
				.append(Attributes.SESSION_ID, aSessionId));

		return (null == lConnector) ? null : toConnector(lConnector, false);
	}

	private HTTPConnector toConnector(DBObject aRecord, boolean aStartupConnection) throws Exception {
		String lConnectorId = (String) aRecord.get(Attributes.CONNECTION_ID);

		HTTPConnector lConnector = new HTTPConnector(mEngine, lConnectorId, mPacketsQueue);

		String lSessionId = (String) aRecord.get(Attributes.SESSION_ID);
		lConnector.getSession().setSessionId(lSessionId);
		IBasicStorage<String, Object> lSessionStorage;
		if (!aStartupConnection) {
			lSessionStorage = mSessionManager.getStorageProvider().getStorage(lSessionId);
		} else {
			lSessionStorage = mSessionManager.getSession(lSessionId);
		}
		lConnector.getSession().setStorage(lSessionStorage);

		// using the session storage as connector custom vars container
		lConnector.setCustomVarsContainer(lSessionStorage);

		return lConnector;
	}

	@Override
	public void remove(String aConnectorId) throws Exception {
		mConnectors.remove(new BasicDBObject().append(Attributes.CONNECTION_ID, aConnectorId), WriteConcern.SAFE);
	}

	@Override
	public HTTPConnector add(String aSessionId, String aConnectionId) throws Exception {
		Assert.notNull(aConnectionId);
		Assert.notNull(aSessionId);

		if (null == mConnectors.findOne(new BasicDBObject().append(Attributes.CONNECTION_ID, aConnectionId))) {
			mConnectors.save(new BasicDBObject()
					.append(Attributes.CONNECTION_ID, aConnectionId)
					.append(Attributes.SESSION_ID, aSessionId),
					WriteConcern.SAFE);
		}

		return getConnectorById(aConnectionId, true);
	}

	@Override
	public Map<String, WebSocketConnector> getAll() throws Exception {
		DBCursor lCursor = mConnectors.find();

		Map<String, WebSocketConnector> lConnectors = new HashMap<String, WebSocketConnector>();
		while (lCursor.hasNext()) {
			WebSocketConnector lConnector = toConnector(lCursor.next(), false);
			lConnectors.put(lConnector.getId(), lConnector);
		}

		return lConnectors;
	}

	@Override
	public Map<String, WebSocketConnector> getSharedSession(String aSessionId) throws Exception {
		DBCursor lCursor = mConnectors.find(new BasicDBObject()
				.append(Attributes.SESSION_ID, aSessionId));

		Map<String, WebSocketConnector> lConnectors = new HashMap<String, WebSocketConnector>();
		while (lCursor.hasNext()) {
			WebSocketConnector lConnector = toConnector(lCursor.next(), false);
			lConnectors.put(lConnector.getId(), lConnector);
		}

		return lConnectors;
	}

	@Override
	public Iterator<WebSocketConnector> getIterator() {
		final DBCursor lCursor = mConnectors.find();
		return new Iterator<WebSocketConnector>() {

			@Override
			public boolean hasNext() {
				return lCursor.hasNext();
			}

			@Override
			public WebSocketConnector next() {
				try {
					return toConnector(lCursor.next(), false);
				} catch (Exception lEx) {
					throw new RuntimeException(lEx);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove");
			}
		};
	}
}
