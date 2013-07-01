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
package org.jwebsocket.watchdog.listener;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.api.IWatchDogTestListener;
import org.jwebsocket.watchdog.notifier.MailNotifier;
import org.jwebsocket.watchdog.test.TaskExecutionReport;
import org.jwebsocket.watchdog.test.Test;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class WatchDogMailListener implements IWatchDogTestListener {

    /*
     * Attributes
     */
    private MailNotifier mNotifier;

    /*
     * Getter and Setter
     */
    public MailNotifier getNotifier() {
        return mNotifier;
    }

    public void setNotifier(MailNotifier aNotifier) {
        this.mNotifier = aNotifier;
    }

    @Override
    public void process(TaskExecutionReport aReport) {
        String lMessage = "";
        List<String> lTests = new FastList();

        for (ITestReport lTestReport : aReport.getReport()) {
            if (lTestReport.getTestResult().equals(Test.NOT_OK)) {
                lTests.add("- Test: '" + lTestReport.getTestId() + "'{"
                        + lTestReport.getTestDescription() + "} failed.");
                //creating the strucure of the message 
            }
        }

        if (!lTests.isEmpty()) {
            lMessage = "The task '" + aReport.getTask().getId()
                    + "' has failed. Failed execution of the following tests: \n";
            for (String lTest : lTests) {
                lMessage = lMessage + "\n" + lTest;
            }
            mNotifier.notify(lMessage + "\n\nPlease check the server status!");
            //sending the message!
        }
    }
}
