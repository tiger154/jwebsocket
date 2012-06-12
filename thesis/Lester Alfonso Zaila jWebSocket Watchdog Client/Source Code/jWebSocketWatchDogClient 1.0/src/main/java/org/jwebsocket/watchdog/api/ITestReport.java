// ---------------------------------------------------------------------------
// jWebSocket - < ITestReport >
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
package org.jwebsocket.watchdog.api;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public interface ITestReport {

    /**
     * Getting the test id
     * 
     * @return 
     */
    public String getTestId();
    
    /**
     * get if the result is OK(true) or Not OK(false)
     * 
     * @return 
     */
    public Boolean getTestResult();

    /**
     * get the description of the test report
     * 
     * @return 
     */
    public String getTestDescription();

    /**
     * Check if it is fatal
     * 
     * @return 
     */
    public Boolean isFatal();

    /**
     * Set the test result given
     * 
     * @param aResult 
     */
    public void setTestResult(boolean aResult);

    /**
     * Set the test id
     * 
     * @param aTest 
     */
    public void setTestId(String aTest);

    /**
     * Set the description of the test
     * 
     * @param aTestDescription 
     */
    public void setTestDescription(String aTestDescription);

    /**
     * Set if is fatal the test
     * 
     * @param aIsFatal 
     */
    public void setFatal(boolean aIsFatal);
}
