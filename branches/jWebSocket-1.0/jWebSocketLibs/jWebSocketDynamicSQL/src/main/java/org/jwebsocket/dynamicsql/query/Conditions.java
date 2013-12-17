//	---------------------------------------------------------------------------
//	jWebSocket - ClassPathUpdater (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
 * @author markos
 */
public class Conditions {
    
    public static String ATTR_TYPE = "conditionType";
    public static String ATTR_COLUMN_NAME = "conditionColumnName";
    public static String ATTR_VALUE = "conditionValue";
    public static String ATTR_INCLUSIVE = "conditionInclusive";

    public static ICondition lessThan(String aColumnName, Object aValue, Boolean aInclusive) {
        Map<String, Object> lAttrs = new FastMap<String, Object>();
        lAttrs.put(ATTR_TYPE, BinaryCondition.Op.LESS_THAN);
        lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
        lAttrs.put(ATTR_VALUE, aValue);
        lAttrs.put(ATTR_INCLUSIVE, aInclusive);
        return new Condition(lAttrs);
    }

    public static ICondition greaterThan(String aColumnName, Object aValue, Boolean aInclusive) {
        Map<String, Object> lAttrs = new FastMap<String, Object>();
        lAttrs.put(ATTR_TYPE, BinaryCondition.Op.GREATER_THAN);
        lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
        lAttrs.put(ATTR_VALUE, aValue);
        lAttrs.put(ATTR_INCLUSIVE, aInclusive);
        return new Condition(lAttrs);
    }

    public static ICondition equalTo(String aColumnName, Object aValue) {
        Map<String, Object> lAttrs = new FastMap<String, Object>();
        lAttrs.put(ATTR_TYPE, BinaryCondition.Op.EQUAL_TO);
        lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
        lAttrs.put(ATTR_VALUE, aValue);
        return new Condition(lAttrs);
    }

    public static ICondition notEqualTo(String aColumnName, Object aValue) {
        Map<String, Object> lAttrs = new FastMap<String, Object>();
        lAttrs.put(ATTR_TYPE, BinaryCondition.Op.NOT_EQUAL_TO);
        lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
        lAttrs.put(ATTR_VALUE, aValue);
        return new Condition(lAttrs);
    }

    public static ICondition like(String aColumnName, Object aValue) {
        Map<String, Object> lAttrs = new FastMap<String, Object>();
        lAttrs.put(ATTR_TYPE, BinaryCondition.Op.LIKE);
        lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
        lAttrs.put(ATTR_VALUE, aValue);
        return new Condition(lAttrs);
    }

    public static ICondition notLike(String aColumnName, Object aValue) {
        Map<String, Object> lAttrs = new FastMap<String, Object>();
        lAttrs.put(ATTR_TYPE, BinaryCondition.Op.NOT_LIKE);
        lAttrs.put(ATTR_COLUMN_NAME, aColumnName);
        lAttrs.put(ATTR_VALUE, aValue);
        return new Condition(lAttrs);
    }
}
