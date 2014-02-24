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
	
	testRegisterServiceEndPoint : function( aPassword, aClusterAlias, aCheckValue, aDescription) {
		var lSpec = "Register service endpoint (" + aPassword + ", "+ aClusterAlias + "), " + aDescription;
		
		it( lSpec, function () {
			var lResponse = {};	
	
			var lServiceEndPoint = new jws.jWebSocketJSONClient();
			lServiceEndPoint.open("ws://localhost:8787/jWebSocket/jWebSocket", {
				
				OnWelcome: function (){
					lServiceEndPoint.lbRegisterServiceEndPoint( aPassword, {
							clusterAlias: aClusterAlias,
							
							OnResponse: function( aToken ) {
								lResponse = aToken;
							}
					});
							
					lServiceEndPoint.addPlugIn({
						processToken: function(aToken){
							if (aToken.ns == testSumNS){
								if ('sumXY' == aToken.type){		
								
									var lResponseSum = lServiceEndPoint.lbCreateResponse(aToken);
									lResponseSum.data = parseFloat(aToken.x) + parseFloat(aToken.y);						
									lServiceEndPoint.sendToken(lResponseSum);
								} 
							} else if (aToken.ns == testMulNS){
								if ('mulXY' == aToken.type){
								
									var lResponseMul = lServiceEndPoint.lbCreateResponse(aToken);
									lResponseMul.data = parseFloat(aToken.x) * parseFloat(aToken.y);
									lServiceEndPoint.sendToken(lResponseMul);
								} 
							}
						}
					});
				}
			});		
			
			waitsFor(
				function() {
					return( lResponse.code == aCheckValue );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( aCheckValue );
			});
		});
	},
	
	testServices1 : function( ) {
		var lSpec = "Testing service1, sum two numbers ( 3 , 7 )";
		
		it( lSpec, function () {
			var lResponse = {};	
			
			jws.Tests.getAdminTestConn().sendToken({
					ns: testSumNS,
					type: 'sumXY',
					x : 3,
					y : 7,			
				},{
					OnResponse: function( aToken ) {
						lResponse = aToken;			
					}
				}		
			);

			waitsFor(
				function() {
					return( lResponse.type == "response" );
				},
				lSpec,
				5000
			);

			runs( function() {
				expect( lResponse.data ).toEqual( 10 );
			});
		});
	},
	
	testServices2 : function( ) {
		var lSpec = "Testing service2, mul two numbers ( 4 , 5 )";
		
		it( lSpec, function () {
			var lResponse = {};	
			
			jws.Tests.getAdminTestConn().sendToken({
					ns: testMulNS,
					type: 'mulXY',
					x : 4,
					y : 5,			
				},{
					OnResponse: function( aToken ) {
						lResponse = aToken;			
					}
				}		
			);

			waitsFor(
				function() {
					return( lResponse.type == "response" );
				},
				lSpec,
				5000
			);

			runs( function() {
				expect( lResponse.data ).toEqual( 20 );
			});
		});
	},
	
	testChangeAlgorithm : function ( aValue, aDescription ) {
		var lSpec = "Change Algorithm ( "+ aValue +" ), "+ aDescription;
		
		it( lSpec, function () {
			var lResponse = {};	
			
			jws.Tests.getAdminTestConn().lbChangeAlgorithm({
					algorithm: aValue,
					OnResponse: function( aToken ) {	
						lResponse = aToken;			
					}
				}		
			);
			
			waitsFor(
				function() {
					if( aValue > 0 && aValue < 4){
						return( lResponse.code == 0 );
					} else {
						return( lResponse.code == -1 );
					}
				},
				lSpec,
				2000
			);

			runs( function() {
				if( aValue > 0 && aValue < 4){
						expect( lResponse.currentAlgorithm ).toEqual( aValue );
					} else {
						expect(lResponse.code == -1);
					}	
			});
		});
	},
	
	testClustersInfo : function () {
		var lSpec = "Clusters Information ()";
		
		it( lSpec, function () {
			var lResponse = {};	
			
			jws.Tests.getAdminTestConn().lbClustersInfo({	
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);

			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},
	
	testStickyRoutes : function( ){
		var lSpec = "Sticky routes ()";
		
		it( lSpec, function () {
			var lResponse = {};	
			
			jws.Tests.getAdminTestConn().lbStickyRoutes({	
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});
		});
	},
	
	testDeregisterServiceEndPoint : function ( aPassword ) {
		var lSpec = "Deregister service ("+ aPassword + ", endPointId, service1), whit valid credential and arguments.";
		
		it( lSpec, function () {
			var lResponse = {};	
		
			jws.Tests.getAdminTestConn().lbStickyRoutes({	
					OnResponse: function( aToken ) {
			
						jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
							aPassword,{
								endPointId: aToken.data[0].endPointId,
								clusterAlias: aToken.data[0].clusterAlias,
								
								OnResponse: function( aToken ) {	
									lResponse = aToken;
								}
							}		
						);
					}
				}
			);	
			
			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				2000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});
		}); 
	},
	
	testDeregisterServiceEndPointFailCredential : function ( aPassword ) {
		var lSpec = "Deregister service ("+ aPassword + ", endPointId, service1), whit invalid credential and valid arguments.";
		
		it( lSpec, function () {
			var lResponse = {};	
		
				jws.Tests.getAdminTestConn().lbStickyRoutes({
					OnResponse: function( aToken ) {
				
						jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
							aPassword,
							{	endPointId: aToken.data[0].endPointId,
								clusterAlias: aToken.data[0].clusterAlias,
								
								OnResponse: function( aToken ) {	
									lResponse = aToken;
								}
							}		
						);
					}
				}
			);	
			
			waitsFor(
				function() {
					return( lResponse.code == -1 );
				},
				lSpec,
				2000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( -1 );
			});
		}); 
	},
	
	testDeregisterServiceEndPointFailArguments : function ( aPassword ) {
		var lSpec = "Deregister service ("+ aPassword + ", endPointIdWrong, service3), whit valid credential and invalid arguments.";
		
		it( lSpec, function () {
			var lResponse = {};	
		
			jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
				aPassword,
				{	endPointId: "wrongId",
					clusterAlias: "service3",
					OnResponse: function( aToken ) {	
						lResponse = aToken;
					}
				}		
			);
						
			waitsFor(
				function() {
					return( lResponse.code == -1 );
				},
				lSpec,
				2000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( -1 );
			});
		}); 
	},
	
	testShutdownEndPoint : function ( aPassword ) {
		var lSpec = "Shutdown service ("+ aPassword + ", endPointId, service2), whit valid credential and arguments";
		
		it( lSpec, function () {
			var lResponse = {};	
			
			jws.Tests.getAdminTestConn().lbStickyRoutes({
				OnResponse: function( aToken ) {
					
					jws.Tests.getAdminTestConn().lbShutdownEndPoint(
						aPassword,{
							endPointId: aToken.data[0].endPointId,
							clusterAlias: aToken.data[0].clusterAlias,
							OnResponse: function( aToken ) {	
								lResponse = aToken;
							}
						}		
					);			
				}
			});
				
			waitsFor(
				function() {
					return( lResponse.code == 0 );
				},
				lSpec,
				2000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});
		}); 
	},
	
	testShutdownEndPointFailCredential : function ( aPassword ) {
		var lSpec = "Shutdown service ("+ aPassword + ", endPointId, service2), whit invalid credential and valid arguments";
		
		it( lSpec, function () {
			var lResponse = {};	
			
			jws.Tests.getAdminTestConn().lbShutdownEndPoint(
				aPassword,
				{	endPointId: "endPointId",
					clusterAlias: "service2",
					OnResponse: function( aToken ) {	
						lResponse = aToken;
					}
				}			
			);
			
			waitsFor(
				function() {
					return( lResponse.code == -1 );
				},
				lSpec,
				2000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( -1 );
			});
		}); 
	},
	
	testShutdownEndPointFailArguments : function ( aPassword ) {
		var lSpec = "Shutdown service ("+ aPassword + ", endPointIdWrong, service3), whit valid credential and invalid arguments";
		
		it( lSpec, function () {
			var lResponse = {};	
		
			jws.Tests.getAdminTestConn().lbShutdownEndPoint(
				aPassword,
				{	endPointId: "wrongId",
					clusterAlias: "service3",
					OnResponse: function( aToken ) {	
						lResponse = aToken;
					}
				}			
			);
			
			waitsFor(
				function() {
					return( lResponse.code == -1 );
				},
				lSpec,
				2000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( -1 );
			});
		}); 
	},
	
	runSpecs: function() {
		testSumNS = 'org.jwebsocket.plugins.samplesum';
		testMulNS = 'org.jwebsocket.plugins.samplemul';
		
		jws.tests.LoadBalancer.testRegisterServiceEndPoint("admin","service1",0, "with valid credential and valid service");
		jws.tests.LoadBalancer.testRegisterServiceEndPoint("admin","service2",0, "with valid credential and valid service");
		jws.tests.LoadBalancer.testRegisterServiceEndPoint("manage","service2",-1,"with invalid credential and valid service"); 
		jws.tests.LoadBalancer.testRegisterServiceEndPoint("admin","service3",-1,"with invalid credential and invalid service"); 
		jws.tests.LoadBalancer.testServices1();
		jws.tests.LoadBalancer.testServices2();
		jws.tests.LoadBalancer.testChangeAlgorithm(1, "valid argument");
		jws.tests.LoadBalancer.testChangeAlgorithm(2, "valid argument");
		jws.tests.LoadBalancer.testChangeAlgorithm(3, "valid argument");
		jws.tests.LoadBalancer.testChangeAlgorithm(4, "invalid argument");
		jws.tests.LoadBalancer.testClustersInfo();
		jws.tests.LoadBalancer.testStickyRoutes();
        jws.tests.LoadBalancer.testDeregisterServiceEndPoint("admin");	
		jws.tests.LoadBalancer.testDeregisterServiceEndPointFailCredential("noAdmin");
		jws.tests.LoadBalancer.testDeregisterServiceEndPointFailArguments("admin");
		jws.tests.LoadBalancer.testShutdownEndPoint("admin");	
		jws.tests.LoadBalancer.testShutdownEndPointFailCredential("noAdmin");
		jws.tests.LoadBalancer.testShutdownEndPointFailArguments("admin");
		
	}
};
