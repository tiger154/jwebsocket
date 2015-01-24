//	---------------------------------------------------------------------------
//	jWebSocket DateHandler (Community Edition, CE)
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
package org.jwebsocket.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Provides some convenience methods to support the web socket development.
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class DateHandler {

	/**
	 * Current date with "yyyy-MM-dd" format
	 *
	 * @return String
	 */
	public static String getCurrentDate() {
		String lDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return lDate;
	}

	/**
	 * Current time with "HH:mm:ss" format
	 *
	 * @return String
	 */
	public static String getCurrentTime() {
		String lDate = new SimpleDateFormat("HH:mm:ss").format(new Date());
		return lDate;
	}

	/**
	 * Current time with "HH:mm:ss" format
	 *
	 * @param aDate
	 * @param aDays
	 * @return String
	 */
	public static Date addDays(Date aDate, Integer aDays) {
		Calendar lCalendar = new GregorianCalendar();
		lCalendar.setTimeInMillis(aDate.getTime());
		lCalendar.add(Calendar.DATE, aDays);
		return new Date(lCalendar.getTimeInMillis());
	}

	/**
	 * Current time with "HH:mm:ss" format
	 *
	 * @param aDate
	 * @param aDays
	 * @return String
	 */
	public static Date substractDays(Date aDate, Integer aDays) {
		Calendar lCalendar = new GregorianCalendar();
		lCalendar.setTimeInMillis(aDate.getTime());
		lCalendar.add(Calendar.DATE, -aDays);
		return new Date(lCalendar.getTimeInMillis());
	}

	/**
	 * Range of days between two date
	 *
	 * @param aInitialDate
	 * @param aEndDate
	 * @return Integer
	 */
	public static synchronized Integer rangeOfDaysBetweenDates(Date aInitialDate, Date aEndDate) {
		long lRange = aEndDate.getTime() - aInitialDate.getTime();
		double lDays = Math.floor(lRange / (1000 * 60 * 60 * 24));
		return ((int) lDays);
	}
}
