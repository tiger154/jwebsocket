/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.utils;

import java.util.UUID;
import javolution.util.FastList;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaBaseInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaCountdownSI;
import org.jwebsocket.util.Tools;

/**
 *
 * @author osvaldo
 */
public class QuotaHelper {

	/**
	 *
	 * @return
	 */
	public static String generateQuotaUUID() {
        return Tools.getMD5(UUID.randomUUID().toString());
    }

    // to do with map

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
	 * @return
	 */
	    public static IQuotaSingleInstance factorySingleInstance(long aValue, String aInstance,
            String aUuid, String aNamesPace, String aQuotaType, String aQuotaIdentifier,
            String aInstanceType, String aActions) {

        if (aQuotaType.equals("CountDown")) {
            return new QuotaCountdownSI(aValue, aInstance, aUuid,
                    aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
        } else {
            return new QuotaBaseInstance(aValue, aInstance, aUuid, aNamesPace,
                    aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
        }

    }

	/**
	 *
	 * @return
	 */
	public static FastList<String> ignoredUsers() {
        FastList<String> lIgnoredUsers = new FastList<String>();
        lIgnoredUsers.add("anonymous");
        return lIgnoredUsers;
    }
}
