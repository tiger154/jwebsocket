Ext.define('IS.view.collection.EnterAccessPwd', {
	extend: 'IS.view.base.Window',
	iconCls: 'key',
	
	doAction: function(){
		
	},
	
	initComponent: function() {
		this.items = [{
			xtype: 'form',
			bodyPadding: 10,
			border: 0,
			items: [{
				xtype: 'textfield',
				inputType: 'password',
				name : 'accessPassword',
				fieldLabel: 'Please enter the collection access password',
				labelWidth: 150,
				minLength: 4
			}]
		}];

		this.buttons = [{
			text: 'Do Action',
			scope: this,
			handler: function (){
				this.doAction();
			}
		},{
			text: 'Cancel',
			scope: this,
			handler: this.close
		}];

		this.callParent(arguments);
	}
});
