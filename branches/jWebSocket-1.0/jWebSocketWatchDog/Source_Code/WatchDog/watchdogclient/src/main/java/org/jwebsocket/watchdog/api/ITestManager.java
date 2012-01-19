package org.jwebsocket.watchdog.api;

import java.util.List;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.watchdog.test.TaskExecutionReport;

/**
 *
 * @author lester
 */
public interface ITestManager extends IInitializable {

    List<IWatchDogTestListener> getListeners();

    TaskExecutionReport execute(IWatchDogTask aTask);
}
