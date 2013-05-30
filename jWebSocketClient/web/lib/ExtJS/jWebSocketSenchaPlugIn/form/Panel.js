Ext.define('Ext.jws.form.Panel', {
	override: 'Ext.form.Panel',
	alternateClassName: 'Ext.jwsFormPanel',
	submit: function(aOptions) {
		var lThis = this,
				lForm = lThis.element.dom || {},
				lFormValues;

		aOptions = Ext.apply({
			url: lThis.getUrl() || lForm.action,
			submit: false,
			method: lThis.getMethod() || lForm.method || 'post',
			autoAbort: false,
			params: null,
			waitMsg: null,
			headers: null,
			success: null,
			failure: null
		}, aOptions || {});

		lFormValues = lThis.getValues(lThis.getStandardSubmit() || !aOptions.submitDisabled);
		if (lThis.jwsSubmit) {
			var lNamespace = aOptions.ns || lThis.ns,
					lTokenType = aOptions.tokentype || lThis.tokentype;
			if (!lNamespace || !lTokenType) {
				Ext.Logger.error("Tokentype or namespace missing! Please check your " +
						"namespace or tokentype, they are both required to " +
						"submit your form. Please provide them in your submit " +
						"options or in your Ext.form.Panel configuration" +
						"configuration with the following structure: " +
						"ns:'your.namespace.com', tokentype:'the_type_of_token'");
				return true;
			} else {
				aOptions.ns = aOptions.ns || lThis.ns;
				aOptions.tokentype = aOptions.tokentype || lThis.tokentype;
				lThis.fireAction('beforesubmit', [lThis, lFormValues, aOptions], 'doJWebSocketRequest')
			}
		} else {
			return lThis.fireAction('beforesubmit', [lThis, lFormValues, aOptions], 'doBeforeSubmit');
		}
	},
	doJWebSocketRequest: function(aMe, aFormValues, aOptions) {
		var lDoRequest = function(aMe, aFormValues, aOptions) {
			var lData = Ext.apply( Ext.apply({}, aMe.getBaseParams() || {}),
					aOptions.params || {},
					aFormValues );
			console.log(lData);
			var lCallbacks = {
				success: function(aToken) {
					if (Ext.isFunction(aOptions.success)) {
						var lResponse = aToken;
						aOptions.success.call(aOptions.scope || aMe, aMe, lResponse, Ext.encode(aToken));
					}
					aMe.fireEvent('submit', aMe, lResponse);
				},
				failure: function(aToken) {
					if (Ext.isFunction(aOptions.failure)) {
						var lResponse = aToken;
						aOptions.failure.call(aOptions.scope || aMe, aMe, lResponse, Ext.encode(aToken));
					}
					aMe.fireEvent('exception', aMe, lResponse);
				}
			};

			Ext.jws.Client.send(aOptions.ns, aOptions.tokentype, lData, lCallbacks, aMe);
		};
		// If the user forgot to open the connection
		var lConnection = Ext.jws.Client.getConnection();
		if (lConnection && lConnection.isConnected()) {
			lDoRequest(aMe, aFormValues, aOptions);
		} else {
			Ext.jws.Client.on('open', function() {
				lDoRequest(aMe, aFormValues, aOptions);
			});
			Ext.jws.Client.open();
		}
	}
});