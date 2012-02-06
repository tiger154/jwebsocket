Ext.require(['Ext.data.*','Ext.chart.*']);

Ext.onReady(function(){
    Ext.jws.open("ws://localhost:8787/jwebsocket/jwebsocket");

    Ext.jws.on('open',function(){
        initDemo();
    });

    Ext.jws.on('close',function(){
        alert("The server is disconnected");
    });
});

function initDemo(){
    Ext.BLANK_IMAGE_URL = '../css/images/s.gif';

    Ext.require([
        'Ext.form.field.ComboBox',
        'Ext.form.FieldSet',
        'Ext.tip.QuickTipManager',
        'Ext.data.*'
        ]);

    //Ext.onReady(function() {
    Ext.tip.QuickTipManager.init();

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
        "name":"Machine Resources"
    },{
        "name":"Exchanges on the server"
    },{
        "name":"Online users"
    },{
        "name":"Browser statistics"
    }];

    // The data store holding the states; shared by each of the ComboBox examples below
    var store = Ext.create('Ext.data.Store', {
        model: 'State',
        data: states
    });

    // Simple ComboBox using the data store
    var simpleCombo = Ext.create('Ext.form.field.ComboBox', {
        fieldLabel: 'Data Source',
        displayField: 'name',        
        width: 260,
        alueField: 'value',
        labelWidth: 130,
        emptyText:'Select...',
        store: store,
        queryMode: 'local',
        typeAhead: true
    });

    var labelDate = new Ext.form.Label({
        id:"labelDate",
        x:58,
        y:253,
        cls:"labelDate"
    });

    var dateField = new Ext.form.DateField({  
        fieldLabel: 'Date',  
        emptyText:'Insert a date...',  
        format:'M/d/y',  
        width: 250,
        hide:true,
        minValue: '01/17/2012',//, //<-- day on which the server is started 
        //maxValue:'12-08-2013', // <-- max date  
        // value:new Date()  // value field is initialized
        listeners:{        
            select:function(A, fecha)
            {
                //console.log(A.rawValue);
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
                    interest: "serverXchgInfo",
                    month: mesBd,
                    day: dia,
                    year: anno
                };
               Ext.jws.send("monitoringPlugin.pcinfo","register", modifiedDate);
            }
        }
    });
     
    var combo = new Ext.Window ({
		renderTo: 'combo',
		title:'Choose data and graph',
        closable: false,
        resizable: false,
        draggable:false,
        modal: false, 
        border: false,
        plain: true,
        width:280,
        //bodyStyle:'background:#c8c8c8;',
        height:100,
        items:[simpleCombo,dateField,labelDate]
    });

    combo.show();
    //});

	var lToken = {
		interest: "computerInfo"
	};
	
    Ext.jws.send("monitoringPlugin.pcinfo","register", lToken);

    var myStore = Ext.create('Ext.data.JsonStore', {
        fields: ['name', 'Memory','Cpu','Swap']
    });
    
    var myStorePie = Ext.create('Ext.data.JsonStore', {
        fields: ['name', 'Total Hdd Space','Free','Used']
    });
    
    var lBrowserStore = Ext.create('Ext.data.JsonStore', {
        fields: ['name','Chromium', 'Firefox', 'Opera', 'Netscape', 'Internet Explorer', 'Safari', 'Native Clients']
    });
    
    var myStoreExchange = Ext.create('Ext.data.JsonStore', {
        fields: ['name','1','2','3','4','5','6','7','8','9',
        '10','11','12','13','14','15','16','17','18',
        '19','20','21','22','23','24']
    });
    
    var myStoreUsersOnline = Ext.create('Ext.data.JsonStore', {
        fields: ['name','Users Online']
    });
    
    var plugin = {};
    plugin.processToken = function(aToken){
        if (aToken.ns == "monitoringPlugin.pcinfo"){
            if (aToken.type == "computerInfo"){
                var memory          = parseInt(aToken.usedMemPercent);
                var cpu             = parseInt(aToken.consumeCPU);
                var swap            = parseInt(aToken.swapPercent);
                //                var netReceived     = parseInt(aToken.netReceived);
                //                var netSent         = parseInt(aToken.netSent);
						
                var dataToShow = [{
                    name:'Cpu', 
                    data1: cpu
                },{
                    name:'Memory',
                    data1: memory
                },{
                    name:'Swap',
                    data1: swap
                }/*,{
                    name:'In Network',
                    data1: netReceived
                },
                {
                    name:'Out Network',
                    data1: netSent
                }
                */
                ]
                myStore.loadData(dataToShow);
						
                //var totalhd = parseInt(aToken.totalHddSpace);
                var freehd = parseInt(aToken.freeHddSpace);
                var usedhd = parseInt(aToken.usedHddSpace);
						
                var show = [//	{name:'Total Hdd Space ', data1: totalhd },
                {
                    name:'Free Hdd Space',
                    data1: freehd
                },{
                    name:'Used Hdd Space',
                    data1: usedhd
                }];
                myStorePie.loadData(show);
           
            }
            //IF THE INCOMING TOKEN BRINGS INFORMATION ABOUT BROWSERS
            else if (aToken.type == "browserInfo"){
                var show_browser = [{
                    name:'Chromium Browser',
                    data: parseInt(aToken.chromium)
                },{
                    name:'Firefox',
                    data: parseInt(aToken.firefox)
                },{
                    name:'Opera',
                    data: parseInt(aToken.opera)
                },{
                    name:'Netscape',
                    data: parseInt(aToken.netscape)
                },{
                    name:'Internet Explorer',
                    data: parseInt(aToken.ie)
                },{
                    name:'Safari',
                    data: parseInt(aToken.safari)
                },{
                    name:'Native Clients',
                    data: parseInt(aToken.nativeClients)
                }];
                lBrowserStore.loadData(show_browser);
            }
            
            else if (aToken.type == "userInfo"){
                
                var connUsers       = parseInt(aToken.connectedUsers);
                var usersOnline = [
                {
                    name:'Users Online',
                    data1: connUsers
                }
                ]
                
                myStoreUsersOnline.loadData(usersOnline);
            }
            //IF THE INCOMING TOKEN BRINGS INFORMATION ABOUT BROWSERS
            else if (aToken.type == "serverXchgInfo"){
                
                var lExchangeInfo = [{
                    name:'12',
                    data1: parseInt(aToken.exchanges.h0) || 0
                },{
                    name:'1',
                    data1: parseInt(aToken.exchanges.h1) || 0
                },{
                    name:'2',
                    data1: parseInt(aToken.exchanges.h2) || 0
                },{
                    name:'3',
                    data1: parseInt(aToken.exchanges.h3) || 0
                },{
                    name:'4',
                    data1: parseInt(aToken.exchanges.h4) || 0
                },{
                    name:'5',
                    data1: parseInt(aToken.exchanges.h5) || 0
                },{
                    name:'6',
                    data1: parseInt(aToken.exchanges.h6) || 0
                },{
                    name:'7',
                    data1: parseInt(aToken.exchanges.h7) || 0
                },{
                    name:'8',
                    data1: parseInt(aToken.exchanges.h8) || 0
                },{
                    name:'9',
                    data1: parseInt(aToken.exchanges.h9) || 0
                },{
                    name:'10',
                    data1: parseInt(aToken.exchanges.h10) || 0
                },{
                    name:'11',
                    data1: parseInt(aToken.exchanges.h11) || 0
                },{
                    name:'12',
                    data1: parseInt(aToken.exchanges.h12) || 0
                },{
                    name:'1',
                    data1: parseInt(aToken.exchanges.h13) || 0
                },{
                    name:'2',
                    data1: parseInt(aToken.exchanges.h14) || 0
                },{
                    name:'3',
                    data1: parseInt(aToken.exchanges.h15) || 0
                },{
                    name:'4',
                    data1: parseInt(aToken.exchanges.h16) || 0
                },{
                    name:'5',
                    data1: parseInt(aToken.exchanges.h17) || 0
                },{
                    name:'6',
                    data1: parseInt(aToken.exchanges.h18) || 0
                },{
                    name:'7',
                    data1: parseInt(aToken.exchanges.h19) || 0
                },{
                    name:'8',
                    data1: parseInt(aToken.exchanges.h20) || 0
                },{
                    name:'9',
                    data1: parseInt(aToken.exchanges.h21) || 0
                },{
                    name:'10',
                    data1: parseInt(aToken.exchanges.h22) || 0
                },{
                    name:'11',
                    data1: parseInt(aToken.exchanges.h23) || 0
                }];
                myStoreExchange.loadData(lExchangeInfo);
            }
        }
		else if (aToken.ns == "org.jwebsocket.plugins.system") {
			if(aToken.type == "welcome") {
				console.log(aToken);
				$("#client_id").text("Client-ID: " + aToken.sourceId);
				$("#client_status").removeClass("offline").removeClass("online").addClass("authenticated").text("online");
				$("#websocket_type").text("WebSocket: " + (jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)" ));
			}
		}
    }
    
    Ext.jws.addPlugIn(plugin);
   
    // graphics server resources
    var colors = ['url(#v-1)',
    'url(#v-2)',
    'url(#v-3)',
    'url(#v-4)',
    'url(#v-5)'
    ];
    
    var baseColor = '#eee';
    
    Ext.define('Ext.chart.theme.Fancy', {
        extend: 'Ext.chart.theme.Base',
        
        constructor: function(config) {
            this.callParent([Ext.apply({
                axis: {
                    fill: baseColor,
                    stroke: baseColor
                },
                axisLabelLeft: {
                    fill: baseColor
                },
                axisLabelBottom: {
                    fill: baseColor
                },
                axisTitleLeft: {
                    fill: baseColor
                },
                axisTitleBottom: {
                    fill: baseColor
                },
                colors: colors
            }, config)]);
        }
    });

    var graph = new Ext.TabPanel({//('Ext.TabPanel', {
        id: 'main-tabs',
        height: 360,
        activeTab: 0,
        margin:0,
        defaults: {
            padding: 10
        },
        hideTabStripItem:true,
        items:[
        {
            title: 'Machines Resources',
            id: 'tab1',           
            layout: 'fit',            
            items: {
                xtype: 'chart',
                style: 'background:#e4e4e4',
                animate: false,
                shadow: false,
                store: myStore,
                axes: [{
                    type: 'Numeric',
                    position: 'left',
                    fields: ['data1'],
                    label: {
                        renderer: Ext.util.Format.numberRenderer('0,0')
                    },
                    //title: 'Number of Hits',
                    grid: true,
                    minimum: 0
                }, {
                    type: 'Category',
                    position: 'bottom',
                    fields: ['name'],
                    title: 'Resources'
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
                            this.setTitle(storeItem.get('name') + ': ' + storeItem.get('data1') + ' %');
                        }
                    },
                    label: {
                        display: 'insideEnd',
                        'text-anchor': 'middle',
                        field: 'data1',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'vertical',
                        color: '#333'
                    },
                    xField: 'name',
                    yField: 'data1'
                }]
            }
        },
        {
            title: 'Hard Disk Data',
            id: 'tab2',   
        
            //x:500,
            // y:-300,
            // renderTo: Ext.getBody(),
            layout: 'fit',
            /* tbar: [{
            //text: 'Reload Data',
            handler: function() {
            Ext.jws.send("monitoringPlugin.pcinfo","information");
            }
        }*//*, {
            enableToggle: true,
            pressed: false,
            //text: 'Donut',
            toggleHandler: function(btn, pressed) {
                var chart = Ext.getCmp('chartCmp');
                chart.series.first().donut = pressed ? 35 : false;
                chart.refresh();
            }
        }],*/
        
            items: {
                xtype: 'chart',
                id: 'chartCmp',               
                animate: true,
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
                    field: 'data1',
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
                                total += rec.get('data1');
                            });
                            this.setTitle(storeItem.get('name') + ': ' + Math.round(storeItem.get('data1') / total * 100) + '%');
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
    
    
    var graph_exchanges = new Ext.TabPanel({//('Ext.TabPanel', {
        id: 'exchanges-main-tabs',
        height: 400,        
        width: 430,
        activeTab: 0,
        margin:0,
        defaults: {
            padding: 10
        },
        hideTabStripItem:true,
        items:[{
            title: 'Exchanges to Hours',
            id: 'tab3',           
            layout: 'fit',            
            items: {
                xtype: 'chart',
                style: 'background:#e4e4e4',
                animate: false,
                shadow: false,
                store: myStoreExchange,
                axes: [{
                    type: 'Numeric',
                    position: 'left',
                    fields: ['data1'],
                    label: {
                        renderer: Ext.util.Format.numberRenderer('0,0')
                    },
                    //title: 'Number of Hits',
                    grid: true,
                    minimum: 0
                }, {
                    type: 'Category',
                    position: 'bottom',
                    fields: ['name'],
                    title: 'Requests'
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
                            this.setTitle(storeItem.get('name') + ': ' + storeItem.get('data1') + ' requests');
                        }
                    },
                    label: {
                        display: 'insideEnd',
                        'text-anchor': 'middle',
                        field: 'data1',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'vertical',
                        color: '#333'
                    },
                    xField: 'name',
                    yField: 'data1'
                }]
            }
            
        },
        {
            title: 'Exchanges to days',
            id: 'tab4',           
            layout: 'fit',            
            items: {
        }
            
        },
        {
            title: 'Exchanges to months',
            id: 'tab5',           
            layout: 'fit',            
            items: {
        }
            
        }]
    });
    
    var graph_users_online = new Ext.TabPanel({//('Ext.TabPanel', {
        id: 'userOnline-main-tabs',
       // layout: 'fit',
        height: 400,        
        width: 430,
        activeTab: 0,
        margin:0,
        defaults: {
            padding: 10
        },
        hideTabStripItem:true,
       /* tbar: [{
            text: 'Reload Data',
            handler: function() {
                myStoreUsersOnline.loadData(generateData());
            }
        }],*/
    
        items:[
        
        {
            title: 'Online Users',
            id: 'tab6',           
            layout: 'fit',            
            items: {
                xtype: 'chart',
                style: 'background:#e4e4e4',
                animate: false,
                shadow: false,
                store: myStoreUsersOnline,
                axes: [{
                    type: 'Numeric',
                    position: 'left',
                    fields: ['data1'],
                    label: {
                        renderer: Ext.util.Format.numberRenderer('0,0')
                    },
                    //title: 'Number of Hits',
                    grid: true,
                    minimum: 0,
                    maximum:100
                }, {
                    type: 'Category',
                    position: 'bottom',
                    fields: ['name'],
                    title: 'Number of users Online'
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
                            this.setTitle(storeItem.get('name') + ': ' + storeItem.get('data1') + ' %');
                        }
                    },
                    label: {
                        display: 'insideEnd',
                        'text-anchor': 'middle',
                        field: 'data1',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'vertical',
                        color: '#333'
                    },
                    xField: 'name',
                    yField: 'data1'
                }]
            }
        }]
                  
    /* renderTo: Ext.getBody(),
        id: 'userOnline-main-tabs',
        height: 400,        
        width: 430,
        activeTab: 0,
        margin:0,
        defaults: {
            padding: 10
        },
        hideTabStripItem:true,
        items: [{
            id:'tab6',
            title:'Online Users',
            xtype: 'chart',
            style: 'background:#e4e4e4',
            animate: false,
            shadow: false,
            store: myStoreUsersOnline,
            axes: [{
                type: 'Numeric',
                position: 'left',
                fields: ['data1'],
                //title: 'Hits',
                grid: true,
                minimum: 0,
                maximum:100
            }, {
                type: 'Category',
                position: 'bottom',
                fields: ['name'],
                title: 'Number of users Online'                     
            }],
            series: [{
                type: 'column',
                axis: 'left',
                gutter: 5,
                xField: 'name',
                yField: ['data1'],
                tips: {
                    trackMouse: true,
                    width: 90,
                    height: 50,
                    renderer: function(storeItem, item) {
                        this.setTitle(storeItem.get('name') + '<br />' + storeItem.get('data1'));
                    }
                },
                style: {
                    fill: '#38B8BF'
                }
            }]
        }]*/
    });
    
    var graph_browsers = new Ext.TabPanel({//('Ext.TabPanel', {
        id: 'browser-main-tab',
        height: 400,        
        width: 430,
        activeTab: 0,
        margin:0,
        defaults: {
            padding: 10
        },
        hideTabStripItem:true,
        
        items: [{
            title: 'Browsers statistics',
            id: 'tab5',           
            layout: 'fit',            
            items: {
                xtype: 'chart',
                id: 'chartCmp1',               
                animate: true,
                store: lBrowserStore,
                background: {
                    fill: '#e4e4e4'
                },
                shadow: false,
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
                        height: 28,
                        renderer: function(storeItem, item) {
                            //calculate percentage.
                            var total = 0;
                            lBrowserStore.each(function(rec) {
                                total += rec.get('data');
                            });
                            this.setTitle(storeItem.get('name') + ': ' + Math.round(storeItem.get('data') / total * 100) + '%');
                        }
                    },
                    highlight: {
                        segment: {
                            margin: 20
                        }
                    },
                    label: {
                        field: 'name',
                        display: 'rotate',
                        contrast: true,
                        font: '10px Arial'
                    }
                }]
            }
        }]
    });
    
    
    var panelPrincipal = new Ext.Panel({
        id:'principal',
        x:0,
        y:0,
        activeItem:0,
        layout:'card',
        collapsible:false,
        border:false,
        items:[graph,graph_exchanges,graph_users_online, graph_browsers]
    });
    //return panelPrincipal;
    
    var windowCharting = new Ext.Window({  
		renderTo: 'content',
		cls: 'box',
        closable: false,
        resizable: false,
        //disabled: true,
        modal: false,
        border: false,
        plain: true,  
        draggable:false,
        //closeAction: 'hide',  
//        title: 'Charting',
        width: 700,
        height: 350,  
        paddingLeft:12,
        items: [panelPrincipal]  
    });  
    
   windowCharting.show(); 
    
  /*  var demo = new Ext.Window({  
        closable: false,  
        resizable: true,  
        //disabled: true,
        modal: false,  
        border: true,  
        plain: true,  
        draggable:false,  
        x:250,
        y:100,             
        //closeAction: 'hide',  
        title: 'Charting Demo',  
        width: 500,  
        height: 500,  
        paddingLeft:12,
        items: [panelPrincipal,combo]  
    }); 
    demo.show();*/
    
    
    
    
    
    simpleCombo.on('select',function(cmb, aRecord, aIndex){
        var lV      = simpleCombo.getValue();
        var aRecord = simpleCombo.findRecord(simpleCombo.valueField || simpleCombo.displayField, lV);
        var aIndex  = simpleCombo.store.indexOf(aRecord);
        
        var prin = Ext.getCmp('principal');
        if(aIndex == 0) {
            //send token register with your interest
            Ext.jws.send("monitoringPlugin.pcinfo","register", {
                "interest":"computerInfo"
            });
            // windowCharting.enable();
            prin.layout.setActiveItem(0);
        }else if (aIndex == 1) {
            Ext.jws.send("monitoringPlugin.pcinfo","register", {
                "interest":"serverXchgInfo"
            });
            prin.layout.setActiveItem(1);
            
        }else if (aIndex == 2) {
            Ext.jws.send("monitoringPlugin.pcinfo","register", {
                "interest":"userInfo"
            });
            prin.layout.setActiveItem(2);
            
        }else if (aIndex == 3) {
            Ext.jws.send("monitoringPlugin.pcinfo","register", {
                "interest":"browserInfo"
            });
            prin.layout.setActiveItem(3);
        }
    },this);    
    
       
}
