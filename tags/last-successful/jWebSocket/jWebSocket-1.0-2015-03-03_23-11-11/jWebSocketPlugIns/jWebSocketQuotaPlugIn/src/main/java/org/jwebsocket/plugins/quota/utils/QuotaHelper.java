//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Helper (Community Edition, CE)
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
package org.jwebsocket.plugins.quota.utils;

import java.util.UUID;
import javolution.util.FastList;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaBaseInstance;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaCountdownSI;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaHelper {

    /**
     * method used to generate a uuid for each new parent quota. 
     * 
     * @return
     */
    public static String generateQuotaUUID() {
        return Tools.getMD5(UUID.randomUUID().toString());
    }

    /**
     * return a quota Single object for a quota, given all the quota attributes
     * 
     * @param aValue
     * @param aInstance
     * @param aUuid
     * @param aNamesPace
     * @param aQuotaType
     * @param aQuotaIdentifier
     * @param aInstanceType
     * @param aActions
     * @return
     */
    public static IQuotaSingleInstance factorySingleInstance(long aValue, String aInstance,
            String aUuid, String aNamesPace, String aQuotaType, String aQuotaIdentifier,
            String aInstanceType, String aActions) {

        if (aQuotaType.equals("CountDown")) {
            return new QuotaCountdownSI(aValue, aInstance, aUuid,
                    aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
        } else {
            return new QuotaBaseInstance(aValue, aInstance, aUuid, aNamesPace,
                    aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
        }

    }

    /**
     * return a ingnored users by the quota plugin. 
     * 
     * @return
     */
    public static FastList<String> ignoredUsers() {
        FastList<String> lIgnoredUsers = new FastList<String>();
        lIgnoredUsers.add("anonymous");
        return lIgnoredUsers;
    }
}
