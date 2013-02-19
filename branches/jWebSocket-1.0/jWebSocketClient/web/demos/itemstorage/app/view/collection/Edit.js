Ext.define('IS.view.collection.Edit', {
	extend: 'IS.view.base.Window',
	title: 'Edit collection',
	alias: 'widget.c_edit',
	iconCls: 'c_edit',
	
	loadData: function( aData ){
		this.showAt({
			y: 100
		});
		var lForm = this.down('form');
		lForm.down('textfield[name=collectionName]').setValue(aData.name);
		lForm.down('textfield[name=type]').setValue(aData.type);
		lForm.down('textfield[name=capacity]').setValue(aData.capacity);
		lForm.down('checkbox[name=private]').setValue(aData['private']);
		lForm.down('checkbox[name=capped]').setValue(aData.capped);
	},

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
				allowBlank: false,
				disabled: true
			}, {
				xtype: 'textfield',
				name : 'type',
				fieldLabel: 'Item type',
				maskRe: /^[a-zA-Z0-9]/,
				regex: /^[a-zA-Z]+([a-zA-Z0-9]+)*/,
				allowBlank: false,
				disabled: true
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'secretPassword',
				fieldLabel: 'Secret Password',
				minLength: 4,
				allowBlank: false
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'newSecretPassword',
				fieldLabel: 'New Secret Password',
				minLength: 4,
				linkType: 'pwd' 
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'newSecretPassword2',
				fieldLabel: 'Confirm New Secret Password',
				minLength: 4,
				vtype: 'confirm'
			}, {
				xtype: 'textfield',
				inputType: 'password',
				name : 'accessPassword',
				fieldLabel: 'Access Password',
				minLength: 4,
				linkType: 'pwd' 
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
			action: 'edit'
		},{
			text: 'Cancel',
			scope: this,
			handler: this.close
		}];

		this.callParent(arguments);
	}
});
