/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.watchdog.test;

import java.util.List;
import org.jwebsocket.watchdog.api.ITaskExecutionReport;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.api.IWatchDogTask;

/**
 *
 * @author lester
 */
public class TaskExecutionReport implements ITaskExecutionReport {

    private IWatchDogTask mTask;
    private List<ITestReport> mReport;

    public TaskExecutionReport(IWatchDogTask mTask, List<ITestReport> mReport) {
        this.mTask = mTask;
        this.mReport = mReport;
    }

    /*
     * Getter
     */
    @Override
    public List<ITestReport> getReport() {
        return mReport;
    }

    @Override
    public IWatchDogTask getTask() {
        return mTask;
    }
}
