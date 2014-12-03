/*
 *
 * @author Osvaldo Aguilar Lauzurique
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
    'Ext.selection.CheckboxModel',
    'Ext.form.*',
    'Ext.window.Window',
    'Ext.jws.Client',
    'Ext.jws.data.Proxy',
    'Ext.jws.form.action.Load',
    'Ext.jws.form.action.Submit'
]);

Ext.tip.QuickTipManager.init();

Ext.define('jws.quotaPlugin.alterWindow', {
    extend: 'Ext.window.Window',
    layout: 'hbox',
    width: 620,
    autoShow: false,
    modal: true,
    title: "Alter Quota",
    initComponent: function() {

        var me = this;
        var lQuotaObject = me.initialConfig;


        var lQuotaValueFunction = function(aValue) {
            Ext.getCmp("get_quota_value").setValue(aValue);
        };

        var lSetQuotaFunction = function() {

            var lValue = Ext.getCmp("set_quota_field").getValue();

            if (!Ext.isNumeric(lValue)) {
                Ext.Msg.alert("Alert", +"(" + lValue + ") Must be numeric");
                return;
            }

            var lArguments = {
                uuid: lQuotaObject.uuid,
                identifier: lQuotaObject.identifier,
                value: lValue,
                namespace: lQuotaObject.namespace,
                instance: lQuotaObject.instance,
                instance_type: lQuotaObject.instance_type,
                actions: lQuotaObject.actions
            };

            Ext.jwsClient.send(jws.QuotaDemo.NS_QUOTA_PLUGIN, jws.QuotaDemo.TT_SET_QUOTA, lArguments, {
                success: function(aResponse) {
                    lQuotaValueFunction(aResponse.value);
                },
                failure: function(aResponse){
                    if(aResponse.code == -1 ){
                        Ext.Msg.alert(aResponse.msg, "Permission denied, not enough privilege" );
                    }
                }
            });

        };

        var lReduceQuotaFunction = function() {

            var lValue = Ext.getCmp("reduce_quota").getValue();

            if (!Ext.isNumeric(lValue)) {
                Ext.Msg.alert("Alert", +"(" + lValue + ") Must be numeric");
                return;
            }

            var lArguments = {
                uuid: lQuotaObject.uuid,
                identifier: lQuotaObject.identifier,
                value: lValue,
                namespace: lQuotaObject.namespace,
                instance: lQuotaObject.instance,
                instance_type: lQuotaObject.instance_type,
                actions: lQuotaObject.actions
            };

            Ext.jwsClient.send(jws.QuotaDemo.NS_QUOTA_PLUGIN, jws.QuotaDemo.TT_REDUCE_QUOTA, lArguments, {
                success: function(aResponse) {
                    lQuotaValueFunction(aResponse.value);
                },failure: function(aResponse){
                    if(aResponse.code == -1 ){
                        Ext.Msg.alert(aResponse.msg, "Permission denied, not enough privilege" );
                    }
                }
            });

        };

        var lIncreaseQuotaFunction = function() {

            var lValue = Ext.getCmp("increase_quota").getValue();

            if (!Ext.isNumeric(lValue)) {
                Ext.Msg.alert("Alert", +"(" + lValue + ") Must be numeric");
                return;
            }

            var lArguments = {
                uuid: lQuotaObject.uuid,
                identifier: lQuotaObject.identifier,
                value: lValue,
                namespace: lQuotaObject.namespace,
                instance: lQuotaObject.instance,
                instance_type: lQuotaObject.instance_type,
                actions: lQuotaObject.actions
            };

            Ext.jwsClient.send(jws.QuotaDemo.NS_QUOTA_PLUGIN, jws.QuotaDemo.TT_INCREASE_QUOTA, lArguments, {
                success: function(aResponse) {
                    lQuotaValueFunction(aResponse.value);
                },
                failure: function(aResponse){
                    if(aResponse.code == -1 ){
                        Ext.Msg.alert(aResponse.msg, "Permission denied, not enough privilege" );
                    }
                }
            });
        };

        var lGetQuotaFunction = function() {

            var lValue = Ext.getCmp("get_quota_value").getValue();

            if (!Ext.isNumeric(lValue)) {
                Ext.Msg.alert("Alert", +"(" + lValue + ") Must be numeric");
                return;
            }

            var lArguments = {
                uuid: lQuotaObject.uuid,
                identifier: lQuotaObject.identifier,
                value: lValue,
                namespace: lQuotaObject.namespace,
                instance: lQuotaObject.instance,
                instance_type: lQuotaObject.instance_type,
                actions: lQuotaObject.actions
            };

            Ext.jwsClient.send(jws.QuotaDemo.NS_QUOTA_PLUGIN, jws.QuotaDemo.TT_GET_QUOTA, lArguments, {
                success: function(aResponse) {
                    lQuotaValueFunction(aResponse.value);
                }
            });
        };


        this.items = [
            {
                xtype: 'form',
                bodyPadding: 15,
                width: 250,
                id: 'alterQuotaForm',
                margins: '10 5 10 10',
                title: 'Alter Form',
                height: 300,
                frame: true,
                jwsSubmit: true,
                bodyStyle: 'background-color:#D8E4F2',
                border: false,
                items: [
                    {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                width: 120,
                                xtype: 'textfield',
                                readOnly: true,
                                id: 'get_quota_value',
                                fieldLabel: 'Get Quota Value',
                                value: lQuotaObject.value,
                                vtype: 'num',
                                margins: '0 0 0 0',
                                allowBlank: true,
                                editable: false
                            }, {
                                xtype: 'button',
                                width: 80,
                                handler: lGetQuotaFunction,
                                iconCls: 'icon-accept',
                                margins: '20 0 0 2',
                                text: 'Execute'
                            }]
                    },
                    {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [
                            {
                                xtype: 'textfield',
                                width: 120,
                                vtype: 'num',
                                margins: '0 0 0 0',
                                name: 'set_quota_field',
                                id: 'set_quota_field',
                                fieldLabel: 'Set Quota value'
                            }, {
                                xtype: 'button',
                                width: 80,
                                handler: lSetQuotaFunction,
                                iconCls: 'icon-accept',
                                margins: '20 0 0 2',
                                text: 'Execute'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'increase_quota',
                                id: 'increase_quota',
                                width: 120,
                                margins: '0 0 0 0',
                                fieldLabel: 'Increase Quota Value'
                            }, {
                                xtype: 'button',
                                width: 80,
                                handler: lIncreaseQuotaFunction,
                                iconCls: 'icon-accept',
                                margins: '20 0 0 2',
                                text: 'Execute'
                            }]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'reduce_quota',
                                id: 'reduce_quota',
                                width: 120,
                                margins: '0 0 0 0',
                                fieldLabel: 'Reduce Quota Value'
                            }, {
                                xtype: 'button',
                                width: 80,
                                handler: lReduceQuotaFunction,
                                iconCls: 'icon-accept',
                                margins: '20 0 0 2',
                                text: 'Execute'
                            }]
                    }
                ]
            }, {
                xtype: 'form',
                margins: '10 5 10 5',
                title: "Quota Information",
                width: 330,
                frame: true,
                bodyStyle: 'background-color:#D8E4F2',
                height: 300,
                items: [{
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'UUID:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.uuid,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Identifier:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.identifier,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Type:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.quotaType,
                                margins: '0 0 0 0'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Name Space:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.namespace,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Instance',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.instance,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Instance Type:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.instance_type,
                                margins: '0 0 0 0'
                            }
                        ]
                    }

                ]
            }
        ];

        this.listeners = {
            destroy: function() {

                var lForm = Ext.getCmp('lFilterFormPanelQuota').getForm();
                var lStore = Ext.getCmp('gridPanelQuota').getStore();

                if (lForm.isValid()) {

                    var lFilterParams = lForm.getValues();
                    lStore.load({
                        params: lFilterParams
                    });
                }
            }
        };

        this.callParent(arguments);
    }
});

Ext.define('jws.quotaPlugin.registerQuotaWindow', {
    extend: 'Ext.window.Window',
    layout: 'hbox',
    width: 620,
    autoShow: false,
    modal: true,
    title: "Register Quota",
    initComponent: function() {

        var me = this;
        var lQuotaObject = me.initialConfig;

        var lSetRegisterQuota = function() {

            var lForm = Ext.getCmp("RegisterQuotaForm").getForm();

            if (lForm.isValid()) {

                var lFilterParams = lForm.getValues();

                var lArguments = {
                    uuid: lQuotaObject.uuid,
                    type: lQuotaObject.quotaType,
                    instance: lFilterParams.req_instance,
                    instance_type: 'User',
                    identifier: lQuotaObject.identifier
                };

                Ext.jwsClient.send(jws.QuotaDemo.NS_QUOTA_PLUGIN, jws.QuotaDemo.TT_REGISTER, lArguments, {
                    success: function(aToken) {
                        log(aToken);
                    },
                    failure: function(aToken) {
                        log(aToken);
                    }
                });
            }



        };

        this.items = [
            {
                xtype: 'form',
                bodyPadding: 15,
                width: 250,
                id: 'RegisterQuotaForm',
                margins: '10 5 10 10',
                title: 'Alter Form',
                height: 230,
                frame: true,
                jwsSubmit: true,
                bodyStyle: 'background-color:#D8E4F2',
                border: false,
                items: [
                    {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [
                            {
                                xtype: 'textfield',
                                width: 150,
                                allowBlank: false,
                                margins: '0 0 0 0',
                                name: 'req_instance',
                                id: 'reg_instance',
                                fieldLabel: 'Instance to Register'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [
                            {
                                xtype: 'button',
                                width: 150,
                                handler: lSetRegisterQuota,
                                iconCls: 'icon-add',
                                margins: '20 0 0 2',
                                text: 'Register Quota'
                            }]
                    }
                ]
            }, {
                xtype: 'form',
                margins: '10 5 10 5',
                title: "Quota Information",
                width: 330,
                frame: true,
                bodyStyle: 'background-color:#D8E4F2',
                height: 230,
                items: [{
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'UUID:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.uuid,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Type:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.quotaType,
                                margins: '0 0 0 0'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Name Space:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.namespace,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Instance',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.instance,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Instance Type:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.instance_type,
                                margins: '0 0 0 0'
                            }
                        ]
                    }, {
                        xtype: 'fieldcontainer',
                        labelStyle: 'font-weight:bold;padding:0',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        fieldDefaults: {
                            labelAlign: 'top'
                        },
                        items: [{
                                xtype: 'label',
                                text: 'Identifier:',
                                margins: '0 0 0 0',
                                width: 100
                            },
                            {
                                xtype: 'label',
                                flex: 1,
                                text: lQuotaObject.identifier,
                                margins: '0 0 0 0'
                            }
                        ]
                    }

                ]
            }
        ];

        this.listeners = {
            destroy: function() {

                var lForm = Ext.getCmp('lFilterFormPanelQuota').getForm();
                var lStore = Ext.getCmp('gridPanelQuota').getStore();

                if (lForm.isValid()) {

                    var lFilterParams = lForm.getValues();
                    lStore.load({
                        params: lFilterParams
                    });
                }
            }
        };

        this.callParent(arguments);
    }
});

//Model difinition
Ext.define('Quota', {
    extend: 'Ext.data.Model',
    fields: ['uuid', 'instance', 'namespace', 'identifier',
        'instance_type', 'actions', 'quotaType',
        {
            name: 'value',
            type: 'int'
        }
    ],
    hasMany: {
        model: 'QuotaChild',
        name: 'childQuotas'
    }
});

Ext.define('QuotaChild', {
    extend: 'Ext.data.Model',
    fields: ['q_instance', 'q_instance_type', 'q_uuid', 'q_value'],
    belongsTo: 'Quota'
});

jws.QuotaDemo = {
    NS_EXTJS_DEMO: jws.NS_BASE + '.plugins.sencha',
    NS_QUOTA_PLUGIN: jws.NS_BASE + '.plugins.quota',
// Type of tokens
    TT_OPEN: 'open',
    TT_WELCOME: 'welcome',
    TT_LOGON: 'logon',
    TT_CLOSE: 'close',
    TT_REGISTER: 'registerQuota',
    TT_CREATE: 'createQuota',
    TT_UNREGISTER: 'unregisterQuota',
    TT_QUERY: 'query',
    TT_GET_QUOTA: 'getQuota',
    TT_GET_ACTIVE_QUOTA: 'getActivesQuota',
    TT_SET_QUOTA: 'setQuota',
    TT_REDUCE_QUOTA: 'reduceQuota',
    TT_INCREASE_QUOTA: 'increaseQuota',
    TT_RESET: 'reset',
// Texts
    TEXT_WEBSOCKET: "WebSocket: ",
    TEXT_CLIENT_ID: "Client-ID: ",
    TEXT_AUTHENTICATED: "authenticated",
    CLS_AUTH: "authenticated",
    CLS_ONLINE: "online",
    CLS_OFFLINE: "offline"
};
//Define the model for all QuotaTypes
//TODO: Those data will be load from the server
jws.QuotaDemo.quota_types = [
    {
        name: 'CountDown'
    },
    {
        name: 'Interval'
    },
    {
        name: 'DiskSpace'
    },
    {
        name: 'Volume'
    },
    {
        name: 'ConcurrentUsers'
    },
    {
        name: 'AutoRecharge'
    },
    {
        name: 'Time'
    }
];

//Define the model for all Instance type
//TODO: Those data will be load from the server
jws.QuotaDemo.quota_instance = [
    {
        name: 'User'
    },
    {
        name: 'Group'
    }
];
jws.QuotaDemo.namespace_diskspace = [
    {
        name: 'private'
    },
    {
        name: 'public'
    }
];

Ext.regModel('Quota_types', {
    fields: [
        {
            type: 'string',
            name: 'name'
        },
        {
            type: 'string',
            name: 'quotaType'
        }
    ]
});

Ext.regModel('Quota_instances', {
    fields: [
        {
            type: 'string',
            name: 'name'
        }
    ]
});
Ext.regModel('Namespace_diskspace', {
    fields: [
        {
            type: 'string',
            name: 'name'
        }
    ]
});

jws.QuotaDemo.quota_type_store = Ext.create('Ext.data.Store', {
    model: 'Quota_types',
    data: jws.QuotaDemo.quota_types
});

jws.QuotaDemo.quota_instance_store = Ext.create('Ext.data.Store', {
    model: 'Quota_instances',
    data: jws.QuotaDemo.quota_instance
});

jws.QuotaDemo.namespace_diskspace_store = Ext.create('Ext.data.Store', {
    model: 'Namespace_diskspace',
    data: jws.QuotaDemo.namespace_diskspace
});


Ext.onReady(function() {
    // DOM elements
    var eDisconnectMessage = Ext.get("not_connected"),
            eBtnDisconnect = Ext.get("disconnect_button"),
            eBtnConnect = Ext.get("connect_button"),
            eClient = document.getElementById("client_status");

    eClientId = document.getElementById("client_id");
    eWebSocketType = document.getElementById("websocket_type");

    Ext.jwsClient.on(jws.QuotaDemo.TT_OPEN, function() {
        eClient.innerHTML = jws.QuotaDemo.TEXT_CONNECTED;
        eClient.className = jws.QuotaDemo.CLS_ONLINE;
        eBtnDisconnect.show();
        eBtnConnect.hide();
        eDisconnectMessage.hide();
        initDemo();
    });

    Ext.jwsClient.on(jws.QuotaDemo.TT_WELCOME, function(aToken) {
        if (aToken.username !== "anonymous") {
            // If the user is already logged in
            Ext.jwsClient.fireEvent(jws.QuotaDemo.TT_LOGON, aToken, Ext.jwsClient);
        }
    });


    Ext.jwsClient.on(jws.QuotaDemo.TT_CLOSE, function() {
        eClient.innerHTML = jws.QuotaDemo.TEXT_DISCONNECTED;
        eClient.className = jws.QuotaDemo.CLS_OFFLINE;
        eDisconnectMessage.show();
        eBtnDisconnect.hide();
        eBtnConnect.show();
        exitDemo();
        eWebSocketType.innerHTML = jws.QuotaDemo.TEXT_WEBSOCKET + "-";
        eClientId.innerHTML = jws.QuotaDemo.TEXT_CLIENT_ID + "- ";
    });
    eBtnDisconnect.hide();

    eBtnConnect.on("click", function() {
        Ext.jwsClient.open();
    });
    eBtnDisconnect.on("click", function() {

        var lWindowMain = Ext.WindowManager.get("mainWindowsQuotaPlugin");

        if (lWindowMain !== undefined) {

            lWindowMain.close();
        }
        Ext.jwsClient.close();
    });

    Ext.jwsClient.on('logoff', function(aResponse) {
        Ext.getCmp('user_label').setText('         ');
        closeQuotaPluginMainwindows();
        showQuotaPluginLoginWindows();
    });

    Ext.jwsClient.on(jws.QuotaDemo.TT_LOGON, function(aResponse) {
        eClient.innerHTML = jws.QuotaDemo.TEXT_AUTHENTICATED;
        eClient.className = jws.QuotaDemo.CLS_AUTH;
        showQuotaPluginMainWindows();
        closeQuotaPluginLoginWindows();
        Ext.getCmp('user_label').setText(aResponse.username);
        Ext.getCmp("namespace_diskspace").setVisible(false);
    });
    // Auto opening the connection
    Ext.jwsClient.open();
});

function logMessage(aToken) {
    
    var lMsgContiner = Ext.getCmp('msg_label');

    lMsgContiner.setText("");
    if (aToken.code === -1) {
        Ext.getCmp('msg_label').addCls('error_log');
        Ext.getCmp('msg_label').removeCls('ok_log');

        lMsgContiner.setText(aToken.msg);

        new Ext.fx.Anim({
            target: lMsgContiner,
            duration: 3000,
            from: {
                opacity: 100 //starting width 400
            },
            to: {
                opacity: 0
            }
        });

    } else {
        Ext.getCmp('msg_label').addCls('ok_log');
        Ext.getCmp('msg_label').removeCls('error_log');
        if (aToken.message !== undefined) {

            lMsgContiner.setText(aToken.message);
            
            new Ext.fx.Anim({
                target: lMsgContiner,
                duration: 3000,
                from: {
                    opacity: 100 //starting width 400
                },
                to: {
                    opacity: 0
                }
            });
        }

    }
}

function showQuotaPluginMainWindows() {

    var lWindowMain = Ext.WindowManager.get("mainWindowsQuotaPlugin");

    if (lWindowMain !== undefined) {
        lWindowMain.show();
        return;
    }

    var lProxyCfg = {
        ns: jws.QuotaDemo.NS_QUOTA_PLUGIN,
        api: {
            read: jws.QuotaDemo.TT_QUERY,
            destroy: jws.QuotaDemo.TT_UNREGISTER
        },
        reader: {
            root: 'data',
            totalProperty: 'totalCount'
        }
    };

    var lJWSProxy = new Ext.jws.data.Proxy(lProxyCfg);

    var lStore = new Ext.data.Store({
        pageSize: 50,
        model: 'Quota',
        proxy: lJWSProxy,
        listeners: {
            load: {
                fn: function(records, operation, success) {
                    
                     //the operation object contains all of the details of the load operation
                     Ext.each(expandedRecordRowExpandedExtQuota, function(el, index,to){
                         
                         var lEl = expandedRecordRowExpandedExtQuota.get(index);
                         if ( lEl.expanded == true ){
                             rowExpandedExtQuota.expandRow(index);
                         }
                     });
                }
            }
        }
    });

    var lJWSProxyQuotaActive = new Ext.jws.data.Proxy({
        ns: jws.QuotaDemo.NS_QUOTA_PLUGIN,
        api: {
            read: jws.QuotaDemo.TT_GET_ACTIVE_QUOTA
        },
        reader: {
            root: 'data',
            totalProperty: 'totalCount'
        }
    });

    var lStoreActiveQuota = new Ext.data.Store({
        model: 'Quota_types',
        proxy: lJWSProxyQuotaActive
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
    var lFilterFormPanel = Ext.create('Ext.form.Panel', {
        jwsSubmit: true,
        id: 'lFilterFormPanelQuota',
        border: false,
        bodyStyle: 'background-color:#fafafa',
        bodyPadding: 10,
        fieldDefaults: {
            labelAlign: 'top',
            labelWidth: 100,
            labelStyle: 'font-weight:bold'

        },
        defaults: {
            margins: '0 0 0 0'
        },
        items: [{
                xtype: 'fieldcontainer',
                labelStyle: 'font-weight:bold;padding:0',
                layout: 'hbox',
                defaultType: 'textfield',
                fieldDefaults: {
                    labelAlign: 'top'
                },
                items: [
                    {
                        xtype: 'combobox',
                        flex: 1,
                        name: 'quotaType',
                        fieldLabel: 'Quota Type',
                        displayField: 'name',
                        store: jws.QuotaDemo.quota_type_store,
                        blankText: 'The Quota type is mandatory to filter',
                        queryMode: 'local',
                        typeAhead: true,
                        emptyText: 'select quota',
                        allowBlank: false
                    }, {
                        xtype: 'textfield',
                        name: 'namespace',
                        margins: '0 0 0 10',
                        flex: 2,
                        fieldLabel: 'Name Space',
                        emptyText: 'example: org.jwebsocket.smsPlugin'
                    }, {
                        xtype: 'textfield',
                        name: 'instance',
                        margins: '0 0 0 10',
                        fieldLabel: 'Instance',
                        flex: 1,
                        emptyText: 'example: myuser2013'
                    }, {
                        xtype: 'button',
                        fieldLabel: 'Pres to filter',
                        text: 'Filter',
                        margins: '20 0 0 10',
                        width: 70,
                        handler: function() {

                            var lForm = this.up('form').getForm();

                            if (lForm.isValid()) {

                                lFilterParams = lForm.getValues();
                                lStore.load({
                                    params: lFilterParams
                                });
                            }
                        }
                    }]
            }]
    });



    //=====form============
    var lFormPanel = Ext.create('Ext.form.Panel', {
        bodyPadding: 15,
        width: 630,
        height: 330,
        frame: true,
        title: "Create Quota",
        jwsSubmit: true,
        bodyStyle: 'background-color:#D8E4F2',
        border: false,
        items: [
            {
                xtype: 'fieldcontainer',
                labelStyle: 'font-weight:bold;padding:0',
                layout: 'hbox',
                defaultType: 'textfield',
                fieldDefaults: {
                    labelAlign: 'top'
                },
                items: [{
                        xtype: 'combobox',
                        flex: 1,
                        id: 'identifier',
                        name: 'identifier',
                        margins: '0 10 0 0',
                        fieldLabel: 'Select Quota ',
                        displayField: 'name',
                        valueField: 'name',
                        store: lStoreActiveQuota,
                        typeAhead: true,
                        allowBlank: false
                                /*listeners: {
                                 select: function(identifier) {
                                 if (identifier.getValue() === "DiskSpace") {
                                 Ext.getCmp("namespace").setVisible(false);
                                 Ext.getCmp("namespace").submitValue=false;
                                 Ext.getCmp("namespace_diskspace").setVisible(true);
                                 Ext.getCmp("namespace_diskspace").submitValue=true;
                                 } else {
                                 Ext.getCmp("namespace").setVisible(true);
                                 Ext.getCmp("namespace").submitValue=true;
                                 Ext.getCmp("namespace_diskspace").setVisible(false);
                                 Ext.getCmp("namespace_diskspace").submitValue=false;
                                 }
                                 }
                                 }*/
                    }, {
                        flex: 1,
                        xtype: 'textfield',
                        name: 'value',
                        id: 'value',
                        fieldLabel: 'Value',
                        vtype: 'num',
                        margins: '0 0 0 10',
                        allowBlank: false
                    }]
            },
            {
                xtype: 'fieldcontainer',
                labelStyle: 'font-weight:bold;padding:0',
                layout: 'hbox',
                defaultType: 'textfield',
                fieldDefaults: {
                    labelAlign: 'top'
                },
                items: [
                    {
                        xtype: 'textfield',
                        flex: 1,
                        margins: '10 10 0 0',
                        name: 'instance',
                        id: 'instance',
                        fieldLabel: 'Instance',
                        allowBlank: false,
                        emptyText: 'required...'
                    }, {
                        xtype: 'combobox',
                        flex: 1,
                        id: 'instance_type',
                        name: 'instance_type',
                        fieldLabel: 'Select the instance type',
                        displayField: 'name',
                        margins: '10 0 0 10',
                        store: jws.QuotaDemo.quota_instance_store,
                        allowBlank: false,
                        queryMode: 'local',
                        typeAhead: true
                    }
                ]
            }, {
                xtype: 'fieldcontainer',
                labelStyle: 'font-weight:bold;padding:0',
                layout: 'hbox',
                defaultType: 'textfield',
                fieldDefaults: {
                    labelAlign: 'top'
                },
                items: [
                    {
                        xtype: 'textfield',
                        name: 'namespace',
                        flex: 1,
                        margins: '10 0 0 0',
                        id: 'namespace',
                        fieldLabel: 'Name Space',
                        emptyText: 'required...'
                    },
                    {
                        xtype: 'combobox',
                        flex: 1,
                        id: 'namespace_diskspace',
                        name: 'namespace',
                        fieldLabel: 'Select the folder type',
                        displayField: 'name',
                        margins: '10 0 0 0',
                        store: jws.QuotaDemo.namespace_diskspace_store,
                        allowBlank: true,
                        queryMode: 'local',
                        typeAhead: true,
                        editable: false
                    }]
            }, {
                xtype: 'fieldcontainer',
                labelStyle: 'font-weight:bold;padding:0',
                layout: 'hbox',
                defaultType: 'textfield',
                fieldDefaults: {
                    labelAlign: 'top'
                },
                items: [
                    {
                        xtype: 'textfield',
                        name: 'actions',
                        flex: 1,
                        margins: '10 0 0 0',
                        id: 'actions',
                        fieldLabel: 'Actions',
                        emptyText: 'required...',
                        allowBlank: false
                    }]
            }
        ],
        buttons: [{
                text: 'Reset',
                handler: function() {
                    this.up('form').getForm().reset();
                }
            },
            {
                xtype: 'button',
                text: 'Create Quota',
                id: 'submit_button',
                width: 200,
                margins: '5 10 0 0',
                alignTo: 'br',
                bodyStyle: 'background-color:#D8E4F2',
                handler: function() {
                    var lForm = this.up('form').getForm();
                    var lAction = {
                        ns: jws.QuotaDemo.NS_QUOTA_PLUGIN,
                        tokentype: jws.QuotaDemo.TT_CREATE
                    };
                    if (lForm.isValid()) {


                        var lArguments = lForm.getValues();

                        Ext.jwsClient.send(lAction.ns, lAction.tokentype, lArguments, {
                            success: function(aToken) {
                                logMessage(aToken);
                            },
                            failure: function(aToken) {
                                logMessage(aToken);
                            }
                        });

                    } else {
                        logMessage({
                            code: -1,
                            msg: "Invalid Form"
                        });
                    }

                }
            }
        ]
    });





    //=====form============
    var lFormPanelTesting = Ext.create('Ext.form.Panel', {
        width: '100%',
        height: '450',
        id: "HelpPanel",
        frame: true,
        title: "Help",
        jwsSubmit: true,
        bodyStyle: 'background-color:#D8E4F2',
        border: false,
        html: "<iframe src='resources/quotaManagerHelp.htm' style='min-width:100%; min-height: 325px;'></iframe>"
    });

    //=====gridPanel=======
    lGridPanel = Ext.create('Ext.grid.Panel', {
        store: lStore,
        border: false,
        id: 'gridPanelQuota',
        frame: false,
        width: '100%',
        height: '230',
        mLastSelected: -1,
        viewConfig: {
            loadMask: false
        },
        iconCls: 'icon-user',
        columns: [{
                text: 'Uuid',
                width: 170,
                sortable: true,
                dataIndex: 'uuid'
            }, {
                text: 'Name space',
                width: 180,
                sortable: true,
                dataIndex: 'namespace'
            }, {
                header: 'Quota Identifier',
                width: 100,
                sortable: true,
                dataIndex: 'identifier'
            }, {
                text: 'Instance',
                width: 60,
                sortable: true,
                dataIndex: 'instance'
            }, {
                text: 'Actions',
                width: 60,
                sortable: true,
                dataIndex: 'actions'
            }, {
                text: 'Instance type',
                width: 80,
                sortable: true,
                dataIndex: 'instance_type'
            }, {
                text: 'Value',
                width: 40,
                sortable: true,
                dataIndex: 'value'
            }],
        listeners: {
            select: function(aView, aRecord) {
                lGridPanel.mLastSelected = aRecord.index;
            },
            selectionchange: function(aSelModel, aSelections) {

                Ext.getCmp('unregisterQuotabtn').setDisabled(aSelections.length === 0);
                Ext.getCmp('alterQuotabtn').setDisabled(aSelections.length === 0);
                Ext.getCmp('registerQuotabtn').setDisabled(aSelections.length === 0);

            }
        },
        plugins: [{
                ptype: 'rowexpander',
                rowBodyTpl: [
                    '<div style="float:left;display:block;width:100%;heigth:40px;padding-left:18em;background-color:#003399;color:white;">REGISTERED QUOTAS</div>',
                    '<tpl for="childQuotas">', // interrogate the kids property within the data
                    '<div style="float:left;display:block;padding-left:.5em;heigth:30px;">',
                    '<div style="float:left;display:inline;width:160px;">User: {q_instance}</div>',
                    '<div style="float:left;display:inline;width:180px">Value: {q_value}</div>',
                    '<div id="{q_uuid}-{q_instance}-alter-{q_value}" onclick="lAlterQuotaChildClick(this)" class="icon-alter row-icon" title="Alater quota"></div>',
                    '<div id="{q_uuid}-{q_instance}-remove" onclick="lUnregisterQuotaIconClick(this)" class="icon-delete row-icon" title="Unregister quota"></div>',
                    '</div>',
                    '</tpl>'
                ]
            }]
    });


    lAlterQuotaChildClick = function(el) {
        var lSelection = lGridPanel.getView().getSelectionModel().getSelection()[0];
        var childQuota = el.id.split("-");

        if (lSelection && childQuota[2] === 'alter') {

            var lparams = {
                'actions': lSelection.data['actions'],
                'identifier': lSelection.data['identifier'],
                'instance': childQuota[1],
                'instance_type': "User",
                'namespace': lSelection.data['namespace'],
                'quotaType': lSelection.data['quotaType'],
                'uuid': childQuota[0],
                'value': childQuota[3]
            };

            var lAlterWin = Ext.create('jws.quotaPlugin.alterWindow', lparams);
            lAlterWin.show();
        }


    };


    lUnregisterQuotaIconClick = function(el) {

        var lSelection = lGridPanel.getView().getSelectionModel().getSelection()[0];
        if (lSelection) {

            var childQuota = el.id.split("-");
            log(childQuota);

            var lArguments = {
                identifier: lSelection.data['identifier'],
                uuid: childQuota[0],
                instance: childQuota[1]
            };


            Ext.jwsClient.send(jws.QuotaDemo.NS_QUOTA_PLUGIN, jws.QuotaDemo.TT_UNREGISTER, lArguments, {
                success: function( ) {
                    var lForm = lFilterFormPanel.getForm();
                    if (lForm.isValid()) {

                        var lFilterParams = lForm.getValues();
                        lStore.load({
                            params: lFilterParams
                        });
                    }
                }
            });

        }
    };


    var lDeleteQuotaClick = function() {
        var lSelection = lGridPanel.getView().getSelectionModel().getSelection()[0];
        if (lSelection) {
            var lArguments = lSelection.data;

            Ext.jwsClient.send(jws.QuotaDemo.NS_QUOTA_PLUGIN, jws.QuotaDemo.TT_UNREGISTER, lArguments, {
                success: function( ) {
                    var lForm = lFilterFormPanel.getForm();
                    if (lForm.isValid()) {

                        var lFilterParams = lForm.getValues();
                        lStore.load({
                            params: lFilterParams
                        });
                    }
                }
            });

        }
    };

    var lRegisterQuotaClick = function() {

        var lSelection = lGridPanel.getView().getSelectionModel().getSelection()[0];

        var lAlterWin = Ext.create('jws.quotaPlugin.registerQuotaWindow', lSelection.data);
        lAlterWin.show();
    };


    var lAlterQuotaClick = function() {
        var lSelection = lGridPanel.getView().getSelectionModel().getSelection()[0];

        var lAlterWin = Ext.create('jws.quotaPlugin.alterWindow', lSelection.data);
        lAlterWin.show();
    };

    var lFilterPanel = Ext.create('Ext.panel.Panel', {
        title: 'Quota Admin',
        id: "formPanelFilter",
        items: [lFilterFormPanel, lGridPanel],
        dockedItems: [{
                xtype: 'toolbar',
                items: [{
                        iconCls: 'icon-alter',
                        text: 'Alter Quota',
                        id: 'alterQuotabtn',
                        disabled: true,
                        handler: lAlterQuotaClick
                    }, {
                        iconCls: 'icon-delete',
                        text: 'Delete Quota',
                        disabled: true,
                        id: 'unregisterQuotabtn',
                        handler: lDeleteQuotaClick
                    }, {
                        iconCls: 'icon-add',
                        text: 'Register Quota',
                        disabled: true,
                        id: 'registerQuotabtn',
                        handler: lRegisterQuotaClick
                    }]
            }]
    });

    var lTabPanel = Ext.create('Ext.tab.Panel', {
        activeTab: 0,
        border: false,
        items: [lFilterPanel, lFormPanel, lFormPanelTesting],
        listeners: {
            tabchange: function(aTabPanel, aNewCard, aOldCard, aOptions) {
                Ext.getCmp('msg_label').setText("");
            }
        }
    });

    Ext.create('Ext.window.Window', {
        y: 90,
        x: 40,
        id: 'mainWindowsQuotaPlugin',
        bodyStyle: 'float: right;position:relative;padding:4px',
        width: 645,
        height: 400,
        border: false,
        closeAction: 'hide',
        resizable: false,
        draggable: false,
        closable: false,
        items: [lTabPanel],
        dockedItems: [{
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        xtype: 'label',
                        text: 'Message: '
                    },
                    {
                        xtype: 'label',
                        text: '',
                        id: 'msg_label',
                        margins: '5 0 0 0'
                    },
                    '->',
                    {
                        xtype: 'tbspacer'
                    },
                    {
                        xtype: 'label',
                        text: 'user: '
                    },
                    {
                        xtype: 'button',
                        style: 'width: auto; min-width: 50px;',
                        id: 'user_label',
                        iconCls: 'icon-user'
                    },
                    '|',
                    {
                        xtype: 'button',
                        componentCls: 'x-btn-default-toolbar-small-noicon x-over x-btn-over x-btn-default-toolbar-small-over',
                        text: 'logout',
                        listeners: {
                            'mouseout': function(aButton, aEvent) {
                                aButton.addCls('x-btn-default-toolbar-small-noicon x-over x-btn-over x-btn-default-toolbar-small-over');
                            }
                        },
                        handler: function() {
                            Ext.jwsClient.getConnection().logout();
                        }
                    }
                ]
            }]
    }).show();

    Ext.tip.QuickTipManager.register({
        target: 'identifier',
        text: 'Select the Quota Identifier to defined the behavior of your new quota',
        title: 'Description:',
        mouseOffset: [10, 0],
        dismissDelay: 10000
    });
    Ext.tip.QuickTipManager.register({
        target: 'value',
        text: 'Numeric value that gives to a quota, \n\
        indicating the number of times that the quota can be executed.',
        title: 'Description:',
        mouseOffset: [10, 0],
        dismissDelay: 10000
    });
    Ext.tip.QuickTipManager.register({
        target: 'instance',
        text: 'Represented in who will apply the quota',
        title: 'Description:',
        mouseOffset: [10, 0],
        dismissDelay: 10000
    });
    Ext.tip.QuickTipManager.register({
        target: 'instance_type',
        text: 'Define the instance type of the instance element, this element can take two values,\n\
         "User" when instance is a user and "Group" when instance is a user group.',
        title: 'Description:',
        mouseOffset: [10, 0],
        dismissDelay: 10000
    });
    Ext.tip.QuickTipManager.register({
        target: 'namespace',
        text: "The plugin's namespace to which the quota will be applied. \n\
        For example: org.jwebsocket.plugins.sms",
        title: 'Description:',
        mouseOffset: [10, 0],
        dismissDelay: 10000
    });
    Ext.tip.QuickTipManager.register({
        target: 'namespace_diskspace',
        text: 'Choose "private" to execute quota in private folders and\n\
         "public" to execute quota in public folders.',
        title: 'Description:',
        mouseOffset: [10, 0],
        dismissDelay: 10000
    });
    Ext.tip.QuickTipManager.register({
        target: 'actions',
        text: 'To restrict all actions in the plugin indicated per Name Space, then this value is set to "*".\n\
            If you only want to restrict some of the features and not all, you must specify\n\
             separated by a "," the actions that will be restricted by the quota.\n\
            For example:create, delete, update',
        title: 'Description:',
        mouseOffset: [10, 0],
        dismissDelay: 10000
    });

}

function closeQuotaPluginMainwindows() {
    var lWindowMain = Ext.WindowManager.get("mainWindowsQuotaPlugin");

    if (lWindowMain !== undefined) {

        lWindowMain.hide();
    }
}

function showQuotaPluginLoginWindows() {

    var lLoginWin = Ext.create('Ext.window.Window', {
        title: 'Loggin',
        x: 220,
        id: 'loginWindQuotaPlugin',
        items: [{
                xtype: 'form',
                bodyPadding: 10,
                border: 0,
                items: [{
                        xtype: 'textfield',
                        id: 'username',
                        name: 'username',
                        fieldLabel: 'Username',
                        value: 'root',
                        allowBlank: false

                    }, {
                        xtype: 'textfield',
                        inputType: 'password',
                        id: 'password',
                        name: 'password',
                        fieldLabel: 'Password',
                        value: 'root',
                        listeners: {
                            specialkey: function(af, aE) {
                                if (af.getKey() === aE.ENTER) {
                                    ldoQuotaPluginLogin(lLoginWin.down('form'));
                                }
                            }
                        }
                    }]
            }],
        buttons: [{
                text: 'Login',
                handler: function() {
                    ldoQuotaPluginLogin(lLoginWin.down('form'));
                }
            }]
    }).show();


    var ldoQuotaPluginLogin = function(alForm) {

        if (alForm.getForm().isValid()) {
            Ext.jwsClient.getConnection().systemLogon(alForm.down('textfield[name=username]').getValue(),
                    alForm.down('textfield[name=password]').getValue());
        }
    };

}

function closeQuotaPluginLoginWindows() {
    var lWindowLogin = Ext.WindowManager.get("loginWindQuotaPlugin");

    if (lWindowLogin !== undefined)
        lWindowLogin.close();
}

function initDemo() {


    var lPlugIn = {};
    lPlugIn.processToken = function(aToken) {

//		log(aToken);

        if (aToken.ns === jws.QuotaDemo.NS_QUOTA_PLUGIN) {
            
            if (aToken.reqType === "query" && aToken.msg === "ok")
                return;
            
            logMessage(aToken);

  
        }

        if (aToken.type === "welcome") {
            eClientId.innerHTML = jws.QuotaDemo.TEXT_CLIENT_ID + aToken.sourceId;
            eWebSocketType.innerHTML = jws.QuotaDemo.TEXT_WEBSOCKET + (jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)");
        }
    };
    Ext.jwsClient.addPlugIn(lPlugIn);

    showQuotaPluginLoginWindows();


}

function exitDemo() {
    var lWindowMain = Ext.WindowManager.get("mainWindowsQuotaPlugin"),
            lWindowLogin = Ext.WindowManager.get("loginWindQuotaPlugin");
    if (lWindowMain !== undefined) {
        lWindowMain.close();
    }
    if (lWindowLogin !== undefined) {
        lWindowLogin.close();
    }
}

function log(aMsg) {
    if (console && typeof console.log === "function") {
        //console.log(aMsg);
    }
}