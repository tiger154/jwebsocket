Ext.define('Ext.jws.data.Proxy', {
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
		       
                       //console.log(lToken)
                       
                         if(aOperation.action == 'read'){
                            
                           lToken.args = [{
                                 page:aOperation.page,
                                 limit:aOperation.limit
                             }]
                             
                         }
                    
                      //callScriptMethod: function(aToken,aCallbacks,aScope)
                      Ext.jws.Client.callScriptMethod(lToken,{
				success: function( aToken ) {
                                    
                                    
				  var lText = Ext.encode( aToken.result );
                                 // var obj = Ext.decode( aToken.result);
				  
                                    
                                     var lResponse = { request: lRequest,
						requestId: lRequest.id,
						status: aToken.code,
						statusText: aToken.msg,
						responseText: lText,
						responseObject: aToken.result
					};

                                      //  console.log(lResponse)

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
				}},aScope);
                      
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
        
       processResponse: function(success, operation, request, response, callback, scope) {
        var me = this,
            reader,
            result;

        if (success === true) {
            reader = me.getReader();

            // Apply defaults to incoming data only for read operations.
            // For create and update, there will already be a client-side record
            // to match with which will contain any defaulted in values.
            reader.applyDefaults = operation.action === 'read';
            
            result = reader.read(response.responseObject);
              
               if (result.success !== false) {
                //see comment in buildRequest for why we include the response object here
               
                   //console.log(result)
                Ext.apply(operation, {
                    response: response,
                    resultSet: result
                });

                operation.commitRecords(result.records);
                operation.setCompleted();
                operation.setSuccessful();
            } else {
                operation.setException(result.message);
                me.fireEvent('exception', this, response, operation);
            }
        } else {
            me.setException(operation, response);
            me.fireEvent('exception', this, response, operation);
        }

        //this callback is the one that was passed to the 'read' or 'write' function above
        if (typeof callback == 'function') {
            callback.call(scope || me, operation);
        }

        me.afterRequest(request, success);
    }, 
        
        
       getObjectID: function(){return this.objectId;}, 
       getApp: function(){return this.app;}, 
       getParams: function(){return this.params;},
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
                                Method = undefined,
				lData;
		var lScope = aRequest;

		if ( Ext.isFunction( lParams ) ) {
			lParams = lParams.call( lScope, aRequest );
		}

		lData = aRequest.rawData || lJsonData || lXMLData || null;
		var lAction = typeof aRequest.getAction === "function" ? aRequest.getAction() : aRequest.action;
		switch ( lAction ) {
			case 'create':
				Method = this.getApi().create;
				break;
			case 'update':
				Method = this.getApi().update;
				break;
			case 'destroy':
				Method = this.getApi().destroy;
				break;
			case 'read':
				Method = this.getApi().read;
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
			type:'callMethod',
                        method:Method,
                        objectId: this.getObjectID(),
                        app: this.getApp(),
                        args:[lJsonData]
                        //args: [aRequest.jsonData,'example']
                        //args: this.getParams()
                        //type: lTokenType,
			//data: lData
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
});

