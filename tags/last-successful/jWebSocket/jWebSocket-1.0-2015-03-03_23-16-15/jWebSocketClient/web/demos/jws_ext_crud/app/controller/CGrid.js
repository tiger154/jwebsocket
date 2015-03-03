Ext.define('CRUD.controller.CGrid', {
    extend: 'Ext.app.Controller',
    views: ['VGrid'],
    init: function() {
        this.control({
            'grid': {
                edit: this.onEdit,
                select: this.onSelect
             },   
            '#delete': {
                click: this.onDelete
            },
            '#add': {
                click: this.onCreate
            }
        });
    },
    onEdit: function(editor, context, opt) {
        if (context.record.data) {
            context.store.save();
            context.store.load();
        }
        
        

    },
    onSelect: function(rowModel, record, index, opt) {
        var button_delete = Ext.ComponentQuery.query('grid toolbar button')[1];
        button_delete.setDisabled(false);

    },
    onDelete: function(b, e, opt) {

        Ext.Msg.show({
            title: 'Warning',
            msg: 'Do you want remove this item?',
            width: 300,
            buttons: Ext.Msg.YESNO,
            fn: function(b, text, opt) {

                if (b == 'yes') {
                    var grid = Ext.ComponentQuery.query('grid')[0];
                    var store = grid.getStore();
                    var selection = grid.getSelectionModel().getSelection();
                    store.remove(selection);
                    store.save();
                    store.load();

                }

            },
            icon: Ext.Msg.WARNING
        });



    },
    onCreate: function(b, e, opt) {

        var grid = Ext.ComponentQuery.query('grid')[0];
        var store = grid.getStore();
        
        store.insert(store.getCount(),new Ext.create('CRUD.model.MWork'));
        var editor = grid.getPlugin('celleditor');
        
        editor.startEdit(store.getCount()-1,0);
        
    }



});
