// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn v1.0
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.plugins.jmx.util;

import java.util.List;
import java.util.Map;
import javax.management.openmbean.*;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * Class that dynamically converts the Map data type to the CompositeData data 
 * type.  
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXHandler {

	private static Logger mLog = Logging.getLogger();

	/**
	 * Determine if the data type of the values contained within the map is 
	 * simple or not.
	 * 
	 * @param aMap
	 * @return Boolean
	 */
	public static Boolean isSimpleType(Map aMap) {
		for (int i = 0; i < aMap.size(); i++) {
			Object lValue = aMap.values().toArray()[i];
			if ((lValue instanceof Map) || (lValue instanceof Token) 
					|| (lValue instanceof List)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * It allows to dynamically set the values of CompositeType data type.
	 * 
	 * @param aMap
	 * @param aKey
	 * @return CompositeType
	 */
	public static CompositeType createDynamicCompositeType(Map aMap, String aKey) {
		CompositeType lMapType = null;
		try {
			//attributes names and descriptions
			String[] lItemNames = new String[aMap.size()];
			String[] lItemDescriptions = new String[aMap.size()];
			for (int i = 0; i < aMap.size(); i++) {
				lItemNames[i] = aMap.keySet().toArray()[i].toString();
				lItemDescriptions[i] = aMap.keySet().toArray()[i].toString();
			}

			//attributes data types
			OpenType[] lItemTypes = new OpenType[aMap.size()];

			for (int i = 0; i < aMap.size(); i++) {
				Object lValue = aMap.values().toArray()[i];
				if (lValue instanceof String) {
					lItemTypes[i] = SimpleType.STRING;
				} else if (lValue instanceof Integer) {
					lItemTypes[i] = SimpleType.INTEGER;
				} else if (lValue instanceof Boolean) {
					lItemTypes[i] = SimpleType.BOOLEAN;
				} else if (lValue instanceof Double) {
					lItemTypes[i] = SimpleType.DOUBLE;
				} else if (lValue instanceof CompositeData) {
					lItemTypes[i] = ((CompositeData) lValue).getCompositeType();
				} else if (lValue == null) {
					lItemTypes[i] = SimpleType.STRING;
				}
			}
			lMapType = new CompositeType(
					aKey,
					"Composite Type that represent the response map",
					lItemNames,
					lItemDescriptions,
					lItemTypes);

		} catch (OpenDataException e) {
			mLog.error("JMXHandler on actionPerformed: " + e.getMessage());
		}
		return lMapType;
	}

	/**
	 * It allows to dynamically set the values of CompositeData data type.
	 * 
	 * @param aMap
	 * @param aMapType
	 * @return CompositeData
	 */
	public static CompositeData createDynamicCompositeData(Map aMap, 
			CompositeType aMapType) {
		CompositeData lMapData = null;
		try {
			String[] lItemNames = new String[aMap.size()];
			Object[] lItemValues = new Object[aMap.size()];

			for (int i = 0; i < aMap.size(); i++) {
				lItemNames[i] = aMap.keySet().toArray()[i].toString();
				if (aMap.values().toArray()[i] == null) {
					lItemValues[i] = "";
				} else {
					lItemValues[i] = aMap.values().toArray()[i];
				}
			}
			lMapData = new CompositeDataSupport(aMapType, lItemNames, 
					lItemValues);
		} catch (Exception e) {
			mLog.error("JMXHandler on actionPerformed: " + e.getMessage());
		}
		return lMapData;
	}

	/**
	 * Recursive method to convert the Map data type to the CompositeData data 
	 * type.
	 * 
	 * @param aMap
	 * @param aKey
	 * @return CompositeData
	 */
	public static CompositeData convertMapToCompositeData(Map aMap, String aKey) {
		if (isSimpleType(aMap)) {
			CompositeType lMapType = createDynamicCompositeType(aMap, aKey);
			return createDynamicCompositeData(aMap, lMapType);
		}

		String[] lItemNames = new String[aMap.size()];
		Object[] lItemValues = new Object[aMap.size()];
		OpenType[] lItemTypes = new OpenType[aMap.size()];

		for (int i = 0; i < aMap.size(); i++) {
			Object lValue = aMap.values().toArray()[i];
			if ((lValue instanceof Map) || (lValue instanceof Token) 
					|| (lValue instanceof List)) {
				Map lMap = null;
				if (lValue instanceof Token) {
					Token lToken = (Token) lValue;
					lToken.remove("ns");
					lToken.remove("type");
					lMap = lToken.getMap();
				} else if (lValue instanceof List) {
					List lList = (List) lValue;
					lMap = convertListToMap(lList, aMap.keySet().toArray()[i].toString());
				} else {
					lMap = (Map) lValue;
				}

				CompositeData lCompositeData = convertMapToCompositeData(lMap, 
						aMap.keySet().toArray()[i].toString());
				aMap.put(aMap.keySet().toArray()[i].toString(), lCompositeData);
				lItemValues[i] = lCompositeData;
				lItemTypes[i] = lCompositeData.getCompositeType();
				lItemNames[i] = aMap.keySet().toArray()[i].toString();
			} else {
				lItemNames[i] = aMap.keySet().toArray()[i].toString();
				lItemValues[i] = lValue;

				if (lValue instanceof String) {
					lItemTypes[i] = SimpleType.STRING;
				} else if (lValue instanceof Integer) {
					lItemTypes[i] = SimpleType.INTEGER;
				} else if (lValue instanceof Boolean) {
					lItemTypes[i] = SimpleType.BOOLEAN;
				} else if (lValue instanceof Double) {
					lItemTypes[i] = SimpleType.DOUBLE;
				}
			}
		}
		CompositeType lMapType = createDynamicCompositeType(aMap, aKey);
		return createDynamicCompositeData(aMap, lMapType);
	}

	/**
	 * Main method of the class which calls the recursive method that allows the
	 * conversion.
	 * 
	 * @param aMap
	 * @return CompositeData
	 */
	public static CompositeData convertMapToCompositeData(Map aMap) {
		return convertMapToCompositeData(aMap, "Main");
	}

	/**
	 * Converts a list on a map.
	 * 
	 * @param aList
	 * @param aKey
	 * @return Map
	 */
	public static Map convertListToMap(List aList, String aKey) {
		Map lMap = new FastMap();
		for (int i = 1; i <= aList.size(); i++) {
			lMap.put(aKey + "_" + i, aList.get(i - 1));
		}
		return lMap;
	}
}
