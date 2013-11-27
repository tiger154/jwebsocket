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

	public static String generateQuotaUUID() {
		return Tools.getMD5(UUID.randomUUID().toString());
	}

        // to do with map
	public static IQuotaSingleInstance factorySingleInstance(long aValue, String aInstance,
			String aUuid, String aNamesPace, String aQuotaType, String aQuotaIdentifier,
                        String aInstanceType, String aActions ) {

		if (aQuotaType.equals("CountDown")) {
			return new QuotaCountdownSI(aValue, aInstance, aUuid,
					aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
		} else {
			return new QuotaBaseInstance(aValue, aInstance, aUuid, aNamesPace,
					aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
		}

	}

	public static FastList<String> ignoredUsers() {
		FastList<String> lIgnoredUsers = new FastList<String>();
		lIgnoredUsers.add("anonymous");
		return lIgnoredUsers;
	}
}
