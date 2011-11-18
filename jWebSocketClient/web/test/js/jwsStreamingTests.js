//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the Channel Plug-in
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

jws.tests.Streaming = {

	NS: "jws.tests.streaming", 

	// this spec tests the register method of the streaming plug-in
	testRegister: function( aStreamId ) {
		var lSpec = this.NS + ": register (" + aStreamId + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().registerStream( 
				aStreamId,
				{	OnResponse: function( aToken ) {
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

	// this spec tests the unregister method of the streaming plug-in
	testUnregister: function( aStreamId ) {
		var lSpec = this.NS + ": unregister (" + aStreamId + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().unregisterStream( 
				aStreamId,
				{	OnResponse: function( aToken ) {
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

	runSpecs: function() {
		jws.tests.Streaming.testRegister( "timeStream" );
		jws.tests.Streaming.testUnregister( "timeStream" );
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}

