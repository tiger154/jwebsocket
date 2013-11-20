/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.utils;

import java.util.Calendar;

/**
 *
 * @author dino
 */
public class Interval {

	private String mStartDate;
	private String mEndDate;
	private Interval mInterval;
	private IntervalType mIntervalType;

	public Interval() {
	}

	public void setStrTointervalType(String aIntervalType) {
		mIntervalType = IntervalType.valueOf(aIntervalType);
	}

	public String getStartDate() {
		return mStartDate;
	}

	public void setStartDate(String mStartDate) {
		this.mStartDate = mStartDate;
	}

	public String getEndDate() {
		return mEndDate;
	}

	public void setEndDate(String mEndDate) {
		this.mEndDate = mEndDate;
	}

	public Interval getInterval() {
		return mInterval;
	}

	public void setInterval(Interval mInterval) {
		this.mInterval = mInterval;
	}

	public IntervalType getIntervalType() {
		return mIntervalType;
	}

	public void setIntervalType(IntervalType mIntervalType) {
		this.mIntervalType = mIntervalType;
	}

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
