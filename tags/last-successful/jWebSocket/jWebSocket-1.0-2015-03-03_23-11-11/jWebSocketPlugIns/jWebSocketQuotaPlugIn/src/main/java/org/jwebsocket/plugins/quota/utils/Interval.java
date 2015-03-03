//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Interval (Community Edition, CE)
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

import java.util.Calendar;

/**
 *
 * @author Osvaldo Aguilar Lauzurique
 */
public class Interval {

    private String mStartDate;
    private String mEndDate;
    private Interval mInterval;
    private IntervalType mIntervalType;

    /**
     *
     */
    public Interval() {
    }

    /**
     *
     * @param aIntervalType
     */
    public void setStrTointervalType(String aIntervalType) {
        mIntervalType = IntervalType.valueOf(aIntervalType);
    }

    /**
     *
     * @return
     */
    public String getStartDate() {
        return mStartDate;
    }

    /**
     *
     * @param mStartDate
     */
    public void setStartDate(String mStartDate) {
        this.mStartDate = mStartDate;
    }

    /**
     *
     * @return
     */
    public String getEndDate() {
        return mEndDate;
    }

    /**
     *
     * @param mEndDate
     */
    public void setEndDate(String mEndDate) {
        this.mEndDate = mEndDate;
    }

    /**
     *
     * @return
     */
    public Interval getInterval() {
        return mInterval;
    }

    /**
     *
     * @param mInterval
     */
    public void setInterval(Interval mInterval) {
        this.mInterval = mInterval;
    }

    /**
     *
     * @return
     */
    public IntervalType getIntervalType() {
        return mIntervalType;
    }

    /**
     *
     * @param mIntervalType
     */
    public void setIntervalType(IntervalType mIntervalType) {
        this.mIntervalType = mIntervalType;
    }

    /**
     *
     * @return
     */
    public String toResetDate() {
        Calendar lToday = Calendar.getInstance();
        if (mIntervalType.equals(IntervalType.Yearly)) {
            return mEndDate + "-" + String.valueOf(lToday.get(Calendar.YEAR));
        }
        if (mIntervalType.equals(IntervalType.Monthly)) {
            return mEndDate + "-" + String.valueOf(lToday.get(Calendar.MONTH));
        }
        if (mIntervalType.equals(IntervalType.Weekly)) {
            return mEndDate + "-" + String.valueOf(lToday.get(Calendar.DAY_OF_MONTH));
        }
        if (mIntervalType.equals(IntervalType.Daily)) {
            return mEndDate + "-" + String.valueOf(lToday.get(Calendar.DAY_OF_MONTH));
        }
        return mEndDate + "-" + String.valueOf(lToday.get(Calendar.HOUR_OF_DAY));
    }
}
