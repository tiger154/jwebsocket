//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer BaseClusterEndPoint (Community Edition, CE)
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
package org.jwebsocket.plugins.loadbalancer.api;

import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public abstract class BaseClusterEndPoint implements IClusterEndPoint {

	@Override
	public void writeToToken(Token aToken) {
		aToken.setString(Attributes.GENERIC_ID_FIELD, getEndPointId());
		aToken.setString(Attributes.STATUS, getStatus().name());
		aToken.setString(Attributes.ENDPOINT_ID, getConnectorId());
		aToken.setLong(Attributes.REQUESTS, getRequests());
		if (getCpuUsage() != -1) {
			aToken.setDouble(Attributes.CPU, getCpuUsage());
		}
	}

	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
