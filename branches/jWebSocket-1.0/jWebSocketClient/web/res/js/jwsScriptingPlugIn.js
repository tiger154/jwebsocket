//	---------------------------------------------------------------------------
//	jWebSocket Scripting Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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

//:package:*:jws
//:class:*:jws.ScriptingPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.ScriptingPlugIn[/tt] class.
//:d:en:This client-side plug-in provides the API to access the features of the _
//:d:en:Scripting plug-in on the jWebSocket server.
jws.ScriptingPlugIn = {
	//:const:*:NS:String:org.jwebsocket.plugins.scripting (jws.NS_BASE + ".plugins.scripting")
	//:d:en:Namespace for the [tt]ScriptingPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.scripting",
	//:const:*:JWS_NS:String:scripting
	//:d:en:Namespace within the jWebSocketClient instance.
	// if namespace changed update the applications accordingly!
	JWS_NS: "scripting",
	callJsMethod: function(aApp, aObjectId, aMethod, aArgs, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.ScriptingPlugIn.NS,
				type: "callJsMethod",
				method: aMethod,
				objectId: aObjectId,
				app: aApp,
				args: aArgs
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	reloadJsApp: function(aApp, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.ScriptingPlugIn.NS,
				type: "reloadApp",
				app: aApp
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	sendJsToken: function(aApp, aToken, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code && aToken) {
			aToken.app = aApp;
			aToken.ns = jws.ScriptingPlugIn.NS;
			aToken.type = "token";

			this.sendToken(aToken, aOptions);
		}
		return lRes;
	}

};

// add the JWebSocket Scripting PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.ScriptingPlugIn);
