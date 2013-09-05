/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaCountdownSingleinstance;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author osvaldo
 */
public class QuotaCountdown extends BaseQuota {

    @Override
    public void setQuotaType(String mQuotaType) {
        super.setQuotaType(mQuotaType);
    }

    @Override
    public void setStorage(IQuotaStorage aQuotaStorage) {
        super.setStorage(aQuotaStorage);
    }

    @Override
    public long getQuota(String aInstance, String aNameSpace, String aInstanceType) {
        return 0;
    }

    @Override
    public long reduceQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount) {
        return 0;
    }

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
    public long reduceQuota( String aUuid ) {
        
        long lValue = getQuota(aUuid);
        if (lValue == 0) {
            return -1;
        }
        if (lValue - mDefaultReduceValue < 0) {
            getStorage().update(aUuid, (long) 0);
            return -1;
        }
        return getStorage().update( aUuid, lValue - mDefaultReduceValue );
    }
    
    

    @Override
    public long increaseQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount) {
        return 0;
    }

    @Override
    public long increaseQuota(String aUuid, long aAmount) {
        
        long lValue = getQuota(aUuid);
        return getStorage().update(aUuid, lValue + aAmount);
        
    }

    @Override
    public long setQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount) {
        return 0;
    }

    @Override
    public long setQuota(String aUuid, long aAmount) {
        return getStorage().update(aUuid, aAmount);
    }

    @Override
    public void register( String aUuid, String aInstance, String aInstanceType )
            throws Exception {
        if(!mQuotaStorage.quotaExist(aUuid))
            throw new ExceptionQuotaNotFound(aUuid);
        
        if (!mQuotaStorage.save(aUuid, aInstance, aInstanceType)) {
            throw new ExceptionQuotaAlreadyExist(aUuid);
        }
    }

    @Override
    public void register(String aInstance, String aNameSpace, String aUuid,
            long aAmount, String aInstanceType) throws ExceptionQuotaAlreadyExist {

        if (mQuotaStorage.quotaExist(aNameSpace, mQuotaType, aInstance)) {
            throw new ExceptionQuotaAlreadyExist(aUuid);
        }

        IQuotaSingleInstance lSingleQuota;
        lSingleQuota = new QuotaCountdownSingleinstance(aAmount, aInstance, aUuid, aNameSpace, mQuotaType, aInstanceType);
        mQuotaStorage.save(lSingleQuota);

    }

    @Override
    public void unregister(String aInstance, String aUuid)
            throws ExceptionQuotaNotFound {

        if (!mQuotaStorage.quotaExist(aUuid)) {
            throw new ExceptionQuotaNotFound(aUuid);
        }
        mQuotaStorage.remove(aUuid, aInstance);
    }

    @Override
    public void unregister(String aInstance, String aUuid,
            String aInstanceType) {
    }

    @Override
    public List<String> getRegisteredInstances(String aNamespace, String aUuid) {
        return new FastList<String>();
    }

    @Override
    public List<String> getRegisterdQuotas(String aNamespace) {
        return new FastList<String>();
    }


}
