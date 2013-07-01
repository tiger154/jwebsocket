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

import java.util.LinkedList;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.watchdog.api.ITestManager;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTestListener;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class TestManager implements ITestManager {

    private BaseTokenClient mClient;
    String mServerUrl;
    private List<IWatchDogTestListener> mListeners = new FastList<IWatchDogTestListener>();

    public String getServerUrl() {
        return mServerUrl;
    }

    public void setServerUrl(String aServerUrl) {
        this.mServerUrl = aServerUrl;
    }

    @Override
    public void initialize() throws Exception {
        mClient = new BaseTokenClient();
        mClient.open(mServerUrl);
    }

    @Override
    public void shutdown() throws Exception {
        mClient.close();
    }

    /*
     * Getter
     */
    @Override
    public List<IWatchDogTestListener> getListeners() {
        return mListeners;
    }

    public BaseTokenClient getJwsClient() {
        return mClient;
    }

    /*
     * Setter
     */
    public void setListeners(List<IWatchDogTestListener> aListeners) {
        this.mListeners = aListeners;
    }

    public void setJwsClient(BaseTokenClient jwsClient) {
        this.mClient = jwsClient;
    }

    /*
     * To Execute a Test it needs a Task
     */
    @Override
    public TaskExecutionReport execute(IWatchDogTask aTask) {
        List<ITestReport> lTestReports = new LinkedList<ITestReport>();

        aTask.execute(mClient, lTestReports);
        TaskExecutionReport lResult = new TaskExecutionReport(aTask, lTestReports);

        for (IWatchDogTestListener lListener : mListeners) {
            lListener.process(lResult);
            //processing the results
        }
        return lResult;
    }
}
