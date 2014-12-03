Ext.define('CRUD.store.SWork', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'CRUD.model.MWork',
    pageSize: 10,
    id:'swork',
    proxy: new Ext.create('Ext.jws.data.Proxy', {
        ns: 'org.jwebsocket.plugins.scripting',
        //folder name
        app:'jws_extjs_crud',
        objectId:'Crud',
        //params:['Aquiles','Perez Miranda'],
        api: {
            create: 'create',
            read: 'read',
            update: 'update',
            destroy: 'destroy'
        },
        reader: {
            type:'json',
            root: 'users',
            totalProperty: 'totalCount'
        }
    })
});
