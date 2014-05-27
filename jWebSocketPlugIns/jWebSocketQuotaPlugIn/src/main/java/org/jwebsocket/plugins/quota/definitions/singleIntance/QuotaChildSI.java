//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Child Single Instance (Community Edition, CE)
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

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaChildSI implements ITokenizable {

    long mValue;
    String mInstance;
    String mUuid;
    String mInstanceType;

    /**
     *
     * @param mInstance
     * @param mUuid
     * @param mInstanceType
     */
    public QuotaChildSI(String mInstance, String mUuid, String mInstanceType) {
        this.mInstance = mInstance;
        this.mUuid = mUuid;
        this.mInstanceType = mInstanceType;
        mValue = 0;
    }

    /**
     *
     * @return
     */
    public long getValue() {
        return mValue;
    }

    /**
     *
     * @return
     */
    public String getInstance() {
        return mInstance;
    }

    /**
     *
     * @return
     */
    public String getUuid() {
        return mUuid;
    }

    /**
     *
     * @return
     */
    public String getInstanceType() {
        return mInstanceType;
    }

    /**
     *
     * @param aValue
     */
    public void setValue(long aValue) {
        this.mValue = aValue;
    }

    /**
     *
     * @param aInstance
     */
    public void setInstance(String aInstance) {
        this.mInstance = aInstance;
    }

    /**
     *
     * @param aUuid
     */
    public void setUuid(String aUuid) {
        this.mUuid = aUuid;
    }

    /**
     *
     * @param aInstanceType
     */
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
