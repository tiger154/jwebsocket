package org.jwebsocket.watchdog.api;

import java.util.List;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.watchdog.test.WatchDogTest;

/**
 *
 * @author lester
 */
public interface IWatchDogTestService extends IInitializable {

    /*
     * add a test
     */
    void add(WatchDogTest aTest) throws Exception;
    
    /*
     * Remove test
     */
    void remove(String aTestId) throws Exception;
    
    /*
     *update or modify test receiveing the task's id and the test
     */
    void modify(String aTestId, WatchDogTest aTest) throws Exception;
    
    
    /*
     *lists of test
     */
    List<WatchDogTest> list();

    List<IWatchDogTest> list(String aTaskId);
}
