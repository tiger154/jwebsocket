//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.eventmodel.filter.validator;

import java.util.Map;
import org.jwebsocket.eventmodel.exception.InvalidTypeException;

/**
 *
 * @author kyberneees
 */
public class TypesMap {

	private Map<String, Class<?>> mTypes;

	/**
	 * @return The Abstract and Java corresponding types
	 */
	public Map<String, Class<?>> getTypes() {
		return mTypes;
	}

	/**
	 * @param aTypes The abstract and java corresponding types to set
	 */
	public void setTypes(Map<String, Class<?>> aTypes) {
		this.mTypes = aTypes;
	}

	/**
	 * @param aType The abstract type
	 * @return The corresponding java type for the abstract type
	 * @throws InvalidTypeException
	 */
	public Class<?> swapType(String aType) throws InvalidTypeException {
		if (!mTypes.containsKey(aType)) {
			throw new InvalidTypeException("The abstract type'" + aType
					+ "' has not a similar class type in the server side!");
		}

		return mTypes.get(aType);
	}
}
