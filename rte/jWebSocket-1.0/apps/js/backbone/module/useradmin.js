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
		aJMSManager.subscribe({
			onMessage: function(aMessage) {
				var lMsgId = aMessage.getStringProperty('msgId');
				var lTokenType = aMessage.getStringProperty('tokenType');

				// required for cluster compatibility
				if (App.isClusterActive() && !App.getWebSocketServer().getSynchronizer()
						.getWorkerTurn(lMsgId)) {
					return;
				}

				// processing message
				if ('registerNewUser' == lTokenType) {
					App.getLogger().debug(aMessage.toString());
				}
			}}, 'ns = \'org.jwebsocket.plugins\' AND '
				+ 'msgType = \'tokenProcessed\' AND '
				+ 'code = 0 AND '
				+ 'tokenNS = \'' + this.targetNS + '\'',
				true,
				this.subscriptionId);
	},
	// called when the module is unloaded
	unload: function(aJMSManager) {

	}
});