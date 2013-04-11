app.importScript("lib/myAppLib.js");

importClass(org.apache.commons.io.FileUtils);
importClass(java.io.File);

var Object = {
	sayHello: sayHello
};

var lFn = function() {
	app.getLogger().debug("Processing lFn...");

	return {
		name: "Rolando SM",
		age: 27
	};
};

var read = function(aFile) {
	return FileUtils.readFileToString(new File(aFile));
};

var getVersion = function() {
	return java.lang.System.getProperty("java.version");
};

app.publish("Main", {
	read: read,
	version: getVersion,
	sayHello: Object.sayHello,
	fn: lFn
});