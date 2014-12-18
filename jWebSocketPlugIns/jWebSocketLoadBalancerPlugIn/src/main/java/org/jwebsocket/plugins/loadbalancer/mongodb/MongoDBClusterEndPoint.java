//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer MongoDBClusterEndPoint (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.loadbalancer.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.jwebsocket.plugins.loadbalancer.EndPointStatus;
import static org.jwebsocket.plugins.loadbalancer.api.Attributes.*;
import org.jwebsocket.plugins.loadbalancer.api.BaseClusterEndPoint;

/**
 *
 * @author kyberneees
 */
public class MongoDBClusterEndPoint extends BaseClusterEndPoint {

	private DBCollection mEndPoints;
	private DBObject mDocument;

	public MongoDBClusterEndPoint(DBObject aThis, DBCollection aEndPoints) {
		this.mEndPoints = aEndPoints;
		this.mDocument = aThis;
	}

	void sync() {
		mEndPoints.save(mDocument);
	}

	@Override
	public String getConnectorId() {
		return (String) mDocument.get(CONNECTOR_ID);
	}

	@Override
	public double getCpuUsage() {
		return (Double) mDocument.get(CPU);
	}

	@Override
	public long getRequests() {
		return (Long) mDocument.get(REQUESTS);
	}

	@Override
	public String getEndPointId() {
		return (String) mDocument.get(ENDPOINT_ID);
	}

	@Override
	public EndPointStatus getStatus() {
		return EndPointStatus.valueOf((String) mDocument.get(STATUS));
	}

	@Override
	public void increaseRequests() {
		mEndPoints.update(mDocument, new BasicDBObject()
				.append("$inc",
						new BasicDBObject()
						.append(REQUESTS, 1)));
	}

	@Override
	public void setCpuUsage(double aCpuUsage) {
		mDocument.put(CPU, aCpuUsage);
		sync();
	}

	@Override
	public void setStatus(EndPointStatus aStatus) {
		mDocument.put(STATUS, aStatus.name());
		sync();
	}

	@Override
	public String getClientRuntimePlatform() {
		return (String) mDocument.get(RUNTIME_PLATFORM);
	}

	@Override
	public void setClientRuntimePlatform(String aPlatform) {
		mDocument.put(RUNTIME_PLATFORM, aPlatform);
		sync();
	}

	public Long getUses() {
		return (Long) mDocument.get(ENDPOINT_USES);
	}

	public void incrementUses() {
		mDocument.put(ENDPOINT_USES, getUses() + 1);
		sync();
	}
}
