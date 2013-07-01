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
package org.jwebsocket.watchdog.api;

import java.util.List;
import org.jwebsocket.client.token.BaseTokenClient;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public abstract interface IWatchDogTask extends IMongoDocument {

    /*
     * Getter
     */
    public String getId();

    public String getType();

    public List<IWatchDogTest> getTests();

    public String getLastExecution();

    public Integer getEveryNMinutes();

    public Integer getEveryNHours();

    public Integer getEveryNDays();

    /*
     * Setter
     */
    public void setId(String id);

    public void setType(String id);

    public void setTests(List<IWatchDogTest> aTests);

    public void setLastExecution(String lastExecution);

    public void setEveryNMinutes(Integer minutes);

    public void setEveryNHours(Integer hours);

    public void setEveryNDays(Integer days);

    /*
     * Execute use a BaseTokenClient and a list of Reports.
     */
    public void execute(BaseTokenClient aClient, List<ITestReport> aReport);
}