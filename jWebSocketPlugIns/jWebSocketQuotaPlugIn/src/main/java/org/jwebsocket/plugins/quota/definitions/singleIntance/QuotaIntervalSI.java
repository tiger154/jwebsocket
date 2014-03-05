/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions.singleIntance;

import javolution.util.FastMap;
import org.jwebsocket.plugins.quota.definitions.QuotaInterval;
import org.jwebsocket.plugins.quota.utils.Interval;

/**
 *
 * @author osvaldo
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
