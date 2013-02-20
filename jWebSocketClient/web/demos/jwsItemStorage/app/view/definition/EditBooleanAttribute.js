Ext.define('IS.view.definition.EditBooleanAttribute', {
	extend: 'IS.view.definition.EditAttribute',

	initComponent: function() {
		this.items = [{
			xtype: 'form',
			bodyPadding: 10,
			border: 0,
			autoScroll: true,
			items: [{
				xtype: 'checkbox',
				fieldLabel: 'Default',
				name: 'default'
			}]
		}];
	
		this.on('afterrender', function ( aWindow ){
			aWindow.down('form').down('textfield[name=in]').emptyText = "[true]";
			aWindow.down('form').down('textfield[name=in]').vtype = 'booleanArray';
			aWindow.down('form').down('textfield[name=not_in]').emptyText = "[]";
			aWindow.down('form').down('textfield[name=not_in]').vtype = 'booleanArray';
		});

		this.callParent(arguments);
	}
});
