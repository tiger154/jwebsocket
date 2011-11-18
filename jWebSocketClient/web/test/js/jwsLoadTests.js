//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the System Plug-in
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

jws.tests.Load = {

	NS: "jws.tests.load", 
	
	// this spec tests the login function of the system plug-in
	testConcurrentConnections: function( aAmount ) {
		var lSpec = this.NS + ": Trying to establish " + aAmount + " concurrent connections...";
		it( lSpec, function () {

			var lLoggedIn = 0;

			var lConns = [];
			for( var lIdx = 0; lIdx < aAmount; lIdx++ ) {
				lConns[ lIdx ] = new jws.jWebSocketJSONClient();
				lConns[ lIdx ].setParam( "connectionIndex", lIdx );
				lConns[ lIdx ].logon( jws.getDefaultServerURL(), 
					jws.GUEST_USER_LOGINNAME, 
					jws.GUEST_USER_PASSWORD, {
					OnToken: function ( aToken ) {
						if( "org.jwebsocket.plugins.system" == aToken.ns
							&& "login" == aToken.reqType) {
							if( 0 == aToken.code ) {
								lLoggedIn++;
								// console.log("Logged-In " + this.getParam( "connectionIndex" ) + ": " + lLoggedIn );
							}
						} else if( "org.jwebsocket.plugins.system" == aToken.ns
							&& "welcome" == aToken.type) {
							// console.log("Connected " + this.getParam( "connectionIndex" ) + ": " + aToken.usid );
						}
					}
				});
			}
			
			waitsFor(
				// wait a maximum of 200ms per connection
				function() {
					return( lLoggedIn == aAmount );
				},
				lSpec,
				aAmount * 300
			);

			runs( function() {
				expect( lLoggedIn ).toEqual( aAmount );
				for( var lIdx = 0, lCnt = lConns.length; lIdx < lCnt; lIdx++ ) {
					lConns[ lIdx ].close();
				}
			});
		});
	},




	// this spec tests the send method of the system plug-in by sending
	// this spec requires an established connection
	testEcho: function() {
		var lSpec = this.NS + ": Send and Loopback";
		it( lSpec, function () {

			// we need to "control" the server to broadcast to all connections here
			var lResponse = {};
			var lMsg = "This is my message";

			// open a separate control connection
			var lToken = {
				ns: jws.NS_SYSTEM,
				type: "send",
				targetId: jws.Tests.getAdminConn().getId(),
				sourceId: jws.Tests.getAdminConn().getId(),
				sender: jws.Tests.getAdminConn().getUsername(),
				data: lMsg
			};

			var lListener = function( aToken ) {
				if( "org.jwebsocket.plugins.system" == aToken.ns
					&& "send" == aToken.type) {
					lResponse = aToken;
				}
			};

			jws.Tests.getAdminConn().addListener( lListener );
			jws.Tests.getAdminConn().sendToken( lToken );

			waitsFor(
				function() {
					return( lResponse.data == lMsg );
				},
				lSpec,
				1500
			);

			runs( function() {
				expect( lResponse.data ).toEqual( lMsg );
				jws.Tests.getAdminConn().removeListener( lListener );
			});

		});
	},

	runSpecs: function() {
		// jws.tests.System.testEcho();
		for( var lIdx = 0; lIdx < 10; lIdx++ ) {
			jws.tests.Load.testConcurrentConnections( 20 );
		}
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}

