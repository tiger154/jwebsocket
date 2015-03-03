//	---------------------------------------------------------------------------
//	jWebSocket - ClassPathUpdater (Community Edition, CE)
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
package org.jwebsocket.dynamicsql.query;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.dynamicsql.api.ICondition;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class Conditions {

	// Key attributes that contains a map condition.
	/**
	 *
	 */
	public static String ATTR_TYPE = "conditionType";

	/**
	 *
	 */
	public static String ATTR_COLUMN_NAME = "conditionColumnName";

	/**
	 *
	 */
	public static String ATTR_VALUE = "conditionValue";

	/**
	 *
	 */
	public static String ATTR_INCLUSIVE = "conditionInclusive";

	/**
	 * Returns a less than condition.
	 *
	 * @param aColumnName The column name.
	 * @param aValue The value of column.
	 * @param aInclusive If value is <TRUE> the operator of the condition would
	 * be '<=', otherwise it would be only '<'.
	 * @return a less than condition.
	 */
	public static ICondition lessThan(String aColumnName, Object aValue, Boolean aInclusive) {
		Map<String, Object> lAttrs = new FastMap<String, Object>();
		lAttrs.put(ATTR_TYPE, BinaryCondition.Op.LESS_THAN);
		lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
		lAttrs.put(ATTR_VALUE, aValue);
		lAttrs.put(ATTR_INCLUSIVE, aInclusive);
		return new Condition(lAttrs);
	}

	/**
	 * Returns a greater than condition.
	 *
	 * @param aColumnName The column name.
	 * @param aValue The value of column.
	 * @param aInclusive If value is <TRUE> the operator of the condition would
	 * be '>=', otherwise it would be only '>'.
	 * @return a greater than condition.
	 */
	public static ICondition greaterThan(String aColumnName, Object aValue, Boolean aInclusive) {
		Map<String, Object> lAttrs = new FastMap<String, Object>();
		lAttrs.put(ATTR_TYPE, BinaryCondition.Op.GREATER_THAN);
		lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
		lAttrs.put(ATTR_VALUE, aValue);
		lAttrs.put(ATTR_INCLUSIVE, aInclusive);
		return new Condition(lAttrs);
	}

	/**
	 * Returns a equalTo condition.
	 *
	 * @param aColumnName The column name.
	 * @param aValue The value of column.
	 *
	 * @return a equalTo condition.
	 */
	public static ICondition equalTo(String aColumnName, Object aValue) {
		Map<String, Object> lAttrs = new FastMap<String, Object>();
		lAttrs.put(ATTR_TYPE, BinaryCondition.Op.EQUAL_TO);
		lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
		lAttrs.put(ATTR_VALUE, aValue);
		return new Condition(lAttrs);
	}

	/**
	 * Returns a not equalTo condition.
	 *
	 * @param aColumnName The column name.
	 * @param aValue The value of column.
	 *
	 * @return a not equalTo condition.
	 */
	public static ICondition notEqualTo(String aColumnName, Object aValue) {
		Map<String, Object> lAttrs = new FastMap<String, Object>();
		lAttrs.put(ATTR_TYPE, BinaryCondition.Op.NOT_EQUAL_TO);
		lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
		lAttrs.put(ATTR_VALUE, aValue);
		return new Condition(lAttrs);
	}

	/**
	 * Returns a like condition.
	 *
	 * @param aColumnName The column name.
	 * @param aValue The value of column.
	 *
	 * @return a like condition.
	 */
	public static ICondition like(String aColumnName, Object aValue) {
		Map<String, Object> lAttrs = new FastMap<String, Object>();
		lAttrs.put(ATTR_TYPE, BinaryCondition.Op.LIKE);
		lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
		lAttrs.put(ATTR_VALUE, aValue);
		return new Condition(lAttrs);
	}

	/**
	 * Returns a not like condition.
	 *
	 * @param aColumnName The column name.
	 * @param aValue The value of column.
	 *
	 * @return a not like condition.
	 */
	public static ICondition notLike(String aColumnName, Object aValue) {
		Map<String, Object> lAttrs = new FastMap<String, Object>();
		lAttrs.put(ATTR_TYPE, BinaryCondition.Op.NOT_LIKE);
		lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
		lAttrs.put(ATTR_VALUE, aValue);
		return new Condition(lAttrs);
	}
}
