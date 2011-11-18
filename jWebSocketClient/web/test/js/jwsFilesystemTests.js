//	---------------------------------------------------------------------------
//	jWebSocket TestSpecs for the Filesystem Plug-in
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


jws.tests.FileSystem = {

	NS: "jws.tests.filesystem", 
	
	TEST_FILE_DATA: "This is a string to be saved into the test file!",
	TEST_FILE_NAME: "test.txt",

	// this spec tests the file save method of the fileSystem plug-in
	testFileSave: function() {
		var lSpec = this.NS + ": FileSave (public, admin)";
		var lData = this.TEST_FILE_DATA;
		var lFilename = this.TEST_FILE_NAME;
		
		it( lSpec, function () {

			var lResponse = {};

			jws.Tests.getAdminConn().fileSave( lFilename, lData, {
				encoding: "base64",
				scope: "public",
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( lResponse.code === 0 );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},

	// this spec tests the file save method of the fileSystem plug-in
	testFileLoad: function() {
		var lSpec = this.NS + ": FileLoad (public, admin)";
		var lData = this.TEST_FILE_DATA;
		var lFilename = this.TEST_FILE_NAME;
		
		it( lSpec, function () {

			var lResponse = {};

			jws.Tests.getAdminConn().fileLoad( lFilename, {
				encoding: "base64",
				scope: "public",
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( lResponse.data == lData );
				},
				lSpec,
				3000
			);

			runs( function() {
				expect( lResponse.data ).toEqual( lData );
			});

		});
	},

	runSpecs: function() {
		jws.tests.FileSystem.testFileSave();
		jws.tests.FileSystem.testFileLoad();
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};