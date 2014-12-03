//	---------------------------------------------------------------------------
//	jWebSocket - TypesMap (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter.validator;

import java.util.Map;
import org.jwebsocket.eventmodel.exception.InvalidTypeException;

/**
 *
 * @author Rolando Santamaria Maso
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
