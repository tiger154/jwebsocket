//	---------------------------------------------------------------------------
//	jWebSocket Reporting Plug-in test specs (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
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


// requires web/res/js/jwsLoadBalancerPlugIn.js previously loaded
jws.tests.LoadBalancer = {
	title: "Load balancer plug-in",
	description: "jWebSocket load balancer plug-in for balance and manage the load in the jWebSocket server",
	category: "Community Edition",
	
	// this spec tests the clusters information feature	
	testClustersInfo: function() {
		var lSpec = "Clusters Information ()";
		
		it(lSpec, function() {
			var lResponse = {};
			
			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == 0);
					},
					lSpec,
					2000
					);
					
			// check the result 
			runs(function() {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint: function() {
		var lSpec = "Register service endpoint with valid and invalid arguments ( password, clusterAlias)";
	
		it(lSpec, function(){
			var lResponse = {};
			
			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aToken) {
					var lClusterInfoValues = aToken.data;
					
					// check if the response contains data
					if(lClusterInfoValues.length != 0){
						lResponse = {
							code: 0,
							msg: 'ok'
						};
					
						for(var lPos=0; lPos < lClusterInfoValues.length; lPos++){
						
							// perform the create sample service on the server
							// with valid credential and valid arguments	
							jws.Tests.getAdminTestConn().lbSampleService("admin", {
								clusterAlias: lClusterInfoValues[lPos].clusterAlias,
								nameSpace: lClusterInfoValues[lPos].clusterNS,
								OnResponse: function(aToken) {
									if(aToken.code == -1){
										lResponse = {
											code: -1,
											msg: 'failure'
										};
										return;
									}
								}
							});		
						}
					
						// perform the create five sample service on the server
						// with valid credential and valid arguments
						jws.Tests.getAdminTestConn().lbSampleService("admin", {
							clusterAlias: lClusterInfoValues[0].clusterAlias,
							nameSpace: lClusterInfoValues[0].clusterNS,
							OnResponse: function(aToken) {
								if(aToken.code == -1){
									lResponse = {
										code: -1,
										msg: 'failure'
									};
									return;
								}
							}
						});		
					
						// perform the create five sample service on the server
						// with invalid credential and valid arguments
						jws.Tests.getAdminTestConn().lbSampleService("noAdmin", {
							clusterAlias: lClusterInfoValues[0].clusterAlias,
							nameSpace: lClusterInfoValues[0].clusterNS,
							OnResponse: function(aToken) {
								if(aToken.code == 0){
									lResponse = {
										code: -1,
										msg: 'failure'
									};
									return;
								}
							}
						});		
					
						// perform the create five sample service on the server
						// with valid credential and invalid arguments.
						jws.Tests.getAdminTestConn().lbSampleService("admin", {
							clusterAlias: 'serviceWrong',
							nameSpace: lClusterInfoValues[0].clusterNS,
							OnResponse: function(aToken) {
								if(aToken.code == 0){
									lResponse = {
										code: -1,
										msg: 'failure'
									};
									return;
								}
							}
						});
					}
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == 0);
					},
					lSpec,
					5000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual('ok');
			});
		});
	},
	
	// this spec tests the change algorithm feature
	testChangeAlgorithm: function(aValue, aDescription) {
		var lSpec = "Change Algorithm ( " + aValue + " ), " + aDescription;

		it(lSpec, function() {
			var lResponse = {};

			// perform the change algorithm  on the server
			jws.Tests.getAdminTestConn().lbChangeAlgorithm({
				algorithm: aValue,
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						if (aValue > 0 && aValue < 4) {
							return(lResponse.code == 0);
						} else {
							return(lResponse.code == -1);
						}
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				if (aValue > 0 && aValue < 4) {
					expect(lResponse.code).toEqual(0);
				} else {
					expect(lResponse.code).toEqual(-1);
				}
			});
		});
	},
	
	// this spec tests the sticky routes feature
	testStickyRoutes: function( ) {
		var lSpec = "Sticky routes ()";

		it(lSpec, function() {
			var lResponse = {};

			// perform the sticky routes feature on the server
			jws.Tests.getAdminTestConn().lbStickyRoutes({
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == 0);
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	
	// this spec tests the test services feature
	testServices: function( ) {
		var lSpec = "Testing services()";

		it(lSpec, function() {
			var lResponse = {};
			
			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aToken) {
					var lClusterInfoValues = aToken.data;
					
					// check if the response contains data
					if(lClusterInfoValues.length != 0){
						lResponse = {
							code: 0,
							msg: 'ok'
						};
					
						for(var lPos=0; lPos < lClusterInfoValues.length; lPos++){
							jws.Tests.getAdminTestConn().sendToken({
								ns: lClusterInfoValues[lPos].clusterNS,
								type: 'test',
							}, {
								OnResponse: function(aToken) {
									if(aToken.code == -1){
										lResponse = {
											code: -1,
											msg: 'failure'
										};
										return;
									}
								}
							});
						}
					}
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == 0);
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual('ok');
			});
		});
	},
	
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint: function(aPassword, aArguments, aValue, aDescription) {
		var lSpec = "Shutdown service (" + aPassword + ", endPointId, service), " + aDescription ;

		it(lSpec, function() {
			var lResponse = {};

			// perform the sticky routes feature on the server
			jws.Tests.getAdminTestConn().lbStickyRoutes({
				OnResponse: function(aToken) {
					if(aArguments == "valid")
						aArguments = aToken.data;
					
					// perform the shutdown feature an specific service endpoint
					jws.Tests.getAdminTestConn().lbShutdownEndPoint(
						aPassword, {
							endPointId: aArguments[0].endPointId,
							clusterAlias: aArguments[0].clusterAlias,
							OnResponse: function(aToken) {
								lResponse = aToken;
							}
					});
				}
			});	
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == aValue);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(aValue);
			});
		});
	},
	
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint: function(aPassword, aArguments, aValue, aDescription) {
		var lSpec = "Deregister service (" + aPassword + ", endPointId, service1), " + aDescription;

		it(lSpec, function() {
			var lResponse = {};

			// perform the sticky routes feature on the server
			jws.Tests.getAdminTestConn().lbStickyRoutes({
				OnResponse: function(aToken) {
					if(aArguments == "valid")
						aArguments = aToken.data;
						
					// perform the deregister feature an specific service endpoint	
					jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
						aPassword, {
							endPointId: aArguments[0].endPointId,
							clusterAlias: aArguments[0].clusterAlias,
							OnResponse: function(aToken) {
								lResponse = aToken;
							}
						}
					);
				}		
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == aValue);
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(aValue);
			});
		});
	},
	
	runSpecs: function() {
	
		//run alls tests
		this.testClustersInfo();
		
		this.testRegisterServiceEndPoint();
		
		this.testStickyRoutes();
		
		this.testChangeAlgorithm(1, "valid argument");
		this.testChangeAlgorithm(2, "valid argument");
		this.testChangeAlgorithm(3, "valid argument");
		this.testChangeAlgorithm(4, "invalid argument");
		
		this.testServices();
		
		this.testShutdownEndPoint("admin", "valid", 0, "whit valid credential and arguments.");
		this.testShutdownEndPoint("noAdmin", "valid", -1, "whit invalid credential and valid arguments.");
		this.testShutdownEndPoint("admin", "invalid", -1, "whit valid credential and invalid arguments.");
		
		this.testDeregisterServiceEndPoint("admin", "valid", 0, "whit valid credential and arguments.");
		this.testDeregisterServiceEndPoint("noAdmin", "valid", -1, "whit invalid credential and valid arguments.");
		this.testDeregisterServiceEndPoint("admin",  "invalid", -1, "whit valid credential and invalid arguments.");
	}
};
