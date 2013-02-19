Ext.define('IS.view.collection.Create', {
	extend: 'IS.view.base.Window',
	alias: 'widget.c_create',

	title: 'Create collection',
	iconCls: 'c_add',

	initComponent: function() {
		this.items = [{
			xtype: 'form',
			bodyPadding: 10,
			border: 0,
			items: [{
				xtype: 'textfield',
				name : 'collectionName',
				fieldLabel: 'Name',
				maskRe: /^[a-zA-Z0-9_-]/,
				regex: /^[a-zA-Z0-9]+(.[_a-zA-Z0-9-]+)*/,
				allowBlank: false
			}, {
				xtype: 'textfield',
				name : 'itemType',
				fieldLabel: 'Item type',
				maskRe: /^[a-zA-Z0-9]/,
				regex: /^[a-zA-Z]+([a-zA-Z0-9]+)*/,
				allowBlank: false
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'secretPassword',
				fieldLabel: 'Secret Password',
				minLength: 4
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'secretPassword2',
				fieldLabel: 'Confirm Secret Password',
				minLength: 4,
				vtype: 'confirm'
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'accessPassword',
				fieldLabel: 'Access Password',
				minLength: 4
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'accessPassword2',
				fieldLabel: 'Confirm Access Password',
				minLength: 4,
				vtype: 'confirm'
			}, {
				xtype: 'checkbox',
				name : 'private',
				fieldLabel: 'Private',
				checked: true
			}, {
				xtype: 'numberfield',
				name : 'capacity',
				fieldLabel: 'Capacity',
				minValue: 0,
				maxValue: 2147483647,
				allowDecimals: false
			}, {
				xtype: 'checkbox',
				name : 'capped',
				fieldLabel: 'Capped',
				disabled: true
			}]
		}];

		this.buttons = [{
			text: 'Save',
			action: 'create'
		},{
			text: 'Cancel',
			scope: this,
			handler: this.close
		}];

		this.callParent(arguments);
	}
});
