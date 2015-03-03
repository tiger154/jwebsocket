//	---------------------------------------------------------------------------
//	jWebSocket - ServerRequestListener (Community Edition, CE)
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
package org.jwebsocket.plugins.monitoring;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServerListener;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author merlyta
 */
public class ServerRequestListener implements WebSocketServerListener {

	private Mongo mConnection;
	private DBCollection mColl;
	private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 */
	public ServerRequestListener() {
		try {
			// suppress stack traces from mongo db to console
			java.util.logging.Logger.getLogger("com.mongodb").setLevel(
					java.util.logging.Level.OFF);
			mConnection = new MongoClient();
			DB lDB = mConnection.getDB("db_charting");
			List<String> lDBNames = mConnection.getDatabaseNames();
			if (mLog.isInfoEnabled()) {
				mLog.info("Found databases: " + lDBNames.toString() + ".");
			}
			String lVersion = mConnection.getVersion();
			mColl = lDB.getCollection("exchanges_server");

			if (mLog.isInfoEnabled()) {
				mLog.info("Instantiated server request listener for MongoDB " + lVersion + ".");
			}

		} catch (UnknownHostException ex) {
			mLog.error(ex.getMessage());

		}
	}

	@Override
	public void processPacket(WebSocketServerEvent wsse, WebSocketPacket wsp) {
		SimpleDateFormat lFormat = new SimpleDateFormat("MM/dd/yyyy");
		String lToday = lFormat.format(new Date());

		try {
			// TODO: check this error handling!
			if (null == mColl) {
				// mLog.error("Mongo DB collection not accessible.");
				return;
			}

			DBObject lRecord = mColl.findOne(new BasicDBObject().append("date", lToday));
			if (null == lRecord) {
				mColl.insert(new BasicDBObject().append("date", lToday));
				lRecord = mColl.findOne(new BasicDBObject().append("date", lToday));
			}
			mColl.update(lRecord, new BasicDBObject().append("$inc", new BasicDBObject().append("h" + String.valueOf(new Date().getHours()), 1)));
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "Instantiating ServerRequestListener"));
		}
	}

	@Override
	public void processOpened(WebSocketServerEvent aEvent) {
	}

	@Override
	public void processClosed(WebSocketServerEvent aEvent) {
	}
}
