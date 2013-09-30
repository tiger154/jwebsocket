/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jms.api;

import java.util.Map;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.jms.JMSConnector;
import org.jwebsocket.jms.JMSEngine;

/**
 *
 * @author kyberneees
 */
public interface IConnectorsManager extends IInitializable {

	JMSConnector add(String aConnectionId, String aConsumerId, String aReplySelector) throws Exception;

	boolean exists(String aReplySelector) throws Exception;

	JMSConnector get(String aReplySelector) throws Exception;

	void remove(String aConsumerId) throws Exception;

	void setEngine(JMSEngine aEngine);

	Map<String, WebSocketConnector> getAll() throws Exception;

	String getReplySelectorByConsumerId(String aConsumerId) throws Exception;

	/**
	 * Sets a connector status. 0 == online, 1 = offline
	 *
	 * @param aReplySelector
	 */
	void setStatus(String aReplySelector, int aStatus) throws Exception;
}
