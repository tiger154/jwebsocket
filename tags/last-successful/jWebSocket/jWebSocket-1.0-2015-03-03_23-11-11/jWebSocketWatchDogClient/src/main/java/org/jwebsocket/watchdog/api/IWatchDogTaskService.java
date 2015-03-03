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

import com.mongodb.MongoException;
import java.util.Date;
import java.util.List;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public interface IWatchDogTaskService extends IInitializable {

    /*
     * add a task
     */
    void add(IWatchDogTask aTask) throws Exception;

    /*
     * remove task
     */
    void remove(String aTaskId) throws Exception;

    /*
     * update or modify task receiveing the task's id and the task
     */
    void modify(String aTaskId, IWatchDogTask aTask) throws Exception;

    /*
     * list  of task
     */
    List<IWatchDogTask> list() throws MongoException;

    void updateLastExecution(IWatchDogTask aTask, Date aDate) throws Exception;
}
