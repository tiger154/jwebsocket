// ---------------------------------------------------------------------------
// jWebSocket - < WatchDogTokenResponseListener >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
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

import org.jwebsocket.token.Token;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.watchdog.api.ITest;
import org.jwebsocket.watchdog.api.ITestReport;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class WatchDogTokenResponseListener
        implements WebSocketResponseTokenListener {

    ITest mTest;
    ITestReport mReport;
    long mTimeout = 5000;

    public WatchDogTokenResponseListener(ITest aTest, ITestReport aReport) {
        mTest = aTest;
        mReport = aReport;
    }
    
    /**
     * Get the report
     * 
     * @return 
     */
    public ITestReport getReport() {
        return mReport;
    }

    /**
     * Get the test
     * 
     * @return 
     */
    public ITest getTest() {
        return mTest;
    }

    @Override
    public void OnTimeout(Token aToken) {
        getReport().setTestResult(Test.NOT_OK);
        getTest().setIsDone(true);
    }

    @Override
    public void OnFailure(Token aToken) {
        getReport().setTestResult(Test.NOT_OK);
        getTest().setIsDone(true);
    }

    @Override
    public void OnResponse(Token aToken) {
    }

    @Override
    public void OnSuccess(Token aToken) {
    }

    @Override
    public long getTimeout() {
        return mTimeout;
    }

    @Override
    public void setTimeout(long l) {
        mTimeout = l;
        //starting in 1
    }
}
