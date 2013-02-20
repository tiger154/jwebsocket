Ext.define('IS.view.definition.EditStringAttribute', {
	extend: 'IS.view.definition.EditAttribute',

	initComponent: function() {
		this.items = [{
			xtype: 'form',
			bodyPadding: 10,
			border: 0,
			autoScroll: true,
			items: [{
				xtype: 'textfield',
				fieldLabel: 'Default',
				name: 'default'
			},{
				xtype: 'numberfield',
				name : 'min_length',
				fieldLabel: 'Min Length',
				minValue: 0,
				allowDecimals: false
			},{
				xtype: 'numberfield',
				name : 'max_length',
				fieldLabel: 'Max Length',
				minValue: 0,
				allowDecimals: false,
				vtype: 'max'
			}, {
				xtype: 'checkbox',
				name : 'mail',
				fieldLabel: 'Mail'
			}, {
				xtype: 'textfield',
				name : 'input_type',
				fieldLabel: 'Input Type'
			}, {
				xtype: 'textfield',
				name : 'reg_exp',
				fieldLabel: 'Reg Exp'
			}, {
				xtype: 'checkbox',
				name : 'multi_line',
				fieldLabel: 'Multi Line'
			}]
		}];
	
		this.on('afterrender', function ( aWindow ){
			aWindow.down('form').down('textfield[name=in]').emptyText = "['Juan', 'Pedro', 'Maria']";
			aWindow.down('form').down('textfield[name=in]').vtype = 'stringArray';
			aWindow.down('form').down('textfield[name=not_in]').emptyText = "['Judas']";
			aWindow.down('form').down('textfield[name=not_in]').vtype = 'stringArray';
		});

		this.callParent(arguments);
	}
});
