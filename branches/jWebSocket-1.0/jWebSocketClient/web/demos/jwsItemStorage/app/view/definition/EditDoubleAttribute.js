Ext.define('IS.view.definition.EditDoubleAttribute', {
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
				name: 'default'
			},{
				xtype: 'numberfield',
				name : 'min_value',
				fieldLabel: 'Min Value'
			},{
				xtype: 'numberfield',
				name : 'max_value',
				fieldLabel: 'Max Value',
				vtype: 'max'
			},{
				xtype: 'arrayfield',
				name: 'between',
				fieldLabel: 'Between',
				vtype: 'doubleArray',
				emptyText: '[-5.1,5.1]'
			}, {
				xtype: 'arrayfield',
				name: 'not_between',
				fieldLabel: 'Not Between',
				vtype: 'doubleArray',
				emptyText: '[-1.1,1.1]'
			}]
		}];
	
		this.on('afterrender', function ( aWindow ){
			aWindow.down('form').down('textfield[name=in]').emptyText = "[1.1, 2.1, 4.1]";
			aWindow.down('form').down('textfield[name=in]').vtype = 'doubleArray';
			aWindow.down('form').down('textfield[name=not_in]').emptyText = "[3.1]";
			aWindow.down('form').down('textfield[name=not_in]').vtype = 'doubleArray';
		});

		this.callParent(arguments);
	}
});
