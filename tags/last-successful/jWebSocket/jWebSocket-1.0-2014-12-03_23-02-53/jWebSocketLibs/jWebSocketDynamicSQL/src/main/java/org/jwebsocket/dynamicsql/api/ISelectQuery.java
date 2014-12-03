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

import org.jwebsocket.dynamicsql.query.Ordering;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public interface ISelectQuery {

	/**
	 * Add a condition of type <AND> to the query.
	 *
	 * @param aCondition The query condition.
	 * @return The select query.
	 */
	public ISelectQuery and(ICondition aCondition);

	/**
	 * Add a condition of type <OR> to the query.
	 *
	 * @param aCondition The query condition.
	 * @return The select query.
	 */
	public ISelectQuery or(ICondition aCondition);

	/**
	 * Add a condition of type <AND> to the query.
	 *
	 * @param aColumnName The column name.
	 * @param aDir The ordering direction.
	 * @return The select query.
	 */
	public ISelectQuery orderBy(String aColumnName, Ordering aDir);

	/**
	 * Returns the SQL select query.
	 *
	 * @return The SQL select query.
	 */
	public String getSQL();
}
