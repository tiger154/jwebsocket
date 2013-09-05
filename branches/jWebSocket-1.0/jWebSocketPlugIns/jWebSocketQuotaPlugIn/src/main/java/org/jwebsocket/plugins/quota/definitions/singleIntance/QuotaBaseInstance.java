/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.definitions.singleIntance;

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.token.Token;

/**
 *
 * @author osvaldo
 */
public class QuotaBaseInstance implements IQuotaSingleInstance, ITokenizable {

    long mValue;
    String mInstance;
    String mUuid;
    String mNamesPace;
    String mQuotaType;
    String mInstanceType;

    @Override
    public void writeToToken(Token aToken) {
        aToken.setString("q_uuid", mUuid );
        aToken.setString("q_instance", mInstance  );
        aToken.setString("q_namespace", mNamesPace);
        aToken.setLong("q_value", mValue );
        aToken.setString("q_type", mQuotaType );
        aToken.setString("q_instance_type", mInstanceType );
    }

    @Override
    public void readFromToken(Token aToken) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
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
        if (this.mValue != other.mValue) {
            return false;
        }
        if ((this.mInstance == null) ? (other.mInstance != null) : !this.mInstance.equals(other.mInstance)) {
            return false;
        }
        if ((this.mUuid == null) ? (other.mUuid != null) : !this.mUuid.equals(other.mUuid)) {
            return false;
        }
        if ((this.mNamesPace == null) ? (other.mNamesPace != null) : !this.mNamesPace.equals(other.mNamesPace)) {
            return false;
        }
        if ((this.mQuotaType == null) ? (other.mQuotaType != null) : !this.mQuotaType.equals(other.mQuotaType)) {
            return false;
        }
        if ((this.mInstanceType == null) ? (other.mInstanceType != null) : !this.mInstanceType.equals(other.mInstanceType)) {
            return false;
        }
        return true;
    }

    public QuotaBaseInstance(long aValue, String aInstance, String aUuid, String aNamesPace, String aQuotaType, String aInstanceType) {
        this.mValue = aValue;
        this.mInstance = aInstance;
        this.mUuid = aUuid;
        this.mNamesPace = aNamesPace;
        this.mQuotaType = aQuotaType;
        this.mInstanceType = aInstanceType;
    }

    public void setValue(long mValue) {
        this.mValue = mValue;
    }

    public void setInstance(String mInstance) {
        this.mInstance = mInstance;
    }

    public void setUuid(String mUuid) {
        this.mUuid = mUuid;
    }

    public void setNamesPace(String mNamesPace) {
        this.mNamesPace = mNamesPace;
    }

    public void setQuotaType(String mQuotaType) {
        this.mQuotaType = mQuotaType;
    }

    public void setInstanceType(String mInstanceType) {
        this.mInstanceType = mInstanceType;
    }

    @Override
    public long getvalue() {
        return this.mValue;
    }

    @Override
    public String getInstance() {
        return mInstance;
    }

    @Override
    public String getUuid() {
        return mUuid;
    }

    @Override
    public String getNamespace() {
        return mNamesPace;
    }

    @Override
    public String getQuotaType() {
        return mQuotaType;
    }

    @Override
    public String getInstanceType() {
        return mInstanceType;
    }
}
