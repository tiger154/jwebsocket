/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions.singleIntance;

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 *
 * @author osvaldo
 */
public class QuotaChildSI implements ITokenizable {

    long mValue;
    String mInstance;
    String mUuid;
    String mInstanceType;

    public QuotaChildSI(String mInstance, String mUuid, String mInstanceType) {
        this.mInstance = mInstance;
        this.mUuid = mUuid;
        this.mInstanceType = mInstanceType;
        mValue = 0;
    }

    public long getValue() {
        return mValue;
    }

    public String getInstance() {
        return mInstance;
    }

    public String getUuid() {
        return mUuid;
    }

    public String getInstanceType() {
        return mInstanceType;
    }

    public void setValue(long aValue) {
        this.mValue = aValue;
    }

    public void setInstance(String aInstance) {
        this.mInstance = aInstance;
    }

    public void setUuid(String aUuid) {
        this.mUuid = aUuid;
    }

    public void setInstanceType(String aInstanceType) {
        this.mInstanceType = aInstanceType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QuotaBaseInstance other = (QuotaBaseInstance) obj;

        if ((this.mInstance == null) ? (other.mInstance != null) : !this.mInstance.equals(other.mInstance)) {
            return false;
        }
        if ((this.mUuid == null) ? (other.mUuid != null) : !this.mUuid.equals(other.mUuid)) {
            return false;
        }

        if ((this.mInstanceType == null) ? (other.mInstanceType != null) : !this.mInstanceType.equals(other.mInstanceType)) {
            return false;
        }
        return true;
    }

    @Override
    public void writeToToken(Token aToken) {
        aToken.setString("q_uuid", mUuid);
        aToken.setString("q_instance", mInstance);
        aToken.setLong("q_value", mValue);
        aToken.setString("q_instance_type", mInstanceType);
    }

    @Override
    public void readFromToken(Token aToken) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
