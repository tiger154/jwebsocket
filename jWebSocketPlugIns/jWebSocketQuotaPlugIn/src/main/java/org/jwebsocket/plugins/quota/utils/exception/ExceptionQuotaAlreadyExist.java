/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.utils.exception;

/**
 *
 * @author osvaldo
 */
public class ExceptionQuotaAlreadyExist extends Exception {

    public static String MMESSAGE = "Quota already exist";

    public ExceptionQuotaAlreadyExist(String aUuid) {
        super(MMESSAGE + " with " + aUuid);
    }
}
