/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaCountdownSI;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaCountdown extends BaseQuota {

    @Override
    public long reduceQuota(String aUuid, long aAmount) {

        long lValue = getQuota(aUuid).getvalue();
        if (lValue == 0) {
            return -1;
        }
        if (lValue - aAmount < 0) {
            getStorage().update(aUuid, (long) 0);
            return -1;
        }
        return getStorage().update(aUuid, lValue - aAmount);
    }

}
