Ext.define('IS.view.portal.Right' ,{
	extend: 'Ext.panel.Panel',
	alias: 'widget.p_right',
	width: 600,
	border: 0,
	padding: 5,
	items: [{
		xtype: 'tabpanel',
		id: 'workspace',
		height: 500,
		activeTab: 0,
		items: [{
			id: '$intro',
			title: '.: Introduction :.',
			items: [{
					xtype: 'p_introduction'
			}]
		}]
	}]
});
