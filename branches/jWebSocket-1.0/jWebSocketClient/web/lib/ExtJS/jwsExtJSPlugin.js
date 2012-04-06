		
Ext.define( 'Ext.jws', {
    extend: 'Ext.util.Observable',
    singleton: true,

    constructor: function( config ) {

        this.addEvents( {
            "open" : true,
            "close" : true,
            "timeout":true
        } );

        // Call our superclass constructor to complete construction process.
        this.superclass.constructor.call( this, config )
    },
		
    init:function( config ) {
        
        /*=== Override the submit method in the prototype of the class=====*/
        Ext.form.Basic.prototype.submit = function( options ) {
            return this.doAction( this.standardSubmit ? 'standardsubmit' : this.api ? 'directsubmit': this.jwsSubmit ? 'jwssubmit' : 'submit', options );
	}
	
        /*=== Override the load method in the prototype of the class=====*/
	Ext.form.Basic.prototype.load = function( options ) {
            return this.doAction( this.api ? 'directload' : this.jwsSubmit ? 'jwsload' : 'load', options );
	}
							
    },

    open: function( jwsServerURL, aTokenClient, timeout ) {
        var me = this;
        if( jws.browserSupportsWebSockets() ) {
            var url = jwsServerURL || jws.getDefaultServerURL();

            if( aTokenClient )
                this.aTokenClient = aTokenClient;
            else
                this.aTokenClient = new jws.jWebSocketJSONClient();


            this.aTokenClient.open( url, {
                OnOpen: function( aToken ) {
                    me.init();
                    me.fireEvent( 'open' );
                },
                OnClose: function() {
                    me.fireEvent( 'close' );
                },
                OnTimeout: function() {
                    me.fireEvent( 'timeout' );
                }
            } );
            if( timeout )
                this.setDefaultTimeOut( timeout );
        }
        else{
            var lMsg = jws.MSG_WS_NOT_SUPPORTED;
            Ext.Error.raise( lMsg );
        }
    },
		
    send: function( ns, type, args, callbacks, scope ) {
        
        var meScope  = scope;
        var lToken   = {};
        if ( args ) {
            lToken = args;
        }
        lToken.ns   = ns;
        lToken.type = type;

        this.aTokenClient.sendToken( lToken, {
            callbacks: callbacks,
            OnResponse: function( token ) {
                if ( token.code == -1 ) {
                    if( scope == undefined )
                        return callbacks.failure( token );
                    return callbacks.failure.call( meScope,token );

                }
                else if ( token.code == 0 ) {
                    if( scope == undefined )
                        return callbacks.success( token );
                    return callbacks.success.call( meScope,token );
						
                }
            },
            OnTimeOut: function( token ) {
                if( scope == undefined )
                    return callbacks.timeout( token );
                return callbacks.timeout.call( meScope,token );
					
            }
        } );
    },
		
    addPlugIn: function( aPlugin ) {
        this.aTokenClient.addPlugIn( aPlugin );
    },
    setDefaultTimeOut:function( timeout ) {

        if( this.aTokenClient )
            this.aTokenClient.DEF_RESP_TIMEOUT = timeout;
        else
            jws.DEF_RESP_TIMEOUT = timeout;
    },
    close : function() {
        this.aTokenClient.close();
        this.fireEvent( 'close' );
    }

} );
	
	
 /*
 *    This class is the jWebSocket implementation for Ext.data.proxy     
 *                
 */       

Ext.define( 'Ext.jws.data.proxy', {
    extend: 'Ext.data.proxy.Server',
        
    /**
     * @cfg {String} ns default namespace used for all proxy's actions ( read, write, create,  )
    */
       
    ns: undefined,
    
    /**
     * @cfg {Object} api define tokens for each action that will be performs by the proxy
    */
    api: {
        create : 'create',
        read   : 'read',
        update : 'update',
        destroy: 'destroy'
    },     
       
    /**
     * Creates the proxy, throws an error if namespace is not given
     * @param {Object} config Config object is not Opcional.
     */
    constructor: function( config ) {
        me = this;
			
        me.callParent( arguments );
        
        if ( me.ns == undefined )
            Ext.Error.raise( "the namespace must be specify, jwk proxy need a namespace" );
				       
    },

    
    doRequest: function( operation, callback, scope ) {

        var  me      = this;
        var  writer  = this.getWriter(),
        request = this.buildRequest( operation, callback, scope );
            
        if ( operation.allowWrite() ) {
            request = writer.write( request );
        }
        
        var requestData = this.setupDataForRequest( request );
        
        
        
        Ext.jws.send( requestData.ns, requestData.tt,requestData.data,{
            success : function( aToken ) {
                
                var text = Ext.encode( aToken );
                
                var response = {
                    request       : request,
                    requestId     : request.id,
                    status        : aToken.code,
                    statusText    : aToken.msg,
                    responseText  : text,
                    responseObject: aToken
                };
                
                me.processResponse( true, operation, request, response, callback, scope );
                
                                
            },
            failure:  function( aToken ) {
                
                    var text = Ext.encode( aToken );
                    
                    var response = {
                        request       : request,
                        requestId     : request.id,
                        status        : aToken.code,
                        statusText    : aToken.msg,
                        responseText  : text,
                        responseObject: aToken
                    };
                    
                me.processResponse( false, operation, request, response, callback, scope );
            }
        },scope );
        
    },
    setupDataForRequest:function( options ) {
            
        var params  = options.params || {},                     
        jsonData    = options.jsonData,
        nameSpace   = this.ns,
        tokenType   = undefined,
        data;
            
        var scope = options;
            
        if ( Ext.isFunction( params ) ) {
            params = params.call( scope, options );
        }
                
        data = options.rawData || options.xmlData || jsonData || null;    
               
            
            
        switch ( options.action ) {
            case 'create':
                tokenType = this.api.create;
                break;
            case 'update':
                tokenType = this.api.update;
                break;
            case 'destroy':
                tokenType = this.api.destroy;
                break;
            case 'read':
                tokenType = this.api.read;
                break;
            default:
                break;
        }
            
        return  {
            ns: nameSpace,
            tt: tokenType,
            data: data || params || null
        };
            
            
    },
    setException: function( operation, response ) {
        operation.setException( {
            status        : response.status,
            statusText    : response.statusText,
            responseText  : response.responseText,
            responseObject: response.responseObject
        } );     
    }
} );

/*
 * This class is the jWebSocket implementation for Ext.form.action.Submit
 * 
 * 
 */

Ext.define( 'Ext.jws.form.action.Submit', {
    extend:'Ext.form.action.Submit',
    alternateClassName: 'Ext.jws.form.Action.Submit',
    alias: 'formaction.jwssubmit',

    type: 'jwssubmit',
    ns: undefined,
    tokentype: undefined,

    constructor:function( config ) {
        me = this;

        me.callParent( arguments );

        if ( me.ns == undefined )
			Ext.Error.raise( "you must specify the namespace" );
        if ( me.tokentype == undefined )
			Ext.Error.raise( "you must specify the tokentype" );
    },


    getNS: function() {
        return this.ns  || this.form.ns;
       },
     getTokenType: function() {
         return this.tokentype || this.form.tokentype;
       },

    doSubmit: function() {
        var formEl,
            jwsOptions = Ext.apply( {
                ns: this.getNS(),
                tokentype: this.getTokenType()
              } );
          var callbacks = this.createCallback();

        if ( this.form.hasUpload() ) {
            formEl = jwsOptions.form = this.buildForm();
            jwsOptions.isUpload = true;

        } else {
            jwsOptions.params = this.getParams();

        }

        Ext.jws.send( jwsOptions.ns,jwsOptions.tokentype,jwsOptions.params,callbacks,this );
        if ( formEl ) {
            Ext.removeNode( formEl );
        }
    },
     processResponse : function( response ) {
        this.response = response;
        if ( !response.responseText && !response.responseXML && !response.type ) {
            return true;
        }
        return ( this.result = this.handleResponse( response ) );
    },
    handleResponse: function( response ) {
        if ( response ) {
            records = response.data;
            data = [];
            if ( records ) {
                for( i = 0, len = records.length; i < len; i++ ) {
                    data[i] = records[i];
                }
            }
            
            if ( data.length < 1 ) {
                data = null;
            }
            return {
                success : response.success,
                data : data
            };
        }
        return Ext.decode( response.data );
    }
  
} );

/*
 * 
 * This class is the jWebSocket implementation for Ext.form.action.Load
 * 
 * 
 */

Ext.define( 'Ext.jws.form.action.Load', {
    extend:'Ext.form.action.Load',
    requires: ['Ext.direct.Manager'],
    alternateClassName: 'Ext.jws.form.action.Load',
    alias: 'formaction.jwsload',

    type: 'jwsload',
    ns: undefined,
    tokentype: undefined,

    constructor:function( config ) {
        me = this;
        me.callParent( arguments );

        if ( me.ns == undefined )
			Ext.Error.raise( "you must specify the namespace" );
        if ( me.tokentype == undefined )
			Ext.Error.raise( "you must specify the tokentype" );
    },


run: function() {
        var callbacks =  this.createCallback();
        Ext.jws.send( Ext.apply( 
            {
                ns:this.ns,
                tokentype:this.tokentype,
                params: this.getParams()
            }
         ) );
        Ext.jws.send( this.ns,this.tokentype,this.getParams(),callbacks,this );
        
    },

    processResponse : function( response ) {
        this.response = response;
        if ( !response.responseText && !response.responseXML && !response.type ) {
            return true;
        }
        return ( this.result = this.handleResponse( response ) );
    },
    
  handleResponse: function( response ) {
        if ( response ) {
            
            data = response.data[0] ? response.data : null;
            return {
                success : response.success,
                data : data
            };
        }
        return Ext.decode( response.data );
    }

} );











