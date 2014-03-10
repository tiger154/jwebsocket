// ---------------------------------------------------------------------------
// jWebSocket - SharedObject (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.sharedobjects;

import java.util.Date;

/**
 *
 * @author Alexander Schulze
 */
public class SharedObject {

	/**
	 *
	 */
	public static int MODE_READ_WRITE = 0;
	/**
	 *
	 */
	public static int MODE_READ_ONLY = 1;
	/**
	 *
	 */
	public static int LOCK_STATE_RELEASED = 0;
	/**
	 *
	 */
	public static int LOCK_STATE_LOCKED = 1;
	private Object object = null;
	private int mode = 0; // see above
	private int lockstate = 0; // see above
	private Date lock_timestamp = null;

	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * @return the lockstate
	 */
	public int getLockstate() {
		return lockstate;
	}

	/**
	 * @param lockstate the lockstate to set
	 */
	public void setLockstate(int lockstate) {
		this.lockstate = lockstate;
	}

	/**
	 * @return the lock_timestamp
	 */
	public Date getLock_timestamp() {
		return lock_timestamp;
	}

	/**
	 * @param lock_timestamp the lock_timestamp to set
	 */
	public void setLock_timestamp(Date lock_timestamp) {
		this.lock_timestamp = lock_timestamp;
	}
}
