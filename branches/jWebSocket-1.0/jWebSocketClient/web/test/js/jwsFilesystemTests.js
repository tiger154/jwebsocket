//	---------------------------------------------------------------------------
//	jWebSocket Filesystem Plug-in CE test specs (Community Edition, CE)
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


jws.tests.FileSystem = {

	title: "FileSystem plug-in",
	description: "jWebSocket filesystem plug-in. Designed for files management on the server.",
	category: "Community Edition",
	
	TEST_FILE_DATA: "This is a string to be saved into the test file!",
	TEST_FOLDER: "privFolder",
	TEST_FILE_NAME: "test.txt",

	testFileSave: function(aFilename, aData, aScope) {
		var lSpec = "FileSave (admin, " + aFilename + ", " + aScope + ")";
		var lData = aData;
		var lFilename = aFilename;
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileSave( lFilename, lData, {
				encode: true,
				encoding: 'zipBase64',
				scope: aScope,
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null !== lResponse );
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
		var lSpec = "FileSend (admin, " + aFilename + ")";
		
		it( lSpec, function () {

			var lResponse = null;
			jws.Tests.getAdminTestConn().setFileSystemCallbacks({
				OnFileReceived: function(aToken){
					lResponse = aToken;
				}
			});
			jws.Tests.getAdminTestConn().fileSend( jws.Tests.getAdminTestConn().getId(), aFilename, aData, {
				encoding: "base64"
			});

			waitsFor(
				function() {
					return( null !== lResponse );
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
		var lSpec = "GetFilelist (admin, " + aAlias + ", " + 
		JSON.stringify(aFilemasks) + ", " + aRecursive + ")";
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileGetFilelist( aAlias, aFilemasks, {
				recursive: aRecursive,
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null !== lResponse );
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
					
				expect( lObtainedKeys.sort().join(",") ).toContain( aExpectedList.sort().join(",") );
			});

		});
	},

	testFileLoad: function(aFilename, aAlias, aExpectedData) {
		var lSpec = "FileLoad (admin, " + aFilename + ", " + aAlias + ")";
		var lData = aExpectedData;
		var lFilename = aFilename;
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileLoad( lFilename, aAlias, {
				decode: true,
				encoding: 'zipBase64',
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null !== lResponse );
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
		var lSpec = "FileDelete (admin, " + aFilename + ", " + aExpectedCode + ")";
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileDelete( aFilename, aForce, {
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null !== lResponse );
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
		var lSpec = "FileExists (admin, " + aAlias + ", " + aFilename + ")";
		var lFilename = aFilename;
		var lAlias = aAlias;
		
		it( lSpec, function () {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileExists( lFilename, lAlias, {
				OnResponse: function( aToken ) {
					lResponse = aToken;
				}
			});

			waitsFor(
				function() {
					return( null !== lResponse );
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
		jws.tests.FileSystem.testFileSave(
			this.TEST_FILE_NAME,
			this.TEST_FILE_DATA, 
			jws.SCOPE_PUBLIC);
		
		jws.tests.FileSystem.testFileLoad(
			this.TEST_FILE_NAME, 
			jws.FileSystemPlugIn.ALIAS_PUBLIC,
			this.TEST_FILE_DATA);
			
		jws.tests.FileSystem.testFileExists(
			jws.FileSystemPlugIn.ALIAS_PUBLIC, 
			this.TEST_FILE_NAME, 
			true);
		
		jws.tests.FileSystem.testFileExists(
			jws.FileSystemPlugIn.ALIAS_PRIVATE, 
			"unexisting_file.txt", 
			false);
		
		jws.tests.FileSystem.testFileSave(
			this.TEST_FILE_NAME, 
			this.TEST_FILE_DATA, 
			jws.SCOPE_PRIVATE);
			
		jws.tests.FileSystem.testFileExists(
			jws.FileSystemPlugIn.ALIAS_PRIVATE, 
			this.TEST_FILE_NAME, 
			true);
		
		jws.tests.FileSystem.testGetFilelist(
			jws.FileSystemPlugIn.ALIAS_PUBLIC, 
			["*.txt"], 
			true, [this.TEST_FILE_NAME]);
		
		jws.tests.FileSystem.testGetFilelist(
			jws.FileSystemPlugIn.ALIAS_PRIVATE, 
			["*.txt"], 
			true, 
			[this.TEST_FILE_NAME]);
		
		jws.tests.FileSystem.testFileSend(this.TEST_FILE_NAME, this.TEST_FILE_DATA);
		jws.tests.FileSystem.testFileSend(this.TEST_FILE_NAME, this.TEST_FILE_DATA);
		jws.tests.FileSystem.testFileDelete(this.TEST_FILE_NAME, true, 0);
		jws.tests.FileSystem.testFileDelete(this.TEST_FILE_NAME, true, -1);
	}
};