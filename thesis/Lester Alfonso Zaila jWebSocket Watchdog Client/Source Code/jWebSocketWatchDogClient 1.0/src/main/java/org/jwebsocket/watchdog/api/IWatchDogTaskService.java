// ---------------------------------------------------------------------------
// jWebSocket - < IWatchDogTaskService >
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

import com.mongodb.MongoException;
import java.util.Date;
import java.util.List;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public interface IWatchDogTaskService extends IInitializable {

    /**
     * add a task
     * 
     * @param aTask
     * @throws Exception 
     */
    void add(IWatchDogTask aTask) throws Exception;

    /**
     * remove a task
     * 
     * @param aTaskId
     * @throws Exception 
     */
    void remove(String aTaskId) throws Exception;

    /**
     * update or modify task receiveing the task's id and the task
     * 
     * @param aTaskId
     * @param aTask
     * @throws Exception 
     */
    void modify(String aTaskId, IWatchDogTask aTask) throws Exception;

    /**
     * Get the list of the task
     * 
     * @return
     * @throws MongoException 
     */
    List<IWatchDogTask> list() throws MongoException;

   /**
    * Update the last time the task was executed
    * 
    * @param aTask
    * @param aDate
    * @throws Exception 
    */
    void updateLastExecution(IWatchDogTask aTask, Date aDate) throws Exception;
}
