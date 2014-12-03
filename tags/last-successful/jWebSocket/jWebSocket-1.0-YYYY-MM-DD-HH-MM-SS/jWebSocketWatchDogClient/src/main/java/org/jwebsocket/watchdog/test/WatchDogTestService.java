// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-212 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.watchdog.test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.watchdog.api.IWatchDogTest;
import org.jwebsocket.watchdog.api.IWatchDogTestService;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class WatchDogTestService implements IWatchDogTestService {

    private String mId;
    private Mongo mConnection;
    private String mDatabaseName;
    private DBCollection mCollection;
    private String mCollectionName;

    /*
     * Getter and Setter
     */
    public String getCollectionName() {
        return mCollectionName;
    }

    public void setCollectionName(String aCollectionName) {
        this.mCollectionName = aCollectionName;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public void setDatabaseName(String aDatabaseName) {
        this.mDatabaseName = aDatabaseName;
    }

    public Mongo getConnection() throws MongoException {
        return mConnection;
    }

    public void setConnection(Mongo aConnection) {
        this.mConnection = aConnection;
    }

    public void setId(String aId) {
        this.mId = aId;
    }

    public String getId() {
        return mId;
    }

    /*
     * Overriding add method from IWatchDogTest Interface
     */
    @Override
    public void add(WatchDogTest aTest) throws MongoException {
        mCollection.insert(aTest.asDocument());
    }

    /*
     * Overriding remove method from IWatchDogTest Interface
     */
    @Override
    public void remove(String aTestId) throws Exception {
        if (mCollection.findOne() == null) {
            throw new Exception("There is not test to remove");
        } else {
            DBObject lToRemove = mCollection.findOne(new BasicDBObject("id", aTestId));
            if (lToRemove != null) {
                mCollection.remove(lToRemove);
            }
        }
    }

    /*
     * Overriding modify method from IWatchDogTest Interface
     */
    @Override
    public void modify(String aTestId, WatchDogTest aTest) throws Exception {
        DBObject lFind = mCollection.findOne(new BasicDBObject("id", aTestId));
        if (lFind == null) {
            throw new Exception("There is not test to Update");
        } else {
            mCollection.update(lFind, aTest.asDocument());
        }
    }

    /*
     * Overriding list method from IWatchDogTest Interface
     */
    @Override
    public List<IWatchDogTest> list(String aTaskId) throws MongoException {
        DBCollection lTtaskCol = mConnection.getDB(
                "mongotest").getCollection("mongotaskcollection");
        DBCursor lCur = lTtaskCol.find(new BasicDBObject().append("id", aTaskId));
        DBObject lCurrent;

        List<IWatchDogTest> lResult = new FastList<IWatchDogTest>();

        List<WatchDogTest> lAllTests = list();

        if (!lCur.hasNext()) {
            return lResult;
        } else {
            lCurrent = lCur.next();
        }

        List idTests = (List) (lCurrent.get("idTests"));
        for (IWatchDogTest lTest : lAllTests) {
            if (idTests.contains(lTest.getId())) {
                lResult.add(lTest);
            }
        }
        return lResult;
    }

    /*
     * Overriding list method from IWatchDogTest Interface
     */
    @Override
    public List<WatchDogTest> list() throws MongoException {

        DBCursor lCur = mCollection.find();

        FastList<WatchDogTest> lResult = new FastList<WatchDogTest>();
        DBObject lCurrent;
        WatchDogTest lCurrent2;

        while (lCur.hasNext()) {
            lCurrent = lCur.next();
            lCurrent2 = new WatchDogTest();
            lCurrent2.setId(lCurrent.get("id").toString());
            lCurrent2.setImplClass(lCurrent.get("implClass").toString());
            lCurrent2.setDescription(lCurrent.get("description").toString());
            lCurrent2.setIsFatal((Boolean) lCurrent.get("isFatal"));
            lResult.add(lCurrent2);
        }
        return lResult;
    }

    /*
     * Overriding initialize method from IWatchDogTest Interface
     */
    @Override
    public void initialize() throws MongoException {
        mCollection = mConnection.getDB(getDatabaseName()).
                getCollection(getCollectionName());
    }

    /*
     * Overriding shutdown method from IWatchDogTest Interface
     */
    @Override
    public void shutdown() throws Exception {
    }
}