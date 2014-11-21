//	---------------------------------------------------------------------------
//	jWebSocket Local Storage Library (Community Edition, CE)
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

jws.AbstractStorage = {
	isSupported: function () {
		return typeof (Storage) !== "undefined";
	},
	setItem: function (aStorage, aKey, aValue, aOnSuccess, aOnFailure) {
		var lResponse = {
			code: 0,
			msg: "Ok",
			op: "setItem",
			key: aKey,
			value: aValue
		};
		try {
			aStorage.setItem(aKey, aValue);
			if (aOnSuccess) {
				aOnSuccess(lResponse);
			}
		} catch (lEx) {
			lResponse.code = -1;
			lResponse.msg = lEx.message;
			if (aOnFailure) {
				aOnFailure(lResponse);
			}
		}
		return lResponse;
	},
	getItem: function (aStorage, aKey, aOnSuccess, aOnFailure) {
		var lResponse = {
			code: 0,
			msg: "Ok",
			op: "getItem",
			key: aKey,
			value: undefined
		};
		try {
			var lValue = aStorage.getItem(aKey);
			lResponse.value = lValue;
			if (lValue) {
				if (aOnSuccess) {
					aOnSuccess(lResponse);
				}
			} else {
				lResponse.code = -1;
				lResponse.msg = "No value for key '" + aKey + "' found";
				if (aOnFailure) {
					aOnFailure(lResponse);
				}
			}
		} catch (lEx) {
			lResponse.code = -1;
			lResponse.msg = lEx.message;
			if (aOnFailure) {
				aOnFailure(lResponse);
			}
		}
		return lResponse;
	},
	removeItem: function (aStorage, aKey, aOnSuccess, aOnFailure) {
		var lResponse = {
			code: 0,
			msg: "Ok",
			op: "removeItem",
			key: aKey,
			value: undefined
		};
		try {
			aStorage.removeItem(aKey);
			if (aOnSuccess) {
				aOnSuccess(lResponse);
			}
		} catch (lEx) {
			lResponse.code = -1;
			lResponse.msg = lEx.message;
			if (aOnFailure) {
				aOnFailure(lResponse);
			}
		}
		return lResponse;
	},
	listItems: function (aStorage, aOnSuccess, aOnFailure) {
		var lResponse = {
			code: -1,
			msg: "n/a",
			op: "listItems",
			items: []
		};
		try {
			lResponse.code = 0;
			lResponse.msg = "Ok";
			for (var lKey in aStorage) {
				lResponse.items.push({"key": lKey, "lValue": aStorage[lKey]});
			}
			if (aOnSuccess) {
				aOnSuccess(lResponse);
			}
		} catch (lEx) {
			lResponse.code = -1;
			lResponse.msg = lEx.message;
			if (aOnFailure) {
				aOnFailure(lResponse);
			}
		}
		return lResponse;
	},
	clearItems: function (aStorage, aOnSuccess, aOnFailure) {
		var lResponse = {
			code: -1,
			msg: "n/a",
			op: "clearItems"
		};
		try {
			lResponse.code = 0;
			lResponse.msg = "Ok";
			for (var lKey in aStorage) {
				delete aStorage[lKey];
			}
			if (aOnSuccess) {
				aOnSuccess(lResponse);
			}
		} catch (lEx) {
			lResponse.code = -1;
			lResponse.msg = lEx.message;
			if (aOnFailure) {
				aOnFailure(lResponse);
			}
		}
		return lResponse;
	},
	hasItem: function (aStorage, aKey, aOnSuccess, aOnFailure) {
		aStorage[ aKey ] !== undefined;
	}
};

jws.LocalStorage = {
	isSupported: function () {
		return window.localStorage !== "undefined";
	},
	setItem: function (aKey, aValue, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.setItem(window.localStorage, aKey, aValue, aOnSuccess, aOnFailure);
	},
	getItem: function (aKey, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.getItem(window.localStorage, aKey, aOnSuccess, aOnFailure);
	},
	removeItem: function (aKey, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.removeItem(window.localStorage, aKey, aOnSuccess, aOnFailure);
	},
	listItems: function (aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.listItems(window.localStorage, aOnSuccess, aOnFailure);
	},
	clearItems: function (aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.clearItems(window.localStorage, aOnSuccess, aOnFailure);
	},
	hasItem: function (aKey, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.hasItem(window.localStorage, aKey);
	}
};

jws.SessionStorage = {
	isSupported: function () {
		return window.sessionStorage !== "undefined";
	},
	setItem: function (aKey, aValue, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.setItem(window.sessionStorage, aKey, aValue, aOnSuccess, aOnFailure);
	},
	getItem: function (aKey, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.getItem(window.sessionStorage, aKey, aOnSuccess, aOnFailure);
	},
	removeItem: function (aKey, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.removeItem(window.sessionStorage, aKey);
	},
	listItems: function (aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.listItems(window.sessionStorage, aOnSuccess, aOnFailure);
	},
	clearItems: function (aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.clearItems(window.sessionStorage, aOnSuccess, aOnFailure);
	},
	hasItem: function (aKey, aOnSuccess, aOnFailure) {
		return jws.AbstractStorage.hasItem(window.sessionStorage, aKey);
	}
};

// browser abstraction
window.indexedDB = window.indexedDB
		|| window.mozIndexedDB
		|| window.webkitIndexedDB
		|| window.msIndexedDB;
window.IDBTransaction = window.IDBTransaction
		|| window.webkitIDBTransaction
		|| window.msIDBTransaction;
window.IDBKeyRange = window.IDBKeyRange
		|| window.webkitIDBKeyRange
		|| window.msIDBKeyRange;

jws.IndexedDB = {
	mDB: null,
	DB_NAME: "localStorageDB",
	OBJ_STORE: "localStorageStore",
	KEY_FIELD: "key",
	VALUE_FIELD: "value"
};

jws.IndexedDB.onerror = function (aEvent) {
	jws.console.error(aEvent);
};

jws.IndexedDB.open = function () {
	var lVersion = 1;
	var lRequest = indexedDB.open(jws.IndexedDB.DB_NAME, lVersion);

	lRequest.onsuccess = function (aEvent) {
		jws.IndexedDB.mDB = aEvent.target.result;
	};
	lRequest.onerror = jws.IndexedDB.onerror;

	lRequest.onupgradeneeded = function (aEvent) {
		jws.IndexedDB.mDB = aEvent.target.result;
		var lDB = jws.IndexedDB.mDB;

		// in case an upgrade is needed delete the previous version
		if (lDB.objectStoreNames.contains(jws.IndexedDB.OBJ_STORE)) {
			lDB.deleteObjectStore(jws.IndexedDB.OBJ_STORE);
		}
		// and then create the new version
		if (!lDB.objectStoreNames.contains(jws.IndexedDB.OBJ_STORE)) {
			lDB.createObjectStore(jws.IndexedDB.OBJ_STORE, {
				keyPath: jws.IndexedDB.KEY_FIELD
			});
		}
	};
};

jws.IndexedDB.setItem = function (aKey, aValue, aOnSuccess, aOnFailure) {
	var lDB = jws.IndexedDB.mDB;
	var lTA = lDB.transaction([jws.IndexedDB.OBJ_STORE], "readwrite");
	var lStore = lTA.objectStore(jws.IndexedDB.OBJ_STORE);

	var lItem = {
		"key": aKey,
		"value": aValue
	};

	var lRequest = lStore.put(lItem);
	var lResponse = {
		code: -1,
		msg: "n/a",
		op: "setItem",
		key: aKey,
		value: aValue
	};

	lRequest.onsuccess = function (aEvent) {
		if (aOnSuccess) {
			lResponse.code = 0;
			lResponse.msg = "ok";
			aOnSuccess(lResponse);
		}
	};

	lRequest.onerror = function (aEvent) {
		if (aOnFailure) {
			aOnFailure(lResponse);
		}
	};
};

jws.IndexedDB.getItem = function (aKey, aOnSuccess, aOnFailure) {
	var lDB = jws.IndexedDB.mDB;
	var lTA = lDB.transaction([jws.IndexedDB.OBJ_STORE], "readwrite");
	var lStore = lTA.objectStore(jws.IndexedDB.OBJ_STORE);

	var lRequest = lStore.get(aKey);
	var lResponse = {
		code: -1,
		msg: "n/a",
		op: "getItem",
		key: null,
		value: null
	};

	lRequest.onsuccess = function (aEvent) {
		lResponse.key = aKey;
		if (aEvent.target.result) {
			lResponse.code = 0;
			lResponse.msg = "ok";
			lResponse.value = aEvent.target.result.value;
			if (aOnSuccess) {
				aOnSuccess(lResponse);
			}
		} else {
			lResponse.msg = "No value for key '" + aKey + "' found";
			lResponse.value = undefined;
			if (aOnFailure) {
				aOnFailure(lResponse);
			}
		}
	};

	lRequest.onerror = function (aEvent) {
		if (aOnFailure) {
			aOnFailure(lResponse);
		}
	};
};

jws.IndexedDB.removeItem = function (aKey, aOnSuccess, aOnFailure) {
	var lDB = jws.IndexedDB.mDB;
	var lTA = lDB.transaction([jws.IndexedDB.OBJ_STORE], "readwrite");
	var lStore = lTA.objectStore(jws.IndexedDB.OBJ_STORE);

	var lRequest = lStore.delete(aKey);
	var lResponse = {
		code: -1,
		msg: "n/a",
		op: "removeItem",
		key: null,
		value: undefined
	};

	lRequest.onsuccess = function (aEvent) {
		if (aOnSuccess) {
			lResponse.code = 0;
			lResponse.msg = "ok";
			lResponse.key = aKey;
			// lResponse.value = aEvent.target.result.value;
			aOnSuccess(lResponse);
		}
	};

	lRequest.onerror = function (aEvent) {
		if (aOnFailure) {
			aOnFailure(lResponse);
		}
	};
};

jws.IndexedDB.listItems = function (aOnSuccess, aOnFailure) {
	var lDB = jws.IndexedDB.mDB;
	var lTA = lDB.transaction([jws.IndexedDB.OBJ_STORE], "readwrite");
	var lStore = lTA.objectStore(jws.IndexedDB.OBJ_STORE);

	// Get everything in the store;
	var lCursorRequest = lStore.openCursor();

	var lResponse = {
		code: -1,
		msg: "n/a",
		op: "listItems",
		items: []
	};
	lCursorRequest.onsuccess = function (e) {
		var lResult = e.target.result;
		if (!lResult) {
			if (aOnSuccess) {
				lResponse.code = 0;
				lResponse.msg = "ok";
				aOnSuccess(lResponse);
			}
			return(lResponse);
		}
		lResponse.items.push({"key": lResult.value.key, "value": lResult.value.value});
		lResult.continue();
	};

	lCursorRequest.onerror = function (aEvent) {
		if (aOnFailure) {
			aOnFailure(lResponse);
		}
	};
};

jws.IndexedDB.clearItems = function (aOnSuccess, aOnFailure) {
	var lDB = jws.IndexedDB.mDB;
	var lTA = lDB.transaction([jws.IndexedDB.OBJ_STORE], "readwrite");
	var lStore = lTA.objectStore(jws.IndexedDB.OBJ_STORE);

	var lRequest = lStore.clear();
	var lResponse = {
		code: -1,
		msg: "n/a",
		op: "clearItems"
	};

	lRequest.onsuccess = function (aEvent) {
		if (aOnSuccess) {
			lResponse.code = 0;
			lResponse.msg = "ok";
			aOnSuccess(lResponse);
		}
	};

	lRequest.onerror = function (aEvent) {
		if (aOnFailure) {
			aOnFailure(lResponse);
		}
	};
};

jws.IndexedDB.deleteStore = function (aOnSuccess, aOnFailure) {
	var lDB = jws.IndexedDB.mDB;

	var lRequest = lDB.deleteObjectStore(jws.IndexedDB.OBJ_STORE);
	var lResponse = {
		code: -1,
		msg: "n/a",
		op: "deleteStore"
	};

	lRequest.onsuccess = function (aEvent) {
		if (aOnSuccess) {
			lResponse.code = 0;
			lResponse.msg = "ok";
			aOnSuccess(lResponse);
		}
	};

	lRequest.onerror = function (aEvent) {
		if (aOnFailure) {
			aOnFailure(lResponse);
		}
	};
};

jws.IndexedDB.deleteDatabase = function (aOnSuccess, aOnFailure) {

	var lRequest = window.indexedDB.deleteDatabase(jws.IndexedDB.DB_NAME);

	var lResponse = {
		code: -1,
		msg: "n/a",
		op: "deleteDatabase"
	};

	lRequest.onsuccess = function (aEvent) {
		if (aOnSuccess) {
			lResponse.code = 0;
			lResponse.msg = "ok";
			aOnSuccess(lResponse);
		}
	};

	lRequest.onerror = function (aEvent) {
		if (aOnFailure) {
			aOnFailure(lResponse);
		}
	};
};

function init() {
	jws.IndexedDB.open();
}

window.addEventListener("DOMContentLoaded", init, false);


jws.IndexedDBStorage = {
	isSupported: function () {
		return window.indexedDB !== undefined;
	},
	setItem: function (aKey, aValue, aOnSuccess, aOnFailure) {
		jws.IndexedDB.setItem(aKey, aValue, aOnSuccess, aOnFailure);
	},
	getItem: function (aKey, aOnSuccess, aOnFailure) {
		jws.IndexedDB.getItem(aKey, aOnSuccess, aOnFailure);
	},
	removeItem: function (aKey, aOnSuccess, aOnFailure) {
		jws.IndexedDB.removeItem(aKey, aOnSuccess, aOnFailure);
	},
	listItems: function (aOnSuccess, aOnFailure) {
		jws.IndexedDB.listItems(aOnSuccess, aOnFailure);
	},
	clearItems: function (aOnSuccess, aOnFailure) {
		jws.IndexedDB.clearItems(aOnSuccess, aOnFailure);
	},
	hasItem: function (aKey, aOnSuccess, aOnFailure) {
		return false;
	},
	deleteStore: function (aOnSuccess, aOnFailure) {
		jws.IndexedDB.deleteStore(aOnSuccess, aOnFailure);
	},
	deleteDatabase: function (aOnSuccess, aOnFailure) {
		jws.IndexedDB.deleteDatabase(aOnSuccess, aOnFailure);
	}
};

function globalSuccessHandler(aData) {
	jws.console.info("Success: " + JSON.stringify(aData));
}

function globalFailureHandler(aData) {
	jws.console.error("Failure: " + JSON.stringify(aData));
}


jws.StorageTests = {};

jws.StorageTests.run = function () {

	var lStorageSupported = jws.AbstractStorage.isSupported();
	var lLocalStorageSupported = jws.LocalStorage.isSupported();
	var lSessionStorageSupported = jws.SessionStorage.isSupported();
	var lIndexedDBStorageSupported = jws.IndexedDBStorage.isSupported();

	jws.console.log("Supporting storage at all: " + lStorageSupported);
	jws.console.log("Supporting local storage: " + lLocalStorageSupported);
	jws.console.log("Supporting session storage: " + lSessionStorageSupported);
	jws.console.log("Supporting indexed db storage: " + lIndexedDBStorageSupported);

	if (lLocalStorageSupported) {
		jws.console.log("local_test1: " + jws.LocalStorage.getItem("local_test1"));
		jws.console.log("local_test2: " + jws.LocalStorage.getItem("local_test2"));
		jws.console.log("local_test3: " + jws.LocalStorage.getItem("local_test3"));
	}
	if (lSessionStorageSupported) {
		jws.console.log("session_test1: " + jws.SessionStorage.getItem("session_test1"));
		jws.console.log("session_test2: " + jws.SessionStorage.getItem("session_test2"));
		jws.console.log("session_test3: " + jws.SessionStorage.getItem("session_test3"));
	}

	if (lLocalStorageSupported) {
		jws.LocalStorage.setItem("local_test1", "abc");
		jws.LocalStorage.setItem("local_test2", "xyz");
		jws.LocalStorage.setItem("local_test3", "123");
	}

	if (lSessionStorageSupported) {
		jws.SessionStorage.setItem("session_test1", "abc");
		jws.SessionStorage.setItem("session_test2", "xyz");
		jws.SessionStorage.setItem("session_test3", "123");
	}

	setTimeout(function () {

		if (lIndexedDBStorageSupported) {
			jws.IndexedDBStorage.getItem("indexed_test1", globalSuccessHandler, globalFailureHandler);
			jws.IndexedDBStorage.getItem("indexed_test2", globalSuccessHandler, globalFailureHandler);
			jws.IndexedDBStorage.getItem("indexed_test3", globalSuccessHandler, globalFailureHandler);
		}
		if (lIndexedDBStorageSupported) {
			jws.IndexedDBStorage.setItem("indexed_test1", "abc", globalSuccessHandler, globalFailureHandler);
			jws.IndexedDBStorage.setItem("indexed_test2", "xyz", globalSuccessHandler, globalFailureHandler);
			jws.IndexedDBStorage.setItem("indexed_test3", "123", globalSuccessHandler, globalFailureHandler);
		}
		if (lIndexedDBStorageSupported) {
			jws.IndexedDBStorage.removeItem("indexed_test2", globalSuccessHandler, globalFailureHandler);
			jws.IndexedDBStorage.removeItem("indexed_test2", globalSuccessHandler, globalFailureHandler);
		}
	}
	, 1000);

};
