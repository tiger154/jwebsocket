//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the JMS Plug-in
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

jws.tests.JMS = {

	NS: "jws.tests.jms", 

	// this spec tests the listen method of the JMS plug-in
	testListen: function() {
		var lSpec = this.NS + ": listen (no Pub/Sub)";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().listenJms( 
				"connectionFactory",	// aConnectionFactoryName, 
				"testQueue",			// aDestinationName, 
				false,					// aPubSubDomain,
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

	// this spec tests the listen method of the JMS plug-in
	testUnlisten: function() {
		var lSpec = this.NS + ": unlisten (no Pub/Sub)";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminConn().unlistenJms( 
				"connectionFactory",	// aConnectionFactoryName, 
				"testQueue",			// aDestinationName, 
				false,					// aPubSubDomain,
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
		jws.tests.JMS.testListen();
		jws.tests.JMS.testUnlisten();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}

