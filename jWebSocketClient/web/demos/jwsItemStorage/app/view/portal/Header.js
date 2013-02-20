Ext.define('IS.view.portal.Header' ,{
	extend: 'Ext.panel.Panel',
	alias: 'widget.p_header',
	layout: 'column',
	region: 'north',
	maxWidth: 750,
	border: 0,
	padding: 5,
	items: [{
		xtype: 'panel',
		border: 0,
		width: 400,
		html: '<h1>ItemStoragePlugIn administration tool</h1>'
	},{
		xtype: 'button',
		id: 'logoff_button',
		text: 'Logoff',
		handler: function() {
			Ext.jws.getConnection().logout();
		}
	}]
});
