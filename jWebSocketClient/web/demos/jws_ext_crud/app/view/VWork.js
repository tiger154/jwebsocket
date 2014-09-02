Ext.define('CRUD.view.VWork', {
    extend: 'Ext.form.Panel',
    alias: 'widget.vwork',
    ns:'org.jwebsocket.plugins.scripting',
    type:'callMethod',
    padding:10,
    frame:true,
    init: function() {
        this.callParent(arguments);

    },
    items: [{
            xtype: 'textfield',
            fieldLabel: 'User',
            value:'root'
        },{
            xtype: 'textfield',
            fieldLabel: 'Password',
            inputType: 'password',
            value:'root'
        },{
            xtype:'displayfield',
            fieldLabel:'Server Status',
            value:'Fail',
            fieldStyle:'color:red'
            
        }],
    buttons: [{
            text: 'Send'
        }]

});

