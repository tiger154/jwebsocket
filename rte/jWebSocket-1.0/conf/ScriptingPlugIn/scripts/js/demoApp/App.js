// importing App script
App.importScript("${APP_HOME}/lib/myAppLib.js");

importClass(org.apache.commons.io.FileUtils);
importClass(java.io.File);

var Object = {
	sayHello: sayHello
};

App.on("filterIn", function(aToken) {
	App.getLogger().debug("Calling filter in: " + aToken.toString());
});

App.on("token", function(aToken, aConnector) {
	var lResponse = App.createResponse(aToken);
	lResponse.put("required", true);
	lResponse.put("name", aToken.get("name"));

	App.sendToken(aConnector, lResponse);
});

App.on("filterOut", function(aToken) {
	App.getLogger().debug("Calling filter out: " + aToken.toString());
});

App.on("connectorStarted", function(aConnector) {
	App.getLogger().debug("New client started: " + aConnector.getId());
});

App.on("connectorStopped", function(aConnector) {
	App.getLogger().debug("Client stopped: " + aConnector.getId());
});

var lFn = function(aConnector) {
	App.getLogger().debug("Processing lFn...");
	App.requireAuthority(aConnector, "admin");
};

var read = function(aFile) {
	return FileUtils.readFileToString(new File(aFile));
};

var getVersion = function() {
	return java.lang.System.getProperty("java.version");
};

// publish App object to be accessed from the client
App.publish("Main", {
	read: read,
	version: getVersion,
	sayHello: Object.sayHello,
	fn: lFn
});