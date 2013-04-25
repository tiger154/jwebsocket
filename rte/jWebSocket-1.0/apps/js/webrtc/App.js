var lNS = "org.jwebsocket.plugins.webrtc";

// application clients container
var lClients = App.newThreadSafeMap();

// processing connector stopped event
App.on(["connectorStopped", "logoff"], function(aConnector) {
	lUnregister(aConnector);
});

// register a new app client 
var lRegister = function(aConnector) {
	App.logger.debug("Processing register...");

	// client require "webrtc" role
	App.requireAuthority(aConnector, lNS + ".register");
	// registering the client
	lClients.put(aConnector.getUsername(), aConnector);

	App.broadcast(lClients.getValues(), {
		type: "event",
		name: "register",
		user: aConnector.getUsername()
	});
};

// unregister an app client
var lUnregister = function(aConnector) {
	App.logger.debug("Processing unregister...");
	
	var lUsername = aConnector.getUsername();
	if (null === lUsername){
		// client not logged on
		return;
	}
	
	if (lClients.containsKey(aConnector.getUsername())) {
		// removing the client
		lClients.remove(aConnector.getUsername());
	}

	App.broadcast(lClients.getValues(), {
		type: "event",
		name: "unregister",
		user: aConnector.getUsername()
	});
};

var lGetAppClients = function() {
	App.logger.debug("Processing get app clients...");

	return lClients.keySet();
};

// process client command to start a WebRTC session with a target client
var lConnect = function(aTargetUser, aOffer, aConnector) {
	App.logger.debug("Processing connect...");

	App.assertTrue(lClients.constainKey(aConnector.getUsername()), "The client should register first!");
	App.assertTrue(lClients.constainKey(aTargetUser), "The target client does not exists!");

	// sending offer
	App.sendToken(lClients.get(aTargetUser), {
		type: "connect",
		offer: aOffer,
		user: aConnector.getUsername()
	});
};

// process client answer on client WebRTC session request
var lAcceptConnect = function(aTargetUser, aAnswer, aConnector) {
	App.logger.debug("Processing accept connect...");

	App.assertTrue(lClients.constainKey(aConnector.getUsername()), "The client should register first!");
	App.assertTrue(lClients.constainKey(aTargetUser), "The target client does not exists!");

	// sending accept
	App.sendToken(lClients[aTargetUser], {
		type: "accept",
		answer: aAnswer,
		user: aConnector.getUsername()
	});
};

// stop a previous started WebRTC session with a client
var lDisconnect = function(aTargetUser, aConnector) {
	App.logger.debug("Processing disconnect...");

	App.assertTrue(lClients.constainKey(aConnector.getUsername()), "The client should register first!");
	App.assertTrue(lClients.constainKey(aTargetUser), "The target client does not exists!");

	App.sendToken(lClients.get(aTargetUser), {
		type: "disconnect",
		user: aConnector.getUsername()
	});
};

// export application API
App.publish("Main", {
	register: lRegister,
	unregister: lUnregister,
	connect: lConnect,
	accept: lAcceptConnect,
	disconnect: lDisconnect,
	getClients: lGetAppClients
});
