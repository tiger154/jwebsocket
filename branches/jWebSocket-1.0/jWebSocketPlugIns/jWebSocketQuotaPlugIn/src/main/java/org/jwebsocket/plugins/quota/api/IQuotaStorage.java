//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket IQuota Storage (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.quota.api;

import java.util.List;
import java.util.Map;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaChildSI;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public interface IQuotaStorage {

    /**
     * save a quota object instance
     * 
     * @param aQuota
     * @return
     */
    public boolean save(IQuotaSingleInstance aQuota);

    /**
     * register a child quota to one parent quota. 
     * 
     * @param aQuota
     * @return
     */
    public boolean save(QuotaChildSI aQuota);

    /**
     * initialize the connection or any thing used to work with the database. 
     * 
     * @throws Exception
     */
    public void initialize() throws Exception;

    /**
     * remove a given quota. 
     * 
     * @param aInstance
     * @param aUuid
     */
    public void remove(String aInstance, String aUuid);

    /**
     * unregisterd a quota child for a parent quota. 
     * 
     * @param aQuotaChild
     */
    public void remove(QuotaChildSI aQuotaChild);

    /**
     * update the quota value. 
     * 
     * @param aUuid
     * @param aValue
     * @return
     */
    public long update(String aUuid, Long aValue);

    /**
     * update the quota value to a child quota. 
     * 
     * @param aQuotaChild
     * @return
     */
    public long update(QuotaChildSI aQuotaChild);

    /**
     * check if one quota exist given their uuid. 
     * 
     * @param aUuid
     * @return
     */
    public boolean quotaExist(String aUuid);

    /**
     *  check if one quota exist. 
     * 
     * @param aNameSpace
     * @param aQuotaIdentifier
     * @param aInstance
     * @param aActions
     * @return
     */
    public boolean quotaExist(String aNameSpace, String aQuotaIdentifier,
            String aInstance, String aActions);

    /**
     * get the quota action, action means the token restricted by the quota. 
     * 
     * @param aUuid
     * @return
     */
    public String getActions(String aUuid);

    /**
     * get the quota list for a given quota type. 
     * 
     * @param aQuotaType
     * @return
     */
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType);

    /**
     * return the quotaList by a given quota identifier. 
     * 
     * @param aIdentifier
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier);

    /**
     * return the quota object list by the given parameters
     * 
     * @param aIdentifier
     * @param aNameSpace
     * @param aInstanceType
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByIdentifierNSInstanceType(String aIdentifier,
            String aNameSpace, String aInstanceType);

    /**
     * return a list with all quota that match with the given parameters
     * 
     * @param aQuotaType
     * @param aNs
     * @param aInstance
     * @return
     */
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs, String aInstance);

    /**
     * get the quota uuid. 
     * 
     * @param aQuotaIdentifier
     * @param aNs
     * @param aInstance
     * @param aInstanceType
     * @param aActions
     * @return
     * @throws ExceptionQuotaNotFound
     */
    public String getUuid(String aQuotaIdentifier, String aNs, String aInstance,
            String aInstanceType, String aActions) throws ExceptionQuotaNotFound;

    /**
     * get quota a list of quota by instance type an instance
     * 
     * @param aQuotaType
     * @param aInstance
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType, String aInstance);

    /**
     * get a quota list that match with quota type and namespace. 
     * 
     * @param aQuotaType
     * @param aNs
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType, String aNs);

    /**
     * get quota object instance by uuid 
     * 
     * @param aUuid
     * @return
     */
    public IQuotaSingleInstance getQuotaByUuid(String aUuid);

    /**
     * get a Raw Quota. 
     * 
     * @param aUuid
     * @param aInstance
     * @return
     */
    public Map<String, Object> getRawQuota(String aUuid, String aInstance);

    /**
     * This is and issues only for interval quota.
     * that allow reset the interval reset date. 
     * 
     * @param aUuid
     * @param aResetDate
     */
    public void updateIntervalResetDate(String aUuid, String aResetDate);
}
