//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket ServerRequestListener
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.monitoring;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
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
	private static Logger mLog = Logging.getLogger(ServerRequestListener.class);

	public ServerRequestListener() {
		String lVersion = "<unknown>";

		try {
			// suppress stack traces from mongo db to console
			java.util.logging.Logger.getLogger("com.mongodb").setLevel(
					java.util.logging.Level.OFF);
			mConnection = new Mongo();
			DB lDB = mConnection.getDB("db_charting");
			List<String> lDBNames = mConnection.getDatabaseNames();
			if (mLog.isInfoEnabled()) {
				mLog.info("Found databases: " + lDBNames.toString() + ".");
			}
			lVersion = mConnection.getVersion();
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
