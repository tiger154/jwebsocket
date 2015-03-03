//	---------------------------------------------------------------------------
//	jWebSocket Chat Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------

/**
 * jWebSocket Charting Demo
 * @author Merly Lopez Barroso, Victor Antonio Barzana Crespo
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
	'Ext.chart.*',
	'Ext.jws.Client'
]);
Ext.BLANK_IMAGE_URL = 's.gif';

Ext.require([
	'Ext.form.field.ComboBox',
	'Ext.form.FieldSet',
	'Ext.tip.QuickTipManager',
	'Ext.data.*'
]);
jws.ChartingPlugIn = {
	NS_MONITORING: jws.NS_BASE + ".plugins.monitoring",
	NS_SYSTEM: jws.NS_BASE + ".plugins.system",
	TT_COMPUTER_INFO: "computerInfo",
	TT_ONLINE_USERS: "userInfo",
	TT_SERVER_XCHG_INFO: "serverXchgInfo",
	TT_SERVER_XCHG_INFO_X_DAYS: "serverXchgInfoXDays",
	TT_SERVER_XCHG_INFO_X_MONTH: "serverXchgInfoXMonth",
	TT_PLUGINS_INFO: "pluginsInfo",
	TT_WELCOME: "welcome",
	TT_REGISTER: "register",
	TT_UNREGISTER: "unregister",
	stores: {
		computerInfo: {},
		hDDInfo: {},
		serverExchangesPerHour: {},
		serverExchangesPerDay: {},
		serverExchangesPerMonth: {},
		onlineUsers: {},
		useOfPlugIns: {},
		monthlyPackets: {},
		yearPackets: {}
	},
	views: {
	},
	data: {},
	/**
	 * Registers to the server monitoring plugin by sending the interest and some 
	 * aditional data that the user should want to get from the server
	 * @param {type} aInterest
	 * @param {type} aData
	 */
	registerTo: function(aInterest, aData) {
		if (aInterest) {
			if (typeof aData === "undefined") {
				aData = {};
			}

			aData.interest = aInterest;
			Ext.jwsClient.send(this.NS_MONITORING, this.TT_REGISTER, aData);
		}
	},
	unregister: function( ) {
		Ext.jwsClient.send(this.NS_MONITORING, this.TT_UNREGISTER);
	},
	onReady: function() {
		var lClientStatus = Ext.get("client_status"),
				lClientId = Ext.get("client_id"),
				lWebSocketType = Ext.get("websocket_type");
		var lScope = this;

		lWebSocketType.dom.innerHTML = "WebSocket: " +
				(jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)");

		Ext.jwsClient.open(jws.getAutoServerURL());

		Ext.jwsClient.on('open', function( ) {
			lScope.registerTo(lScope.TT_COMPUTER_INFO);
		});

		Ext.jwsClient.on('close', function( ) {
			lClientId.dom.innerHTML = "Client-ID: - ";
			lClientStatus.dom.innerHTML = "disconnected";
			lClientStatus.dom.className = "offline";
		});
		Ext.jwsClient.addPlugIn(this);
		this.init();
	},
	init: function() {
		var lScope = this;
		lScope.initModels();
		lScope.initStores();
		// Graphic representation of the resources of the machine
		lScope.views.tabPanelComputerInfo = new Ext.TabPanel({
			renderTo: Ext.getBody( ),
			id: 'main-tabs',
			bodyCls: 'x-jwebsocket-tab',
			height: 380,
			width: 400,
			activeTab: 0,
			border: false,
			margin: 0,
			defaults: {
				padding: 10
			},
			bodyStyle: 'background:#e4e4e4;',
			items: [{
					title: 'Machine resources',
					id: 'tab1',
					layout: 'fit',
					border: false,
					items: {
						xtype: 'chart',
						border: false,
						style: 'background:#e4e4e4',
						animate: false,
						shadow: false,
						store: lScope.stores.computerInfo,
						axes: [{
								type: 'Numeric',
								position: 'left',
								fields: ['data'], label: {
									renderer: Ext.util.Format.numberRenderer('0,0')
								},
								//title: 'Number of Hits',
								grid: true,
								minimum: 0, maximum: 100
							}, {
								type: 'Category',
								position: 'bottom',
								fields: ['name'], title: 'Machine resources'
							}], series: [{
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
									renderer: Ext.util.Format.numberRenderer('0.0'), orientation: 'horizontal',
									color: '#333'
								},
								xField: 'name',
								yField: 'data'
							}]
					}
				}, {
					title: 'Hard Disk Data',
					id: 'tab2',
					border: false,
					layout: 'fit',
					items: {
						xtype: 'chart',
						id: 'chartCmp',
						animate: true,
						border: false,
						store: lScope.stores.hDDInfo,
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
										lScope.stores.hDDInfo.each(function(rec) {
											total += rec.get('data');
										});
										this.setTitle(storeItem.get('name') + ': '
												+ Math.round(storeItem.get('data') / total * 100) + '%');
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

		lScope.views.comboChooseMonth = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: 'Month',
			displayField: 'name',
			labelAlign: 'right',
			fieldStyle: "padding-left: 6px;",
			labelWidth: 80,
			width: 260,
			value: new Date().getMonthName(),
			emptyText: 'Select...',
			store: lScope.stores.monthlyPackets,
			queryMode: 'local',
			typeAhead: true,
			listeners: {
				change: function(aField, aRecord, aIndex) {
					var lValue = aField.getValue( );

					var lDate = new Date(),
							lMonthNr = Ext.Array.indexOf(lDate.getMonths( ), lValue) + 1;
					lScope.registerTo(lScope.TT_SERVER_XCHG_INFO_X_DAYS, {
						month: lMonthNr < 10 ? "0" + lMonthNr.toString() : lMonthNr.toString()
					})
				}
			}
		});
		lScope.views.comboChooseMonth.hide( );

		//Creating the combobox in the main ExchangesXMonth
		lScope.views.comboChooseYear = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: 'Year',
			displayField: 'name',
			labelAlign: 'right',
			labelWidth: 80,
			width: 260,
			emptyText: 'Select...',
			value: new Date().getFullYear(),
			fieldStyle: "padding-left: 6px;",
			store: lScope.stores.yearPackets,
			queryMode: 'local',
			typeAhead: true,
			listeners: {
				select: function(aField, aRecord, aIndex) {
					var lValue = aField.getValue( );
					lScope.registerTo(lScope.TT_SERVER_XCHG_INFO_X_MONTH, {
						year: lValue.toString()
					});
				}
			}
		});
		lScope.views.comboChooseYear.hide( );

		//Creating the calendar
		lScope.views.comboChooseDate = new Ext.form.DateField({
			fieldLabel: 'Date',
			emptyText: 'Select a date...',
			format: 'M/d/y',
			labelAlign: 'right',
			fieldStyle: "padding-left: 6px;",
			labelWidth: 80,
			width: 260,
			value: new Date(),
			minValue: '01/01/2013',
			listeners: {
				select: function(aField, aDate) {
					var lSelectedDate = aField.rawValue,
							lDateArray = lSelectedDate.split('/', 3), lSelMonth = lDateArray[0],
							lDay = lDateArray[1],
							lYear = lDateArray[2],
							lDate = new Date(),
							lMonthNr = Ext.Array.indexOf(lDate.getShortMonths( ), lSelMonth) + 1;
					if (lMonthNr > 0) {
						lMonthNr = lMonthNr < 10 ? "0" + lMonthNr : lMonthNr;
					}

					lScope.registerTo(lScope.TT_SERVER_XCHG_INFO, {
						month: lMonthNr.toString(),
						day: lDay,
						year: lYear
					});
				}
			}

		});
		// Adding the calendar to a panel
		lScope.views.panelChooseDate = new Ext.Panel({
			frame: false,
			border: false,
			width: 270,
			height: 46,
			x: 0, y: 0, layout: 'absolute',
			bodyStyle: 'background:#e4e4e4',
			items: [lScope.views.comboChooseDate]
		});
		lScope.views.panelChooseDate.hide( );

		//Creating the combobox in the main view
		lScope.views.comboSelectStream = Ext.create('Ext.form.field.ComboBox', {
			fieldLabel: 'Data Source',
			labelAlign: 'right',
			labelWidth: 80,
			displayField: 'name',
			value: lScope.data.typesOfStream[0].name,
			width: 260,
			fieldStyle: "padding-left: 6px;",
			emptyText: 'Select...',
			store: lScope.stores.typeOfStream,
			queryMode: 'local',
			typeAhead: false,
			editable: false,
			listeners: {
				change: function(aField, aValue) {
					var lIdx = lScope.stores.typeOfStream.find("name", aValue),
							lMainCmp = Ext.getCmp('main_panel');

					if (lIdx === 0) {
						//send token register with your interest
						lScope.registerTo(lScope.TT_COMPUTER_INFO);
						lScope.views.panelChooseDate.hide( );
						lScope.views.comboChooseMonth.hide( );
						lScope.views.comboChooseYear.hide( );
					}
					else if (lIdx === 1) {
						lScope.registerTo(lScope.TT_SERVER_XCHG_INFO);
						lScope.views.panelChooseDate.show();
					}
					else if (lIdx === 2) {
						lScope.registerTo(lScope.TT_ONLINE_USERS);
						lScope.views.panelChooseDate.hide( );
						lScope.views.comboChooseMonth.hide( );
						lScope.views.comboChooseYear.hide( );
					}
					if (lIdx === 3) {
						lScope.registerTo(lScope.TT_PLUGINS_INFO);
						lScope.views.panelChooseDate.hide( );
						lScope.views.comboChooseMonth.hide( );
						lScope.views.comboChooseYear.hide( );
					}
					lMainCmp.layout.setActiveItem(parseInt(lIdx));
				}
			}
		}); 	// Creating panel containing the combobox and calendar        
		lScope.views.panelOptions = new Ext.Panel({
			border: true,
			frame: false,
			x: 400,
			y: -470,
			width: 290,
			bodyStyle: 'background:#e4e4e4;padding-top: 7px;',
			height: 75,
			items: [lScope.views.comboSelectStream, lScope.views.comboChooseMonth, lScope.views.comboChooseYear,
				lScope.views.panelChooseDate]
		});
		// Graphic representation of the server requests
		lScope.views.tabPanelServerPackets = new Ext.TabPanel({
			renderTo: Ext.getBody( ),
			id: 'exchanges-main-tabs',
			bodyCls: 'x-jwebsocket-tab',
			height: 380,
			width: 400,
			activeTab: 0, border: false,
			margin: 0, defaults: {
				padding: 10},
			hideTabStripItem: true,
			bodyStyle: 'background:#e4e4e4;',
			items: [{
					title: 'Packets/Hour',
					id: 'tab_pkt_x_h',
					layout: 'fit',
					border: false,
					items: [
						Ext.create('Ext.chart.Chart', {
							style: 'background:#e4e4e4',
							animate: true,
							shadow: true,
							store: lScope.stores.serverExchangesPerHour,
							legend: {
								position: 'top'
							},
							axes: [{
									type: 'Numeric',
									position: 'left',
									fields: ['incoming', 'outgoing', 'total'],
									minimum: 0,
									label: {
										renderer: Ext.util.Format.numberRenderer('0,0')
									},
									grid: true,
									title: 'Packets'
								}, {
									type: 'Category',
									position: 'bottom',
									fields: ['name'],
									title: 'Hours'
								}],
							series: [{
									type: 'column',
									axis: 'left',
									xField: 'name',
									yField: ['incoming', 'outgoing', 'total'],
									tips: {
										trackMouse: true,
										width: 180,
										height: 28,
										renderer: function(aRecord, aSerie, aOther) {
											this.setTitle("Hour " + aRecord.get('name') + ', Requests: '
													+ (aSerie.value.length > 1 ? aSerie.value[1] : " total: " + aRecord.get("total")));
										}
									},
									label: {
										display: 'outside',
										'text-anchor': 'middle',
										field: 'data',
										renderer: Ext.util.Format.numberRenderer('0'),
										orientation: 'horizontal',
										color: '#333'
									}
								}]
						})]
				},
				{
					title: 'Packets/Day',
					id: 'tab_pkt_x_d',
					layout: 'fit',
					border: false,
					items: [
						Ext.create('Ext.chart.Chart', {
							id: 'exchangesPerDayChart',
							style: 'background:#e4e4e4',
							animate: true,
							shadow: true,
							store: lScope.stores.serverExchangesPerDay,
							legend: {
								position: 'top'
							},
							axes: [{
									type: 'Numeric',
									position: 'left',
									fields: ['incoming', 'outgoing', 'total'],
									minimum: 0,
									label: {
										renderer: Ext.util.Format.numberRenderer('0,0')
									},
									grid: true,
									title: 'Packets'
								}, {
									type: 'Category',
									position: 'bottom',
									fields: ['name'],
									title: 'Days'
								}],
							series: [{
									type: 'column',
									axis: 'left',
									xField: ['name'],
									yField: ['incoming', 'outgoing', 'total'],
									tips: {
										trackMouse: true,
										width: 180,
										height: 28,
										renderer: function(aRecord, aSerie, aOther) {
											this.setTitle("Day " + aRecord.get('name') + ', Requests: '
													+ (aSerie.value.length > 1 ? aSerie.value[1] : " total: " + aRecord.get("total")));
										}
									},
									label: {
										display: 'outside',
										'text-anchor': 'middle',
										field: 'data',
										renderer: Ext.util.Format.numberRenderer('0'),
										orientation: 'horizontal',
										color: '#333'
									}
								}]
						})]
				},
				{
					title: 'Packets/Month',
					id: 'tab_pkt_x_m',
					layout: 'fit',
					border: false,
					items: [Ext.create('Ext.chart.Chart', {
							style: 'background:#e4e4e4',
							animate: true,
							shadow: true,
							store: lScope.stores.serverExchangesPerMonth,
							legend: {
								position: 'right'
							},
							axes: [{
									type: 'Numeric',
									position: 'left',
									fields: ['incoming', 'outgoing', 'total'],
									minimum: 0,
									label: {
										renderer: Ext.util.Format.numberRenderer('0,0')
									},
									grid: true,
									title: 'Packets'
								}, {
									type: 'Category',
									position: 'bottom',
									fields: ['name'],
									title: 'Month'
								}],
							series: [{
									type: 'column',
									axis: 'left',
									xField: 'name',
									yField: ['incoming', 'outgoing', 'total'],
									tips: {
										trackMouse: true,
										width: 180,
										height: 28,
										renderer: function(aRecord, aSerie, aOther) {
											this.setTitle("Day " + aRecord.get('name') + ', Requests: '
													+ (aSerie.value.length > 1 ? aSerie.value[1] : " total: " + aRecord.get("total")));
										}
									},
									label: {
										display: 'outside',
										'text-anchor': 'middle',
										field: 'data',
										renderer: Ext.util.Format.numberRenderer('0'),
										orientation: 'horizontal',
										color: '#333'
									}
								}]
						})]
				}],
			listeners: {
				tabchange: function(aTabPanel, aNewCard, aOldCard) {
					switch (aNewCard.id) {
						case 'tab_pkt_x_h':
							lScope.registerTo(lScope.TT_SERVER_XCHG_INFO);
							lScope.views.panelChooseDate.show( );
							lScope.views.comboChooseMonth.hide( );
							lScope.views.comboChooseYear.hide( );

							break;
						case 'tab_pkt_x_d':
							var lMonthNr = new Date().getMonth() + 1;
							lScope.registerTo(lScope.TT_SERVER_XCHG_INFO_X_DAYS, {
								month: lMonthNr < 10 ? "0" + lMonthNr.toString() : lMonthNr.toString()
							});
							lScope.views.comboChooseMonth.show( );
							lScope.views.panelChooseDate.hide( );
							lScope.views.comboChooseYear.hide( );
							break;
						case 'tab_pkt_x_m':
							lScope.registerTo(lScope.TT_SERVER_XCHG_INFO_X_MONTH, {
								year: new Date().getFullYear().toString()
							});
							lScope.views.comboChooseYear.show( );
							lScope.views.panelChooseDate.hide( );
							lScope.views.comboChooseMonth.hide( );
							break;
					}
				}
			}
		});
		// Graphic representation of Users Online
		lScope.views.tabPaneOnlineUsers = new Ext.TabPanel({
			renderTo: Ext.getBody( ),
			id: 'tab_panel_users_online',
			bodyCls: 'x-jwebsocket-tab',
			layout: 'fit',
			height: 380,
			width: 400,
			border: false,
			activeTab: 0, margin: 0, defaults: {
				padding: 5
			},
			hideTabStripItem: true,
			bodyStyle: 'background:#e4e4e4;',
			items: [
				{
					bodyStyle: 'background:#e4e4e4;',
					title: 'Online Users',
					id: 'tab_users_online',
					layout: 'fit',
					border: false,
					items: {
						xtype: 'chart',
						animate: true,
						store: lScope.stores.onlineUsers,
//						shadow: true,
						axes: [{
								type: 'Numeric',
								minimum: 0, position: 'left',
								fields: 'data',
								title: "Online Users",
								grid: false,
								label: {
									renderer: Ext.util.Format.numberRenderer('0,0'), font: '7pt Verdana'
								}
							}, {
								type: 'Category',
								position: 'bottom',
								fields: 'name',
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
								fill: true,
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
									'stroke-width': 0.5
								},
								markerConfig: {
									type: 'circle',
									size: 3,
									radius: 3,
									'stroke-width': 0,
									fill: '#38B8BF',
									stroke: '#38B8BF'
								}
							}]
					}
				}]
		});
		// Graphic representation of use of plugins
		lScope.views.tabPaneUseOfPlugIns = new Ext.TabPanel({
			renderTo: Ext.getBody( ),
			id: 'tab_panel_use_of_plug_ins',
			bodyCls: 'x-jwebsocket-tab',
			height: 380,
			width: 400,
			activeTab: 0, border: false,
			margin: 0, defaults: {
				padding: 10},
			//hideTabStripItem:true,
			bodyStyle: 'background:#e4e4e4;',
			items: [
				{
					bodyStyle: 'background:#e4e4e4;',
					title: 'Use of plug-ins',
					id: 'tab7',
					layout: 'fit',
					border: false,
					items: {
						xtype: 'chart',
						animate: false,
						shadow: false,
						store: lScope.stores.useOfPlugIns,
						axes: [{
								type: 'Numeric',
								position: 'bottom',
								fields: ['data'], label: {
									renderer: Ext.util.Format.numberRenderer('0,0')
								},
								title: 'Number of Requests',
								grid: true,
								minimum: 0
										//maximum:100
							}, {
								type: 'Category',
								position: 'left',
								fields: ['name'], title: 'Plug-ins'
							}], series: [{
								type: 'bar',
								axis: 'bottom',
								highlight: false,
								tips: {
									trackMouse: true,
									width: 180,
									height: 28,
									renderer: function(storeItem, item) {
										this.setTitle(storeItem.get('name') + ': '
												+ storeItem.get('data') + " packets");
									}
								},
								label: {
									display: 'insideEnd',
									field: 'data',
									renderer: Ext.util.Format.numberRenderer('0'), orientation: 'horizontal',
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
		// Panel that contains the graphics
		var lMainPanel = new Ext.Panel({
			id: 'main_panel',
			border: false,
			frame: false,
			width: 700,
			x: 0,
			y: 0,
			activeItem: 0,
			layout: 'card',
			//collapsible:false,
			items: [
				lScope.views.tabPanelComputerInfo,
				lScope.views.tabPanelServerPackets,
				lScope.views.tabPaneOnlineUsers,
				lScope.views.tabPaneUseOfPlugIns
			]
		});

		// Panel that contains the text of the main view
		var lDescription = new Ext.Panel({
			frame: false,
			border: false,
			width: 350,
			height: 100,
			html: "The Charting Demo displays server statistics graphically in " +
					"real-time. Please select one of the data sources provided " +
					"by server to see the various possibilities of real-time " +
					"server monitoring with jWebSocket.",
			x: 20,
			y: 10,
			//  layout:'absolute',
			bodyStyle: 'background:#eaf4dc;'
		});

		var lGraphicPanel = new Ext.Panel({
			frame: false,
			border: false,
			x: 10,
			y: 10,
			width: 700,
			height: 380,
			items: [lMainPanel]
		});

		var lChartingDemo = new Ext.Window({
			renderTo: 'content',
			x: 0, y: 85,
			width: 710,
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
			items: [lDescription, lGraphicPanel, lScope.views.panelOptions]
		});
		lChartingDemo.show( );
	},
	initModels: function() {
		Ext.define('State', {
			extend: 'Ext.data.Model',
			fields: ['name']
		});
	},
	initStores: function() {
		var lHours = ['name'],
				lHour, lDay,
				lDate = new Date();

		for (lHour = 1; lHour <= 24; lHour++) {
			lHours.push(lHour);
		}

		this.stores.computerInfo = Ext.create('Ext.data.JsonStore', {
			fields: ['name', 'Memory', 'Cpu', 'Swap']
		});
		this.stores.hDDInfo = Ext.create('Ext.data.JsonStore', {
			fields: ['name', 'Total Hdd Space', 'Free', 'Used']
		});
		this.stores.serverExchangesPerHour = Ext.create('Ext.data.JsonStore', {
			fields: lHours
		});
		this.stores.serverExchangesPerDay = Ext.create('Ext.data.JsonStore', {
			fields: ['name', 'incoming', 'outgoing', 'total']
		});
		this.stores.serverExchangesPerMonth = Ext.create('Ext.data.JsonStore', {
			fields: ['name', lDate.getFullYear() - 1, lDate.getFullYear(), lDate.getFullYear() + 1]
		});
		this.stores.onlineUsers = Ext.create('Ext.data.JsonStore', {
			fields: ['name', {name: 'data', type: 'Integer'}]
		});
		this.stores.useOfPlugIns = Ext.create('Ext.data.JsonStore', {
			fields: ['name', 'plugins']
		});

		var lDate = new Date(),
				lMonth_array = lDate.getMonths(),
				lMonths = [],
				lDay;

		for (lDay = 0; lDay < lMonth_array.length; lDay++) {
			lMonths.push({
				"name": lMonth_array[lDay]
			});
		}
		this.stores.monthlyPackets = Ext.create('Ext.data.Store', {
			model: 'State',
			data: lMonths
		});
		this.stores.yearPackets = Ext.create('Ext.data.Store', {
			model: 'State',
			data: [{
					"name": lDate.getFullYear() - 1
				}, {
					"name": lDate.getFullYear()
				}, {
					"name": lDate.getFullYear() + 1
				}]
		});
		// The data for all states
		this.data.typesOfStream = [{
				"name": "Machine resources"
			}, {
				"name": "Server requests"
			}, {
				"name": "Online users"
			}, {
				"name": "Use of plug-ins"
			}];

		// The data store holding the states; shared by each of the ComboBox 
		this.stores.typeOfStream = Ext.create('Ext.data.Store', {
			model: 'State',
			data: this.data.typesOfStream
		});

	},
	processToken: function(aToken) {
		if (aToken.ns === this.NS_MONITORING) {
			var lScope = this;
			if (aToken.type === this.TT_COMPUTER_INFO) {
				var lMemory = parseInt(aToken.usedMemPercent),
						lTotalCPUUsage = parseFloat(aToken.consumeTotal),
						lCPUs = aToken.consumeCPUCharts,
						lCPUData = [], lHDDData,
						lIdx, lSwap, lFreeHd, lUsedHd;

				for (lIdx = 0; lIdx < lCPUs.length; lIdx++) {
					lCPUData.push({
						name: 'CPU ' + (lIdx + 1),
						data: parseFloat(lCPUs[lIdx])
					});
				}

				lSwap = parseInt(aToken.swapPercent);
				lCPUData.push({
					name: 'Total CPU',
					data: lTotalCPUUsage
				});
				lCPUData.push({
					name: 'Memory',
					data: lMemory
				});
				lCPUData.push({
					name: 'Swap',
					data: lSwap
				});
				this.stores.computerInfo.loadData(lCPUData);

				lFreeHd = parseInt(aToken.freeHddSpace);
				lUsedHd = parseInt(aToken.usedHddSpace);

				lHDDData = [
					{
						name: 'Free Hdd Space',
						data: lFreeHd
					}, {
						name: 'Used Hdd Space',
						data: lUsedHd
					}];
				this.stores.hDDInfo.loadData(lHDDData);

			}
			else if (aToken.type === this.TT_ONLINE_USERS) {
				var lConnUsers = aToken.connectedUsers,
						lOnlineUsers = [], lKey; 				// Rendering 60 seconds of connected users in the graph
				for (lKey = 59; lKey >= 0; lKey--) {
					lOnlineUsers.push({
						name: 59 - lKey,
						data: lConnUsers[ lKey ]
					});
				}
				this.stores.onlineUsers.loadData(lOnlineUsers);
			}
			else if (aToken.type === this.TT_SERVER_XCHG_INFO) {
				if (typeof aToken.exchanges === "undefined") {
					lScope.views.comboChooseDate.markInvalid("There are no transactions " +
							"during the selected day in the server, please " +
							"select other day");
				} else {
					var lExchangeInfo = [], lKey;

					for (lKey in aToken.exchanges) {
						if (aToken.exchanges.hasOwnProperty(lKey)) {
							if (lKey.toString()[0] === 'h') {
								lExchangeInfo.push({
									name: lKey.substr(1, lKey.length),
									incoming: parseInt(aToken.exchanges[ lKey ].in) || 0,
									outgoing: parseInt(aToken.exchanges[ lKey ].out) || 0,
									total: parseInt(aToken.exchanges[ lKey ].in) + parseInt(aToken.exchanges[ lKey ].out) || 0
								});
							}
						}
					}
					lScope.stores.serverExchangesPerHour.loadData(lExchangeInfo);
				}
			}
			else if (aToken.type === this.TT_SERVER_XCHG_INFO_X_DAYS) {
				if (aToken.code === -1) {
					lScope.views.comboChooseMonth.markInvalid("There are no transactions " +
							"for the selected month in the server, please " +
							"select other month");
				} else {
					var lPacketsXDay = [], lDay, lMap;
					for (lDay = 1; lDay <= 31; lDay++) {
						lMap = aToken[(lDay < 10) ? '0' + lDay : lDay];
						lPacketsXDay.push({
							name: lDay,
							incoming: parseInt((lMap ? lMap.in : 0) || 0, 10),
							outgoing: parseInt((lMap ? lMap.out : 0) || 0, 10),
							total: parseInt((lMap ? lMap.in : 0) || 0, 10) +
									parseInt((lMap ? lMap.out : 0) || 0, 10)
						});
					}
					lScope.stores.serverExchangesPerDay.loadData(Ext.clone(lPacketsXDay));
				}
			}

			else if (aToken.type === this.TT_SERVER_XCHG_INFO_X_MONTH) {
				if (aToken.code === -1) {
					lScope.views.comboChooseYear.markInvalid("There are no transactions " +
							"for the selected year in the server, please " +
							"select other year");
				}
				else {
					var lDate = new Date(),
							lMonths = lDate.getShortMonths( ),
							lPacketsXMonth = [], lMonthNr, lIdx;
					for (lIdx = 1; lIdx <= lMonths.length; lIdx++) {
						lMonthNr = lIdx;
						if (lMonthNr > 0) {
							lMonthNr = lMonthNr < 10 ? "0" + lMonthNr : lMonthNr;
						}
						lMap = aToken[lMonthNr];
						lPacketsXMonth.push({
							name: lMonths[ lIdx - 1 ],
							incoming: parseInt((lMap ? lMap.in : 0) || 0, 10),
							outgoing: parseInt((lMap ? lMap.out : 0) || 0, 10),
							total: parseInt((lMap ? lMap.in : 0) || 0, 10) +
									parseInt((lMap ? lMap.out : 0) || 0, 10)
						});

					}
					this.stores.serverExchangesPerMonth.loadData(lPacketsXMonth);
				}
			}

			else if (aToken.type === this.TT_PLUGINS_INFO) {
				var lUsePlugins = [], lPluginsInUse = aToken.usePlugins, lIdx;

				if (lPluginsInUse.length === 0) {
					lScope.views.comboSelectStream.markInvalid('No information for this item',
							'Sorry, the information for the Plug-ins is not ' +
							'available at this moment please, try later');
				} else {
					for (lIdx = 0; lIdx < lPluginsInUse.length; lIdx++) {
						lUsePlugins.push({
							name: aToken.usePlugins[lIdx].id,
							data: aToken.usePlugins[lIdx].requests || 0
						});
					}

					this.stores.useOfPlugIns.loadData(lUsePlugins);
				}
			}
		}

		else if (aToken.ns === this.NS_SYSTEM) {
			if (aToken.type === this.TT_WELCOME) {
				Ext.get("client_id").dom.innerHTML = "Client-ID: " + aToken.sourceId;
				Ext.get("client_status").dom.innerHTML = aToken.username || "online";
				Ext.get("client_status").dom.className = "authenticated";
			}
		}
	}
};

Ext.onReady(function( ) {
	Ext.tip.QuickTipManager.init( );
	jws.ChartingPlugIn.onReady();
});


/* Defining some month names to be used later */
Date.prototype.getMonthName = function(aLang, aNumber) {
	var lLang = aLang && (aLang in Date.locale) ? aLang : 'en';
	var lMonth = (aNumber && aNumber >= 0) ? aNumber : this.getMonth( );
	return Date.locale[ lLang ].month_names[ lMonth ];
};

Date.prototype.getMonthNameShort = function(aLang, aNumber) {
	var lLang = aLang && (aLang in Date.locale) ? aLang : 'en',
			lMonth = (aNumber && aNumber >= 0) ? aNumber : this.getMonth( );
	return Date.locale[ lLang ].month_names_short[ lMonth ];
};

Date.prototype.getMonths = function(aLang) {
	var lLang = aLang && (aLang in Date.locale) ? aLang : 'en';
	return Date.locale[ lLang ].month_names;
};

Date.prototype.getShortMonths = function(aLang) {
	var lLang = aLang && (aLang in Date.locale) ? aLang : 'en';
	return Date.locale[ lLang ].month_names_short;
};

Date.locale = {
	en: {
		month_names: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
		month_names_short: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
	}
};