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
	 * Gets and removes the consumer identifier given a correlationId value.
	 * Removes the record once retrieved.
	 *
	 * @param aCorrelationId
	 * @return
	 * @throws Exception
	 */
	String getConsumerId(String aCorrelationId) throws Exception;

	/**
	 * Gets a Map with consumerId, connectionId, destination and correlationId data.
	 * Removes the record once retrieved.
	 *
	 * @param aCorrelationId
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getData(String aCorrelationId) throws Exception;
}
