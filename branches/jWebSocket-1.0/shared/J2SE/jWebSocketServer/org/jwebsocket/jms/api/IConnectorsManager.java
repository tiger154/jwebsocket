//	---------------------------------------------------------------------------
//	jWebSocket - IConnectorsManager interface (Community Edition, CE)
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
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.jms.JMSConnector;
import org.jwebsocket.jms.JMSEngine;

/**
 * Component that manages the jWebSocket cluster connectors data.
 *
 * @author Rolando Santamaria Maso
 */
public interface IConnectorsManager extends IInitializable {

	/**
	 * Stores new JMSConnector data and returns a JMSConnector instance.
	 *
	 * @param aConnectionId The JMS client connecton id.
	 * @param aConsumerId The JMS client consumer id.
	 * @param aReplySelector The client unique 'reply' selector value.
	 * @param aSessionId  The client session identifier.
	 * @return
	 * @throws Exception
	 */
	JMSConnector add(String aConnectionId, String aConsumerId, String aReplySelector,
			String aSessionId) throws Exception;

	/**
	 * Returns TRUE if a given replySelector is associated to an existing
	 * JMSConnector data, FALSE otherwise.
	 *
	 * @param aReplySelector
	 * @return
	 * @throws Exception
	 */
	boolean exists(String aReplySelector) throws Exception;

	/**
	 * Gets a JMSConnector instance given it replySelector. Returns NULL if
	 * connector data does not exists.
	 *
	 * @param aReplySelector
	 * @return
	 * @throws Exception
	 */
	JMSConnector get(String aReplySelector) throws Exception;

	/**
	 * Removes a connector data given it's consumer id.
	 *
	 * @param aConsumerId
	 * @throws Exception
	 */
	void remove(String aConsumerId) throws Exception;

	/**
	 * Sets the JMSEngine instance.
	 *
	 * @param aEngine
	 */
	void setEngine(JMSEngine aEngine);

	/**
	 * Get all existing JMSConnector intances.
	 *
	 * @return
	 * @throws Exception
	 */
	Map<String, WebSocketConnector> getAll() throws Exception;

	/**
	 * Gets the client replySelector by it's JMS consumer id.
	 *
	 * @param aConsumerId The client JMS consumer id.
	 * @return The client replySelector value.
	 * @throws Exception
	 */
	String getReplySelectorByConsumerId(String aConsumerId) throws Exception;

	/**
	 * Sets the connector status. 0 == ONLINE, 1 = OFFLINE
	 *
	 * @param aReplySelector
	 * @param aStatus
	 * @throws java.lang.Exception
	 */
	void setStatus(String aReplySelector, int aStatus) throws Exception;
}
