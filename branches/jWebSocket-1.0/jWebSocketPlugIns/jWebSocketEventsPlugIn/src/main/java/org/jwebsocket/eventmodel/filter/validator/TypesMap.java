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

	private Map<String, Class<?>> table;

	/**
	 * @return The Abstract and Java corresponding types
	 */
	public Map<String, Class<?>> getTable() {
		return table;
	}

	/**
	 * @param table The abstract and java corresponding types to set
	 */
	public void setTable(Map<String, Class<?>> table) {
		this.table = table;
	}

	/**
	 * @param abstractType The abstract type
	 * @return The corresponding java type for the abstract type
	 * @throws InvalidTypeException
	 */
	public Class<?> swapType(String abstractType) throws InvalidTypeException {
		if (!table.containsKey(abstractType)) {
			throw new InvalidTypeException("The abstract type'" + abstractType
					+ "' has not a similar class type in the server side!");
		}

		return table.get(abstractType);
	}
}
