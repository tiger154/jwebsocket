//	---------------------------------------------------------------------------
//	jWebSocket - JDBCTools
//	Copyright (c) 2010 Innotrade GmbH (http://jWebSocket.org)
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
//  for more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jdbc;

import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;
import javax.sql.rowset.serial.SerialClob;
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
 * @author aschulze
 */
public class JDBCTools {

	/**
	 * 
	 * @param aData
	 * @return
	 */
	public static String test(List aData) {
		return aData.toString();
	}

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
		WebSocketDataType lType = WebSocketDataType.INVALID;
		for (int lColIdx = 0; lColIdx < lColCount; lColIdx++) {
			lType = lDataTypes[lColIdx];
			Object lObj = aRowSet.getObject(lIdx);
			if (null != lObj) {
				if (lType.equals(WebSocketDataType.TEXT)) {
					if (lObj.getClass().equals(javax.sql.rowset.serial.SerialClob.class)) {
						javax.sql.rowset.serial.SerialClob lClob =
								(javax.sql.rowset.serial.SerialClob) lObj;
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
	 */
	public static Token resultSetToToken(SqlRowSet aRowSet)
			throws InvalidResultSetAccessException, SerialException {

		// instantiate response token
		Token lResponse = TokenFactory.createToken();
		int lRowCount = 0;
		int lColCount = 0;
		// TODO: should work with usual arrays as well!
		List<Map> lColumns = new FastList<Map>();
		List lData = new FastList();
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
	public static String fieldListToString(FastList aFieldList) {
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
	public static String fieldListToString(List aFieldList) {
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
	 * @param aFieldList
	 * @return
	 */
	public static String valueListToString(List aFieldList) {
		StringBuilder lRes = new StringBuilder();
		int lIdx = 0;
		int lCnt = aFieldList.size();
		for (Object lField : aFieldList) {
			lRes.append(valueToString(lField));
			/*
			if (lField instanceof String
			&& !((String) lField).startsWith("TO_DATE")) {
			lRes.append("'");
			lRes.append(lField);
			lRes.append("'");
			} else {
			lRes.append(lField);
			}
			 */
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}
}
