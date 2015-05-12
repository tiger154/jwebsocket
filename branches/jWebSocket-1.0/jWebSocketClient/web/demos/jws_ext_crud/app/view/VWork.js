Ext.define('CRUD.view.VWork', {
	extend: 'Ext.jws.form.Panel',
	alias: 'widget.vwork',
	padding: 10,
	frame: true,
	init: function () {
		this.callParent(arguments);
	},
	config: {
		ns: 'org.jwebsocket.plugins.scripting',
		tokentype: 'callMethod'
	},
	items: [{
			xtype: 'textfield',
			fieldLabel: 'User',
			name: 'username',
			value: 'root'
		}, {
			xtype: 'textfield',
			fieldLabel: 'Password',
			inputType: 'password',
			name: 'password',
			value: 'root'
		}, {
			xtype: 'displayfield',
			fieldLabel: 'Server Status',
			id: 'server_status',
			value: 'Disconnected',
			cls: 'error'
		}],
	buttons: [{
			text: 'Send'
		}]

});

