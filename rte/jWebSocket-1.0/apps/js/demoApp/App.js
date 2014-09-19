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
		args: {
			email: {
				not_null: false,
				type: 'string',
				regex: 'email'
			},
			age: {
				not_null: false,
				type: 'integer',
				min_value: 18,
				max_value: 65,
				validator: function(aValue) {
					return true;
				}
			}
		},
		sayHello: {
			//authority: 'ADMIN',
			authenticated: true,
			args: ['email', 'age'],
//			handler: function(aEmail, aAge) {
//				return {
//					msg: 'Hello ' + aEmail + ' - ' + aAge
//				};
//			}
			ebBridge: {
				ns: 'test.queue', //test.topic
				action: 'send' // publish
			}
		},
		broadcast: function(aMessage) {
			App.broadcast({
				message: aMessage
			});
		},
		toList: function() {
			return App.toList(['white', 'black', 'blue']);
		},
		toMap: function() {
			return App.toMap({name: 'Rolando SM', email: 'rsantamaria@jwebsocket.org', age: 28});
		},
		asyncMethod: function(aConnector, aHandler) {
			App.newAsyncResult(aHandler)
					.setResult(App.toJava({
						msg: 'Hello from async method call!',
						numbers: [1, 2, 3],
						username: aConnector.getUsername()
					}));
		},
		getType: function(aValue) {
			return App.getType(aValue);
		}
	});

	EventBus.register('test.queue', {
		OnMessage: function(aMessage) {
			aMessage.reply({
				msg: 'Hello ' + aMessage.email + ' - ' + aMessage.age
			});
		}
	});
	EventBus.register('test.topic', {
		OnMessage: function(aMessage) {
			App.getLogger().debug('Hello ' + aMessage.email + ' - ' + aMessage.age);
		}
	});

// Example using the JMS built-in client
// See: https://jwebsocket.org/documentation/Plug-Ins/Scripting-Plug-In/Developer-Guide/jms-integration
// -------------------------------------
//	var JMS = App.getJMSManager();
//	App.publish('JMSManager', {
//		test: function(aMessage) {
//			App.getLogger().debug('Sending message: ' + aMessage);
//			JMS.send('queue://test.queue', aMessage);
//			JMS.send('topic://test.topic', aMessage);
//		},
//		shutdown: function() {
//			JMS.shutdown();
//		}
//	});
//
//	JMS.subscribeTo('queue://test.queue', {
//		onMessage: function(aMsg) {
//			App.getLogger().debug('received from queue: ' + aMsg.getText());
//		}
//	});
//	JMS.subscribeTo('topic://test.topic', {
//		onMessage: function(aMsg) {
//			App.getLogger().debug('received from topic: ' + aMsg.getText());
//		}
//	});


//	EventBus.register('test.topic', {
//		OnMessage: function(aMessage) {
//			App.getLogger().debug('Received:' + aMessage.msg);
//		}
//	});
//
//	EventBus.register('test.q*', {
//		OnMessage: function(aMessage) {
//			App.getLogger().debug('Received:' + aMessage.msg);
//			App.getLogger().debug('Replying...');
//			aMessage.reply({
//				msg: 'This is a response message'
//			});
////			aMessage.fail({
////				msg: 'This is a response error message'
////			});
//		}
//	});
//
//	EventBus.publish({
//		ns: 'test.topic',
//		type: 'publish',
//		msg: 'Message is broadcasted to all active listeners on target namespace'
//	});
//
//	EventBus.send({
//		ns: 'test.queue',
//		type: 'send',
//		msg: 'Message is sent to only one listener on target namespace'
//	}, {
//		OnSuccess: function(aResponse) {
//			App.getLogger().debug('Processing response...');
//			App.getLogger().debug(aResponse.msg);
//		},
//		OnFailure: function(aResponse) {
//			App.getLogger().debug(aResponse.msg);
//		}
//	});
//
//	EventBus.send({
//		ns: 'namespace_without_handlers',
//		type: 'timeout'
//	}, {
//		timeout: 3000,
//		OnTimeout: function(aResponse) {
//			App.getLogger().debug('Timeout');
//		}
//	});
//

});
