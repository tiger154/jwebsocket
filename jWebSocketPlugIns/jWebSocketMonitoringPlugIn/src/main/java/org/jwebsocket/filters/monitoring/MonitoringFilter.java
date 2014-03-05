//	---------------------------------------------------------------------------
//	jWebSocket - MonitoringFilter (Community Edition, CE)
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
package org.jwebsocket.filters.monitoring;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.filter.TokenFilter;
import static org.jwebsocket.filters.monitoring.MonitoringFilter.mCurrentHour;
import static org.jwebsocket.filters.monitoring.MonitoringFilter.mIsMemoryDataToMongoDBRunning;
import static org.jwebsocket.filters.monitoring.MonitoringFilter.mIsUpdatePlugInsRunning;
import static org.jwebsocket.filters.monitoring.MonitoringFilter.mPlugInsMemoryStorage;
import static org.jwebsocket.filters.monitoring.MonitoringFilter.mThreadMemoryDataToMongoDB;
import static org.jwebsocket.filters.monitoring.MonitoringFilter.mThreadUpdatePlugIns;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.logging.Logging;
import static org.jwebsocket.plugins.monitoring.MonitoringPlugIn.NS_MONITORING;
import org.jwebsocket.plugins.monitoring.util.PlugInObjectInMemory;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.storage.memory.MemoryStorage;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.ConnectionManager;

/**
 *
 * @author Merly Lopez Barroso, Victor Antonio Barzana Crespo
 */
public class MonitoringFilter extends TokenFilter {

	private static final Logger mLog = Logging.getLogger();
	private Mongo mDBConnection;
	private static DB mDB;
	private DBCollection mPluginCollection;
	private DBCollection mExchangesCollection;
	private final static String DB_NAME = "db_charting";
	private final static String DB_COL_PLUGINS_USAGE = "use_plugins";
	private final static String DB_COL_EXCHANGES_USAGE = "exchanges_server";

	/**
	 *
	 */
	public static MemoryStorage<String, PlugInObjectInMemory> mPlugInsMemoryStorage;
	private final static String TT_IN = "in";
	private final static String TT_OUT = "out";
	private static final String TT_REQUESTS = "requests";
	private final static Integer TIME_TO_SAVE = 800; // Miliseconds required
	private final static Integer TIME_TO_UPDATE_PLUGINS = 1000; // Seconds required

	/**
	 *
	 */
	public static Thread mThreadMemoryDataToMongoDB;

	/**
	 *
	 */
	public static boolean mIsMemoryDataToMongoDBRunning = true;

	/**
	 *
	 */
	public static Thread mThreadUpdatePlugIns;

	/**
	 *
	 */
	public static boolean mIsUpdatePlugInsRunning = true;

	/**
	 *
	 */
	public static String mCurrentHour = "h" + String.valueOf(Calendar.getInstance().get(Calendar.HOUR));
	private static final SimpleDateFormat mFormat = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 *
	 */
	public PlugInObjectInMemory mCurrentPlugIn;

	/**
	 *
	 * @param aConfig
	 * @throws java.lang.Exception
	 */
	public MonitoringFilter(FilterConfiguration aConfig) throws Exception {
		super(aConfig);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Monitoring Filter...");
		}
		String lMongoVersion;
		// suppress stack traces from mongo db to console
		java.util.logging.Logger.getLogger("com.mongodb").setLevel(
				java.util.logging.Level.OFF);

		ConnectionManager lCM = (ConnectionManager) JWebSocketBeanFactory.getInstance()
				.getBean(JWebSocketServerConstants.CONNECTION_MANAGER_BEAN_ID);
		if (lCM.isValid(NS_MONITORING)) {
			mDBConnection = (Mongo) lCM.getConnection(NS_MONITORING);
			mDB = mDBConnection.getDB(DB_NAME);
			// As we have to count every incoming token for each PlugIn I 
			// consider much better to create also a memory storage for the PlugIns
			// so, on each incoming token we count in a map object each PlugIn and 
			// this information will be written to MongoDB every TIME_TO_SAVE ms
			mPlugInsMemoryStorage = new MemoryStorage<String, PlugInObjectInMemory>(DB_COL_PLUGINS_USAGE);
			mPlugInsMemoryStorage.initialize();

			List<String> lDBNames = mDBConnection.getDatabaseNames();
			if (mLog.isInfoEnabled()) {
				mLog.info("Found databases: " + lDBNames.toString() + ".");
			}
			lMongoVersion = mDBConnection.getVersion();

			mPluginCollection = mDB.getCollection(DB_COL_PLUGINS_USAGE);
			mExchangesCollection = mDB.getCollection(DB_COL_EXCHANGES_USAGE);

			if (mLog.isInfoEnabled()) {
				mLog.info("Instantiated Monitoring Filter with MongoDB version: " + lMongoVersion + "!");
			}
		} else {
			mLog.error("Missing required valid database connection. Monitoring filter cannot start!");
			throw new RuntimeException("Missing required valid database connection for MonitoringFilter!");
		}
	}

	@Override
	public void systemStarted() {
		mIsUpdatePlugInsRunning = true;
		mThreadUpdatePlugIns = new Thread(new MonitoringFilter.UpdatePlugIns(), "jWebSocket Monitoring Plug-in Thread to Update PlugIns");
		mThreadUpdatePlugIns.start();

		mIsMemoryDataToMongoDBRunning = true;
		mThreadMemoryDataToMongoDB = new Thread(new MonitoringFilter.MemoryDataToMongoDB(),
				"jWebSocket Monitoring Plug-in MongoDB Performance Thread");
		mThreadMemoryDataToMongoDB.start();
	}

	@Override
	public void systemStopped() {
		try {
			mIsMemoryDataToMongoDBRunning = false;
			mThreadMemoryDataToMongoDB.join(2000);
			mThreadMemoryDataToMongoDB.stop();

			mIsUpdatePlugInsRunning = false;
			mThreadUpdatePlugIns.join(2000);
			mThreadUpdatePlugIns.stop();

		} catch (InterruptedException ex) {
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
		// counting the incoming token
		if (null != aToken.getNS()) {
			incrementIncoming(aToken.getNS());
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
	public void processTokenOut(FilterResponse aResponse, WebSocketConnector aSource,
			WebSocketConnector aTarget, Token aToken) {
		// Counting the outgoing token
		if (null != aToken.getNS()) {
			incrementOutgoing(aToken.getNS());
		}
	}

	private void incrementIncoming(String aNamespace) {
		if (mCurrentPlugIn == null || !mCurrentPlugIn.getNamespace().equals(aNamespace)) {
			mCurrentPlugIn = mPlugInsMemoryStorage.get(aNamespace);
		}
		if (mCurrentPlugIn != null) {
			mCurrentPlugIn.incrementIncoming();
		}
	}

	private void incrementOutgoing(String aNamespace) {
		if (mCurrentPlugIn == null || !mCurrentPlugIn.getNamespace().equals(aNamespace)) {
			mCurrentPlugIn = mPlugInsMemoryStorage.get(aNamespace);
		}
		if (mCurrentPlugIn != null) {
			mCurrentPlugIn.incrementOutgoing();
		}
	}

	class UpdatePlugIns implements Runnable {

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			while (mIsUpdatePlugInsRunning) {
				try {
					updatePlugIns();
					Thread.sleep(TIME_TO_UPDATE_PLUGINS);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void updatePlugIns() {
		try {
			String lPlugInId, lNamespace;
			PlugInObjectInMemory lPlugInItem = null;
			for (WebSocketPlugIn lPlugIn : getServer().getPlugInChain().getPlugIns()) {
				lNamespace = lPlugIn.getNamespace();
				lPlugInId = lPlugIn.getId();
				if (!mPlugInsMemoryStorage.isEmpty()) {
					lPlugInItem = mPlugInsMemoryStorage.get(lNamespace);
				}
				if (null == lPlugInItem) {
					mPlugInsMemoryStorage.put(lNamespace, new PlugInObjectInMemory(lPlugInId, lNamespace));
				} else {
					String lId = lPlugInItem.getPlugInId();
					if (!lId.equals(lPlugInId)) {
						lPlugInItem.setPlugInId(lPlugInId);
					}
				}
			}
			String lNow = "h" + String.valueOf(Calendar.getInstance().get(Calendar.HOUR));
			if (!lNow.equals(mCurrentHour)) {
				mCurrentHour = lNow;
				for (Map.Entry<String, PlugInObjectInMemory> lEntry : mPlugInsMemoryStorage.entrySet()) {
					lEntry.getValue().setOutgoing(0);
					lEntry.getValue().setmIncoming(0);
				}
			}
		} catch (Exception aEx) {
			mLog.error(Logging.getSimpleExceptionMessage(aEx, "updatePlugIns"));
		}
	}

	class MemoryDataToMongoDB implements Runnable {

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			while (mIsMemoryDataToMongoDBRunning) {
				try {
					Thread.sleep(TIME_TO_SAVE);
					writePlugInsMemoryToMongoDB();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void writePlugInsMemoryToMongoDB() {
		String lToday = mFormat.format(new Date());
		long lIncomingRequests, lIncoming = 0, lOutgoing = 0, lOutgoingRequests;
		try {
			// WRITING THE PLUGINS INTO THE DATABASE
			String lPlugInId;
			for (Map.Entry<String, PlugInObjectInMemory> lPlugIns : mPlugInsMemoryStorage.entrySet()) {
				PlugInObjectInMemory lPlugInMemory = lPlugIns.getValue();
				lIncomingRequests = lPlugInMemory.getIncoming();
				lOutgoingRequests = lPlugInMemory.getOutgoing();
				if (lIncomingRequests > 0 || lOutgoingRequests > 0) {
					lPlugInId = lPlugInMemory.getPlugInId();
					//To save in the database
					DBObject lRecord = mPluginCollection.findOne(new BasicDBObject()
							.append("id", lPlugInId));
					if (lRecord == null) {
						mPluginCollection.insert(new BasicDBObject().append("id",
								lPlugInId).append(TT_REQUESTS, lIncomingRequests + lOutgoingRequests));
					} else {
						mPluginCollection.update(lRecord, new BasicDBObject().append("$inc",
								new BasicDBObject().append(TT_REQUESTS, lIncomingRequests + lOutgoingRequests)));
					}
					lIncoming += lIncomingRequests;
					lOutgoing += lOutgoingRequests;
					lPlugInMemory.setOutgoing(0);
					lPlugInMemory.setmIncoming(0);
				}
			}
			DBObject lQuery = new BasicDBObject().append("date", lToday);
			String lCurrent = "h" + String.valueOf(Calendar.getInstance().get(Calendar.HOUR));
			DBObject lRecord;
			if (lIncoming > 0) {
				lRecord = mExchangesCollection.findOne(lQuery);
				if (null == lRecord) {
					mExchangesCollection.insert(new BasicDBObject().append("date",
							lToday).append(lCurrent, new BasicDBObject(TT_IN, lIncoming)));
				} else {
					if (lRecord.get(lCurrent) != null) {
						if (((DBObject) lRecord.get(lCurrent)).get(TT_IN) != null) {
							((DBObject) lRecord.get(lCurrent)).put(TT_IN, Long.parseLong(((DBObject) lRecord.get(lCurrent)).get(TT_IN).toString()) + lIncoming);
						} else {
							((DBObject) lRecord.get(lCurrent)).put(TT_IN, lIncoming);
						}
					} else {
						lRecord.put(lCurrent, new BasicDBObject(TT_IN, lIncoming));
					}
					mExchangesCollection.save(lRecord);
				}
			}
			if (lOutgoing > 0) {
				lRecord = mExchangesCollection.findOne(lQuery);
				if (null == lRecord) {
					mExchangesCollection.insert(new BasicDBObject().append("date",
							lToday).append(lCurrent, new BasicDBObject(TT_OUT, lOutgoing)));
				} else {
					if (lRecord.get(lCurrent) != null) {
						if (((DBObject) lRecord.get(lCurrent)).get(TT_OUT) != null) {
							((DBObject) lRecord.get(lCurrent)).put(TT_OUT, Long.parseLong(((DBObject) lRecord.get(lCurrent)).get(TT_OUT).toString()) + lOutgoing);
						} else {
							((DBObject) lRecord.get(lCurrent)).put(TT_OUT, lOutgoing);
						}
					} else {
						lRecord.put(lCurrent, new BasicDBObject(TT_OUT, lOutgoing));
					}
					mExchangesCollection.save(lRecord);
				}
			}
		} catch (NumberFormatException aEx) {
			mLog.error(Logging.getSimpleExceptionMessage(aEx, "writePlugInsMemoryToMongoDB"));
		}
	}
}
