Ext.require(['Ext.data.*', 'Ext.chart.*']);
Ext.BLANK_IMAGE_URL = 's.gif';

Ext.require([
	'Ext.form.field.ComboBox',
	'Ext.form.FieldSet',
	'Ext.tip.QuickTipManager',
	'Ext.data.*'
]);
NS_MONITORING = jws.NS_BASE + ".plugins.monitoring";
NS_SYSTEM = jws.NS_BASE + ".plugins.system";

// Token types
TT_COMPUTER_INFO = "computerInfo";
TT_USER_INFO = "userInfo";
TT_SERVER_XCHG_INFO = "serverXchgInfo";
TT_SERVER_XCHG_INFO_X_DAYS = "serverXchgInfoXDays";
TT_SERVER_XCHG_INFO_X_MONTH = "serverXchgInfoXMonth";
TT_PLUGINS_INFO = "pluginsInfo";
TT_WELCOME = "welcome";
TT_REGISTER = "register";

Ext.onReady(function( ) {
	Ext.tip.QuickTipManager.init( );

	// Namespaces
	var
			lClientStatus = Ext.get("client_status"),
			lClientId = Ext.get("client_id"),
			lWebSocketType = Ext.get("websocket_type");

	lWebSocketType.dom.innerHTML = "WebSocket: " +
			(jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)");

	Ext.jws.open(jws.getAutoServerURL());

	Ext.jws.on('open', function( ) {
		var lToken = {
			interest: TT_COMPUTER_INFO
		};
		Ext.jws.send(NS_MONITORING, TT_REGISTER, lToken);
	});

	Ext.jws.on('close', function( ) {
		lClientId.dom.innerHTML = "Client-ID: - ";
		lClientStatus.dom.innerHTML = "disconnected";
		lClientStatus.dom.className = "offline";
	});

	initDemo( );

});

function initDemo( ) {
	Ext.jws.send(NS_MONITORING, TT_REGISTER);

	var myStore = Ext.create('Ext.data.JsonStore', {
		fields: ['name', 'Memory', 'Cpu', 'Swap']
	});

	var myStorePie = Ext.create('Ext.data.JsonStore', {
		fields: ['name', 'Total Hdd Space', 'Free', 'Used']
	});

	var myStoreExchange = Ext.create('Ext.data.JsonStore', {
		fields: ['name', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'10', '11', '12', '13', '14', '15', '16', '17', '18',
			'19', '20', '21', '22', '23', '24']
	});

	var myStoreExchangeForDays = Ext.create('Ext.data.JsonStore', {
		fields: ['name', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'10', '11', '12', '13', '14', '15', '16', '17', '18',
			'19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31']
	});

	var myStoreExchangeForMonth = Ext.create('Ext.data.JsonStore', {
		fields: ['name', '2012', '2013', '2014', '2015']
	});

	var myStoreUsersOnline = Ext.create('Ext.data.JsonStore', {
		fields: ['name', 'Users Online']
	});

	var myStoreUsePlugins = Ext.create('Ext.data.JsonStore', {
		fields: ['name', 'Plugins in Use']
	});

	var lPlugin = {};
	var monthError = false;
	var yearError = false;

	lPlugin.processToken = function(aToken) {
		if (aToken.ns === NS_MONITORING) {
			if (aToken.type === TT_COMPUTER_INFO) {
				var memory = parseInt(aToken.usedMemPercent);
				var totalConsumeCPU = parseFloat(aToken.consumeTotal);
				var cpu = aToken.consumeCPUCharts;
				var dataToShow = new Array( );
				for (var i = 0; i < cpu.length; i++) {
					dataToShow.push({
						name: 'CPU ' + (i + 1),
						data: parseFloat(cpu[i])
					});
				}

				var swap = parseInt(aToken.swapPercent);

				dataToShow.push(
						{
							name: 'Total CPU',
							data: totalConsumeCPU
						});
				dataToShow.push({
					name: 'Memory',
					data: memory
				});
				dataToShow.push({
					name: 'Swap',
					data: swap
				});
				myStore.loadData(dataToShow);


				var freehd = parseInt(aToken.freeHddSpace);
				var usedhd = parseInt(aToken.usedHddSpace);

				var show = [
					{
						name: 'Free Hdd Space',
						data: freehd
					}, {
						name: 'Used Hdd Space',
						data: usedhd
					}];
				myStorePie.loadData(show);

			}
			else if (aToken.type === TT_USER_INFO) {

				var lConnUsers = aToken.connectedUsers,
						lOnlineUsers = [];
				// Rendering 60 seconds of connected users in the graph
				for (var lKey = 59; lKey >= 0; lKey--) {
					lOnlineUsers.push({
						name: 59 - lKey,
						data: lConnUsers[ lKey ]
					});
				}
				myStoreUsersOnline.loadData(lOnlineUsers);
			}
			else if (aToken.type === TT_SERVER_XCHG_INFO) {

				if (aToken.exchanges == null) {
					Ext.MessageBox.alert('Information', 'There were not requests to \n\
					the server this day.');
				}
				else
				{
					var lExchangeInfo = [], lCounter = 0;

					for (var lKey in aToken.exchanges) {
						if (lKey.toString()[0] === 'h') {
							lExchangeInfo.push({
								name: lKey.substr(1, lKey.length),
								data: parseInt(aToken.exchanges[ lKey ]) || 0
							});
						}
					}
					myStoreExchange.loadData(lExchangeInfo);

				}

			}
			else if (aToken.type === TT_SERVER_XCHG_INFO_X_DAYS) {
				if (aToken.code === -1) {
					if (!monthError) {
						Ext.MessageBox.alert("Information", "There is not information \n\
						for this month in the server");
					}
					monthError = true;

				}
				else
				{
					monthError = false;
					var lExchangeInfoForDays = [];
					for (var lDay = 1; lDay <= 31; lDay++) {
						lExchangeInfoForDays.push({
							name: lDay,
							data: aToken[(lDay < 10) ? '0' + lDay : lDay] || 0
						});
					}
					myStoreExchangeForDays.loadData(lExchangeInfoForDays);
				}
			}

			else if (aToken.type === TT_SERVER_XCHG_INFO_X_MONTH) {
				if (aToken.code == -1) {
					if (yearError) {
						Ext.MessageBox.alert("Information", "There is not information \n\
						for this year in the server");
					}
				}
				else
				{
					yearError = false;
					var lExchangeInfoForMonth = [
						{
							name: 'Jan',
							data: aToken["01"] || 0
						}, {
							name: 'Feb',
							data: aToken["02"] || 0
						}, {
							name: 'Mar',
							data: aToken["03"] || 0
						}, {
							name: 'Apr',
							data: aToken["04"] || 0
						}, {
							name: 'May',
							data: aToken["05"] || 0
						}, {
							name: 'Jun',
							data: aToken["06"] || 0
						}, {
							name: 'Jul',
							data: aToken["07"] || 0
						}, {
							name: 'Aug',
							data: aToken["08"] || 0
						}, {
							name: 'Sep',
							data: aToken["09"] || 0
						}, {
							name: 'Oct',
							data: aToken["10"] || 0
						}, {
							name: 'Nov',
							data: aToken["11"] || 0
						}, {
							name: 'Dec',
							data: aToken["12"] || 0
						}];
					myStoreExchangeForMonth.loadData(lExchangeInfoForMonth);
				}
			}

			else if (aToken.type === TT_PLUGINS_INFO) {
				var lUsePlugins = new Array( );
				var lPluginsInUse = aToken.usePlugins;
				if (lPluginsInUse.length === 0) {
					Ext.MessageBox.alert('No Plug-ins', 'No plugIn has been used.');
				}
				else
				{
					for (var j = 0; j < lPluginsInUse.length; j++) {
						lUsePlugins.push({
							name: aToken.usePlugins[j].id,
							data: aToken.usePlugins[j].requests || 0
						});
					}

					myStoreUsePlugins.loadData(lUsePlugins);
				}
			}
		}

		else if (aToken.ns === NS_SYSTEM) {
			if (aToken.type === TT_WELCOME) {
				Ext.get("client_id").dom.innerHTML = "Client-ID: " + aToken.sourceId;
				Ext.get("client_status").dom.innerHTML = "online";
				Ext.get("client_status").dom.className = "authenticated";
			}
		}
	}
	Ext.jws.addPlugIn(lPlugin);


	//**************** graphical representations*****************

	// Graphic representation of the resources of the machine
	var graph = new Ext.TabPanel({
		renderTo: Ext.getBody( ),
		id: 'main-tabs',
		height: 380,
		width: 400,
		activeTab: 0,
		border: false,
		margin: 0,
		defaults: {
			padding: 10
		},
		//hideTabStripItem:true,
		bodyStyle: 'background:#e4e4e4;',
		items: [
			{
				title: 'Machine Resources',
				id: 'tab1',
				layout: 'fit',
				border: false,
				items: {
					xtype: 'chart',
					border: false,
					style: 'background:#e4e4e4',
					animate: false,
					shadow: false,
					store: myStore,
					axes: [{
							type: 'Numeric',
							position: 'left',
							fields: ['data'],
							label: {
								renderer: Ext.util.Format.numberRenderer('0,0')
							},
							//title: 'Number of Hits',
							grid: true,
							minimum: 0,
							maximum: 100
						}, {
							type: 'Category',
							position: 'bottom',
							fields: ['name'],
							title: 'Machine Resources'
						}],
					series: [{
							type: 'column',
							axis: 'left',
							//gutter: 150,
							highlight: false,
							tips: {
								trackMouse: true,
								width: 100,
								height: 28,
								renderer: function(storeItem, item) {
									this.setTitle(storeItem.get('name') + ': '
											+ storeItem.get('data') + ' %');
								}
							},
							label: {
								display: 'outside',
								'text-anchor': 'middle',
								field: 'data',
								renderer: Ext.util.Format.numberRenderer('0.0'),
								orientation: 'horizontal',
								color: '#333'
							},
							xField: 'name',
							yField: 'data'
						}]
				}
			},
			{
				title: 'Hard Disk Data',
				id: 'tab2',
				border: false,
				layout: 'fit',
				items: {
					xtype: 'chart',
					id: 'chartCmp',
					animate: true,
					border: false,
					store: myStorePie,
					background: {
						fill: '#e4e4e4'
					},
					shadow: true,
					legend: {
						position: 'right'
					},
					//insetPadding: 60,
					theme: 'Base:gradients',
					series: [{
							type: 'pie',
							field: 'data',
							showInLegend: true,
							//  donut: donut,
							tips: {
								trackMouse: true,
								width: 90,
								height: 35,
								renderer: function(storeItem, item) {
									//calculate percentage.
									var total = 0;
									myStorePie.each(function(rec) {
										total += rec.get('data');
									});
									this.setTitle(storeItem.get('name') + ': '
											+ Math.round(storeItem.get('data')
											/ total * 100) + '%');
								}
							},
							/* highlight: {
							 segment: {
							 margin: 20
							 }
							 },*/
							label: {
								field: 'name',
								display: 'rotate',
								contrast: true,
								font: '10px Arial'
							}
						}]
				}

			}

		]

	});

	// Graphic representation of the server requests
	var graph_exchanges = new Ext.TabPanel({
		renderTo: Ext.getBody( ),
		id: 'exchanges-main-tabs',
		height: 380,
		width: 400,
		activeTab: 0,
		border: false,
		margin: 0,
		defaults: {
			padding: 10
		},
		hideTabStripItem: true,
		bodyStyle: 'background:#e4e4e4;',
		items: [{
				title: 'Exchanges to Hours',
				id: 'tab3',
				layout: 'fit',
				border: false,
				items: {
					xtype: 'chart',
					style: 'background:#e4e4e4',
					animate: false,
					border: false,
					shadow: false,
					store: myStoreExchange,
					axes: [{
							type: 'Numeric',
							position: 'left',
							fields: ['data'],
							label: {
								renderer: Ext.util.Format.numberRenderer('0,0')
							},
							title: 'Total Amount of Exchanges',
							grid: true,
							minimum: 0
						}, {
							type: 'Category',
							position: 'bottom',
							fields: ['name'],
							title: 'Time of Day'
						}],
					series: [{
							type: 'column',
							axis: 'left',
							highlight: false,
							tips: {
								trackMouse: true,
								width: 140,
								height: 28,
								renderer: function(storeItem, item) {
									this.setTitle(storeItem.get('name') + ': '
											+ storeItem.get('data') + ' requests');
								}
							},
							label: {
								display: 'outside',
								'text-anchor': 'middle',
								field: 'data',
								renderer: Ext.util.Format.numberRenderer('0'),
								orientation: 'horizontal',
								color: '#333'
							},
							xField: 'name',
							yField: 'data'
						}]
				}

			},
			{
				title: 'Exchanges to Days',
				id: 'tab4',
				layout: 'fit',
				border: false,
				items: {
					xtype: 'chart',
					style: 'background:#e4e4e4',
					animate: false,
					border: false,
					shadow: false,
					store: myStoreExchangeForDays,
					axes: [{
							type: 'Numeric',
							position: 'left',
							fields: ['data'],
							label: {
								renderer: Ext.util.Format.numberRenderer('0,0')
							},
							title: 'Total Amount of Exchanges',
							grid: true,
							minimum: 0
						}, {
							type: 'Category',
							position: 'bottom',
							fields: ['name'],
							title: 'Time of Month'
						}],
					series: [{
							type: 'column',
							axis: 'left',
							highlight: false,
							tips: {
								trackMouse: true,
								width: 140,
								height: 28,
								renderer: function(storeItem, item) {
									this.setTitle(storeItem.get('name') + ': '
											+ storeItem.get('data') + ' requests');
								}
							},
							label: {
								display: 'outside',
								'text-anchor': 'middle',
								field: 'data',
								renderer: Ext.util.Format.numberRenderer('0'),
								orientation: 'horizontal',
								color: '#333'
							},
							xField: 'name',
							yField: 'data'
						}]
				}

			},
			{
				title: 'Exchanges to Months',
				id: 'tab5',
				layout: 'fit',
				border: false,
				items: {
					xtype: 'chart',
					style: 'background:#e4e4e4',
					animate: false,
					border: false,
					shadow: false,
					store: myStoreExchangeForMonth,
					axes: [{
							type: 'Numeric',
							position: 'left',
							fields: ['data'],
							label: {
								renderer: Ext.util.Format.numberRenderer('0,0')
							},
							title: 'Total Amount of Exchanges',
							grid: true,
							minimum: 0
						}, {
							type: 'Category',
							position: 'bottom',
							fields: ['name'],
							title: 'Time of Year'
						}],
					series: [{
							type: 'column',
							axis: 'left',
							highlight: false,
							tips: {
								trackMouse: true,
								width: 140,
								height: 28,
								renderer: function(storeItem, item) {
									this.setTitle(storeItem.get('name') + ': '
											+ storeItem.get('data') + ' requests');
								}
							},
							label: {
								display: 'outside',
								'text-anchor': 'middle',
								field: 'data',
								renderer: Ext.util.Format.numberRenderer('0'),
								orientation: 'horizontal',
								color: '#333'
							},
							xField: 'name',
							yField: 'data'
						}]
				}

			}]
	});

	// Graphic representation of Users Online

	var graph_users_online = new Ext.TabPanel({
		renderTo: Ext.getBody( ),
		id: 'userOnline-main-tabs',
		layout: 'fit',
		height: 380,
		width: 400,
		border: false,
		activeTab: 0,
		margin: 0,
		defaults: {
			padding: 5
		},
		hideTabStripItem: true,
		bodyStyle: 'background:#e4e4e4;',
		items: [
			{
				bodyStyle: 'background:#e4e4e4;',
				title: 'Online Users',
				id: 'tab6',
				layout: 'fit',
				border: false,
				items: {
					xtype: 'chart',
					animate: true,
					store: myStoreUsersOnline,
					insetPadding: 10,
					axes: [{
							type: 'Numeric',
							minimum: 0,
							position: 'left',
							fields: ['data'],
							title: "Online Users",
							grid: true,
							label: {
								renderer: Ext.util.Format.numberRenderer('0,0'),
								font: '7pt Verdana'
							}
						}, {
							type: 'Category',
							position: 'bottom',
							fields: ['name'],
							title: "Seconds ago",
							label: {
								font: '7pt Verdana'
							}
						}],
					series: [{
							type: 'line',
							axis: 'left',
							xField: 'name',
							yField: 'data',
							tips: {
								trackMouse: true,
								width: 120,
								height: 40,
								renderer: function(storeItem, item) {
									this.setTitle(storeItem.get('name') + " Seconds ago"
											+ '<br />' + storeItem.get('data')
											+ " users online");
								}
							},
							style: {
								fill: '#38B8BF',
								stroke: '#38B8BF',
								'stroke-width': 1
							},
							markerConfig: {
								type: 'circle',
								size: 4,
								radius: 4,
								'stroke-width': 0,
								fill: '#38B8BF',
								stroke: '#38B8BF'
							}
						}]

				}
			}]

	});

	// Graphic representation of use of plugins
	var graph_usePlugins = new Ext.TabPanel({
		renderTo: Ext.getBody( ),
		id: 'usePlugins-tabs',
		height: 380,
		width: 400,
		activeTab: 0,
		border: false,
		margin: 0,
		defaults: {
			padding: 10
		},
		//hideTabStripItem:true,
		bodyStyle: 'background:#e4e4e4;',
		items: [
			{
				bodyStyle: 'background:#e4e4e4;',
				title: 'Use of Plugins',
				id: 'tab7',
				layout: 'fit',
				border: false,
				items: {
					xtype: 'chart',
					animate: false,
					shadow: false,
					store: myStoreUsePlugins,
					axes: [{
							type: 'Numeric',
							position: 'bottom',
							fields: ['data'],
							label: {
								renderer: Ext.util.Format.numberRenderer('0,0')
							},
							title: 'Number of Requests',
							grid: true,
							minimum: 0
									//maximum:100
						}, {
							type: 'Category',
							position: 'left',
							fields: ['name'],
							title: 'Plugins in Use'
						}],
					series: [{
							type: 'bar',
							axis: 'bottom',
							highlight: false,
							tips: {
								trackMouse: true,
								width: 140,
								height: 28,
								renderer: function(storeItem, item) {
									this.setTitle(storeItem.get('name') + ': '
											+ storeItem.get('data') + ' %');
								}
							},
							label: {
								display: 'insideEnd',
								field: 'data',
								renderer: Ext.util.Format.numberRenderer('0'),
								orientation: 'horizontal',
								color: '#333',
								'text-anchor': 'middle'
							},
							xField: 'name',
							yField: ['data']
						}]
				}
			}
		]
	});

	//********************************************************

	// Define the model for a State
	Ext.define('State', {
		extend: 'Ext.data.Model',
		fields: [
			{
				type: 'string',
				name: 'name'
			}
		]
	});

	// The data for all states
	var states = [{
			"name": "Machine Resources"
		}, {
			"name": "Exchanges on the server"
		}, {
			"name": "Online users"
		}, {
			"name": "Use of Plugins"
		}];

	var states1 = [{
			"name": "January"
		}, {
			"name": "February"
		}, {
			"name": "March"
		}, {
			"name": "April"
		}, {
			"name": "May"
		}, {
			"name": "June"
		}, {
			"name": "July"
		}, {
			"name": "August"
		}, {
			"name": "September"
		}, {
			"name": "October"
		}, {
			"name": "November"
		}, {
			"name": "December"
		}];

	var states2 = [{
			"name": "2012"
		}, {
			"name": "2013"
		}, {
			"name": "2014"
		}, {
			"name": "2015"
		}];

	// The data store holding the states; shared by each of the ComboBox 
	var store = Ext.create('Ext.data.Store', {
		model: 'State',
		data: states
	});

	var storeMonth = Ext.create('Ext.data.Store', {
		model: 'State',
		data: states1
	});

	var storeYear = Ext.create('Ext.data.Store', {
		model: 'State',
		data: states2
	});

	//Creating the combobox in the main view
	var comboGraphics = Ext.create('Ext.form.field.ComboBox', {
		fieldLabel: 'Data Source',
		labelAlign: 'right',
		labelWidth: 80,
		displayField: 'name',
		width: 260,
		alueField: 'value',
		emptyText: 'Select...',
		store: store,
		queryMode: 'local',
		typeAhead: true
	});

	//Creating the combobox in the main ExchangesXDays
	var comboExchangesXDays = Ext.create('Ext.form.field.ComboBox', {
		fieldLabel: 'Month',
		displayField: 'name',
		labelAlign: 'right',
		labelWidth: 80,
		width: 260,
		alueField: 'value',
		emptyText: 'Select...',
		store: storeMonth,
		queryMode: 'local',
		typeAhead: true
	});
	comboExchangesXDays.hide( );

	//Creating the combobox in the main ExchangesXMonth
	var comboExchangesXMonth = Ext.create('Ext.form.field.ComboBox', {
		fieldLabel: 'Year',
		displayField: 'name',
		labelAlign: 'right',
		labelWidth: 80,
		width: 260,
		alueField: 'value',
		emptyText: 'Select...',
		store: storeYear,
		queryMode: 'local',
		typeAhead: true
	});
	comboExchangesXMonth.hide( );

	//Creating the calendar
	var dateField = new Ext.form.DateField({
		fieldLabel: 'Date',
		emptyText: 'Insert a date...',
		format: 'M/d/y',
		labelAlign: 'right',
		labelWidth: 80,
		width: 260,
		minValue: '01/17/2012', //, //<-- day on which the server is started 
		//maxValue:'12-08-2013', // <-- max date  
		// value:new Date( )  // value field is initialized
		listeners: {
			select: function(A, fecha)
			{
				var date = A.rawValue;
				var array = date.split('/', 3);
				var mes = array[0];
				var dia = array[1];
				var anno = array[2];
				var mesBd;
				if (mes == 'Jan') {
					mesBd = '01';
				}
				if (mes == 'Feb') {
					mesBd = '02';
				}
				if (mes == 'Mar') {
					mesBd = '03';
				}
				if (mes == 'Apr') {
					mesBd = '04';
				}
				if (mes == 'May') {
					mesBd = '05';
				}
				if (mes == 'Jun') {
					mesBd = '06';
				}
				if (mes == 'Jul') {
					mesBd = '07';
				}
				if (mes == 'Aug') {
					mesBd = '08';
				}
				if (mes == 'Sep') {
					mesBd = '09';
				}
				if (mes == 'Oct') {
					mesBd = '10';
				}
				if (mes == 'Nov') {
					mesBd = '11';
				}
				if (mes == 'Dec') {
					mesBd = '12';
				}

				var modifiedDate = {
					interest: TT_SERVER_XCHG_INFO,
					month: mesBd,
					day: dia,
					year: anno
				};

				Ext.jws.send(NS_MONITORING, TT_REGISTER, modifiedDate);
			}
		}

	});

	// Adding the calendar to a panel
	var dateFieldPanel = new Ext.Panel({
		frame: false,
		border: false,
		width: 270,
		height: 46,
		x: 0,
		y: 0,
		layout: 'absolute',
		bodyStyle: 'background:#e4e4e4',
		items: [dateField]
	});
	dateFieldPanel.hide( );

	// Creating panel containing the combobox and calendar        
	var chooseData = new Ext.Panel({
		border: true,
		closable: false,
		resizable: false,
		modal: false,
		plain: true,
		draggable: false,
		frame: false,
		x: 400,
		y: -470,
		width: 290,
		bodyStyle: 'background:#e4e4e4;',
		height: 80,
		items: [comboGraphics, comboExchangesXDays, comboExchangesXMonth,
			dateFieldPanel]
	});

	// Panel that contains the graphics
	var mainPanel = new Ext.Panel({
		id: 'principal',
		border: false,
		frame: false,
		width: 700,
		x: 0,
		y: 0,
		activeItem: 0,
		layout: 'card',
		//collapsible:false,
		items: [graph, graph_exchanges, graph_users_online, graph_usePlugins]
	});

	// Panel that contains the text of the main view
	var text_right = new Ext.Panel({
		frame: false,
		border: false,
		width: 350,
		height: 100,
		html: 'Charting Demo allows real-time graph server behavior. \n\
		To see the behavior of the server to select the data source and \n\
		type of graphic in which to display the required information.',
		x: 20,
		y: 10,
		//  layout:'absolute',
		bodyStyle: 'background:#eaf4dc;'
	});


	var graphics = new Ext.Panel({
		frame: false,
		border: false,
		x: 10,
		y: 10,
		width: 700,
		height: 380,
		items: [mainPanel]
	});


	var chartingDemo = new Ext.Window({
		//title:'Charting Demo',
		//        cls: 'box',
		renderTo: 'content',
		x: 0,
		y: 85,
		width: 715,
		height: 490,
		border: false,
		frame: true,
		resizable: false,
		closable: false,
		draggable: false,
		maximizable: false,
		cls: "border",
		minimizable: false,
		bodyStyle: 'background:#eaf4dc;',
		items: [text_right, graphics, chooseData]

	});
	chartingDemo.show( );



	comboGraphics.on('select', function(cmb, aRecord, aIndex) {
		var lV = comboGraphics.getValue( );
		var aRecord = comboGraphics.findRecord(comboGraphics.valueField
				|| comboGraphics.displayField, lV);
		var aIndex = comboGraphics.store.indexOf(aRecord);

		Ext.jws.send(NS_MONITORING, "unregister");

		var prin = Ext.getCmp('principal');
		if (aIndex == 0) {
			//send token register with your interest
			Ext.jws.send(NS_MONITORING, TT_REGISTER, {
				"interest": TT_COMPUTER_INFO
			});

			dateFieldPanel.hide( );
			comboExchangesXDays.hide( );
			comboExchangesXMonth.hide( );

			prin.layout.setActiveItem(0);
		}
		else if (aIndex == 1) {
			Ext.jws.send(NS_MONITORING, TT_REGISTER, {
				"interest": TT_SERVER_XCHG_INFO
			});
			dateFieldPanel.show( );
			comboExchangesXDays.hide( );
			comboExchangesXMonth.hide( );

			var tab3 = Ext.getCmp('tab3');
			tab3.on('activate', function( ) {
				dateFieldPanel.show( );
				comboExchangesXDays.hide( );
				comboExchangesXMonth.hide( );
			});

			var tab4 = Ext.getCmp('tab4');
			tab4.on('activate', function( ) {
				comboExchangesXDays.show( );
				dateFieldPanel.hide( );
				comboExchangesXMonth.hide( );

				var lmonth = {
					interest: TT_SERVER_XCHG_INFO_X_DAYS,
					month: '01'
				};

				Ext.jws.send(NS_MONITORING, TT_REGISTER, lmonth);

			});

			var tab5 = Ext.getCmp('tab5');
			tab5.on('activate', function( ) {
				comboExchangesXMonth.show( );
				dateFieldPanel.hide( );
				comboExchangesXDays.hide( );

				var lYear = {
					interest: TT_SERVER_XCHG_INFO_X_MONTH,
					year: '2012'
				};

				Ext.jws.send(NS_MONITORING, TT_REGISTER, lYear);

			});

			prin.layout.setActiveItem(1);
		}
		else if (aIndex == 2) {
			Ext.jws.send(NS_MONITORING, TT_REGISTER, {
				"interest": TT_USER_INFO
			});
			dateFieldPanel.hide( );
			comboExchangesXDays.hide( );
			comboExchangesXMonth.hide( );
			prin.layout.setActiveItem(2);

		}
		if (aIndex == 3) {
			//send token register with your interest
			Ext.jws.send(NS_MONITORING, TT_REGISTER, {
				"interest": TT_PLUGINS_INFO
			});

			dateFieldPanel.hide( );
			comboExchangesXDays.hide( );
			comboExchangesXMonth.hide( );

			prin.layout.setActiveItem(3);
		}

	}, this);


	comboExchangesXDays.on('select', function(cmb, aRecord, aIndex) {
		var lV = comboExchangesXDays.getValue( );
		var mes = "";
		if (lV == "January") {
			mes = '01';
		}
		if (lV == "February") {
			mes = '02';
		}
		if (lV == "March") {
			mes = '03';
		}
		if (lV == "April") {
			mes = '04';
		}
		if (lV == "May") {
			mes = '05';
		}
		if (lV == "June") {
			mes = '06';
		}
		if (lV == "July") {
			mes = '07';
		}
		if (lV == "August") {
			mes = '08';
		}
		if (lV == "September") {
			mes = '09';
		}
		if (lV == "October") {
			mes = '10';
		}
		if (lV == "November") {
			mes = '11';
		}
		if (lV == "December") {
			mes = '12';
		}

		var lmonth = {
			interest: TT_SERVER_XCHG_INFO_X_DAYS,
			month: mes
		};

		Ext.jws.send(NS_MONITORING, TT_REGISTER, lmonth);
	}, this);
	comboExchangesXDays.setValue("January");

	comboExchangesXMonth.on('select', function(cmb, aRecord, aIndex) {
		var lV = comboExchangesXMonth.getValue( );

		var lYear = {
			interest: TT_SERVER_XCHG_INFO_X_MONTH,
			year: lV
		};

		Ext.jws.send(NS_MONITORING, TT_REGISTER, lYear);
	}, this);
	comboExchangesXMonth.setValue("2012");

	comboGraphics.setValue("Machine Resources");
	//send token register with your interest
	Ext.jws.send(NS_MONITORING, TT_REGISTER, {
		"interest": TT_COMPUTER_INFO
	});
	Ext.getCmp('principal').layout.setActiveItem(0);
}