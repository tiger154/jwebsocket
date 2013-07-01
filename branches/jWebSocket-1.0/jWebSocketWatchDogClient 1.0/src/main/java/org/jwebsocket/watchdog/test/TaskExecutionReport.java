// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-212 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.watchdog.test;

import java.util.List;
import org.jwebsocket.watchdog.api.ITaskExecutionReport;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.api.IWatchDogTask;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
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
