Ext.define("BPMNEditor.view.Login", {
	extend: 'Ext.form.Panel',
	xtype: 'loginForm',
	// Authenticate with the System PlugIn
	ns: jws.NS_BASE + '.plugins.system',
	// The tokentype to process the incoming data
	tokentype: 'logon',
	config: {
		title: 'Login',
		items: [{
				xtype: 'fieldset',
				title: 'User Log-in',
				margin: 0,
				defaults: {
					required: true
				},
				items: [{
						xtype: 'textfield',
						name: 'username',
						label: 'User Name',
						allowBlank: false
					}, {
						xtype: 'textfield',
						name: 'password',
						inputType: 'password',
						label: 'Password',
						allowBlank: false
					}, {
						text: 'Login',
						xtype: 'button',
						handler: function() {
							var lForm = this.up("loginForm"),
									lFields = lForm.getValues();
							if (lForm.isValid()) {
								Ext.jwsClient.getConnection().systemLogon(lFields.username, lFields.password, {
									OnSuccess: function() {
										lForm.destroy();
										BPMNEditor.app.redirectTo("main/index");
									},
									OnFailure: function(aToken) {
										Ext.Msg.alert("Error", aToken.msg);
									}
								});
							}
						}
					}, {
						text: 'Cancel',
						xtype: 'button',
						handler: function() {
							BPMNEditor.app.redirectTo("main/index");
							this.up("loginForm").destroy();
						}
					}]
			}]
	}
});