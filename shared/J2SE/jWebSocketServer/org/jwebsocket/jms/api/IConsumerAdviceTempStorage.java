//	---------------------------------------------------------------------------
//	jWebSocket - IConsumerAdviceTempStorage interface (Community Edition, CE)
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
package org.jwebsocket.jms.api;

import java.util.Map;

/**
 * Stores the JMS ConsumerInfo event related data into a distributed database,
 * to be used by the jWebSocket Cluster LoadBalancers during the connector
 * starting process.
 *
 * @author Rolando Santamaria Maso
 */
public interface IConsumerAdviceTempStorage {

	/**
	 * Gets and removes the JMS consumer id given a custom correlationId.
	 * Removes the record once retrieved.
	 *
	 * @param aCorrelationId
	 * @return
	 * @throws Exception
	 */
	String getConsumerId(String aCorrelationId) throws Exception;

	/**
	 * Gets a Map with consumerId, connectionId, destination and correlationId
	 * data. Removes the record once retrieved.
	 *
	 * @param aCorrelationId
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getData(String aCorrelationId) throws Exception;
}
