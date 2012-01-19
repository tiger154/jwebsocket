/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.watchdog.test;

import org.jwebsocket.client.cgi.CGITokenClient;
import org.jwebsocket.watchdog.api.ITest;
import org.jwebsocket.watchdog.api.ITestReport;

/**
 *
 * @author lester
 */
public class Test implements ITest {

    private boolean mIsDone;
    private CGITokenClient mClient;
    public final static boolean OK = true;
    public final static boolean NOT_OK = false;

    /*
     * 
     */
    @Override
    public void execute(ITestReport aReport) {

        aReport.setTestResult(OK);

        setIsDone(true);
    }

    /*
     * Getter
     */
    @Override
    public CGITokenClient getClient() {
        return mClient;
    }

    @Override
    public boolean isDone() {
        return mIsDone;
    }

    /*
     * Setter
     */
    @Override
    public void setIsDone(boolean aIsDone) {
        this.mIsDone = aIsDone;
    }

    /*
     * Overriding SetClient method from ITest Interface
     */
    @Override
    public void setClient(CGITokenClient aClient) {
        this.mClient = aClient;
    }
}
