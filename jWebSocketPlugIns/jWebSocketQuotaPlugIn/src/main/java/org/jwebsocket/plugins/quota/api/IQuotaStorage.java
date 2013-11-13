/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.api;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.List;
import java.util.Map;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author osvaldo
 */
public interface IQuotaStorage {

    public void save(IQuotaSingleInstance aQuota);

    public boolean save(String aUuid, String aInstance, String aInstanceType );

    public void remove(String aInstance, String aUuid);

    public long update(String aUuid, Long aValue);

    public boolean quotaExist(String aUuid);

    public boolean quotaExist(String aNameSpace, String aQuotaType, String aInstance);

    public List<String> getAllQuotaUuid(String aQuotaType);

    public List<IQuotaSingleInstance> getQuotas(String aQuotaType);

    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier);
    
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs, String aInstance);
    
    public String getUuid(String aQuotaType, String aNs, String aInstance,
            String aInstanceType ) throws ExceptionQuotaNotFound;

    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType, String aInstance);

    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType, String aNs);

    public IQuotaSingleInstance getQuotaByUuid(String aUuid);
    
    public Map<String, Object> getRawQuota(String aUuid,String ainstance);
    
    
}
