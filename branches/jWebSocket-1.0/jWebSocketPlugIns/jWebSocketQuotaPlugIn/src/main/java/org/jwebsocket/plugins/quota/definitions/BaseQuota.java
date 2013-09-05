/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions;

import java.util.List;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaBaseInstance;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaAlreadyExist;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author osvaldo
 */
public abstract class BaseQuota implements IQuota{
    
    IQuotaStorage mQuotaStorage;
    String mQuotaType;
    long mDefaultReduceValue;
    long mDefaultIncrease;

    public void setDefaultReduceValue(long aDefaultReduceValue) {
        this.mDefaultReduceValue = aDefaultReduceValue;
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
    public long getQuota(String aInstance, String aNameSpace, String aInstanceType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getQuota(String aUuid) {
        
        QuotaBaseInstance lQuotaInstance = (QuotaBaseInstance) mQuotaStorage.getQuotaByUuid(aUuid);
        return lQuotaInstance.getvalue();
        
    }
    
    @Override
    abstract public long reduceQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount);

    @Override
    abstract public long reduceQuota(String aUuid, long aAmount);
    
    @Override
    abstract public long reduceQuota(String aUuid);

    @Override
    abstract public long increaseQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount);

    @Override
    abstract public long increaseQuota(String aUuid, long aAmount);

    @Override
    abstract public long setQuota(String aInstance, String aNameSpace, String aInstanceType, long aAmount);

    @Override
    abstract public long setQuota(String aUuid, long aAmount);

    
    @Override
    abstract public void register(String aUuid, String aInstance, 
        String aInstanceType) throws Exception;

    @Override
    abstract public void register(String aInstance, String aNameSpace, 
    String aUuid, long aAmount, String aInstanceType) 
            throws ExceptionQuotaAlreadyExist;

    @Override
    abstract public void unregister(String aInstance, String aUuid) 
            throws ExceptionQuotaNotFound;

    @Override
    abstract public void unregister(String aInstance, String aUuid
            ,String aInstanceType)throws ExceptionQuotaNotFound;

    @Override
    abstract public List<String> getRegisteredInstances(String aNamespace, String aId);

    @Override
    abstract public List<String> getRegisterdQuotas( String aNamespace );

    @Override
    public String getQuotaUuid(String aNamespace, String aInstance, String aInstanceType){
        try {
            return mQuotaStorage.getUuid(mQuotaType, aNamespace, aInstance, aInstanceType);
        } catch (ExceptionQuotaNotFound ex) {
            return "not-found";
        }
    }

    
   
    
}
