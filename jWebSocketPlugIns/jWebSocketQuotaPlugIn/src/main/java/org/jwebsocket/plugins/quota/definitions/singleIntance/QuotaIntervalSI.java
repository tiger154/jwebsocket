//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Interval Single Instance (Community Edition, CE)
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
package org.jwebsocket.plugins.quota.definitions.singleIntance;

import javolution.util.FastMap;
import org.jwebsocket.plugins.quota.utils.Interval;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaIntervalSI extends QuotaBaseInstance {

    private Interval mInterval;

    /**
     *
     * @param aInterval
     * @param aValue
     * @param aInstance
     * @param aUuid
     * @param aNamesPace
     * @param aQuotaType
     * @param aQuotaIdentifier
     * @param aInstanceType
     * @param aActions
     */
    public QuotaIntervalSI(Interval aInterval, long aValue,
            String aInstance, String aUuid, String aNamesPace,
            String aQuotaType, String aQuotaIdentifier, String aInstanceType, String aActions) {
        super(aValue, aInstance, aUuid, aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
        this.mInterval = aInterval;

    }

    /**
     *
     * @return
     */
    @Override
    public FastMap<String, Object> writeToMap() {
        FastMap<String, Object> ltemMap = super.writeToMap();
        ltemMap.put("maxValue", mValue);
        ltemMap.put("resetDate", mInterval.toResetDate());
        return ltemMap;
    }
}
