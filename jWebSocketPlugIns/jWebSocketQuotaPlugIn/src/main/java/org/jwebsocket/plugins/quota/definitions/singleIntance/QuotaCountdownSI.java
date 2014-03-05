/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions.singleIntance;

/**
 *
 * @author osvaldo
 */
public class QuotaCountdownSI extends QuotaBaseInstance {

	/**
	 *
	 * @param aValue
	 * @param aInstance
	 * @param aUuid
	 * @param aNamesPace
	 * @param aQuotaType
	 * @param aQuotaIdentifier
	 * @param aInstanceType
	 * @param aActions
	 */
	public QuotaCountdownSI(long aValue, String aInstance, String aUuid, String aNamesPace,
            String aQuotaType, String aQuotaIdentifier, String aInstanceType, String aActions) {
        super(aValue, aInstance, aUuid, aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
    }
}
