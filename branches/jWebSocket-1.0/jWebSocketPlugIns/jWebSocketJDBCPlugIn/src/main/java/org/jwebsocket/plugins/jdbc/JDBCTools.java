//	---------------------------------------------------------------------------
//	jWebSocket - JDBCTools (Community Edition, CE)
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
package org.jwebsocket.plugins.jdbc;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.rowset.serial.SerialException;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.kit.WebSocketDataType;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 *
 * @author Alexander Schulze
 */
public class JDBCTools {

	/**
	 *
	 * @param aData
	 * @return
	 */
	/*
	public static String test(List aData) {
		return aData.toString();
	}
	*/
	
	/**
	 *
	 * @param aClassName
	 * @return
	 */
	public static String extractSimpleClass(String aClassName) {
		if (aClassName.equals("[B")) {
			return ("Blob");
		}
		if (aClassName.equals("[C")) {
			return ("Clob");
		}
		int lLastDotPos = aClassName.lastIndexOf('.');
		if (lLastDotPos >= 0) {
			aClassName = aClassName.substring(lLastDotPos + 1);
		}
		return (aClassName);
	}

	/**
	 *
	 * @param aJavaType
	 * @param aMetaData
	 * @return
	 */
	public static String getJSONType(String aJavaType, ResultSetMetaData aMetaData) {
		String lResStr = aJavaType.toLowerCase();
		if (lResStr != null) {
			if (lResStr.equals("bigdecimal")
					|| lResStr.equals("long")
					|| lResStr.equals("int")
					|| lResStr.equals("byte")
					|| lResStr.equals("short")
					|| lResStr.equals("float")
					|| lResStr.equals("double")) {
				lResStr = "number";
			} else if (lResStr.equals("date")
					|| lResStr.equals("time")
					|| lResStr.equals("timestamp")) {
				lResStr = "date";
			} else if (lResStr.equals("clob")
					|| lResStr.equals("blob")) {
				lResStr = "string";
			}
		}
		return lResStr;
	}

	/**
	 *
	 * @param aJavaType
	 * @param aMetaData
	 * @return
	 */
	public static String getJSONType(String aJavaType, SqlRowSetMetaData aMetaData) {
		String lResStr = aJavaType.toLowerCase();
		if (lResStr != null) {
			if (lResStr.equals("bigdecimal")
					|| lResStr.equals("long")
					|| lResStr.equals("int")
					|| lResStr.equals("byte")
					|| lResStr.equals("short")
					|| lResStr.equals("float")
					|| lResStr.equals("double")) {
				lResStr = "number";
			} else if (lResStr.equals("date")
					|| lResStr.equals("time")
					|| lResStr.equals("timestamp")) {
				lResStr = "date";
			} else if (lResStr.equals("clob")
					|| lResStr.equals("blob")) {
				lResStr = "string";
			}
		}
		return lResStr;
	}

	/**
	 *
	 * @param aJavaType
	 * @param aMetaData
	 * @return
	 */
	public static WebSocketDataType getDataType(String aJavaType, SqlRowSetMetaData aMetaData) {
		WebSocketDataType lType = WebSocketDataType.INVALID;
		String lResStr = aJavaType.toLowerCase();
		if (lResStr != null) {
			if (lResStr.equals("string")) {
				lType = WebSocketDataType.TEXT;
			} else if (lResStr.equals("bigdecimal")) {
				lType = WebSocketDataType.DOUBLE;
			} else if (lResStr.equals("long")) {
				lType = WebSocketDataType.LONG;
			} else if (lResStr.equals("int")) {
				lType = WebSocketDataType.INTEGER;
			} else if (lResStr.equals("byte")) {
				lType = WebSocketDataType.BYTE;
			} else if (lResStr.equals("short")) {
				lType = WebSocketDataType.INTEGER;
			} else if (lResStr.equals("float")) {
				lType = WebSocketDataType.FLOAT;
			} else if (lResStr.equals("double")) {
				lType = WebSocketDataType.DOUBLE;
			} else if (lResStr.equals("boolean")) {
				lType = WebSocketDataType.BOOLEAN;
			} else if (lResStr.equals("date")) {
				// Java SQL Date type contains date and time
				lType = WebSocketDataType.TIMESTAMP;
			} else if (lResStr.equals("time")) {
				// Java SQL Date type contains date and time
				lType = WebSocketDataType.TIMESTAMP;
			} else if (lResStr.equals("timestamp")) {
				lType = WebSocketDataType.TIMESTAMP;
			} else if (lResStr.equals("clob")) {
				lType = WebSocketDataType.TEXT;
			} else if (lResStr.equals("blob")) {
				lType = WebSocketDataType.BINARY;
			}
		}
		return lType;
	}

	/**
	 *
	 * @param aRowSet
	 * @param lColCount
	 * @return
	 */
	private static List<Object> getResultColumns(SqlRowSet aRowSet,
			WebSocketDataType[] lDataTypes)
			throws InvalidResultSetAccessException, SerialException {

		// TODO: should work with usual arrays!
		int lColCount = lDataTypes.length;
		List<Object> lDataRow = new FastList<Object>();
		// Caution! index for getObjects starts with 1!
		int lIdx = 1;
		WebSocketDataType lType;
		for (int lColIdx = 0; lColIdx < lColCount; lColIdx++) {
			lType = lDataTypes[lColIdx];
			Object lObj = aRowSet.getObject(lIdx);
			if (null != lObj) {
				if (lType.equals(WebSocketDataType.TEXT)) {
					if (lObj.getClass().equals(javax.sql.rowset.serial.SerialClob.class)) {
						javax.sql.rowset.serial.SerialClob lClob
								= (javax.sql.rowset.serial.SerialClob) lObj;
						// Caution! index for getSubString starts with 1!
						lObj = lClob.getSubString(1, (int) lClob.length());
					} else {
						lObj = aRowSet.getString(lIdx);
					}
				} else if (lType.equals(WebSocketDataType.LONG)) {
					lObj = aRowSet.getLong(lIdx);
				} else if (lType.equals(WebSocketDataType.INTEGER)) {
					lObj = aRowSet.getInt(lIdx);
				} else if (lType.equals(WebSocketDataType.BYTE)) {
					lObj = aRowSet.getByte(lIdx);
				} else if (lType.equals(WebSocketDataType.FLOAT)) {
					lObj = aRowSet.getFloat(lIdx);
				} else if (lType.equals(WebSocketDataType.DOUBLE)) {
					lObj = aRowSet.getDouble(lIdx);
				} else if (lType.equals(WebSocketDataType.BOOLEAN)) {
					lObj = aRowSet.getBoolean(lIdx);
				} else if (lType.equals(WebSocketDataType.TIME)) {
					lObj = aRowSet.getTime(lIdx);
				} else if (lType.equals(WebSocketDataType.DATE)) {
					lObj = aRowSet.getDate(lIdx);
				} else if (lType.equals(WebSocketDataType.TIMESTAMP)) {
					lObj = aRowSet.getTimestamp(lIdx);
				}
			}
			lDataRow.add(lObj);
			lIdx++;
		}
		return lDataRow;
	}

	/**
	 *
	 * @param aRowSet
	 * @return
	 * @throws InvalidResultSetAccessException
	 * @throws SerialException
	 */
	public static Token resultSetToToken(SqlRowSet aRowSet)
			throws InvalidResultSetAccessException, SerialException {

		// instantiate response token
		Token lResponse = TokenFactory.createToken();
		int lRowCount = 0;
		int lColCount;
		// TODO: should work with usual arrays as well!
		List<Map<String, Object>> lColumns = new FastList<Map<String, Object>>();
		List<Object> lData = new FastList<Object>();
		// TODO: metadata should be optional to save bandwidth!
		// generate the meta data for the response
		SqlRowSetMetaData lMeta = aRowSet.getMetaData();
		lColCount = lMeta.getColumnCount();
		lResponse.setInteger("colcount", lColCount);

		WebSocketDataType[] lDataTypes = new WebSocketDataType[lColCount];

		int lIdx = 1;
		for (int lColIdx = 0; lColIdx < lColCount; lColIdx++) {
			// get name of colmuns
			String lSimpleClass = extractSimpleClass(lMeta.getColumnClassName(lIdx));
			// convert to json type
			String lJSONType = getJSONType(lSimpleClass, lMeta);
			lDataTypes[lColIdx] = getDataType(lSimpleClass, lMeta);

			Map<String, Object> lColHeader = new FastMap<String, Object>();
			lColHeader.put("name", lMeta.getColumnName(lIdx));
			lColHeader.put("jsontype", lJSONType);
			lColHeader.put("jdbctype", lMeta.getColumnTypeName(lIdx));

			lColumns.add(lColHeader);
			lIdx++;
		}

		// generate the result data
		while (aRowSet.next()) {
			lData.add(getResultColumns(aRowSet, lDataTypes));
			lRowCount++;
		}

		// complete the response token
		lResponse.setInteger("rowcount", lRowCount);
		lResponse.setList("columns", lColumns);
		lResponse.setList("data", lData);

		return lResponse;
	}

	/**
	 *
	 * @param aFieldList
	 * @return
	 */
	public static String fieldListToString(List<String> aFieldList) {
		StringBuilder lRes = new StringBuilder();
		int lIdx = 0;
		int lCnt = aFieldList.size();
		for (Object lField : aFieldList) {
			lRes.append(lField);
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}

	/**
	 *
	 * @param aFieldList
	 * @return
	 */
	public static String fieldListToString(FastList<String> aFieldList) {
		return fieldListToString((List<String>) aFieldList);
	}

	/**
	 *
	 * @param aFieldList
	 * @return
	 */
	public static String fieldListToString(ArrayList<String> aFieldList) {
		return fieldListToString((List<String>) aFieldList);
	}

	/**
	 *
	 * @param lField
	 * @return
	 */
	public static String valueToString(Object lField) {
		String lRes;
		if (lField instanceof String
				&& !((String) lField).startsWith("TO_DATE")) {
			lRes = "'" + (String) lField + "'";
		} else {
			lRes = lField.toString();
		}
		return lRes;
	}

	/**
	 *
	 * @param aValueList
	 * @return
	 */
	public static String valueListToString(List<Object> aValueList) {
		StringBuilder lRes = new StringBuilder();
		int lIdx = 0;
		int lCnt = aValueList.size();
		for (Object lField : aValueList) {
			lRes.append(valueToString(lField));
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}

	/**
	 *
	 * @param aValueList
	 * @return
	 */
	public static String valueListToString(FastList<Object> aValueList) {
		return valueListToString((List<Object>) aValueList);
	}

	/**
	 *
	 * @param aValueList
	 * @return
	 */
	public static String valueListToString(ArrayList<Object> aValueList) {
		return valueListToString((List<Object>) aValueList);
	}
}
