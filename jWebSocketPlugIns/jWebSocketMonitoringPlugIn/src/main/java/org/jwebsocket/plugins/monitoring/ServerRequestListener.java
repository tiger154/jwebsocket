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


	public ServerRequestListener() {

		try {
			mConnection = new Mongo();
			mChartingCollection = mConnection.getDB("db_charting").getCollection("exchanges_server");
		} catch (UnknownHostException ex) {
			mLog.error(ex.getMessage());

		}
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

	@Override
	public void processOpened(WebSocketServerEvent aEvent) {
		
	}

	@Override
	public void processClosed(WebSocketServerEvent aEvent) {
		
	}
}
