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
import java.util.Date;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTaskService;
import org.jwebsocket.watchdog.api.IWatchDogTestService;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public final class WatchDogTaskService implements IWatchDogTaskService {

    //Creating the attributes
    private List<IWatchDogTask> mWatchDogTask;
    private String mId;
    private Mongo mConnection;
    private String mDatabaseName;
    private DBCollection mCollection;
    private String mCollectionName;
    private IWatchDogTestService mTestService;

    /**
     * Getter and Setter
     */
    public String getCollectionName() {
        return mCollectionName;
    }

    public IWatchDogTestService getTestService() {
        return mTestService;
    }

    public void setCollectionName(String aCollectionName) {
        this.mCollectionName = aCollectionName;
    }

    public void setTestService(IWatchDogTestService aTestService) {
        this.mTestService = aTestService;
    }

    public DBCollection getCollection() {
        return mCollection;
    }

    public void setCollection(DBCollection aCollection) {
        this.mCollection = aCollection;
    }

    public Mongo getConnection() {
        return mConnection;
    }

    public void setConnection(Mongo aConnection) {
        this.mConnection = aConnection;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public void setDatabaseName(String aDatabaseName) {
        this.mDatabaseName = aDatabaseName;
    }

    public String getId() {
        return mId;
    }

    public void setId(String aId) {
        this.mId = aId;
    }

    public void setTask(List<IWatchDogTask> aTask) {
        this.mWatchDogTask = aTask;
    }

    public List<IWatchDogTask> getTask() {
        return mWatchDogTask;
    }

    /*
     * Overriding add method from IWatchDogTask Interface
     */
    @Override
    public void add(IWatchDogTask aTask) throws MongoException {
        mCollection.insert(aTask.asDocument());
    }

    /*
     * Overriding remove method from IWatchDogTask Interface
     */
    @Override
    public void remove(String aTaskId) throws Exception {
        if (mCollection.findOne() == null) {
            //checking if the element exist
            throw new Exception("There is not test to remove");
        } else {
            DBObject toRemove = mCollection.findOne(new BasicDBObject("id", aTaskId));
            //preparing to remove the element once finded
            if (toRemove != null) {
                mCollection.remove(toRemove);
            }
        }
    }

    /*
     * Overriding modify method from IWatchDogTask Interface
     */
    @Override
    public void modify(String aTaskId, IWatchDogTask aTask) throws Exception {
        DBObject lFind = mCollection.findOne(new BasicDBObject("id", aTaskId));
        if (lFind == null) {
            //checking if the element exist
            throw new Exception("There is not Task to Update");
        } else {
            mCollection.update(lFind, aTask.asDocument());
            //updating the element
        }
    }

    /*
     * Overriding list method from IWatchDogTask Interface
     */
    @Override
    public List<IWatchDogTask> list() throws MongoException {
        DBCursor lCur = mCollection.find();
        
        FastList<IWatchDogTask> lResult = new FastList<IWatchDogTask>();
        
        DBObject current;
        while (lCur.hasNext()) {
            //will stop when the cursor arrive to the end
            current = lCur.next();
            IWatchDogTask lWatchDogTask = new WatchDogTask();
            lWatchDogTask.setId(current.get("id").toString());
            lWatchDogTask.setEveryNMinutes((Integer) current.get("everyNMinutes"));
            lWatchDogTask.setEveryNHours((Integer) current.get("everyNHours"));
            lWatchDogTask.setEveryNDays((Integer) current.get("everyNDays"));
            lWatchDogTask.setType(current.get("frequency").toString());
            lWatchDogTask.setLastExecution(current.get("lastExecution").toString());

            lWatchDogTask.setTests(mTestService.list(lWatchDogTask.getId()));

            lResult.add((lWatchDogTask));
            //adding the tasks to show results
        }
        return lResult;
    }

    /*
     * Overriding initialize method from IWatchDogTest Interface
     */
    public void initialize() throws Exception {
        mCollection = mConnection.getDB(getDatabaseName()).
                getCollection(getCollectionName());
    }

    /*
     * Overriding shutdown method from IWatchDogTest Interface
     */
    public void shutdown() throws Exception {
    }

    @Override
    public void updateLastExecution(IWatchDogTask aTask, Date aDate) throws Exception {
        DBObject find = mCollection.findOne(new BasicDBObject("id", aTask.getId()));
        if (find == null) {
            //checking if the element exist
            throw new Exception("There is not test to Update");
        } else {
            mCollection.update(find, new BasicDBObject().append("$set",
                    new BasicDBObject().append("lastExecution", aDate.toString())));
            //updating the task
        }
    }
}
