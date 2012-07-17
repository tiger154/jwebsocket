//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Tools
//	Copyright (c) 2012 Innotrade GmbH
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
package org.jwebsocket.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Provides some convenience methods to support the web socket development.
 * 
 * @author Marcos Antonio González Huerta (markos0886, UCI)
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
	 * @param Date
	 * @param Integer
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
	 * @param Date
	 * @param Integer
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
	 * @param Date
	 * @param Date
	 * @return Integer
	 */
    public static synchronized Integer rangeOfDaysBetweenDates(Date aInitialDate, Date aEndDate) {
        long lRange = aEndDate.getTime() - aInitialDate.getTime();
        double lDays = Math.floor(lRange / (1000 * 60 * 60 * 24));
        return ((int) lDays);
    }
}
