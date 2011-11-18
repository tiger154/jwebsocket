//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2011 Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.client.java;

/**
 *
 * @author aschulze
 */
public class ReliabilityOptions {

	private boolean mAutoReconnect = false;
	private long mReconnectDelay = -1;
	private long mReconnectTimeout = -1;
	private int mQueueItemLimit = -1;
	private int mQueueSizeLimit = -1;

	public ReliabilityOptions(boolean aAutoReconnect, long aReconnectDelay,
			long aReconnectTimeout, int aQueueItemLimit, int aQueueSizeLimit) {
		mAutoReconnect = aAutoReconnect;
		mReconnectDelay = aReconnectDelay;
		mReconnectTimeout = aReconnectTimeout;
		mQueueItemLimit = aQueueItemLimit;
		mQueueSizeLimit = aQueueSizeLimit;
	}

	/**
	 * @return the AutoReconnect
	 */
	public boolean isAutoReconnect() {
		return mAutoReconnect;
	}

	/**
	 * @param aAutoReconnect the AutoReconnect to set
	 */
	public void setAutoReconnect(boolean aAutoReconnect) {
		this.mAutoReconnect = aAutoReconnect;
	}

	/**
	 * @return the ReconnectDelay
	 */
	public long getReconnectDelay() {
		return mReconnectDelay;
	}

	/**
	 * @param aReconnectDelay the ReconnectDelay to set
	 */
	public void setReconnectDelay(long aReconnectDelay) {
		this.mReconnectDelay = aReconnectDelay;
	}

	/**
	 * @return the ReconnectTimeout
	 */
	public long getReconnectTimeout() {
		return mReconnectTimeout;
	}

	/**
	 * @param aReconnectTimeout the ReconnectTimeout to set
	 */
	public void setReconnectTimeout(int aReconnectTimeout) {
		this.mReconnectTimeout = aReconnectTimeout;
	}

	/**
	 * @return the QueueItemLimit
	 */
	public int getQueueItemLimit() {
		return mQueueItemLimit;
	}

	/**
	 * @param aQueueItemLimit the QueueItemLimit to set
	 */
	public void setQueueItemLimit(int aQueueItemLimit) {
		this.mQueueItemLimit = aQueueItemLimit;
	}

	/**
	 * @return the QueueSizeLimit
	 */
	public int getQueueSizeLimit() {
		return mQueueSizeLimit;
	}

	/**
	 * @param aQueueSizeLimit the QueueSizeLimit to set
	 */
	public void setQueueSizeLimit(int aQueueSizeLimit) {
		this.mQueueSizeLimit = aQueueSizeLimit;
	}
}
