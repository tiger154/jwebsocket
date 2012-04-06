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



jws.tests.Benchmarks = {

	
	/*  TODO: Finish this TestCase
	 *	this.NS: jws.NS_BASE  + ".plugins.benchmark",
	 *
	 *
		var this.MAX_CONNECTIONS = 50;
		var this.MAX_BROADCASTS = 100;
		var this.OPEN_CONNECTIONS_TIMEOUT = 30000;w
		var BROADCAST_TIMEOUT = 30000;
		var this.CLOSE_CONNECTIONS_TIMEOUT = 30000;
		var this.BROADCAST_MESSAGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghihjklmnopqrstuvwxyz 0123456789";

		var this.ROOT_USER = "root";

		var this.lConnectionsOpened = 0;
		var this.lConnections = [];
		var this.lPacketsReceived = 0;
	 */

	NS: jws.NS_BASE  + ".plugins.benchmark",
	
	MAX_CONNECTIONS: 50,
	
	MAX_BROADCASTS: 100,
	
	BROADCAST_TIMEOUT: 3000,
	
	OPEN_CONNECTIONS_TIMEOUT: 30000,
	
	CLOSE_CONNECTIONS_TIMEOUT: 30000,
	
	BROADCAST_MESSAGE: "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghihjklmnopqrstuvwxyz 0123456789",
	
	ROOT_USER: "root",
	
	lConnectionsOpened: 0,
	
	lConnections: [],
	
	lPacketsReceived: 0,
	
	lSpecs: [],
	
	// this spec opens all connections
	testOpenConnections: function() {
		var lSpec = "Opening " + this.MAX_CONNECTIONS + " connections";
		it( lSpec, function () {

			// reset all watches
			jws.StopWatchPlugIn.resetWatches();

			// start stop watch for this spec
			jws.StopWatchPlugIn.startWatch( "openConn", lSpec );

			for( var lIdx = 0; lIdx < this.MAX_CONNECTIONS; lIdx++ ) {

				this.lConnections[ lIdx ] = new jws.jWebSocketJSONClient();
				this.lConnections[ lIdx ].open( jws.getDefaultServerURL(), {

					OnOpen: function () {
						this.lConnectionsOpened++;
					},

					OnClose: function () {
						this.lConnectionsOpened--;
					},

					OnToken: function( aToken ) {
						if ( "s2c_performance" == aToken.type
							&& this.NS == aToken.ns ) {
							this.lPacketsReceived++;
						}
					}

				});
			}

			// wait for expected connections being opened
			waitsFor(
				function() {
					return this.lConnectionsOpened == this.MAX_CONNECTIONS;
				},
				"opening connection...",
				this.OPEN_CONNECTIONS_TIMEOUT
				);

			runs(
				function () {
					expect( this.lConnectionsOpened ).toEqual( this.MAX_CONNECTIONS );
					// stop watch for this spec
					jws.StopWatchPlugIn.stopWatch( "openConn" );
				}
				);
		});
	},


	// this spec closes all connections
	testCloseConnections: function() {
		var lSpec = "Closing " + this.MAX_CONNECTIONS + " connections";
		it( lSpec, function () {

			// start stop watch for this spec
			jws.StopWatchPlugIn.startWatch( "closeConn", lSpec );

			for( var lIdx = 0; lIdx < this.MAX_CONNECTIONS; lIdx++ ) {
				this.lConnections[ lIdx ].close({
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
					return this.lConnectionsOpened == 0;
				},
				"closing connections...",
				this.CLOSE_CONNECTIONS_TIMEOUT
				);

			runs(
				function () {
					expect( this.lConnectionsOpened ).toEqual( 0 );

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
		var lSpec = "Broadcasting " + this.MAX_BROADCASTS + " packets to " + this.MAX_CONNECTIONS + " connections";
		it( lSpec, function () {

			// start stop watch for this spec
			jws.StopWatchPlugIn.startWatch( "broadcast", lSpec );

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();

			// open a separate control connection
			lConn.open(jws.getDefaultServerURL(), {

				OnOpen: function () {
					this.lPacketsReceived = 0;
					var lToken = {
						ns: this.NS,
						type: "s2c_performance",
						count: this.MAX_BROADCASTS,
						message: this.BROADCAST_MESSAGE
					};
					lConn.sendToken( lToken );
				}
			});

			waitsFor(
				function() {
					return this.lPacketsReceived == this.MAX_CONNECTIONS * this.MAX_BROADCASTS;
				},
				"broadcasting test packages...",
				this.BROADCAST_TIMEOUT
				);

			runs( function() {
				expect( this.lPacketsReceived ).toEqual( this.MAX_CONNECTIONS * this.MAX_BROADCASTS );

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
