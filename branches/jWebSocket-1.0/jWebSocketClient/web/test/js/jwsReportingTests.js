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

// requires web/res/js/jwsReportingPlugIn.js previously loaded
jws.tests.Reporting = {
    title: "Reporting plug-in",
    description: "jWebSocket reporting plug-in for application reports generation",
    category: "Community Edition",
    // this spec tests the 'get report templates' feature 
    testGetReports: function() {

        var lSpec = "get report templates(admin)";
        it(lSpec, function() {
            var lResponse = null;

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().reportingGetReports({
                OnResponse: function(aToken) {
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
                expect(lResponse.data.indexOf("jWebSocketContactReport") > -1).toEqual(true);
            });

        });
    },
    // this spec tests the generateReport feature
    testGenerateReport: function(aReportName, aParams, aFields) {

        var lSpec = "generateReport(" + aReportName + "," + aFields + "," + aParams + ")";
        it(lSpec, function() {
            var lResponse = null;

            // perform the generate reports on the server
            jws.Tests.getAdminTestConn().reportingGenerateReport(
                    aReportName,
                    aParams,
                    aFields,
                    {
                        OnResponse: function(aToken) {
                            lResponse = aToken;
                        }
                    }
            );

            // wait for result, consider reasonable timeout
            waitsFor(
                    function() {
                        return(null != lResponse);
                    },
                    lSpec,
                    5000
                    );

            // check the result 
            runs(function() {
                expect(lResponse.code).toEqual(0);
                expect(lResponse.path.indexOf("jWebSocketContactReport.pdf") > -1);

                jws.Tests.getAdminTestConn().fileLoad(lResponse.path, jws.FileSystemPlugIn.ALIAS_PRIVATE, {
                    OnSuccess: function(aToken) {
                        if (!jws.isIExplorer())
                            window.open("data:application/pdf;base64," + aToken.data, "_blank");
                    }
                });
            });
        });
    },
    testGenerateJDBCReport: function(aReportName, aParams, aFields) {

        var lSpec = "generateReport(" + aReportName + "," + aFields + "," + aParams + ")";
        it(lSpec, function() {
            var lResponse = null;

            // perform the generate reports on the server
            jws.Tests.getAdminTestConn().reportingGenerateReport(
                    aReportName,
                    aParams,
                    aFields,
                    {
                        useJDBCConnection: true,
                        connectionAlias: "default",
                        outputType: "pdf",
                        OnResponse: function(aToken) {
                            lResponse = aToken;
                        }
                    }
            );

            // wait for result, consider reasonable timeout
            waitsFor(
                    function() {
                        return(null != lResponse);
                    },
                    lSpec,
                    5000
                    );

            // check the result 
            runs(function() {
                expect(lResponse.code).toEqual(0);
                expect(lResponse.path.indexOf("jWebSocketContactReport.pdf") > -1);

                jws.Tests.getAdminTestConn().fileLoad(lResponse.path, jws.FileSystemPlugIn.ALIAS_PRIVATE, {
                    OnSuccess: function(aToken) {
                        if (!jws.isIExplorer())
                            window.open("data:application/pdf;base64," + aToken.data, "_blank");
                    }
                });
            });
        });
    },
    runSpecs: function() {
        this.testGetReports();

        // generate report calling args
        var lReportName = "jWebSocketContactReport";
        var lParams = {
            reportTitle: 'jWebSocket Contact Report'
        };
        var lFields = [
            {
                name: 'Alexander',
                lastName: 'Schulze',
                age: 40,
                email: 'a.schulze@jwebsocket.org'
            },
            {
                name: 'Rolando',
                lastName: 'Santamaria Maso',
                age: 27,
                email: 'rsantamaria@jwebsocket.org'
            },
            {
                name: 'Lisdey',
                lastName: 'Perez',
                age: 27,
                email: 'lperez@jwebsocket.org'
            },
            {
                name: 'Marcos',
                lastName: 'Gonzalez',
                age: 27,
                email: 'mgonzalez@jwebsocket.org,'
            },
            {
                name: 'Osvaldo',
                lastName: 'Aguilar',
                age: 27,
                email: 'oaguilar@jwebsocket.org,'
            },
            {
                name: 'Victor',
                lastName: 'Barzana',
                age: 27,
                email: 'vbarzana@jwebsocket.org,'
            },
            {
                name: 'Javier Alejandro',
                lastName: 'Puentes Serrano',
                age: 27,
                email: 'jpuentes@jwebsocket.org'
            }];

        this.testGenerateReport(lReportName, lParams, lFields);
        // generate report with jdbc connection
        lReportName = "JDBCExampleReport";

        // generating/creating the report
        this.testGenerateJDBCReport(lReportName, null, null);
    }
};