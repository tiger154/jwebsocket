/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jms.api;

import javax.jms.MessageProducer;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.jms.JMSEngine;

/**
 *
 * @author kyberneees
 */
public interface IConnectorsManager extends IInitializable {

	WebSocketConnector addConnector(String aSessionId, String aIpAddress, String aReplyDestination) throws Exception;

	boolean sessionExists(String aSessionId) throws Exception;

	WebSocketConnector getConnector(String aSessionId) throws Exception;

	void removeConnector(String aSessionId) throws Exception;

	void setReplyProducer(MessageProducer aReplyProducer);
	
	void setEngine(JMSEngine aEngine);
			
	/**
	 * Sets a connector status. 0 == online, 1 = offline
	 *
	 * @param aStatus
	 */
	void setStatus(String aSessionId, int aStatus) throws Exception;
}
