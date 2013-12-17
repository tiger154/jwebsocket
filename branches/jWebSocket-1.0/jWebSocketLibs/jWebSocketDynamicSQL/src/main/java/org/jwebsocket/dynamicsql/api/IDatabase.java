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
package org.jwebsocket.dynamicsql.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.DynaBean;

/**
 *
 * @author markos
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
	 * Find if a table exists in the database with the name specified in the parameters.
	 *
	 * @param aTableName the table name to find.
	 */
    public Boolean existsTable(String aTableName);
    
    /**
	 * Allows to drop physically a table to the database.
	 *
	 * @param aTable the table name to drop.
	 */
    public void dropTable(String aTableName);
    
    /**
	 * Allows to create physically in the database all tables added previously.
	 *
	 * @param aDropTablesFirst If its value is [true], execute the drop table 
     * syntax before creating it.
     * @param aContinueOnError If its value is [true], continuous creating tables 
     * although errors occur in the process.
	 */
    public void createTables(boolean aDropTablesFirst, boolean aContinueOnError);
    /**
	 * List all the tables names.
	 *
	 * @return list all the tables names.
	 */
    public List<String> getTables();
    
    public void insert(String aTableName, Map<String, Object> aItem);
    public void update(String aTableName, Map<String, Object> aItem);
    public void delete(String aTableName, Map<String, Object> aItem);
    /**
	 * Return the database options to support a platform specifically.
	 *
	 * @return The options to support a platform specifically.
	 */
    public Map<String, String> getOptions();
    public List<DynaBean> fetch(IQuery aQuery, Integer aOffset, Integer aLimit);
    public Iterator execute(IQuery aQuery);
}
