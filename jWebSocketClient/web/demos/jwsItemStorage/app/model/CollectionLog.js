Ext.define('IS.model.CollectionLog', {
	extend: 'Ext.data.Model',
	fields: ['id', 'action', 'user', 'time'],
	idProperty: 'none'
});