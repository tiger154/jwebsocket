//	---------------------------------------------------------------------------
//	jWebSocket - JMS Gateway Client (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import javax.jms.JMSException;

/**
 *
 * @author Alexander Schulze
 */
public class JWSEndPoint extends JMSEndPoint {

	// private constructor, public API only allows contructors 
	// with arguments, see below.

	/**
	 *
	 */
		protected JWSEndPoint() {
	}
	
	/**
	 *
	 * @param aBrokerURI
	 * @param aGatewayTopic
	 * @param aGatewayId
	 * @param aEndPointId
	 * @param aThreadPoolSize
	 * @param aDurable
	 */
	public JWSEndPoint(String aBrokerURI, String aGatewayTopic,
			String aGatewayId, String aEndPointId, int aThreadPoolSize,
			boolean aDurable) {
		super(aBrokerURI, aGatewayTopic,
				aGatewayId, aEndPointId, aThreadPoolSize,
				aDurable);
	}

	/**
	 *
	 * @param aBrokerURI
	 * @param aGatewayTopic
	 * @param aGatewayId
	 * @param aEndPointId
	 * @param aThreadPoolSize
	 * @param aDurable
	 * @return
	 * @throws JMSException
	 */
	public static JWSEndPoint getInstance(String aBrokerURI, String aGatewayTopic,
			String aGatewayId, String aEndPointId, int aThreadPoolSize,
			boolean aDurable) throws JMSException {
		// create an "empty" endpoint instance
		JWSEndPoint lEP = new JWSEndPoint();
		// and initialize it
		// checking for duplicate endpoints and raising exception if such
		lEP.init(aBrokerURI, aGatewayTopic,
				aGatewayId, aEndPointId, aThreadPoolSize,
				aDurable);
		// return JMS Endpoint instance in case of success
		return lEP;
	}
	
}
