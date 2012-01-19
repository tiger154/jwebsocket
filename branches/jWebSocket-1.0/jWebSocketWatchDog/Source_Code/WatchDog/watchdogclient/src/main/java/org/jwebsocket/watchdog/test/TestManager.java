package org.jwebsocket.watchdog.test;

import java.util.LinkedList;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.client.cgi.CGITokenClient;
import org.jwebsocket.watchdog.api.ITestManager;
import org.jwebsocket.watchdog.api.ITestReport;
import org.jwebsocket.watchdog.api.IWatchDogTask;
import org.jwebsocket.watchdog.api.IWatchDogTestListener;

/**
 *
 * @author lester
 */
public class TestManager implements ITestManager {

    private CGITokenClient mClient;
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
        mClient = new CGITokenClient();
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

    public CGITokenClient getJwsClient() {
        return mClient;
    }

    /*
     * Setter
     */
    public void setListeners(List<IWatchDogTestListener> aListeners) {
        this.mListeners = aListeners;
    }

    public void setJwsClient(CGITokenClient jwsClient) {
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
        }
        return lResult;
    }
}
