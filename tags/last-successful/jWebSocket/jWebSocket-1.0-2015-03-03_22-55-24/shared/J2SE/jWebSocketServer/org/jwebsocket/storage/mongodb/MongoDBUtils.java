//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBUtils (Community Edition, CE)
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
package org.jwebsocket.storage.mongodb;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.springframework.util.Assert;

/**
 * Utility class for Mongo DB generic helper methods
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBUtils {

	/**
	 *
	 * @param aConnection
	 * @param aDatabaseName
	 * @param aUsername
	 * @param aPassword
	 * @return
	 */
	public static DB getDB(Mongo aConnection, String aDatabaseName, String aUsername, String aPassword) {
		DB lDB = aConnection.getDB(aDatabaseName);
		if (null != aUsername) {
			Assert.isTrue(lDB.authenticate(aUsername, aPassword.toCharArray()),
					"Invalid credentials!");
		}

		return lDB;
	}

	/**
	 *
	 * @param aConnection
	 * @param aDatabaseName
	 * @return
	 */
	public static DB getDB(Mongo aConnection, String aDatabaseName) {
		return getDB(aConnection, aDatabaseName, null, null);
	}
}
