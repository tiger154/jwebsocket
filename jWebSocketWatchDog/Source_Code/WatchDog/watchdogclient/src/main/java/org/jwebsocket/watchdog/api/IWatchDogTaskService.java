package org.jwebsocket.watchdog.api;

import java.util.List;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.watchdog.test.WatchDogTest;

/**
 *
 * @author lester
 */
public interface IWatchDogTaskService extends IInitializable {

    /*
     * add a task
     */
    void add(IWatchDogTask aTask) throws Exception;
    
    /*
     * remove task
     */
    void remove(String aTaskId) throws Exception;
    
    /*
     * update or modify task receiveing the task's id and the task
     */
    void modify(String aTaskId, IWatchDogTask aTask) throws Exception;
    
    /*
     * list  of task
     */
    List<IWatchDogTask> list();
}
