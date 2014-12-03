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

import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.watchdog.api.ITest;
import org.jwebsocket.watchdog.api.ITestReport;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class Test implements ITest {

    private boolean mIsDone;
    private BaseTokenClient mClient;
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
    public BaseTokenClient getClient() {
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
    public void setClient(BaseTokenClient aClient) {
        this.mClient = aClient;
    }
}
