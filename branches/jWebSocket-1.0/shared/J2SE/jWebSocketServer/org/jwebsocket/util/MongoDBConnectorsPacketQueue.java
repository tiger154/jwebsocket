//	---------------------------------------------------------------------------
//	jWebSocket MongoDBConnectorsResponseBuffer (Community Edition, CE)
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
package org.jwebsocket.util;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.util.ArrayList;
import org.jwebsocket.api.IConnectorsPacketQueue;
import java.util.List;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBConnectorsPacketQueue implements IConnectorsPacketQueue {

	private final DBCollection mPackets;

	public MongoDBConnectorsPacketQueue(DBCollection aPackets) {
		Assert.notNull(aPackets, "The packets collection argument is not valid!");

		mPackets = aPackets;
	}

	@Override
	public void enqueue(String aConnectorId, String aPacket) throws Exception {
		mPackets.save(new BasicDBObject()
				.append("c", aConnectorId)
				.append("p", aPacket));
	}

	@Override
	public List<String> dequeue(String aConnectorId) throws Exception {
		DBCursor lCursor = mPackets.find(new BasicDBObject().append("c", aConnectorId))
				.sort(new BasicDBObject().append("_id", 1));
		List<String> lList = new ArrayList<String>(lCursor.count());

		while (lCursor.hasNext()) {
			lList.add((String) lCursor.next().get("p"));
		}
		mPackets.remove(new BasicDBObject().append("c", aConnectorId));

		return lList;
	}

	@Override
	public void initialize() throws Exception {
		mPackets.ensureIndex(new BasicDBObject().append("c", 1));
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public void clear(String aConnectorId) throws Exception {
		mPackets.remove(new BasicDBObject().append("c", aConnectorId));
	}

	@Override
	public void clearAll() throws Exception {
		mPackets.remove(new BasicDBObject());
	}

}
