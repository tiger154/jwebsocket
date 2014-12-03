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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javolution.util.FastList;
import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.jwebsocket.dynamicsql.api.IDatabase;
import org.jwebsocket.dynamicsql.api.IDeleteQuery;
import org.jwebsocket.dynamicsql.api.ISelectQuery;
import org.jwebsocket.dynamicsql.api.ITable;
import org.jwebsocket.dynamicsql.platform.derby.Derby107Platform;
import org.jwebsocket.dynamicsql.query.DynaDeleteQuery;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class DynaDB implements IDatabase {

	private final Platform mPlatform;
	private Database mDB;
	private final Map<String, String> mOptions;

	/**
	 * Constructor
	 *
	 * @param aDatabaseName
	 * @param aDataSource
	 */
	public DynaDB(String aDatabaseName, DataSource aDataSource) {
		PlatformFactory.registerPlatform("Derby", Derby107Platform.class);

		mPlatform = PlatformFactory.createNewPlatformInstance(aDataSource);
		mPlatform.setDelimitedIdentifierModeOn(true);
		mDB = mPlatform.readModelFromDatabase(aDatabaseName);
		mOptions = SupportUtils.getOptions(aDataSource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return mDB.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTable(ITable aTable) {
		if (mDB.findTable(aTable.getName()) == null) {
			Database lDB = new Database();
			lDB.addTable(aTable.getTable());
			mPlatform.createTables(lDB, false, true);
			mDB = mPlatform.readModelFromDatabase(mDB.getName());
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aTableName
	 */
	@Override
	public void dropTable(String aTableName) {
		if (existsTable(aTableName)) {
			mPlatform.dropTable(mDB, mDB.findTable(aTableName), true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createTables(boolean aDropTablesFirst, boolean aContinueOnError) {
		mPlatform.createTables(mDB, aDropTablesFirst, aContinueOnError);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getTables() {
		List<String> lTables = new FastList<String>();
		for (Table lTable : mDB.getTables()) {
			lTables.add(lTable.getName());
		}
		return lTables;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return
	 */
	@Override
	public Boolean existsTable(String aTableName) {
		return (mDB.findTable(aTableName) != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insert(String aTableName, Map<String, Object> aItem) {
		mPlatform.insert(mDB, createDynaBean(aTableName, aItem));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(String aTableName, Map<String, Object> aItem) {
		mPlatform.update(mDB, createDynaBean(aTableName, aItem));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String aTableName, Map<String, Object> aItem) {
		mPlatform.delete(mDB, createDynaBean(aTableName, aItem));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(IDeleteQuery aQuery) {
		mPlatform.evaluateBatch(aQuery.getSQL(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearTable(String aTableName) {
		mPlatform.evaluateBatch(new DynaDeleteQuery(this, aTableName).getSQL(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getOptions() {
		return mOptions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DynaBean> fetch(ISelectQuery aQuery, Integer aOffset, Integer aLimit) {
		return mPlatform.fetch(mDB, aQuery.getSQL(), aOffset, aLimit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DynaBean fetchOne(ISelectQuery aQuery) {
		List<DynaBean> lList = fetch(aQuery, 0, 1);

		if (lList.isEmpty()) {
			return null;
		}
		return lList.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator execute(ISelectQuery aQuery) {
		return mPlatform.query(mDB, aQuery.getSQL());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DynaBean> fetch(ISelectQuery aQuery) {
		return mPlatform.fetch(mDB, aQuery.getSQL());
	}

	/**
	 * Convert the item map to DynaBean object of a table.
	 *
	 * @param aTableName The table name.
	 * @param aItem The values ​​of a tuple in map form.
	 * @return The DynaBean object.
	 */
	private DynaBean createDynaBean(String aTableName, Map<String, Object> aItem) {
		DynaBean lDynaBean = mDB.createDynaBeanFor(aTableName, true);

		for (Map.Entry<String, Object> entry : aItem.entrySet()) {
			lDynaBean.set(entry.getKey(), entry.getValue());
		}
		return lDynaBean;
	}
}
