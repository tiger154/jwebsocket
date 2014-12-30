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

jws.tests.Backbone = {
    title: "Backbone integration UserAdmin and Quota plug-in",
    description: "jWebSocket Backbone automated functional tests",
    category: "Community Edition",
    UseradminEE_NS: jws.NS_BASE + ".plugins.useradmin",
    QuotaPlugin_NS: jws.NS_BASE + ".plugins.quota",
    conn: new jws.jWebSocketJSONClient(),
    getConn: function () {
        return this.conn;
    },
    testCreateUser: function (aUser) {

        var lSpec = "test create User:" + aUser.username;
        it(lSpec, function () {

            var lResponse = null;
            var lToken = aUser;
            lToken.ns = jws.tests.Backbone.UseradminEE_NS;
            lToken.type = "addUser";
            jws.tests.Backbone.getConn().sendToken(lToken, {
                OnResponse: function (aToken) {
                    lResponse = aToken;
                    aUser.id = aToken.id;
                }
            });

            waits(2000);
            waitsFor(function () {
                return(null !== lResponse);
            }, lSpec, 3000);
            runs(function () {
                expect(lResponse.code).toEqual(0);
            });
        });
    },
    testCreateQuota: function (aIdentifier, aValue, aInstance, aInstanceType,
            aActions, aNamespace, aExpectedCode) {

        var lSpec = this.QuotaPlugin_NS + ": create quota (admin)";
        it(lSpec, function () {
            var lResponse = null;

            jws.Tests.getAdminTestConn().createQuota(
                    aIdentifier, aNamespace,
                    aInstance, aInstanceType, aActions, aValue, {
                        OnResponse: function (aToken) {
                            lResponse = aToken;
                        }
                    }
            );

            // wait for result, consider reasonable timeout
            waitsFor(
                    function () {
                        // check response
                        return(null != lResponse);
                    },
                    lSpec,
                    1500
                    );

            // check the result 
            runs(function () {
                // Not needed to check since the code could be -1 in case that 
                // the quota already exists
               // expect(aExpectedCode).toEqual(lResponse.code);
            });
        });
    },
    testAssignmentQuotaToNewUser: function (aIdentifier, aInstance, aInstanceType, aNamespace,
            aActions, aExpectedCode) {

        var lSpec = "test quota Assignment by the backbone to the user: " + aInstance;
        it(lSpec, function () {
            var lResponse = null;

            var lToken = {
                ns: jws.tests.Backbone.QuotaPlugin_NS,
                type: "getQuota",
                namespace: aNamespace,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aIdentifier,
                actions: aActions
            };

            jws.tests.Backbone.getConn().sendToken(lToken, {
                OnResponse: function (aToken) {
                    lResponse = aToken;

                    var lUnToken = {
                        ns: jws.tests.Backbone.QuotaPlugin_NS,
                        type: "unregisterQuota",
                        instance: lResponse.instance,
                        identifier: lResponse.identifier,
                        uuid: lResponse.uuid
                    };
                    jws.tests.Backbone.getConn().sendToken(lUnToken);
                }
            });

            // wait for result, consider reasonable timeout
            waitsFor(
                    function () {
                        // check response
                        return(null != lResponse);
                    },
                    lSpec, 1500
                    );

            // check the result 
            runs(function () {
                expect(aExpectedCode).toEqual(lResponse.code);
                expect(aActions).toEqual(lResponse.actions);
                expect(aInstance).toEqual(lResponse.instance);
            });
        });
    },
    testRemoveUser: function (aUser, aExpectedCode) {
        var lSpec = "removeUser =" + aUser.username;
        it(lSpec, function () {

            var lResponse = null;
            var lToken = {id: aUser.id};
            lToken.ns = jws.tests.Backbone.UseradminEE_NS;
            lToken.type = "removeUser";
            jws.tests.Backbone.getConn().sendToken(lToken, {
                OnResponse: function (aToken) {
                    lResponse = aToken;
                }
            });
            waitsFor(function () {
                return(null !== lResponse);
            }, lSpec, 3000);
            runs(function () {
                expect(lResponse.code).toEqual(aExpectedCode);
            });
        });
    },
    testLogon: function (aUsername, aPassword) {
        var lSpec = "Opening connection and logon with the user: " + aUsername + ".";
        it(lSpec, function () {

            // we need to "control" the server to broadcast to all connections here
            if (null === jws.tests.Backbone.getConn()) {
                jws.Tests.setAdminConn(new jws.jWebSocketJSONClient());
            }
            var lResponse = {};
            // open a separate control connection
            if (jws.tests.Backbone.getConn().isOpened()) {
                jws.tests.Backbone.getConn().login(aUsername, aPassword, {
                    OnResponse: function (aToken) {
                        lResponse = aToken;
                    }
                });
            } else {
                jws.tests.Backbone.getConn().open(jws.getDefaultServerURL()
                        + ";sessionCookieName=myBackboneSession", {
                            OnWelcome: function (aToken) {
                                jws.tests.Backbone.getConn().login(aUsername, aPassword, {
                                    OnResponse: function (aToken) {
                                        lResponse = aToken;
                                    }
                                });
                            }
                        });
            }

            waitsFor(
                    function () {
                        return(lResponse.code !== undefined);
                    },
                    lSpec,
                    3000
                    );
            runs(function () {
                expect(lResponse.username).toEqual(aUsername);
            });
        });
    },
    testLogoff: function () {
        var lSpec = "Logging off test user";
        it(lSpec, function () {
            var lResponse = null;
            jws.tests.Backbone.getConn().logout({
                OnResponse: function (aResponse) {
                    lResponse = aResponse;
                }
            });


            waitsFor(
                    function () {
                        return(lResponse != null);
                    },
                    lSpec,
                    3000
                    );
            runs(function () {
                expect(lResponse.code).toEqual(0);
            });
        });
    },
    runSpecs: function () {

        var lUser = {
            firstname: "Firstname",
            lastname: "Lastname",
            email: "backbone_user@gmail.com",
            mobile: "+53 406 0012",
            username: "backbone_user",
            password: "Backbone_user00",
            securityQuestion: "Color",
            securityAnswer: "black",
            myInterest: "interest_02",
            subscriptions: [{"subscription": "sub_01", "media": "media_01"}, {"subscription": "sub_02", "media": "media_03"}]
        };

        this.testLogon("alexander", "A.schulze0");

        this.testCreateUser(lUser);

        this.testCreateQuota("CountDown", 5, lUser.username,
                "User", "sendSMS", "org.jwebsocket.plugins.sms", 0);

        this.testAssignmentQuotaToNewUser("CountDown", lUser.username,
                "User", "org.jwebsocket.plugins.sms",
                "sendSMS", 0);

        this.testRemoveUser(lUser, 0);
        this.testLogoff();
    }
};