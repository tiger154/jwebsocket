/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jms.api;

import java.util.List;
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

	JMSConnector addConnector(String aSessionId, String aIpAddress, String aConnectionId) throws Exception;

	boolean sessionExists(String aSessionId) throws Exception;

	JMSConnector getConnector(String aSessionId) throws Exception;

	void removeConnector(String aSessionId) throws Exception;

	void setEngine(JMSEngine aEngine);

	Map<String, WebSocketConnector> getConnectors() throws Exception;

	List<String> getSessionsByConnectionId(String aConnectionId) throws Exception;

	/**
	 * Sets a connector status. 0 == online, 1 = offline
	 *
	 * @param aStatus
	 */
	void setStatus(String aSessionId, int aStatus) throws Exception;
}
