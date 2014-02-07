//	<JasobNoObfs>
//  ---------------------------------------------------------------------------
//  jWebSocket - jQuery PlugIn (Community Edition, CE)
//  ---------------------------------------------------------------------------
//  Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  ---------------------------------------------------------------------------
//	</JasobNoObfs>

// ## :#file:*:jWebSocketJQueryPlugIn.js
// ## :#version:*:1.0
// ## :#d:en:Allows including jWebSocket Client in jQuery Applications. _
// ## :#d:en:Gives to jQuery users a new way to create their jWebSocket applications_
// ## :#d:en:includes the jWebSocket JavaScript Client transparently for the user _ 
// ## :#d:en:inside a jQuery namespace $.jws and fires WebSocket events inside it.

/**
 * @author Carlos Feyt, (cfeyt, La Habana), Victor Antonio Barzana Crespo (vbarzana, La Habana)
 **/

(function($) {
	//:package:*:jQuery
	//:class:*:$.jws
	//:ancestor:*:jQuery
	//:d:en:Implementation of the [tt]jWebSocketTokenClient[/tt] class inside _
	//:d:en:jQuery framework.
	$.jws = $({});

	//:m:*:$.jws.open
	//:d:en:Opens the jWebSocket connection and fires the corresponding events _
	//:d:en:If the browser doesn't support WebSocket and no other fallback _
	//:d:en:mechanism is established then an exception is fired "WebSocket _
	//:d:en:not supported"
	//:a:en::aURL:String:The server URL. This field is optional.
	//:a:en::aTokenClient:jWebSocketTokenClient:Any TokenClient (JSON, XML, CSV). This field is optional, default is jWebSocketJSONClient.
	//:a:en::aTimeout:Integer:The timeout number in miliseconds to wait for the server response when opening the connection. This field is optional.
	//:r:*:::void:none
	$.jws.open = function(aURL, aTokenClient, aTimeout) {
		if (jws.browserSupportsWebSockets()) {
			var lURL = aURL || jws.getAutoServerURL();

			if (aTokenClient) {
				$.jws.fTokenClient = aTokenClient;
			} else {
				$.jws.fTokenClient = new jws.jWebSocketJSONClient();
			}

			if (!this.isConnected()) {
				var lOptions = {
					OnOpen: function(aToken) {
						$.jws.fTokenClient.addPlugIn($.jws);
						$.jws.trigger('open', aToken);
					},
					OnWelcome: function(aToken) {
						$.jws.trigger('welcome', aToken);
					},
					OnClose: function(aToken) {
						var lMsg = "jWebSocket connection closed, please " +
								"check that your jWebSocket server is running";
						jws.console.log(lMsg);
						$.jws.trigger('close', aToken);
					},
					OnTimeout: function(aToken) {
						$.jws.trigger('timeout', aToken);
					},
					OnLogon: function(aToken) {
						$.jws.trigger('logon', aToken);
					},
					OnLogoff: function(aToken) {
						$.jws.trigger('logoff', aToken);
					},
					OnMessage: function(aToken) {
						$.jws.trigger('message', aToken);
					}
				};
				if (aTimeout) {
					lOptions.timeout = aTimeout;
				}
				$.jws.fTokenClient.open(lURL, lOptions);
			} else {
				jws.console.log("The connection was already open while " +
						"trying to open a new one!");
			}
		} else {
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			alert(lMsg);
		}
	};

	//:m:*:send
	//:d:en:Sends data to the jWebSocket server
	//:a:en::aNS:String:The namespace of the application in the server that will process the message, required.
	//:a:en::aType:String:The token type to be sent to the server with which it will process the message, required.
	//:a:en::aArgs:Object:The parameters of the request, not required.
	//:a:en::aCallbacks:Object:An object with the functions (success, failure, timeout) to be executed if the server responds, not required.
	//:a:en::aOptions:Object:The jWebSocket default send options, not required.
	//:r:*:::void:none
	$.jws.send = function(aNs, aType, aArgs, aCallbacks, aOptions) {
		if (aNs && aType) {
			var lToken = {};
			if (aArgs) {
				lToken = aArgs;
			}
			lToken.ns = aNs;
			lToken.type = aType;

			var lOptions = {
				OnResponse: function(aToken) {
					if (aToken.code === -1) {
						if (aCallbacks &&
								typeof aCallbacks.failure === "function") {
							return aCallbacks.failure(aToken);
						}
					}
					else if (aToken.code === 0) {
						if (aCallbacks &&
								typeof aCallbacks.success === "function") {
							return aCallbacks.success(aToken);
						}
					}
				},
				OnTimeOut: function() {
					if (aCallbacks &&
							typeof aCallbacks.timeout === "function") {
						return aCallbacks.timeout();
					}
				}
			};
			if (aOptions && aOptions.timeout) {
				lOptions.timeout = aOptions.timeout;
			}
			if (this.isConnected()) {
				this.fTokenClient.sendToken(lToken, lOptions);
			} else {
				jws.console.log("Trying to send a message using a non " +
						"connected client, please verify that your jWebSocket" +
						" connection is opened");
			}
		} else {
			jws.console.log("The namespace and the token type are required, " +
					"please provide them correctly to send the data to the server");
		}
	};

	//:m:*:processToken
	//:d:en:Private method used to know when a new message arrives from the _
	//:d:en:server, fires events with every incoming message_
	//:a:en::::none
	//:r:*::void:none
	$.jws.processToken = function(aToken) {
		$.jws.trigger('all:all', aToken);
		$.jws.trigger('all:' + aToken.type, aToken);
		$.jws.trigger(aToken.ns + ':all', aToken);
		$.jws.trigger(aToken.ns + ':' + aToken.type, aToken);
	};

	//:m:*:getDefaultServerURL
	//:d:en:Returns the current instance of the jWebSocketTokenClient which _
	//:d:en:contains the reference to the opened connection with the server
	//:a:en::::none
	//:r:*::fTokenClient:jWebSocketTokenClient:The jWebSocketTokenClient class instance.
	$.jws.getDefaultServerURL = function() {
		if (this.fTokenClient &&
				typeof this.fTokenClient.getAutoServerURL === "function") {
			return this.fTokenClient.getAutoServerURL();
		} else {
			return jws.getAutoServerURL();
		}
	};

	//:m:*:setDefaultTimeOut
	//:d:en:jWebSocket Client defines a constant with the default timeout. This _
	//:d:en:method allows to the user change the default timeout to wait for _
	//:d:en:all the jWebSocket Server responses, the default timeout is 3000
	//:a:en::aTimeout:Integer:The time to wait for every response from the server without firing the timeout event
	//:r:*::void:none
	$.jws.setDefaultTimeOut = function(aTimeout) {
		if (this.fTokenClient) {
			this.fTokenClient.DEF_RESP_TIMEOUT = aTimeout;
		} else {
			jws.DEF_RESP_TIMEOUT = aTimeout;
		}
	};

	//:m:*:close
	//:d:en:Closes the connection with the jWebSocket Server and fires the _
	//:d:en:close event within the $.jws namespace.
	//:a:en::::none
	//:r:*::void:none
	$.jws.close = function() {
		try {
			var lRes = this.fTokenClient.close({
				timeout: jws.DEF_RESP_TIMEOUT
			});

			if (lRes.code !== 0) {
				jws.console.log(lRes.msg);
			} else {
				$.jws.trigger('close', lRes);
			}
		} catch (aException) {
			jws.console.log(aException);
		}
	};

	//:m:*:setTokenClient
	//:d:en:Wraps an existing jWebSocketTokenClient inside our application_
	//:d:en:so, that we can also use jQueryPlugIn inside an opened connection_
	//:d:en:and will respond with the same efectivity.
	//:a:en::fTokenClient:jWebSocketTokenClient:an existing jWebSocketTokenClient class instance.
	//:r:*::void:none
	$.jws.setTokenClient = function(aTokenClient) {
		$.jws.fTokenClient = aTokenClient;
		$.jws.fTokenClient.addPlugIn($.jws);
	};

	//:m:*:getConnection
	//:d:en:Returns the current instance of the jWebSocketTokenClient which _
	//:d:en:contains the reference to the opened connection with the server
	//:a:en::::none
	//:r:*::fTokenClient:jWebSocketTokenClient:The jWebSocketTokenClient class instance.
	$.jws.getConnection = function() {
		return this.fTokenClient;
	};

	//:m:*:isConnected
	//:d:en:Returns true if the connection with jWebSocket Server is opened_
	//:d:en:allows the user know the state of their connections.
	//:a:en::::none
	//:r:*::aIsConnected:Boolean:Return true if the connection is open, false in other case.
	$.jws.isConnected = function( ) {
		if ($.jws.fTokenClient) {
			return $.jws.fTokenClient.isConnected();
		}
		return false;
	};

	//:m:*:addPlugIn
	//:d:en:Adds a PlugIn with a processToken method to the jWebSocketTokenClient. _
	//:d:en:Whenever a message comes will be passed through all the PlugIn chain _
	//:d:en:executing all their processToken methods with the incoming message as parameter.
	//:a:en::aPlugIn:Object:An Object with a processToken method to be executed whenever a message comes from the server.
	//:r:*::void:none
	$.jws.addPlugIn = function(aPlugIn) {
		if (aPlugIn && typeof aPlugIn.processToken === "function") {
			$.jws.fTokenClient.addPlugIn(aPlugIn);
		} else {
			throw ("Please check, your PlugIn Object should contain a " +
					"processToken function");
		}
	};

})(jQuery);
