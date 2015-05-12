Ext.application({
	name: 'CRUD',
	appFolder: 'app',
	controllers: ['CWork', 'CGrid'],
	launch: function () {
		Ext.Msg.render(Ext.get('demo_box'));
		Ext.create('Ext.window.Window', {
			title: 'User Loggin',
			x: 200,
			y: 200,
			height: 170,
			width: 300,
			renderTo: Ext.get('main_content'),
			draggable: false,
			closable: false,
			resizable: false,
			layout: 'fit',
			items: [{
					xtype: 'vwork'
				}]
		}).show();
	}
});