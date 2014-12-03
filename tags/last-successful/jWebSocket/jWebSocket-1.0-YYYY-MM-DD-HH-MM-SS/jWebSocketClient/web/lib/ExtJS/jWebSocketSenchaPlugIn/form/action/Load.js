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

/**
 * @author Osvaldo Aguilar Lauzurique, (oaguilar, La Habana), 
 * Alexander Rojas Hernandez (arojas, Pinar del Rio), 
 * Victor Antonio Barzana Crespo (vbarzana, Münster Westfalen)
 **/

//	---------------------------------------------------------------------------
//  This class constains the jWebSocket implementation of the 
//  [tt]Ext.form.action.Load[/tt] class
//	---------------------------------------------------------------------------

//:package:*:Ext.jws.form.action
//:class:*:Ext.jws.form.action.Load
//:ancestor:*:Ext.form.action.Load
//:d:en:Implementation of the default load action of the form but using _
//:d:en:jWebSocketClient to get the data from the jWebSocket Server. _
//:d:en:This class representsis a proxy for the underlying BasicForm's load call
Ext.define('Ext.jws.form.action.Load', {
	extend: 'Ext.form.action.Load',
	requires: ['Ext.direct.Manager', 'Ext.jws.Client'],
	alternateClassName: 'Ext.jws.form.action.Load',
	alias: 'formaction.jwsload',
	type: 'jwsload',
	ns: undefined,
	tokentype: undefined,
	//:m:*:constructor
	//:d:en:Creates the Ext.jws.form.action.Load class, this object will be _
	//:d:en:passed as parameter in the form.load method and will use the _
	//:d:en:current implementation to process the load action using jWebSocketClient
	//:a:en::aConfig:Object:The proxy configuration, this parameter is required.
	//:r:*::void:none
	constructor: function( ) {
		var lSelf = this;
		lSelf.callParent(arguments);

		if (typeof lSelf.ns === "undefined") {
			Ext.Error.raise("You must specify a namespace (ns) value!");
		}

		if (typeof lSelf.tokentype === "undefined") {
			Ext.Error.raise("You must specify a token type (tokentype) value!");
		}
	},
	//:m:*:getNS
	//:d:en:Returns the namespace from the current form action
	//:a:en::::none
	//:r:*:lNs:String:The namespace
	getNS: function() {
		return this.ns || this.form.ns;
	},
	//:m:*:getTokenType
	//:d:en:Returns the tokentype from the current form action
	//:a:en::::none
	//:r:*:lTokenType:String:The type of the token
	getTokenType: function() {
		return this.tokentype || this.form.tokentype;
	},
	//:m:*:run
	//:d:en:This method is executed by the internal doAction method to _
	//:d:en:perform application-specific processing. By overriding this method _
	//:d:en:the request to load the data from the server will be made using _
	//:d:en:the jWebSocketClient
	//:a:en::::none
	//:r:*::void:none
	run: function( ) {
		var lCallbacks = this.createCallback();
		Ext.jws.Client.send(this.getNS(), this.getTokenType(), this.getParams(), lCallbacks, this);
	},
	//:m:*:processResponse
	//:d:en:Processes the response returned by the server and fires the success _
	//:d:en:or failure callback depending on the results
	//:a:en::aResponse:Object:The response returned by the server
	//:r:*:fResult:Object:The response ready for the user
	processResponse: function(aResponse) {
		this.fResponse = aResponse;
		if (!aResponse.responseText && !aResponse.responseXML && !aResponse.type) {
			return true;
		}
		return (this.fResult = this.handleResponse(aResponse));
	},
	//:m:*:handleResponse
	//:d:en:Internal method to change the incoming jWebSocket server resopnse _
	//:d:en:to a normal ExtJS response, there are two important aspects to know _
	//:d:en:will be failure when the code != 0 and success variable not defined _
	//:d:en:or false, in other case the response will be success
	//:a:en::aResponse:Object:The response returned by the server
	//:r:*:fResult:Object:The response modified and ready for the client
	handleResponse: function(aResponse) {
		if (aResponse) {
			var lData = aResponse.data ? aResponse.data[0] : null;
			return {
				success: aResponse.success || aResponse.code === 0,
				data: lData
			};
		}
		return Ext.decode(aResponse.data);
	}
});