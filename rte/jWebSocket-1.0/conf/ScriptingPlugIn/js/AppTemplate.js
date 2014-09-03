//	---------------------------------------------------------------------------
//	jWebSocket ScriptingPlugIn JavaScript App module (Community Edition, CE)
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

// @author Rolando Santamaria Maso
var App = (function() {
// app listeners container
	var mListeners = AppUtils.newThreadSafeMap();
	// app public objects (controllers) container
	var mAPI = AppUtils.newThreadSafeMap();
	// app utility storage
	var mStorage = AppUtils.newThreadSafeMap();
	// app version
	var mVersion = '1.0.0';
	// app description
	var mDescription = '';
	// app server client instance
	var mServerClient;
	// app event bus reference
	var mEventBus;
	// common regular expressions
	var mCommonRegExps = {
		username: /^[a-z0-9_-]{3,20}$/,
		slug: /^[a-z0-9-]+$/,
		email: /^([a-z0-9_\.-]+)@([\da-z\.-]+)\.([a-z\.]{2,6})$/,
		url: /^(((ws|wss|ftps|http|https|ftp):\/\/)?([[a-zA-Z0-9]\-\.])+(\.)([[a-zA-Z0-9]]){2,4}([[a-zA-Z0-9]\/+=%&_\.~?\-]*))*$/,
		password: /^(?=^.{6,}$)((?=.*[A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z]))^.*$/
	};
	return {
		getJMSManager: function(aUseTransaction, aConn) {
			var lJMSManager;
			if (undefined !== aUseTransaction && undefined !== aConn) {
				lJMSManager = AppUtils.getJMSManager(aUseTransaction, aConn);
			} else if (undefined !== aUseTransaction) {
				lJMSManager = AppUtils.getJMSManager(aUseTransaction);
			}
			lJMSManager = AppUtils.getJMSManager();
			return lJMSManager;
		},
		newAsyncResult: function(aHandler) {
			return new Packages.org.jwebsocket.async.AsyncResult(aHandler);
		},
		getDescription: function() {
			return mDescription;
		},
		getWebSocketServer: function() {
			return AppUtils.getWebSocketServer();
		},
		isArray: function(aObject) {
			return 'array' === App.getType(aObject);
		},
		isObject: function(aObject) {
			return 'object' === App.getType(aObject);
		},
		setDescription: function(aDescription) {
			mDescription = aDescription;
		},
		getVersion: function() {
			return mVersion;
		},
		getStorage: function() {
			return mStorage;
		},
		set: function(aAttrName, aValue) {
			mStorage.put(aAttrName, aValue);
			return aValue;
		},
		get: function(aAttrName, aDefaultValue) {
			var lValue = mStorage.get(aAttrName);
			if (null === lValue) {
				lValue = aDefaultValue;
			}

			return lValue;
		},
		setVersion: function(aVersion) {
			mVersion = aVersion;
		},
		getName: function() {
			return AppUtils.getName();
		},
		getNodeId: function() {
			return AppUtils.getNodeId();
		},
		loadJar: function(aFile) {
			return AppUtils.loadJar(aFile);
		},
		getPath: function() {
			return AppUtils.getPath();
		},
		publish: function(aObjectId, aObject) {
			mAPI.put(aObjectId, aObject);
		},
		getPublished: function(aObjectId) {
			return mAPI.get(aObjectId);
		},
		unpublish: function(aObjectId) {
			mAPI.remove(aObjectId);
		},
		getClientAPI: function() {
			var lClientAPI = {};
			var lIt = mAPI.keySet().iterator();
			while (lIt.hasNext()) {
				var lObjId = lIt.next();
				var lObj = mAPI.get(lObjId);
				var lObjAPI = {
					methods: [],
					description: lObj.description || null,
					args: lObj.args || {}
				};
				for (var lMethod in lObj) {
					var lType = typeof (lObj[lMethod]);
					if ('function' === lType) {
						lObjAPI.methods.push({
							name: lMethod,
							length: lObj[lMethod].length
						});
					} else if ('object' === lType) {
						if ('function' === typeof (lObj[lMethod]['handler'])) {
							lObjAPI.methods.push({
								name: lMethod,
								length: lObj[lMethod]['handler'].length
							});
						} else if ('object' === typeof (lObj[lMethod]['ebBridge'])) {
							lObjAPI.methods.push({
								name: lMethod,
								length: lObj[lMethod]['ebBridge']['args'].length
							});
						}
					}
				}

				lClientAPI[lObjId] = lObjAPI;
			}

			return lClientAPI;
		},
		isPublished: function(aObjectId) {
			return mAPI.containsKey(aObjectId);
		},
		getLogger: function() {
			return AppUtils.getLogger();
		},
		assertTrue: function(aBoolean, aMessage) {
			AppUtils.assertTrue(aBoolean, aMessage);
		},
		assertNotNull: function(aObject, aMessage) {
			AppUtils.assertNotNull(aObject, aMessage);
		},
		importScript: function(aFile) {
			AppUtils.importScript(aFile);
		},
		sendToken: function(aConnector, aToken, aArg3, aArg4) {
			var lToken = App.toMap(aToken);
			if (!aArg3) {
				AppUtils.sendToken(aConnector, lToken);
			} else if (!aArg4) {
				AppUtils.sendToken(aConnector, lToken, aArg3);
			} else {
				AppUtils.sendToken(aConnector, lToken, aArg3, aArg4);
			}
		},
		sendChunkable: function(aConnector, aChunkable, aListener) {
			(!aListener)
					? AppUtils.sendChunkable(aConnector, aChunkable)
					: AppUtils.sendChunkable(aConnector, aChunkable, aListener);
		},
		getAllConnectors: function() {
			return AppUtils.getAllConnectors();
		},
		hasAuthority: function(aConnector, aAuthority) {
			return AppUtils.hasAuthority(aConnector, aAuthority);
		},
		requireAuthority: function(aConnector, aAuthority) {
			AppUtils.requireAuthority(aConnector, aAuthority);
		},
		requireAuthenticated: function(aConnector) {
			AppUtils.requireAuthenticated(aConnector);
		},
		createResponse: function(aInToken) {
			return App.toJS(AppUtils.createResponse(App.toMap(aInToken)));
		},
		broadcast: function(aArg1, aArg2) {
			if (null !== aArg2) {
				AppUtils.broadcast(aArg1, App.toMap(aArg2));
			} else {
				AppUtils.broadcast(App.toMap(aArg1));
			}
		},
		newThreadSafeMap: function() {
			return AppUtils.newThreadSafeMap();
		},
		newThreadSafeCollection: function() {
			return AppUtils.newThreadSafeCollection();
		},
		on: function(aEventName, aFn) {
			if ('array' === App.getType(aEventName)) {
				for (var lIndex = 0; lIndex < aEventName.length; lIndex++) {
					App.on(aEventName[lIndex], aFn);
				}
				return;
			}
			if (!mListeners.containsKey(aEventName)) {
				mListeners.put(aEventName, App.newThreadSafeCollection());
			}
			mListeners.get(aEventName).add(aFn);
		},
		un: function(aEventName, aFn) {
			if (mListeners.containsKey(aEventName)) {
				mListeners.get(aEventName).remove(aFn);
			}
		},
		notifyEvent: function(aEventName, aArgs) {
			if (mListeners.containsKey(aEventName)) {
				var lArgs = new Array();
				for (var lIndex = 0; lIndex < aArgs.length; lIndex++) {
					lArgs.push(App.toJS(aArgs[lIndex]));
				}

				var lIt = mListeners.get(aEventName).iterator();
				while (lIt.hasNext()) {
					lIt.next().apply(this, lArgs);
				}
			}
		},
		invokeObject: function(aObjectId, aMethod, aArgs) {
			var lObject = App.getPublished(aObjectId);
			if ('function' === typeof (lObject[aMethod])) {
				return lObject[aMethod].apply(lObject, App.toJS(aArgs));
			} else if ('object' === typeof (lObject[aMethod])
					&& ('function' === typeof (lObject[aMethod]['handler'])
							|| 'object' === typeof (lObject[aMethod]['ebBridge']))) {
				var lDef = lObject[aMethod];
				// checking method authenticated metadata
				if (lDef.authenticated) {
					App.requireAuthenticated(aArgs[aArgs.length - 2]);
				}
				// checking method authority metadata
				if (lDef.authority) {
					App.requireAuthority(aArgs[aArgs.length - 2], lDef.authority);
				}

				// checking method arguments metadata
				if (lDef.args) {
					for (var lIndex in lDef.args) {
						var lArgName = lDef.args[lIndex];
						App.assertTrue(undefined !== lObject.args[lArgName],
								'Argument \'' + lArgName + '\' definition is missing!');
						App.assertValue(lObject.args[lArgName], aArgs[lIndex],
								'Invalid \'' + lArgName + '\' argument value, constraint violation: ');
					}
				}
				// checking method cache metadata
				if (lDef.cache) {
					//NOTE: cache metadata suppose to be supported on the client
				}

				// calling custom method annotation processors
				// passing connector instance as last argument for developers convenience 
				App.notifyEvent('controller.method.annotation.evaluation', [aMethod, lDef, aArgs, aArgs[aArgs.length - 2]]);

				if ('function' === typeof (lObject[aMethod]['handler'])) {
					return lObject[aMethod].handler.apply(lObject, App.toJS(aArgs));
				} else {
					// supporting EventBus bridge
					var lBridge = lObject[aMethod]['ebBridge'];
					App.assertValue('string' === App.getType(lBridge.ns), 'Expecting valid \'ns\' property in ebBrige definition!');
					App.assertValue('string' === App.getType(lBridge.type), 'Expecting valid \'type\' property in ebBrige definition!');

					var lMessage = {
						ns: lBridge.ns,
						type: aMethod
					};
					for (var lIndex in lDef.args) {
						lMessage[lDef.args[lIndex]] = aArgs[lIndex];
					}

					// sending message through the EventBus
					if ('publish' === lBridge.action) {
						EventBus.publish(lMessage);

						return;
					} else if ('send' === lBridge.action) {
						var lResponseHandler = aArgs[aArgs.length - 1];
						EventBus.send(lMessage, {
							OnSuccess: function(aResponse) {
								// removing response header 
								delete aResponse.code;
								delete aResponse.message_uuid;
								delete aResponse.reqType;
								delete aResponse.type;
								delete aResponse.ns;

								App.newAsyncResult(lResponseHandler)
										.setResult(App.toJava(aResponse));
							},
							OnFailure: function(aResponse) {
								App.newAsyncResult(lResponseHandler)
										.setFailure(new Packages.java.lang.Exception(aResponse.msg));
							}
						});

						return;
					}
				}
			}

			throw new Error('Method \'' + aMethod + '\' does not exists on target object!');
		},
		assertValue: function(aDef, aValue, aMessage) {
			if (aDef['not_null']) {
				App.assertTrue(aValue !== null, aMessage + 'not_null');
			}
			var lType = App.getType(aValue);
			if (aDef['type'] !== undefined) {
				App.assertTrue(aDef['type'] === lType, aMessage + 'type');
			}

			if ('string' === lType || 'array' === lType) {
				if (aDef['min_length'] !== undefined) {
					App.assertTrue(aValue.length >= aDef['min_length'], aMessage + 'min_length');
				}
				if (aDef['max_length'] !== undefined) {
					App.assertTrue(aValue.length <= aDef['max_length'], aMessage + 'max_length');
				}
			}

			if ('string' === lType) {
				if (aDef['regex'] !== undefined) {
					var lMatches;
					if ('string' === typeof (aDef['regex'])) {
						if (!mCommonRegExps[aDef['regex']]) {
							throw new Error('Regex \'' + aDef['regex'] + '\' is not supported!');
						}
						lMatches = ("" + aValue).match(mCommonRegExps[aDef['regex']]);
					} else {
						lMatches = ("" + aValue).match(aDef['regex']);
					}
					App.assertTrue(lMatches !== null && lMatches.length > 0 && lMatches[0].length === aValue.length,
							aMessage + 'regex');
				}
			} else if (('integer' || 'double') === lType) {
				if (aDef['min_value'] !== undefined) {
					App.assertTrue(aValue >= aDef['min_value'], aMessage + 'min_value');
				}
				if (aDef['max_value'] !== undefined) {
					App.assertTrue(aValue <= aDef['max_value'], aMessage + 'max_value');
				}
			}

			if ('function' === App.getType(aDef['validator'])) {
				App.assertTrue(aDef['validator'](aValue), aMessage + 'validator');
			}

			// calling custom argument annotation processors
			App.notifyEvent('controller.argument.annotation.evaluation', [aDef, aValue, aMessage]);

		},
		invokePlugIn: function(aPlugInId, aConnector, aToken) {
			return AppUtils.invokePlugIn(aPlugInId, aConnector, App.toMap(aToken));
		},
		isClusterEnabled: function() {
			return AppUtils.isClusterEnabled();
		},
		toMap: function(aObject) {
			var lMap = new Packages.java.util.HashMap();
			if (aObject instanceof Packages.java.util.Map) {
				lMap = aObject;
			} else {
				for (var lAttr in aObject) {
					var lValue = aObject[lAttr];
					if ('function' !== typeof (lValue)) {
						lMap.put(lAttr, App.toJava(lValue));
					}
				}
			}

			return lMap;
		},
		toJava: function(aObject) {
			if (App.isObject(aObject)) {
				return App.toMap(aObject);
			} else if (App.isArray(aObject)) {
				return App.toList(aObject);
			} else {
				return aObject;
			}
		},
		toJS: function(aObject) {
			var lJSON;
			if (aObject instanceof Packages.java.util.Map) {
				lJSON = {};
				var lIt = aObject.keySet().iterator();
				while (lIt.hasNext()) {
					var lProp = lIt.next();
					lJSON[lProp] = App.toJS(aObject.get(lProp));
				}

				return lJSON;
			} else if (aObject instanceof Packages.java.util.List) {
				lJSON = [];
				var lIt = aObject.iterator();
				while (lIt.hasNext()) {
					lJSON.push(App.toJS(lIt.next()));
				}

				return lJSON;
			} else if (aObject instanceof Packages.java.lang.Boolean) {
				return true === aObject;
			}

			return aObject;
		},
		toList: function(aArray) {
			var lList = new Packages.java.util.LinkedList();
			for (var lIndex in aArray) {
				lList.add(App.toJava(aArray[lIndex]));
			}

			return lList;
		},
		getAppBeanFactory: function() {
			return AppUtils.getAppBeanFactory();
		},
		loadToAppBeanFactory: function(aFile) {
			AppUtils.loadToAppBeanFactory(aFile);
		},
		getBean: function(aBeanId, aNamespace) {
			return (undefined === aNamespace)
					? AppUtils.getBean(aBeanId)
					: AppUtils.getBean(aBeanId, aNamespace);
		},
		getType: function(aObject) {
			var lRes = typeof aObject;
			if (aObject === undefined) {
				lRes = 'undefined';
			} else if (aObject === null) {
				lRes = 'null';
			} else if ('number' === lRes) {
				if ((parseFloat(aObject) === parseInt(aObject))) {
					lRes = 'integer';
				} else {
					lRes = 'double';
				}
			} else if (aObject instanceof Packages.java.util.List ||
					aObject.constructor
					&& aObject.constructor.toString().indexOf('function Array()') === 0) {
				lRes = 'array';
			}

			return lRes;
		},
		getAppBean: function(aBeanId) {
			return AppUtils.getAppBean(aBeanId);
		},
		getSystemProperty: function(aPropertyName) {
			return AppUtils.getSystemProperty(aPropertyName);
		},
		setSystemProperty: function(aPropertyName, aValue) {
			return AppUtils.setSystemProperty(aPropertyName, aValue);
		},
		setModule: function(aName, aModule) {
			App.getStorage().put('module.' + aName, aModule);
			return aModule;
		},
		getModule: function(aName) {
			return App.getStorage().get('module.' + aName);
		},
		hasModule: function(aName) {
			return App.getStorage().containsKey('module.' + aName);
		},
		removeModule: function(aName) {
			return App.getStorage().remove('module.' + aName);
		},
		getEventBus: function() {
			if (!mEventBus) {
				var lEventBus = App.getWebSocketServer().getEventBus();
				var lSetResponseFields = function(aReq, aResponse) {
					aResponse.ns = aReq.ns;
					aResponse.type = 'response';
					aResponse.reqType = aReq.type;
					aResponse.code = 0;
					aResponse.message_uuid = aReq.message_uuid;
				};
				var lToHandler = function(aListener) {
					if (!aListener)
						return null;
					var lListener = new Packages.org.jwebsocket.eventbus.Handler.IEventListener(){
						OnMessage: function(aToken) {
							var lMessage = App.toJS(aToken.getMap());
							lMessage.reply = function(aResponse, aListener) {
								lSetResponseFields(lMessage, aResponse);
								mEventBus.send(aResponse, aListener);
							};
							lMessage.fail = function(aResponse, aListener) {
								lSetResponseFields(lMessage, aResponse);
								aResponse.code = -1;
								mEventBus.send(aResponse, aListener);
							};
							if (aListener.OnMessage) {
								aListener.OnMessage(lMessage);
							}
							if ('response' === lMessage.type) {
								if (aListener.OnResponse) {
									aListener.OnResponse(lMessage);
								}
								if (0 === aToken.getCode()) {
									if (aListener.OnSuccess) {
										aListener.OnSuccess(lMessage);
									}
								} else {
									if (aListener.OnFailure) {
										aListener.OnFailure(lMessage);
									}
								}
							}
						},
						OnTimeout: function(aToken) {
							var lMessage = App.toJS(aToken.getMap());
							if (aListener.OnTimeout) {
								aListener.OnTimeout(lMessage);
							}
						}
					};
					return new Packages.org.jwebsocket.eventbus.Handler(lListener, aListener.timeout || 0);
				};
				mEventBus = {
					cancelHandlersOnShutdown: true,
					publish: function(aObject) {
						lEventBus.publish(AppUtils.toToken(App.toMap(aObject)));
					},
					send: function(aObject, aListener) {
						lEventBus.send(AppUtils.toToken(App.toMap(aObject)), lToHandler(aListener));
					},
					register: function(aNS, aListener) {
						var lRegistration = lEventBus.register(aNS, lToHandler(aListener));
						if (this.cancelHandlersOnShutdown) {
							App.on(['undeploying', 'beforeAppReload'], function() {
								lRegistration.cancel();
							});
						}

						return lRegistration;
					}
				};
			}

			return mEventBus;
		}
		,
		getServerClient: function() {
			if (!mServerClient) {
				// get internal client instance
				var lClient = AppUtils.getServerClient();
				// return JavaScript wrapper
				mServerClient = {
					NS_SYSTEM: 'org.jwebsocket.plugins.system',
					listeners: {},
					getConnection: function() {
						return lClient;
					},
					sendToken: function(aToken, aCallbacks) {
						if (null === aCallbacks) {
							aCallbacks = {};
						}
						return lClient.sendToken(App.toMap(aToken), {
							getTimeout: function() {
								if (aCallbacks['getTimeout']) {
									return aCallbacks['getTimeout']();
								}
								return 5000;
							},
							setTimeout: function(aTimeout) {
							},
							OnTimeout: function(aToken) {
								if (aCallbacks['OnTimeout']) {
									aCallbacks['OnTimeout'](App.toJS(aToken.getMap()));
								}
							},
							OnResponse: function(aResponse) {
								if (aCallbacks['OnResponse']) {
									aCallbacks['OnResponse'](App.toJS(aResponse.getMap()));
								}
							},
							OnSuccess: function(aResponse) {
								if (aCallbacks['OnSuccess']) {
									aCallbacks['OnSuccess'](App.toJS(aResponse.getMap()));
								}
							},
							OnFailure: function(aResponse) {
								if (aCallbacks['OnFailure']) {
									aCallbacks['OnFailure'](App.toJS(aResponse.getMap()));
								}
							}
						});
					},
					open: function() {
						lClient.open();
					},
					isConnected: function() {
						return lClient.isConnected();
					},
					addListener: function(aListener) {
						return lClient.addListener({
							processPacket: function(aPacket) {
								if (aListener['processPacket']) {
									aListener['processPacket'](aPacket);
								}
							},
							processToken: function(aToken) {
								if (aListener['processToken']) {
									aListener['processToken'](App.toJS(aToken.getMap()));
								}
							},
							processClosed: function(aReason) {
								if (aListener['processClosed']) {
									aListener['processClosed'](aReason);
								}
							},
							processWelcome: function(aToken) {
								if (aListener['processWelcome']) {
									aListener['processWelcome'](App.toJS(aToken.getMap()));
								}
							},
							processOpened: function() {
								if (aListener['processOpened']) {
									aListener['processOpened']();
								}
							}
						});
					},
					logon: function(aUsername, aPassword, aCallbacks) {
						this.sendToken({
							ns: this.NS_SYSTEM,
							type: 'logon',
							username: aUsername,
							password: aPassword
						}, aCallbacks);
					},
					logoff: function(aCallbacks) {
						this.sendToken({
							ns: this.NS_SYSTEM,
							type: 'logoff'
						}, aCallbacks);
					},
					removeListener: function(aListener) {
						lClient.removeListener(aListener);
					},
					close: function() {
						lClient.close();
					},
					checkConnected: function() {
						var lRes = {
							code: 0,
							msg: 'Ok'
						};
						if (!this.isConnected()) {
							lRes.code = -1;
							lRes.msg = 'Not connected!';
						}
						return lRes;
					}
				};
				mServerClient.addListener({
					processToken: function(aToken) {
						for (var lIndex in mServerClient.listeners) {
							var lListener = mServerClient.listeners[lIndex];
							if (lListener) {
								lListener.call(mServerClient, App.toJS(aToken));
							}
						}
					}
				});
				App.on('beforeAppReload', function(aHotLoad) {
					if (false === aHotLoad) {
						mServerClient.close();
					}
				});
			}

			return mServerClient;
		}
	};
})();
/**
 * Global app EventBus object
 * @type @exp;App@call;getEventBus
 */
EventBus = App.getEventBus();
/**
 * jWebSocket JavaScript plug-ins bridge
 */
var jws = {
	NS_BASE: 'org.jwebsocket',
	NS_SYSTEM: 'org.jwebsocket.plugins.system',
	oop: {
		addPlugIn: function(a, aPlugIn) {
			// getting server instance
			var lServer = App.getServerClient();
			// storing the plugin for future incoming token notifications.
			App.assertTrue(undefined !== aPlugIn.NS,
					'The given plug-in class has invalid NS property value!');
			// registering the plugin listener
			if (typeof (aPlugIn['processToken']) === 'function') {
				lServer.listeners[aPlugIn.NS] = aPlugIn['processToken'];
			}

			// prototyping server instance.
			for (var lField in aPlugIn) {
				if (!lServer[ lField ]) {
					lServer[ lField ] = aPlugIn[ lField ];
				}
			}
		}
	}
};