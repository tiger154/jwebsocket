// ---------------------------------------------------------------------------
// jWebSocket - < WatchDogTaskService >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
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

import com.mongodb.*;
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
    * Get the collection name where the task are located
    * 
    * @return 
    */
    public String getCollectionName() {
        return mCollectionName;
    }

    /**
     * Get the test services
     * 
     * @return 
     */
    public IWatchDogTestService getTestService() {
        return mTestService;
    }

    /**
     * Set the collection name
     * 
     * @param aCollectionName 
     */
    public void setCollectionName(String aCollectionName) {
        this.mCollectionName = aCollectionName;
    }

    /**
     * Set the test services
     * 
     * @param aTestService 
     */
    public void setTestService(IWatchDogTestService aTestService) {
        this.mTestService = aTestService;
    }

    /**
     * Get the collection
     * 
     * @return 
     */
    public DBCollection getCollection() {
        return mCollection;
    }

    /**
     * Set the collection
     * 
     * @param aCollection 
     */
    public void setCollection(DBCollection aCollection) {
        this.mCollection = aCollection;
    }

    /**
     * Get the connection with MongoDB
     * 
     * @return 
     */
    public Mongo getConnection() {
        return mConnection;
    }

    /**
     * Set the connection with MongoDG
     * 
     * @param aConnection 
     */
    public void setConnection(Mongo aConnection) {
        this.mConnection = aConnection;
    }

    /**
     * Get the name of the database
     * 
     * @return 
     */
    public String getDatabaseName() {
        return mDatabaseName;
    }

    /**
     * Set the name of the database
     * 
     * @param aDatabaseName 
     */
    public void setDatabaseName(String aDatabaseName) {
        this.mDatabaseName = aDatabaseName;
    }

    /**
     * Get the id
     * 
     * @return 
     */
    public String getId() {
        return mId;
    }

    /**
     * Set the test id
     * 
     * @param aId 
     */
    public void setId(String aId) {
        this.mId = aId;
    }

    /**
     * Set the task given the list of it
     * 
     * @param aTask 
     */
    public void setTask(List<IWatchDogTask> aTask) {
        this.mWatchDogTask = aTask;
    }

    /**
     * Get the list of task
     * 
     * @return 
     */
    public List<IWatchDogTask> getTask() {
        return mWatchDogTask;
    }

    @Override
    public void add(IWatchDogTask aTask) throws MongoException {
        mCollection.insert(aTask.asDocument());
    }

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

    @Override
    public void initialize() throws Exception {
        mCollection = mConnection.getDB(getDatabaseName()).
                getCollection(getCollectionName());
    }
    
    @Override
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
