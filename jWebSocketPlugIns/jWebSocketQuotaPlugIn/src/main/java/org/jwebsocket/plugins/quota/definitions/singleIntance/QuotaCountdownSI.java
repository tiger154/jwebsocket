//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Countdown Single Instance (Community Edition, CE)
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

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaCountdownSI extends QuotaBaseInstance {

    /**
     *
     * @param aValue
     * @param aInstance
     * @param aUuid
     * @param aNamesPace
     * @param aQuotaType
     * @param aQuotaIdentifier
     * @param aInstanceType
     * @param aActions
     */
    public QuotaCountdownSI(long aValue, String aInstance, String aUuid, String aNamesPace,
            String aQuotaType, String aQuotaIdentifier, String aInstanceType, String aActions) {
        super(aValue, aInstance, aUuid, aNamesPace, aQuotaType, aQuotaIdentifier, aInstanceType, aActions);
    }
}
