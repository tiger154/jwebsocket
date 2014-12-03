//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Cache Item (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.quota.utils;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaCacheItem {

    private boolean mHasQuota;
    private long mExpiredTime;

	/**
	 *
	 * @param hasQuota
	 * @param expiredTime
	 */
	public QuotaCacheItem(boolean hasQuota, long expiredTime) {
        this.mHasQuota = hasQuota;
        this.mExpiredTime = expiredTime;
    }

	/**
	 *
	 * @param aHasQuota
	 */
	public void setHasQuota(boolean aHasQuota) {
        this.mHasQuota = aHasQuota;
    }

	/**
	 *
	 * @param aExpiredTime
	 */
	public void setExpiredTime(long aExpiredTime) {
        this.mExpiredTime = aExpiredTime;
    }

	/**
	 *
	 * @return
	 */
	public boolean isAvailableQuota() {
        return mHasQuota;
    }

	/**
	 *
	 * @return
	 */
	public long getExpiredTime() {
        return mExpiredTime;
    }

}
