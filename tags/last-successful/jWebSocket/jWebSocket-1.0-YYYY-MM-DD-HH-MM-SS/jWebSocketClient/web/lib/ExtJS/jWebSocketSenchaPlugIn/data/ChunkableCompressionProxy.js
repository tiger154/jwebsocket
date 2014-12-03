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
//  including a compression and chunking mechanism
//	---------------------------------------------------------------------------

//:package:*:Ext.jws.data
//:class:*:Ext.jws.data.ChunkableCompressionProxy
//:ancestor:*:Ext.jws.data.Proxy
//:d:en:Implementation of an ExtJS proxy using the jWebSocket connection, _ 
//:d:en:zip compression and chunking, in case that you wish to use compression _ 
//:d:en:you have to include the following files in your html index: _
//:d:en:  (jszip.js, jszip-deflate.js, jszip-inflate.js, jws/jszip-load.js) _
//:d:en: These files can be downloaded here https://github.com/Stuk/jszip
Ext.define( 'Ext.jws.data.ChunkableCompressionProxy', {
	alternateClassName: 'Ext.jws.data.ChunkableProxy',
	extend: 'Ext.jws.data.Proxy',
	alias: 'proxy.jws.chunkable',
	compression: "none",
	//:m:*:constructor
	//:d:en:Creates the Ext.jws.data.Proxy, throws an error if namespace is not given
	//:a:en::aConfig:Object:The proxy configuration, this parameter is required.
	//:r:*::void:none
	constructor: function( aConfig ) {
		var self = this;
		self.callParent( arguments );

		if ( !JSZip && (self.compression === "zip" || self.compression === "zipbase64") ) {
			Ext.Error.raise( "To instantiate a jWebSocket proxy using the " +
					"compression mechanism " + self.compression );
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
		var self = this,
				lWriter = self.getWriter(),
				lRequest = self.buildRequest( aOperation, aCallback, aScope );

		if ( aOperation.allowWrite() ) {
			lRequest = lWriter.write( lRequest );
		}

		var lToken = this.setupDataForRequest( lRequest );
		// In case that the user wants to get the information from the server in chunks
		if ( aOperation.action === "read" && self.reader.chunks ) {
			lToken.data = lToken.data || { };
			lToken.data.chunkable = true;
			var lTokenType = lToken.type;
			if ( !self.processToken ) {
				var lData = [ ];
				//Listener to wait for the chunks
				self.processToken = function( aToken ) {
					if ( aToken.ns == self.ns && aToken.type == lTokenType ) {
						lData[ aToken.chunkType ] = aToken.data;

						if ( aToken.isLastChunk ) {
							var lJSON = "", lKey;
							for ( lKey in lData ) {
								lJSON += lData[ lKey ];
							}

							if ( self.compression == "zip" ) {
								lJSON = jws.tools.unzip( lJSON, false );
							} else if ( self.compression == "zipbase64" ) {
								lJSON = jws.tools.unzip( lJSON, true );
							}

							self.processResponse( true, aOperation, lRequest, JSON.parse( lJSON ), aCallback, aScope );
							lData = [ ];
						}
					}

				}
				Ext.jws.Client.addPlugIn( self );
			}
			Ext.jws.Client.send( lToken.ns, lToken.type, lToken.data );

		} else if ( aOperation.action === "write" && self.writer.chunks && self.writer.chunksize ) {
			var lChunkSize = self.writer.chunksize,
					lData = JSON.stringify( lToken.data ),
					lLength = lData.length / lChunkSize,
					lIdx;

			if ( self.compression == "zip" ) {
				lData = jws.tools.zip( lData, false );
			} else if ( self.compression == "zipbase64" ) {
				lData = jws.tools.zip( lData, true );
			}
			var lStreamId = -1,
					lChunkingCallbacks = {
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

					self.processResponse( false, aOperation, lRequest, lResponse, aCallback, aScope );
				}
			};
			for ( lIdx = 0; lIdx < lLength; lIdx = (lIdx + lChunkSize) < lLength ? lIdx + lChunkSize : lLength ) {
				lToken.chunkType = "stream" + (++lStreamId);
				lToken.isChunk = true;
				lToken.isLastChunk = lIdx !== lLength;
				if ( lToken.isLastChunk ) {
					lChunkingCallbacks.success = function( aToken ) {
						var lResponse = {
							request: lRequest,
							requestId: lRequest.id,
							status: aToken.code,
							statusText: aToken.msg,
							responseText: lText,
							responseObject: aToken
						};

						self.processResponse( true, aOperation, lRequest, lResponse, aCallback, aScope );
					}
				}
				Ext.jws.Client.send( lToken.ns, lToken.type, lData.substr( lIdx,
						(lIdx + lChunkSize) < lLength ? lIdx + lChunkSize : lLength ), lChunkingCallbacks );
			}
		} else { // In case that a normal request be done
			Ext.jws.Client.send( lToken.ns, lToken.type, lToken.data, {
				success: function( aToken ) {
					if ( aOperation.action === "read" ) {
						if ( self.compression == "zip" ) {
							aToken.data = jws.tools.unzip( aToken.data, false );
						} else if ( self.compression == "zipbase64" ) {
							aToken.data = jws.tools.unzip( aToken.data, true );
						}
					} else if ( aOperation.action === "write" ) {
						if ( self.compression == "zip" ) {
							aToken.data = jws.tools.zip( aToken.data, false );
						} else if ( self.compression == "zipbase64" ) {
							aToken.data = jws.tools.zip( aToken.data, true );
						}
					}

					var lText = Ext.encode( aToken );

					var lResponse = {
						request: lRequest,
						requestId: lRequest.id,
						status: aToken.code,
						statusText: aToken.msg,
						responseText: lText,
						responseObject: aToken
					};

					self.processResponse( true, aOperation, lRequest, lResponse, aCallback, aScope );
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

					self.processResponse( false, aOperation, lRequest, lResponse, aCallback, aScope );
				}
			}, aScope );
		}
	}
} );