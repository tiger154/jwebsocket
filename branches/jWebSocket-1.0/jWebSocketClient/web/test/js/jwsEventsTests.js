//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the EventsPlugIn test application
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
	authenticated: false,

	// this spec tests the login operation of the test application
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
			});

			waitsFor(
				function() {
					return( lResponse != null );
				},
				lSpec,
				5000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				expect( lResponse.username ).toEqual( lUsername );
				expect( lResponse.uuid ).toEqual( lUsername );
				expect( lResponse.roles instanceof Array ).toEqual( true );
				jws.tests.Events.authenticated = true;
			});
		});
	},
	
	// this spec tests the logoff operation of the test application
	testLogoff: function() {
		
		var lSpec = this.NS + ": logoff";
		
		it( lSpec, function () {
			var lResponse = null;
			
			waitsFor(function(){
				return jws.tests.Events.authenticated;
			});
			
			auth.logoff({
				OnResponse: function(aResponse){
					lResponse = aResponse;
				}
			});

			waitsFor(
				function() {
					return( lResponse != null );
				}, lSpec, 5000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				jws.tests.Events.authenticated = false;
			});
		});
	},
	
	// this spec tests the getEventsInfo operation of the test application
	testGetEventsInfo: function() {
		
		var lSpec = this.NS + ": getEventsInfo";
		
		it( lSpec, function () {
			var lResponse = null;
			
			test.getEventsInfo({
				OnResponse: function(aResponse){
					lResponse = aResponse;
				}
			});

			waitsFor(
				function() {
					return( lResponse != null );
				}, lSpec, 5000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				expect( lResponse.table ).toBeTypeOf("object");
				expect( lResponse.table.name ).toBeTypeOf("string");
				expect( lResponse.table.version ).toBeTypeOf("string");
			});
		});
	},
	
	// this spec tests the S2C event notification operation of the test application
	testS2CEventNotification: function() {
		
		var lSpec = this.NS + ": S2CEventNotification";
		
		it( lSpec, function () {
			var lX = 0;
			var lY = 0;
			var lCalled = false;
			
			test.plusXY = function(e){
				lX = e.x;
				lY = e.y;
				lCalled = true;
				
				return e.x + e.y;
			}
			
			test.s2cNotification();

			waitsFor(
				function() {
					return lCalled;
				}, lSpec, 5000
				);

			runs( function() {
				expect( lX + lY ).toEqual( 10 );
			});
		});
	},

	
	runSpecs: function() {
		jws.tests.Events.testLogon();
		jws.tests.Events.testLogoff();
		jws.tests.Events.testGetEventsInfo();
		jws.tests.Events.testS2CEventNotification();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

}

