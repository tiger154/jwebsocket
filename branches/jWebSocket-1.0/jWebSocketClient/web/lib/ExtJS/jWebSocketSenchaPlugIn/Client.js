//	<JasobNoObfs>
//  ---------------------------------------------------------------------------
//  jWebSocket - Sencha ExtJS PlugIn (Community Edition, CE)
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

// ## :#file:*:Client.js
// ## :#d:en:Allows including jWebSocket Client in ExtJS/Sencha Touch Applications. _
// ## :#d:en:Gives to ExtJS users a new WebSocket based Ext.data.Proxy and also _
// ## :#d:en:includes the jWebSocket JavaScript Client transparently for the user _ 
// ## :#d:en:inside the class Ext.jwsClient and fires WebSocket events inside it.

/**
 * @author Osvaldo Aguilar Lauzurique, (oaguilar, La Habana), Alexander Rojas Hernandez (arojas, Pinar del Rio), Victor Antonio Barzana Crespo (vbarzana, Münster Westfalen)
 **/

//	---------------------------------------------------------------------------
//  Ext.jws.Client class, this class is a wrapper mechanism for the 
//  jWebSocketTokenClient class to allow having the jWebSocket Client 
//  inside an ExtJS class.
//	---------------------------------------------------------------------------

//:package:*:Ext.jws
//:class:*:Ext.jws.Client
//:ancestor:*:Ext.util.Observable
//:d:en:Implementation of the [tt]jWebSocketTokenClient[/tt] class inside _
//:d:en:a Sencha ExtJS and Sencha Touch frameworks.
Ext.define('Ext.jws.Client', {
	extend: 'Ext.util.Observable',
	alternateClassName: 'Ext.jwsClient',
	singleton: true,
	fTokenClient: undefined,
	constructor: function(aConfig) {
		// Call our superclass constructor to complete construction process.
		this.superclass.constructor.call(this, aConfig);
	},
	//:m:*:init
	//:d:en:This method allows overriding the submit and load methods of the _
	//:d:en:Ext.form prototype and is executed when the connection is opened.
	//:a:en::aConfig:Object:The default configuration.
	//:r:*:::void:none
	init: function(aConfig) {
		if (Ext.form.Basic) {
			//:m:*:Ext.form.submit
			//:d:en:Override the submit method in the prototype of the class Ext.form
			//:a:en::aOptions:Object:The default submit options.
			//:r:*::Ext.form.doAction:function:The function to be executed when submit method of the form is called.
			Ext.form.Basic.prototype.submit = function(aOptions) {
				return this.doAction(this.standardSubmit ? 'standardsubmit' :
						this.api ? 'directsubmit' : this.jwsSubmit ? 'jwssubmit' :
						'submit', aOptions);
			}
			//:m:*:Ext.form.load
			//:d:en:Override the load method in the prototype of the class Ext.form
			//:a:en::aOptions:Object:The default load options.
			//:r:*::Ext.form.doAction:function:The function to be executed when load method of the form is called.
			Ext.form.Basic.prototype.load = function(aOptions) {
				return this.doAction(this.api ? 'directload' : this.jwsSubmit ? 'jwsload' : 'load', aOptions);
			}
		}
	},
	//:m:*:open
	//:d:en:Opens the jWebSocket connection and fires the events to monitor _
	//:d:en:the WebSocket connection with the server. If the browser doesn't _
	//:d:en:support WebSocket and no other fallback mechanism is established _
	//:d:en:then an exception is fired "WebSocket not supported"
	//:a:en::aURL:String:The server URL. This field is optional.
	//:a:en::aTokenClient:jWebSocketTokenClient:Any TokenClient (JSON, XML, CSV). This field is optional, default is jWebSocketJSONClient.
	//:a:en::aTimeout:Integer:The timeout number in miliseconds to wait for the server response when opening the connection. This field is optional.
	//:r:*:::void:none
	open: function(aURL, aTokenClient, aTimeout) {
		// Keeping the scope in a variable to be used when the websocket 
		// events of the jWebSocket TokenClient will be executed 
		var self = this;
		if (jws.browserSupportsWebSockets()) {
			// In case that not given URL gets the default URL to the 
			// jWebSocket Server. The schema ws/wss is automatically selected 
			// by the http/https schema. The default URL is generally composed
			// of schema://host:port/context/servlet 
			// Ex: ws://jwebsocket.org:9797/jWebSocket/jWebSocket
			var lUrl = aURL || jws.getAutoServerURL();

			if (aTokenClient) {
				this.fTokenClient = aTokenClient;
			}
			else {
				if (!this.fTokenClient) {
					this.fTokenClient = new jws.jWebSocketJSONClient();
				}
			}

			// If user pass a custom timeout, then the jWebSocket Client will 
			// take care of that by calling the function setDefaultTimeOut
			if (aTimeout) {
				this.setDefaultTimeOut(aTimeout);
			}
			if (typeof this.fTokenClient.isConnected === "function" && !this.fTokenClient.isConnected()) {
				// Binding the jWebSocketTokenClient callbacks and firing events 
				// within the Ext.jws class when messages arrive from the server
				this.fTokenClient.open(lUrl, {
					OnOpen: function(aToken) {
						// Executing the init method of the class to override the 
						// prototype of load and submit methods of the Ext.form class
						self.init();
						var lMsg = "jWebSocket connection opened";
						self.fireEvent('open', aToken);
						if (Ext.Logger) {
							Ext.Logger.log(lMsg);
						} else {
							jws.console.log(lMsg);
						}
					},
					OnWelcome: function(aToken) {
						self.fireEvent('welcome', aToken);
					},
					OnClose: function(aToken) {
						var lMsg = "jWebSocket connection closed, please " +
								"check that your jWebSocket server is running";
						if (Ext.Logger) {
							Ext.Logger.warn(lMsg);
						} else {
							jws.console.log(lMsg);
						}
						self.fireEvent('close', aToken);
					},
					OnTimeout: function() {
						self.fireEvent('timeout');
					},
					OnLogon: function(aToken) {
						self.fireEvent('logon', aToken);
					},
					OnLogoff: function(aToken) {
						self.fireEvent('logoff', aToken);
					},
					OnMessage: function(aToken) {
						self.fireEvent('message', aToken);
					}
				});
			}
		}
		else {
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			Ext.Error.raise(lMsg);
		}
	},
	//:m:*:getConnection
	//:d:en:Returns the current instance of the jWebSocketTokenClient which _
	//:d:en:keeps the connection opened with the server
	//:a:en::::none
	//:r:*::fTokenClient:jWebSocketTokenClient:The jWebSocketTokenClient class instance.
	getConnection: function() {
		return this.fTokenClient || {};
	},
	//:m:*:isConnected
	//:d:en:Checks if the connection with jWebSocket Server is available or not
	//:a:en::::none
	//:r:*::lIsConnected:Boolean:True if the connection is active.
	isConnected: function() {
		return typeof this.getConnection().isConnected === "function" &&
				this.getConnection().isConnected();
	},
	//:m:*:send
	//:d:en:Sends data to the jWebSocket server
	//:a:en::aNS:String:The namespace of the application in the server that will process the message.
	//:a:en::aType:String:The key string to be sent to the server with which it will process the message.
	//:a:en::aArgs:Object:The data to be sent to the server.
	//:a:en::aCallbacks:Object:An object with the functions to be executed if the server responds.
	//:a:en::aScope:Object:The scope from which the message was sent, when the server responds the callback functions will execute within this scope.
	//:r:*:::void:none
	send: function(aNS, aType, aArgs, aCallbacks, aScope) {

		var lScope = aScope;
		var lToken = {};
		if (aArgs) {
			lToken = Ext.clone(aArgs);
		}
		lToken.ns = aNS;
		lToken.type = aType;

		this.fireEvent('beforesend', lToken);

		this.fTokenClient.sendToken(lToken, {
			// This callback will be fired when the server returns a response 
			// message
			OnResponse: function(aToken) {
				// The code of the token received from the server defines if 
				// the sent message had a successful impact on the server or if 
				// any failure happened while sending or in the server side
				// code < 0 received a failure, otherwise received a success token
				// Note: jWebSocket client also specifies OnSuccess, OnFailure 
				// callbacks that are executed when the new message comes as a 
				// response to a previous message
				if (aToken.code < 0) {
					if ('function' === typeof aCallbacks.failure) {
						if (typeof aScope === "undefined") {
							return aCallbacks.failure(aToken);
						}
						aCallbacks.failure.call(lScope, aToken);
					}
					if ('function' === typeof aCallbacks.OnFailure) {
						if (typeof aScope === "undefined") {
							return aCallbacks.OnFailure(aToken);
						}
						aCallbacks.OnFailure.call(lScope, aToken);
					}
				} else {
					if ('function' === typeof aCallbacks.success) {
						if (typeof aScope === "undefined") {
							return aCallbacks.success(aToken);
						}
						aCallbacks.success.call(lScope, aToken);
					}
					if ('function' === typeof aCallbacks.OnSuccess) {
						if (typeof aScope === "undefined") {
							return aCallbacks.OnSuccess(aToken);
						}
						aCallbacks.OnSuccess.call(lScope, aToken);
					}
				}
			},
			// Fired when the server doesn't reach the server while sending the 
			// message in the defined timeout
			OnTimeOut: function(aToken) {
				if ('function' === typeof aCallbacks.timeout) {
					if (typeof aScope === "undefined") {
						return aCallbacks.timeout(aToken);
					}
					aCallbacks.timeout.call(lScope, aToken);
				}
				if ('function' === typeof aCallbacks.OnTimeout) {
					if (typeof aScope === "undefined") {
						return aCallbacks.OnTimeout(aToken);
					}
					aCallbacks.OnTimeout.call(lScope, aToken);
				}
			}
		});
	},
	//:m:*:addPlugIn
	//:d:en:Adds a PlugIn with a processToken method to the jWebSocketTokenClient. _
	//:d:en:Whenever a message comes will be passed through all the PlugIn chain _
	//:d:en:executing all their processToken methods with the incoming message as parameter.
	//:a:en::aPlugIn:Object:An Object with a processToken method to be executed whenever a message comes from the server.
	//:r:*::void:none
	addPlugIn: function(aPlugIn) {
		this.fTokenClient.addPlugIn(aPlugIn);
	},
	//:m:*:setDefaultTimeOut
	//:d:en:jWebSocket Client defines a constant with the default timeout. This _
	//:d:en:method allows to the user change the default timeout to wait for _
	//:d:en:all the jWebSocket Server responses, the default timeout is 3000
	//:a:en::aTimeout:Integer:The time to wait for every response from the server without firing the timeout event
	//:r:*::void:none
	setDefaultTimeOut: function(aTimeout) {
		if (this.fTokenClient) {
			this.fTokenClient.DEF_RESP_TIMEOUT = aTimeout;
		}
		else {
			jws.DEF_RESP_TIMEOUT = aTimeout;
		}
	},
	//:m:*:close
	//:d:en:Closes the connection with the jWebSocket Server and fires the _
	//:d:en:close event within the Ext.jws namespace.
	//:a:en::::none
	//:r:*::void:none
	close: function() {
		try {
			var lRes = this.fTokenClient.close({
				timeout: 3000
			});

			if (lRes.code != 0) {
				jws.console.log(lRes.msg);
			} else {
				this.fireEvent('close');
			}
		} catch (aException) {
			jws.console.log(aException);
		}
	}
});