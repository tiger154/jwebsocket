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

import java.util.Map;
import javax.sql.DataSource;
import javolution.util.FastMap;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.ddlutils.PlatformUtils;
import org.apache.ddlutils.platform.mssql.MSSqlPlatform;
import org.apache.ddlutils.platform.mysql.MySql50Platform;
import org.apache.ddlutils.platform.mysql.MySqlPlatform;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class SupportUtils {

	//Option Types
	/**
	 *
	 */
	public static String ESCAPE_TABLE_LITERAL = "escapeTablesAndColumnsLiteral";

	/**
	 *
	 */
	public static String ESCAPE_LIKE_LITERAL = "escapeLikeLiteral";

	/**
	 * Returns the options map corresponding to the platform entered in the data
	 * source.
	 *
	 * @param aDataSource The connection data source.
	 * @return The compatibilities options to the platform entered in the data
	 * source.
	 */
	public static Map<String, String> getOptions(DataSource aDataSource) {
		PlatformUtils lUtils = new PlatformUtils();
		String lPlatform = lUtils.determineDatabaseType(aDataSource);
		Map<String, String> lOptions = new FastMap<String, String>();
		//Default Values Options
		lOptions.put(ESCAPE_TABLE_LITERAL, "\"\"");
		lOptions.put(ESCAPE_LIKE_LITERAL, "%");

		if (lPlatform == null || "".equals(lPlatform)) {
			return lOptions;
		} else if (lPlatform.equals(MySqlPlatform.DATABASENAME)
				|| lPlatform.equals(MySql50Platform.DATABASENAME)) {
			lOptions.put(ESCAPE_TABLE_LITERAL, "``");
		} else if (lPlatform.equals(MSSqlPlatform.DATABASENAME)) {
			lOptions.put(ESCAPE_TABLE_LITERAL, "[]");
		}
		//TODO: Check support queries on the following platforms
        /* AxionPlatform
		 * CloudscapePlatform
		 * MaxDbPlatform
		 * MckoiPlatform
		 * SapDbPlatform
		 */

		return lOptions;
	}

	/**
	 * Convert a DynaBean object to Map.
	 *
	 * @param aDynaBean The DynaBean object.
	 * @return The Map<String, Object>
	 */
	public static Map<String, Object> convertToMap(DynaBean aDynaBean) {
		Map<String, Object> lMap = new FastMap<String, Object>();

		for (DynaProperty lDynaProperty : aDynaBean.getDynaClass().getDynaProperties()) {
			lMap.put(lDynaProperty.getName(), aDynaBean.get(lDynaProperty.getName()));
		}

		return lMap;
	}
}
