//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Storage Quota Mongo (Community Edition, CE)
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
package org.jwebsocket.plugins.quota.storage;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.definitions.singleIntance.QuotaChildSI;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class StorageQuotaMongo implements IQuotaStorage {

    Mongo mConnection;
    DB mDBconn;
    String mDatabaseName;
    String mUser;
    String mPassword;
    String mHost;
    Integer mPort;
    DBCollection mCollection;
    DBCollection mCollectionInstance;
    private static final Logger mLog = Logging.getLogger();

    /**
     *
     * @param aUser
     */
    public void setUser(String aUser) {
        this.mUser = aUser;
    }

    /**
     *
     * @return
     */
    public DB getDBconn() {
        return mDBconn;
    }

    /**
     *
     * @param aPassword
     */
    public void setPassword(String aPassword) {
        this.mPassword = aPassword;
    }

    /**
     *
     * @param aHost
     */
    public void setHost(String aHost) {
        this.mHost = aHost;
    }

    /**
     *
     * @param aPort
     */
    public void setPort(Integer aPort) {
        this.mPort = aPort;
    }

    /**
     *
     * @param aDB
     */
    public void setDatabaseName(String aDB) {
        this.mDatabaseName = aDB;
    }

    /**
     *
     * @throws UnknownHostException
     */
    public StorageQuotaMongo() throws UnknownHostException {

    }

    /**
     *
     * @throws Exception
     */
    public void initialize() throws Exception {
        mConnection = new MongoClient(mHost);
        mDBconn = this.mConnection.getDB("quotaPlugin");
        mCollection = mDBconn.getCollection("quota");
        mCollectionInstance = mDBconn.getCollection("quotainstance");
    }

    /**
     *
     * @param aQuota
     * @return
     */
    @Override
    public boolean save(IQuotaSingleInstance aQuota) {

        try {
            BasicDBObject lDoc = new BasicDBObject();
            FastMap<String, Object> ltemMap = aQuota.writeToMap();

            for (Map.Entry<String, Object> entry : ltemMap.entrySet()) {
                String string = entry.getKey();
                Object object = entry.getValue();

                lDoc.put(string, object);
            }
            mCollection.insert(lDoc);
        } catch (Exception e) {
            return false;
        }
        return true;

    }

//remove
    /**
     *
     * @param aChildSI
     * @return
     */
    @Override
    public boolean save(QuotaChildSI aChildSI) {

        try {
            BasicDBObject lDoc = new BasicDBObject();

            lDoc.put("uuidquota", aChildSI.getUuid());
            lDoc.put("instance", aChildSI.getInstance());
            lDoc.put("instanceType", aChildSI.getInstanceType());
            lDoc.put("value", aChildSI.getValue());

            mCollectionInstance.insert(lDoc);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param aUuid
     * @param aInstance
     */
    @Override
    public void remove(String aUuid, String aInstance) {

        BasicDBObject lWhere = new BasicDBObject();
        lWhere.put("uuid", aUuid);
        lWhere.put("instance", aInstance);
        mCollection.remove(lWhere);
        lWhere = new BasicDBObject();
        lWhere.put("uuidquota", aUuid);
        mCollectionInstance.remove(lWhere);
    }

    /**
     *
     * @param aQuotaChild
     */
    @Override
    public void remove(QuotaChildSI aQuotaChild) {
        BasicDBObject lWhere = new BasicDBObject();
        lWhere.put("uuidquota", aQuotaChild.getUuid());
        lWhere.put("instance", aQuotaChild.getInstance());
        mCollectionInstance.remove(lWhere);

    }

    /**
     *
     * @param aUuid
     * @param aValue
     * @return
     */
    @Override
    public long update(String aUuid, Long aValue) {

        BasicDBObject lWhere = new BasicDBObject();
        BasicDBObject lSetValue = new BasicDBObject();
        lWhere.put("uuid", aUuid);
        lSetValue.append("$set", new BasicDBObject().append("value", aValue));
        DBObject lObj = mCollection.findOne(lWhere);
        mCollection.update(lWhere, lSetValue);
        return aValue;

    }

    /**
     *
     * @param aChildSI
     * @return
     */
    @Override
    public long update(QuotaChildSI aChildSI) {

        BasicDBObject lWhere = new BasicDBObject();
        BasicDBObject lSetValue = new BasicDBObject();
        lWhere.put("uuidquota", aChildSI.getUuid());
        lWhere.put("instance", aChildSI.getInstance());
        lSetValue.append("$set", new BasicDBObject().append("value", aChildSI.getValue()));
        mCollectionInstance.update(lWhere, lSetValue);
        return aChildSI.getValue();

    }

    /**
     *
     * @param aUuid
     * @return
     */
    @Override
    public String getActions(String aUuid) {

        String lAction = "*";
        if (quotaExist(aUuid)) {
            BasicDBObject lQuery = new BasicDBObject();
            lQuery.put("uuid", aUuid);

            DBObject lObj = mCollection.findOne(lQuery);
            lAction = lObj.get("actions").toString();
        }
        return lAction;
    }

    /**
     *
     * @param aQuotaType
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType) {

        FastList<IQuotaSingleInstance> lResult;
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        DBCursor lCur = mCollection.find(lQuery);

        lResult = getListInstance(lCur);
        return lResult;
    }

    /**
     *
     * @param aIdentifier
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier) {

        FastList<IQuotaSingleInstance> lResult;
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaIdentifier", aIdentifier);
        DBCursor lCur = mCollection.find(lQuery);
        lResult = getListInstance(lCur);

        return lResult;
    }

    /**
     *
     * @param aIdentifier
     * @param aNameSpace
     * @param aInstanceType
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByIdentifierNSInstanceType(String aIdentifier,
            String aNameSpace, String aInstanceType) {

        FastList<IQuotaSingleInstance> lResult;
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaIdentifier", aIdentifier);
        lQuery.put("instanceType", aInstanceType);
        lQuery.put("ns", aNameSpace);

        DBCursor lCur = mCollection.find(lQuery);

        lResult = getListInstance(lCur);

        return lResult;
    }

    /**
     *
     * @param aQuotaType
     * @param aNs
     * @param aInstance
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs,
            String aInstance) {
        FastList<IQuotaSingleInstance> lResult;
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        lQuery.put("instance", aInstance);
        lQuery.put("ns", aNs);
        DBCursor lCur = mCollection.find(lQuery);

        lResult = getListInstance(lCur);
        return lResult;
    }

    /**
     *
     * @param aQuotaIdentifier
     * @param aNameSpace
     * @param aInstance
     * @param aInstanceType
     * @param aActions
     * @return
     * @throws ExceptionQuotaNotFound
     */
    @Override
    public String getUuid(String aQuotaIdentifier, String aNameSpace, String aInstance,
            String aInstanceType, String aActions) throws ExceptionQuotaNotFound {

        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaIdentifier", aQuotaIdentifier);
        lQuery.put("ns", aNameSpace);
        lQuery.put("instance", aInstance);
        lQuery.put("instanceType", aInstanceType);
        lQuery.put("actions", aActions);

        DBObject lResponse = mCollection.findOne(lQuery);
        String lUuid;
        lUuid = lResponse.get("uuid").toString();

        if (null != lUuid) {
            lUuid = lResponse.get("uuid").toString();
        } else {
            throw new ExceptionQuotaNotFound("not found");
        }
        return lUuid;
    }

    /**
     *
     * @param aQuotaType
     * @param aInstance
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType,
            String aInstance) {
        FastList<IQuotaSingleInstance> lResult;
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        lQuery.put("instance", aInstance);
        DBCursor lCur = mCollection.find(lQuery);
        lResult = getListInstance(lCur);

        return lResult;
    }

    /**
     *
     * @param aQuotaType
     * @param aNs
     * @return
     */
    @Override
    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType,
            String aNs) {
        FastList<IQuotaSingleInstance> lResult;
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        lQuery.put("ns", aNs);
        DBCursor lCur = mCollection.find(lQuery);
        lResult = getListInstance(lCur);

        return lResult;
    }

    /**
     *
     * @param aUuid
     * @return
     */
    @Override
    public boolean quotaExist(String aUuid) {
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("uuid", aUuid);

        DBCursor lCur = mCollection.find(lQuery);
        if (lCur.hasNext()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param aNameSpace
     * @param aQuotaIdentifier
     * @param aInstance
     * @param aActions
     * @return
     */
    @Override
    public boolean quotaExist(String aNameSpace, String aQuotaIdentifier,
            String aInstance, String aActions) {
        BasicDBObject lQuery = new BasicDBObject();

        lQuery.put("ns", aNameSpace);
        lQuery.put("instance", aInstance);
        lQuery.put("quotaIdentifier", aQuotaIdentifier);

        DBCursor lCur = mCollection.find(lQuery);
        if (lCur.hasNext()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param aUuid
     * @return
     */
    @Override
    public IQuotaSingleInstance getQuotaByUuid(String aUuid) {

        IQuotaSingleInstance lSingle = null;
        if (quotaExist(aUuid)) {
            BasicDBObject lQuery = new BasicDBObject();
            lQuery.put("uuid", aUuid);

            DBObject lObj = mCollection.findOne(lQuery);

            lSingle = getSingleInstance(lObj);
        }
        return lSingle;

    }

    private FastList<IQuotaSingleInstance> getListInstance(DBCursor aQuotas) {

        FastList<IQuotaSingleInstance> lResult = new FastList<IQuotaSingleInstance>();

        while (aQuotas.hasNext()) {
            DBObject lObjInstance = aQuotas.next();

            IQuotaSingleInstance lQuota = getSingleInstance(lObjInstance);
            lResult.add(lQuota);
        }
        return lResult;
    }

    private IQuotaSingleInstance getSingleInstance(DBObject aObjQuota) {

        IQuotaSingleInstance lQuota;
        if (!aObjQuota.isPartialObject()) {
            lQuota = QuotaHelper.factorySingleInstance(
                    Long.parseLong(aObjQuota.get("value").toString()),
                    aObjQuota.get("instance").toString(),
                    aObjQuota.get("uuid").toString(),
                    aObjQuota.get("ns").toString(),
                    aObjQuota.get("quotaType").toString(),
                    aObjQuota.get("quotaIdentifier").toString(),
                    aObjQuota.get("instanceType").toString(),
                    aObjQuota.get("actions").toString());
        } else {
            return null;
        }

        //Getting child quota.
        BasicDBObject lObject = new BasicDBObject();
        lObject.put("uuidquota", aObjQuota.get("uuid"));
        DBCursor lDBQuota = mCollectionInstance.find(lObject);

        while (lDBQuota.hasNext()) {
            DBObject lObjInstance = lDBQuota.next();
            //adding quota Child of this quota.
            QuotaChildSI lChild = new QuotaChildSI(lObjInstance.get("instance").toString(),
                    aObjQuota.get("uuid").toString(), lObjInstance.get("instanceType").toString());

            lChild.setValue(Long.parseLong(lObjInstance.get("value").toString()));
            lQuota.addChildQuota(lChild);
        }
        return lQuota;
    }

    /**
     *
     * @param aUuid
     * @param aInstance
     * @return
     */
    @Override
    public Map<String, Object> getRawQuota(String aUuid, String aInstance) {

        BasicDBObject lObject = new BasicDBObject();
        BasicDBObject lObjectInstance = new BasicDBObject();
        lObject.put("uuid", aUuid);
        lObjectInstance.put("uuidquota", aUuid);
        lObjectInstance.put("instance", aInstance);
        DBObject lQuery = mCollection.findOne(lObject);

        DBObject lQueryInstance = mCollectionInstance.findOne(lObjectInstance);
        Map<String, Object> lMap = lQuery.toMap();
        lMap.put("instance", lQueryInstance.get("instance"));
        lMap.put("instanceType", lQueryInstance.get("instanceType"));
        return lMap;
    }

    // to see for change the method's name
    /**
     *
     * @param aUuid
     * @param aResetDate
     */
    @Override
    public void updateIntervalResetDate(String aUuid, String aResetDate) {
        BasicDBObject lWhere = new BasicDBObject();
        BasicDBObject lSetValue = new BasicDBObject();
        lWhere.put("uuid", aUuid);
        lSetValue.append("$set", new BasicDBObject().append("resetDate", aResetDate));
        mCollection.update(lWhere, lSetValue);
    }
}
