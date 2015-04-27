//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBCacheStorageV2 (Community Edition, CE)
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
package org.jwebsocket.cachestorage.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.api.IBasicCacheStorage;
import org.jwebsocket.storage.BaseStorage;

/**
 * This class uses MongoDB servers to persist the information. <br> All the
 * cache storages entries are located in the same database collection.
 *
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public class MongoDBCacheStorageV2<K, V> extends BaseStorage<K, V> implements IBasicCacheStorage<K, V> {

    private String mName;
    private final DBCollection mCollection;

    /**
     *
     * @param aName
     * @param aCollection
     */
    public MongoDBCacheStorageV2(String aName, DBCollection aCollection) {
        this.mName = aName;
        mCollection = aCollection;
    }

    /**
     * {@inheritDoc
     *
     * @param aKey
     * @param aValue
     * @param aExpTime
     */
    @Override
    public V put(K aKey, V aValue, int aExpTime) {
        DBObject lRecord = new BasicDBObject();
        lRecord.put("k", aKey);
        lRecord.put("ns", mName);

        DBObject lExisting = mCollection.findOne(lRecord);
        if (null != lExisting) {
            lRecord = lExisting;
        }

        lRecord.put("v", aValue);
        lRecord.put("it", (Long) (System.currentTimeMillis() / 1000));
        lRecord.put("et", aExpTime);

        mCollection.save(lRecord);

        return aValue;
    }

    /**
     * {@inheritDoc
     *
     * @param aKey
     * @param aValue
     */
    @Override
    public V put(K aKey, V aValue) {
        return put(aKey, aValue, 0);
    }

    /**
     * {@inheritDoc
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * {@inheritDoc
     *
     * @param aNewName
     * @throws java.lang.Exception
     */
    @Override
    public void setName(String aNewName) throws Exception {
        mCollection.update(new BasicDBObject().append("ns", mName),
                new BasicDBObject().append("$set", new BasicDBObject().append("ns", aNewName)));

        mName = aNewName;
    }

    /**
     * {@inheritDoc
     */
    @Override
    public void clear() {
        mCollection.remove(new BasicDBObject().append("ns", mName));
    }

    /**
     * {@inheritDoc
     *
     * @param aKey
     */
    @Override
    public boolean containsKey(Object aKey) {
        DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).
                append("k", aKey));

        if (lRecord != null && isValid(lRecord)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param aRecord The DBObject record
     * @return TRUE if the record is not expired, FALSE otherwise
     */
    private boolean isValid(DBObject aRecord) {
        Integer lExpTime = (Integer) aRecord.get("et");
        if (lExpTime < 1) {
            return true;
        }

        if (((Long) aRecord.get("it")) + lExpTime >= System.currentTimeMillis() / 1000) {
            return true;
        }
        // keeping the collection up to date with only non-expired values
        mCollection.remove(aRecord);

        return false;
    }

    /**
     * {@inheritDoc
     *
     * @param aValue
     */
    @Override
    public boolean containsValue(Object aValue) {
        DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).
                append("v", aValue));

        if (lRecord != null && isValid(lRecord)) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc
     *
     * @param aValue
     */
    @Override
    public V get(Object aValue) {
        DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).
                append("k", aValue));

        if (lRecord != null) {
            if (isValid(lRecord)) {
                return (V) lRecord.get("v");
            }
        }
        return null;
    }

    /**
     * {@inheritDoc
     */
    @Override
    public Set<K> keySet() {
        Set<K> lKeySet = new FastSet<K>();
        DBCursor lCursor = mCollection.find(new BasicDBObject().append("ns", mName));

        while (lCursor.hasNext()) {
            DBObject lRecord = lCursor.next();
            if (isValid(lRecord)) {
                lKeySet.add((K) lRecord.get("k"));
            }
        }

        return lKeySet;
    }

    /**
     * {@inheritDoc
     *
     * @param aKey
     */
    @Override
    public V remove(Object aKey) {
        DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).
                append("k", aKey));

        if (lRecord != null) {
            mCollection.remove(lRecord);
            if (isValid(lRecord)) {
                return (V) lRecord.get("v");
            }
        }
        return null;
    }

    /**
     * {@inheritDoc
     *
     * @throws java.lang.Exception
     */
    @Override
    public void initialize() throws Exception {
        mCollection.createIndex(new BasicDBObject().append("ns", 1).append("k", 1),
                new BasicDBObject().append("unique", true));
    }
}
