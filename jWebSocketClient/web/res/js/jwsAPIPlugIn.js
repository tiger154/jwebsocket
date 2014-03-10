//	---------------------------------------------------------------------------
//	jWebSocket API PlugIn (Community Edition, CE)
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

//:author:*:kyberneees
//:author:*:aschulze

//:package:*:jws
//:class:*:jws.APIPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.APIPlugIn[/tt] instance plug-in. This _
//:d:en:plug-in provides the methods to register and unregister at certain _
//:d:en:stream sn the server.
jws.APIPlugIn = {
	//:const:*:NS:String:org.jwebsocket.plugins.API (jws.NS_BASE + ".plugins.api")
	//:d:en:Namespace for the [tt]APIPlugIn[/tt] class.
	// if namespace changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.api",
	//:const:*:ID:String:APIPlugIn
	//:d:en:Id for the [tt]APIPlugIn[/tt] class.
	ID: "api",
	
	hasPlugIn: function(aId, aOptions) {
		var lToken = {
			ns: jws.APIPlugIn.NS,
			type: "hasPlugIn",
			plugin_id: aId
		};
		var lOptions = {};
		if (aOptions) {
			if (aOptions.OnResponse) {
				lOptions.OnResponse = aOptions.OnResponse;
			}
		}
		this.sendToken(lToken, lOptions);
	},
	
	getPlugInAPI: function(aId, aOptions) {
		var lToken = {
			ns: jws.APIPlugIn.NS,
			type: "getPlugInAPI",
			plugin_id: aId
		};
		var lOptions = {};
		if (aOptions) {
			if (aOptions.OnResponse) {
				lOptions.OnResponse = aOptions.OnResponse;
			}
		}
		this.sendToken(lToken, lOptions);
	},
	
	supportsToken: function(aId, aOptions) {
		var lToken = {
			ns: jws.APIPlugIn.NS,
			type: "supportsToken",
			token_type: aId
		};
		var lOptions = {};
		if (aOptions) {
			if (aOptions.OnResponse) {
				lOptions.OnResponse = aOptions.OnResponse;
			}
		}
		this.sendToken(lToken, lOptions);
	},
	
	getServerAPI: function(aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.APIPlugIn.NS,
				type: "getServerAPI"
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	getPlugInIds: function(aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.APIPlugIn.NS,
				type: "getPlugInIds"
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	getPlugInInfo: function(aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.APIPlugIn.NS,
				type: "getPlugInInfo"
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	createSpecFromAPI: function(aConn, aServerPlugIn) {

		// a plug-in might have more than one feature
		var lCnt = aServerPlugIn.supportedTokens.length;
		var lSpecs = [];

		for (var lIdx = 0; lIdx < lCnt; lIdx++) {

			var lToken = aServerPlugIn.supportedTokens[ lIdx ];
			lToken.ns = aServerPlugIn.namespace;

			// console.log( JSON.stringify( lToken ) );

			// this is the function which has to be executed as a parameter
			// of the it call within a describe statement (actually the suite).
			var lItFunc = function() {
				var lResponseReceived = false;
				// create the automated test token
				var lTestToken = {
					ns: lToken.ns,
					type: lToken.type
				};
				// add all arguments
				var lInArgs = lToken.inArguments;
				for (var lInArgIdx = 0, lInArgCnt = lInArgs.length; lInArgIdx < lInArgCnt; lInArgIdx++) {
					var lInArg = lInArgs[ lInArgIdx ];
					lTestToken[ lInArg.name ] = lInArg.testValue;
				}
				console.log("Automatically sending " + JSON.stringify(lTestToken));
				aConn.sendToken(lTestToken, {
					OnResponse: function(aToken) {
						console.log("Received auto response: " + JSON.stringify(aToken));
						lResponseReceived = true;
					}
				});

				waitsFor(
						function() {
							return lResponseReceived == true;
						},
						"test",
						20000
						);

				runs(function() {
					expect(lResponseReceived).toEqual(true);
					// stop watch for this spec
					// jws.StopWatchPlugIn.stopWatch( "defAPIspec" );
				});
			};

			lSpecs.push(lItFunc);
		}
		// here the spec function are created and returned only
		// but not yet executed!
		return lSpecs;
	}

};


// add the JWebSocket ExtProcess PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.APIPlugIn);
