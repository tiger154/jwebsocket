/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions.singleIntance;

/**
 *
 * @author osvaldo
 */
public class QuotaCountdownSingleinstance extends QuotaBaseInstance{

    
    public QuotaCountdownSingleinstance(long aValue, String aInstance, String aUuid, 
            String aNamesPace, String aQuotaType, String aInstanceType) {
        super(aValue, aInstance, aUuid, aNamesPace, aQuotaType, aInstanceType);
    }

    
    @Override
    public long getvalue() {
        return super.getvalue(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getInstance() {
        return super.getInstance(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getUuid() {
        return super.getUuid(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getNamespace() {
        return super.getNamespace(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getQuotaType() {
        return super.getQuotaType(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getInstanceType() {
        return super.getInstanceType(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
