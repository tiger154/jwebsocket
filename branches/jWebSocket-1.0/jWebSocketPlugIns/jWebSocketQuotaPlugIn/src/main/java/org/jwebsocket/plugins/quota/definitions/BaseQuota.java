/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaBaseInstance;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author osvaldo
 */
public abstract class BaseQuota implements IQuota {

    protected static Logger mLog = Logging.getLogger();
    protected IQuotaStorage mQuotaStorage;
    protected String mQuotaType;
    protected String mQuotaIdentifier;
    protected long mDefaultReduceValue;
    protected long mDefaultIncrease;

    public void setDefaultReduceValue(long aDefaultReduceValue) {
        this.mDefaultReduceValue = aDefaultReduceValue;
    }

    public void setQuotaIdentifier(String mQuotaIdentifier) {
        this.mQuotaIdentifier = mQuotaIdentifier;
    }

    public void setDefaultIncreaseValue(long aDefaultIncrease) {
        this.mDefaultIncrease = aDefaultIncrease;
    }

    public void setQuotaType(String aQuotaType) {
        this.mQuotaType = aQuotaType;
    }

    @Override
    public IQuotaStorage getStorage() {
        return mQuotaStorage;
    }

    @Override
    public String getType() {
        return mQuotaType;
    }

    @Override
    public void setStorage(IQuotaStorage aQuotaStorage) {
        this.mQuotaStorage = aQuotaStorage;
    }

    @Override
    public long getQuota( String aInstance, String aNameSpace, String aInstanceType) {
        
        String lUuid = getQuotaUuid(mQuotaIdentifier, aNameSpace, aInstance, aInstanceType);
        return getQuota(lUuid);
    }

    @Override
    public long getQuota(String aUuid) {

        QuotaBaseInstance lQuotaInstance = (QuotaBaseInstance) mQuotaStorage.getQuotaByUuid(aUuid);
        return lQuotaInstance.getvalue();

    }

    @Override
    public String getIdentifier() {
        return mQuotaIdentifier;
    }

    @Override
    abstract public long reduceQuota( String aUuid, long aAmount);
    
    @Override
    public long reduceQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount){
        String lUuid = getQuotaUuid(mQuotaIdentifier, aNameSpace, aInstance, aInstanceType);
        return reduceQuota(lUuid, aAmount);
    }

    

    @Override
    public long reduceQuota( String aUuid){
        return reduceQuota(aUuid, mDefaultReduceValue);
    }

    @Override
    public long increaseQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount){
        String lUuid = getQuotaUuid(mQuotaIdentifier, aNameSpace, aInstance, aInstanceType);
        return increaseQuota(lUuid, aAmount);
    
    }

    @Override
    public long increaseQuota(String aUuid, long aAmount){
        long lValue = getQuota(aUuid);
        return getStorage().update(aUuid, lValue + aAmount);
    }

    @Override
    public long setQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount){
        String lUuid = getQuotaUuid(mQuotaIdentifier, aNameSpace, aInstance, aInstanceType);
        return setQuota(lUuid, aAmount);
    }

    @Override
    public long setQuota(String aUuid, long aAmount){
        return getStorage().update(aUuid, aAmount);
    }

    @Override
    public void register(String aUuid, String aInstance,
            String aInstanceType) throws Exception {

        if (!mQuotaStorage.quotaExist(aUuid)) {
            throw new ExceptionQuotaNotFound(aUuid);
        }

        if (!mQuotaStorage.save(aUuid, aInstance, aInstanceType)) {
            throw new ExceptionQuotaAlreadyExist(aUuid);
        }
    }

    @Override
    public void register(String aInstance, String aNameSpace,
            String aUuid, long aAmount, String aInstanceType, String aQuotaType, String aQuotaIdentifier)
            throws Exception {

        if (mQuotaStorage.quotaExist(aNameSpace, aQuotaIdentifier, aInstance)) {
            throw new ExceptionQuotaAlreadyExist(aUuid);
        }
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
    public void unregister(String aInstance, 
            String aNameSpace, String aInstanceType)
            throws ExceptionQuotaNotFound {
        
        String lUuid = getQuotaUuid(mQuotaIdentifier, aNameSpace, aInstance, aInstanceType);
        
        unregister(aInstance, lUuid);
    }

    @Override
    public List<String> getRegisteredInstances(String aNamespace, String aId){
        return new FastList<String>();
    }

    @Override
    public List<String> getRegisterdQuotas(String aNamespace){
        return new FastList<String>();
    }

    @Override
    public String getQuotaUuid(String aQuotaIdentifier, String aNamespace, String aInstance, String aInstanceType) {
        try {
            return mQuotaStorage.getUuid(aQuotaIdentifier, aNamespace, aInstance, aInstanceType);
        } catch (ExceptionQuotaNotFound ex) {
            return "not-found";
        }
    }
}
