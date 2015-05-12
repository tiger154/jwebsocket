//	<JasobNoObfs>
//  ---------------------------------------------------------------------------
//  jWebSocket - Sencha ExtJS PlugIn (Community Edition, CE)
//  ---------------------------------------------------------------------------
//  Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  ---------------------------------------------------------------------------
//	</JasobNoObfs>

/**
 * @author Victor Antonio Barzana Crespo
 **/

//	---------------------------------------------------------------------------
//  This class constains the jWebSocket implementation of the 
//  [tt]Ext.form.Panel[/tt] class
//	---------------------------------------------------------------------------

//:package:*:Ext.jws.form
//:class:*:Ext.jws.form.Panel
//:ancestor:*:Ext.form.Panel
//:d:en:Implementation of the default submit action of the form but using _
//:d:en:jWebSocketClient to submit the data, this class can only be used in _
//:d:en:Sencha Touch Forms.
Ext.define('Ext.jws.form.Panel', {
	extend: 'Ext.form.Panel',
	requires: ['Ext.jws.Client'],
	alternateClassName: 'Ext.jwsFormPanel',
	xtype: 'jwsFormPanel',
	ns: undefined,
	tokentype: undefined,
	submit: function (aOptions) {
		var lThis = this,
				lElem = lThis.element || lThis.el || {},
				lForm = lElem.dom || {},
				lFormValues;

		aOptions = Ext.apply({
			url: lForm.action,
			submit: false,
			method: lForm.method || 'post',
			autoAbort: false,
			params: null,
			waitMsg: null,
			headers: null,
			success: null,
			failure: null
		}, aOptions || {});
		
		lFormValues = Ext.apply(lThis.getValues(), lThis.getRecord() ? lThis.getRecord().data : {});
		var lNamespace = aOptions.ns || lThis.ns ||
				(typeof lThis.getNs === "function" ? lThis.getNs() : ""),
				lTokenType = aOptions.tokentype || lThis.tokentype ||
				(typeof lThis.getTokentype === "function" ? lThis.getTokentype() : "");
		if (!lNamespace || !lTokenType) {
			Ext.Logger.error("Tokentype or namespace missing! Please check your " +
					"namespace or tokentype, they are both required to " +
					"submit your form. Please provide them in your submit " +
					"options or in your Ext.form.Panel configuration" +
					"configuration with the following structure: " +
					"ns:'your.namespace.com', tokentype:'the_type_of_token'");
			return true;
		} else {
			aOptions.ns = lNamespace;
			aOptions.tokentype = lTokenType;
			if (typeof lThis.fireAction === "function") {
				lThis.fireAction('beforesubmit', [lThis, lFormValues, aOptions], 'doJWebSocketRequest');
			} else {
				lThis.fireEvent('beforesubmit', [lThis, lFormValues, aOptions]);
				lThis.doJWebSocketRequest(lThis, lFormValues, aOptions);
			}
		}
	},
	doJWebSocketRequest: function (aForm, aFormValues, aOptions) {
		var lDoRequest = function (aForm, aFormValues, aOptions) {
			var lData = Ext.apply(Ext.apply({}, aForm.baseParams || {}),
					aOptions.params || {},
					aFormValues);
			var lCallbacks = {
				success: function (aToken) {
					if (Ext.isFunction(aOptions.success)) {
						var lResponse = aToken;
						aOptions.success.call(aOptions.scope || aForm, aForm, lResponse, Ext.encode(aToken));
					}
					aForm.fireEvent('submit', aForm, lResponse);
				},
				failure: function (aToken) {
					if (Ext.isFunction(aOptions.failure)) {
						var lResponse = aToken;
						aOptions.failure.call(aOptions.scope || aForm, aForm, lResponse, Ext.encode(aToken));
					}
					aForm.fireEvent('exception', aForm, lResponse);
				}
			};

			Ext.jws.Client.send(aOptions.ns, aOptions.tokentype, lData, lCallbacks, aForm);
		};
		// If the user forgot to open the connection
		var lConnection = Ext.jws.Client.getConnection();
		if (lConnection && lConnection.isConnected()) {
			lDoRequest(aForm, aFormValues, aOptions);
		} else {
			Ext.jws.Client.on('open', function () {
				lDoRequest(aForm, aFormValues, aOptions);
			});
			Ext.jws.Client.open();
		}
	}
});