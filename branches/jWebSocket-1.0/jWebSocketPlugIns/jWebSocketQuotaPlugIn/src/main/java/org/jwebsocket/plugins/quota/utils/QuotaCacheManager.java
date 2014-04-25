//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Quota Filter (Community Edition, CE)
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

import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class QuotaCacheManager {

    private final HashMap<String, QuotaCacheItem> mCache
            = new HashMap<String, QuotaCacheItem>();
    long mExpiredTime;

    public long getExpiredTime() {
        return mExpiredTime;
    }

    public void setExpiredTime(long mExpiredTime) {
        this.mExpiredTime = mExpiredTime;
    }

    public void add(String aCacheId, boolean aHasQuota) {

        Date lNow = new Date();
        QuotaCacheItem lItem = new QuotaCacheItem(aHasQuota, lNow.getTime());
        mCache.put(aCacheId, lItem);
    }

    public boolean checkForAvailableQuota(String aCacheId) {
        //getting the actual time to check with the expired time
        Date lNow = new Date();
        QuotaCacheItem lQuotaCacheItem;

        //check if the quota reques is in the cache list manager
        if (mCache.containsKey(aCacheId)) {
            //Getting the cache item element
            lQuotaCacheItem = mCache.get(aCacheId);
            
            //if the time of the last request is less than expired time then
            //if the last request found an available quota return true to
            //request for the quota, else do not request for a new quota.
            long lTimeLastRequest = lNow.getTime() - lQuotaCacheItem.getExpiredTime();
            if (lTimeLastRequest < this.mExpiredTime) {
                
                if( lQuotaCacheItem.isAvailableQuota() == true ) {
                    return true;
                }else{
                    return false;
                }
            }
        }
        //Return true if this the quota has not been checked before.
        return true;
    }

}
