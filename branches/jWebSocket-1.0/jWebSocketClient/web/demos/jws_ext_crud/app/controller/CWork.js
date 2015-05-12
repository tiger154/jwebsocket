Ext.define('CRUD.controller.CWork', {
	extend: 'Ext.app.Controller',
	views: ['VWork'],
	///models:['MWork'],
	stores: ['SWork'],
	init: function () {
		this.callParent(arguments);
		Ext.jwsClient.on('OnWelcome', function () {
			Ext.ComponentQuery.query('vwork displayfield')[0].setValue('Success');
			Ext.ComponentQuery.query('vwork displayfield')[0].setFieldStyle('color:green');
		});

		Ext.jwsClient.on('OnClose', function () {
			var myWindow = Ext.ComponentQuery.query('window')[1];

			myWindow.removeAll(true);
			myWindow.setTitle('User Loggin');

			myWindow.animate({
				to: {
					x: 200,
					y: 200,
					width: 300,
					height: 170
				}
			});

			myWindow.add({
				xtype: 'vwork'
			});
		});

		this.control({
			'vwork button:first': {
				'click': this.onClick
			}
		});
	},
	onClick: function (aButton) {
		// console.log(Ext.ComponentQuery.query('vwork displayfield')[0].getValue());
		if (Ext.jwsClient.getConnection().fStatus === 1) {
			var lForm = aButton.up('form');
			// Note: arguments are not included because they are extracted
			// internally by the PlugIn
			lForm.submit({
				// Scripting PlugIn parameters
				params: {
					kind: 'jws',
					objectId: 'Crud',
					method: 'authenticate',
					app: 'jws_extjs_crud'
				},
				success: function (aForm, aResponse) {
					var lResult = Ext.decode(aResponse.result);
					if (lResult.success) {
						var lWindow = Ext.ComponentQuery.query('window')[1];
						lWindow.setTitle('jWebSocket Sencha Demo using Scripting Plugin and JavaScript Server-Side');
						Ext.ComponentQuery.query('vwork')[0].destroy();
						lWindow.animate({
							to: {
								x: 30,
								y: 130,
								width: 660,
								height: 350
							}
						});
						lWindow.add(new Ext.create('CRUD.view.VGrid'));
					}
					else {
						Ext.Msg.show({
							title: 'Error',
							msg: 'User Denied!! Contact with Admin',
							buttons: Ext.Msg.OK,
							fn: function () {
								Ext.ComponentQuery.query('vwork')[0].getForm().reset();
								Ext.ComponentQuery.query('vwork textfield')[0].focus();

								Ext.ComponentQuery.query('vwork displayfield')[0].setValue('Success');

							},
							icon: Ext.MessageBox.ERROR
						});
					}
				}
			});

		} else {
			Ext.Msg.show({
				title: 'Error',
				msg: 'The jwebsocket server isn\'t running, please try later!',
				buttons: Ext.Msg.OK,
				fn: function () {
					Ext.ComponentQuery.query('vwork')[0].getForm().reset();
					Ext.ComponentQuery.query('vwork textfield')[0].focus();
				},
				icon: Ext.MessageBox.ERROR
			});
		}
		//Ext.Msg.alert('Error','The jwebsocket server isn´t working');
	},
	onSuccess: function (resp) {
		console.log(resp.result);
	}
});

