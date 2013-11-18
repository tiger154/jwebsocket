/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaCountdownSI;

/**
 *
 * @author osvaldo
 */
public class QuotaCountdown extends BaseQuota {

    
    @Override
    public long reduceQuota(String aUuid, long aAmount) {

        long lValue = getQuota(aUuid);
        if (lValue == 0) {
            return -1;
        }
        if (lValue - aAmount < 0) {
            getStorage().update(aUuid, (long) 0);
            return -1;
        }
        return getStorage().update(aUuid, lValue - aAmount);
    }

     
    @Override
    public void register(String aInstance, String aNameSpace, String aUuid, 
        long aAmount, String aInstanceType, String aQuotaType, String aQuotaIdentifier,
        String aActions)  throws Exception{

        super.register(aInstance, aNameSpace, aUuid, aAmount, aInstanceType, 
                aQuotaType, aQuotaIdentifier, aActions);
        
        IQuotaSingleInstance lSingleQuota;
        lSingleQuota = new QuotaCountdownSI(aAmount, aInstance, aUuid, aNameSpace, aQuotaType,
                aQuotaIdentifier, aInstanceType, aActions);
        mQuotaStorage.save(lSingleQuota);
    }
}
