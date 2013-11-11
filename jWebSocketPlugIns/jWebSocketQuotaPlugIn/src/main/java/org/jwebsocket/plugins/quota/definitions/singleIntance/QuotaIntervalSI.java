/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions.singleIntance;

import javolution.util.FastMap;
import org.jwebsocket.plugins.quota.definitions.QuotaInterval;

/**
 *
 * @author osvaldo
 */
public class QuotaIntervalSI extends QuotaBaseInstance {
    
    private QuotaInterval.Interval mInterval;
    
    public QuotaIntervalSI( QuotaInterval.Interval aInterval, long aValue, 
            String aInstance, String aUuid, String aNamesPace, 
            String aQuotaType, String aQuotaIdentifier, String aInstanceType) {
        super(aValue, aInstance, aUuid, aNamesPace, aQuotaType, aQuotaIdentifier
             , aInstanceType);
        this.mInterval=aInterval;
       
    }

    @Override
    public FastMap<String, Object> writeToMap() {
        FastMap<String, Object> ltemMap = super.writeToMap(); 
        ltemMap.put("resetDate", );
        
        return ltemMap;
    }
    
    

}
