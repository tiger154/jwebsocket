/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.watchdog.api;

import java.util.List;

/**
 *
 * @author lester
 */
public interface ITaskExecutionReport {

    IWatchDogTask getTask();

    List<ITestReport> getReport();
}
