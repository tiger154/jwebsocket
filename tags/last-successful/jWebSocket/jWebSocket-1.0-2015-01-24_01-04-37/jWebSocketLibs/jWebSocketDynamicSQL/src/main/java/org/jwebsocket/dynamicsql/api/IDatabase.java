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
package org.jwebsocket.dynamicsql.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.DynaBean;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public interface IDatabase {

	/**
	 * Return the database name.
	 *
	 * @return the database name.
	 */
	public String getName();

	/**
	 * Allows to add a table to the database.
	 *
	 * @param aTable the table to add.
	 */
	public void addTable(ITable aTable);

	/**
	 * Find if a table exists in the database with the name specified in the
	 * parameters.
	 *
	 * @param aTableName the table name to find.
	 * @return 
	 */
	public Boolean existsTable(String aTableName);

	/**
	 * Allows to drop physically a table to the database.
	 *
	 * @param aTableName the table name to drop.
	 */
	public void dropTable(String aTableName);

	/**
	 * Allows to create physically in the database all tables added previously.
	 *
	 * @param aDropTablesFirst If its value is [true], execute the drop table
	 * syntax before creating it.
	 * @param aContinueOnError If its value is [true], continuous creating
	 * tables although errors occur in the process.
	 */
	public void createTables(boolean aDropTablesFirst, boolean aContinueOnError);

	/**
	 * List all the tables names.
	 *
	 * @return list all the tables names.
	 */
	public List<String> getTables();

	/**
	 * Allows to insert a tuple in the table specified by parameters.
	 *
	 * @param aTableName The table name to insert a tuple.
	 * @param aItem The data to insert entered in a map (key/value). The key
	 * must match the name of the column in each case.
	 */
	public void insert(String aTableName, Map<String, Object> aItem);

	/**
	 * Allows to update a tuple in the table specified by parameters.
	 *
	 * @param aTableName The table name to update a tuple.
	 * @param aItem The data to update entered in a map (key/value). The key
	 * must match the name of the column in each case. The primary key column(s)
	 * of table is required in the map.
	 */
	public void update(String aTableName, Map<String, Object> aItem);

	/**
	 * Allows to delete a tuple in the table specified by parameters.
	 *
	 * @param aTableName The table name to delete a tuple.
	 * @param aItem The data to delete entered in a map (key/value). The key
	 * must match the name of the column in each case. The primary key column(s)
	 * of table is required in the map.
	 */
	public void delete(String aTableName, Map<String, Object> aItem);

	/**
	 * Allows to delete one or more tuples through a query.
	 *
	 * @param aQuery The query to bring together the tuples to be deleted.
	 */
	public void delete(IDeleteQuery aQuery);

	/**
	 * Allows to clear the table. Remove all tuples in the table.
	 *
	 * @param aTableName The table name to clear.
	 */
	public void clearTable(String aTableName);

	/**
	 * Return the database options to support a platform specifically.
	 *
	 * @return The options to support a platform specifically.
	 */
	public Map<String, String> getOptions();

	/**
	 * Return a list with all DynaBean objects associated with the records
	 * returned by the query, considering the offset and limit of the query.
	 *
	 * @param aQuery The query to execute.
	 * @param aOffset The offset of the query.
	 * @param aLimit The limit of the query.
	 * @return list with all the records returned by the query, considering the
	 * offset and limit of the query.
	 */
	public List<DynaBean> fetch(ISelectQuery aQuery, Integer aOffset, Integer aLimit);

	/**
	 * Return a list with all DynaBean objects associated with the records
	 * returned by the query.
	 *
	 * @param aQuery The query to execute.
	 * @return list with all the records returned by the query.
	 */
	public List<DynaBean> fetch(ISelectQuery aQuery);

	/**
	 * Return a first DynaBean object associated with the record returned by the
	 * query.
	 *
	 * @param aQuery The select query to execute.
	 * @return DynaBean object with the first record returned by the query.
	 */
	public DynaBean fetchOne(ISelectQuery aQuery);

	/**
	 * Return a Iterator allowing iterate for all the records returned by the
	 * query.
	 *
	 * @param aQuery The query to execute.
	 * @return a Iterator allowing iterate for all the records returned by the
	 * query.
	 */
	public Iterator execute(ISelectQuery aQuery);
}
