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
jws.tests.Events = {

	NS: "jws.tests.events", 

	// this spec tests the login plugin of the test application
	testLogon: function() {
		
		var lSpec = this.NS + ": logon";
		
		it( lSpec, function () {

			var lResponse = null;
			var lUsername = "kyberneees";
			auth.logon({
				args: {
					username: lUsername,
					password: "123"
				},
				OnResponse: function(aResponse){
					lResponse = aResponse;
				}
			})

			waitsFor(
				function() {
					return( lResponse != null );
				},
				lSpec,
				3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				expect( lResponse.username ).toEqual( lUsername );
				expect( lResponse.uuid ).toEqual( lUsername );
				expect( lResponse.roles instanceof Array ).toEqual( true );
			});

		});
	},

	
	runSpecs: function() {
		jws.tests.Events.testLogon();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}

