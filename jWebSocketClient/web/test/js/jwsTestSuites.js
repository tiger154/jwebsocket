//	---------------------------------------------------------------------------
//	jWebSocket Jasmine Test Suites (Community Edition, CE)
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

function runFullTestSuite(aArgs) {
	/*
	 debugger;
	 jasmine.VERBOSE = true;
	 */
	var lIntv = jasmine.DEFAULT_UPDATE_INTERVAL;
	jasmine.DEFAULT_UPDATE_INTERVAL = 5;

	var lIncreaseTimeoutFactors = {
		generic: 3,
		generic_debug: 5,
		normal: 1,
		slow: 3,
		very_slow: 5,
		fast: 0.7,
		ultra_fast: 0.3,
		fastest: 0.08
	};
	jasmine.INCREASE_TIMEOUT_FACTOR = lIncreaseTimeoutFactors[aArgs.speed] || 1;


	describe("jWebSocket Test Suite", function() {

		var lTestSSL = $('#tls_set').val() === 'wss';
		// open connections for admin and guest

		if ($("#test_set").val() !== "REST") {
			describe("Opening shared connections...", function() {
				jws.Tests.testOpenSharedAdminConn();
				jws.Tests.testOpenSharedGuestConn();
				if (lTestSSL) {
					jws.Tests.testOpenSharedAdminConnSSL();
					jws.Tests.testOpenSharedGuestConnSSL();
				}
			});
		}

		// running selected tests
		for (var lIndex in aArgs.tests) {
			var lTestName = aArgs.tests[lIndex];
			describe("Performing test suite: jws.tests." + lTestName + "", function() {
				jws.tests[lTestName].runSpecs();
			});
		}

		// close connections for admin and guest
		if ($("#test_set").val() !== "REST") {
			describe("Closing shared connections...", function() {
				jws.Tests.testCloseSharedAdminConn();
				jws.Tests.testCloseSharedGuestConn();
				if (lTestSSL) {
					jws.Tests.testCloseSharedAdminConnSSL();
					jws.Tests.testCloseSharedGuestConnSSL();
				}
			});
		}
		jasmine.DEFAULT_UPDATE_INTERVAL = lIntv;
	});
}


var DEFAULT_CATEGORY = "UNCATEGORIZED";
var DEFAULT_PRIORITY = 100;
var DEFAULT_ENABLED = true;


function initTestsIndex() {
	var lCategories = [];
	var lSortedTests = [];

	for (var lTestName in jws.tests) {
		var lTest = jws.tests[lTestName];
		// setting test unique identifier
		lTest.id = lTestName;

		// setting default values for convenience
		if (undefined == lTest['category'])
			lTest['category'] = DEFAULT_CATEGORY;
		if (undefined == lTest['priority'])
			lTest['priority'] = DEFAULT_PRIORITY;
		if (undefined == lTest['enabled'])
			lTest['enabled'] = DEFAULT_ENABLED;

		// getting categories set
		if (lCategories.indexOf(lTest.category) == -1) {
			lCategories.push(lTest.category);
		}

		lSortedTests.push(lTest);
	}

	// sorting ascending
	lCategories.sort();
	lSortedTests.sort(function(t1, t2) {
		if (t1.priority == t2.priority)
			return 0;
		if (t1.priority > t2.priority)
			return 1;

		return -1;
	});

	// returns index object
	return {
		categories: lCategories,
		tests: lSortedTests,
		getTestsByCategory: function(aCategory) {
			var lTests = [];
			if ("__ALL__" == aCategory)
				return this.tests;

			for (var lIndex in this.tests) {
				if (this.tests[lIndex].category == aCategory) {
					lTests.push(this.tests[lIndex]);
				}
			}

			return lTests;
		}
	};
}

function renderTests(aTests, aDiv) {
	var lHtml = "";
	for (var lIndex in aTests) {
		var lT = aTests[lIndex];
		lHtml += "<span title='" + lT.description + "'><label><input id='"
				+ lT.id + "'"
				+ ((lT.enabled) ? " checked='checked' " : " disabled")
				+ " type='checkbox'> " + lT.title + "</label></span></br>";
	}

	aDiv.html(lHtml);
}
