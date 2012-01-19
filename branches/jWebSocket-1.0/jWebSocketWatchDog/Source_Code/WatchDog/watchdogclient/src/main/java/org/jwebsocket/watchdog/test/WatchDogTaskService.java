/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author lester
 */
public final class WatchDogTaskService implements IWatchDogTaskService {

    private List<IWatchDogTask> task;
    private String id;
    private Mongo connection;
    private String databaseName;
    private DBCollection collection;
    private String collectionName;
    private IWatchDogTestService mTestService;

    public IWatchDogTestService getTestService() {
        return mTestService;
    }

    public void setTestService(IWatchDogTestService aTestService) {
        this.mTestService = aTestService;
    }

    /**
     * Getter and Setter
     */
    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public DBCollection getCollection() {
        return collection;
    }

    public void setCollection(DBCollection collection) {
        this.collection = collection;
    }

    public Mongo getConnection() {
        return connection;
    }

    public void setConnection(Mongo connection) {
        this.connection = connection;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<IWatchDogTask> getTask() {
        return task;
    }

    public void setTask(List<IWatchDogTask> aTask) {
        this.task = aTask;
    }

    /*
     * Overriding add method from IWatchDogTask Interface
     */
    @Override
    public void add(IWatchDogTask aTask) throws MongoException {
        collection.insert(aTask.asDocument());
    }

    /*
     * Overriding remove method from IWatchDogTask Interface
     */
    @Override
    public void remove(String aTaskId) throws Exception {
        if (collection.findOne() == null) {
            throw new Exception("There is not test to remove");
        } else {
            DBObject toRemove = collection.findOne(new BasicDBObject("id", aTaskId));
            if (toRemove != null) {
                collection.remove(toRemove);
            }
        }
    }

    /*
     * Overriding modify method from IWatchDogTask Interface
     */
    @Override
    public void modify(String aTaskId, IWatchDogTask aTask) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     * Overriding list method from IWatchDogTask Interface
     */
    @Override
    public List<IWatchDogTask> list() {
        DBCursor cur = collection.find();
        //System.out.println("dimension >>>>"+cur.size());
        FastList<IWatchDogTask> result = new FastList<IWatchDogTask>();
        DBObject current;
        //List<IWatchDogTask> current2; 
        while (cur.hasNext()) {
            current = cur.next();
            WatchDogTask a = new WatchDogTask();
            a.setId(current.get("id").toString());
            a.setDate((Date) current.get("date"));
            a.setLastExecution(current.get("lastExecution").toString());
            a.setFrequency(current.get("frequency").toString());

            //Cambiar esto por  el metodo que devuelve los 
            //objetos IWatchDogTest 
            a.setTests(mTestService.list(a.getId()));

            result.add((a));
        }
        return result;
    }

    /*
     * Overriding initialize method from IWatchDogTest Interface
     */
    @Override
    public void initialize() throws Exception {
        collection = connection.getDB(getDatabaseName()).getCollection(getCollectionName());
    }

    /*
     * Overriding shutdown method from IWatchDogTest Interface
     */
    @Override
    public void shutdown() throws Exception {
    }
}
