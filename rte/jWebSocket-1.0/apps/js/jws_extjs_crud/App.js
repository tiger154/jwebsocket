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

App.importScript('${APP_HOME}/service/CRUDService');

App.publish('Crud', {
    sayHello: function(aName, lastname) {
        return 'Hello "' + aName + ' ' + lastname + '" How are you? ;)';
    },
    authenticate: function(user, pass) {
        if (user === 'root' && pass === 'root')
            return "{success:true}";
        else
            return "{success:false}";
    },
    read: function(param) {

        return CRUDService.read(param);
    },
    update: function(param) {

        CRUDService.update('tb_user', param);

        return "{success:true}";
    },
    destroy: function(param) {
        return CRUDService.destroy(param);
    },
    create: function(param) {
        return CRUDService.add(param);
     }

});


/*lResponse = App.invokePlugIn('id_plugin', connector, {
 type: 'createQuota',
 identifier: 'CountDown',
 namespace: 'org.jwebsocket.plugins.sms',
 instance: 'defaultUser',
 instance_type: 'Group',
 actions: 'sendSMS',
 value: '5'
 });
 
 El primer parametro es el id del plugin, en tu caso es jws.jdbc, el segundo parametro es el conector que invoca, puede ser null si no lo tienes, y el tercero es el token en forma de JSON.
 Ese token es como ejemplo claro, ahi tienes que poner los datos que tu necesitas pasarle a un plugin especifico.
 */






