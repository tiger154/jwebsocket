//	---------------------------------------------------------------------------
//	jWebSocket Alarm application test suite
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

jws.tests = {};
jws.tests.Alarm = {

	NS: "jws.tests.alarm", 

	testLogon: function() {
		var lSpec = this.NS + ": logon";
		it( lSpec, function () {

			var lResponse = null;
			var lUsername = "kyberneees";
			jws.alarmApp.authPlugIn.logon({
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
	
	testLogoff: function() {
		var lSpec = this.NS + ": logoff";
		it( lSpec, function () {
			var lResponse = null;
			
			jws.alarmApp.authPlugIn.logoff({
				OnResponse: function(aResponse){
					lResponse = aResponse;
				}
			});

			waitsFor(
				function() {
					return( lResponse != null );
				}, lSpec, 3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});
		});
	},
	
	testCreate: function() {
		var lSpec = this.NS + ": create alarm";
		it( lSpec, function () {
			var lResponse = null;
			
			jws.alarmApp.alarmPlugIn.create({
				args: {
					time: (new Date().getTime() + 1000).toString(),
					message: "Alarm message!"
				},
				OnResponse: function(aResponse){
					lResponse = aResponse;
				}
			});

			waitsFor(
				function() {
					return( lResponse != null );
				}, lSpec, 3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});
		});
	},
	testAlarmActiveNotification: function() {
		var lSpec = this.NS + ": alarm active notification";
		it( lSpec, function () {
			var lAlarmMessage = null;
			
			jws.alarmApp.alarmPlugIn.alarmActive = function(aEvent) {
				lAlarmMessage = aEvent.message;
			}

			waitsFor(
				function() {
					return( lAlarmMessage != null );
				}, lSpec, 3000
				);

			runs( function() {
		 		expect( lAlarmMessage ).toEqual( "Alarm message!" );
			});
		});
	},
	
	testList: function() {
		var lSpec = this.NS + ": list alarms";
		it( lSpec, function () {
			var lResponse = null;
		
			jws.alarmApp.alarmPlugIn.list({
				OnResponse: function(aResponse){
					lResponse = aResponse;
				}
			});

			waitsFor(
				function() {
					return( lResponse != null );
				}, lSpec, 3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				var lLastPosition = lResponse.data.length - 1;
				expect( lResponse.data[lLastPosition].message ).toEqual( "Alarm message!" );
			});
		});
	},

	
	runSpecs: function() {
		jws.tests.Alarm.testLogon();
		jws.tests.Alarm.testCreate();
		jws.tests.Alarm.testAlarmActiveNotification();
		jws.tests.Alarm.testList();
		jws.tests.Alarm.testLogoff();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	
}

jws.tests.Alarm.runSuite();

