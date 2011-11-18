/*
 *
 * Ext.data and Ext.grid need to be loaded
 */
Ext.require(['Ext.data.*', 'Ext.grid.*','Ext.form.*']);

Ext.define('Customer', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'id',
        type: 'int',
        useNull: true
    }, 'name', 'email',{
        name: 'age',
        type: 'int',
        useNull: true
    }]
});

Ext.onReady(function(){

   Ext.jws.open();

    Ext.jws.on('open',function(){
        initDemo();
    });

    Ext.jws.on('close',function(){
        alert("you are disconnect");
    });

});


function initDemo(){
    Ext.tip.QuickTipManager.init();

    var proxy_cfg = {
        ns:'jws.ext.gridformdemo',
        api:{
            create : 'create',
            read   : 'read',
            update : 'update',
            destroy: 'destroy'
        },
        reader: {
            root: 'data',
            totalProperty: 'totalCount'
        }

    };

    var jWSProxy  = new Ext.jws.data.proxy(proxy_cfg);

    Ext.define('form.load', {
        extend: 'Ext.data.Model',
        fields: ['name','email', 'age'],
        proxy:jWSProxy
    });


    var store = new Ext.data.Store({
        autoSync: true,
        autoLoad:true,
        pageSize: 10,
        model: 'Customer',
        proxy: jWSProxy,
        listeners: {
            write: function(store, operation){
                var record = operation.getRecords()[0],
                name = Ext.String.capitalize(operation.action),
                verb;
                
                if (name == 'Destroy') {
                    record = operation.records[0];
                    verb = 'Destroyed';
                } else {
                    verb = name + 'd';
                }

                var form = formPanel.getForm();
                form.loadRecord(record);
                Ext.getCmp('submit_button').setText('Update Customer');

                var message = Ext.String.format("{0} user: {1}", verb, record.getId());
                log(0, message);
            }
        }
    });

    var rowEditor = Ext.create('Ext.grid.plugin.RowEditing');

    // create Vtype for vtype:'num'
    var numTest = /^[0-9]+$/;
    Ext.apply(Ext.form.field.VTypes, {
        num: function(val, field) {
            return numTest.test(val);
        },
        // vtype Text property: The error text to display when the validation function returns false
        numText: 'Not a valid number.  Must be only numbers".'
    });


    //=====form============
    var formPanel = Ext.create('Ext.form.Panel', {
        frame: false,
        jwsSubmit:true,
        bodyPadding: 10,
        border: false,
        bodyStyle:'background-color:#D8E4F2',
        fieldDefaults: {
            msgTarget: 'side'
        },
        items: [{
            xtype: 'hidden',
            name: 'id',
            id: 'id'
        },{
            xtype: 'textfield',
            name: 'name',
            id: 'name',
            //vtype:'alphanum',
            fieldLabel: 'Nombre',
            allowBlank: false,
            emptyText:'required...',
            blankText:'required',
            minLength: 2
        },{
            xtype: 'textfield',
            name: 'email',
            id: 'email',
            fieldLabel: 'email',
            vtype: 'email',
            allowBlank: false,
            emptyText:'required...'
        },{
            xtype: 'textfield',
            name: 'age',
            id: 'age',
            fieldLabel: 'edad',
            vtype:'num',
            emptyText:'required...',
            allowBlank: false
        },{
            xtype: 'button',
            text: 'Add Customer',
            id:'submit_button',
            width: 130,
            handler: function() {

                var form = this.up('form').getForm();

                var action = null;
                if(form.findField('id').getValue() != ""){
                    action = {
                        ns: 'jws.ext.gridformdemo',
                        tokentype: 'update',
                        params:{
                            updateForm:'yes'
                        }
                    }
                }else{
                    action = {
                        ns: 'jws.ext.gridformdemo',
                        tokentype: 'create'
                    }
                }
                action.failure = function(form, action) {
                    if(action == 'undefined'){
                         var message = "Please you have errors in the form";
                         log(-1,message);
                    }else{
                        log(-1, action.response.message);
                        
                    }
                }

                action.success = function(form,action) {
                    Ext.getCmp('submit_button').setText('Add Customer');
                    form.reset();
                    log(action.response.code, action.response.message);
                }
                
                if(form.isValid())
                    form.submit(action);
                else{
                    var message = "Please you have errors in the form";
                    alert('por aqui');
                    log(-1,message);

                }
                
            }
        },
        {
            xtype: 'button',
            text: 'Reset',
            width: 130,
            handler: function() {
                var form = this.up('form').getForm();
                Ext.getCmp('submit_button').setText('Add Customer');
                form.reset();
            }
        }]
    });

    
    //=====gridPanel=======
    var grid = Ext.create('Ext.grid.Panel', {
        store: store,
        border:false,
        frame:false,
        plugins: [rowEditor],
        width: 400,
        height: 350,
        viewConfig: {
            loadMask: false
        },
        iconCls: 'icon-user',
        columns: [{
            text: 'ID',
            width: 45,
            sortable: true,
            dataIndex: 'id'
        },{
            text: 'Name',
            width: 150,
            sortable: true,
            dataIndex: 'name',
            field: {
                xtype: 'textfield',
                allowBlank: false,
                vtype:'alpha'
            }
        }, {
            header: 'Email',
            width: 160,
            sortable: true,
            dataIndex: 'email',
            field: {
                xtype: 'textfield',
                allowBlank: false,
                vtype:'email'
            }
        }, {
            text: 'Age',
            width: 60,
            sortable: true,
            dataIndex: 'age',
            field: {
                xtype: 'textfield',
                vtype:'num',
                allowBlank: false
            }
        }],
        listeners:{
            itemclick:function( view, record) {
                var form = formPanel.getForm();
                form.loadRecord(record);
                Ext.getCmp('submit_button').setText('Update Customer');
            }
        } ,
        dockedItems: [{
            xtype: 'toolbar',
            items: [{
                text: 'Add',
                iconCls: 'icon-add',
                handler: function(){
                    var phantoms =  store.getNewRecords();
                    Ext.Array.each(phantoms, function(el) {
                        store.remove(el);
                    });

                    store.insert(0, new Customer());

                    rowEditor.startEdit(0, 0);
                }
            }, '-', {
                itemId: 'delete',
                text: 'Delete',
                iconCls: 'icon-delete',
                disabled: true,
                handler: function(){

                    var selection = grid.getView().getSelectionModel().getSelection()[0];
                    if (selection) {
                        var id =  selection.data.id;
                        store.remove(selection);
                    }
                }
            }]
        }],
        bbar: Ext.create('Ext.PagingToolbar', {
            store: store,
            displayInfo: true,
            displayMsg: 'Displaying topics {0} - {1} of {2}',
            emptyMsg: "No topics to display"
        })
    });
    grid.getSelectionModel().on('selectionchange', function(selModel, selections){
        grid.down('#delete').setDisabled(selections.length === 0);
    });

    function log(type,message){
        var body = Ext.get('console');
        if(type == 0){
            body.update('<i>Last action</i><br> \n\
                <b style=color:green> Message: </b> '+message);
        }else if(type == -1){
            body.update('<i>Last action</i><br>\n\
                <b style=color:red> Message: </b> '+message);
        }
    }


    var tabpanel = Ext.create('Ext.tab.Panel', {
        width: 280,
        height: 200,
        renderTo: document.body,
        activeTab: 0,
        items: [{
            title: 'Output Messages',
            id:'message',
            bodyStyle:'padding:5px;',
            html:'<div id="console"></div>'
        }, {
            title: 'Contact',
            contentEl:'contact'
        }]
    });


    Ext.create('Ext.window.Window', {
        title: 'ExtJS form jWebSocket demo',
        x:30,
        y:30,
        closable:false,
        layout: 'fit',
        width: 300,
        items: [formPanel]
    }).show();


    Ext.create('Ext.window.Window', {
        title: 'ExtJS Grid jWebSocket demo',
        layout:'fit',
        y:30,
        x: 385,
        bodyStyle:'padding:5px',
        width: 450,
        closable:false,
        items: [grid]
    }).show();

    Ext.create('Ext.window.Window', {
        title: 'Console jWebSocket demo',
        x:30,
        y:200,
        border:false,
        closable:false,
        layout: 'fit',
        items: [tabpanel]
    }).show();

    var plugin = {}
    plugin.processToken = function(aToken){
        if (aToken.ns == 'jws.ext.gridformdemo'){

            if (aToken.type == 'notifyCreate' || aToken.type == 'notifyUpdate' || aToken.type == 'notifyDestroy'){
                log(0,aToken.message);
                store.load();
            }
        }
    }
    Ext.jws.addPlugIn(plugin);


}