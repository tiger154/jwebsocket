//	---------------------------------------------------------------------------
//	jWebSocket Backbone UserAdmin module (Community Edition, CE)
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
App.setModule('useradmin', {
	// the module durable subscriber id
	subscriptionId: 'backbone_module_useradmin',
	// the target plugin namespace to listen
	targetNS: 'org.jwebsocket.plugins.useradmin',
	// called when the module is loaded
	load: function(aJMSManager) {
		// listening useradmin events
		aJMSManager.subscribe({
			onMessage: function(aMessage) {
				var lMsgId = aMessage.getStringProperty('msgId');
				var lTokenType = aMessage.getStringProperty('tokenType');
				var lUsername = aMessage.getStringProperty('username');
				
                                
				// required for cluster compatibility
				if (App.isClusterActive() && !App.getWebSocketServer().getSynchronizer()
					.getWorkerTurn(lMsgId)) {
					return;
				}

				// processing message
				if ('registerNewUser' == lTokenType) {
                                    
					// 
					// QUOTA plug-in integration
					// *************************
					
					// getting the SMS countdown quota
					var lResponse = App.invokePlugIn('jws.quota', null, {
						type: 'getQuota',
						identifier:'CountDown',
						namespace:'org.jwebsocket.plugins.sms', 
						instance:'defaultUser', 
						instance_type:'Group',
						actions:'sendSMS'
					});
					if ( 0 == lResponse.getCode() ){
						
						// assigning the SMS countdown quota to the new user
						lResponse = App.invokePlugIn('jws.quota', null, {
							type: 'registerQuota',
							uuid: lResponse.getString('uuid'), 
							identifier:'CountDown', 
							instance: lUsername, 
							instance_type:'User'
						});
						
						if (0 != lResponse.getCode() ){
							App.getLogger().error("UserAdmin - Could not register quota: " +lResponse.getString('msg'));
						}
					} else {
						//if there is not a quota for SMSPlugin, create tue quota
						lResponse = App.invokePlugIn('jws.quota', null, {
							type: 'createQuota',
							identifier:'CountDown',
							namespace:'org.jwebsocket.plugins.sms', 
							instance:'defaultUser', 
							instance_type:'Group',
							actions:'sendSMS',
							value: '5'
						});

						if (0 == lResponse.getCode() ){

							lResponse = App.invokePlugIn('jws.quota', null, {
								type: 'registerQuota',
								uuid: lResponse.getString('uuid'), 
								identifier:'CountDown', 
								instance: lUsername, 
								instance_type:'User'
							});

							if (0 != lResponse.getCode() ){
								App.getLogger().error("UserAdmin - Could not register quota: " +lResponse.getString('msg'));
							}

						}else{
							App.getLogger().error("UserAdmin - Could not create the quota: " +lResponse.getString('msg'));
						}
					}
				}
			}
		}, 'ns = \'org.jwebsocket.plugins\' AND '
		+ 'msgType = \'tokenProcessed\' AND '
		+ 'code = 0 AND '
		+ 'tokenNS = \'' + this.targetNS + '\'',
		true,
		this.subscriptionId);
		
	// creating required quotas if missing
		
	},
	// called when the module is unloaded
	unload: function(aJMSManager) {

	}
});