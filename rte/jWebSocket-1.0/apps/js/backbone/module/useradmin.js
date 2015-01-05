//	---------------------------------------------------------------------------
//	jWebSocket Backbone UserAdmin module (Community Edition, CE)
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
App.setModule('useradmin', {
    // the module durable subscriber id
    subscriptionId: 'backbone_module_useradmin:' + App.getNodeId(),
    // the target plugin namespace to listen
    targetNS: 'org.jwebsocket.plugins.useradmin',
    // called when the module is loaded
    load: function(aJMSManager) {
        var self = this;
        // listening useradmin events
        aJMSManager.subscribe({
            onMessage: function(aMessage) {
                var lMsgId = aMessage.getStringProperty('msgId');
                var lTokenType = aMessage.getStringProperty('tokenType');
                var lUsername = aMessage.getStringProperty('username');

                // required for cluster compatibility
                if (App.isClusterEnabled() && !App.getWebSocketServer().getSynchronizer()
                        .getWorkerTurn(lMsgId)) {
                    return;
                }

                // processing message
                if ('registerNewUser' === lTokenType || 'addUser' === lTokenType) {
                    // getting Countdown quota for SMSPlugin
                    var lResponse = self.getSMSQuota('CountDown',
                            'org.jwebsocket.plugins.sms',
                            'defaultUser', 'Group', 'sendSMS');

                    if (0 === lResponse.getCode()) {
                        // assigning the SMS countdown quota to the new user
                        lResponse = self.registerSMSQuota(lResponse.getString('uuid'),
                                'CountDown', lUsername, 'User');
                        
                        if (0 !== lResponse.getCode()) {
                            App.getLogger().error("UserAdmin - Could not register quota: " + lResponse.getString('msg'));
                        } else {
							App.getLogger().debug("UserAdmin - Quota CountDown(SMS) successfully registered for '" + lUsername + "' user!")
						}
                    } else {
                        // if there is not a quota for SMSPlugin, create the quota
                        // for the new user that has been register
                        lResponse = App.invokePlugIn('jws.quota', null, {
                            type: 'createQuota',
                            identifier: 'CountDown',
                            namespace: 'org.jwebsocket.plugins.sms',
                            instance: 'defaultUser',
                            instance_type: 'Group',
                            actions: 'sendSMS',
                            value: '5'
                        });

                        // if quota create was success then register user to the quota.
                        if (0 === lResponse.getCode()) {
                            var lUuidResponse = lResponse.getString('uuid');
                            lResponse = self.registerSMSQuota(lUuidResponse,
                                    'CountDown', lUsername, 'User');

                            if (0 !== lResponse.getCode()) {
                                App.getLogger().error("UserAdmin - Could not register quota: " + lResponse.getString('msg'));
                            }
                            self.initQuotaDefaultServerUsers(lUuidResponse);
                        } else {
                            App.getLogger().error("UserAdmin - Could not create the quota: " + lResponse.getString('msg'));
                        }
                    }
                }
            }
        }, 'ns = \'org.jwebsocket.plugins\' AND '
                + 'msgType = \'tokenProcessed\' AND '
                + 'code = 0 AND '
                + 'tokenNS = \'' + this.targetNS + '\'',
                true,
                this.subscriptionId);

        var lResponse = self.getSMSQuota('CountDown',
                'org.jwebsocket.plugins.sms',
                'defaultUser', 'Group', 'sendSMS');

        // if there is not a quota for SMSPlugin, create the quota
        if (0 !== lResponse.getCode()) {
            lResponse = App.invokePlugIn('jws.quota', null, {
                type: 'createQuota',
                identifier: 'CountDown',
                namespace: 'org.jwebsocket.plugins.sms',
                instance: 'defaultUser',
                instance_type: 'Group',
                actions: 'sendSMS',
                value: '5'
            });

            if (0 === lResponse.getCode()) {
                // registering quotas for defualt server users
                this.initQuotaDefaultServerUsers(lResponse.getString('uuid'));
            } else {
                /*App.getLogger().error("UserAdmin - Could not create the quota: "
                        + lResponse.getString('msg'));*/
            }
        } else {
            // registering quotas for defualt server users
            this.initQuotaDefaultServerUsers(lResponse.getString('uuid'));
        }
    },
    // called when the module is unloaded
    unload: function(aJMSManager) {
        aJMSManager.unsubscribe(this.subscriptionId);
    },
    registerSMSQuota: function(aUuid, aIdentifier,
            aInstance, aIntanceType) {

        // assigning the SMS countdown quota to the new user
        var lResponse = App.invokePlugIn('jws.quota', null, {
            type: 'registerQuota',
            uuid: aUuid,
            identifier: aIdentifier,
            instance: aInstance,
            instance_type: aIntanceType
        });

        return lResponse;
    },
    getSMSQuota: function(aIdentifier, aNamespace, aInstance, aInstanceType, aActions) {
        // getting the SMS countdown quota
        var lResponse = App.invokePlugIn('jws.quota', null, {
            type: 'getQuota',
            identifier: aIdentifier,
            namespace: aNamespace,
            instance: aInstance,
            instance_type: aInstanceType,
            actions: aActions
        });

        return lResponse;
    },
    initQuotaDefaultServerUsers: function(aUuid) {
        // registering quota for SMS plugin to default users anonymous,guest,root
        // the firt time when the quota is created their quota values is equal 0
        // aIdentifier, aNamespace, aInstance, aInstanceType, aActions
        var lResponse = this.getSMSQuota('CountDown',
                'org.jwebsocket.plugins.sms',
                'anonymous', 'User', 'sendSMS');

        if (0 !== lResponse.getCode()) {
            lResponse = this.registerSMSQuota(aUuid, 'CountDown', 'anonymous', 'User');
            if (0 === lResponse.getCode()) {
                App.invokePlugIn('jws.quota', null, {
                    type: 'setQuota',
                    identifier: 'CountDown',
                    namespace: 'org.jwebsocket.plugins.sms',
                    instance: 'anonymous',
                    instance_type: 'User',
                    actions: 'sendSMS',
                    value: '0'
                });
            }
        }

        // creating quota for guest user and setting this to 0
        lResponse = this.getSMSQuota('CountDown',
                'org.jwebsocket.plugins.sms', 'guest', 'User', 'sendSMS');

        if (0 !== lResponse.getCode()) {
            lResponse = this.registerSMSQuota(aUuid,
                    'CountDown', 'guest', 'User');

            if (0 === lResponse.getCode()) {
                App.invokePlugIn('jws.quota', null, {
                    type: 'setQuota',
                    identifier: 'CountDown',
                    namespace: 'org.jwebsocket.plugins.sms',
                    instance: 'guest',
                    instance_type: 'User',
                    actions: 'sendSMS',
                    value: '0'
                });
            }
        }

        // creating quota for root user and setting this to 0
        lResponse = this.getSMSQuota('CountDown',
                'org.jwebsocket.plugins.sms',
                'root', 'User', 'sendSMS');

        if (0 !== lResponse.getCode()) {
            lResponse = this.registerSMSQuota(aUuid,
                    'CountDown', 'root', 'User');

            if (0 === lResponse.getCode()) {
                App.invokePlugIn('jws.quota', null, {
                    type: 'setQuota',
                    identifier: 'CountDown',
                    namespace: 'org.jwebsocket.plugins.sms',
                    instance: 'root',
                    instance_type: 'User',
                    actions: 'sendSMS',
                    value: '0'
                });
            }
        }

        // creating quota for root user and setting this to 0
        lResponse = this.getSMSQuota('CountDown',
                'org.jwebsocket.plugins.sms',
                'alexander', 'User', 'sendSMS');

        if (0 !== lResponse.getCode()) {
            lResponse = this.registerSMSQuota(aUuid,
                    'CountDown', 'alexander', 'User');

            if (0 === lResponse.getCode()) {
                App.invokePlugIn('jws.quota', null, {
                    type: 'setQuota',
                    identifier: 'CountDown',
                    namespace: 'org.jwebsocket.plugins.sms',
                    instance: 'alexander',
                    instance_type: 'User',
                    actions: 'sendSMS',
                    value: '5'
                });
            }
        }
    }
});

