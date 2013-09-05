/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.utils;

import java.util.Map;
import org.jwebsocket.plugins.quota.api.IQuota;
import org.jwebsocket.plugins.quota.api.IQuotaProvider;

/**
 *
 * @author osvaldo
 */
public class QuotaProvider implements IQuotaProvider {
    
    Map<String, IQuota> mavailableQuotaList;

    public void setavailableQuotaList(Map<String, IQuota> mavailableQuotaList) {
        this.mavailableQuotaList = mavailableQuotaList;
    }

    public QuotaProvider() {
    }

    public QuotaProvider(Map<String, IQuota> availableQuotaList) {
        this.mavailableQuotaList = availableQuotaList;
    }

    @Override
    public IQuota getQuotaByType(String aType) throws Exception{
        if (mavailableQuotaList.containsKey( aType )){
            return mavailableQuotaList.get(aType);
        }else{
            throw new Exception("The Quota ("+aType+") not found");
        }
    }

    @Override
    public Map<String, IQuota> getActiveQuotas() {
        return mavailableQuotaList;
    }
    
}
