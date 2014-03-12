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
package org.jwebsocket.dynamicsql;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;
import org.jwebsocket.dynamicsql.api.ITable;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class DynaTable implements ITable {

	private Table mTable;

	/**
	 * Constructor
	 *
	 * @param aName
	 */
	public DynaTable(String aName) {
		mTable = new Table();
		mTable.setName(aName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return mTable.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITable addColumn(String aName, Integer aTypeCode, Boolean aRequired,
			Boolean aPK, Integer aSize, Object aDefault) {
		Column lColumn = new Column();
		lColumn.setName(aName);
		lColumn.setTypeCode(aTypeCode);
		lColumn.setRequired(aRequired);
		lColumn.setPrimaryKey(aPK);
		if (aSize != null) {
			lColumn.setSize(aSize.toString());
		}
		if (null != aDefault) {
			lColumn.setDefaultValue(aDefault.toString());
		}
		mTable.addColumn(lColumn);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITable addIndex(String aColumnName) {
		Column lColumn = getColumn(aColumnName);

		if (lColumn != null) {
			IndexColumn lIndex = new IndexColumn(lColumn);
			NonUniqueIndex lNonUnique = new NonUniqueIndex();
			lNonUnique.setName(mTable.getName() + "_" + aColumnName + "_uk");
			lNonUnique.addColumn(lIndex);
			mTable.addIndex(lNonUnique);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITable addUniqueIndex(String aColumnName) {
		Column lColumn = getColumn(aColumnName);

		if (lColumn != null) {
			IndexColumn lIndex = new IndexColumn(lColumn);
			UniqueIndex lUnique = new UniqueIndex();
			lUnique.setName(mTable.getName() + "_" + aColumnName + "_uk");
			lUnique.addColumn(lIndex);
			mTable.addIndex(lUnique);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Table getTable() {
		return mTable;
	}

	/**
	 * Returns the column object by the name.
	 *
	 * @param aName The name of column.
	 * @return Column object.
	 */
	private Column getColumn(String aName) {
		for (Column lColumn : mTable.getColumns()) {
			if (aName.equals(lColumn.getName())) {
				return lColumn;
			}
		}
		return null;
	}
}
