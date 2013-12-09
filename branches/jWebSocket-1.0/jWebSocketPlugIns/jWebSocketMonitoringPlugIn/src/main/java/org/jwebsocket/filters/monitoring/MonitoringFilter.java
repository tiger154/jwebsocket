//	---------------------------------------------------------------------------
//	jWebSocket - MonitoringFilter (Community Edition, CE)
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
package org.jwebsocket.filters.monitoring;

import org.jwebsocket.filter.TokenFilter;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.token.Token;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;

/**
 *
 * @author Merly
 */
public class MonitoringFilter extends TokenFilter {

	private static final Logger mLog = Logging.getLogger();
	private Mongo mConnection;
	private DBCollection mPluginCollection;

	/**
	 *
	 * @param aConfig
	 */
	public MonitoringFilter(FilterConfiguration aConfig) {

		super(aConfig);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating monitoring filter...");
		}

		try {
			mConnection = new MongoClient();
			mPluginCollection = mConnection.getDB("db_charting").getCollection("use_plugins");
		} catch (UnknownHostException ex) {
			mLog.error(ex.getMessage());
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	@Override
	public void processTokenIn(FilterResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		String lNamespace = aToken.getNS();
		if (null != lNamespace) {
			String lPlugInId;
			for (WebSocketPlugIn lPlugIn : getServer().getPlugInChain().getPlugIns()) {
				if (lNamespace.equals(lPlugIn.getNamespace())) {
					lPlugInId = lPlugIn.getId();

					//To save in the database
					DBObject lRecord = mPluginCollection.findOne(new BasicDBObject().append("id", lPlugInId));

					if (lRecord == null) {
						mPluginCollection.insert(new BasicDBObject().append("id", lPlugInId));
					} else {
						mPluginCollection.update(lRecord, new BasicDBObject().append("$inc", new BasicDBObject().append("requests", 1)));
					}
				}
			}
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	@Override
	public void processTokenOut(FilterResponse aResponse, WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {

		String lNamespace = aToken.getNS();
		if (null != lNamespace) {
			String lPlugInId;
			for (WebSocketPlugIn lPlugIn : getServer().getPlugInChain().getPlugIns()) {
				if (lNamespace.equals(lPlugIn.getPluginConfiguration().getNamespace())) {
					lPlugInId = lPlugIn.getId();

					//To save in the database
					DBObject lRecord = mPluginCollection.findOne(new BasicDBObject().append("id", lPlugInId));

					if (lRecord == null) {
						mPluginCollection.insert(new BasicDBObject().append("id", lPlugInId));
					} else {
						mPluginCollection.update(lRecord, new BasicDBObject().append("$inc", new BasicDBObject().append("requests", 1)));
					}
				}
			}
		}
	}
}
