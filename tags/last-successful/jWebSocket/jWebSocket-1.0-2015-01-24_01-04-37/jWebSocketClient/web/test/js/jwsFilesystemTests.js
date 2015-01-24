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
	TEST_BIG_FILE_NAME: "base64BigFile.txt",
	TEST_BIG_FILE_DATA: "This example is based on the File System PlugIn which allows to submit and receive from the server base64 encoded files.",
	testFileSave: function(aFilename, aData, aScope) {
		var lSpec = "FileSave (admin, " + aFilename + ", " + aScope + ")";
		var lData = aData;
		var lFilename = aFilename;

		it(lSpec, function() {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileSave(lFilename, lData, {
				encode: true,
				encoding: 'zipBase64',
				scope: aScope,
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});

			waitsFor(
					function() {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(lResponse.code).toEqual(0);
			});

		});
	},
	testFileSend: function(aFilename, aData) {
		var lSpec = "FileSend (admin, " + aFilename + ")";

		it(lSpec, function() {

			var lResponse = null;
			jws.Tests.getAdminTestConn().setFileSystemCallbacks({
				OnFileReceived: function(aToken) {
					lResponse = aToken;
				}
			});
			jws.Tests.getAdminTestConn().fileSend(jws.Tests.getAdminTestConn().getId(), aFilename, aData, {
				encoding: "base64"
			});

			waitsFor(
					function() {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(lResponse.filename).toEqual(aFilename);
				expect(lResponse.data).toEqual(aData);
			});

		});
	},
	testGetFilelist: function(aAlias, aFilemasks, aRecursive, aExpectedList) {
		var lSpec = "GetFilelist (admin, " + aAlias + ", " +
				JSON.stringify(aFilemasks) + ", " + aRecursive + ")";

		it(lSpec, function() {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileGetFilelist(aAlias, aFilemasks, {
				recursive: aRecursive,
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});

			waitsFor(
					function() {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(lResponse.code).toEqual(0);

				var lObtainedKeys = [];
				lResponse.files.forEach(function(aItem) {
					lObtainedKeys.push(aItem.filename);
				});

				expect(lObtainedKeys.sort().join(",")).toContain(aExpectedList.sort().join(","));
			});

		});
	},
	testFileLoad: function(aFilename, aAlias, aExpectedData) {
		var lSpec = "FileLoad (admin, " + aFilename + ", " + aAlias + ")";
		var lData = aExpectedData;
		var lFilename = aFilename;

		it(lSpec, function() {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileLoad(lFilename, aAlias, {
				decode: true,
				encoding: 'zipBase64',
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});

			waitsFor(
					function() {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(lResponse.data).toEqual(lData);
			});

		});
	},
	testBigFileLoad: function(aFilename, aAlias, aExpectedData, aIterations) {
		var lSpec = "Base64 big File Load (admin, " + aFilename + ", " + aAlias + ")";
		var lData = aExpectedData;
		var lFilename = aFilename;

		it(lSpec, function() {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileLoad(lFilename, aAlias, {
				decode: true,
				encoding: 'zipBase64',
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});

			waitsFor(
					function() {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(jws.tests.FileSystem.decodeBigFileData(lData, lResponse.data, aIterations)).toEqual(lData);
			});

		});
	},
	testFileDelete: function(aFilename, aForce, aExpectedCode) {
		var lSpec = "FileDelete (admin, " + aFilename + ", " + aExpectedCode + ")";

		it(lSpec, function() {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileDelete(aFilename, aForce, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});

			waitsFor(
					function() {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});

		});
	},
	testFileExists: function(aAlias, aFilename, aExpectedValue) {
		var lSpec = "FileExists (admin, " + aAlias + ", " + aFilename + ")";
		var lFilename = aFilename;
		var lAlias = aAlias;

		it(lSpec, function() {

			var lResponse = null;

			jws.Tests.getAdminTestConn().fileExists(lFilename, lAlias, {
				OnResponse: function(aToken) {
					lResponse = aToken;
				}
			});

			waitsFor(
					function() {
						return(null !== lResponse);
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.exists).toEqual(aExpectedValue);
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

		// testing big base64 data to the server
		jws.tests.FileSystem.testFileSave(
				this.TEST_BIG_FILE_NAME,
				this.generateBigFile(this.TEST_BIG_FILE_DATA, 1000),
				jws.SCOPE_PRIVATE);

		// Loading big base64 data from the server
		jws.tests.FileSystem.testBigFileLoad(
				this.TEST_BIG_FILE_NAME,
				jws.FileSystemPlugIn.ALIAS_PRIVATE,
				this.TEST_BIG_FILE_DATA, 
				1000);

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
	},
	/**
	 * This function simulates the creation of a file in memory
	 * @param {type} aData
	 * @param {type} aIterations
	 * @returns {@exp;Base64@pro;encode@pro;output|Base64@pro;_keyStr@call;charAt|String}
	 */
	generateBigFile: function(aData, aIterations) {
		var lIdx = 0, lResult = "";
		for (lIdx = 0; lIdx < aIterations; lIdx++) {
			lResult += aData;
		}
		return Base64.encode(lResult);
	},
	/**
	 * This functions reverts a big data coming from the server to see if it 
	 * matches with the original given data to be encoded
	 * @param {type} aOriginalData
	 * @param {type} aBigData
	 * @param {type} aIterations
	 * @returns {String} the original data encoded or the error data
	 */
	decodeBigFileData: function(aOriginalData, aBigData, aIterations) {
		var lIdx = 0,
				lResult = Base64.decode(aBigData),
				aOriginalDataLength = aOriginalData.length;

		for (lIdx = 0; lIdx < aIterations; lIdx++) {
			if (lResult.substr(aOriginalDataLength * lIdx, aOriginalDataLength) !== aOriginalData) {
				return lResult.substr(aOriginalDataLength * lIdx, aOriginalDataLength);
			}
			if (lIdx === aIterations - 1) {
				return aOriginalData;
			}
		}
		return aOriginalData;
	}
};