/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.watchdog.test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jwebsocket.watchdog.api.IWatchDogTest;

/**
 *
 * @author lester
 */
public class WatchDogTest implements IWatchDogTest {

    private String implClass;
    private String description;
    private String id;
    private String idTask;
    private boolean isFatal;

    /**
     * Setter  
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    @Override
    public void setIsFatal(Boolean isFatal) {
        this.isFatal = isFatal;
    }

    @Override
    public void setIdTask(String idTask) {
        this.idTask = idTask;
    }

    /**
     * Getter
     */
    @Override
    public String getImplClass() {
        return implClass;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Boolean isFatal() {
        return isFatal;
    }

    @Override
    public String getIdTask() {
        return idTask;
    }

    /*
     * Converting as Document so you can save it into MongoDB
     */
    @Override
    public DBObject asDocument() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("id", getId());
        obj.put("implClass", getImplClass());
        obj.put("description", getDescription());
        obj.put("isFatal", isFatal());
        return obj;
    }
}
