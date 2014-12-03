//	---------------------------------------------------------------------------
//	jWebSocket Streaming plug-in test specs (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------

jws.tests.Streaming = {

	title: "Streaming plug-in",
	description: "jWebSocket streaming plug-in",
	category: "Community Edition",

	// this spec tests the register method of the streaming plug-in
	testRegister: function( aStreamId ) {
		var lSpec = "register (" + aStreamId + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminTestConn().registerStream( 
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
		var lSpec = "unregister (" + aStreamId + ")";
		
		it( lSpec, function () {

			var lResponse = {};
			jws.Tests.getAdminTestConn().unregisterStream( 
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
	}
};

