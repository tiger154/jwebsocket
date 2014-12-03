/*
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernández
 */
Ext.Loader.setConfig({
	enabled: true,
	// Don't set to true, it's easier to use the debugger option to disable caching
	disableCaching: false,
	paths: {
		'Ext.jws': '../../lib/ExtJS/jWebSocketSenchaPlugIn/'
	}
});
Ext.require([
	'Ext.data.*',
	'Ext.grid.*',
	'Ext.form.*',
	'Ext.jws.Client',
	'Ext.jws.data.Proxy',
	'Ext.jws.form.action.Load',
	'Ext.jws.form.action.Submit'
]);

Ext.define('User', {
	extend: 'Ext.data.Model',
	fields: [{
			name: 'id',
			type: 'int',
			useNull: true
		},
		'name',
		'email', {
			name: 'age',
			type: 'int',
			useNull: true
		}]
});

jws.ExtJSDemo = {
	NS_EXTJS_DEMO: jws.NS_BASE + '.plugins.sencha',
	// Type of tokens
	TT_OPEN: 'open',
	TT_CLOSE: 'close',
	TT_LOGON: 'logon',
	TT_REGISTER: 'register',
	TT_WELCOME: 'welcome',
	TT_CREATE: 'create',
	TT_READ: 'read',
	TT_UPDATE: 'update',
	TT_DESTROY: 'destroy',
	TT_RESET: 'reset',
	TT_NOTIFY_CREATE: 'notifyCreate',
	TT_NOTIFY_UPDATE: 'notifyUpdate',
	TT_NOTIFY_DESTROY: 'notifyDestroy',
	TT_NOTIFY_RESET: 'notifyReset',
	// Texts
	TEXT_CONNECTED: "connected",
	TEXT_AUTHENTICATED: "authenticated",
	TEXT_DISCONNECTED: "disconnected",
	TEXT_WEBSOCKET: "WebSocket: ",
	TEXT_CLIENT_ID: "Client-ID: ",
	// Styles
	CLS_AUTH: "authenticated",
	CLS_ONLINE: "online",
	CLS_OFFLINE: "offline"
};
Ext.onReady(function() {
	// DOM elements
	var eDisconnectMessage = Ext.get("not_connected"),
			eBtnDisconnect = Ext.get("disconnect_button"),
			eBtnConnect = Ext.get("connect_button"),
			eClient = document.getElementById("client_status");
	eClientId = document.getElementById("client_id");
	eWebSocketType = document.getElementById("websocket_type");

	Ext.jwsClient.on(jws.ExtJSDemo.TT_OPEN, function() {
		// Registering to the Sencha demo to receive notifications 
		// from the server when other clients create, update or remove 
		// users from the server users list
		eClient.innerHTML = jws.ExtJSDemo.TEXT_CONNECTED;
		eClient.className = jws.ExtJSDemo.CLS_ONLINE;

		eBtnDisconnect.show();
		eBtnConnect.hide();
		eDisconnectMessage.hide();
	});

	Ext.jwsClient.on(jws.ExtJSDemo.TT_WELCOME, function(aToken) {
		if (aToken.username !== "anonymous") {
			Ext.jwsClient.fireEvent(jws.ExtJSDemo.TT_LOGON, aToken, Ext.jwsClient);
		} else {
			Ext.jwsClient.getConnection().systemLogon(jws.DEMO_ROOT_LOGINNAME, jws.DEMO_ROOT_PASSWORD);
		}
	});

	Ext.jwsClient.on(jws.ExtJSDemo.TT_LOGON, function(aToken) {
		if (aToken.username) {
			eClient.innerHTML = aToken.username;
		}
		Ext.jwsClient.send(jws.ExtJSDemo.NS_EXTJS_DEMO, jws.ExtJSDemo.TT_REGISTER);

		eClient.className = jws.ExtJSDemo.CLS_AUTH;

		eBtnDisconnect.show();
		eBtnConnect.hide();
		eDisconnectMessage.hide();
		initDemo();
	});

	Ext.jwsClient.on(jws.ExtJSDemo.TT_CLOSE, function() {
		eClient.innerHTML = jws.ExtJSDemo.TEXT_DISCONNECTED;
		eClient.className = jws.ExtJSDemo.CLS_OFFLINE;
		eDisconnectMessage.show();
		eBtnDisconnect.hide();
		eBtnConnect.show();
		exitDemo();
		eWebSocketType.innerHTML = jws.ExtJSDemo.TEXT_WEBSOCKET + "-";
		eClientId.innerHTML = jws.ExtJSDemo.TEXT_CLIENT_ID + "- ";
	});

	eBtnDisconnect.hide();
	eBtnConnect.on("click", function() {
		Ext.jwsClient.open();
	});

	eBtnDisconnect.on("click", function() {
		Ext.jwsClient.getConnection().systemLogoff({
			OnSuccess: function(aToken) {
				Ext.jwsClient.close();
			},
			OnFailure: function() {
				Ext.jwsClient.close();
			}
		});
	});
	Ext.jwsClient.open();
});

function initDemo() {
	Ext.tip.QuickTipManager.init();

	var lProxyCfg = {
		ns: jws.ExtJSDemo.NS_EXTJS_DEMO,
		api: {
			create: jws.ExtJSDemo.TT_CREATE,
			read: jws.ExtJSDemo.TT_READ,
			update: jws.ExtJSDemo.TT_UPDATE,
			destroy: jws.ExtJSDemo.TT_DESTROY
		},
		reader: {
			root: 'data',
			totalProperty: 'totalCount'
		}
	};

	var lJWSProxy = new Ext.jws.data.Proxy(lProxyCfg);

	var lStore = new Ext.data.Store({
//		autoSync: true,
		autoLoad: true,
		pageSize: 10,
		model: 'User',
		proxy: lJWSProxy,
		listeners: {
			write: function(aStore, aOperation) {
				var lRecord = aOperation.getRecords()[0],
						lName = Ext.String.capitalize(aOperation.action),
						lText;

				if (lName === jws.ExtJSDemo.TT_DESTROY) {
					lRecord = aOperation.records[0];
					lText = 'Destroyed';
				} else {
					lText = lName + 'd';
				}


				var lForm = lFormPanel.getForm();
				if (aOperation.action !== jws.ExtJSDemo.TT_DESTROY) {
					lForm.loadRecord(lRecord);

					Ext.getCmp('submit_button').setText('Update User');
				}

				var lMessage = Ext.String.format("{0} user: {1}", lText, lRecord.getId());
				log(0, lMessage);
			}
		}
	});

	var lRowEditor = Ext.create('Ext.grid.plugin.RowEditing', {
		listeners: {
			edit: function(aEdit) {
				if (aEdit.record.data) {
					if (aEdit.record.data.email && aEdit.record.data.name) {
						aEdit.store.save();
					}
				}
			}
		}
	});

	// create Vtype for vtype:'num'
	var lNumTest = /^[0-9]+$/;
	Ext.apply(Ext.form.field.VTypes, {
		num: function(aVal, aField) {
			return lNumTest.test(aVal);
		},
		// vtype Text property: The error text to display when the validation function returns false
		numText: 'Not a valid number.  Must be only numbers".'
	});


	//=====form============
	var lFormPanel = Ext.create('Ext.form.Panel', {
		frame: false,
		jwsSubmit: true,
		bodyPadding: 10,
		id: 'formPanelCreate',
		border: false,
		bodyStyle: 'background-color:#D8E4F2',
		fieldDefaults: {
			msgTarget: 'side'
		},
		items: [{
				xtype: 'hidden',
				name: 'id',
				id: 'id'
			}, {
				xtype: 'textfield',
				name: 'name',
				id: 'name',
				//vtype:'alphanum',
				fieldLabel: 'Name',
				allowBlank: false,
				emptyText: 'required...',
				blankText: 'required',
				minLength: 2
			}, {
				xtype: 'textfield',
				name: 'email',
				id: 'email',
				fieldLabel: 'email',
				vtype: 'email',
				allowBlank: false,
				emptyText: 'required...'
			}, {
				xtype: 'textfield',
				name: 'age',
				id: 'age',
				fieldLabel: 'Age',
				vtype: 'num',
				emptyText: 'required...',
				allowBlank: false
			}, {
				xtype: 'button',
				text: 'Add User',
				id: 'submit_button',
				width: 120,
				handler: function() {

					var lForm = this.up('form').getForm();

					var lAction = null;
					if (lForm.findField('id').getValue() !== "") {
						lAction = {
							ns: jws.ExtJSDemo.NS_EXTJS_DEMO,
							tokentype: jws.ExtJSDemo.TT_UPDATE,
							params: {
								updateForm: 'yes'
							}
						};
					} else {
						lAction = {
							ns: jws.ExtJSDemo.NS_EXTJS_DEMO,
							tokentype: jws.ExtJSDemo.TT_CREATE
						};
					}
					lAction.failure = function(aForm, aAction) {
						if (aAction === 'undefined') {
							var message = "Please you have errors in the form";
							log(-1, message);
						} else {
							log(-1, aAction.response.message);

						}
					};

					lAction.success = function(aForm, aAction) {
						Ext.getCmp('submit_button').setText('Add User');
						aForm.reset();
						log(aAction.response.code, aAction.response.message);
					};

					if (lForm.isValid())
						lForm.submit(lAction);
					else {
						var lMessage = "Please you have errors in the form";
						log(-1, lMessage);
					}
				}
			},
			{
				xtype: 'button',
				text: 'Reset',
				width: 120,
				handler: function() {
					var lForm = this.up('form').getForm();
					Ext.getCmp('submit_button').setText('Add User');
					lForm.reset();
				}
			}]
	});

	//=====gridPanel=======
	var lGridPanel = Ext.create('Ext.grid.Panel', {
		store: lStore,
		border: false,
		frame: false,
		plugins: [lRowEditor],
		width: '100%',
		height: '100%',
		mLastSelected: -1,
		viewConfig: {
			loadMask: false
		},
		iconCls: 'icon-user',
		columns: [{
				text: 'ID',
				width: 45,
				sortable: true,
				dataIndex: 'id'
			}, {
				text: 'Name',
				width: 125,
				sortable: true,
				dataIndex: 'name',
				field: {
					xtype: 'textfield',
					allowBlank: false
							//					vtype: 'alpha'
				}
			}, {
				header: 'Email',
				width: 160,
				sortable: true,
				dataIndex: 'email',
				field: {
					xtype: 'textfield',
					allowBlank: false,
					vtype: 'email'
				}
			}, {
				text: 'Age',
				width: 45,
				flex: 1,
				sortable: true,
				dataIndex: 'age',
				field: {
					xtype: 'textfield',
					vtype: 'num',
					allowBlank: false
				}
			}],
		listeners: {
			select: function(aView, aRecord) {
				lGridPanel.mLastSelected = aRecord.index;
				var lForm = lFormPanel.getForm(),
						lAction = {
							ns: jws.ExtJSDemo.NS_EXTJS_DEMO,
							tokentype: jws.ExtJSDemo.TT_READ,
							params: {
								id: lGridPanel.mLastSelected
							}
							// Optional
//					success: function(aForm, aToken) {
//						console.log("success");
//						console.log(aToken);
//					},
//					failure: function(aForm, aToken) {
//						console.log("failure");
//						console.log(aToken);
//					}
						};

				// This action in this case is not necessary but is a real 
				// example how the jWebSocket implementation for the load 
				// action works, this could simply be changed by a 
				// lForm.loadRecord(aRecord.index)
				lForm.load(lAction);

				Ext.getCmp('submit_button').setText('Update User');
			}
		},
		dockedItems: [{
				xtype: 'toolbar',
				items: [{
						text: 'Add',
						iconCls: 'icon-add',
						handler: function(aAction) {
							var lPhantoms = lStore.getNewRecords();
							Ext.Array.each(lPhantoms, function(el) {
								lStore.remove(el);
							});

							lStore.insert(0, new User());

							lRowEditor.startEdit(0, 0);
						}
					}, '-', {
						itemId: 'delete',
						text: 'Delete',
						iconCls: 'icon-delete',
						disabled: true,
						handler: function() {

							var lSelection = lGridPanel.getView().getSelectionModel().getSelection()[0];
							if (lSelection) {
								var lId = lSelection.data.id;
								var lForm = Ext.getCmp('formPanelCreate').getForm();
								lForm.reset();
								lStore.remove(lSelection);
								lStore.save();
							}
						}
					}, '-', {
						itemId: 'reset',
						text: 'Reset to default',
						iconCls: 'icon-reset',
						disabled: false,
						handler: function() {
							Ext.jwsClient.send(jws.ExtJSDemo.NS_EXTJS_DEMO, jws.ExtJSDemo.TT_RESET);
							var lSelection = lGridPanel.getView().getSelectionModel().getSelection()[0];
							if (lSelection) {
								var lId = lSelection.data.id;
								var lForm = Ext.getCmp('formPanelCreate').getForm();
								lForm.reset();
							}
						}
					}]
			}],
		bbar: Ext.create('Ext.PagingToolbar', {
			store: lStore,
			displayInfo: true,
			displayMsg: 'Users {0} - {1} of {2}',
			emptyMsg: "No rows to display"
		})
	});
	lGridPanel.getSelectionModel().on('selectionchange', function(aSelModel, aSelections) {
		lGridPanel.down('#delete').setDisabled(aSelections.length === 0);
	});
	lGridPanel.getView().on('beforeitemkeydown', function(aView, aRecord, aItem, aIdx, aEvent) {
		if (aEvent.keyCode === 13)
			lFormPanel.loadRecord(aRecord);

	});
	function log(aType, aMsg) {
		var lBody = Ext.get('console');
		if (aType === 0) {
			lBody.update('<i>Last action</i><br> \n\
                <b style=color:green> Message: </b> ' + aMsg);
		} else if (aType === -1) {
			lBody.update('<i>Last action</i><br>\n\
                <b style=color:red> Message: </b> ' + aMsg);
		}
	}

	var lTabPanel = Ext.create('Ext.tab.Panel', {
		width: 280,
		height: 180,
		activeTab: 0,
		items: [{
				title: 'Output Messages',
				id: 'message',
				bodyStyle: 'padding:5px;',
				html: '<div id="console"></div>'
			}, {
				title: 'About',
				contentEl: 'contact'
			}]
	});

	Ext.create('Ext.window.Window', {
		title: 'ExtJS form jWebSocket demo',
		x: 10,
		id: "formDemo",
		resizable: false,
		draggable: false,
		y: 125,
		closable: false,
		layout: 'fit',
		width: 290,
		items: [lFormPanel]
	}).show();

	Ext.create('Ext.window.Window', {
		title: 'ExtJS Grid jWebSocket demo',
		layout: 'fit',
		id: "gridDemo",
		y: 125,
		x: 310,
		bodyStyle: 'float: right;position:relative;padding:5px',
		width: 400,
		height: 382,
		resizable: false,
		draggable: false,
		closable: false,
		items: [lGridPanel]
	}).show();

	Ext.create('Ext.window.Window', {
		title: 'Console jWebSocket demo',
		x: 10,
		y: 295,
		id: "consoleDemo",
		border: false,
		closable: false,
		resizable: false,
		draggable: false,
		layout: 'fit',
		items: [lTabPanel]
	}).show();

	Ext.jwsClient.addPlugIn({
		processToken: function(aToken) {
			if (aToken.ns === jws.ExtJSDemo.NS_EXTJS_DEMO) {
				if (aToken.type === jws.ExtJSDemo.TT_NOTIFY_CREATE ||
						aToken.type === jws.ExtJSDemo.TT_NOTIFY_UPDATE ||
						aToken.type === jws.ExtJSDemo.TT_NOTIFY_DESTROY ||
						aToken.type === jws.ExtJSDemo.TT_NOTIFY_RESET) {
					log(0, aToken.message);
					var lOptions = {};
					if (aToken.type === jws.ExtJSDemo.TT_NOTIFY_UPDATE) {
						lOptions = {
							callback: function() {
								lGridPanel.getSelectionModel().select(lGridPanel.mLastSelected);
							}
						}
					}
					lStore.load(lOptions);
				}
			}
			if (aToken.type === "welcome") {
				eClientId.innerHTML = jws.ExtJSDemo.TEXT_CLIENT_ID + aToken.sourceId;
				eWebSocketType.innerHTML = jws.ExtJSDemo.TEXT_WEBSOCKET + (jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)");
			}
		}
	});
}

function exitDemo() {
	var lWindowForm = Ext.WindowManager.get("formDemo");
	var lWindowGrid = Ext.WindowManager.get("gridDemo");
	var lWindowConsole = Ext.WindowManager.get("consoleDemo");
	if (lWindowForm !== undefined)
		lWindowForm.close();
	if (lWindowGrid !== undefined)
		lWindowGrid.close();
	if (lWindowConsole !== undefined)
		lWindowConsole.close();
}