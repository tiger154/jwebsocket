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
package org.jwebsocket.watchdog.executor;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jwebsocket.watchdog.api.ITestManager;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTaskService;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class TaskExecutor extends Thread {

    /*
     * attributes and constructor
     */
    private ITestManager mTestManager;
    IWatchDogTaskService mTaskService;

    public TaskExecutor(ITestManager aTestManager, IWatchDogTaskService aTaskService) {
        this.mTestManager = aTestManager;
        this.mTaskService = aTaskService;
    }

    @Override
    public void run() {
        try {
            while (true) {

                List<IWatchDogTask> lList = mTaskService.list();
                int lComparison;
                Date lDate;
                for (IWatchDogTask lTask : lList) {
                    if (lTask.getType().equals("m")) {
                        //getting the frequency
                        lDate = addMinutes(lTask.getEveryNMinutes());
                    } else if (lTask.getType().equals("h")) {
                        //getting the frequency
                        lDate = addHour(lTask.getEveryNHours());
                    } else {
                        lDate = addDays(lTask.getEveryNDays());
                    }
                    lComparison = lDate.compareTo(new Date(lTask.getLastExecution()));
                    if (0 >= lComparison) {
                        //Execute the task
                        mTestManager.execute(lTask);
                    }
                }
                Thread.sleep(60000);
            }
        } catch (Exception ex) {
            Logger.getLogger(TaskExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Date addMinutes(int aNumber) {
        Calendar lCalendar = Calendar.getInstance();
        lCalendar.add(Calendar.MINUTE, -1 * aNumber);
        return new Date(lCalendar.getTimeInMillis());
    }

    private Date addHour(int aNumber) {
        Calendar lCalendar = Calendar.getInstance();
        lCalendar.add(Calendar.HOUR, -1 * aNumber);
        return new Date(lCalendar.getTimeInMillis());
    }

    private Date addDays(int aNumber) {
        Calendar lCalendar = Calendar.getInstance();
        lCalendar.add(Calendar.DATE, -1 * aNumber);
        return new Date(lCalendar.getTimeInMillis());
    }
}
