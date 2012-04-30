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
		jws.tests.Benchmarks.testOpenConnections();

		// run the benchmark
		jws.tests.Benchmarks.testBenchmark();

		// close all connections
		jws.tests.Benchmarks.testCloseConnections();
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
		OnWelcome: function (){
			//Initializing events in the client... 
			//Creating the filter chain
			var securityFilter = new jws.SecurityFilter();
			securityFilter.OnNotAuthorized = function(aEvent){
			//Not Authorized global callback!
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
			if ( undefined != dialog ) {
				dialog( "You are not connected to the server!", "jWebSocket Message", true, null, null, "alert");
			} else {
				alert( "You are not connected to the server!" );
			}
		}
	});
}

function runFullTestSuite(aArgs) {

	/*
	debugger;
	jasmine.VERBOSE = true;
	 */
	var lIntv = jasmine.DEFAULT_UPDATE_INTERVAL;
	jasmine.DEFAULT_UPDATE_INTERVAL = 5;
   
	describe( "jWebSocket Test Suite", function () {
		
		if (aArgs.openConns){
			var lTestSSL = false;
			// open connections for admin and guest
			jws.Tests.testOpenSharedAdminConn();
			jws.Tests.testOpenSharedGuestConn();
			if( lTestSSL ) {
				jws.Tests.testOpenSharedAdminConnSSL();
				jws.Tests.testOpenSharedGuestConnSSL();
			}
		}
		
		if (aArgs.load){
			// run load tests
			jws.tests.Load.runSuite();
		}
		
		// run test suites for the various plug-ins
		if (aArgs.systemPlugIn){
			jws.tests.System.runSuite();
		}
		if (aArgs.filesystemPlugIn){
			jws.tests.FileSystem.runSuite();
		}
		// jws.tests.Logging.runSuite();
		if (aArgs.automatedAPIPlugIn){
			jws.tests.AutomatedAPI.runSuite();
		}
		if (aArgs.rpcPlugIn){
			// run RPC tests
			jws.tests.RPC.runSuite();
		}
		// run JMS tests
		// jws.tests.JMS.runSuite();
   
		if (aArgs.channelsPlugIn){
			// run Channel tests
			jws.tests.Channels.runSuite();
		}
		
		if (aArgs.streamingPlugIn){
			// run Streaming tests
			jws.tests.Streaming.runSuite();
		}
		// run JDBC tests
		// jws.tests.JDBC.runSuite();
		
		if (aArgs.closeConns){
			// close connections for admin and guest
			jws.Tests.testCloseSharedAdminConn();
			jws.Tests.testCloseSharedGuestConn();
			if( lTestSSL ) {
				jws.Tests.testCloseSharedAdminConnSSL();
				jws.Tests.testCloseSharedGuestConnSSL();
			}
		}
		
		if (aArgs.ioc){
			//run IOC tests
			jws.tests.ioc.runSuite();
		}
		
		if (aArgs.events){
			runEventsSuite();
		}
		
		jasmine.DEFAULT_UPDATE_INTERVAL = lIntv;	
	});
}
