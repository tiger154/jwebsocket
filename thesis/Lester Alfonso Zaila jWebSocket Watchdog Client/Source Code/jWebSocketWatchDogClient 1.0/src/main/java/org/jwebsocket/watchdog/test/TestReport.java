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

import org.jwebsocket.watchdog.api.ITestReport;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class TestReport implements ITestReport {

    private String mTestDescription;
    private String mTestId;
    private boolean mTestResult;
    private boolean mfatal;

    /**
     * Setter
     */
    @Override
    public void setTestDescription(String aTestDescription) {
        this.mTestDescription = aTestDescription;
    }

    @Override
    public void setTestId(String aTestId) {
        this.mTestId = aTestId;
    }

    @Override
    public void setTestResult(boolean aTestResult) {
        this.mTestResult = aTestResult;
    }

    @Override
    public void setFatal(boolean aIsFatal) {
        this.mfatal = aIsFatal;
    }

    /**
     *Getter
     */
    @Override
    public String getTestDescription() {
        return mTestDescription;
    }

    @Override
    public String getTestId() {
        return mTestId;
    }

    @Override
    public Boolean getTestResult() {
        return mTestResult;
    }

    @Override
    public Boolean isFatal() {
        return mfatal;
    }

    @Override
    public String toString() {
        return "TestReport{" + "testDescription=" + mTestDescription
                + ", testId=" + mTestId + ", testResult=" + mTestResult
                + ", fatal=" + mfatal + '}';
    }
}
