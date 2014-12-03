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
		border:0,
		minWidth: 150,
		html: '<img src="resources/images/jwebsocket_logo.png"/> '
	},{
		xtype: 'panel',
		border: 0,
		html: '<h1>ItemStoragePlugIn administration tool</h1> <div class="header_text">Synchronizing cross-platform application data in real-time.</div>',
		padding: '10 10 0 0'
	},{
		xtype: 'button',
		id: 'cleardb_button',
		text: 'Clear Database',
		handler: function() {
			Ext.Msg.show({
				title:'Confirm?',
				msg: 'This operation will remove all data (collections and definitions)! Are you sure to clear the item storage database?',
				buttons: Ext.Msg.YESNO,
				icon: Ext.Msg.QUESTION,
				fn: function ( aButton ){
					if ('yes' != aButton)
						return;
					
					Ext.jws.send(jws.ItemStoragePlugIn.NS, 'clearDatabase', {}, {
						success: function (){
							Ext.Msg.show({
								title:'Information',
								msg: 'ItemStorage database has been cleared successfully!',
								buttons: Ext.Msg.OK,
								icon: Ext.Msg.INFO,
								fn: function (){
									Ext.ComponentQuery.query('#contenttabpanel')[0].setActiveTab('collectionstab');
									Ext.ComponentQuery.query('#contenttabpanel')[0].setActiveTab('definitionstab');
								}
							});
						}
					});
				} 
			});
		}
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
		text: 'Help',
		target: '_blank'
	},{
		xtype: 'button',
		id: 'about_button',
		text: 'About',
		handler: function() {
			Ext.Msg.show({
				msg: '<center>jWebSocket ItemStorage Web Admin v1.0 <br> Copyright (c) 2013 Innotrade GmbH <p>&nbsp<p><a href="http://jwebsocket.org">http://jwebsocket.org</a></center>', 
				buttons: Ext.Msg.OK, 
				icon: Ext.Msg.INFO
			});
		}
	}]
});
