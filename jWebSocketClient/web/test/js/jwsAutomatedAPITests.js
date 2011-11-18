//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the jWebSocket benchmarks
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


jws.tests.AutomatedAPI = {

	NS: "jws.tests.automated", 
	mSpecs: [],

	testGetAPIDefaults: function() {

		var lSpec = this.NS + ": Running default API spec";

		it( lSpec, function () {

			var lDone = false;

			// start stop watch for this spec
			jws.StopWatchPlugIn.startWatch( "defAPIspec", lSpec );

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();

			// open a separate control connection
			lConn.open(jws.getDefaultServerURL(), {

				OnWelcome: function () {
					var lAPIPlugIn = new jws.APIPlugIn();
					lConn.addPlugIn( lAPIPlugIn );
					// request the API of the benchmark plug-in
					lAPIPlugIn.getPlugInAPI(
						"jws.benchmark", {
						// if API received successfully run the tests...
						OnResponse: function( aServerPlugIn ) {
							jws.tests.AutomatedAPI.mSpecs = 
								lAPIPlugIn.createSpecFromAPI( lConn, aServerPlugIn );
							lDone = true;
						},
						OnTimeout: function() {
							lConn.close();
							lDone = true;
						}
					});
				}
			});

			waitsFor(
				function() {
					return lDone == true;
				},
				"Running against API...",
				3000
			);

			runs( function() {
				expect( lDone ).toEqual( true );

				// stop watch for this spec
				jws.StopWatchPlugIn.stopWatch( "defAPIspec" );
			});
		});
	},

	testRunAPIDefaults: function() {
		it( this.NS + ": Running default tests", function() {
			eval( 
				"  for( var i = 0; i < jws.tests.AutomatedAPI.mSpecs.length; i++ ) { "
				+ "  jws.tests.AutomatedAPI.mSpecs[ i ]();"
				+ "}"
			);
		});
	},

	runSpecs: function() {
		// get the default specs from the API
		this.testGetAPIDefaults();
		// run all the obtained default specs
		// this.testRunAPIDefaults();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};