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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jwebsocket.watchdog.api.IWatchDogTest;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public class WatchDogTest implements IWatchDogTest {

    private String mImplClass;
    private String mDescription;
    private String mId;
    private String mIdTask;
    private boolean mIsFatal;

    /**
     * Setter  
     */
    @Override
    public void setDescription(String aDescription) {
        this.mDescription = aDescription;
    }

    @Override
    public void setId(String aId) {
        this.mId = aId;
    }

    @Override
    public void setImplClass(String aImplClass) {
        this.mImplClass = aImplClass;
    }

    @Override
    public void setIsFatal(Boolean aIsFatal) {
        this.mIsFatal = aIsFatal;
    }

    @Override
    public void setIdTask(String aIdTask) {
        this.mIdTask = aIdTask;
    }

    /**
     * Getter
     */
    @Override
    public String getImplClass() {
        return mImplClass;
    }

    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public Boolean isFatal() {
        return mIsFatal;
    }

    @Override
    public String getIdTask() {
        return mIdTask;
    }

    /*
     * Converting as Document so you can save it into MongoDB
     */
    @Override
    public DBObject asDocument() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("id", getId());
        obj.put("implClass", getImplClass());
        obj.put("description", getDescription());
        obj.put("isFatal", isFatal());
        return obj;
    }
}
