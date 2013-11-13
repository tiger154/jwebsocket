/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.storage;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.plugins.quota.api.IQuotaSingleInstance;
import org.jwebsocket.plugins.quota.api.IQuotaStorage;
import org.jwebsocket.plugins.quota.utils.QuotaHelper;
import org.jwebsocket.plugins.quota.utils.exception.ExceptionQuotaNotFound;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author osvaldo
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

    public void setUser(String aUser) {
        this.mUser = aUser;
    }

    public DB getDBconn() {
        return mDBconn;
    }

    public void setPassword(String aPassword) {
        this.mPassword = aPassword;
    }

    public void setHost(String aHost) {
        this.mHost = aHost;
    }

    public void setPort(Integer aPort) {
        this.mPort = aPort;
    }

    public void setDatabaseName(String aDB) {
        this.mDatabaseName = aDB;
    }

    public StorageQuotaMongo() throws UnknownHostException {

        /*TODO Aqui le paso los valores en bruto por que tengo que pasarlos
         * por via constructor de spring y no por setter para que esten dispo
         * nibles en el constructor
         * 
         */
        mConnection = new Mongo(mHost);
        mDBconn = this.mConnection.getDB("quotaPlugin");
        mCollection = mDBconn.getCollection("quota");
        mCollectionInstance = mDBconn.getCollection("quotaInstance");

    }

    @Override
    public void save(IQuotaSingleInstance aQuota) {

        BasicDBObject lDoc = new BasicDBObject();
        FastMap<String, Object> ltemMap = aQuota.writeToMap();

        for (Map.Entry<String, Object> entry : ltemMap.entrySet()) {
            String string = entry.getKey();
            Object object = entry.getValue();

            lDoc.put(string, object);
        }
        mCollection.insert(lDoc);

        lDoc = new BasicDBObject();

        lDoc.put("uuidQuota", aQuota.getUuid());
        lDoc.put("instance", aQuota.getInstance());
        lDoc.put("instanceType", aQuota.getInstanceType());
        mCollectionInstance.insert(lDoc);

    }

//remove
    @Override
    public boolean save(String aUuid, String aInstance, String aInstanceType) {
        BasicDBObject lDoc = new BasicDBObject();

        lDoc.put("uuidQuota", aUuid);
        lDoc.put("instance", aInstance);
        lDoc.put("instanceType", aInstanceType);
        DBCursor lCur = mCollectionInstance.find(lDoc);
        if (!lCur.hasNext()) {
            mCollectionInstance.insert(lDoc);
            return true;
        }
        return false;
    }

    @Override
    public void remove(String aUuid, String aInstance) {

        BasicDBObject lDoc = new BasicDBObject();
        lDoc.put("uuidQuota", aUuid);
        lDoc.put("instance", aInstance);
        mCollectionInstance.remove(lDoc);
        lDoc = new BasicDBObject();
        lDoc.put("uuidQuota", aUuid);
        DBCursor lCur = mCollectionInstance.find(lDoc);
        if (!lCur.hasNext()) {
            lDoc = new BasicDBObject();
            lDoc.put("uuid", aUuid);
            mCollection.remove(lDoc);
        }
    }

    @Override
    public long update(String aUuid, Long aValue) {

        BasicDBObject lWhere = new BasicDBObject();
        BasicDBObject lSetValue = new BasicDBObject();
        lWhere.put("uuid", aUuid);
        lSetValue.append("$set", new BasicDBObject().append("value", aValue));
        mCollection.update(lWhere, lSetValue);
        return aValue;

    }
// to see use quotaType or QuotaIdentifier

    @Override
    public List<String> getAllQuotaUuid(String aQuotaType) {

        FastList<String> lResult = new FastList<String>();
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        DBCursor lCur = mCollection.find(lQuery);
        while (lCur.hasNext()) {
            DBObject lObj = lCur.next();
            String lUuid = lObj.get("uuid").toString();
            lResult.add(lUuid);
        }
        return lResult;
    }

    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType) {


        FastList<IQuotaSingleInstance> lResult =
                new FastList<IQuotaSingleInstance>();
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        DBCursor lCur = mCollection.find(lQuery);
        while (lCur.hasNext()) {
            DBObject lObj = lCur.next();

            FastList<IQuotaSingleInstance> lAux = getListInstance(lObj);
            for (Iterator<IQuotaSingleInstance> lQuotaIt = lAux.iterator();
                    lQuotaIt.hasNext();) {
                lResult.add(lQuotaIt.next());
            }
        }
        return lResult;
    }

    @Override
    public List<IQuotaSingleInstance> getQuotasByIdentifier(String aIdentifier) {


        FastList<IQuotaSingleInstance> lResult =
                new FastList<IQuotaSingleInstance>();
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaIdentifier", aIdentifier);
        DBCursor lCur = mCollection.find(lQuery);
        while (lCur.hasNext()) {
            DBObject lObj = lCur.next();

            FastList<IQuotaSingleInstance> lAux = getListInstance(lObj);
            for (Iterator<IQuotaSingleInstance> lQuotaIt = lAux.iterator();
                    lQuotaIt.hasNext();) {
                lResult.add(lQuotaIt.next());
            }
        }
        return lResult;
    }

    @Override
    public List<IQuotaSingleInstance> getQuotas(String aQuotaType, String aNs,
            String aInstance) {
        FastList<IQuotaSingleInstance> lResult =
                new FastList<IQuotaSingleInstance>();
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        lQuery.put("ns", aNs);
        DBCursor lCur = mCollection.find(lQuery);

        while (lCur.hasNext()) {
            DBObject lObj = lCur.next();
            FastList<IQuotaSingleInstance> lAux =
                    getListInstance(lObj, aInstance);

            lResult.addAll(lAux);
            /*for (Iterator<IQuotaSingleInstance> lQuotaIt = lAux.iterator();
             lQuotaIt.hasNext();) {
             lResult.add(lQuotaIt.next());
             }*/
        }
        return lResult;
    }

    @Override
    public String getUuid(String aQuotaIdentifier, String aNs, String aInstance,
            String aInstanceType) throws ExceptionQuotaNotFound {

        boolean lFlag = false;
        String lResult = null;
        BasicDBObject lQuery = new BasicDBObject();

        lQuery.put("quotaIdentifier", aQuotaIdentifier);
        lQuery.put("ns", aNs);

        DBCursor lCur = mCollection.find(lQuery);

        BasicDBObject lSubQuery = new BasicDBObject();
        while (lCur.hasNext()) {
            DBObject lObj = lCur.next();

            String lUuid = lObj.get("uuid").toString();

            lSubQuery.put("instance", aInstance);
            lSubQuery.put("instanceType", aInstanceType);
            lSubQuery.put("uuidQuota", lUuid);

            DBCursor lSubCur = mCollectionInstance.find(lSubQuery);

            if (lSubCur.hasNext()) {
                lResult = lUuid;
                lFlag = true;
                break;
            }
        }
        if (lFlag == false) {
            throw new ExceptionQuotaNotFound("not found");
        }
        return lResult;


    }

    @Override
    public List<IQuotaSingleInstance> getQuotasByInstance(String aQuotaType,
            String aInstance) {
        FastList<IQuotaSingleInstance> lResult =
                new FastList<IQuotaSingleInstance>();
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        DBCursor lCur = mCollection.find(lQuery);

        while (lCur.hasNext()) {
            DBObject lObj = lCur.next();
            FastList<IQuotaSingleInstance> lAux =
                    getListInstance(lObj, aInstance);
            for (Iterator<IQuotaSingleInstance> lQuotaIt = lAux.iterator();
                    lQuotaIt.hasNext();) {
                lResult.add(lQuotaIt.next());
            }
        }
        return lResult;
    }

    @Override
    public List<IQuotaSingleInstance> getQuotasByNs(String aQuotaType,
            String aNs) {
        FastList<IQuotaSingleInstance> lResult =
                new FastList<IQuotaSingleInstance>();
        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("quotaType", aQuotaType);
        lQuery.put("ns", aNs);
        DBCursor lCur = mCollection.find(lQuery);
        while (lCur.hasNext()) {
            DBObject lObj = lCur.next();
            FastList<IQuotaSingleInstance> lAux = getListInstance(lObj);
            for (Iterator<IQuotaSingleInstance> lQuotaIt = lAux.iterator();
                    lQuotaIt.hasNext();) {
                lResult.add(lQuotaIt.next());
            }
        }

        return lResult;
    }

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

    @Override
    public boolean quotaExist(String aNameSpace, String aIdentifier,
            String aInstance) {

        BasicDBObject lQuery = new BasicDBObject();
        lQuery.put("ns", aNameSpace);
        lQuery.put("quotaIdentifier", aIdentifier);

        DBCursor lCur = mCollection.find(lQuery);
        if (lCur.hasNext()) {
            while (lCur.hasNext()) {
                DBObject obj = lCur.next();
                FastList<IQuotaSingleInstance> lAux =
                        getListInstance(obj, aInstance);
                if (!lAux.isEmpty()) {
                    return true;
                }
            }

        }
        return false;
    }

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

    private FastList<IQuotaSingleInstance> getListInstance(DBObject aObjQuota) {
        BasicDBObject lObject = new BasicDBObject();
        lObject.put("uuidQuota", aObjQuota.get("uuid"));

        DBCursor lDBQuota = mCollectionInstance.find(lObject);
        FastList<IQuotaSingleInstance> lResult =
                new FastList<IQuotaSingleInstance>();

        while (lDBQuota.hasNext()) {

            DBObject lObjInstance = lDBQuota.next();
            IQuotaSingleInstance lQuota = QuotaHelper.factorySingleInstance(
                    Long.parseLong(aObjQuota.get("value").toString()),
                    lObjInstance.get("instance").toString(),
                    lObjInstance.get("uuidQuota").toString(),
                    aObjQuota.get("ns").toString(),
                    aObjQuota.get("quotaType").toString(),
                    aObjQuota.get("quotaIdentifier").toString(),
                    lObjInstance.get("instanceType").toString());
            lResult.add(lQuota);
        }

        return lResult;
    }

    private FastList<IQuotaSingleInstance> getListInstance(DBObject aObjQuota,
            String aInstance) {
        BasicDBObject lObject = new BasicDBObject();
        lObject.put("uuidQuota", aObjQuota.get("uuid"));
        lObject.put("instance", aInstance);
        DBCursor lDBQuota = mCollectionInstance.find(lObject);
        FastList<IQuotaSingleInstance> lResult =
                new FastList<IQuotaSingleInstance>();
        while (lDBQuota.hasNext()) {
            DBObject lObjInstance = lDBQuota.next();
            IQuotaSingleInstance lQuota = QuotaHelper.factorySingleInstance(
                    Long.parseLong(aObjQuota.get("value").toString()),
                    lObjInstance.get("instance").toString(),
                    lObjInstance.get("uuidQuota").toString(),
                    aObjQuota.get("ns").toString(),
                    aObjQuota.get("quotaType").toString(),
                    aObjQuota.get("quotaIdentifier").toString(),
                    lObjInstance.get("instanceType").toString());
            lResult.add(lQuota);
        }

        return lResult;
    }

    private IQuotaSingleInstance getSingleInstance(DBObject aObjQuota) {
        BasicDBObject lObject = new BasicDBObject();
        lObject.put("uuidQuota", aObjQuota.get("uuid"));
        DBObject lQuery = mCollectionInstance.findOne(lObject);
        IQuotaSingleInstance lQuota = null;
        if (!lQuery.isPartialObject()) {
            lQuota = QuotaHelper.factorySingleInstance(
                    Long.parseLong(aObjQuota.get("value").toString()),
                    lQuery.get("instance").toString(),
                    lQuery.get("uuidQuota").toString(),
                    aObjQuota.get("ns").toString(),
                    aObjQuota.get("quotaType").toString(),
                    aObjQuota.get("quotaIdentifier").toString(),
                    lQuery.get("instanceType").toString());
        }
        return lQuota;
    }

    @Override
    public Map<String, Object> getRawQuota(String aUuid, String aInstance) {

        BasicDBObject lObject = new BasicDBObject();
        BasicDBObject lObjectInstance = new BasicDBObject();
        lObject.put("uuid", aUuid);
        lObjectInstance.put("uuidQuota", aUuid);
        lObjectInstance.put("instance", aInstance);
        DBObject lQuery = mCollection.findOne(lObject);
        DBObject lQueryInstance = mCollectionInstance.findOne(lObjectInstance);
        Map<String, Object> lMap = lQuery.toMap();
        lMap.put("instance", lQueryInstance.get("instance"));
        lMap.put("instanceType", lQueryInstance.get("instanceType"));
        return lMap;
    }
}

