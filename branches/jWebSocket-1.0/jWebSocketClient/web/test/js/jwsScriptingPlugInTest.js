//	---------------------------------------------------------------------------
//	jWebSocket ScriptingPlugIn test specs (Community Edition, CE)
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

jws.tests.Scripting = {
	title: "Scripting plug-in",
	description: "jWebSocket Scripting plug-in for server side script apps.",
	category: "System",
	scriptApps: {},
	TEST_APP1: "demoApp",
	TEST_APP2: "someApp",
	testListScriptApps: function() {
		var self = this;
		var lSpec = "getting script apps list";

		it(lSpec, function() {
			var lResponse = null;
			jws.Tests.getAdminTestConn().listScriptApps({userOnly: true, namesOnly: false,
				OnResponse: function(aResponse) {
					lResponse = aResponse;
					self.scriptApps = aResponse.data;
				}
			});

			waitsFor(
					function() {
						return null !== lResponse;
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(null !== lResponse.data[self.TEST_APP1]).toEqual(true);
				expect(null !== lResponse.data[self.TEST_APP2]).toEqual(true);
			});
		});
	},
	testGetVersion: function(aAppName) {
		var self = this;
		var lSpec = "getting script app version '" + aAppName + "'";

		it(lSpec, function() {
			var lResponse = null;
			jws.Tests.getAdminTestConn().getScriptAppVersion(aAppName, {
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});

			waitsFor(
					function() {
						return null !== lResponse;
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(self.scriptApps[aAppName].version).toEqual(lResponse.version);
			});
		});
	},
	testGetManifest: function(aAppName) {
		var self = this;
		var lSpec = "getting script app manifest '" + aAppName + "'";

		it(lSpec, function() {
			var lResponse = null;
			jws.Tests.getAdminTestConn().getScriptAppManifest(aAppName, {
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});

			waitsFor(
					function() {
						return null !== lResponse;
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(undefined !== lResponse.data.jws_version).toEqual(true);
				expect(undefined !== lResponse.data.language_ext).toEqual(true);
				expect(undefined !== lResponse.data.jws_dependencies).toEqual(true);
				expect(undefined !== lResponse.data.author).toEqual(true);
				expect(undefined !== lResponse.data.permissions).toEqual(true);
			});
		});
	},
	testReload: function(aAppName, aHotReload, aExpectedCode) {
		var self = this;
		var lSpec = "reloading script app '" + aAppName + "', hot reload: " + aHotReload
				+ ", expected code: " + aExpectedCode;

		it(lSpec, function() {
			var lResponse = null;
			jws.Tests.getAdminTestConn().reloadScriptApp(aAppName, aHotReload, {
				OnResponse: function(aResponse) {
					lResponse = aResponse;
				}
			});

			waitsFor(
					function() {
						return null !== lResponse;
					},
					lSpec,
					3000
					);

			runs(function() {
				expect(lResponse.code).toEqual(aExpectedCode);
			});
		});
	},
	testGetScriptApp: function() {
		var self = this;
		var lSpec = "generating script app '" + self.TEST_APP1;

		it(lSpec, function() {
			var lApp = null;
			var lResponse = null;
			jws.Tests.getAdminTestConn().getScriptApp(self.TEST_APP1, function(aApp) {
				lApp = aApp;

				// calling method toMap on Main controller
				lApp.Main.toMap(function(aResponse) {
					lResponse = aResponse;
				});
			});

			waitsFor(
					function() {
						return null !== lResponse;
					},
					lSpec,
					3000
					);


			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.result.name).toEqual("Rolando SM");
				expect(lResponse.result.email).toEqual("rsantamaria@jwebsocket.org");
				expect(lResponse.result.age).toEqual(28);
			});
		});
	},
	runSpecs: function() {
		// test list apps
		this.testListScriptApps();
		// test get script app version
		this.testGetVersion(this.TEST_APP1);
		this.testGetVersion(this.TEST_APP2);
		// test get script app manifest
		this.testGetManifest(this.TEST_APP1);
		this.testGetManifest(this.TEST_APP2);
		// test script app reloads
		this.testReload(this.TEST_APP1, true, 0);
		this.testReload(this.TEST_APP1, false, 0);
		this.testReload(this.TEST_APP2, true, 0);
		this.testReload(this.TEST_APP2, false, 0);
		this.testReload("InvalidAppName", false, -1);
		// test client side apps generation
		this.testGetScriptApp();
	}
};