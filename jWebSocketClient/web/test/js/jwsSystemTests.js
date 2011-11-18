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

jws.tests.System = {

	NS: "jws.tests.system", 

	// this spec tests the login function of the system plug-in
	testLoginValidCredentials: function() {
		var lSpec = this.NS + ": Logging in with valid credentials";
		it( lSpec, function () {

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();
			var lResponse = {};

			// open a separate control connection
			lConn.logon( jws.getDefaultServerURL(), "guest", "guest", {
				OnToken: function ( aToken ) {
					if( "org.jwebsocket.plugins.system" == aToken.ns
						&& "login" == aToken.reqType) {
						lResponse = aToken;
					}
				}
			});

			waitsFor(
				function() {
					return( lResponse.code != undefined );
				},
				lSpec,
				1500
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				lConn.close();
			});
		});
	},


	// this spec tests the login function of the system plug-in
	testLoginInvalidCredentials: function() {
		var lSpec = this.NS + ": Logging in with invalid credentials";
		it( lSpec, function () {

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();
			var lResponse = {};

			// open a separate control connection
			lConn.logon( jws.getDefaultServerURL(), "InVaLiD", "iNvAlId", {
				OnToken: function ( aToken ) {
					if( "org.jwebsocket.plugins.system" == aToken.ns
						&& "login" == aToken.reqType) {
						lResponse = aToken;
					}
				}
			});

			waitsFor(
				function() {
					return( lResponse.code != undefined );
				},
				lSpec,
				1500
			);

			runs( function() {
				expect( lResponse.code ).toEqual( -1 );
				lConn.close();
			});
		});
	},


	// this spec tests the send method of the system plug-in by sending
	// this spec requires an established connection
	testSendLoopBack: function() {
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

	// this spec tests the connect timeout behaviour of the client
	testConnectTimeout: function( aURL, aOpenTimeout, aExpectedResult ) {
		var lSpec = this.NS + ": connect timeout" 
			+ " (timeout: " + aOpenTimeout + "ms)";
		
		it( lSpec, function () {

			// we need to "control" the server to broadcast to all connections here
			var lConn = new jws.jWebSocketJSONClient();
			var lStatus = jws.CONNECTING;

			// open a separate control connection
			lConn.open( aURL ? aURL : jws.getDefaultServerURL(), {
				
				openTimeout: aOpenTimeout,
				OnOpenTimeout: function ( aToken ) {
					debugger;
					lStatus = jws.OPEN_TIMED_OUT;
				},
				
				OnOpen: function ( aToken ) {
					// prevent screwing up result 
					// if timeout has been fired before
					if( lStatus == jws.CONNECTING ) {
						lStatus = jws.OPEN;
					}
				},
				
				OnClose: function ( aToken ) {
					lStatus = jws.CLOSED;
				}
			});

			waitsFor(
				function() {
					return( lStatus != jws.CONNECTING );
				},
				lSpec,
				aOpenTimeout + 500
			);

			runs( function() {
				expect( lStatus ).toEqual( aExpectedResult );
				lConn.close();
			});
		});
	},

	// this spec tests the response timeout behaviour of the client
	testResponseTimeout: function( aServerDelay, aClientTimeout ) {
		var lSpec = this.NS + ": response timeout" 
			+ " (Server: " + aServerDelay + "ms," 
			+ " client: " + aClientTimeout + "ms)";
		
		it( lSpec, function () {

			var lResponse = {};
			var lExpectTimeout = aServerDelay > aClientTimeout;
			var lTimeoutFired = false;
			jws.Tests.getAdminConn().testTimeout( 
				aServerDelay,
				{
					OnResponse: function( aToken ) {
						lResponse = aToken;
					},
					
					timeout: aClientTimeout,
					OnTimeout: function( aToken ) {
						lTimeoutFired = true;
					}
				}
				);

			waitsFor(
				function() {
					if( lExpectTimeout ) {
						return( lTimeoutFired === true );
					} else {
						return( lResponse.code == 0 );
					}	
				},
				lSpec,
				aClientTimeout + 1000
			);

			runs( function() {
				if( lExpectTimeout ) {
					expect( lTimeoutFired ).toEqual( true );
				} else {
					expect( lResponse.code ).toEqual( 0 );
				}	
			});

		});
	},

	runSpecs: function() {
		jws.tests.System.testLoginValidCredentials();
		jws.tests.System.testLoginInvalidCredentials();
		jws.tests.System.testSendLoopBack();

		jws.tests.System.testConnectTimeout( null, 5000, jws.OPEN );
		// jws.tests.System.testConnectTimeout( null, 20, jws.OPEN_TIMED_OUT );
		 
		// use an invalid port to simulate "server not available" case
		// jws.tests.System.testConnectTimeout( "ws://jwebsocket.org:1234", 10000, jws.CLOSED );
		// jws.tests.System.testConnectTimeout( "ws://jwebsocket.org:1234", 1000, jws.OPEN_TIMED_OUT );
	   
		// should return a result within timeout
		jws.tests.System.testResponseTimeout( 500, 100 );
		// should exceed the timeout and fire timeout event
		jws.tests.System.testResponseTimeout( 1000, 500 );
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}

