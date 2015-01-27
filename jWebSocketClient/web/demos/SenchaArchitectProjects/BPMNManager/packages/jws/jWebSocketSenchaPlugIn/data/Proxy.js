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
 * @author Osvaldo Aguilar Lauzurique, (oaguilar, La Habana), Alexander Rojas Hernandez (arojas, Pinar del Rio), Victor Antonio Barzana Crespo (vbarzana, Münster Westfalen)
 **/

//	---------------------------------------------------------------------------
//  This class contains the jWebSocket implementation 
//  of the [tt]Ext.data.proxy.Proxy[/tt] class
//	---------------------------------------------------------------------------

//:package:*:Ext.jws.data
//:class:*:Ext.jws.data.Proxy
//:ancestor:*:Ext.data.proxy.Server
//:d:en:Implementation of an ExtJS/Sencha Touch proxy using a jWebSocket _
//:d:en:connection, in case that the connection with the server is not open _
//:d:en:the proxy opens the connection itself.
Ext.define( 'Ext.jws.data.Proxy', {
	extend: 'Ext.data.proxy.Server',
	requires: ['Ext.jws.Client'],
	alias: 'proxy.jws',
	alternateClassName: 'Ext.jws.JWebSocketProxy',
	config: {
		ns: "",
		api: {
			create: 'create',
			read: 'read',
			update: 'update',
			destroy: 'destroy'
		},
		reader: {
			rootProperty: 'data',
			totalProperty: 'totalCount'
		},
		autoOpen: false
	},
	//:m:*:constructor
	//:d:en:Creates the Ext.jws.data.Proxy, throws an error if namespace is not given
	//:a:en::aConfig:Object:The proxy configuration, this parameter is required.
	//:r:*::void:none
	constructor: function( aConfig ) {
		aConfig = aConfig || { };
		if ( typeof aConfig.ns === "undefined" ) {
			var lMsg = "To instantiate a jWebSocket proxy is required " +
					"a namespace, jws proxy requires a namespace";
			if ( Ext.Logger ) {
				Ext.Logger.error( lMsg );
			} else if ( Ext.error ) {
				Ext.error.raise( lMsg );
			} else {
				jws.console.log( lMsg );
			}
		}
		this.callParent( [ aConfig ] );
		var lFtokenClient = Ext.jws.Client.getConnection() || { };
		if ( typeof lFtokenClient.isConnected === "undefined" && this.config.autoOpen ) {
			var lMsg = "The connection is being opened by the proxy, if you " +
					"don't want to let the proxy open the connection itself " +
					"you can open your own connection by invoking " +
					"Ext.jws.Client.open() in your main app";
			if ( Ext.Logger ) {
				Ext.Logger.warn( lMsg );
			} else {
				jws.console.log( lMsg );
			}
			Ext.jws.Client.open();
		}
	},
	//:m:*:doRequest
	//:d:en:This is the most important method of the proxy, allows sending _
	//:d:en:the data using the jWebSocketClient normal send method.
	//:a:en::aOperation:Ext.data.Operation:Operation objects are used to enable communication between Stores and Proxies
	//:a:en::aCallback:function:The callback to be executed when the operation is complete
	//:a:en::aScope:Object:The scope to execute the callback function
	//:r:*::void:none
	doRequest: function( aOperation, aCallback, aScope ) {
		var self = this;
		var lExecutionScope = function( aOperation, aCallback, aScope, aSelf ) {
			var lWriter = aSelf.getWriter(),
					lRequest = aSelf.buildRequest( aOperation, aCallback, aScope );

			if ( aOperation.allowWrite() ) {
				lRequest = lWriter.write( lRequest );
			}

			var lToken = aSelf.setupDataForRequest( lRequest );
			Ext.jws.Client.send( lToken.ns, lToken.type, lToken.data, {
				success: function( aToken ) {
					var lText = Ext.encode( aToken );
					var lResponse = { request: lRequest,
						requestId: lRequest.id,
						status: aToken.code,
						statusText: aToken.msg,
						responseText: lText,
						responseObject: aToken
					};

					aSelf.processResponse( true, aOperation, lRequest, lResponse, aCallback, aScope );
				},
				failure: function( aToken ) {
					var lText = Ext.encode( aToken );

					var lResponse = {
						request: lRequest,
						requestId: lRequest.id,
						status: aToken.code,
						statusText: aToken.msg,
						responseText: lText,
						responseObject: aToken
					};

					aSelf.processResponse( false, aOperation, lRequest, lResponse, aCallback, aScope );
				}
			}, aScope );
		};
		var lFtokenClient = Ext.jws.Client.getConnection() || { };
		if ( typeof lFtokenClient.isConnected !== "undefined" && lFtokenClient.isConnected() ) {
			lExecutionScope( aOperation, aCallback, aScope, self );
		} else {
			Ext.jws.Client.on( "open", function() {
				lExecutionScope( aOperation, aCallback, aScope, self );
			} );
		}
	},
	//:m:*:setupDataForRequest
	//:d:en:Prepares the data that will be sent to the jWebSocket Server and _
	//:d:en:converts the request into a jWebSocket Token.
	//:a:en::aRequest:Ext.data.Request:Represents the request that will be made by the doRequest method
	//:r:*:lToken:Object:The token to be sent to the jWebSocket server
	setupDataForRequest: function( aRequest ) {
		// In sencha touch the parambs come in aRequest.getParams
		var lParams = typeof aRequest.getParams === "function" ?
				aRequest.getParams() : aRequest.params || { },
				lJsonData = typeof aRequest.getJsonData === "function" ? aRequest.getJsonData() : aRequest.jsonData || { },
				lXMLData = typeof aRequest.getXmlData === "function" ? aRequest.getXmlData() : aRequest.xmlData || { },
				lNS = this.getNs(),
				lTokenType = undefined,
				lData;
		var lScope = aRequest;

		if ( Ext.isFunction( lParams ) ) {
			lParams = lParams.call( lScope, aRequest );
		}

		lData = aRequest.rawData || lJsonData || lXMLData || null;
		var lAction = typeof aRequest.getAction === "function" ? aRequest.getAction() : aRequest.action;
		switch ( lAction ) {
			case 'create':
				lTokenType = this.getApi().create;
				break;
			case 'update':
				lTokenType = this.getApi().update;
				break;
			case 'destroy':
				lTokenType = this.getApi().destroy;
				break;
			case 'read':
				lTokenType = this.getApi().read;
				break;
			default:
				break;
		}
		var lIsEmpty = function( aObject ) {
			for ( var lIdx in aObject ) {
				return false;
			}
			return true;
		}
		lData = !lIsEmpty( lData ) ? lData : lParams || null;
		return  {
			ns: lNS,
			type: lTokenType,
			data: lData
		};
	},
	setException: function( aOperation, aResponse ) {
		aOperation.setException( {
			status: aResponse.status,
			statusText: aResponse.statusText,
			responseText: aResponse.responseText,
			responseObject: aResponse.responseObject
		} );
	}
} );
