Ext.define('IS.view.definition.List' ,{
	extend: 'Ext.grid.Panel',
	alias: 'widget.d_list',
	id: 'definitionsGrid',
	border: 0,
	store: 'Definitions',
	autoScroll: true,
	enableColumnHide: false,
	minHeight: 475,
	dockedItems: [{
		xtype: 'd_toolbar'
	},{
		xtype: 'pagingtoolbar',
		store: 'Definitions',
		id: 'definitionsPager',
		dock: 'bottom',
		displayInfo: false,
		beforePageText: '',
		afterPageText: ''
	}],
	columns: [{
		header: 'Type',  
		dataIndex: 'type',  
		flex: 1
	}],
	viewConfig: {
		loadMask: false
	},
	listeners: {
		render: function(aGrid) {
			aGrid.getView().on('render', function(aView) {
				aView.tip = Ext.create('Ext.tip.ToolTip', {
					target: aView.el,
					delegate: aView.itemSelector,
					trackMouse: true,
					renderTo: Ext.getBody(),
					listeners: {
						beforeshow: function updateTipBody(tip) {
							tip.update('<b>Type:</b> ' + aView.getRecord(tip.triggerElement).get('type'));
						}
					}
				});
			});
		}
	}
});
