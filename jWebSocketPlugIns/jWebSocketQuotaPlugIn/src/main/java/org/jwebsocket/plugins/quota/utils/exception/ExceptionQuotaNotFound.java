/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.utils.exception;

/**
 *
 * @author osvaldo
 */
public class ExceptionQuotaNotFound extends Exception {

	/**
	 *
	 */
	public static String MMESSAGE = "Quota not found";

	/**
	 *
	 * @param aUuid
	 */
	public ExceptionQuotaNotFound(String aUuid) {
        super(MMESSAGE + " with id " + aUuid);
    }
}
