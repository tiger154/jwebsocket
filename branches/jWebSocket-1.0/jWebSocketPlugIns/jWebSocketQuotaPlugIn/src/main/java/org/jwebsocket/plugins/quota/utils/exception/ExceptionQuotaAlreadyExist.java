/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.utils.exception;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class ExceptionQuotaAlreadyExist extends Exception {

	/**
	 *
	 */
	public static String MMESSAGE = "Quota already exist";

	/**
	 *
	 * @param aUuid
	 */
	public ExceptionQuotaAlreadyExist(String aUuid) {
        super(MMESSAGE + " with " + aUuid);
    }
}
