package org.jwebsocket.watchdog.test;

import org.jwebsocket.token.Token;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.watchdog.api.ITest;
import org.jwebsocket.watchdog.api.ITestReport;

/**
 *
 * @author lester
 */
public class WatchDogTokenResponseListener implements WebSocketResponseTokenListener {

    ITest mTest;
    ITestReport mReport;
    long mTimeout = 5000;

    public WatchDogTokenResponseListener(ITest aTest, ITestReport aReport) {
        mTest = aTest;
        mReport = aReport;
    }

    public ITestReport getReport() {
        return mReport;
    }

    public ITest getTest() {
        return mTest;
    }
    
    @Override
    public void OnFailure(Token token) {
    }

    @Override
    public void OnResponse(Token token) {
    }

    @Override
    public void OnSuccess(Token token) {
    }

    @Override
    public void OnTimeout(Token token) {
    }

    @Override
    public long getTimeout() {
        return mTimeout;
    }

    @Override
    public void setTimeout(long l) {
        mTimeout = l;
    }
}
