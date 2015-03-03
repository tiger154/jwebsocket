// ---------------------------------------------------------------------------
// jWebSocket - JSON Token Processor (Community Edition, CE)
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
package org.jwebsocket.packetProcessors;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.token.MapToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.MapAppender;

/**
 * converts JSON formatted data packets into tokens and vice versa.
 *
 * @author Alexander Schulze, Roderick Baier (improvements regarding JSON
 * array), Quentin Ambard (add support for Map and List for PacketToToken and
 * tokeToPacket).
 * @author Rolando Santamaria Maso
 */
@SuppressWarnings("rawtypes")
public class JSONProcessor {

	/**
	 * Convert a JSON string to a token. If the JSON string isn't a valid one,
	 * return an empty token. Note that if you need a more generic conversion
	 * (other sub protocol than JSON), you may also use the following: Token
	 * lToken = TokenServer.packetToToken(aConnector, new
	 * RawPacket(aJsonString)) Depending of the SubProtocol of aConnector, the
	 * token will be automatically created (if the SubProtocol is
	 * WS_SUBPROT_JSON, the conversion will be done internally using this
	 * method)
	 *
	 * @param aJSONString a JSON string
	 * @return the token corresponding to the JSON string, or an empty token
	 */
	public static Token JSONStringToToken(String aJSONString) {
		Token lToken = new MapToken();
		try {
			ObjectMapper lMapper = new ObjectMapper();
			Map<String, Object> lTree = lMapper.readValue(aJSONString, Map.class);
			lToken.setMap(lTree);
		} catch (Exception lEx) {
			// // TODO: process exception
			// log.error(ex.getClass().getSimpleName() + ": " +
			// ex.getMessage());
		}
		return lToken;
	}

	/**
	 * converts a JSON formatted data packet into a token.
	 *
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(WebSocketPacket aDataPacket) {
		Token lToken = null;
		try {
			lToken = JSONStringToToken(aDataPacket.getString("UTF-8"));
		} catch (Exception lEx) {
			// // TODO: process exception
			// log.error(ex.getClass().getSimpleName() + ": " +
			// ex.getMessage());
		}
		return lToken;
	}

	/**
	 *
	 * @param aList
	 * @param aBuffer
	 */
	public static void listToJSONString(List aList, StringBuffer aBuffer) {
		aBuffer.append("[");
		for (Iterator lIt = aList.iterator(); lIt.hasNext();) {
			Object lObj = lIt.next();

			objectToJSONString(lObj, aBuffer);

			if (lIt.hasNext()) {
				aBuffer.append(",");
			}
		}
		aBuffer.append("]");
	}

	/**
	 *
	 * @param aArray
	 * @param aBuffer
	 */
	public static void arrayToJSONString(Object[] aArray, StringBuffer aBuffer) {
		aBuffer.append("[");
		boolean lWritten = false;

		if (aArray.length > 0) {
			for (Object lObj : aArray) {

				objectToJSONString(lObj, aBuffer);
				lWritten = true;
				aBuffer.append(",");
			}
		}

		if (lWritten) {
			aBuffer.deleteCharAt(aBuffer.length() - 1);
		}
		aBuffer.append("]");
	}

	/**
	 *
	 * @param aMap
	 * @param aBuffer
	 */
	public static void mapToJSONString(Map aMap, StringBuffer aBuffer) {
		aBuffer.append("{");
		for (Iterator lIt = aMap.entrySet().iterator(); lIt.hasNext();) {
			Map.Entry lE = (Map.Entry) lIt.next();
			String lK = lE.getKey().toString();
			Object lV = lE.getValue();

			aBuffer.append("\"").append(lK).append("\":");
			objectToJSONString(lV, aBuffer);

			if (lIt.hasNext()) {
				aBuffer.append(",");
			}
		}
		aBuffer.append("}");
	}

	/**
	 * Gets the JSON string representation of a given object.
	 *
	 * @param aObject
	 * @return The JSON string
	 */
	public static String objectToJSONString(Object aObject) {
		StringBuffer lBuffer = new StringBuffer();
		objectToJSONString(aObject, lBuffer);

		return lBuffer.toString();
	}

	/**
	 * Gets the JSON string representation of a given object. The string value
	 * is copied into the StringBuffer instance.
	 *
	 * @param aObject
	 * @param aBuffer
	 */
	public static void objectToJSONString(Object aObject, StringBuffer aBuffer) {
		if (null == aObject) {
			aBuffer.append("null");
		} else if (aObject instanceof String || aObject instanceof WebSocketPacket) {
			aBuffer.append("\"").append(escapeForJSON(aObject.toString())).append("\"");
		} else if (aObject instanceof Integer) {
			aBuffer.append(((Integer) aObject).toString());
		} else if (aObject instanceof Object[]) {
			arrayToJSONString((Object[]) aObject, aBuffer);
		} else if (aObject instanceof Double) {
			aBuffer.append(((Double) aObject).toString());
		} else if (aObject instanceof Long) {
			aBuffer.append(((Long) aObject).toString());
		} else if (aObject instanceof Boolean) {
			aBuffer.append(((Boolean) aObject).toString());
		} else if (aObject instanceof Token) {
			objectToJSONString(((Token) aObject).getMap(), aBuffer);
		} else if (aObject instanceof ITokenizable) {
			Token lToken = TokenFactory.createToken();
			((ITokenizable) aObject).writeToToken(lToken);
			objectToJSONString(((Token) lToken).getMap(), aBuffer);
		} else if (aObject instanceof List) {
			listToJSONString((List) aObject, aBuffer);
		} else if (aObject instanceof Map) {
			mapToJSONString((Map) aObject, aBuffer);
		} else if (aObject instanceof MapAppender) {
			mapToJSONString(((MapAppender) aObject).getMap(), aBuffer);
		} else {
			aBuffer.append("\"").append(escapeForJSON(aObject.toString())).append("\"");
		}
	}

	/**
	 * Escapes a string value for JSON compatibility.
	 *
	 * @param aValue
	 * @return
	 */
	public static String escapeForJSON(String aValue) {
		aValue = aValue
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\t", "\\t")
				.replace("\r", "\\r")
				.replace("\b", "\\b")
				.replace("\f", "\\f");
		return aValue;
	}

	/**
	 * Transforms a Token object into a WebSocketPacket object
	 *
	 * @param aToken
	 * @return
	 */
	public static WebSocketPacket tokenToPacket(Token aToken) {
		WebSocketPacket lPacket = null;
		StringBuffer lBuffer = new StringBuffer();

		try {
			objectToJSONString(aToken.getMap(), lBuffer);
			lPacket = new RawPacket(lBuffer.toString(), "UTF-8");
		} catch (Exception lEx) {
		}

		return lPacket;
	}

	/**
	 * transform a list to a JSONArray
	 *
	 * @param aList
	 * @return a JSONArray which represents aList
	 * @throws JSONException
	 */
	public static JSONArray listToJSONArray(List aList) throws JSONException {
		JSONArray lArray = new JSONArray();
		for (Object item : aList) {
			lArray.put(convertObjectToJSON(item));
		}
		return lArray;
	}

	/**
	 * transform a list of objects to a JSONArray
	 *
	 * @param aObjectList
	 * @return a JSONArray which represents aObjectList
	 * @throws JSONException
	 */
	public static JSONArray objectListToJSONArray(Object[] aObjectList)
			throws JSONException {
		JSONArray lArray = new JSONArray();
		for (Object lObj : aObjectList) {
			lArray.put(convertObjectToJSON(lObj));
		}
		return lArray;
	}

	/**
	 * transform a map to a JSONObject
	 *
	 * @param aMap
	 * @return a JSONObject which represents aMap. All the keys values are
	 * passed as String using the toString method of the key.
	 * @throws JSONException
	 */
	public static JSONObject mapToJSONObject(Map<?, ?> aMap) throws JSONException {
		JSONObject lObject = new JSONObject();
		for (Entry<?, ?> lEntry : aMap.entrySet()) {
			String lKey = lEntry.getKey().toString();
			Object lValue = convertObjectToJSON(lEntry.getValue());
			lObject.put(lKey, lValue);
		}
		return lObject;
	}

	/**
	 * transform an object to another JSON object (match all possibilities)
	 *
	 * @param aObject
	 * @return an Object which represents aObject
	 * @throws JSONException
	 */
	public static Object convertObjectToJSON(Object aObject)
			throws JSONException {
		if (aObject instanceof List) {
			return listToJSONArray((List) aObject);
		} else if (aObject instanceof ITokenizable) {
			Token lToken = TokenFactory.createToken();
			((ITokenizable) aObject).writeToToken(lToken);
			return tokenToJSON(lToken);
		} else if (aObject instanceof Token) {
			return tokenToJSON((Token) aObject);
		} else if (aObject instanceof Object[]) {
			return objectListToJSONArray((Object[]) aObject);
		} else if (aObject instanceof Map) {
			return mapToJSONObject((Map<?, ?>) aObject);
		} else if (aObject instanceof WebSocketPacket || aObject instanceof String) {
			return escapeForJSON((String) aObject);
		} else {
			return aObject;
		}
	}

	/**
	 * transform a token to a JSON object
	 *
	 * @param aToken
	 * @return a JSONObject which represents aToken (looks for List, Token and
	 * Maps)
	 * @throws JSONException
	 */
	public static JSONObject tokenToJSON(Token aToken) throws JSONException {
		JSONObject lJSO = new JSONObject();
		Iterator<String> iterator = aToken.getKeyIterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = aToken.getObject(key);
			lJSO.put(key, convertObjectToJSON(value));
		}
		return lJSO;
	}
}
