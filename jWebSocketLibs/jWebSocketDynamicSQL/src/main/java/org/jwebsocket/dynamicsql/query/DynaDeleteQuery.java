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
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import java.util.Map;
import org.jwebsocket.dynamicsql.SupportUtils;
import org.jwebsocket.dynamicsql.api.ICondition;
import org.jwebsocket.dynamicsql.api.IDatabase;
import org.jwebsocket.dynamicsql.api.IDeleteQuery;

/**
 *
 * @author markos
 */
public class DynaDeleteQuery implements IDeleteQuery {

    private DeleteQuery mQuery;
    private IDatabase mDB;

    public DynaDeleteQuery(IDatabase aDB, String aTableName) {
        this.mDB = aDB;
        mQuery = new DeleteQuery(parse(aTableName));
    }

    @Override
    public IDeleteQuery and(ICondition aCondition) {
        Map<String, Object> lAttrs = aCondition.getCondition();
        BinaryCondition.Op lType = (BinaryCondition.Op) lAttrs.get(Conditions.ATTR_TYPE);
        Object lColumnName = parse((String) lAttrs.get(Conditions.ATTR_COLUMN_NAME));

        if (lType == BinaryCondition.Op.GREATER_THAN) {
            mQuery.addCondition(ComboCondition.and(BinaryCondition
                    .greaterThan(lColumnName,
                    lAttrs.get(Conditions.ATTR_VALUE),
                    (Boolean) lAttrs.get(Conditions.ATTR_INCLUSIVE))));
        } else if (lType == BinaryCondition.Op.LESS_THAN) {
            mQuery.addCondition(ComboCondition.and(BinaryCondition
                    .lessThan(lColumnName,
                    lAttrs.get(Conditions.ATTR_VALUE),
                    (Boolean) lAttrs.get(Conditions.ATTR_INCLUSIVE))));
        } else {
            mQuery.addCondition(ComboCondition.and(new BinaryCondition(lType,
                    lColumnName,
                    lAttrs.get(Conditions.ATTR_VALUE))));
        }

        return this;
    }

    @Override
    public IDeleteQuery or(ICondition aCondition) {
        Map<String, Object> lAttrs = aCondition.getCondition();
        BinaryCondition.Op lType = (BinaryCondition.Op) lAttrs.get(Conditions.ATTR_TYPE);
        Object lColumnName = parse((String) lAttrs.get(Conditions.ATTR_COLUMN_NAME));

        if (lType == BinaryCondition.Op.GREATER_THAN) {
            mQuery.addCondition(ComboCondition.or(BinaryCondition
                    .greaterThan(lColumnName,
                    lAttrs.get(Conditions.ATTR_VALUE),
                    (Boolean) lAttrs.get(Conditions.ATTR_INCLUSIVE))));
        } else if (lType == BinaryCondition.Op.LESS_THAN) {
            mQuery.addCondition(ComboCondition.or(BinaryCondition
                    .lessThan(lColumnName,
                    lAttrs.get(Conditions.ATTR_VALUE),
                    (Boolean) lAttrs.get(Conditions.ATTR_INCLUSIVE))));
        } else {
            mQuery.addCondition(ComboCondition.or(new BinaryCondition(lType,
                    lColumnName,
                    lAttrs.get(Conditions.ATTR_VALUE))));
        }

        return this;
    }

    @Override
    public String getSQL() {
        return mQuery.validate().toString().replaceAll("%", getEscLike());
    }

    private Object parse(String aValue) {
        return new CustomSql(getEscChar().charAt(0) + aValue + getEscChar().charAt(1));
    }

    private String getEscChar() {
        return mDB.getOptions().get(SupportUtils.ESCAPE_TABLE_LITERAL);
    }

    private String getEscLike() {
        return mDB.getOptions().get(SupportUtils.ESCAPE_LIKE_LITERAL);
    }
}
