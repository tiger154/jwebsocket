Ext.define('IS.controller.Item', {
	extend: 'Ext.app.Controller',
	stores: ['ItemLogs'],
	models: ['ItemLog'],
	views: ['item.Toolbar', 'item.List'],
	refs: [{
		ref: 'workspace',
		selector: '#workspace'
	}],

	getActiveItem: function(aTab){
		return aTab.down('grid').getSelectionModel().getSelection()[0];
	},
	
	checkAuthorization: function(aCollectionName, aAction){
		Ext.jws.send(jws.ItemStoragePlugIn.NS, 'findCollection', {
			collectionName: aCollectionName
		}, {
			success: function( aResponse ) {
				var lAuthorized = aResponse.data.authorized;
				if (lAuthorized){
					aAction();
					return;
				}
				
				var lView = Ext.create('IS.view.collection.EnterSecretPwd', {
					title: 'Authorization required',
					text: 'To write content on a collection is required that you be authorized first.',
					textColor: 'green',
					doAction : function (){
						var lForm = this.down('form');
						if (!lForm.getForm().isValid()){
							return;
						}
						
						var lArguments = lForm.getValues();
						lArguments.collectionName = aCollectionName;
					
						Ext.jws.send(jws.ItemStoragePlugIn.NS, 'authorize', lArguments, {
							success: function() {
								aAction();
								lView.close();
							}
						});
					}
				});
				lView.showAt({
					y: 100
				});
			}
		});
	},

	init: function() {
		var self = this;
		
		this.application.on('itemSaved', function(aEvent){
			var lCollectionTab = this.getWorkspace().queryById(aEvent.collectionName);
			if (lCollectionTab){
				lCollectionTab.down('grid').getStore().reload();
			}
		}, this);
		this.application.on('itemRemoved', function(aEvent){
			var lCollectionTab = this.getWorkspace().queryById(aEvent.collectionName);
			if (lCollectionTab){
				lCollectionTab.down('grid').getStore().reload();
			}
		}, this);
		
		this.control({
			'i_create button[action=save]': {
				click: function( aButton ){
					var lView = aButton.up('window');
					var lForm = lView.down('form');
					if (!lForm.getForm().isValid()){
						return;
					}
					
					var lItem = lForm.getForm().getFieldValues();
					if (lView.recordId){
						lItem['id'] = lView.recordId;
					}
					Ext.jws.send(jws.ItemStoragePlugIn.NS, 'saveItem', {
						collectionName: lView.collectionName,
						item: lItem
					}, {
						success: function() {
							lView.close();
						}
					});
				}
			},
			'i_toolbar > button[iconCls=i_add]': {
				click: function( aButton ){
					var lCollectionName = aButton.findParentByType('tabpanel').getActiveTab().getId()
					
					self.checkAuthorization(lCollectionName, function(){
						var lDef = self.application.collection2def[lCollectionName];
					
						var lView = Ext.create('IS.view.item.Create');
						lView.link = aButton;
						lView.loadForCreation(lCollectionName, lDef);
					});
				}
			},
			'i_toolbar > button[iconCls=i_edit]': {
				click: function( aButton ){
					var lTab = aButton.findParentByType('tabpanel').getActiveTab();
					var lCollectionName = lTab.getId()
					var lRecord = self.getActiveItem(lTab);
					
					self.checkAuthorization(lCollectionName, function(){
						var lDef = self.application.collection2def[lCollectionName];
					
						var lView = Ext.create('IS.view.item.Create');
						lView.link = aButton;
						lView.loadForEdit(lCollectionName, lDef, lRecord);
					});
				}
			},
			'i_toolbar > button[iconCls=i_details]': {
				click: function( aButton ){
					var lTab = aButton.findParentByType('tabpanel').getActiveTab();
					var lCollectionName = lTab.getId()
					var lRecord = self.getActiveItem(lTab);
					
					var lDef = self.application.collection2def[lCollectionName];
					
					var lView  = Ext.create('IS.view.item.Details');
					lView.link = aButton;
					lView.loadData( lRecord.get(lDef.pk_attr), lRecord.data, IS.lib.Util.createDetailsTpl(lDef));
				}
			},
			'i_toolbar > button[iconCls=history]': {
				click: function( aButton ){
					if (!IS.lib.Util.isEE()){
						return;
					}
					
					var lTab = aButton.findParentByType('tabpanel').getActiveTab();
					var lCollectionName = lTab.getId()
					var lRecord = self.getActiveItem(lTab);
					var lDef = self.application.collection2def[lCollectionName];
					console.log(lDef);
					self.checkAuthorization(lCollectionName, function(){
						if (!self.historyView){
							self.historyView = Ext.create('IS.view.item.History');
						}
						self.historyView.loadHistory(lCollectionName, lRecord.get(lDef.pk_attr));
					});
				}
			},
			'i_list': {
				itemdblclick: function(){
					var lTab = this.getWorkspace().getActiveTab();
					var lBtn = lTab.down('button[iconCls=i_edit]');
					lBtn.fireEvent('click', lBtn);
				},
				selectionchange: function( aModel ){
					if ( aModel.getCount() > 1){
						if (self.historyView && !self.historyView.isHidden()){
							self.historyView.down('pagingtoolbar').enable();
							var lHistoryBtn = self.getWorkspace().getActiveTab()
							.down('toolbar')
							.down('button[iconCls=history]');
							
							lHistoryBtn.fireEvent('click', lHistoryBtn);
						}
					}
				}
			},
			'i_toolbar > button[iconCls=i_remove]': {
				click: function( aButton ){
					var lTab = aButton.findParentByType('tabpanel').getActiveTab();
					var lCollectionName = lTab.getId()
					var lRecord = self.getActiveItem(lTab);
					
					self.checkAuthorization(lCollectionName, function(){
						Ext.Msg.show({
							title:'Confirm?',
							msg: 'Are you sure to remove the selected item?',
							buttons: Ext.Msg.YESNO,
							icon: Ext.Msg.QUESTION,
							fn: function ( aButton ){
								if ('yes' != aButton)
									return;
							
								var lArguments = {
									collectionName: lCollectionName,
									itemPK: lRecord.get('id')
								}
					
								Ext.jws.send(jws.ItemStoragePlugIn.NS, 'removeItem', lArguments);
							} 
						});
					});
				}
			}
		});
	}
});
