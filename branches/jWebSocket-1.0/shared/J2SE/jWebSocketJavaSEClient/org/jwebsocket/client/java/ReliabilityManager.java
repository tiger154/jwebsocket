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

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 *
 * @author aschulze
 */
public class ReliabilityManager {

	// private static ReliabilityManager mReliabilityManager = null;
	private static ScheduledThreadPoolExecutor mExecutor = null;

	/**
	 * @return the mExecutor
	 */
	public static ScheduledThreadPoolExecutor getExecutor() {
		if (mExecutor == null) {
			mExecutor = new ScheduledThreadPoolExecutor(2);
		}
		return mExecutor;
	}
	
	/*
	private ReliabilityManager() {
	// "jWebSocket Connection Reliability Manager"
	mExecutor = new ScheduledThreadPoolExecutor(2);
	}
	
	public static ReliabilityManager getInstance() {
	if (mReliabilityManager == null) {
	mReliabilityManager = new ReliabilityManager();
	}
	return mReliabilityManager;
	}
	 */
}