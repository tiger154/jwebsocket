//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Filter (Community Edition, CE)
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
     *
     * @param aQuota
     * @return
     */
    public boolean save(IQuotaSingleInstance aQuota);

    /**
     *
     * @param aQuota
     * @return
     */
    public boolean save(QuotaChildSI aQuota);

    /**
     *
     * @throws Exception
     */
    public void initialize() throws Exception;

    /**
     *
     * @param aInstance
     * @param aUuid
     */
    public void remove(String aInstance, String aUuid);

    /**
     *
     * @param aQuotaChild
     */
    public void remove(QuotaChildSI aQuotaChild);

    /**
     *
     * @param aUuid
     * @param aValue
     * @return
     */
    public long update(String aUuid, Long aValue);

    /**
     *
     * @param aQuotaChild
     * @return
     */
    public long update(QuotaChildSI aQuotaChild);

    /**
     *
     * @param aUuid
     * @return
     */
    public boolean quotaExist(String aUuid);

    /**
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
     *
     * @param aUuid
     * @return
     */
    public String getActions(String aUuid);

    /**
     *
     * @param aQuotaType
     * @return
     */
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType);

    /**
     *
     * @param aIdentifier
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier);

    /**
     *
     * @param aIdentifier
     * @param aNameSpace
     * @param aInstanceType
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByIdentifierNSInstanceType(String aIdentifier,
            String aNameSpace, String aInstanceType);

    /**
     *
     * @param aQuotaType
     * @param aNs
     * @param aInstance
     * @return
     */
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs, String aInstance);

    /**
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
     *
     * @param aQuotaType
     * @param aInstance
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType, String aInstance);

    /**
     *
     * @param aQuotaType
     * @param aNs
     * @return
     */
    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType, String aNs);

    /**
     *
     * @param aUuid
     * @return
     */
    public IQuotaSingleInstance getQuotaByUuid(String aUuid);

    /**
     *
     * @param aUuid
     * @param aInstance
     * @return
     */
    public Map<String, Object> getRawQuota(String aUuid, String aInstance);

    /**
     *
     * @param aUuid
     * @param aResetDate
     */
    public void updateIntervalResetDate(String aUuid, String aResetDate);
}
