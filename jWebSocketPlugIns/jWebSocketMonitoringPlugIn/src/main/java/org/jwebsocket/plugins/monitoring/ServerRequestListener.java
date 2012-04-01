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
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServerListener;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author merlyta
 */
public class ServerRequestListener implements WebSocketServerListener {

	Mongo mConnection;
	DBCollection mChartingCollection;
	Logger mLog = Logging.getLogger(ServerRequestListener.class);
	
//	DBCollection mConnUsersCollection;
//	private static boolean mIsRunning = false;
//	private static int mConnectedUsers = 0;
//	private static int mTimeCounter = 0;
//	private static boolean mCountConnectedUsersRunning = false;
//	private static Thread mThreadConnectedUsers;

	public ServerRequestListener() {

		try {
			mConnection = new Mongo();
			mChartingCollection = mConnection.getDB("db_charting").getCollection("exchanges_server");
//			mConnUsersCollection = mConnection.getDB("db_users").getCollection("connected_users");

			//initializing the thread to count each 1 second the online users and save it into mongoDB
//			mThreadConnectedUsers = new Thread(new CountConnectedUsersTask());

		} catch (UnknownHostException ex) {
			mLog.error(ex.getMessage());

		}
	}
/*
	class CountConnectedUsersTask implements Runnable {

		@Override
		public void run() {
			while (mCountConnectedUsersRunning) {
				//mongoDB logic
				/*
				DBObject lRecord = mConnUsersCollection.findOne(
				new BasicDBObject().append("seconds", 0));
				
				if (null == lRecord) {
				mConnUsersCollection.insert(new BasicDBObject().append("seconds", 0));
				}
				mConnUsersCollection.update(lRecord, new BasicDBObject()
				.append("$set", new BasicDBObject().append("s" + mTimeCounter, mConnectedUsers)));
				
				/* a mechanism should be implemented in order to insert into mongoDB
				For example: 
				Seconds [{0:5, 1:7, 2:5, 3:9, 4:150, 5:90, 6:95 ... 59:11}];
				Minutes 0 1 2 3 4 5 6 ... 59
				Hours   0 1 2 3 4 5 6 ... 23
				Days    1 2 3 4 5 6 7 ... 31
				Months  
				* 
			}
			try {
				Thread.sleep(1000);
				mTimeCounter++;
				if (mTimeCounter >= 60) {
					mTimeCounter = 0;
				}
			} catch (InterruptedException ex) {
			}
		}
	}
*/

	@Override
	public void processClosed(WebSocketServerEvent wsse) {
//		mConnectedUsers--;
	}

	@Override
	public void processOpened(WebSocketServerEvent wsse) {
//		mConnectedUsers++;
//		if (!mIsRunning) {
//			mThreadConnectedUsers.start();
//			mIsRunning = true;
//		}
//		
	}

	@Override
	public void processPacket(WebSocketServerEvent wsse, WebSocketPacket wsp) {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		String lToday = format.format(new Date());

		DBObject lRecord = mChartingCollection.findOne(new BasicDBObject().append("date", lToday));

		if (null == lRecord) {
			mChartingCollection.insert(new BasicDBObject().append("date", lToday));
			lRecord = mChartingCollection.findOne(new BasicDBObject().append("date", lToday));
		}
		mChartingCollection.update(lRecord, new BasicDBObject().append("$inc", new BasicDBObject().append("h" + String.valueOf(new Date().getHours()), 1)));


	}
	/*
	public void destroy() {
	try {
	mCountConnectedUsersRunning = false;
	mThreadConnectedUsers.join(1000);
	mThreadConnectedUsers.stop();
	} catch (Exception e) {
	mLog.error(e.getMessage());
	}
	}
	 */
}
