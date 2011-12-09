//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.EventsCallbacksHandler
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsCallbacksHandler[/tt] class. _
//:d:en:This class handle request callbacks on the events plug-in
jws.oop.declareClass( "jws", "EventsCallbacksHandler", null, {
	OnTimeout: function(rawRequest, aArgs){
		if (undefined != aArgs.meta.OnTimeout){
			aArgs.meta.OnTimeout(rawRequest);
		}
	}
	,
	OnResponse: function(aResponseEvent, aArgs){
		aArgs.meta.elapsedTime = (new Date().getTime()) - aArgs.sentTime;

		if (undefined != aArgs.meta.eventDefinition){
			var index = aArgs.filterChain.length - 1;
			while (index > -1){
				try
				{
					aArgs.filterChain[index].afterCall(aArgs.meta, aResponseEvent);
				}
				catch(err)
				{
					switch (err)
					{
						case "stop_filter_chain":
							return;
							break;
						default:
							throw err;
							break;
					}
				}
				index--;
			}
		}
		
		if (aResponseEvent.code == 0){
			if (undefined != aArgs.meta.OnResponse)
				aArgs.meta.OnResponse(aResponseEvent);

			if (undefined != aArgs.meta.OnSuccess)
				aArgs.meta.OnSuccess(aResponseEvent);
		}
		else {
			if (undefined != aArgs.meta.OnResponse)
				aArgs.meta.OnResponse(aResponseEvent);

			if (undefined != aArgs.meta.OnFailure)
				aArgs.meta.OnFailure(aResponseEvent);
		}
	}
});

//:file:*:jwsEventsPlugIn.js
//:d:en:Implements the EventsPlugIn in the client side

//:package:*:jws
//:class:*:jws.EventsNotifier
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsNotifier[/tt] class. _
//:d:en:This class handle raw events notifications to/from the server side.
jws.oop.declareClass( "jws", "EventsNotifier", null, {
	ID: ""
	,
	jwsClient: {}
	,
	NS: ""
	,
	filterChain: []
	,
	plugIns: []
	,
	//:m:*:initialize
	//:d:en:Initialize this component. 
	//:a:en::::none
	//:r:*:::void:none
	initialize : function(){
		//Registering the notifier as plug-in of the used connection
		this.jwsClient.addPlugIn(this);
		
		//Initializing each filters
		for (var i = 0, end = this.filterChain.length; i < end; i++){
			if (this.filterChain[i]["initialize"]){
				this.filterChain[i].initialize(this);
			}
		}
	}
	,
	//:m:*:notify
	//:d:en:Notify an event in the server side
	//:a:en::aEventName:String:The event name.
	//:a:en::aOptions:Object:Contains the event arguments and the OnResponse, OnSuccess and OnFailure callbacks.
	//:r:*:::void:none
	notify: function(aEventName, aOptions){
		if (this.jwsClient.isConnected()){
			var lToken = {};
			if (aOptions.args){
				lToken = aOptions.args;
				delete (aOptions.args);
			}
			lToken.ns   = this.NS;
			lToken.type = aEventName;
			
			aOptions.UTID = jws.tools.generateSharedUTID(lToken);
			
			var request;
			if (!aOptions['OnResponse'] && !aOptions['OnSuccess'] && !aOptions['OnFailure'] && !aOptions['OnTimeout']){
				request = {};
			}
			else{
				request = new jws.EventsCallbacksHandler();	
			}
			
			request.args = {
				meta: aOptions,
				filterChain: this.filterChain,
				sentTime: new Date().getTime()
			};
			
			
			if (undefined != aOptions.eventDefinition){
				for (var i = 0; i < this.filterChain.length; i++){
					try {
						this.filterChain[i].beforeCall(lToken, request);
					}
					catch(err) {
						switch (err) {
							case "stop_filter_chain":
								return;
								break;
							default:
								throw err;
								break;
						}
					}
				}
			}
			
			this.jwsClient.sendToken(lToken, request);
		}
		else
			throw "client:not_connected";
	}
	,
	//:m:*:processToken
	//:d:en:Processes an incoming token. Used to support S2C events notifications. _
	//:d:en:Use the "event_name" and "plugin_id" information to execute _
	//:d:en:a targered method in a plug-in.
	//:a:en::aToken:Object:Token to be processed
	//:r:*:::void:none
	processToken: function (aToken) {
		if (this.NS == aToken.ns && "s2c.event_notification" == aToken.type){
			var event_name = aToken.event_name;
			var plugin_id = aToken.plugin_id;

			if (undefined != this.plugIns[plugin_id] && undefined != this.plugIns[plugin_id][event_name]){
				var startTime = new Date().getTime();
				var result = this.plugIns[plugin_id][event_name](aToken);
				var processingTime = (new Date().getTime()) - startTime;

				//Sending response back to the server
				if (aToken.has_callback){
					this.notify("s2c.onresponse", {
						args: {
							req_id: aToken.uid,
							response: result,
							processingTime: processingTime
						}
					});
				}
			}
			else {
				//Sending the "not supported" event notification
				this.notify("s2c.event_not_supported", {
					args: {
						req_id: aToken.uid
					}
				});
				throw "s2c_event_support_not_found:" + event_name;
			}
		}
	}
});

//:package:*:jws
//:class:*:jws.EventsPlugInGenerator
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsPlugInGenerator[/tt] class. _
//:d:en:This class handle the generation of server plug-ins as _
//:d:en:Javascript objects.
jws.oop.declareClass( "jws", "EventsPlugInGenerator", null, {

	//:m:*:generate
	//:d:en:Processes an incoming token. Used to support S2C events notifications. _
	//:a:en::aPlugInId:String:Remote plug-in "id" to generate in the client.
	//:a:en::aNotifier:jws.EventsNotifier:The event notifier used to connect with the server.
	//:a:en::OnReady:Function:This callback is called when the plug-in has been generated.
	//:r:*:::void:none
	generate: function(aPlugInId, aNotifier, OnReady){
		var plugIn = new jws.EventsPlugIn();
		plugIn.notifier = aNotifier;

		aNotifier.notify("plugin.getapi", {
			args: {
				plugin_id: aPlugInId
			}
			,
			plugIn: plugIn
			,
			OnReady: OnReady
			,
			OnSuccess: function(aResponseEvent){
				this.plugIn.id = aResponseEvent.id;
				this.plugIn.plugInAPI = aResponseEvent.api;

				//Generating the plugin methods
				for (method in aResponseEvent.api){
					eval("this.plugIn." + method + "=function(aOptions){if (undefined == aOptions){aOptions = {};};var eventName=this.plugInAPI."+method+".type; aOptions.eventDefinition=this.plugInAPI."+ method + "; aOptions.timeout = this.plugInAPI."+method+".timeout; this.notifier.notify(eventName, aOptions);}")
				}

				//Registering the plugin in the notifier
				this.plugIn.notifier.plugIns[this.plugIn.id] = this.plugIn;

				//Plugin is ready to use
				this.OnReady(this.plugIn);
			}
			,
			OnFailure: function(aResponseEvent){
				throw aResponseEvent.msg;
			}	
		});

		return plugIn;
	}
});

//:package:*:jws
//:class:*:jws.EventsPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsPlugIn[/tt] class. _
//:d:en:This class represents an abstract client plug-in. The methods are _
//:d:en:generated in runtime.
jws.oop.declareClass( "jws", "EventsPlugIn", null, {
	id: ""
	,
	notifier: {}
	,
	plugInAPI: {}
	
//Methods are generated in runtime!
//Custom methods can be added using the OnReady callback
});

//:package:*:jws
//:class:*:jws.AppUser
//:ancestor:*:-
//:d:en:Application user instance.
jws.oop.declareClass( "jws", "AppUser", null, {
	principal: ""
	,
	uuid: ""
	,
	roles: []
	,
	//:m:*:clear
	//:d:en:Clear the user instance
	//:r:*:::void:none
	clear: function (){
		this.principal = "";
		this.roles = [];
		this.uuid = "";
	}
	,
	//:m:*:isAuthenticated
	//:d:en:Returns TRUE if the user is authenticated, FALSE otherwise
	//:r:*:::boolean:none
	isAuthenticated: function(){
		return (this.principal)? true : false
	}
	,
	//:m:*:hasRole
	//:d:en:TRUE if the user have the given role, FALSE otherwise
	//:a:en::r:String:A role
	//:r:*:::boolean:none
	hasRole: function(r){
		var end = this.roles.length;
		
		for (var i = 0; i < end; i++){
			if (r == this.roles[i])
				return true
		}
	
		return false;
	}
});


//:package:*:jws
//:class:*:jws.EventsBaseFilter
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.EventsBaseFilter[/tt] class. _
//:d:en:This class represents an abstract client filter.
jws.oop.declareClass( "jws", "EventsBaseFilter", null, {
	id: ""
	,
	
	//:m:*:initialize
	//:d:en:Initialize the filter instance
	//:a:en::aNotifier:jws.EventsNotifier:The filter notifier
	//:r:*:::void:none
	initialize: function(aNotifier){}
	,

	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aRequest:Object:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aRequest){}
	,
	//:m:*:afterCall
	//:d:en:This method is called after every C2S event notification.
	//:a:en::aRequest:Object:The request to be filtered.
	//:a:en::aResponseEvent:Object:The response token from the server.
	//:r:*:::void:none
	afterCall: function(aRequest, aResponseEvent){}
});

//:package:*:jws
//:class:*:jws.SecurityFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.SecurityFilter[/tt] class. _
//:d:en:This class handle the security for every C2S event notification _
//:d:en:in the client, using the server side security configuration.
jws.oop.declareClass( "jws", "SecurityFilter", jws.EventsBaseFilter, {
	id: "security"
	,
	initialize: function(aNotifier){
		jws.user = new jws.AppUser();
	},
	
	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks that the logged in user has the correct roles to notify _
	//:d:en:a custom event in the server.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aRequest:Object:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aRequest){
		if (aRequest.args.meta.eventDefinition.isSecurityEnabled){
			var r, u;
			var roles, users = null;
			var exclusion = false;
			var role_authorized = false;
			var user_authorized = false;
			var stop = false;
			
			//@TODO: Support IP addresses restrictions checks on the JS client

			//Getting users restrictions
			users = aRequest.args.meta.eventDefinition.users;

			//Getting roles restrictions
			roles = aRequest.args.meta.eventDefinition.roles;
			
			//Avoid unnecessary checks if the user is not authenticated
			if (users && roles && !jws.user.isAuthenticated()){
				if (aRequest.OnResponse){
					aRequest.OnResponse({
						code: -1,
						msg: "User is not authenticated yet!"
					}, aRequest.args);
				}
				this.OnNotAuthorized(aToken);
				throw "stop_filter_chain";
			}

			//Checking if the user have the allowed roles
			if (users.length > 0){
				var user_match = false;
				for (var k = 0; k < users.length; k++){
					u = users[k];
					
					if ("all" != u){
						exclusion = (u.substring(0,1) == "!") ? true : false;
						u = (exclusion) ? u.substring(1) : u;

						if (u == jws.user.principal){
							user_match = true;
							if (!exclusion){
								user_authorized = true;
							}
							break;
						}
					} else {
						user_match = true;
						user_authorized = true;
						break;
					}
				}

				//Not Authorized USER
				if (!user_authorized && user_match || 0 == roles.length){
					aRequest.OnResponse({
						code: -1,
						msg: "Not autorized to notify this event. USER restrictions: " + users.toString()
					}, aRequest.args);
					
					this.OnNotAuthorized(aToken);
					throw "stop_filter_chain";
				}
			}

			//Checking if the user have the allowed roles
			if (roles.length > 0){
				for (var i = 0; i < roles.length; i++){
					for (var j = 0; j < jws.user.roles.length; j++){
						r = roles[i];
					
						if ("all" != r){
							exclusion = (r.substring(0,1) == "!") ? true : false;
							r = (exclusion) ? r.substring(1) : r;

							if (r == jws.user.roles[j]){
								if (!exclusion){
									role_authorized = true;
								}
								stop = true;
								break;
							}
						} else {
							role_authorized = true;
							stop = true;
							break;
						}	
					}
					if (stop){
						break;
					}
				}

				//Not Authorized ROLE
				if (!role_authorized){
					if (aRequest.OnResponse){
						aRequest.OnResponse({
							code: -1,
							msg: "Not autorized to notify this event. ROLE restrictions: " + roles.toString()
						}, aRequest.args);
					}
					this.OnNotAuthorized(aToken);
					throw "stop_filter_chain";
				}
			}
		}
	}
	,
	//:m:*:OnNotAuthorized
	//:d:en:This method is called when a "not authorized" event notification _
	//:d:en:is detected. Allows to define a global behiavor for this kind _
	//:d:en:of exception.
	//:a:en::aToken:Object:The "not authorized" token to be processed.
	//:r:*:::void:none
	OnNotAuthorized: function(aToken){
		throw "not_authorized";
	}
});

//:package:*:jws
//:class:*:jws.CacheFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.CacheFilter[/tt] class. _
//:d:en:This class handle the cache for every C2S event notification _
//:d:en:in the client, using the server side cache configuration.
jws.oop.declareClass( "jws", "CacheFilter", jws.EventsBaseFilter, {
	id: "cache"
	,
	cache:{}
	,
	initialize: function(notifier){
		notifier.notify("clientcacheaspect.setstatus", {
			args: {
				enabled: true
			}
		});
		
		notifier.plugIns['__cache__'] = {
			cache: this.cache,
			cleanEntries: function(event){
				for (var i = 0, end = event.entries.length; i < end; i++){
					this.cache.removeItem_(jws.user.principal.toString() + event.suffix + event.entries[i]);
				}
			}
		}
	}
	,
	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks if exist a non-expired cached response for the outgoing event. _
	//:d:en:If TRUE, the cached response is used and the server is not notified.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aRequest:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aRequest){
		if (aRequest.args.meta.eventDefinition.isCacheEnabled){
			var key = aRequest.args.meta.eventDefinition.type + aRequest.args.meta.UTID;
			
			//Storing in the user private cache storage if required
			if (aRequest.args.meta.eventDefinition.isCachePrivate && jws.user.isAuthenticated()){
				key = jws.user.uuid + key;
			}
			
			var cachedResponseEvent = this.cache.getItem(key);

			if (null != cachedResponseEvent){
				//Setting the processing time of the cached response to 0
				cachedResponseEvent.processingTime = 0;
				
				//Updating the elapsed time
				aRequest.args.meta.elapsedTime = (new Date().getTime()) - aRequest.sentTime;
				
				//Calling the OnResponse callback
				if (aRequest.OnResponse){
					aRequest.OnResponse(cachedResponseEvent, aRequest.args);
				}
				
				throw "stop_filter_chain";
			}
		}
	}
	,
	//:m:*:afterCall
	//:d:en:This method is called after every C2S event notification. _
	//:d:en:Checks if a response needs to be cached. The server configuration _
	//:d:en:for cache used.
	//:a:en::aRequest:Object:The request to be filtered.
	//:a:en::aResponseEvent:Object:The response token from the server.
	//:r:*:::void:none
	afterCall: function(aRequest, aResponseEvent){
		if (aRequest.eventDefinition.isCacheEnabled){
			var key = aRequest.eventDefinition.type 
			+ aRequest.UTID;

			//Storing in the user private cache storage if required
			if (aRequest.eventDefinition.isCachePrivate){
				key = jws.user.uuid + key;
			}
			
			this.cache.setItem(key, aResponseEvent, {
				expirationAbsolute: null,
				expirationSliding: aRequest.eventDefinition.cacheTime,
				priority: CachePriority.High
			});
		}
	}
});

//:package:*:jws
//:class:*:jws.ValidatorFilter
//:ancestor:*:jws.EventsBaseFilter
//:d:en:Implementation of the [tt]jws.ValidatorFilter[/tt] class. _
//:d:en:This class handle the validation for every argument in the request.
jws.oop.declareClass( "jws", "ValidatorFilter", jws.EventsBaseFilter, {
	id: "validator"
	,
	
	//:m:*:beforeCall
	//:d:en:This method is called before every C2S event notification. _
	//:d:en:Checks if the request arguments match with the validation server rules.
	//:a:en::aToken:Object:The token to be filtered.
	//:a:en::aRequest:jws.OnResponseObject:The OnResponse callback to be called.
	//:r:*:::void:none
	beforeCall: function(aToken, aRequest){
		var arguments = aRequest.args.meta.eventDefinition.incomingArgsValidation;
		
		for (var index = 0; index < arguments.length; index++){
			if (undefined === aToken[arguments[index].name] && !arguments[index].optional){
				if (aRequest.OnResponse){
					aRequest.OnResponse({
						code: -1,
						msg: "Argument '"+arguments[index].name+"' is required!"
					}, aRequest.args);
				}
				throw "stop_filter_chain";
			}else if (aToken.hasOwnProperty(arguments[index].name)){
				var requiredType = arguments[index].type;
				if (requiredType != jws.tools.getType(aToken[arguments[index].name])){
					if (aRequest.OnResponse){
						aRequest.OnResponse({
							code: -1,
							msg: "Argument '"+arguments[index].name+"' has invalid type. Required: '"+requiredType+"'"
						}, aRequest.args);
					}
					throw "stop_filter_chain";
				}
			}
		}
	}
});


