/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jms.api;

import java.util.Map;
import javax.jms.MessageProducer;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.jms.JMSConnector;
import org.jwebsocket.jms.JMSEngine;

/**
 *
 * @author kyberneees
 */
public interface IConnectorsManager extends IInitializable {

	JMSConnector addConnector(String aSessionId, String aIpAddress, String aReplyDest) throws Exception;

	boolean sessionExists(String aSessionId) throws Exception;

	JMSConnector getConnector(String aSessionId) throws Exception;

	void removeConnector(String aSessionId) throws Exception;

	void setReplyProducer(MessageProducer aReplyProducer);

	void setEngine(JMSEngine aEngine);

	Map<String, WebSocketConnector> getConnectors() throws Exception;

	/**
	 * Sets a connector status. 0 == online, 1 = offline
	 *
	 * @param aStatus
	 */
	void setStatus(String aSessionId, int aStatus) throws Exception;

	/**
	 * Gets the client session id by its reply destination
	 *
	 * @param aReplyDest
	 * @return
	 */
	public String getSessionByReplyDest(String aReplyDest);
}
