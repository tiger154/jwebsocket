//	---------------------------------------------------------------------------
//	jWebSocket Testsuites
//	(C) 2011 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------


// ---------------------------------------------------------------------------
// the various jWebSocket test suites
// ---------------------------------------------------------------------------

// this is a suite
function runOpenCloseSuite () {

	describe( "Open/Close Test Suite", function () {
		testOpenConnections();
		testCloseConnections();
	});
}

function runBenchmarkSuite() {

	describe( "Benchmark Test Suite", function () {

		// open all connections
		testOpenConnections();

		// run the benchmark
		testBenchmark();

		// close all connections
		testCloseConnections();
	});

}

function runDefaultAPISuite() {

	describe( "Default API test Suite", function () {

		// open all connections
		testOpenConnections();

		// get the default specs from the API
		testGetAPIDefaults();

		// run all the obtained default specs
		testRunAPIDefaults();

		// close all connections
		testCloseConnections();
	});

}

function runEventsSuite() {
	//run Events tests
	jws.myConn = new jws.jWebSocketJSONClient();
	jws.myConn.open(jws.JWS_SERVER_URL, {
		OnOpen: function (){
			//Initializing events in the client... 
			//Creating the filter chain
			var securityFilter = new jws.SecurityFilter();
			securityFilter.OnNotAuthorized = function(aEvent){
			//Not Authorized!
			}
			
			var cacheFilter = new jws.CacheFilter();
			cacheFilter.cache = new Cache();
			var validatorFiler = new jws.ValidatorFilter();
			
			//Creating a event notifier
			var notifier = new jws.EventsNotifier();
			notifier.ID = "notifier0";
			notifier.NS = "test";
			notifier.jwsClient = jws.myConn;
			notifier.filterChain = [securityFilter, cacheFilter, validatorFiler];
			notifier.initialize();
			  
			//Creating a plugin generator
			var generator = new jws.EventsPlugInGenerator();

			//Generating the auth & test plug-ins.
			auth = generator.generate("auth", notifier, function(){
				test = generator.generate("test", notifier, function(){
					/*
				 * Run the events test suite when generate the last plugin
				 */
					jws.tests.Events.runSuite();
				});
			});
		},
		OnClose: function(){
			alert("Not connected to the server!")
		}
	});
}

function runFullTestSuite() {

	/*
	debugger;
	jasmine.VERBOSE = true;
	 */
	var lIntv = jasmine.DEFAULT_UPDATE_INTERVAL;
	jasmine.DEFAULT_UPDATE_INTERVAL = 5;
   
	describe( "jWebSocket Test Suite", function () {
		
		var lTestSSL = false;
		
		// open connections for admin and guest
		jws.Tests.testOpenSharedAdminConn();
		jws.Tests.testOpenSharedGuestConn();
		if( lTestSSL ) {
			jws.Tests.testOpenSharedAdminConnSSL();
			jws.Tests.testOpenSharedGuestConnSSL();
		}
		
		// run load tests
		jws.tests.Load.runSuite();
		
		// run test suites for the various plug-ins
		jws.tests.System.runSuite();
		jws.tests.FileSystem.runSuite();
		// jws.tests.Logging.runSuite();
		jws.tests.AutomatedAPI.runSuite();
		
		// run RPC tests
		jws.tests.RPC.runSuite();

		// run JMS tests
		// jws.tests.JMS.runSuite();
   
		// run Channel tests
		jws.tests.Channels.runSuite();
	
		// run Streaming tests
		jws.tests.Streaming.runSuite();
		
		// run JDBC tests
		// jws.tests.JDBC.runSuite();
		
		// close connections for admin and guest
		jws.Tests.testCloseSharedAdminConn();
		jws.Tests.testCloseSharedGuestConn();
		if( lTestSSL ) {
			jws.Tests.testCloseSharedAdminConnSSL();
			jws.Tests.testCloseSharedGuestConnSSL();
		}
		
		//run IOC tests
		jws.tests.ioc.runSuite();

		jasmine.DEFAULT_UPDATE_INTERVAL = lIntv;	
	});
}
