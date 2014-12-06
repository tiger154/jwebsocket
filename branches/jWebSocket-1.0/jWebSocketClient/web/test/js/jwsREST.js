//	---------------------------------------------------------------------------
//	jWebSocket REST Engine test specs (Community Edition, CE)
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

jws.tests.REST = {
	title: "REST support (http://localhost:8787/jWebSocket/http)",
	description: "jWebSocket REST support for remote interaction with the jWebSocket server infrastructure",
	category: "REST",
	connectionId: new Date().getTime(),
	getURL: function() {
		return "http://localhost:8787/jWebSocket/http?connectionId=" + jws.tests.REST.connectionId;
	},
	// this spec tests the 'open' feature 
	testOpen: function() {

		var lSpec = "open connection";
		it(lSpec, function() {
			var lResponse = null;
			$.getJSON(jws.tests.REST.getURL() + "&action=open", function(aJson) {
				lResponse = aJson;
			});

			// wait for result, consider reasonable timeout
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 1500);

			// check the result 
			runs(function() {
				expect(true).toEqual(true);
			});

		});
	},
	// this spec tests the 'login' feature 
	testLogin: function() {

		var lSpec = "login (root, root)";
		it(lSpec, function() {
			var lResponse = null;
			$.getJSON(jws.tests.REST.getURL() + "&action=login&username=root&password=root", function(aJson) {
				lResponse = aJson;
			});

			// wait for result, consider reasonable timeout
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 1500);

			// check the result 
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.username).toEqual("root");
			});

		});
	},
	// this spec tests the 'sync' feature 
	testSync: function() {

		var lSpec = "sync";
		it(lSpec, function() {
			var lResponse = null;
			$.getJSON(jws.tests.REST.getURL() + "&action=sync", function(aJson) {
				lResponse = aJson;
			});

			// wait for result, consider reasonable timeout
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 1500);

			// check the result 
			runs(function() {
				expect(jws.tools.getType(lResponse)).toEqual("array");
				expect(lResponse.length > 0).toEqual(true);
			});

		});
	},
	// this spec tests the 'login' feature 
	testSend: function() {

		var lSpec = "send (echo)";
		it(lSpec, function() {
			var lResponse = null;
			$.getJSON(jws.tests.REST.getURL() + "&action=send&data=" + JSON.stringify({
				ns: "org.jwebsocket.plugins.system",
				type: "echo",
				data: "REST"}), function(aJson) {
				lResponse = aJson;
			});

			// wait for result, consider reasonable timeout
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 1500);

			// check the result 
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.data).toEqual("REST");
			});

		});
	},
	// this spec tests the 'logout' feature 
	testLogout: function() {

		var lSpec = "logout (root)";
		it(lSpec, function() {
			var lResponse = null;
			$.getJSON(jws.tests.REST.getURL() + "&action=logout", function(aJson) {
				lResponse = aJson;
			});

			// wait for result, consider reasonable timeout
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 1500);

			// check the result 
			runs(function() {
				expect(lResponse.code).toEqual(0);
			});

		});
	},
	// this spec tests the 'close' feature 
	testClose: function() {

		var lSpec = "close connection";
		it(lSpec, function() {
			var lResponse = null;
			$.get(jws.tests.REST.getURL() + "&action=close", function(aResponse) {
				lResponse = aResponse;
			});

			// wait for result, consider reasonable timeout
			waitsFor(function() {
				return(null !== lResponse);
			}, lSpec, 1500);

			// check the result 
			runs(function() {
				expect(lResponse).toEqual("http.command.close");
			});

		});
	},
	runSpecs: function() {
		this.testOpen();
		this.testLogin();
		this.testSync();
		this.testSend();
		this.testLogout();
		this.testClose();
	}
};