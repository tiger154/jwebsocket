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
		var lResponse = {};
		
		it(lSpec, function() {
			
			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					lResponse = aResponse;
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
	testRegisterServiceEndPoint1: function() {
		var lSpec = "Register service endpoint( valid password, valid clusterAlias, valid clusterNS )";
		var lResponse = {};
		var lFailure = false;
		
		it(lSpec, function(){
		
			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					var lClusterInfoValues = aResponse.data;
					
					for(var lPos=0; lPos < lClusterInfoValues.length; lPos++){
						
						// perform the create sample service on the server
						// with valid credential and valid arguments	
						jws.Tests.getAdminTestConn().lbSampleService("admin", {
							clusterAlias: lClusterInfoValues[lPos].clusterAlias,
							nameSpace: lClusterInfoValues[lPos].clusterNS,
							OnSuccess: function(aResponse) {
								lResponse = aResponse;	
							},
							OnFailure: function(aResponse){
								lResponse = aResponse;
								lFailure = true;
							}
						});	
					}
					
					if(lClusterInfoValues.length == 0 || lFailure == true){
						lResponse = {
							code: -1,
							msg: 'failure'
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
					5000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual('ok');
			});
		});
	},
	
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint2: function() {
		var lSpec = "Register service endpoint( invalid password, valid clusterAlias, valid clusterNS )";
		var lResponse = {};
		
		it(lSpec, function(){
		
			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					var lClusterInfoValues = aResponse.data;
					if(lClusterInfoValues.length > 0){
					
						// perform the create sample service on the server
						// with invalid credential and valid arguments
						jws.Tests.getAdminTestConn().lbSampleService("wrongUser", {
							clusterAlias: lClusterInfoValues[0].clusterAlias,
							nameSpace: lClusterInfoValues[0].clusterNS,
							OnSuccess: function(aResponse) {
								lResponse = aResponse;	
							},
							OnFailure: function(aResponse){
								lResponse = aResponse;
							}
						});	
					}else{
						lResponse = {
							code: -1,
							msg: 'failure'
						}						
					}
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint3: function() {
		var lSpec = "Register service endpoint( valid password, invalid clusterAlias, invalid clusterNS )";
		var lResponse = {};
		
		it(lSpec, function(){
		
			// perform the create sample service on the server
			// with valid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbSampleService("admin", {
				clusterAlias: 'wrongClusterAlias',
				nameSpace: 'wrongClusterNS',
				OnSuccess: function(aResponse) {
					lResponse = aResponse;	
				},
				OnFailure: function(aResponse){
					lResponse = aResponse;
				}
			});		
		
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint4: function() {
		var lSpec = "Register service endpoint( invalid password, invalid clusterAlias, invalid clusterNS )";
		var lResponse = {};
		
		it(lSpec, function(){
		
			// perform the create sample service on the server
			// with invalid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbSampleService("wrongUser", {
				clusterAlias: 'wrongClusterAlias',
				nameSpace: 'wrongClusterNS',
				OnSuccess: function(aResponse) {
					lResponse = aResponse;	
				},
				OnFailure: function(aResponse){
					lResponse = aResponse;
				}
			});		
		
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the change algorithm feature
	testChangeAlgorithm1: function() {
		var lSpec = "Change Algorithm ( valid argument )" ;
		var lResponse = {
			code: -1,
			msg: 'failure'
		};
		
		it(lSpec, function() {

			// perform the change algorithm  on the server
			jws.Tests.getAdminTestConn().lbChangeAlgorithm({
				algorithm: 1,
				OnResponse: function(aResponse) {
					if(aResponse.code == 0){
						this.lbChangeAlgorithm({
							algorithm: 2,
							OnResponse: function(aResponse) {
								if(aResponse.code == 0){
									this.lbChangeAlgorithm({
										algorithm: 3,
										OnResponse: function(aResponse) {
											lResponse = aResponse;
										}
									});
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
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual('ok');
			});
		});
	},
	
	// this spec tests the change algorithm feature
	testChangeAlgorithm2: function() {
		var lSpec = "Change Algorithm ( invalid argument )" ;
		var lResponse = {
			code: 0,
			msg: 'ok'
		};
		
		it(lSpec, function() {

			// perform the change algorithm  on the server
			jws.Tests.getAdminTestConn().lbChangeAlgorithm({
				algorithm: 4,
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the sticky routes feature
	testStickyRoutes: function( ) {
		var lSpec = "Sticky routes ()";
		var lResponse = {};

		it(lSpec, function() {
			
			// perform the sticky routes feature on the server
			jws.Tests.getAdminTestConn().lbStickyRoutes({
				OnResponse: function(aResponse) {
					lResponse = aResponse;
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
		var lResponse = {};

		it(lSpec, function() {
			
			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					var lClusterInfoValues = aResponse.data;
					
					for(var lPos=0; lPos < lClusterInfoValues.length; lPos++){
						jws.Tests.getAdminTestConn().sendToken({
							ns: lClusterInfoValues[lPos].clusterNS,
							type: 'test'
						}, {
							OnResponse:function(aResponse){
								lResponse = aResponse;
							}
						});
					}
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.type == 'response');
					},
					lSpec,
					2000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.type).toEqual('response');
				expect(lResponse.reqType).toEqual('test');
			});
		});
	},
	
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint1: function() {
		var lSpec = "Shutdown service ( valid password, valid endPointId, valid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					var lClusterInfoValues = aResponse.data;
					
					// perform the create sample service on the server
					// with valid credential and valid arguments
					jws.Tests.getAdminTestConn().lbSampleService("admin", {
						clusterAlias: lClusterInfoValues[0].clusterAlias,
						nameSpace: lClusterInfoValues[0].clusterNS,
						OnSuccess: function(aResponse) {
								
							// perform the shutdown feature an specific service endpoint
							// with valid credential and valid arguments
							jws.Tests.getAdminTestConn().lbShutdownEndPoint( "admin", {
								endPointId: aResponse.endPointId,
								clusterAlias: lClusterInfoValues[0].clusterAlias,
								OnResponse: function(aResponse) {
									lResponse = aResponse;
								}
							});
						}
					});	
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == 0);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual('ok');
			});
		});
	},
	
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint2: function() {
		var lSpec = "Shutdown service ( invalid password, valid endPointId, valid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					var lClusterInfoValues = aResponse.data;
					
					// perform the create sample service on the server
					// with valid credential and valid arguments
					jws.Tests.getAdminTestConn().lbSampleService("admin", {
						clusterAlias: lClusterInfoValues[0].clusterAlias,
						nameSpace: lClusterInfoValues[0].clusterNS,
						OnSuccess: function(aResponse) {
								
							// perform the shutdown feature an specific service endpoint
							// with invalid credential and valid arguments
							jws.Tests.getAdminTestConn().lbShutdownEndPoint( "wrongUser", {
								endPointId: aResponse.endPointId,
								clusterAlias: lClusterInfoValues[0].clusterAlias,
								OnResponse: function(aResponse) {
									lResponse = aResponse;
								}
							});
						}
					});	
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint3: function() {
		var lSpec = "Shutdown service ( valid password, invalid endPointId, invalid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {
					
			// perform the shutdown feature an specific service endpoint
			// with valid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbShutdownEndPoint( "admin", {
				endPointId: 'wrongEndPointId',
				clusterAlias: 'wrongClusterAlias',
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint4: function() {
		var lSpec = "Shutdown service ( invalid password, invalid endPointId, invalid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {
					
			// perform the shutdown feature an specific service endpoint
			// with invalid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbShutdownEndPoint( "wrongUser", {
				endPointId: 'wrongEndPointId',
				clusterAlias: 'wrongClusterAlias',
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint1: function() {
		var lSpec = "Deregister service ( valid password, valid endPointId, valid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					var lClusterInfoValues = aResponse.data;
					
					// perform the create sample service on the server
					// with valid credential and valid arguments
					jws.Tests.getAdminTestConn().lbSampleService("admin", {
						clusterAlias: lClusterInfoValues[0].clusterAlias,
						nameSpace: lClusterInfoValues[0].clusterNS,
						OnSuccess: function(aResponse) {
								
							// perform the deregister feature an specific service endpoint
							// with valid credential and valid arguments
							jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint( "admin", {
								endPointId: aResponse.endPointId,
								clusterAlias: lClusterInfoValues[0].clusterAlias,
								OnResponse: function(aResponse) {
									lResponse = aResponse;
								}
							});
						}
					});	
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == 0);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual('ok');
			});
		});
	},
	
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint2: function() {
		var lSpec = "Deregister service ( invalid password, valid endPointId, valid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function(aResponse) {
					var lClusterInfoValues = aResponse.data;
					
					// perform the create sample service on the server
					// with valid credential and valid arguments
					jws.Tests.getAdminTestConn().lbSampleService("admin", {
						clusterAlias: lClusterInfoValues[0].clusterAlias,
						nameSpace: lClusterInfoValues[0].clusterNS,
						OnSuccess: function(aResponse) {
								
							// perform the deregister feature an specific service endpoint
							// with invalid credential and valid arguments
							jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint( "wrongUser", {
								endPointId: aResponse.endPointId,
								clusterAlias: lClusterInfoValues[0].clusterAlias,
								OnResponse: function(aResponse) {
									lResponse = aResponse;
								}
							});
						}
					});	
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint3: function() {
		var lSpec = "Deregister service ( valid password, invalid endPointId, invalid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {
					
			// perform the shutdown feature an specific service endpoint
			// with valid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint( "admin", {
				endPointId: 'wrongEndPointId',
				clusterAlias: 'wrongClusterAlias',
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint4: function() {
		var lSpec = "Deregister service ( invalid password, invalid endPointId, invalid service )" ;
		var lResponse = {};
		
		it(lSpec, function() {
					
			// perform the deregister feature an specific service endpoint
			// with invalid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint( "wrongUser", {
				endPointId: 'wrongEndPointId',
				clusterAlias: 'wrongClusterAlias',
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});
			
			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						return(lResponse.code == -1);
					},
					lSpec,
					3000
					);
					
			// check the result
			runs(function() {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	
	runSpecs: function() {
	
		//run alls tests
		this.testClustersInfo();
		
		this.testRegisterServiceEndPoint1();
		this.testRegisterServiceEndPoint2();
		this.testRegisterServiceEndPoint3();
		this.testRegisterServiceEndPoint4();
		
		this.testStickyRoutes();
		
		this.testChangeAlgorithm1();
		this.testChangeAlgorithm2();
		
		this.testServices();
		
		this.testShutdownEndPoint1();
		this.testShutdownEndPoint2();
		this.testShutdownEndPoint3();
		this.testShutdownEndPoint4();
		
		this.testDeregisterServiceEndPoint1();
		this.testDeregisterServiceEndPoint2();
		this.testDeregisterServiceEndPoint3();
		this.testDeregisterServiceEndPoint4();
	}
};
