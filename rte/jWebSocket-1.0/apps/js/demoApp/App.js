/**
 * JavaScript Script App demo for testing and learning purposes
 * 
 * See: https://jwebsocket.org/documentation/Plug-Ins/Scripting-Plug-In/Developer-Guide 
 * for more details
 */

// importing app scripts
App.importScript('${APP_HOME}/lib/myAppLib');

App.on('appLoaded', function() {
	// setting app description
	App.setDescription('Demo application with JavaScript at the server-side.');

	// importing Java classes
	var FileUtils = Packages.org.apache.commons.io.FileUtils;
	var File = Packages.java.io.File;

	// registering on app event for filter in tokens
	App.on('filterIn', function(aToken) {
		App.getLogger().debug('Calling filter in: ' + aToken.toString());
	});

	// registering on app event to process tokens 
	App.on('token', function(aConnector, aToken) {
		var lResponse = App.createResponse(aToken);
		lResponse.name = aToken['name'];

		// normal token send (response)
		App.sendToken(aConnector, lResponse);

		// normal token send (custom token)
		App.sendToken(aConnector, {
			another: 'token'
		});

		// fragmented token send
		App.sendToken(aConnector, lResponse, 10);

		// akcnowledge token send
		App.sendToken(aConnector, lResponse, {
			getTimeout: function() {
				return 1000;
			},
			OnTimeout: function() {
				App.getLogger().debug('Token delivery timeout!');
			},
			OnSuccess: function() {
				App.getLogger().debug('Token delivery success!');
			},
			OnFailure: function() {
				App.getLogger().debug('Token delivery failure!');
			}
		});
	});

	// registering on app event for filter out tokens
	App.on('filterOut', function(aToken) {
		App.getLogger().debug('Calling filter out: ' + aToken.toString());
	});

	// registering on app event to process new connections
	App.on('connectorStarted', function(aConnector) {
		App.getLogger().debug('New client started: ' + aConnector.getId());
	});

	// registering on app event to process stopped connections
	App.on('connectorStopped', function(aConnector) {
		App.getLogger().debug('Client stopped: ' + aConnector.getId());
	});

	// publishing to client public object (controllers)
	App.publish('Main', {
		sayHello: function(aName) {
			// see: lib/myAppLib.js
			return sayHello(aName);
		},
		broadcast: function(aMessage) {
			App.broadcast({
				message: aMessage
			});
		},
		toList: function() {
			return App.toList(["white", "black", "blue"]);
		}
	});

// Example using the JMS built-in client
// See: https://jwebsocket.org/documentation/Plug-Ins/Scripting-Plug-In/Developer-Guide/jms-integration
// -------------------------------------
//	var JMS = App.getJMSManager();
//	App.publish('JMSManager', {
//		test: function(aMessage) {
//			JMS.send('queue://test.queue', aMessage);
//			JMS.send('topic://test.topic', aMessage);
//		},
//		shutdown: function() {
//			JMS.shutdown();
//		}
//	});
//
//	JMS.subscribe('queue://test.queue', {
//		onMessage: function(aMsg) {
//			App.getLogger().debug("queue: " + aMsg.getText());
//		}
//	});
//	JMS.subscribe('topic://test.topic', {
//		onMessage: function(aMsg) {
//			App.getLogger().debug("topic: " + aMsg.getText());
//		}
//	});
});
