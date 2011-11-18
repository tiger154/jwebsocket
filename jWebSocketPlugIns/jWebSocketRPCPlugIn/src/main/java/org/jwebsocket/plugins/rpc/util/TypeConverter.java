//	---------------------------------------------------------------------------
//	jWebSocket - RPC PlugIn TypeConverter
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.rpc.util;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.rpc.RPCPlugIn;
import org.jwebsocket.token.ITokenizable;
import org.jwebsocket.token.Token;

/**
 * map javascript types with java types
 * @author Quentin Ambard
 */
public class TypeConverter {

	private static Logger mLog = Logging.getLogger(RPCPlugIn.class);
	
	//allowed type that can be found in the .xml file
	public final static String PROTOCOL_TYPE_INT = "int";
	public final static String PROTOCOL_TYPE_BOOLEAN = "boolean";
	public final static String PROTOCOL_TYPE_STRING = "string";
	public final static String PROTOCOL_TYPE_MAP = "map";
	public final static String PROTOCOL_TYPE_DOUBLE = "double";
	public final static String PROTOCOL_TYPE_ARRAY = "array";
	public final static String PROTOCOL_TYPE_TOKENIZABLE = "tokenizable";
	private final static Map<String, String> mProtocolValue = new FastMap<String, String>();
	private final static List<String> mProtocolList = new FastList<String>();

	static {
		mProtocolList.add(PROTOCOL_TYPE_INT);
		mProtocolList.add(PROTOCOL_TYPE_BOOLEAN);
		mProtocolList.add(PROTOCOL_TYPE_STRING);
		mProtocolList.add(PROTOCOL_TYPE_MAP);
		mProtocolList.add(PROTOCOL_TYPE_DOUBLE);
		mProtocolList.add(PROTOCOL_TYPE_ARRAY);
		mProtocolList.add(PROTOCOL_TYPE_TOKENIZABLE);

		mProtocolValue.put(boolean.class.getName(), PROTOCOL_TYPE_BOOLEAN);
		mProtocolValue.put(Boolean.class.getName(), PROTOCOL_TYPE_BOOLEAN);
		mProtocolValue.put(double.class.getName(), PROTOCOL_TYPE_DOUBLE);
		mProtocolValue.put(Double.class.getName(), PROTOCOL_TYPE_DOUBLE);
		mProtocolValue.put(Integer.class.getName(), PROTOCOL_TYPE_INT);
		mProtocolValue.put(int.class.getName(), PROTOCOL_TYPE_INT);
		mProtocolValue.put(String.class.getName(), PROTOCOL_TYPE_STRING);
		mProtocolValue.put(List.class.getName(), PROTOCOL_TYPE_ARRAY);
		mProtocolValue.put(Map.class.getName(), PROTOCOL_TYPE_MAP);
		mProtocolValue.put(Token.class.getName(), PROTOCOL_TYPE_MAP);
		mProtocolValue.put(ITokenizable.class.getName(), PROTOCOL_TYPE_MAP);
	}

	/**
	 * 
	 * @param aProtocolType
	 * @return true if "aProtocolType" (value found in the jwebsocket.xml for instance) is a correct type
	 */
	public static boolean isValidProtocolType(String aProtocolType) {
		return mProtocolList.contains(aProtocolType);
	}

	/**
	 * @param aJavaType
	 * @return protocol value that match with aJavaType, for instance String ==> string, Token ==> map...
	 */
	public static String getProtocolValue(String aJavaType) {
		return mProtocolValue.get(aJavaType);
	}

	/**
	 * @param aJavaClass
	 * @return protocol value that match with aJavaType, for instance String ==> string, Token ==> map...
	 */
	public static String getProtocolValue(Class aJavaClass) {
		return getProtocolValue(aJavaClass.getName());
	}

	/**
	 * @param aJavaType
	 * @return true if aJavaType is allowed in the protocol
	 */
	public static boolean isValidProtocolJavaType(String aJavaType) {
		return mProtocolValue.containsKey(aJavaType);
	}

	/**
	 * 
	 * @param aJavaClass
	 * @return true if aJavaClass is allowed in the protocol
	 */
	public static boolean isValidProtocolJavaType(Class aJavaClass) {
		boolean lIsValid = isValidProtocolJavaType(aJavaClass.getName());
		if (!lIsValid) {
			Class[] lInterfaces = aJavaClass.getInterfaces();
			for (int lIdx = 0; lIdx < lInterfaces.length; lIdx++) {
				Class lIF = lInterfaces[lIdx];
				lIsValid = lIF.getSimpleName().equals("ITokenizable");
				if (lIsValid) {
					break;
				}
			}
		}
		return lIsValid;
	}

	/**
	 * 
	 * @param aProtocolType
	 * @param aJavaType
	 * @return true if aProtocolType match with aJavaType, for instance (map, Token) returns true
	 */
	public static boolean matchProtocolTypeToJavaType(String aProtocolType, String aJavaType) {
		if (!mProtocolValue.containsKey(aJavaType)) {
			return false;
		} else {
			return mProtocolValue.get(aJavaType).equals(aProtocolType);
		}
	}

	/**
	 * 
	 * @param aProtocolType
	 * @param aJavaClass
	 * @return true if aProtocolType match with aJavaClass, for instance (map, Token) returns true
	 */
	public static boolean matchProtocolTypeToJavaType(String aProtocolType, Class aJavaClass) {
		return matchProtocolTypeToJavaType(aProtocolType, aJavaClass.getName());
	}

	/**
	 * @return the list of valid parameter types that can be found in the jwebsocket.xml file
	 */
	public static String getValidParameterTypes() {
		StringBuilder lValidParameters = new StringBuilder();
		for (String type : mProtocolList) {
			lValidParameters.append(type + ", ");
		}
		return lValidParameters.toString();
	}
}
