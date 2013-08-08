//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
	
	lbClustersInfo: function ( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 === lRes.code ) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "clustersInfo"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},
	
	lbStickyRoutes: function ( aOptions ) {
		var lRes = this.checkConnected();
		if( 0 === lRes.code ) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "stickyRoutes"
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},
	
	lbRegisterServiceEndPoint: function (aClusterAlias, aPassword, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 === lRes.code ) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "registerServiceEndPoint",
				clusterAlias: aClusterAlias,
				password: aPassword
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},
	
	lbDeregisterServiceEndPoint: function ( aClusterAlias, aEndPointId, aPassword, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 === lRes.code ) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "deregisterServiceEndPoint",
				endPointId: aEndPointId,
				clusterAlias: aClusterAlias,
				password: aPassword
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},
	
	lbShutdownEndPoint: function ( aClusterAlias, aEndPointId, aPassword, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 === lRes.code ) {
			var lToken = {
				ns: jws.LoadBalancerPlugIn.NS,
				type: "shutdownServiceEndPoint",
				endPointId: aEndPointId,
				clusterAlias: aClusterAlias,
				password: aPassword
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},
	lbCreateResponse: function(aToken){
		var lResponse =  {
			ns: jws.LoadBalancerPlugIn.NS,
			type: 'response',
			utid: aToken.utid,
			sourceId: aToken.sourceId,
			reqType: aToken.type
		}
		
		return lResponse;
	}
};

// add the JWebSocket Load Balancer PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.LoadBalancerPlugIn );	
	
	
