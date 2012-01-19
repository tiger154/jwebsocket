package org.jwebsocket.watchdog.api;

import com.mongodb.DBObject;

/**
 *
 * @author lester
 */
public interface IMongoDocument {

    /*
     * save the data as objects
     */
    DBObject asDocument();
}
