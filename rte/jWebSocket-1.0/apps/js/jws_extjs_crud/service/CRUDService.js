//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket ExtJS CRUD Plug-In (Community Edition, CE)
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

// Author: Aquiles Perez Miranda

var CRUDService = {
    /*
     *  this function prepare a json to be readed  
     *  for read method
     * 
     */

    prepareJSON: function(jdbc_response) {

        //creating proto object with columns
        var cols = jdbc_response.getObject('columns');

        var data = jdbc_response.getObject('data');

        var users = []; //new object to return


        for (var j = 0; j < data.size(); j++)
        {
            var row = data.get(j);

            var new_obj = {};

            for (var i = 0; i < cols.size(); i++) {

                var col = cols.get(i);

                var value = row.get(i);
                new_obj[col.get('name')] = value;


                users[j] = new_obj;
            }
        }

        var lResponse = App.invokePlugIn('jws.jdbc', null, {
            type: 'querySQL',
            alias: 'jws_extjs_crud', //my default connection to db
            sql: 'SELECT * FROM tb_user'
        });

        var data = lResponse.getObject('data');

        var myObj = {
            'totalCount': data.size(),
            'users': users
        };
		

        return myObj;

    },
    /*
     *  Creating read funtionality of AgentService
     */
    read: function(param) {

        var page = param.get('page');
        var limit = param.get('limit');

        var init = (page - 1) * limit;
        var end = limit;

        var lResponse = App.invokePlugIn('jws.jdbc', null, {
            type: 'querySQL',
            alias: 'jws_extjs_crud', //my default connection to db
            sql: 'SELECT * FROM tb_user LIMIT ' + init + ',' + end
        });

        return this.prepareJSON(lResponse);

    },
    /*
     *  Creating update funtionality of AgentService
     */
    update: function(table, param) {

        var lResponse = App.invokePlugIn('jws.jdbc', null, {
            type: 'querySQL',
            alias: 'default', //my default connection to db
            sql: 'SELECT * FROM tb_user'
        });

        var cols = lResponse.getObject('columns');

        var sql = " UPDATE " + table + " SET ";

        var keys = param.keySet();
        var keys_array = keys.toArray();

        //start from 1, iduser no included
        for (var i = 1; i < keys_array.length; i++)
        {
            var key = keys_array[i];
            var col = null;

            //finding the data type of the column
            for (var j = 0; j < cols.size(); j++) {

                col = cols.get(j);

                if (col.get('name') === key)
                {
                    break;
                }

            }
            if (col.get('jsontype') === 'string' || col.get('jsontype') === 'date')
                sql += key + "='" + param.get(key) + "', ";
            else
                sql += key + "=" + param.get(key) + ", ";
        }

        var sql = sql.substr(0, sql.length - 2);

        //finally, i obtain the sql query
        sql += " WHERE " + keys_array[0] + '=' + param.get(keys_array[0]);

        //I invoke jdbc plugin
        App.invokePlugIn('jws.jdbc', null, {
            type: 'updateSQL',
            alias: 'default', //my default connection to db
            sql: sql
        });

    },
    /*
     *  Creating delete funtionality of AgentService
     */



    destroy: function(param) {

        //if not array
        if (param.get(0) === null) {

            var sql = 'DELETE FROM  tb_user WHERE  iduser =' + param.get('iduser');
            App.getLogger().debug(sql);
            App.invokePlugIn('jws.jdbc', null, {
                type: 'execSQL',
                alias: 'default', //my default connection to db
                sql: sql
            });
            
        } else {
             //if array
            for (var i = 0; i < param.size(); i++)
            {
                var sql = 'DELETE FROM  tb_user WHERE  iduser =' + param.get(i).get('iduser');
                App.getLogger().debug(sql);
                App.invokePlugIn('jws.jdbc', null, {
                    type: 'execSQL',
                    alias: 'default', //my default connection to db
                    sql: sql
                });
            }
        }
        return "{success:true}";
    },
    /*
     *  Creating add funtionality of AgentService
     */

    add: function(param) {

        var sql = 'INSERT INTO tb_user(';

        var keys = param.keySet();
        var keys_array = keys.toArray();

        //start from 1, iduser no included
        for (var i = 1; i < keys_array.length; i++)
        {
            var key = keys_array[i];
            sql += key + ",";

        }

        var sql = sql.substr(0, sql.length - 1) + ") VALUES (";

        var lResponse = App.invokePlugIn('jws.jdbc', null, {
            type: 'querySQL',
            alias: 'default', //my default connection to db
            sql: 'SELECT * FROM tb_user'
        });

        var cols = lResponse.getObject('columns');

        for (var i = 1; i < keys_array.length; i++)
        {
            var key = keys_array[i];
            var col = null;

            //finding the data type of the column
            for (var j = 0; j < cols.size(); j++) {

                col = cols.get(j);

                if (col.get('name') === key)
                {
                    break;
                }

            }
            if (col.get('jsontype') === 'string' || col.get('jsontype') === 'date')
                sql += "'" + param.get(key) + "', ";
            else
                sql += param.get(key) + ", ";
        }

        var sql = sql.substr(0, sql.length - 2) + ")";

        App.getLogger().debug(sql);

        App.invokePlugIn('jws.jdbc', null, {
            type: 'execSQL',
            alias: 'default', //my default connection to db
            sql: sql
        });

        return "{success:true}";
    }

};