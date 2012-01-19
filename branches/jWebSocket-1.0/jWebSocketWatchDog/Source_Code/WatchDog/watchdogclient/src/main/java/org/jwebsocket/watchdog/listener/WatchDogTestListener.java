/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.watchdog.listener;

import org.jwebsocket.watchdog.api.IWatchDogTestListener;
import org.jwebsocket.watchdog.test.TaskExecutionReport;

/**
 *
 * @author lester
 */
public class WatchDogTestListener implements IWatchDogTestListener {

    @Override
    public void process(TaskExecutionReport aReport) {
       System.out.println(aReport.getReport());
    }
}
