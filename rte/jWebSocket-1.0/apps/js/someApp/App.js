//App.importScript('${EXT}/jwsItemStoragePlugIn');
//App.importScript('${EXT}/jwsItemStoragePlugInEE_min');
App.loadJar('${APP_HOME}/StaticTest.jar');

App.on('appLoaded', function(aHotLoad){
	if (false == aHotLoad){
		App.loadToAppBeanFactory('${APP_HOME}/beans.xml');
	}
	
//	logger = App.getLogger();
//	server = App.getServerClient();
//	server.addListener({
//		processWelcome: function(){
//			server.subscribeCollection('Contacts', 'secret');
//			server.setItemStorageCallbacks({
//				OnItemSaved: function(aEvent){
//					logger.debug(aEvent.name);
//				}
//			});
//		}
//	})
//	server.open();

	App.setModule('clock', {
		getTime: function(){
			return new Date().getTime();
		}
	});

	lClock = App.getModule('clock');

	App.publish('Main', {
		list: function(aConnector){
			server.listItems('Contacts', {
				OnSuccess: function(aResponse){
					App.sendToken(aConnector, aResponse);
				}
			});
		},
		test: function(){
			return 'Response from test method on Main object ;)';
		},
		sandbox: function(){
			// should fire a security exception
			Class('org.jwebsocket.util.Tools').getTimer();
		},
		time: function(){
			return lClock.getTime();
		},
		name: function(){
			var lPerson = App.getAppBean('person');
			return lPerson.getName();
		}
	});
});
