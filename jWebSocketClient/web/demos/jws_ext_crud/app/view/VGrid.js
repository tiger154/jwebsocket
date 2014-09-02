Ext.define('CRUD.view.VGrid', {
    extend: 'Ext.grid.Panel',
    store: 'SWork',
    constructor: function() {
        this.callParent(arguments);
        //this.getView().setLoading(false);
    },
    viewConfig:{
        loadMask: false
    },
    selModel: {
        selType: 'checkboxmodel',
        mode:'MULTI'
    },
    columns: [
        {xtype: 'rownumberer',text:'#'},
        {text: 'Name', dataIndex: 'name'
            , editor: {
                xtype: 'textfield',
                allowBlank: false,
                validateBlank: false
            }},
        {text: 'Last Name', dataIndex: 'last_name', editor: 'textfield', editor: {
                xtype: 'textfield',
                allowBlank: false,
                validateBlank: false
            }},
        {text: 'Address', dataIndex: 'address', flex: 1, editor: 'textfield', editor: {
                xtype: 'textfield',
                allowBlank: false,
                validateBlank: false
            }},
        {text: 'Zip Code', dataIndex: 'zip_code', flex: 1, editor: 'textfield', editor: {
                xtype: 'textfield',
                allowBlank: false,
                validateBlank: false
            }},
        {text: 'City Address', dataIndex: 'city_address', flex: 1, editor: 'textfield', editor: {
                xtype: 'textfield',
                allowBlank: false,
                validateBlank: false
            }}
    ],
    plugins: [Ext.create('Ext.grid.plugin.RowEditing', {
            clicksToEdit: 2,
            pluginId: 'celleditor',
            editing: true

        })],
    height: 140,
    width: 400,
    tbar: ['-', {
            itemId: 'add',
            text: '<b>Add</b>',
            disabled: false,
            iconCls: 'icon-add'
        }, '-', {
            itemId: 'delete',
            text: '<b>Delete</b>',
            disabled: true,
            iconCls: 'icon-delete'
        }, '-'],
    bbar: {
        xtype: 'pagingtoolbar',
        store: 'SWork'

    }

});

