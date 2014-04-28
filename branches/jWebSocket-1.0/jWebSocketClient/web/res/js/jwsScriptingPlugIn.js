//	---------------------------------------------------------------------------
//	jWebSocket Scripting Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the 'License');
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an 'AS IS' BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.ScriptingPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.ScriptingPlugIn[/tt] class.
//:d:en:This client-side plug-in provides the API to access the features of the _
//:d:en:Scripting plug-in on the jWebSocket server.
jws.ScriptingPlugIn = {
    //:const:*:NS:String:org.jwebsocket.plugins.scripting (jws.NS_BASE + '.plugins.scripting')
    //:d:en:Namespace for the [tt]ScriptingPlugIn[/tt] class.
    // if namespace is changed update server plug-in accordingly!
    NS: jws.NS_BASE + '.plugins.scripting',
    //:m:*:callScriptAppMethod
    //:d:en:Call an script application published object method. 
    //:a:en::aApp:String:The script application name
    //:a:en::aObjectId:String:The published object identifier
    //:a:en::aMethod:String:The method name
    //:a:en::aArgs:Array:The method calling arguments
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    callScriptAppMethod: function(aApp, aObjectId, aMethod, aArgs, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);
        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'callMethod',
                method: aMethod,
                objectId: aObjectId,
                app: aApp,
                args: aArgs
            };
            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:fnToJWSCallback
    //:d:en:Encapsulate a function into a jWebSocket client 'sendToken' compliant callback. 
    //:a:en::aFn:Function:The function to be encapsulated in the jWebSocket callback
    //:r:*:::Object:The jWebSocket callback
    fnToJWSCallback: function(aFn) {
        var lJWSCallback = aFn;

        if ('function' == typeof aFn) {
            var lFn = aFn;
            lJWSCallback = {
                OnSuccess: function(aResponse) {
                    lFn(aResponse);
                },
                OnFailure: function(aResponse) {
                    lFn(new Error(aResponse.msg));
                },
                OnTimeout: function() {
                    lFn(new Error('timeout'));
                }
            };
        }

        return lJWSCallback;
    },
    //:m:*:getScriptApp
    //:d:en:Generate the server-side script app into client-side object.
    //:a:en::aApp:String:The script application name
    //:a:en::aSuccessFn:Function:Called when the app generation succeed. Receives the generated app object as argument
    //:a:en::aFailureFn:Function:Called when the app generation fail
    //:r:*:::void:Object: The generated app object
    getScriptApp: function(aApp, aSuccessFn, aFailureFn) {
        var lWSC = this;
        var lApp = {};
        // adding utility methods
        lApp.getName = function() {
            return aApp;
        };
        lApp.sendToken = function(aToken, aOptions) {
            lWSC.sendScriptAppToken(aApp, aToken, aOptions);
        };
        lApp.getVersion = function(aOptions) {
            lWSC.getScriptAppVersion(aApp, aOptions);
        };
        lApp.getManifest = function(aOptions) {
            lWSC.getScriptAppManifest(aApp, aOptions);
        };
        // generating app controllers
        this.getScriptAppAPI(aApp, {
            OnSuccess: function(aResponse) {
                var lAPI = aResponse.API;
                // iterating by app controllers
                for (var lCName in lAPI) {
                    // controller object
                    var lC = {};
                    // getting controller API
                    var lCAPI = lAPI[lCName];
                    // setting controller description
                    lC.description = lCAPI.description;
                    lC.name = lCName;
                    // generating controller methods
                    for (var lIndex in lCAPI.methods) {
                        var lMethod = lCAPI.methods[lIndex].name;
                        var lLength = lCAPI.methods[lIndex].length;
                        eval('lC["' + lMethod + '"] = function() {lWSC.callScriptAppMethod("'
                                + aApp + '","' + lCName + '","' + lMethod
                                + '",Array.prototype.slice.call(arguments, 0,' + lLength + '),'
                                + 'arguments[' + lLength + ']);};');
                    }
                    // setting controller in app object
                    lApp[lCName] = lC;
                }
                // calling success callback
                aSuccessFn(lApp);
            },
            OnFailure: function(aToken) {
                aFailureFn(aToken);
            }
        });

        return lApp;
    },
    //:m:*:reloadScriptApp
    //:d:en:Reload an script application in runtime.
    //:a:en::aApp:String:The script application name
    //:a:en::aHotReload:Boolean: If TRUE, the script app is reloaded without to destroy the app context. Default TRUE
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    reloadScriptApp: function(aApp, aHotReload, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'reloadApp',
                hotReload: aHotReload,
                app: aApp
            };
            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:getScriptAppVersion
    //:d:en:Get the version of an script application
    //:a:en::aApp:String:The script application name
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    getScriptAppVersion: function(aApp, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'getVersion',
                app: aApp
            };
            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:getScriptAppAPI
    //:d:en:Get the client API of an script application
    //:a:en::aApp:String:The script application name
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    getScriptAppAPI: function(aApp, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'getClientAPI',
                app: aApp
            };
            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:sendScriptToken
    //:d:en:Send a token to an script application.
    //:a:en::aApp:String:The script application name
    //:a:en::aToken:Object:The token to be sent
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none		
    sendScriptAppToken: function(aApp, aToken, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        var lRes = this.checkConnected();
        if (0 === lRes.code && aToken) {
            this.sendToken({
                app: aApp,
                ns: jws.ScriptingPlugIn.NS,
                type: 'token',
                token: aToken
            }, aOptions);
        }
        return lRes;
    },
    //:m:*:deployScriptApp
    //:d:en:Deploy a previous uploaded script application.
    //:a:en::aAppFile:String:The uploaded application filename. A single ZIP file. 
    //:a:en::aHotDeploy:Boolean:If TRUE and the application is already deployed, the application context is not destroyed during deployment. Default FALSE.
    //:a:en::aOptions.deleteAfterDeploy:Boolean:If TRUE, the uploaded application ZIP file is removed after deployment. Default TRUE.
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    deployScriptApp: function(aAppFile, aHotDeploy, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'deploy',
                hotDeploy: aHotDeploy,
                appFile: aAppFile,
                deleteAfterDeploy: (aOptions) ? aOptions.deleteAfterDeploy || true : true
            };

            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:listScriptApps
    //:d:en:List script apps
    //:a:en::aOptions.userOnly:Boolean:If TRUE, only the active user apps are listed, FALSE will list all apps. Default: FALSE
    //:a:en::aOptions.namesOnly:Boolean:If TRUE, only the names value is retrieved per app, FALSE will include more app data. Default: TRUE
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    listScriptApps: function(aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        if (!aOptions)
            aOptions = {};

        var lUserOnly = aOptions.userOnly || false;
        var lNamesOnly = (undefined === aOptions.namesOnly) ? true : aOptions.namesOnly;

        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'listApps',
                userOnly: lUserOnly,
                namesOnly: lNamesOnly
            };

            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:undeployScriptApp
    //:d:en:Undeploy an script app
    //:a:en::aApp:String:The script application name
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    undeployScriptApp: function(aApp, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'undeploy',
                app: aApp
            };

            this.sendToken(lToken, aOptions);
        }
        return lRes;
    },
    //:m:*:getScriptAppManifest
    //:d:en:Get a target application manifest content.
    //:a:en::aApp:String:The script application name
    //:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
    //:r:*:::void:none
    getScriptAppManifest: function(aApp, aOptions) {
        // supporting basic response function callback
        aOptions = this.fnToJWSCallback(aOptions);

        var lRes = this.checkConnected();
        if (0 === lRes.code) {
            var lToken = {
                ns: jws.ScriptingPlugIn.NS,
                type: 'getManifest',
                app: aApp
            };

            this.sendToken(lToken, aOptions);
        }
        return lRes;
    }
};

// add the JWebSocket Scripting PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.ScriptingPlugIn);
