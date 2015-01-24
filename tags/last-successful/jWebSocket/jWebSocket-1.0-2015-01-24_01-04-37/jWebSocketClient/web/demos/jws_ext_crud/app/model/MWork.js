Ext.define('CRUD.model.MWork', {
    extend: 'Ext.data.Model',
    idProperty: 'iduser',
    fields: [{
            name: 'iduser',
            type: 'int',
            useNull: false
        }, {
            name: 'name',
            type: 'string',
            useNull: true
        }, {
            name: 'last_name',
            type: 'string',
            useNull: true
        }, {
            name: 'address',
            type: 'string',
            useNull: true
        }, {
            name: 'zip_code',
            type: 'string',
            useNull: true
        }, {
            name: 'city_address',
            type: 'string',
            useNull: true
        }]


});
