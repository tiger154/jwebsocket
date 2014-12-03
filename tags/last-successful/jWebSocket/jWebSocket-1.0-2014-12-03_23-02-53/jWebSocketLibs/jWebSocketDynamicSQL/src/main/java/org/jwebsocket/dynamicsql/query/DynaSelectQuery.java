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
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.OrderObject;
import org.jwebsocket.dynamicsql.api.ICondition;
import org.jwebsocket.dynamicsql.api.ISelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.util.Map;
import org.jwebsocket.dynamicsql.SupportUtils;
import org.jwebsocket.dynamicsql.api.IDatabase;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class DynaSelectQuery implements ISelectQuery {

	private SelectQuery mQuery;
	private IDatabase mDB;

	/**
	 * Costructor.
	 *
	 * @param aDB The database instance.
	 * @param aTableName The table name.
	 */
	public DynaSelectQuery(IDatabase aDB, String aTableName) {
		this.mDB = aDB;
		mQuery = new SelectQuery()
				.addAllColumns()
				.addCustomFromTable(parse(aTableName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISelectQuery and(ICondition aCondition) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISelectQuery or(ICondition aCondition) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISelectQuery orderBy(String aColumnName, Ordering aDir) {
		OrderObject.Dir lDir = OrderObject.Dir.ASCENDING;
		if (aDir == Ordering.DESCENDING) {
			lDir = OrderObject.Dir.DESCENDING;
		}
		mQuery.addCustomOrdering(parse(aColumnName), lDir);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSQL() {
		return mQuery.validate().toString().replaceAll("%", getEscLike());
	}

	/**
	 * Allows to parse the names of tables and column in the diferents platform.
	 *
	 * @param aValue The value (name of table or column)
	 * @return The parse value.
	 */
	private Object parse(String aValue) {
		return new CustomSql(getEscChar().charAt(0) + aValue + getEscChar().charAt(1));
	}

	/**
	 * The escape char to tables and columns name in the diferents platform.
	 *
	 * @return The escape char.
	 */
	private String getEscChar() {
		return mDB.getOptions().get(SupportUtils.ESCAPE_TABLE_LITERAL);
	}

	/**
	 * The escape char to like condition in the diferents platform.
	 *
	 * @return The escape char.
	 */
	private String getEscLike() {
		return mDB.getOptions().get(SupportUtils.ESCAPE_LIKE_LITERAL);
	}
}
