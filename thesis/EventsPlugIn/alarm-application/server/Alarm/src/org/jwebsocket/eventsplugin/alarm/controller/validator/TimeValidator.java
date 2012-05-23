//  ---------------------------------------------------------------------------
//  jWebSocket 
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventsplugin.alarm.controller.validator;

import java.util.Date;
import org.jwebsocket.eventmodel.filter.validator.Argument;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author kyberneees
 */
public class TimeValidator implements Validator {

	@Override
	public boolean supports(Class<?> aType) {
		if (aType.equals(String.class)) {
			return true;
		}
		return false;
	}

	@Override
	public void validate(Object aArg, Errors aErrors) {
		Argument lArg = (Argument) aArg;

		try {
			long lTime = Long.parseLong(lArg.getValue().toString());
			if (lTime <= new Date().getTime()) {
				aErrors.rejectValue(lArg.getName(), "The alarm time value is not valid. "
						+ "Please enter a valid future time value!");
			}
		} catch (Exception lEx) {
			aErrors.rejectValue(lArg.getName(), "The alarm time value cannot be parsed to Long!");
		}
	}
}
