//	---------------------------------------------------------------------------
//	jWebSocket Simplechat script app demo (Community Edition, CE)
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

// making the clients collection persistent across
// app hot reloads
if (!App.getStorage().containsKey('clients')){
	App.getStorage().put('clients', App.newThreadSafeMap());
}

var NS = "org.jwebsocket.plugins.scripting.simplechat";

// getting the clients collection
var mClients = App.getStorage().get('clients');

App.on('appLoaded', function(){
	// setting the app description
	App.setDescription('Basic Chat application with public and private chats');
	
	App.publish('Chat', {
		register: function(aConnector){
			App.requireAuthority(aConnector, NS + ".register");
			mClients.put(aConnector.getUsername(), aConnector);
			
			App.broadcast(mClients.values(), {
				type: 'event',
				name: 'connection',
				user: aConnector.getUsername()
			});
		},
		broadcast: function(aMessage, aConnector){
			App.assertTrue('string' == typeof aMessage, 'The "message" argument cannot be null!');
			App.assertTrue(mClients.containsKey(aConnector.getUsername()), 
				"Client not registered yet!");
			
			App.broadcast(mClients.values(), {
				type: 'event',
				name: 'pubmessage',
				message: aMessage,
				user: aConnector.getUsername()
			});
		},
		sendPrivate: function(aTarget, aMessage, aConnector){
			App.assertTrue('string' == typeof aTarget, 'The "target" argument cannot be null!');
			App.assertTrue('string' == typeof aMessage, 'The "message" argument cannot be null!');
			App.assertTrue(mClients.containsKey(aConnector.getUsername()), 
				"Client not registered yet!");
			
			var lTarget = mClients.get(aTarget);
			App.assertTrue(null != lTarget, 'The target client does not exists!');
			
			App.sendToken(lTarget, {
				type: 'event',
				name: 'privmessage',
				message: aMessage,
				user: aConnector.getUsername()
			});
		},
		unregister: function(aConnector){
			if (mClients.containsKey(aConnector.getUsername())){
				mClients.remove(aConnector.getUsername());
			
				App.broadcast(mClients.values(), {
					type: 'event',
					name: 'disconnection',
					user: aConnector.getUsername()
				});
			}
		},
		getUsers: function(){
			return mClients.keySet().toArray();
		}
	});
	
	App.on(['connectorStopped', 'logoff'], function (aConnector){
		App.getPublished('Chat').unregister(aConnector);
	});
});