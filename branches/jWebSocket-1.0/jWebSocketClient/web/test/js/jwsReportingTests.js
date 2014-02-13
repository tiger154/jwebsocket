//	---------------------------------------------------------------------------
//	jWebSocket Reporting Plug-in test specs (Community Edition, CE)
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


jws.tests.Reporting = {
	NS: "jws.tests.reporting",
	title: "Reporting plug-in",
	description: "jWebSocket reporting plug-in for application reports generation",
	category: "Community Edition",
	// this spec tests the 'get report templates' feature of reporting plug-in
	testGetReports: function() {

		var lSpec = this.NS + ": get report templates(admin)";
		it(lSpec, function() {

			// init response
			var lResponse = null;

			// perform the get report templates...
			jws.Tests.getAdminTestConn().reportingGetReports(
					{OnResponse: function(aToken) {
							lResponse = aToken;
						}
					}
			);

			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						// check response
						return(null != lResponse);
					},
					lSpec,
					1500
					);

			// check the result 
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual("ok");
				expect(lResponse.data.length >= 3).toEqual(true);
				expect(lResponse.data.indexOf("UserList") > -1).toEqual(true);
				expect(lResponse.data.indexOf("UserSMSReport") > -1).toEqual(true);
				expect(lResponse.data.indexOf("UserRolesRights") > -1).toEqual(true);
			});

		});
	},
	// this spec tests the generateReport feature
	testGenerateReport: function(aReportName, aParams, aFields) {

		var lSpec = this.NS + ": generateReport(" + aReportName + "," + aFields + "," + aParams + ")";
		it(lSpec, function() {

			// init response
			var lResponse = null;


			// perform the generate reports...
			jws.Tests.getAdminTestConn().reportingGenerateReport(
					aReportName,
					aFields,
					aParams,
					{OnResponse: function(aToken) {
							lResponse = aToken;
						}
					}
			);

			// wait for result, consider reasonable timeout
			waitsFor(
					function() {
						// check response
						return(null != lResponse);
					},
					lSpec,
					1500
					);

			// check the result 
			runs(function() {
				expect(lResponse.code).toEqual(0);
				expect(lResponse.msg).toEqual("ok");
				var lCont = 10;
				var lReportName = "";
				while (lCont < 20) {
					lReportName += lResponse.path[lCont++];
				}
				expect(lReportName).toEqual("person.pdf");
			});

		});
	},
	runSpecs: function() {
		this.testGetReports();

		// args
		var lReportName = "person";
		var lParams = "{reportTitle: 'My Report'}";
		var lFields = "[{name: 'Javier', lastName: 'Alejandro Puentes', age: 27, dni: 9898797987}]";

		this.testGenerateReport(lReportName, lParams, lFields);
	},
	runSuite: function() {
		// run alls tests as a separate test suite
		var lThis = this;
		describe("Performing test suite: " + this.NS + "...", function() {
			lThis.runSpecs();
		});
	}

};