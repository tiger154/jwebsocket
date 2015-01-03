//	---------------------------------------------------------------------------
//	jWebSocket Notebook script app demo (Community Edition, CE)
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
App.importScript('${EXT}/MongoDBUtils');
App.importScript('${APP_HOME}/service/NotesService');

App.on('appLoaded', function(){
	// setting the app description
	App.setDescription('Basic Notebook application for notes management');
	
	NotesService.initialize();
	
	App.on('filterIn', function(aToken, aConnector){
		App.getLogger().debug(aToken.toString());
		if ('callMethod' == aToken.getString('type') && 'Notes' == aToken.getString('objectId')){
			// clients require to be authenticated first
			// in order to use the Notes controller actions
			App.assertTrue('anonymous' != aConnector.getUsername(), 'Authenticate first!');
		}
	});
	
	App.publish('Notes', {
		add: function(aTitle, aBody, aConnector){
			App.assertTrue('string' == typeof aTitle, 'The "title" argument cannot be null!');
			App.assertTrue('string' == typeof aBody, 'The "body" argument cannot be null!');
			
			var lUsername = aConnector.getUsername();
			NotesService.add(lUsername, aTitle, aBody);
		},
		list: function(aOffset, aLength, aConnector){
			App.assertTrue('number' == typeof aOffset, 'The "offset" argument cannot be null!');
			App.assertTrue('number' == typeof aLength, 'The "length" argument cannot be null!');
			
			var lUsername = aConnector.getUsername();
			return {
				data: NotesService.list(lUsername, aOffset, aLength),
				total: NotesService.count(lUsername) 
			};
		},
		edit: function(aNoteId, aTitle, aBody, aConnector){
			App.assertTrue('string' == typeof aNoteId, 'The "noteId" argument cannot be null!');
			App.assertTrue('string' == typeof aTitle, 'The "title" argument cannot be null!');
			App.assertTrue('string' == typeof aBody, 'The "body" argument cannot be null!');
				
			var lUsername = aConnector.getUsername();
			return NotesService.edit(lUsername, aNoteId, aTitle, aBody);
		},
		remove: function(aNoteId, aConnector){
			App.assertTrue('string' == typeof aNoteId, 'The "noteId" argument cannot be null!');
				
			var lUsername = aConnector.getUsername();
			return NotesService.remove(lUsername, aNoteId);
		}
	});
});

App.on(['beforeAppReload', 'systemStopping'], function(){
	NotesService.shutdown();
});