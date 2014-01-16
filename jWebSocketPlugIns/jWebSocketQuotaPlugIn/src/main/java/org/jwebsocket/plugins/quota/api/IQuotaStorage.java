/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.api;

import java.util.List;
import java.util.Map;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaChildSI;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author osvaldo
 */
public interface IQuotaStorage {

    public boolean save(IQuotaSingleInstance aQuota);

    public boolean save(QuotaChildSI aQuota);
    
    public void initialize() throws Exception;

    public void remove(String aInstance, String aUuid);

    public void remove(QuotaChildSI aQuotaChild);

    public long update(String aUuid, Long aValue);

    public long update(QuotaChildSI aQuotaChild);

    public boolean quotaExist(String aUuid);

    public boolean quotaExist(String aNameSpace, String aQuotaIdentifier, 
            String aInstance, String aActions);

    public String getActions(String aUuid);

    public List<IQuotaSingleInstance> getQuotas(String aQuotaType);

    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier);

    public List<IQuotaSingleInstance> getQuotasByIdentifierNSInstanceType(String aIdentifier,
            String aNameSpace, String aInstanceType);

    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs, String aInstance);
    
    public String getUuid(String aQuotaIdentifier, String aNs, String aInstance,
            String aInstanceType,String aActions ) throws ExceptionQuotaNotFound;

    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType, String aInstance);

    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType, String aNs);

    public IQuotaSingleInstance getQuotaByUuid(String aUuid);

    public Map<String, Object> getRawQuota(String aUuid, String aInstance);

    public void updateIntervalResetDate(String aUuid, String aResetDate);
}
