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
	mEndPointId: "",
	mClusterAlias: "",
	mDeregisterConn: null,
	// this spec tests the clusters information feature	
	testClustersInfo: function () {
		var lSpec = "getting clusters information";
		var lResponse = null;

		it(lSpec, function () {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function (aResponse) {
					lResponse = aResponse;
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return (null !== lResponse);
					},
					lSpec,
					2000
					);

			// check the result 
			runs(function () {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint1: function () {
		var lSpec = "Register service endpoint( invalid password, valid clusterAlias, valid clusterNS )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function (aResponse) {
					var lClusterInfoValues = aResponse.data;
					if (lClusterInfoValues.length > 0) {
						var lConn = null;

						// perform the create sample service on the server
						// with invalid credential and valid arguments
						lConn = jws.Tests.getAdminTestConn().lbSampleService(
								lClusterInfoValues[0].clusterAlias, "wrongUser", {
							nameSpace: lClusterInfoValues[0].clusterNS,
							OnResponse: function (aResponse) {
								lResponse = aResponse;
								lConn.close();
							}
						});
					} else {
						lResponse = {
							code: -1,
							msg: 'failure'
						}
					}
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return (null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint2: function () {
		var lSpec = "Register service endpoint( valid password, invalid clusterAlias, invalid clusterNS )";
		var lResponse = null;

		it(lSpec, function () {
			var lConn = null;

			// perform the create sample service on the server
			// with valid credential and invalid arguments
			lConn = jws.Tests.getAdminTestConn().lbSampleService('wrongClusterAlias', 'admin', {
				nameSpace: 'wrongClusterNS',
				OnResponse: function (aResponse) {
					lResponse = aResponse;
					lConn.close();
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return (null !== lResponse);
					},
					lSpec,
					2000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint3: function () {
		var lSpec = "Register service endpoint( invalid password, invalid clusterAlias, invalid clusterNS )";
		var lResponse = null;

		it(lSpec, function () {
			var lConn = null;

			// perform the create sample service on the server
			// with invalid credential and invalid arguments
			lConn = jws.Tests.getAdminTestConn().lbSampleService('wrongClusterAlias', 'wrongUser', {
				nameSpace: 'wrongClusterNS',
				OnResponse: function (aResponse) {
					lResponse = aResponse;
					lConn.close();
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return (null !== lResponse);
					},
					lSpec,
					2000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the register endpoints feature
	testRegisterServiceEndPoint4: function () {
		var lSpec = "Register service endpoint( valid password, valid clusterAlias, valid clusterNS )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function (aResponse) {
					var lClusterInfoValues = aResponse.data;

					if (lClusterInfoValues.length > 0) {
						var lTarget = lClusterInfoValues.length - 1;
						var lConn = null;

						// perform the create sample service on the server
						// with valid credential and valid arguments	
						lConn = jws.Tests.getAdminTestConn().lbSampleService(
								lClusterInfoValues[lTarget].clusterAlias, "admin", {
							nameSpace: lClusterInfoValues[lTarget].clusterNS,
							OnResponse: function (aResponse) {
								lResponse = aResponse;
								lConn.close();
							}
						});
					} else {
						lResponse = {
							code: -1,
							msg: 'failure'
						};
					}
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return (null !== lResponse);
					},
					lSpec,
					5000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	// this spec tests the change algorithm feature
	testChangeAlgorithm1: function () {
		var lSpec = "Change Algorithm ( valid argument )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the change algorithm  on the server
			jws.Tests.getAdminTestConn().lbChangeAlgorithm(1, {
				OnResponse: function (aResponse) {
					if (aResponse.code > -1) {
						this.lbChangeAlgorithm(2, {
							OnResponse: function (aResponse) {
								if (aResponse.code > -1) {
									this.lbChangeAlgorithm(3, {
										OnResponse: function (aResponse) {
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
					function () {
						return(null !== lResponse);
					},
					lSpec,
					2000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	// this spec tests the change algorithm feature
	testChangeAlgorithm2: function () {
		var lSpec = "Change Algorithm ( invalid argument )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the change algorithm  on the server
			jws.Tests.getAdminTestConn().lbChangeAlgorithm(4, {
				OnResponse: function (aResponse) {
					lResponse = aResponse;
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					2000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the sticky routes feature
	testStickyRoutes: function ( ) {
		var lSpec = "Sticky routes ()";
		var lResponse = null;

		it(lSpec, function () {

			// perform the sticky routes feature on the server
			jws.Tests.getAdminTestConn().lbStickyRoutes({
				OnResponse: function (aResponse) {
					lResponse = aResponse;
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return (null !== lResponse);
					},
					lSpec,
					2000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	// this spec tests the test services feature
	testServices: function ( ) {
		var lSpec = "Testing services()";
		it(lSpec, function () {
			var lResponses = [];
			var lClusters = [];

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function (aResponse) {
					lClusters = aResponse.data;

					for (var lPos = 0; lPos < lClusters.length; lPos++) {
						jws.Tests.getAdminTestConn().sendToken({
							ns: lClusters[lPos].clusterNS,
							type: 'test'
						}, {
							OnResponse: function (aResponse) {
								lResponses.push(aResponse);
							}
						});
					}
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return (lResponses.length === lClusters.length);
					},
					lSpec,
					2000
					);

			// check the result
			runs(function () {
				for (var lPos = 0; lPos < lResponses.length; lPos++) {
					expect(lResponses[lPos].type).toEqual('response');
					expect(lResponses[lPos].reqType).toEqual('test');
				}
			});
		});
	},
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint1: function () {
		var lSpec = "Shutdown service ( invalid password, valid endPointId, valid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function (aResponse) {
					var lClusterInfoValues = aResponse.data;

					// perform the create sample service on the server
					// with valid credential and valid arguments
					jws.Tests.getAdminTestConn().lbSampleService(
							lClusterInfoValues[0].clusterAlias, "admin", {
						nameSpace: lClusterInfoValues[0].clusterNS,
						OnSuccess: function (aResponse) {
							jws.tests.LoadBalancer.mEndPointId = aResponse.endPointId;
							jws.tests.LoadBalancer.mClusterAlias = lClusterInfoValues[0].clusterAlias;

							// perform the shutdown feature an specific service endpoint
							// with invalid credential and valid arguments
							jws.Tests.getAdminTestConn().lbShutdownEndPoint(
									jws.tests.LoadBalancer.mClusterAlias,
									"wrongPassword",
									jws.tests.LoadBalancer.mEndPointId, {
										OnResponse: function (aResponse) {
											lResponse = aResponse;
										}
									});
						}
					});
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint2: function () {
		var lSpec = "Shutdown service ( valid password, invalid endPointId, invalid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the shutdown feature an specific service endpoint
			// with valid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbShutdownEndPoint("wrongClusterAlias", "admin", "wrongEndPointId", {
				OnResponse: function (aResponse) {
					lResponse = aResponse;
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint3: function () {
		var lSpec = "Shutdown service ( invalid password, invalid endPointId, invalid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the shutdown feature an specific service endpoint
			// with invalid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbShutdownEndPoint(
					"wrongClusterAlias", "wrongPassword", "wrongEndPointId", {
						OnResponse: function (aResponse) {
							lResponse = aResponse;
						}
					});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the shutdown service endpoint feature
	testShutdownEndPoint4: function () {
		var lSpec = "Shutdown service ( valid password, valid endPointId, valid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the shutdown feature an specific service endpoint
			// with valid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbShutdownEndPoint(
					jws.tests.LoadBalancer.mClusterAlias,
					"admin",
					jws.tests.LoadBalancer.mEndPointId, {
						OnResponse: function (aResponse) {
							lResponse = aResponse;
						}
					});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint1: function () {
		var lSpec = "Deregister service ( invalid password, valid endPointId, valid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the clusters information feature on the server
			jws.Tests.getAdminTestConn().lbClustersInfo({
				OnResponse: function (aResponse) {
					var lClusterInfoValues = aResponse.data;

					// perform the create sample service on the server
					// with valid credential and valid arguments
					jws.tests.LoadBalancer.mDeregisterConn = jws.Tests.getAdminTestConn().lbSampleService(
							lClusterInfoValues[0].clusterAlias, 'admin', {
						nameSpace: lClusterInfoValues[0].clusterNS,
						OnSuccess: function (aResponse) {
							jws.tests.LoadBalancer.mEndPointId = aResponse.endPointId;
							jws.tests.LoadBalancer.mClusterAlias = lClusterInfoValues[0].clusterAlias;

							// perform the deregister feature an specific service endpoint
							// with invalid credential and valid arguments
							jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
									jws.tests.LoadBalancer.mClusterAlias,
									"wrongPassword",
									jws.tests.LoadBalancer.mEndPointId, {
										OnResponse: function (aResponse) {
											lResponse = aResponse;
										}
									});
						}
					});
				}
			});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint2: function () {
		var lSpec = "Deregister service ( valid password, invalid endPointId, invalid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the shutdown feature an specific service endpoint
			// with valid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
					"wrongClusterAlias", "admin", "wrongEndPointId", {
						OnResponse: function (aResponse) {
							lResponse = aResponse;
						}
					});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint3: function () {
		var lSpec = "Deregister service ( invalid password, invalid endPointId, invalid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the deregister feature an specific service endpoint
			// with invalid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
					"wrongClusterAlias", "wrongPassword", "wrongEndPointId", {
						OnResponse: function (aResponse) {
							lResponse = aResponse;
						}
					});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(-1);
			});
		});
	},
	// this spec tests the deregister service endpoint feature
	testDeregisterServiceEndPoint4: function () {
		var lSpec = "Deregister service ( valid password, valid endPointId, valid service )";
		var lResponse = null;

		it(lSpec, function () {

			// perform the deregister feature an specific service endpoint
			// with valid credential and invalid arguments
			jws.Tests.getAdminTestConn().lbDeregisterServiceEndPoint(
					jws.tests.LoadBalancer.mClusterAlias,
					"admin",
					jws.tests.LoadBalancer.mEndPointId, {
						OnResponse: function (aResponse) {
							lResponse = aResponse;
							jws.tests.LoadBalancer.mDeregisterConn.close();
						}
					});

			// wait for result, consider reasonable timeout
			waitsFor(
					function () {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			// check the result
			runs(function () {
				expect(lResponse.code).toEqual(0);
			});
		});
	},
	runSpecs: function () {

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
