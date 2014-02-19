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


jws.tests.Quota = {
    title: "Quota plug-in",
    description: "jWebSocket Quota plug-in automated functional tests",
    category: "Community Edition",
    NS: "jws.tests.quota",
    NS_PLUGIN: "org.jwebsocket.plugins.quota",
    NS_QUOTA_TEST: "org.jwebsocket.plugins.testingQuota",
    //Quota detail for testing
    QUOTA_IDENTIFIER: "CountDown",
    QUOTA_INSTANCE: "defaultUser",
    QUOTA_INSTANCE_TYPE: "Group",
    QUOTA_ACTIONS: "*",
    QUOTA_TEST_UUID: "aa51e83898192632ac11b8e509e4959d",
    QUOTA_INSTANCE_REG: "oaguilar",
    // this spec tests the 'get report templates' feature 
    testCreateQuota: function(aQuotaType, aValue, aInstance, aInstanceType,
            aActions, aExpectedCode) {

        var lMe = this;

        var lSpec = this.NS + ": create quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            var lToken = {
                ns: lMe.NS_PLUGIN,
                type: "createQuota",
                namespace: lMe.NS_QUOTA_TEST,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aQuotaType,
                actions: aActions,
                value: '5',
                uuid: lMe.QUOTA_TEST_UUID
            };

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().sendToken(lToken, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });

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
                expect(aExpectedCode).toEqual(lResponse.code);
            });
        });
    },
    testGetQuota: function(aQuotaType, aInstance, aInstanceType,
            aActions, aExpectedCode) {

        var lMe = this;

        var lSpec = this.NS + ": get quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            var lToken = {
                ns: lMe.NS_PLUGIN,
                type: "getQuota",
                namespace: lMe.NS_QUOTA_TEST,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aQuotaType,
                actions: aActions
            };

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().sendToken(lToken, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });

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
                expect(aExpectedCode).toEqual(lResponse.code);
                expect("*").toEqual(lResponse.actions);
                expect("defaultUser").toEqual(lResponse.instance);
                expect(lMe.QUOTA_TEST_UUID).toEqual(lResponse.uuid);
            });
        });
    },
    testUnregisterQuota: function(aQuotaType, aInstance, aUuid, aExpectedCode) {


        var lMe = this;

        var lSpec = this.NS + ": unregister quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            var lToken = {
                ns: lMe.NS_PLUGIN,
                type: "unregisterQuota",
                instance: aInstance,
                identifier: aQuotaType,
                uuid: aUuid
            };

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().sendToken(lToken, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });

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
                expect(aExpectedCode).toEqual(lResponse.code);
                //expect(lResponse.).toEqual(lResponse.code);
            });

        });
    },
    /**
     * 
     * @param {type} aQuotaType
     * @param {type} aInstance
     * @param {type} aInstance_type
     * @param {type} aUuid
     * @param {type} aExpectedCode
     * @returns {undefined}
     */
    testRegisterQuota: function(aQuotaType, aInstance, aInstance_type, aUuid, aExpectedCode) {

        var lMe = this;

        var lSpec = this.NS + ": register quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            var lToken = {
                ns: lMe.NS_PLUGIN,
                type: "registerQuota",
                instance: aInstance,
                identifier: aQuotaType,
                instance_type: aInstance_type,
                uuid: aUuid
            };

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().sendToken(lToken, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });

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
                expect(aExpectedCode).toEqual(lResponse.code);
                expect(lMe.QUOTA_INSTANCE_REG).toEqual(lResponse.instance);
                expect(lMe.QUOTA_TEST_UUID).toEqual(lResponse.uuid);
                //expect(lResponse.).toEqual(lResponse.code);
            });
        });
    },
    /**
     * 
     * @param {type} aQuotaType
     * @param {type} aInstance
     * @param {type} aInstanceType
     * @param {type} aValue
     * @param {type} ExpectedValue
     * @param {type} aExpectedCode
     * @returns {undefined}
     */
    testReduceQuota: function(aQuotaType, aInstance, aInstanceType, aActions,
            aValue, ExpectedValue, aExpectedCode) {
        var lMe = this;

        var lSpec = this.NS + ": reduce quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            var lToken = {
                ns: lMe.NS_PLUGIN,
                type: "reduceQuota",
                namespace: lMe.NS_QUOTA_TEST,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aQuotaType,
                actions: aActions,
                value: aValue
            };

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().sendToken(lToken, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });

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
                expect(aExpectedCode).toEqual(lResponse.code);
                expect(ExpectedValue).toEqual(lResponse.value);
                //expect(lResponse.).toEqual(lResponse.code);
            });
        });
    },
    testSetQuota: function(aQuotaType, aInstance, aInstanceType, aActions,
            aValue, ExpectedValue, aExpectedCode) {
        var lMe = this;

        var lSpec = this.NS + ": set quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            var lToken = {
                ns: lMe.NS_PLUGIN,
                type: "setQuota",
                namespace: lMe.NS_QUOTA_TEST,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aQuotaType,
                actions: aActions,
                value: aValue
            };

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().sendToken(lToken, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });

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
                expect(aExpectedCode).toEqual(lResponse.code);
                expect(ExpectedValue).toEqual(lResponse.value);
                //expect(lResponse.).toEqual(lResponse.code);
            });
        });
    }, 
    testIncreaseQuota: function(aQuotaType, aInstance, aInstanceType, aActions,
            aValue, ExpectedValue, aExpectedCode) {
        var lMe = this;

        var lSpec = this.NS + ": increase quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            var lToken = {
                ns: lMe.NS_PLUGIN,
                type: "increaseQuota",
                namespace: lMe.NS_QUOTA_TEST,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aQuotaType,
                actions: aActions,
                value: aValue
            };

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().sendToken(lToken, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });

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
                expect(aExpectedCode).toEqual(lResponse.code);
                expect(ExpectedValue).toEqual(lResponse.value);
                //expect(lResponse.).toEqual(lResponse.code);
            });
        });
    },
    runSpecs: function() {

        //run alls tests within an outer test suite
        //create a temporary quota to testing all operations with
        this.testCreateQuota(this.QUOTA_IDENTIFIER, 5, this.QUOTA_INSTANCE,
                this.QUOTA_INSTANCE_TYPE, this.QUOTA_ACTIONS, 0);

        this.testCreateQuota(this.QUOTA_IDENTIFIER, 5, this.QUOTA_INSTANCE,
                this.QUOTA_INSTANCE_TYPE, this.QUOTA_ACTIONS, -1);

        this.testGetQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE,
                this.QUOTA_INSTANCE_TYPE, this.QUOTA_ACTIONS, 0);

        this.testRegisterQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                "User", this.QUOTA_TEST_UUID, 0);

        this.testReduceQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                "User", this.QUOTA_ACTIONS, 2, 3, 0);

        this.testSetQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                "User", this.QUOTA_ACTIONS, 5, 5, 0);
                
        this.testIncreaseQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                "User", this.QUOTA_ACTIONS, 2, 7, 0);

        this.testUnregisterQuota( this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE, 
                this.QUOTA_TEST_UUID, 0); 

    }
};