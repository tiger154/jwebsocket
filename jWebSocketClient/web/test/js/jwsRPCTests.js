//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the Logging Plug-in
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


jws.tests.RPC = {

	NS: "jws.tests.rpc", 
	
	TEST_STRING: "This is a string to be MD5'ed", 

	// this spec tests the file save method of the fileSystem plug-in
	testMD5Demo: function() {
		
		var lSpec = this.NS + ": MD5 demo (admin)";
		
		it( lSpec, function () {
			
			// init response
			var lResponse = {};

			var lClassName = "org.jwebsocket.rpc.sample.SampleRPCLibrary";
			var lMethodName = "getMD5";
			var lArguments = jws.tests.RPC.TEST_STRING;
			var lMD5 = jws.tools.calcMD5( jws.tests.RPC.TEST_STRING );

			// perform the Remote Procedure Call...
			jws.Tests.getAdminConn().rpc(
				// pass class, method and argument for server java method:
				lClassName,
				lMethodName,
				lArguments,
				{	// run it within the main thread
					spawnThread: false,
					// new easy-to-use response callback
					OnResponse: function( aToken ) {
						lResponse = aToken;
					}
				}
			);
			
			// wait for result, consider reasonably timeout
			waitsFor(
				function() {
					// check response
					return( lResponse.code !== undefined );
				},
				lSpec,
				3000
			);

			// check result if ok
			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				expect( lResponse.result ).toEqual( lMD5 );
			});

		});
	},

	runSpecs: function() {
		// run alls tests within an outer test suite
		this.testMD5Demo();
	},

	runSuite: function() {
		
		// run alls tests as a separate test suite
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};