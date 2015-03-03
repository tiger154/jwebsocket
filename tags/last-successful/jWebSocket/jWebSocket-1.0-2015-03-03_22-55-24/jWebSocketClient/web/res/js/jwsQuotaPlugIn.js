//	---------------------------------------------------------------------------
//	jWebSocket Quota Plug-in  (Community Edition, CE)
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
//
//:package:*:jws
//:class:*:jws.QuotaPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.QuotaPlugIn[/tt] class. This _
//:d:en:This client-side plug-in provides the API to access the features of the _
//:d:en:QuotaPlugIn on the server side.
jws.QuotaPlugIn = {
    // namespace for quota plugin
    // if namespace is changed update server plug-in accordingly!
    NS: jws.NS_BASE + ".plugins.quota",
    // if the followings tokens type are changed update server side _
    // accordingly!
    TT_CREATE_QUOTA: "createQuota",
    TT_GET_QUOTA: "getQuota",
    TT_UNREGISTER_QUOTA: "unregisterQuota",
    TT_REGISTER_QUOTA: "registerQuota",
    TT_REDUCE_QUOTA: "reduceQuota",
    TT_INCREASE_QUOTA: "increaseQuota",
    TT_SET_QUOTA: "setQuota",
    TT_QUERY_QUOTA: "query",
    processToken: function(aToken) {

        // check if namespace matches
        if (aToken.ns === jws.QuotaPlugIn.NS) {
            // here you can handle incomimng tokens from the server
            // in the plug-in if desired.

            if (this.TT_CREATE_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnCreateQuotad) {
                        this.OnCreateQuotad(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            } else if (this.TT_GET_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnGetQuota) {
                        this.OnGetQuota(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            } else if (this.TT_UNREGISTER_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnUnregisterQuota) {
                        this.OnUnregisterQuota(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            } else if (this.TT_REGISTER_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnRegisterQuota) {
                        this.OnRegisterQuota(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            } else if (this.TT_REDUCE_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnReduceQuota) {
                        this.OnReduceQuota(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            } else if (this.TT_SET_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnSetQuota) {
                        this.OnSetQuota(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            } else if (this.TT_INCREASE_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnIncreaseQuota) {
                        this.OnIncreaseQuota(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            } else if (this.TT_QUERY_QUOTA === aToken.reqType) {
                if (0 === aToken.code) {
                    if (this.OnQueryQuota) {
                        this.OnQueryQuota(aToken);
                    }
                } else {
                    if (this.OnErrorQuota) {
                        this.OnErrorQuota(aToken);
                    }
                }
            }
        }
    },
    //:m:*:createQuota
    //:d:en::Create a quota with the given values
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aNamespace:String:The namespace of the feature that is restricted by this quota<tt>Example: org.jwebsocket.plugins.sample</tt>
    //:a:en::aInstance:String:The instance that this quota will be apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aInstanceType:String:Define if the instance is an user or a Group, mandatory values are<tt>User or Group</tt>
    //:a:en::aActions:String:Action restricted by this quota<tt>Example: create, remove or *</tt>.
    //:a:en::aValues:Int:The value for this quota<tt>Example: 10</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none       
    createQuota: function (aIdentifier, aNamespace, aInstance, aInstanceType,
            aActions, aValue, aOptions) {
        var lRes = this.checkConnected(),
                lToken = {
                    ns: jws.QuotaPlugIn.NS,
                    type: jws.QuotaPlugIn.TT_CREATE_QUOTA,
                    namespace: aNamespace,
                    instance: aInstance,
                    instance_type: aInstanceType,
                    identifier: aIdentifier,
                    actions: aActions,
                    value: aValue
                };
        if (aOptions && aOptions.uuid) {
            lToken.uuid = aOptions.uuid;
        }
        if (0 === lRes.code) {
            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:getQuota
    //:d:en::Get a complete JSON object with all quota data
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aNamespace:String:The namespace of the quota<tt>Example: org.jwebsocket.plugins.sample</tt>
    //:a:en::aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aInstanceType:String: Define if the instance above is an user or a Group, mandatory values are<tt>User or Group</tt>
    //:a:en::aActions:String:Action restricted by this quota<tt>Example: create, remove or *</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    getQuota: function(aIdentifier, aNamespace, aInstance, aInstanceType,
            aActions, aOptions) {
        var lRes = this.checkConnected();

        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_GET_QUOTA,
                namespace: aNamespace,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aIdentifier,
                actions: aActions
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:unregisterQuota
    //:d:en::Unregister a quota
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aInstance:String:The instance to be unregister<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aUuid:String: The uuid of the quota<tt>Example: 52a009111753a6e839227295076a2fc4 </tt>
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    unregisterQuota: function(aIdentifier, aInstance, aUuid, aOptions) {
        var lRes = this.checkConnected();

        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_UNREGISTER_QUOTA,
                instance: aInstance,
                identifier: aIdentifier,
                uuid: aUuid
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:registerQuota
    //:d:en::Register an instance to an existent quota
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aInstance:String:The instance to be register<tt>Example: guest</tt>
    //:a:en::aUuid:String: The uuid of the quota<tt>Example: 22a009111753a6e859227295076a2fdd </tt>
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    registerQuota: function(aIdentifier, aInstance, aUuid, aOptions) {
        var lRes = this.checkConnected();

        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_REGISTER_QUOTA,
                instance: aInstance,
                identifier: aIdentifier,
                //Only user instances can be registered to a quota
                instance_type: "User",
                uuid: aUuid
            }, aOptions);
        }
        return lRes;

    },
    //:m:*:reduceQuota
    //:d:en::reduceQuota a quota value
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aNamespace:String:The namespace of the quota<tt>Example: org.jwebsocket.plugins.sample</tt>
    //:a:en::aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aInstanceType:String: Define if the instance above is an user or a Group, mandatory values are<tt>User or Group</tt>
    //:a:en::aActions:String:Action restricted by this quota<tt>Example: create, remove or *</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:a:en::aValues:Int:The value to reduce the current quota value<tt>Example: 10</tt>.
    //:r:*:::void:none
    reduceQuota: function(aIdentifier, aNamespace, aInstance, aInstanceType,
            aActions, aValue, aOptions) {
                
        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_REDUCE_QUOTA,
                namespace: aNamespace,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aIdentifier,
                actions: aActions,
                value: aValue
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:reduceQuotaByUuid
    //:d:en::reduceQuota a quota value
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aUuid:String: The uuid of the quota<tt>Example: 22a009111753a6e859227295076a2fdd </tt>
    //:a:en::aValues:Int:The value to reduce the current quota value<tt>Example: 10</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    reduceQuotaByUuid: function(aIdentifier, aInstance, aUuid, aValue, aOptions) {
                
        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_REDUCE_QUOTA,
                instance: aInstance,
                identifier: aIdentifier,
                uuid: aUuid,
                value: aValue
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:setQuota
    //:d:en::set quota value
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aNamespace:String:The namespace of the quota<tt>Example: org.jwebsocket.plugins.sample</tt>
    //:a:en::aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aInstanceType:String: Define if the instance above is an user or a Group, mandatory values are<tt>User or Group</tt>
    //:a:en::aActions:String:Action restricted by this quota<tt>Example: create, remove or *</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:a:en::aValues:Int:The new value for this quota<tt>Example: 10</tt>.
    //:r:*:::void:none
    setQuota: function(aIdentifier, aNamespace, aInstance, aInstanceType,
            aActions, aValue, aOptions) {
        
        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_SET_QUOTA,
                namespace: aNamespace,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aIdentifier,
                actions: aActions,
                value: aValue
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:setQuotaByUuid
    //:d:en::set quota value
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aUuid:String: The uuid of the quota<tt>Example: 22a009111753a6e859227295076a2fdd </tt>
    //:a:en::aValues:Int:The value new value for this quota<tt>Example: 10</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    setQuotaByUuid: function(aIdentifier, aInstance, aUuid, aValue, aOptions) {
                
        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_SET_QUOTA,
                instance: aInstance,
                identifier: aIdentifier,
                uuid: aUuid,
                value: aValue
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:increaseQuota
    //:d:en::increase the current quota value
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aNamespace:String:The namespace of the quota<tt>Example: org.jwebsocket.plugins.sample</tt>
    //:a:en::aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aInstanceType:String: Define if the instance above is an user or a Group, mandatory values are<tt>User or Group</tt>
    //:a:en::aActions:String:Action restricted by this quota<tt>Example: create, remove or *</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:a:en::aValues:Int:The value to increase the current quota value<tt>Example: 10</tt>.
    //:r:*:::void:none
    increaseQuota: function(aIdentifier, aNamespace, aInstance, aInstanceType,
            aActions, aValue, aOptions) {
        
        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_INCREASE_QUOTA,
                namespace: aNamespace,
                instance: aInstance,
                instance_type: aInstanceType,
                identifier: aIdentifier,
                actions: aActions,
                value: aValue
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:increaseQuotaByUuid
    //:d:en::increase the current quota value
    //:a:en::aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aUuid:String: The uuid of the quota<tt>Example: 22a009111753a6e859227295076a2fdd </tt>
    //:a:en::aValues:Int:The value to increase the current quota value<tt>Example: 10</tt>.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    increaseQuotaByUuid: function(aIdentifier, aInstance, aUuid, aValue, aOptions) {
                
        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_INCREASE_QUOTA,
                instance: aInstance,
                identifier: aIdentifier,
                uuid: aUuid,
                value: aValue
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:queryQuota
    //:d:en::Get a quota array filtering by severals options, this method is used for get all quota that match with
    //:d:en::the given criteria Object
    //:a:en::aCriteria:Object:Object that contains the criteria for build the query
    //:a:en::aCriteria.aIdentifier:String:The identifier of the quota<tt>Examples: CountDown, Diskspace, Interval</tt>
    //:a:en::aCriteria.aNamespace:String:The namespace of the quota<tt>Example: org.jwebsocket.plugins.sample</tt>
    //:a:en::aCriteria.aInstance:String:The instance that this quota apply to, could be an user or a group<tt>Example of user: guest</tt><tt>Example of group: DefaultGruop</tt>.
    //:a:en::aCriteria.aQuotaType:String: The real quota type not the identifier, can be severals quota from a quotaType for example<tt>CountDown and CoundDownBy5</tt>
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    queryQuota: function( aCriteria, aIdentifier, aNamespace, aInstance,
            aQuotaType, aOptions) {
        var lRes = this.checkConnected();

        if (0 === lRes.code) {
            this.sendToken({
                ns: jws.QuotaPlugIn.NS,
                type: jws.QuotaPlugIn.TT_QUERY_QUOTA,
                namespace: aNamespace,
                instance: aInstance,
                identifier: aIdentifier,
                quotaType: aQuotaType
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:setQuotaCallbacks
    //:d:en:Sets the quota plug-in lifecycle callbacks
    //:a:en::aListeners:Object:JSONObject containing the quota lifecycle callbacks
    //:a:en::aListeners.OnCreateQuotad:Function:Called when a quota has been created
    //:a:en::aListeners.OnGetQuota:Function:Called when a get quota action is performed
    //:a:en::aListeners.OnUnregisterQuota:Function:Called when a quota is unregister
    //:a:en::aListeners.OnRegisterQuota:Function:Called when a quota has been registered
    //:a:en::aListeners.OnReduceQuota:Function:Called when the quota reduce action is performed
    //:a:en::aListeners.OnSetQuota:Function:Called when the quota set action is performed
    //:a:en::aListeners.OnIncreaseQuota:Function:Called when the quota increase action is performed
    //:a:en::aListeners.OnQueryQuota:Function:Called when the quota query action is performed
    //:a:en::aListeners.OnErrorQuota:Function:Called when an error occur during a local file load
    //:r:*:::void:none		
    setQuotaCallbacks: function(aListeners) {

        if (!aListeners) {
            aListeners = {};
        }
        if (aListeners.OnCreateQuotad !== undefined) {
            this.OnCreateQuotad = aListeners.OnCreateQuotad;
        }
        if (aListeners.OnGetQuota !== undefined) {
            this.OnGetQuota = aListeners.OnGetQuota;
        }
        if (aListeners.OnUnregisterQuota !== undefined) {
            this.OnUnregisterQuota = aListeners.OnUnregisterQuota;
        }
        if (aListeners.OnRegisterQuota !== undefined) {
            this.OnRegisterQuota = aListeners.OnRegisterQuota;
        }
        if (aListeners.OnReduceQuota !== undefined) {
            this.OnReduceQuota = aListeners.OnReduceQuota;
        }
        if (aListeners.OnSetQuota !== undefined) {
            this.OnSetQuota = aListeners.OnSetQuota;
        }
        if (aListeners.OnIncreaseQuota !== undefined) {
            this.OnIncreaseQuota = aListeners.OnIncreaseQuota;
        }
        if (aListeners.OnQueryQuota !== undefined) {
            this.OnQueryQuota = aListeners.OnQueryQuota;
        }
        if (aListeners.OnErrorQuota !== undefined) {
            this.OnErrorQuota = aListeners.OnErrorQuota;
        }


    }

};
// add the QuotaPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.QuotaPlugIn);
