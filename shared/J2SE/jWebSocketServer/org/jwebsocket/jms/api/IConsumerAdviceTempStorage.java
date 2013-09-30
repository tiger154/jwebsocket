/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jms.api;

import java.util.Map;

/**
 * Component created to support AMQ clusters conduit-subscriptions.
 *
 * @author kyberneees
 */
public interface IConsumerAdviceTempStorage {

	/**
	 * Stores the replySelector/connectionId values into a Map like structure
	 *
	 * @param aReplySelector
	 * @param aConnectionId
	 * @throws Exception
	 */
	void put(String aReplySelector, String aConnectionId, String aConsumerId) throws Exception;

	/**
	 * Gets and removes the consumer identifier given a replySelector value.
	 * Returns NULL if value is not found.
	 *
	 * @param aReplySelector
	 * @return
	 * @throws Exception
	 */
	String getConsumerId(String aReplySelector) throws Exception;

	/**
	 * Gets a Map with consumerId, connectionId and replySelector data.
	 *
	 * @param aReplySelector
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getData(String aReplySelector) throws Exception;

	/**
	 * Remove data associated to a given consumer identifier.
	 *
	 * @param aConsumerId
	 * @throws Exception
	 */
	void remove(String aConsumerId) throws Exception;
}
