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
		width: 600,
		html: '<h1>jWebSocket ItemStoragePlugIn administration tool</h1>'
	},{
		xtype: 'button',
		id: 'logoff_button',
		text: 'Logoff',
		handler: function() {
			Ext.jws.getConnection().logout();
		}
	},{
		xtype: 'button',
		id: 'help_button',
		href: 'http://jwebsocket.org/plugins/itemstorage',
		text: 'Help'
	},{
		xtype: 'button',
		id: 'about_button',
		text: 'About',
		handler: function() {
			Ext.Msg.show({
				msg: '<center>jWebSocket ItemStorage Admin GUI v1.0 <br> Copyright (c) 2013 Innotrade GmbH <p>&nbsp<p><a href="http://jwebsocket.org">http://jwebsocket.org</a></center>', 
				buttons: Ext.Msg.OK, 
				icon: Ext.Msg.INFO
			});
		}
	}]
});
