/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.utils;

import java.util.Map;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaProvider;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;

/**
 *
 * @author osvaldo
 */
public class QuotaProvider implements IQuotaProvider {
    
    Map<String, IQuota> mAvailableQuotaList;
    Map<String, IQuotaStorage> mAvailableStorage;
    Map<String, IQuota> mUnavailableFilterQuotaList;

    public Map<String, IQuota> getAvailableQuotaList() {
        return mAvailableQuotaList;
    }

    public Map<String, IQuotaStorage> getAvailableStorage() {
        return mAvailableStorage;
    }
    

    public void setavailableQuotaList(Map<String, IQuota> mavailableQuotaList) {
        this.mAvailableQuotaList = mavailableQuotaList;
    }

    public QuotaProvider() {
    }

    public QuotaProvider(Map<String, IQuota> aAvailableQuotaList, 
            Map<String, IQuotaStorage> aAvailableStorage ) {
        
        this.mAvailableQuotaList = aAvailableQuotaList;
        this.mAvailableStorage = aAvailableStorage;
    }

    @Override
    public Map<String, IQuotaStorage> getActiveStorages() {
        return getAvailableStorage();
    }
    
    

    @Override
    public IQuota getQuotaByIdentifier(String aIdentifier) throws Exception{
        
        if (mAvailableQuotaList.containsKey( aIdentifier )){
            return mAvailableQuotaList.get( aIdentifier );
        }else{
            throw new Exception("Quota with indentifier ("+aIdentifier+") not found");
        }
    }

    @Override
    public Map<String, IQuota> getActiveQuotas() {
        return mAvailableQuotaList;
    }

    @Override
    public String getIdentifier(int aPos) {
        
        String [] lValues  = (String[])mAvailableQuotaList.keySet().toArray();
        return lValues[aPos];
    }
    
}
