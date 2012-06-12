// ---------------------------------------------------------------------------
// jWebSocket - < IWatchDogTest >
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
public interface IWatchDogTest extends IMongoDocument {

    /**
     * Getter
     */
    /**
     * Get the location of the implementation of the class of the test
     * 
     * @return 
     */
    String getImplClass();

    /**
     * Get the description of the test
     * 
     * @return 
     */
    String getDescription();

    /**
     * Get the test id
     * 
     * @return 
     */
    String getId();

    /**
     * Get the task id
     * 
     * @return 
     */
    String getIdTask();

    /**
     * Get if is fatal the failure of the test
     * 
     * @return 
     */
    Boolean isFatal();
    /*
     * Setter
     */

    /**
     * Set the id of the task
     * 
     * @param idTask 
     */
    void setIdTask(String idTask);

    /**
     * Set the implementation class of the test
     * 
     * @param ImplClass 
     */
    void setImplClass(String ImplClass);

    /**
     * Set the description of the test
     * 
     * @param description 
     */
    void setDescription(String description);

    /**
     * Set the id of the test
     * 
     * @param id 
     */
    void setId(String id);

    /**
     * Set if is fatal the failure of the test
     * 
     * @param isFatal 
     */
    void setIsFatal(Boolean isFatal);
}
