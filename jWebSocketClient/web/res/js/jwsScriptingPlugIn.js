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

	//:m:*:processToken
	//:d:en:Processes an incoming token from the server side scripting plug-in and _
	//:d:en:checks if certains events have to be fired. _
	//:d:en:If e.g. the request type was [tt]selectSQL[/tt] and data is _
	//:d:en:returned the [tt]OnScriptingRowSet[/tt] event is fired. Normally this _
	//:d:en:method is not called by the application directly.
	//:a:en::aToken:Object:Token to be processed by the plug-in in the plug-in chain.
	//:r:*:::void:none
	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns === jws.ScriptingPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			/*
			if( "selectSQL" === aToken.reqType ) {
				if( this.OnScriptingRowSet ) {
					this.OnScriptingRowSet( aToken );
				}
			}
			*/
		}
	},

	//:m:*:invokeJavaScript
	//:d:en:Pending...
	//:a:en::aQuery:String:Single SQL query string to be executed by the server side Scripting plug-in.
	//:a:en::aOptions:Object:Optional arguments, please refer to the [tt]sendToken[/tt] method of the [tt]jWebSocketTokenClient[/tt] class for details.
	//:r:*:::void:none
	invokeJavaScript: function( aAlias, aFunction, aArgs, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 === lRes.code ) {
			var lToken = {
				ns: jws.ScriptingPlugIn.NS,
				type: "invokeJavaScript",
				alias: aAlias,
				function: aFunction,
				args: aArgs
			};
			this.sendToken( lToken, aOptions );
		}
		return lRes;
	},

	setScriptingCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnScriptingMsg !== undefined ) {
			this.OnScriptingMsg = aListeners.OnScriptingMsg;
		}
	}

};

// add the JWebSocket Scripting PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.ScriptingPlugIn );
