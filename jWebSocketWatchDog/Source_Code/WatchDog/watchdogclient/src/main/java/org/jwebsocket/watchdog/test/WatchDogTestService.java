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
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.watchdog.api.IWatchDogTest;
import org.jwebsocket.watchdog.api.IWatchDogTestService;

/**
 *
 * @author lester
 */
public class WatchDogTestService implements IWatchDogTestService {

    private String id;
    private Mongo connection;
    private String databaseName;
    private DBCollection mCollection;
    private String collectionName;

    /*
     * Getter and Setter
     */
    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Mongo getConnection() {
        return connection;
    }

    public void setConnection(Mongo connection) {
        this.connection = connection;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
            DBObject toRemove = mCollection.findOne(new BasicDBObject("id", aTestId));
            if (toRemove != null) {
                mCollection.remove(toRemove);
            }
        }
    }
    
    /*
     * Overriding modify method from IWatchDogTest Interface
     */
    @Override
    public void modify(String aTestId, WatchDogTest aTest) throws Exception {
        DBObject find = mCollection.findOne(new BasicDBObject("id", aTestId));
        if (find == null) {
            throw new Exception("There is not test to Update");
        } else {
            mCollection.update(find, aTest.asDocument());
        }
    }

    /*
     * Overriding list method from IWatchDogTest Interface
     */
    @Override
    public List<IWatchDogTest> list(String aTaskId) {
        DBCollection taskcol = connection.getDB("mongotest").getCollection("mongotaskcollection");
        DBCursor cur = taskcol.find(new BasicDBObject().append("id", aTaskId));
        DBObject current;

        List<IWatchDogTest> lResult = new FastList<IWatchDogTest>();

        List<WatchDogTest> allTests = list();

        if (!cur.hasNext()) {
            return lResult;
        } else {
            current = cur.next();
        }

        List idTests = (List) (current.get("idTests"));
        for (IWatchDogTest lTest: allTests){
            if (idTests.contains(lTest.getId())){
                lResult.add(lTest);
            }
        }

        return lResult;
    }

    /*
     * Overriding list method from IWatchDogTest Interface
     */
    @Override
    public List<WatchDogTest> list() {
        DBCursor cur = mCollection.find();
        //System.out.println("dimension >>>>"+cur.size());
        FastList<WatchDogTest> result = new FastList<WatchDogTest>();
        DBObject current;
        WatchDogTest current2;

        while (cur.hasNext()) {
            current = cur.next();
            current2 = new WatchDogTest();
            current2.setId(current.get("id").toString());
            current2.setImplClass(current.get("implClass").toString());
            current2.setDescription(current.get("description").toString());
            current2.setIsFatal((Boolean) current.get("isFatal"));
            result.add(current2);
        }
        return result;
    }

    /*
     * Overriding initialize method from IWatchDogTest Interface
     */
    @Override
    public void initialize() throws Exception {
        mCollection = connection.getDB(getDatabaseName()).getCollection(getCollectionName());
    }

    /*
     * Overriding shutdown method from IWatchDogTest Interface
     */
    @Override
    public void shutdown() throws Exception {
    }
}
