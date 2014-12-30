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
    QUOTA_INSTANCE_REG: "guest",
    refObject: {},
    // this spec tests the 'get report templates' feature
    testCreateQuota: function(aIdentifier, aValue, aInstance, aInstanceType,
            aActions, aExpectedCode, aRefObject) {

        var lMe = this;
        var lSpec = this.NS + ": create quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            jws.Tests.getAdminTestConn().createQuota(
                    aIdentifier, lMe.NS_QUOTA_TEST,
                    aInstance, aInstanceType, aActions, aValue, {
                uuid: aRefObject.uuid,
                OnResponse: function(aToken) {
                    lResponse = aToken;
                    aRefObject.uuid = aToken.uuid;
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
                expect(aExpectedCode).toEqual(lResponse.code);
            });
        });
    },
    testGetQuota: function(aIdentifier, aInstance, aInstanceType,
            aActions, aExpectedCode, aRefObject) {

        var lMe = this;
        var lSpec = this.NS + ": get quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            jws.Tests.getAdminTestConn().getQuota(aIdentifier, lMe.NS_QUOTA_TEST,
                    aInstance, aInstanceType, aActions, {
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
                    lSpec, 1500
                    );

            // check the result 
            runs(function() {
                expect(aExpectedCode).toEqual(lResponse.code);
                expect("*").toEqual(lResponse.actions);
                expect("defaultUser").toEqual(lResponse.instance);
                expect(aRefObject.uuid).toEqual(lResponse.uuid);
            });
        });
    },
    testUnregisterQuota: function(aIdentifier, aInstance, aExpectedCode, aRefObject) {

        var lSpec = this.NS + ": unregister quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().unregisterQuota(aIdentifier, aInstance,
                    aRefObject.uuid, {
                OnResponse: function(aToken) {
                    lResponse = aToken;
                }
            });
            // wait for result, consider reasonable timeout
            waitsFor(
                    function() {
                        // check response
                        return(null != lResponse);
                    }, lSpec, 1500
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
     * @param {type} aIdentifier
     * @param {type} aInstance
     * @param {type} aInstance_type
     * @param {type} aExpectedCode
     * @returns {undefined}
     */
    testRegisterQuota: function(aIdentifier, aInstance, aInstance_type,
            aExpectedCode, aRefObject) {

        var lMe = this;
        var lSpec = this.NS + ": register quota (admin)";
        it(lSpec, function() {
            var lResponse = null;

            //retgister the quota on the server
            jws.Tests.getAdminTestConn().registerQuota(aIdentifier, aInstance,
                    aRefObject.uuid, {
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
                expect(aRefObject.uuid).toEqual(lResponse.uuid);
                //expect(lResponse.).toEqual(lResponse.code);
            });
        });
    },
    /**
     * 
     * @param {type} aIdentifier
     * @param {type} aInstance
     * @param {type} aInstanceType
     * @param {type} aValue
     * @param {type} ExpectedValue
     * @param {type} aExpectedCode
     * @returns {undefined}
     */
    testReduceQuota: function(aIdentifier, aInstance, aInstanceType, aActions,
            aValue, ExpectedValue, aExpectedCode) {

        var lMe = this;
        var lSpec = this.NS + ": reduce quota (admin)";
        it(lSpec, function() {
            var lResponse = null;
            
            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().reduceQuota(aIdentifier,
                    lMe.NS_QUOTA_TEST, aInstance ,aInstanceType, aActions, aValue, {
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
            });
        });
    },
    testReduceQuotaByUuid: function(aIdentifier, aInstance, aValue, ExpectedValue,
            aExpectedCode, aRefObject) {

        var lSpec = this.NS + ": reduce quota by uuid (admin)";
        it(lSpec, function() {
            var lResponse = null;

            //aIdentifier, aInstance, aUuid, aValue
            // perform the get report templates feature on the server
            jws.Tests.getAdminTestConn().reduceQuotaByUuid(aIdentifier, aInstance,
                    aRefObject.uuid,aValue, {
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
    testSetQuota: function(aIdentifier, aInstance, aInstanceType, aActions,
            aValue, ExpectedValue, aExpectedCode) {
        var lMe = this;

        var lSpec = this.NS + ": set quota (admin)";
        it(lSpec, function() {

            var lResponse = null;
            
            
            //aIdentifier, aInstance, aUuid, aValue
            jws.Tests.getAdminTestConn().setQuota(aIdentifier, lMe.NS_QUOTA_TEST,
                aInstance, aInstanceType, aActions, aValue,{
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
    testSetQuotaByUuid: function(aIdentifier, aInstance, aValue, ExpectedValue,
            aExpectedCode, aRefObject) {

        var lSpec = this.NS + ": set quota by uuid (admin)";
        it(lSpec, function() {
            var lResponse = null;

            
            jws.Tests.getAdminTestConn().setQuotaByUuid(aIdentifier,aInstance,
                aRefObject.uuid, aValue,{
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
    testIncreaseQuotaByUuid: function(aIdentifier, aInstance, aValue, ExpectedValue,
            aExpectedCode, aRefObject) {

        var lSpec = this.NS + ": increase quota by uuid (admin)";
        it(lSpec, function() {
            var lResponse = null;

            jws.Tests.getAdminTestConn().increaseQuotaByUuid(aIdentifier, 
                    aInstance, aRefObject.uuid, aValue,{
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
    testIncreaseQuota: function(aIdentifier, aInstance, aInstanceType, aActions,
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
                identifier: aIdentifier,
                actions: aActions,
                value: aValue
            };

            jws.Tests.getAdminTestConn().increaseQuota( aIdentifier, lMe.NS_QUOTA_TEST,
                    aInstance, aInstanceType, aActions, aValue,{
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
                this.QUOTA_INSTANCE_TYPE, this.QUOTA_ACTIONS, 0, this.refObject);

        this.testCreateQuota(this.QUOTA_IDENTIFIER, 5, this.QUOTA_INSTANCE,
         this.QUOTA_INSTANCE_TYPE, this.QUOTA_ACTIONS, -1, this.refObject);

        this.testGetQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE,
                this.QUOTA_INSTANCE_TYPE, this.QUOTA_ACTIONS, 0, this.refObject);

        this.testRegisterQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                "User", 0, this.refObject);

        this.testReduceQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                "User", this.QUOTA_ACTIONS, 2, 3, 0);

        this.testReduceQuotaByUuid(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                1, 2, 0, this.refObject);

        this.testSetQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE,
                this.QUOTA_INSTANCE_TYPE, this.QUOTA_ACTIONS, 5, 5, 0);
        
        this.testSetQuotaByUuid(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                10, 10, 0, this.refObject);
               
        this.testIncreaseQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                "User", this.QUOTA_ACTIONS, 2, 12, 0);

        this.testIncreaseQuotaByUuid(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE_REG,
                3, 15, 0, this.refObject);

        this.testUnregisterQuota(this.QUOTA_IDENTIFIER, this.QUOTA_INSTANCE,
                0, this.refObject);
    }
};