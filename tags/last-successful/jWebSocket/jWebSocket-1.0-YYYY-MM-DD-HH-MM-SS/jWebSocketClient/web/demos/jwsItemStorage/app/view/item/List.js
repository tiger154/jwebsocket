Ext.define('IS.view.item.List' ,{
	extend: 'Ext.grid.Panel',
	alias : 'widget.i_list',
	border: 0,
	minHeight: 420,
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
							var lData = aView.getRecord(tip.triggerElement).data;
							var lInfo = '';
							for (var lKey in lData){
								lInfo += lKey + ': ' + lData[lKey] + '<br/>'
							}
							tip.update('<b>Preview</b> ' + '<br/>' + lInfo);
						}
					}
				});
			});
		}
	}
});
