//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for automated tests according to API
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


jws.tests.AutomatedAPITests = {

	NS: "jws.tests.automated", 
	lSpecs: [],

	var NS_BENCHMARK = jws.NS_BASE  + ".plugins.benchmark";

	var MAX_CONNECTIONS = 50;
	var MAX_BROADCASTS = 100;
	var OPEN_CONNECTIONS_TIMEOUT = 30000;
	var BROADCAST_TIMEOUT = 30000;
	var CLOSE_CONNECTIONS_TIMEOUT = 30000;
	var BROADCAST_MESSAGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghihjklmnopqrstuvwxyz 0123456789";

	var ROOT_USER = "root";

	var lConnectionsOpened = 0;
	var lConnections = [];
	var lPacketsReceived = 0;


	// this spec opens all connections
	testOpenConnections: function() {
		var lSpec = "Opening " + MAX_CONNECTIONS + " connections";
		it( lSpec, function () {

			// reset all watches
			jws.StopWatchPlugIn.resetWatches();

			// start stop watch for this spec
			jws.StopWatchPlugIn.startWatch( "openConn", lSpec );

			for( var lIdx = 0; lIdx < MAX_CONNECTIONS; lIdx++ ) {

				lConnections[ lIdx ] = new jws.jWebSocketJSONClient();
				lConnections[ lIdx ].open( jws.getDefaultServerURL(), {

					OnOpen: function () {
						lConnectionsOpened++;
					},

					OnClose: function () {
						lConnectionsOpened--;
					},

					OnToken: function( aToken ) {
						if ( "s2c_performance" == aToken.type
								&& NS_BENCHMARK == aToken.ns ) {
							lPacketsReceived++;
						}
					}

				});
			}

			// wait for expected connections being opened
			waitsFor(
				function() {
					return lConnectionsOpened == MAX_CONNECTIONS;
				},
				"opening connection...",
				OPEN_CONNECTIONS_TIMEOUT
			);

			runs(
				function () {
					expect( lConnectionsOpened ).toEqual( MAX_CONNECTIONS );
					// stop watch for this spec
					jws.StopWatchPlugIn.stopWatch( "openConn" );
				}
			);

		});
	},


	// this spec closes all connections
	testCloseConnections: function() {
		var lSpec = "Closing " + MAX_CONNECTIONS + " connections";
		it( lSpec, function () {

			// start stop watch for this spec
			jws.StopWatchPlugIn.startWatch( "closeConn", lSpec );

			for( var lIdx = 0; lIdx < MAX_CONNECTIONS; lIdx++ ) {
				lConnections[ lIdx ].close({
					timeout: 3000,
					// fireClose: true,
					// noGoodBye: true,
					noLogoutBroadcast: true,
					noDisconnectBroadcast: true
				});
			}

			// wait for expected connections being opened
			waitsFor(
				function() {
					return lConnectionsOpened == 0;
				},
				"closing connections...",
				CLOSE_CONNECTIONS_TIMEOUT
			);

			runs(
				function () {
					expect( lConnectionsOpened ).toEqual( 0 );

					// stop watch for this spec
					jws.StopWatchPlugIn.stopWatch( "closeConn" );

					// print all watches to the console
					jws.StopWatchPlugIn.printWatches();

					// reset all watches
					jws.StopWatchPlugIn.resetWatches();
				}
			);
		});
	},

	testBenchmark: function() {
		var lSpec = "Broadcasting " + MAX_BROADCASTS + " packets to " + MAX_CONNECTIONS + " connections";
		it( lSpec, function () {

			// start stop watch for this spec
			jws.StopWatchPlugIn.startWatch( "broadcast", lSpec );

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();

			// open a separate control connection
			lConn.open(jws.getDefaultServerURL(), {

				OnOpen: function () {
					lPacketsReceived = 0;
					var lToken = {
						ns: NS_BENCHMARK,
						type: "s2c_performance",
						count: MAX_BROADCASTS,
						message: BROADCAST_MESSAGE
					};
					lConn.sendToken( lToken );
				}
			});

			waitsFor(
				function() {
					return lPacketsReceived == MAX_CONNECTIONS * MAX_BROADCASTS;
				},
				"broadcasting test packages...",
				BROADCAST_TIMEOUT
			);

			runs( function() {
				expect( lPacketsReceived ).toEqual( MAX_CONNECTIONS * MAX_BROADCASTS );

				// stop watch for this spec
				jws.StopWatchPlugIn.stopWatch( "broadcast" );
			});
		});
	},


	runSpecs: function() {
		// open all connections
		this.testOpenConnections();

		// run the benchmark
		this.testBenchmark();

		// close all connections
		this.testCloseConnections();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};
