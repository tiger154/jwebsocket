//	---------------------------------------------------------------------------
//	jWebSocket ScriptingPlugIn JavaScript App module (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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

// @author kyberneees
var App = (function() {
	// app listeners container
	var mListeners = AppUtils.newThreadSafeMap();
	// app public objects (controllers) container
	var mAPI = AppUtils.newThreadSafeMap();
	// Packages object reference
	var mPackages = Packages; 

	// function to convert a JavaScript native object to a Java Map instance
	var toMap = function(aNativeObject) {
		var lMap = new Packages.java.util.HashMap();
		if (aNativeObject instanceof Packages.java.util.Map){
			lMap = aNativeObject;
		} else {
			for (var lAttr in aNativeObject) {
				lMap.put(lAttr, aNativeObject[lAttr]);
			}
		}
		
		return lMap;
	};
	
	// function to convert a Java Token object to a JavaScript native object instance
	var toNativeObject = function(aObject){
		var lNative, lIt;
		if (aObject instanceof Packages.java.util.Map){
			lNative = {};
			lIt = aObject.keySet().iterator();
			while (lIt.hasNext()){
				var lProp = lIt.next();
				lNative[lProp] = toNativeObject(aObject.get(lProp));
			}
			
			return lNative;
		} else if (aObject instanceof Packages.java.util.List){
			lNative = [];
			lIt = aObject.iterator();
			while (lIt.hasNext()){
				lNative.push(toNativeObject(lIt.next()));
			}
			
			return lNative;
		}
		
		return aObject;
	}
	
	// app utility storage
	var mStorage = AppUtils.newThreadSafeMap();
	// app version
	var mVersion = '1.0.0';
	// app description
	var mDescription = '';
	// app server client instance
	var mServerClient;

	return {
		getJMSManager: function(aUseTransaction, aConn){
			var lJMSManager;
			if (undefined !== aUseTransaction && undefined != aConn){
				lJMSManager = AppUtils.getJMSManager(aUseTransaction, aConn);
			} else if (undefined !== aUseTransaction){
				lJMSManager = AppUtils.getJMSManager(aUseTransaction);
			}
			lJMSManager = AppUtils.getJMSManager();
			
			return lJMSManager;
		},
		getDescription: function(){
			return mDescription;  
		},
		setDescription: function(aDescription){
			mDescription = aDescription;
		},
		getVersion: function(){
			return mVersion;
		}, 
		getStorage: function(){
			return mStorage;
		},
		set: function(aAttrName, aValue){
			mStorage.put(aAttrName, aValue);
			
			return aValue;
		},
		get: function(aAttrName, aDefaultValue){
			var lValue = mStorage.get(aAttrName);
			if (null == lValue){
				lValue = aDefaultValue;
			}
			
			return lValue;
		},
		setVersion: function(aVersion){
			mVersion = aVersion;
		},
		getName: function() {
			return AppUtils.getName();
		},
		loadJar: function(aFile){
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
			var lToken = toMap(aToken);

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
		createResponse: function(aInToken) {
			return toNativeObject(AppUtils.createResponse(toMap(aInToken)));
		},
		broadcast: function(aConnectors, aToken) {
			AppUtils.broadcast(aConnectors, toMap(aToken));
		},
		newThreadSafeMap: function() {
			return AppUtils.newThreadSafeMap();
		},
		newThreadSafeCollection: function() {
			return AppUtils.newThreadSafeCollection();
		},
		on: function(aEventName, aFn) {
			if (Object.prototype.toString.call(aEventName) === '[object Array]') {
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
					lArgs.push(toNativeObject(aArgs[lIndex]));
				}
				
				var lIt = mListeners.get(aEventName).iterator();
				while (lIt.hasNext()) {
					lIt.next().apply(this, lArgs);
				}
			}
		},
		getAppBeanFactory: function(){
			return AppUtils.getAppBeanFactory();
		},
		loadToAppBeanFactory: function(aFile){
			AppUtils.loadToAppBeanFactory(aFile);
		},
		getBean: function(aBeanId, aNamespace){
			return (undefined == aNamespace)
			? AppUtils.getBean(aBeanId)
			: AppUtils.getBean(aBeanId, aNamespace);
		},
		getAppBean: function(aBeanId){
			return AppUtils.getAppBean(aBeanId);
		},
		getSystemProperty: function(aPropertyName){
			return AppUtils.getSystemProperty(aPropertyName);
		},
		setSystemProperty: function(aPropertyName, aValue){
			return AppUtils.setSystemProperty(aPropertyName, aValue);
		},
		getClass: function(aClassName){
			var lPackages = aClassName.split('.');
			var lPackage = mPackages;
			for (var lIndex = 0; lIndex < lPackages.length - 1; lIndex++){
				lPackage = lPackage[lPackages[lIndex]];
				if (!lPackage) return null;
			}
			
			// getting class
			return lPackage[lPackages[lPackages.length - 1]];
		},
		setModule: function(aName, aModule){
			App.getStorage().put('module.' + aName, aModule);
			return aModule;
		},
		getModule: function(aName){
			return App.getStorage().get('module.' + aName);
		},
		hasModule: function(aName){
			return App.getStorage().containsKey('module.' + aName);
		},
		removeModule: function(aName){
			return App.getStorage().remove('module.' + aName);
		},
		getServerClient: function(){
			if (!mServerClient){
				// get internal client instance
				var lClient = AppUtils.getServerClient();
				
				// return JavaScript wrapper
				mServerClient = {
					NS_SYSTEM: 'org.jwebsocket.plugins.system',
					listeners: {},
					getConnection: function(){
						return lClient;
					},
					sendToken: function(aToken, aCallbacks){
						if (null == aCallbacks){
							aCallbacks = {};
						}
						return lClient.sendToken(toMap(aToken), {
							getTimeout: function(){
								if (aCallbacks['getTimeout']){
									return aCallbacks['getTimeout']();
								}
								return 5000;
							},
							setTimeout: function(aTimeout){},
							OnTimeout: function(aToken){
								if (aCallbacks['OnTimeout']){
									aCallbacks['OnTimeout'](toNativeObject(aToken.getMap()));
								}
							},
							OnResponse: function(aResponse){
								if (aCallbacks['OnResponse']){
									aCallbacks['OnResponse'](toNativeObject(aResponse.getMap()));
								}
							},
							OnSuccess: function(aResponse){
								if (aCallbacks['OnSuccess']){
									aCallbacks['OnSuccess'](toNativeObject(aResponse.getMap()));
								}
							},
							OnFailure: function(aResponse){
								if (aCallbacks['OnFailure']){
									aCallbacks['OnFailure'](toNativeObject(aResponse.getMap()));
								}
							}
						});
					},
					open: function(){
						lClient.open();
					}, 
					isConnected: function(){
						return lClient.isConnected();
					},
					addListener: function(aListener){
						return lClient.addListener({
							processPacket: function(aPacket){
								if (aListener['processPacket']){
									aListener['processPacket'](aPacket);
								}
							},
							processToken: function(aToken){
								if (aListener['processToken']){
									aListener['processToken'](toNativeObject(aToken.getMap()));
								}
							},
							processClosed: function(aReason){
								if (aListener['processClosed']){
									aListener['processClosed'](aReason);
								}
							},
							processWelcome: function(aToken){
								if (aListener['processWelcome']){
									aListener['processWelcome'](toNativeObject(aToken.getMap()));
								}
							},
							processOpened: function(){
								if (aListener['processOpened']){
									aListener['processOpened']();
								}
							}
						});
					},
					logon: function(aUsername, aPassword, aCallbacks){
						this.sendToken({
							ns: this.NS_SYSTEM,
							type: 'logon',
							username: aUsername,
							password: aPassword
						}, aCallbacks);
					},
					logoff: function(aCallbacks){
						this.sendToken({
							ns: this.NS_SYSTEM,
							type: 'logoff'
						}, aCallbacks);
					},
					removeListener: function(aListener){
						lClient.removeListener(aListener);
					},	
					close: function(){
						lClient.close();
					},
					checkConnected: function() {
						var lRes = {
							code: 0,
							msg: 'Ok'
						};
						if(!this.isConnected()) {
							lRes.code = -1;
							lRes.msg = 'Not connected!';
						}
						return lRes;
					}
				}
				
				mServerClient.addListener({
					processToken: function(aToken){
						for (var lIndex in mServerClient.listeners){
							var lListener = mServerClient.listeners[lIndex];
							if (lListener){
								lListener.call(mServerClient, toNativeObject(aToken));
							}
						}
					}
				});
				App.on('beforeAppReload', function(aHotLoad){
					if (false == aHotLoad){
						mServerClient.close();
					}
				});
			}
			
			return mServerClient;
		}
	};
})();

// alias of App.getClass method
Class = function(aClassName){
	return App.getClass(aClassName);
};

/**
 * jWebSocket JavaScript plug-ins bridge
 */
var jws = {
	NS_BASE: 'org.jwebsocket',
	NS_SYSTEM: 'org.jwebsocket.plugins.system',
	
	oop : {
		addPlugIn: function(a, aPlugIn){
			// getting server instance
			var lServer = App.getServerClient();
			
			// storing the plugin for future incoming token notifications.
			App.assertTrue(undefined != aPlugIn.NS, 
				'The given plug-in class has invalid NS property value!')
				
			// registering the plugin listener
			if (typeof (aPlugIn['processToken']) == 'function'){
				lServer.listeners[aPlugIn.NS] = aPlugIn['processToken'];
			}
			
			// prototyping server instance.
			for (var lField in aPlugIn){
				if( !lServer[ lField ] ) {
					lServer[ lField ] = aPlugIn[ lField ];
				}
			}
		}
	}
}