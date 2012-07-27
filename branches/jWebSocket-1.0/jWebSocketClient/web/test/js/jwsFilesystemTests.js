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

	testFileSave: function(aFilename, aData, aScope) {
		var lSpec = this.NS + ": FileSave (admin, " + aFilename + ", " + aScope + ")";
		var lData = aData;
		var lFilename = aFilename;
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminConn().fileSave( lFilename, lData, {
				encode: true,
				scope: aScope,
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null != lResponse );
				},
				lSpec,
				3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
			});

		});
	},
	
	testFileSend: function(aFilename, aData) {
		var lSpec = this.NS + ": FileSend (admin, " + aFilename + ")";
		
		it( lSpec, function () {

			var lResponse = null;
			jws.Tests.getAdminConn().setFileSystemCallbacks({
				OnFileReceived: function(aToken){
					lResponse = aToken;
				}
			});
			jws.Tests.getAdminConn().fileSend( jws.Tests.getAdminConn().getId(), aFilename, aData, {
				encoding: "base64"
			});

			waitsFor(
				function() {
					return( null != lResponse );
				},
				lSpec,
				3000
				);

			runs( function() {
				expect( lResponse.filename ).toEqual( aFilename );
				expect( lResponse.data ).toEqual( aData );
			});

		});
	},

	testGetFilelist: function(aAlias, aFilemasks, aRecursive, aExpectedList){
		var lSpec = this.NS + ": GetFilelist (admin, " + aAlias + ", " + 
		JSON.stringify(aFilemasks) + ", " + aRecursive + ")";
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminConn().fileGetFilelist( aAlias, aFilemasks, {
				recursive: aRecursive,
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null != lResponse );
				},
				lSpec,
				3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				
				var lObtainedKeys = [];
				lResponse.files.forEach(function(aItem){
					lObtainedKeys.push(aItem.filename);
				});
					
				expect( lObtainedKeys.sort() ).toEqual( aExpectedList.sort() );
			})

		});
	},

	testFileLoad: function(aFilename, aScope, aExpectedData) {
		var lSpec = this.NS + ": FileLoad (admin, " + aFilename + ", " + aScope + ")";
		var lData = aExpectedData;
		var lFilename = aFilename;
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminConn().fileLoad( lFilename, {
				encoding: "base64",
				scope: aScope,
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null != lResponse );
				},
				lSpec,
				3000
				);

			runs( function() {
				expect( lResponse.data ).toEqual( lData );
			});

		});
	},
	
	testFileDelete: function(aFilename, aForce, aExpectedCode) {
		var lSpec = this.NS + ": FileDelete (admin, " + aFilename + ", " + aExpectedCode + ")";
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminConn().fileDelete( aFilename, aForce, {
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null != lResponse );
				},
				lSpec,
				3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( aExpectedCode );
			});

		});
	},
	
	testFileExists: function(aAlias, aFilename, aExpectedValue) {
		var lSpec = this.NS + ": FileExists (admin, " + aAlias + ", " + aFilename + ")";
		var lFilename = aFilename;
		var lAlias = aAlias;
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminConn().fileExists( lAlias, lFilename, {
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null != lResponse );
				},
				lSpec,
				3000
				);

			runs( function() {
				expect( lResponse.code ).toEqual( 0 );
				expect( lResponse.exists ).toEqual( aExpectedValue );
			});

		});
	},

	runSpecs: function() {
		jws.tests.FileSystem.testFileSave(this.TEST_FILE_NAME, this.TEST_FILE_DATA, jws.SCOPE_PUBLIC);
		jws.tests.FileSystem.testFileLoad(this.TEST_FILE_NAME, jws.SCOPE_PUBLIC, this.TEST_FILE_DATA);
		jws.tests.FileSystem.testFileExists("publicDir", this.TEST_FILE_NAME, true);
		jws.tests.FileSystem.testFileExists("privateDir", "unexisting_file.txt", false);
		jws.tests.FileSystem.testFileSave(this.TEST_FILE_NAME, this.TEST_FILE_DATA, jws.SCOPE_PRIVATE);
		jws.tests.FileSystem.testFileExists("privateDir", this.TEST_FILE_NAME, true);
		jws.tests.FileSystem.testGetFilelist("publicDir", ["*.txt"], true, [this.TEST_FILE_NAME]);
		jws.tests.FileSystem.testGetFilelist("privateDir", ["*.txt"], true, [this.TEST_FILE_NAME]);
		jws.tests.FileSystem.testFileSend(this.TEST_FILE_NAME, this.TEST_FILE_DATA);
		jws.tests.FileSystem.testFileDelete(this.TEST_FILE_NAME, true, 0);
		jws.tests.FileSystem.testFileDelete(this.TEST_FILE_NAME, true, -1);
	},

	runSuite: function() {
		var lThis = this;
		describe( "Performing test suite: " + this.NS + "...", function () {
			lThis.runSpecs();
		});
	}	

};