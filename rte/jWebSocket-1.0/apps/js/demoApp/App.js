// importing App script
App.importScript('${APP_HOME}/lib/myAppLib.js');

var FileUtils = Packages.org.apache.commons.io.FileUtils;
var File = Packages.java.io.File;

App.on('filterIn', function(aToken) {
	App.getLogger().debug('Calling filter in: ' + aToken.toString());
});

App.on('token', function(aConnector, aToken) {
	var lResponse = App.createResponse(aToken);
	lResponse.put('required', true);
	lResponse.put('name', aToken.get('name'));

	// normal send
	App.sendToken(aConnector, lResponse);
	// fragmented send
	App.sendToken(aConnector, lResponse, 10);
	// akcnowledge send
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

App.on('filterOut', function(aToken) {
	App.getLogger().debug('Calling filter out: ' + aToken.toString());
});

App.on('connectorStarted', function(aConnector) {
	App.getLogger().debug('New client started: ' + aConnector.getId());
});

App.on('connectorStopped', function(aConnector) {
	App.getLogger().debug('Client stopped: ' + aConnector.getId());
});

var lFn = function(aConnector) {
	App.getLogger().debug('Processing lFn...');
	App.requireAuthority(aConnector, 'admin');
};

var read = function(aFile) {
	return FileUtils.readFileToString(new File(aFile));
};

var getVersion = function() {
	return java.lang.System.getProperty('java.version');
};

var lObject = {
	version: getVersion
};

// publish App object to be accessed from the client
App.publish('Main', {
	read: read,
	version: lObject.version,
	fn: lFn,
	sayHello: sayHello
});