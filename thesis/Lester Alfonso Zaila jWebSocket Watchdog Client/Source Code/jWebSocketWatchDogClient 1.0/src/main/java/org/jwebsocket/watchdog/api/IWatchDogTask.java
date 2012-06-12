// ---------------------------------------------------------------------------
// jWebSocket - < IWatchDogTask >
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

import java.util.List;
import org.jwebsocket.client.token.BaseTokenClient;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public abstract interface IWatchDogTask extends IMongoDocument {

    /**
     * Get the task id
     * 
     * @return 
     */
    public String getId();
    
    /**
     * Get the type of task
     * 
     * @return 
     */
    public String getType();
    
    /**
     * Getting the list of test
     * 
     * @return 
     */
    public List<IWatchDogTest> getTests();

    /**
     * Get the last time the task was executed
     * 
     * @return 
     */
    public String getLastExecution();
    
    /**
     * Get every n minutes the task will be executed
     * 
     * @return 
     */
    public Integer getEveryNMinutes();

    /**
     * Get every n hours the task will be executed
     * 
     * @return 
     */
    public Integer getEveryNHours();

    /**
     * Get every n days the task will be executed
     * 
     * @return 
     */
    public Integer getEveryNDays();

    /**
     * Set the task id
     * 
     * @param id 
     */
    public void setId(String id);

    /**
     * Set the type of execution
     * 
     * @param id 
     */
    public void setType(String id);

    /**
     * Set the list of test to be executed
     * 
     * @param aTests 
     */
    public void setTests(List<IWatchDogTest> aTests);

    /**
     * Set the last time the task was executed
     * 
     * @param lastExecution 
     */
    public void setLastExecution(String lastExecution);

    /**
     * Set every n minutes the task will be executed
     * 
     * @param minutes 
     */
    public void setEveryNMinutes(Integer minutes);

    /**
     * Set every n hours the task will be executed
     * 
     * @param hours 
     */
    public void setEveryNHours(Integer hours);
    
    /**
     * Set every n days the task will be executed
     * 
     * @param days 
     */
    public void setEveryNDays(Integer days);

    /**
     * Execute use a BaseTokenClient and a list of Reports.
     * 
     * @param aClient
     * @param aReport 
     */
    public void execute(BaseTokenClient aClient, List<ITestReport> aReport);
}