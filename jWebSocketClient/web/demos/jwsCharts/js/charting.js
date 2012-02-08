Ext.require(['Ext.data.*','Ext.chart.*']);

Ext.onReady(function(){
    Ext.jws.open("ws://10.208.7.105:8787/jwebsocket/jwebsocket");

    Ext.jws.on('open',function(){
        var lToken = {
            interest: "computerInfo"
        };
        Ext.jws.send("monitoringPlugin.pcinfo","register", lToken);
    });

    Ext.jws.on('close',function(){
        //        alert("The server is disconnected");
        $("#client_id").text("Client-ID: - ");
        $("#client_status").removeClass("authenticated").removeClass("online").addClass("offline").text("disconnected");
        $("#websocket_type").text("WebSocket: - ");
    });
    
    initDemo();
    
});

function initDemo(){
    Ext.BLANK_IMAGE_URL = 's.gif';

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
    }];

    var states1 = [{
        "name":"January"
    },{
        "name":"February"
    },{
        "name":"March"
    },{
        "name":"April"
    },{
        "name":"May"
    },{
        "name":"June"
    },{
        "name":"July"
    },{
        "name":"August"
    },{
        "name":"September"
    },{
        "name":"October"
    },{
        "name":"November"
    },{
        "name":"December"
    }];

    var states2 = [{
        "name":"2012"
    },{
        "name":"2013"
    },{
        "name":"2014"
    },{
        "name":"2015"
    }];
    
    // The data store holding the states; shared by each of the ComboBox examples below
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
    
    var comboGraphics = Ext.create('Ext.form.field.ComboBox', {
        fieldLabel: 'Data Source',
        labelAlign:'right',
        labelWidth: 80,
        displayField: 'name',        
        width: 260,
        alueField: 'value',
        emptyText:'Select...',
        store: store,
        queryMode: 'local',
        typeAhead: true
    });


    var comboExchangesXDays = Ext.create('Ext.form.field.ComboBox', {
        fieldLabel: 'Month',
        displayField: 'name', 
        labelAlign:'right',
        labelWidth: 80,
        width: 260,
        alueField: 'value',
        emptyText:'Select...',
        store: storeMonth,
        queryMode: 'local',
        typeAhead: true
    });
    comboExchangesXDays.hide();
    
    var comboExchangesXMonth = Ext.create('Ext.form.field.ComboBox', {
        fieldLabel: 'Year',
        displayField: 'name', 
        labelAlign:'right',
        labelWidth: 80,
        width: 260,
        alueField: 'value',
        emptyText:'Select...',
        store: storeYear,
        queryMode: 'local',
        typeAhead: true
    });
    comboExchangesXMonth.hide();

    var dateField = new Ext.form.DateField({  
        fieldLabel: 'Date',  
        emptyText:'Insert a date...',  
        format:'M/d/y',  
        // width: 230,
        labelAlign:'right',
        labelWidth: 80,
        width: 260,
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
    
    var dateFieldPanel = new Ext.Panel({
        frame:false,
        border:false,
        width:270,
        height: 46,
        x:0,
        y:0,
        layout:'absolute',
        bodyStyle:'background:#e4e4e4',
        items:[dateField]
    });
    dateFieldPanel.hide();
    
             
    var chooseData = new Ext.Panel ({
        border:false,
        closable: false,
        resizable: false,
        modal: false,
        plain: true,  
        draggable:false,
        frame:true,
        x:430,
        y:-470,
        width:290,
        bodyStyle:'background:#e4e4e4;' ,
        height:80,
        items:[comboGraphics,comboExchangesXDays,comboExchangesXMonth,dateFieldPanel]
    });

    Ext.jws.send("monitoringPlugin.pcinfo","register");

    var myStore = Ext.create('Ext.data.JsonStore', {
        fields: ['name', 'Memory','Cpu','Swap']
    });
    
    var myStorePie = Ext.create('Ext.data.JsonStore', {
        fields: ['name', 'Total Hdd Space','Free','Used']
    });
    
    var myStoreExchange = Ext.create('Ext.data.JsonStore', {
        fields: ['name','1','2','3','4','5','6','7','8','9',
        '10','11','12','13','14','15','16','17','18',
        '19','20','21','22','23','24']
    });
    
    var myStoreExchangeForDays = Ext.create('Ext.data.JsonStore', {
        fields: ['name','1','2','3','4','5','6','7','8','9',
        '10','11','12','13','14','15','16','17','18',
        '19','20','21','22','23','24','25','26','27','28','29','30','31']
    });
    
    var myStoreExchangeForMonth = Ext.create('Ext.data.JsonStore', {
        fields: ['name','2012','2013','2014','2015']
    });
    
    var myStoreUsersOnline = Ext.create('Ext.data.JsonStore', {
        fields: ['name','Users Online']
    });
    
    var plugin = {}
    plugin.processToken = function(aToken){
        if (aToken.ns == "monitoringPlugin.pcinfo"){
            if (aToken.type == "computerInfo"){
                var memory              = parseInt(aToken.usedMemPercent);
                var totalConsumeCPU     = parseInt(aToken.consumeTotal);
                var cpu                 = aToken.consumeCPU;
                
                var dataToShow = new Array();
                
                for (var i = 0; i < cpu.length; i++) {
                    dataToShow.push({
                        name:'CPU '+ i+1, 
                        data1: parseInt(cpu[i])
                    });
                }
                
                var swap            = parseInt(aToken.swapPercent);
                
                dataToShow.push(
                {
                    name:'Total CPU', 
                    data1: totalConsumeCPU
                });
                dataToShow.push({
                    name:'Memory',
                    data1: memory
                });
                dataToShow.push({
                    name:'Swap',
                    data1: swap
                });                
                myStore.loadData(dataToShow);
		            
						
                var freehd = parseInt(aToken.freeHddSpace);
                var usedhd = parseInt(aToken.usedHddSpace);
						
                var show = [
                {
                    name:'Free Hdd Space',
                    data1: freehd
                },{
                    name:'Used Hdd Space',
                    data1: usedhd
                }];
                myStorePie.loadData(show);
           
            }         
            else if (aToken.type == "userInfo"){
                
                var connUsers       = parseInt(aToken.connectedUsers);
                var usersOnline = [
                {
                    name:'Users Online',
                    data1: connUsers
                }];                
                myStoreUsersOnline.loadData(usersOnline);
            }
            else if (aToken.type == "serverXchgInfo"){
                if (aToken.exchanges == null) {
                    Ext.MessageBox.alert('Msg', 'There were not requests to the server this day.');
                }
                else
                {
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
            else if (aToken.type == "serverXchgInfoXDays"){
            
                var lExchangeInfoForDays = [
                {
                    name:'1',
                    data1:  aToken["01"] || 0
                },{
                    name:'2',
                    data1:  aToken["02"] || 0
                },{
                    name:'3',
                    data1:  aToken["03"] || 0
                },{
                    name:'4',
                    data1:  aToken["04"] || 0
                },{
                    name:'5',
                    data1:  aToken["05"] || 0
                },{
                    name:'6',
                    data1:  aToken["06"] || 0
                },{
                    name:'7',
                    data1:  aToken["07"] || 0
                },{
                    name:'8',
                    data1:  aToken["08"] || 0
                },{
                    name:'9',
                    data1:  aToken["09"] || 0
                },{
                    name:'10',
                    data1:  aToken["10"] || 0
                },{
                    name:'11',
                    data1:  aToken["11"] || 0
                },{
                    name:'12',
                    data1:  aToken["12"] || 0
                },{
                    name:'13',
                    data1:  aToken["13"] || 0
                },{
                    name:'14',
                    data1:  aToken["14"] || 0
                },{
                    name:'15',
                    data1:  aToken["15"] || 0
                },{
                    name:'16',
                    data1:  aToken["16"] || 0
                },{
                    name:'17',
                    data1:  aToken["17"] || 0
                },{
                    name:'18',
                    data1:  aToken["18"] || 0
                },{
                    name:'19',
                    data1:  aToken["19"] || 0
                },{
                    name:'20',
                    data1:  aToken["20"] || 0
                },{
                    name:'21',
                    data1:  aToken["21"] || 0
                },{
                    name:'22',
                    data1:  aToken["22"] || 0
                },{
                    name:'23',
                    data1:  aToken["23"] || 0
                },{
                    name:'24',
                    data1:  aToken["24"] || 0
                },{
                    name:'25',
                    data1:  aToken["25"] || 0
                },{
                    name:'26',
                    data1:  aToken["26"] || 0
                },{
                    name:'27',
                    data1:  aToken["27"] || 0
                },{
                    name:'28',
                    data1:  aToken["28"] || 0
                },{
                    name:'29',
                    data1:  aToken["29"] || 0
                },{
                    name:'30',
                    data1:  aToken["30"] || 0
                },{
                    name:'31',
                    data1:  aToken["31"] || 0
                }];
                myStoreExchangeForDays.loadData(lExchangeInfoForDays);
            }
                 
            else if (aToken.type == "serverXchgInfoXMonth"){
             
                
                var lExchangeInfoForMonth = [
                {
                    name:'Jan',
                    data1:  aToken["01"] || 0
                },{
                    name:'Feb',
                    data1:  aToken["02"] || 0
                },{
                    name:'Mar',
                    data1:  aToken["03"] || 0
                },{
                    name:'Apr',
                    data1:  aToken["04"] || 0
                },{
                    name:'May',
                    data1:  aToken["05"] || 0
                },{
                    name:'Jun',
                    data1:  aToken["06"] || 0
                },{
                    name:'Jul',
                    data1:  aToken["07"] || 0
                },{
                    name:'Aug',
                    data1:  aToken["08"] || 0
                },{
                    name:'Sep',
                    data1:  aToken["09"] || 0
                },{
                    name:'Oct',
                    data1:  aToken["10"] || 0
                },{
                    name:'Nov',
                    data1:  aToken["11"] || 0
                },{
                    name:'Dec',
                    data1:  aToken["12"] || 0
                }];
                myStoreExchangeForMonth.loadData(lExchangeInfoForMonth);
                
            }
        }
        
        else if (aToken.ns == "org.jwebsocket.plugins.system") {
            if(aToken.type == "welcome") {
                $("#client_id").text("Client-ID: " + aToken.sourceId);
                $("#client_status").removeClass("offline").removeClass("online").addClass("authenticated").text("online");
                $("#websocket_type").text("WebSocket: " + (jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)" ));
            }
        }
    }
    
    Ext.jws.addPlugIn(plugin);
   
  
    var graph = new Ext.TabPanel({
        renderTo: Ext.getBody(),
        id: 'main-tabs',
        height: 380,        
        width: 430,
        activeTab: 0,
        border:false,
        margin:0,
        defaults: {
            padding: 10
        },
        //hideTabStripItem:true,
        bodyStyle:'background:#e4e4e4;',
        items:[        
        {
            title: 'Machines Resources',
            id: 'tab1',           
            layout: 'fit',   
            border:false,
            items: {
                xtype: 'chart',
                border:false,
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
                    minimum: 0,
                    maximum:100
                }, {
                    type: 'Category',
                    position: 'bottom',
                    fields: ['name'],
                    title: 'Machines Resources'
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
                        display: 'outside',
                        'text-anchor': 'middle',
                        field: 'data1',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'horizontal',
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
            border:false,
            layout: 'fit',          
            items: {
                xtype: 'chart',
                id: 'chartCmp',               
                animate: true,
                border:false,
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
        renderTo: Ext.getBody(),
        id: 'exchanges-main-tabs',
        height: 380,        
        width: 430,
        activeTab: 0,
        border:false,
        margin:0,
        defaults: {
            padding: 10
        },
        hideTabStripItem:true,
        bodyStyle:'background:#e4e4e4;',
        items:[{
            title: 'Exchanges to Hours',
            id: 'tab3',           
            layout: 'fit',   
            border:false,
            items: {
                xtype: 'chart',
                style: 'background:#e4e4e4',
                animate: false,
                border:false,
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
                    title: 'Exchanges to Hours'
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
                        display: 'outside',                        
                        'text-anchor': 'middle',
                        field: 'data1',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'horizontal',
                        color: '#333'
                    },
                    xField: 'name',
                    yField: 'data1'
                }]
            }
            
        },
        {
            title: 'Exchanges to Days',
            id: 'tab4',           
            layout: 'fit',   
            border:false,
            items: {
                xtype: 'chart',
                style: 'background:#e4e4e4',
                animate: false,
                border:false,
                shadow: false,
                store: myStoreExchangeForDays,
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
                    title: 'Exchanges to Days'
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
                        display: 'outside',
                        'text-anchor': 'middle',
                        field: 'data1',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'horizontal',
                        color: '#333'
                    },
                    xField: 'name',
                    yField: 'data1'
                }]
            }
            
        },
        {
            title: 'Exchanges to Months',
            id: 'tab5',           
            layout: 'fit',    
            border:false,
            items:  {
                xtype: 'chart',
                style: 'background:#e4e4e4',
                animate: false,
                border:false,
                shadow: false,
                store: myStoreExchangeForMonth,
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
                    title: 'Exchanges to Months'
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
                        display: 'outside',
                        'text-anchor': 'middle',
                        field: 'data1',
                        renderer: Ext.util.Format.numberRenderer('0'),
                        orientation: 'horizontal',
                        color: '#333'
                    },
                    xField: 'name',
                    yField: 'data1'
                }]
            }
            
        }]
    });
  
    var graph_users_online = new Ext.TabPanel({//('Ext.TabPanel', {
          
        renderTo: Ext.getBody(),
        id: 'userOnline-main-tabs',
        // layout: 'fit',
        height: 380,        
        width: 430,
        border:false,
        activeTab: 0,
        margin:0,
        defaults: {
            padding: 10
        },
        hideTabStripItem:true,
        bodyStyle:'background:#e4e4e4;',
        items:[
        
        {
            title: 'Online Users',
            id: 'tab6',           
            layout: 'fit',  
            border:false,
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
                    title: 'Online Users'
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
                        display: 'outside',
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
   
    });
    
      
    
    var mainPanel = new Ext.Panel({
        id:'principal',
        border:false,
        frame:false,
        width: 700, 
        x:0,
        y:0,
        activeItem:0,
        layout:'card',
        //collapsible:false,
        items:[graph, graph_exchanges, graph_users_online]
    });
    
        
    var text_right = new Ext.Panel({
        frame:false,
        border:false,
        width:350,
        height: 100,
        html:'Charting Demo allows real-time graph server behavior. To see the behavior of the server to select the data source and type of graphic in which to display the required information.',
        x:20,
        y:10,
        //  layout:'absolute',
        bodyStyle:'background:#eaf4dc;'            
    });
    
    var graphics = new Ext.Panel({  
        frame:true,
        border:false,
        x:10,
        y:10,         
        width: 710,  
        height: 380,  
        // paddingLeft:12,
        items: [mainPanel]  
    });  
    
     
    var chartingDemo = new Ext.Window({
        //title:'Charting Demo',
        //        cls: 'box',
        renderTo:'content',
        x:0,
        y:85, 
        width: 720,
        height: 490,  
        border:false,
        frame:true,
        resizable: false,
        closable: false,
        draggable: false,
        maximizable :false,
        cls:"border",
        minimizable :false,
        // bodyStyle:'background:transparent url(/ChartClient/demo/images/banner.png) no-repeat;',
        bodyStyle:'background:#eaf4dc;',
        items: [text_right,graphics,chooseData] 
    
    });
    chartingDemo.show(); 

    
   
    
    comboGraphics.on('select',function(cmb, aRecord, aIndex){
        var lV      = comboGraphics.getValue();
        var aRecord = comboGraphics.findRecord(comboGraphics.valueField || comboGraphics.displayField, lV);
        var aIndex  = comboGraphics.store.indexOf(aRecord);
        
        
        
        var prin = Ext.getCmp('principal');
        if(aIndex == 0) {
            //send token register with your interest
            Ext.jws.send("monitoringPlugin.pcinfo","register", {
                "interest":"computerInfo"
            });

            dateFieldPanel.hide();
            comboExchangesXDays.hide();
            comboExchangesXMonth.hide();
            
            prin.layout.setActiveItem(0);
        }        
        else if (aIndex == 1) {
            Ext.jws.send("monitoringPlugin.pcinfo","register", {
                "interest":"serverXchgInfo"
            });
            dateFieldPanel.show();
            comboExchangesXDays.hide();
            comboExchangesXMonth.hide();
            
            var tab3 = Ext.getCmp('tab3');
            tab3.on('activate',function(){
                dateFieldPanel.show();
                comboExchangesXDays.hide();
                comboExchangesXMonth.hide();
            });
                       
            var tab4 = Ext.getCmp('tab4');
            tab4.on('activate',function(){
                comboExchangesXDays.show();
                dateFieldPanel.hide();
                comboExchangesXMonth.hide();
                
                var lmonth = {
                    interest: "serverXchgInfoXDays",
                    month: '01'                  
                };
                
                Ext.jws.send("monitoringPlugin.pcinfo","register", lmonth);
                
            });
            
            var tab5 = Ext.getCmp('tab5');
            tab5.on('activate',function(){
                comboExchangesXMonth.show();
                dateFieldPanel.hide();
                comboExchangesXDays.hide();
                
                var lYear = {
                    interest: "serverXchgInfoXMonth",
                    year: '2012'                  
                };
        
                Ext.jws.send("monitoringPlugin.pcinfo","register", lYear);
                
            });
            
            prin.layout.setActiveItem(1);
        }
        else if (aIndex == 2) {
            Ext.jws.send("monitoringPlugin.pcinfo","register", {
                "interest":"userInfo"
            });
            dateFieldPanel.hide();
            comboExchangesXDays.hide();
            comboExchangesXMonth.hide();
            prin.layout.setActiveItem(2);
            
        }
       
    },this);    
    
    
    comboExchangesXDays.on('select',function(cmb, aRecord, aIndex){
        var lV      = comboExchangesXDays.getValue();
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
            interest: "serverXchgInfoXDays",
            month: mes                  
        };
                       
        Ext.jws.send("monitoringPlugin.pcinfo","register", lmonth);
    },this);    
    comboExchangesXDays.setValue("January");
    
    comboExchangesXMonth.on('select',function(cmb, aRecord, aIndex){
        var lV      = comboExchangesXMonth.getValue();
                
        var lYear = {
            interest: "serverXchgInfoXMonth",
            year: lV                  
        };
        
        Ext.jws.send("monitoringPlugin.pcinfo","register", lYear);
    },this);    
    comboExchangesXMonth.setValue("2012");
    
    comboGraphics.setValue("Machine Resources");
    //send token register with your interest
    Ext.jws.send("monitoringPlugin.pcinfo","register", {
        "interest":"computerInfo"
    });
    Ext.getCmp('principal').layout.setActiveItem(0);
}