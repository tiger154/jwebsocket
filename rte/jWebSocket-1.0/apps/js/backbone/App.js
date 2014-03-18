//	---------------------------------------------------------------------------
//	jWebSocket Backbone script app  (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
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

// setting the app description
App.setDescription('The backbone app handle server-side inter-services colaboration');

// module list
var lModules = ["useradmin"];
var lJMS = App.getJMSManager();

App.on('appLoaded', function() {
	// loading modules
	for (var lIndex in lModules) {
		App.importScript('${APP_HOME}/module/' + lModules[lIndex]);
		App.getModule(lModules[lIndex]).load(lJMS);
		App.getLogger().debug('Module "' + lModules[lIndex] + '" successfully loaded!');
	}
	
	App.publish('Main', {

		simulateRegisterUser: function(aUsername){
			var lMsg= lJMS.buildMessage('org.jwebsocket.plugins', 'tokenProcessed');
			lMsg.setStringProperty('tokenNS', 'org.jwebsocket.plugins.useradmin');
			lMsg.setStringProperty('tokenType', 'registerNewUser');
			lMsg.setStringProperty('username', aUsername);
			lMsg.setIntProperty('code', 0);
			
			lJMS.send(lMsg);
		},
		simulateSendSMS: function(){
			var lMsg= lJMS.buildMessage('org.jwebsocket.plugins', 'tokenProcessed');
			lMsg.setStringProperty('tokenNS', 'org.jwebsocket.plugins.sms');
			lMsg.setStringProperty('tokenType', 'sendSMS');
			lMsg.setStringProperty('username', 'guest');
			lMsg.setIntProperty('code', 0);
			
			lJMS.send(lMsg);
		},
		simulateSendSMSError: function(){
			var lMsg= lJMS.buildMessage('org.jwebsocket.plugins', 'tokenProcessed');
			lMsg.setStringProperty('tokenNS', 'org.jwebsocket.plugins.sms');
			lMsg.setStringProperty('tokenType', 'sendSMS');
			lMsg.setStringProperty('username', 'guest');
			lMsg.setIntProperty('code', -1 );
			
			lJMS.send(lMsg);
		}
	});
});

App.on(['systemStopping', 'undeploying', 'beforeAppReload'], function() {
	shutdown();
});

var shutdown = function() {
	// unloading modules
	for (var lIndex in lModules) {
		App.getModule(lModules[lIndex]).unload(lJMS);
	}
};
