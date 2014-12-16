//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
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
//:class:*:jws.LoadBalancerPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.LoadBalancerPlugIn[/tt] class.
//:d:en:This client-side plug-in provides the API to access the features of the _
//:d:en:Load Balancer plug-in on the jWebSocket server.
jws.LoadBalancerPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.loadbalancer (jws.NS_BASE + ".plugins.loadbalancer")
	//:d:en:Namespace for the [tt]LoadBalancerPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.loadbalancer",
	
	//:m:*:lbClustersInfo
	//:d:en:Gets a list (of maps) with the information about all clusters.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	lbClustersInfo: function(aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "clustersInfo"
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	//:m:*:lbStickyRoutes
	//:d:en:Gets a list of all sticky routes managed by the load balancer.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	lbStickyRoutes: function(aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "stickyRoutes"
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	//:m:*:lbChangeAlgorithm
	//:d:en:Changes the type of algorithm used by the load balancer.
	//:a:en::aAlgorithm:Integer:The balancer algorithm to be set.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	lbChangeAlgorithm: function(aAlgorithm, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "changeAlgorithm",
				algorithm: aAlgorithm
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	//:m:*:lbRegisterServiceEndPoint
	//:d:en:Registers a new service endpoint in specific cluster.
	//:a:en::aClusterAlias:String:The cluster alias value.
	//:a:en::aPassword:String:Password to verify privileges.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	lbRegisterServiceEndPoint: function(aClusterAlias, aPassword, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "registerServiceEndPoint",
				clusterAlias: aClusterAlias,
				password: aPassword
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	//:m:*:lbDeregisterServiceEndPoint
	//:d:en:De-registers a connected service endpoint.
	//:a:en::aClusterAlias:String:The cluster alias that contains the service to be deregistered.
	//:a:en::aPassword:String:The cluster password.
	//:a:en::aEndPointId:String:The endpoint to be deregistered.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	lbDeregisterServiceEndPoint: function(aClusterAlias, aPassword, aEndPointId, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "deregisterServiceEndPoint",
				endPointId: aEndPointId,
				clusterAlias: aClusterAlias,
				password: aPassword
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	//:m:*:lbShutdownEndPoint
	//:d:en:Should send a message to the referenced endpoint to gracefully shutdown.
	//:a:en::aClusterAlias:String:The cluster alias that contains the service to be shutdown.
	//:a:en::aPassword:String:The cluster password.
	//:a:en::aEndPointId:String:The endpoint to be shutdown.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	lbShutdownEndPoint: function(aClusterAlias, aPassword, aEndPointId, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "shutdownServiceEndPoint",
				endPointId: aEndPointId,
				clusterAlias: aClusterAlias,
				password: aPassword
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	
	//:m:*:lbCreateResponse
	//:d:en:Create token response with all necessary data to send to remote client.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	lbCreateResponse: function(aToken) {
		var lResponse = {
			ns: jws.LoadBalancerPlugIn.NS,
			type: 'response',
			utid: aToken.utid,
			sourceId: aToken.sourceId,
			reqType: aToken.type
		}

		return lResponse;
	},
	
	//:m:*:lbSampleService
	//:d:en:Create a new sample service endpoint.
	//:a:en::aClusterAlias:String:The cluster alias value.
	//:a:en::aPassword:String:The cluster password.
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:a:en::aOptions.connectionURL:String:Optional argument to override the default service connection URL.
	//:a:en::aOptions.connectionUsername:String:Optional argument that indicates the server connection username. Default: root
	//:a:en::aOptions.connectionPassword:String:Optional argument that indicates the server connection password. Default: root
	//:r:*:::jWebSocketJSONClient:The sample service endpoint instance
	lbSampleService: function(aClusterAlias, aPassword, aOptions) {
		var lWSC = new jws.jWebSocketJSONClient();
		var lURL = aOptions.connectionURL ||
		"ws://localhost:8787/jWebSocket/jWebSocket?sessionCookieName=sSessionId" + new Date().getTime();
		
		lWSC.open(lURL, {
			OnWelcome: function() {
				if(lWSC.isLoggedIn() != "root"){
					lWSC.login(aOptions.connectionUsername || "root", aOptions.connectionPassword || "root"); 
				}
					
				lWSC.lbRegisterServiceEndPoint(aClusterAlias, aPassword, aOptions);
				lWSC.addPlugIn({
					processToken: function(aToken) {
						if (aToken.ns == aOptions.nameSpace) {
							if ('test' == aToken.type) {
								var lResponse = lWSC.lbCreateResponse(aToken);
								lWSC.sendToken(lResponse);
							}
						}

						if (aToken.ns == jws.LoadBalancerPlugIn.NS) {
							if ('shutdown' == aToken.type) {
								lWSC.close();
							}
						}
					}
				});
			}, 
			
			OnMessage: function(aMessage) {
				if ('function' == typeof log){
					log('Message "' + aMessage.data + '" received on endpoint: ' 
						+ (lWSC.getId() == null 
							? aMessage.data.split('"')[11] 
							: lWSC.getId()));
				}
			}
		});
		
		return lWSC;
	}
};

// add the JWebSocket Load Balancer PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.LoadBalancerPlugIn);


