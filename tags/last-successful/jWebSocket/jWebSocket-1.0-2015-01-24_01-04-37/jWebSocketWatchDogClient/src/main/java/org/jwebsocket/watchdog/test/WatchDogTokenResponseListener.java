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

    /*
     * Getter
     */
    public ITestReport getReport() {
        return mReport;
    }

    public ITest getTest() {
        return mTest;
    }

    /*
     * Overriding methods
     */
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
        //starting in 1
    }
}
