//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Base Instance (Community Edition, CE)
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

import java.util.Iterator;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.token.Token;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaBaseInstance implements IQuotaSingleInstance, ITokenizable {

    /**
     *
     */
    protected long mValue;

    /**
     *
     */
    protected String mInstance;

    /**
     *
     */
    protected String mUuid;

    /**
     *
     */
    protected String mNamesPace;

    /**
     *
     */
    protected String mActions;

    /**
     *
     */
    protected String mQuotaType;

    /**
     *
     */
    protected String mInstanceType;

    /**
     *
     */
    protected String mQuotaIdentifier;

    /**
     *
     */
    protected FastList<QuotaChildSI> mChildQuotaList;

    /**
     *
     * @param aChildQuotaList
     */
    public void setChildQuotaList(FastList<QuotaChildSI> aChildQuotaList) {
        this.mChildQuotaList = aChildQuotaList;
    }

    /**
     *
     * @return
     */
    public FastList<QuotaChildSI> getChildQuotaList() {

        if (getInstanceType().equals("User")) {

            for (Iterator<QuotaChildSI> it = mChildQuotaList.iterator(); it.hasNext();) {
                QuotaChildSI lQuotaChild = it.next();

                lQuotaChild.setValue(this.getvalue());
            }
        }
        return mChildQuotaList;
    }

    /**
     *
     * @param aChildQuota
     * @return
     */
    @Override
    public boolean addChildQuota(QuotaChildSI aChildQuota) {

        //Looking for the quota child already exist
        if (getChildQuota(aChildQuota.getInstance()) == null) {
            mChildQuotaList.add(aChildQuota);
            return true;
        }
        return false;
    }

    /**
     *
     * @param aInstance
     * @return
     */
    @Override
    public QuotaChildSI getChildQuota(String aInstance) {

        for (Iterator<QuotaChildSI> it = mChildQuotaList.iterator(); it.hasNext();) {
            QuotaChildSI lQuotaChild = it.next();

            if (lQuotaChild.getInstance().equals(aInstance)) {
                /**
                 * if the quota parent is a User quota, the quota take the
                 * father quota value, because the value is shared between all
                 * quota child.
                 */
                if (mInstanceType.equals("User")) {
                    lQuotaChild.setValue(mValue);
                }
                return lQuotaChild;
            }
        }
        return null;
    }

    @Override
    public void writeToToken(Token aToken) {
        aToken.setString("uuid", mUuid);
        aToken.setString("instance", mInstance);
        aToken.setString("namespace", mNamesPace);
        aToken.setLong("value", mValue);
        aToken.setString("actions", mActions);
        aToken.setString("quotaType", mQuotaType);
        aToken.setString("instance_type", mInstanceType);
        aToken.setString("identifier", mQuotaIdentifier);
        aToken.setList("childQuotas", getChildQuotaList());
    }

    /**
     *
     * @return
     */
    @Override
    public String getActions() {
        return mActions;
    }

    /**
     *
     * @return
     */
    @Override
    public FastMap<String, Object> writeToMap() {
        FastMap<String, Object> lMap = new FastMap<String, Object>();

        lMap.put("uuid", mUuid);
        lMap.put("ns", mNamesPace);
        lMap.put("quotaType", mQuotaType);
        lMap.put("quotaIdentifier", mQuotaIdentifier);
        lMap.put("value", mValue);
        lMap.put("actions", mActions);
        lMap.put("instance", mInstance);
        lMap.put("instanceType", mInstanceType);

        return lMap;
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
        if ((this.mQuotaIdentifier == null) ? (other.mQuotaIdentifier != null) : !this.mQuotaIdentifier.equals(other.mQuotaIdentifier)) {
            return false;
        }
        return true;
    }

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
    public QuotaBaseInstance(long aValue, String aInstance, String aUuid, String aNamesPace,
            String aQuotaType, String aQuotaIdentifier, String aInstanceType, String aActions) {
        this.mValue = aValue;
        this.mInstance = aInstance;
        this.mUuid = aUuid;
        this.mNamesPace = aNamesPace;
        this.mQuotaType = aQuotaType;
        this.mQuotaIdentifier = aQuotaIdentifier;
        this.mInstanceType = aInstanceType;
        this.mActions = aActions;
        mChildQuotaList = new FastList<QuotaChildSI>();
    }

    /**
     *
     * @param mValue
     */
    public void setValue(long mValue) {
        this.mValue = mValue;
    }

    /**
     *
     * @param mInstance
     */
    public void setInstance(String mInstance) {
        this.mInstance = mInstance;
    }

    /**
     *
     * @param mUuid
     */
    public void setUuid(String mUuid) {
        this.mUuid = mUuid;
    }

    /**
     *
     * @param mNamesPace
     */
    public void setNamesPace(String mNamesPace) {
        this.mNamesPace = mNamesPace;
    }

    /**
     *
     * @param mQuotaType
     */
    public void setQuotaType(String mQuotaType) {
        this.mQuotaType = mQuotaType;
    }

    /**
     *
     * @param mInstanceType
     */
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

    /**
     *
     * @return
     */
    @Override
    public String getQuotaIdentifier() {
        return mQuotaIdentifier;
    }
}
