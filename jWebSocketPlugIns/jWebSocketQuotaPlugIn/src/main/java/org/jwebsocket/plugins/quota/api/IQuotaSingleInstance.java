//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket IQuota Single Instance (Community Edition, CE)
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

import javolution.util.FastMap;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaChildSI;
import org.jwebsocket.token.Token;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public interface IQuotaSingleInstance {

    /**
     * the quota value
     * @return 
     */
    public long getvalue();

    /**
     * Return the Instance owner of the quota admin, administrator, sms-app, or
     * x-module
     *
     * @return
     */
    public String getInstance();

    /**
     * the quota unique ID
     * @return 
     */
    public String getUuid();

    /**
     * the namespace of the feature that the quota is apply to
     * @return 
     */
    public String getNamespace();

    /**
     *  the quota type
     * @return
     */
    public String getQuotaType();

    /**
     * The type of the Instance (e.g) user, gruop of users, app or module
     *
     * @return
     */
    public String getInstanceType();

    /**
     * add a registered quota to the quota child list of each parent quota
     * 
     * @param aChildQuota
     * @return
     */
    public boolean addChildQuota(QuotaChildSI aChildQuota);

    /**
     * aeach quota object instance has their own quota list that are the 
     * quota registed to this quota. This method return the quota child list
     * 
     * @param aInstance
     * @return
     */
    public QuotaChildSI getChildQuota(String aInstance);

    /**
     * retun the quota identifier
     * 
     * @return
     */
    public String getQuotaIdentifier();

    /**
     * return the quota action.
     * @return
     */
    public String getActions();

    /**
     * write the quota to their token represantation. 
     * 
     * @param lAuxToken
     */
    public void writeToToken(Token lAuxToken);

    /**
     * Write the quota to their map representation.
     *  
     * @return
     */
    public FastMap<String, Object> writeToMap();
}
