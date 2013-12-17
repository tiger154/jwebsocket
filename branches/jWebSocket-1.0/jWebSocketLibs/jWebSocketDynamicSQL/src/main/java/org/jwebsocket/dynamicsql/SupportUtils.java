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
package org.jwebsocket.dynamicsql;

import java.util.Map;
import javax.sql.DataSource;
import javolution.util.FastMap;
import org.apache.ddlutils.PlatformUtils;
import org.apache.ddlutils.platform.mssql.MSSqlPlatform;
import org.apache.ddlutils.platform.mysql.MySql50Platform;
import org.apache.ddlutils.platform.mysql.MySqlPlatform;

/**
 *
 * @author markos
 */
public class SupportUtils {
    
    
    //Option Types
    public static String ESCAPE_TABLE_LITERAL = "escapeTablesAndColumnsLiteral";
    public static String ESCAPE_LIKE_LITERAL = "escapeLikeLiteral";
    
    public static Map<String, String> getOptions(DataSource aDataSource) {
        PlatformUtils utils = new PlatformUtils();
        String lPlatform = utils.determineDatabaseType(aDataSource);
        Map<String, String> lOptions = new FastMap<String, String>();
        //Default Values Options
        lOptions.put(ESCAPE_TABLE_LITERAL, "\"\"");
        lOptions.put(ESCAPE_LIKE_LITERAL, "%");
        
        if(lPlatform == null || "".equals(lPlatform)) {
            return lOptions;
        } else if(lPlatform.equals(MySqlPlatform.DATABASENAME) 
                || lPlatform.equals(MySql50Platform.DATABASENAME)) {
            lOptions.put(ESCAPE_TABLE_LITERAL, "``");
        } else if(lPlatform.equals(MSSqlPlatform.DATABASENAME)) {
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
}
