Ext.define('IS.view.definition.EditLongAttribute', {
	extend: 'IS.view.definition.EditAttribute',

	initComponent: function() {
		this.items = [{
			xtype: 'form',
			bodyPadding: 10,
			border: 0,
			autoScroll: true,
			items: [{
				xtype: 'numberfield',
				fieldLabel: 'Default',
				name: 'default',
				allowDecimals: false
			},{
				xtype: 'numberfield',
				name : 'min_value',
				fieldLabel: 'Min Value',
				allowDecimals: false
			},{
				xtype: 'numberfield',
				name : 'max_value',
				fieldLabel: 'Max Value',
				allowDecimals: false,
				vtype: 'max'
			},{
				xtype: 'arrayfield',
				name: 'between',
				fieldLabel: 'Between',
				vtype: 'integerArray',
				emptyText: '[0,10]'
			}, {
				xtype: 'arrayfield',
				name: 'not_between',
				fieldLabel: 'Not Between',
				vtype: 'integerArray',
				emptyText: '[5,6]'
			}]
		}];
	
		this.on('afterrender', function ( aWindow ){
			aWindow.down('form').down('textfield[name=in]').emptyText = "[1, 2, 4]";
			aWindow.down('form').down('textfield[name=in]').vtype = 'longArray';
			aWindow.down('form').down('textfield[name=not_in]').emptyText = "[3]";
			aWindow.down('form').down('textfield[name=not_in]').vtype = 'longArray';
		});

		this.callParent(arguments);
	}
});
