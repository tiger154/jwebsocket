package org.jwebsocket.watchdog.api;

import java.util.List;
import org.jwebsocket.watchdog.test.TaskExecutionReport;

/**
 *
 * @author lester
 */
public interface IWatchDogTestListener {

    /*
     * Process method needs a report of task executions.
     */
    public void process(TaskExecutionReport report);
}
