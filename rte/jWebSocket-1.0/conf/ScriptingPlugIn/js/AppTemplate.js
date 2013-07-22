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
	var mListeners = AppUtils.newThreadSafeMap();
	var mAPI = AppUtils.newThreadSafeMap();
	var mPackages = Packages; // saving packages reference
	var toMap = function(aObject) {
		var lMap = new App.getClass('java.util.HashMap')();
		if (aObject instanceof App.getClass('java.util.Map')){
			lMap = aObject;
		} else {
			for (var lAttr in aObject) {
				lMap.put(lAttr, aObject[lAttr]);
			}
		}
		
		return lMap;
	};
	
	var mStorage = AppUtils.newThreadSafeMap();
	var mVersion = '1.0.0';
	var mDescription = '';

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
		setVersion: function(aVersion){
			mVersion = aVersion;
		},
		getName: function() {
			return AppUtils.getName();
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
			if (!aListener) {
				AppUtils.sendChunkable(aConnector, aChunkable);
			} else {
				AppUtils.sendChunkable(aConnector, aChunkable, aListener);
			}
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
			return AppUtils.createResponse(aInToken);
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
				var $this = this;
				for (var lIndex = 0; lIndex < aEventName.length; lIndex++) {
					$this.on(aEventName[lIndex], aFn);
				}
				return;
			}
			if (!mListeners.containsKey(aEventName)) {
				mListeners.put(aEventName, this.newThreadSafeCollection());
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
					lArgs.push(aArgs[lIndex]);
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
			if (undefined == aNamespace){
				return AppUtils.getBean(aBeanId);
			}
			return AppUtils.getBean(aBeanId, aNamespace);
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
				
				if (null == lPackage) return null;
			}
			
			// getting class
			return lPackage[lPackages[lPackages.length - 1]];
		}
	};
})();

// shortcut for apps ClassLoader
Class = function(aClassName){
	return App.getClass(aClassName);
};

/**
 * jWebSocket JavaScript plug-ins bridge
 */
/*
var jws = {
	oop : {
		addPlugIn: function(a, aPlugIn){
			// getting server instance
			var lServer = App.getServer();
			
			// storing the plugin for future incoming token notifications.
			lServer.listeners.push(aPlugIn);
			
			// prototyping server instance.
			for (var lField in aPlugIn){
				if( !lServer.prototype[ lField ] ) {
					lServer.prototype[ lField ] = aPlugIn[ lField ];
				}
			}
		}
	}
}
*/

// blocking direct access to classes 
// required for sandboxing purposes
java = undefined;
com = undefined;
Packages = undefined;
importPackage = undefined;
importClass = undefined;
JavaImporter = undefined;