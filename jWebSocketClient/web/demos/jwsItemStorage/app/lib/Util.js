Ext.define('IS.lib.Util', {
	singleton: true,
	def2tpl: {},
	
	isEE: function (){
		if ( 'undefined' == typeof jws.ItemStoragePlugIn.registerItemDefinition ){
			Ext.Msg.show({
				msg: 'Feature available only for enterprise clients! <br>See http://jwebsocket.org for details.',
				buttons: Ext.Msg.OK, 
				icon: Ext.Msg.ERROR
			});
			return false;
		}
		
		return true;
	},
	
	createDetailsTpl: function (aItemDefinition){
		if (!this.def2tpl[aItemDefinition.type]){
			var lTpl = "";
			
			for (var lAttr in aItemDefinition.attr_types){
				lTpl += '<p><b>' + lAttr.substr(0,1).toUpperCase() + lAttr.substr(1)
				+ '</b>: {'+ lAttr + '}</p>';
			}
			if ('id' != aItemDefinition.pk_attr){
				lTpl += '<p><b>Id</b>: {id}</p>';
			}
			
			this.def2tpl[aItemDefinition.type] = Ext.create('Ext.XTemplate', lTpl);
		}
		
		return this.def2tpl[aItemDefinition.type];
	}, 
	
	createFormField: function ( aName, aTypeDefiniton ){
		var lPos = aTypeDefiniton.indexOf('{');
		
		var lType = (lPos > 0) ? aTypeDefiniton.substr(0, lPos): aTypeDefiniton;
		var lDefinition;
		
		try {
			lDefinition = Ext.decode(aTypeDefiniton.substr(lPos));
		} catch (lEx){
			lDefinition = {};
		}
		var lField = {
			name: aName,
			allowBlank: !lDefinition.required,
			fieldLabel: aName
		};
		
		if ('string' == lType){
			this.createStringField(lField, lDefinition);
		} else if ('boolean' == lType){
			this.createBooleanField(lField, lDefinition);
		} else {
			this.createNumberField(lField, lType, lDefinition);
		}
		
		return lField;
	},
	
	createStringField: function ( aField, aDefinition ){
		aField.xtype = 'textfield';
		if (aDefinition['multi_line']){
			aField.xtype = 'textarea';
		}
		
		if (undefined != aDefinition['max_length']){
			aField.maxLength = aDefinition['max_length'];
		}
		if (undefined != aDefinition['min_length']){
			aField.minLength = aDefinition['min_length'];
		}
		if (undefined != aDefinition['input_type']){
			aField.inputType = aDefinition['input_type'];
		}
		if (undefined != aDefinition['reg_exp']){
			aField.regex = aDefinition['reg_exp'];
		}
		if (aDefinition['mail']){
			aField.regex = this.mailRegex;
		}
		aField.value = aDefinition['default'];
	},
	mailRegex: /^([\w]+)(.[\w]+)*@([\w-]+\.){1,5}([A-Za-z]){2,4}$/,
	 
	createNumberField: function ( aField, aType, aDefinition ){
		aField.xtype = 'numberfield';
		
		if (undefined != aDefinition['max_value']){
			aField.maxValue = aDefinition['max_value'];
		}
		if (undefined != aDefinition['min_value']){
			aField.minValue = aDefinition['min_value'];
		}
		if ('double' != aType){
			aField.allowDecimals = false;
		}
	},
	
	createBooleanField: function ( aField, aDefinition ){
		aField.xtype = 'checkbox';
	},
	
	defineModel: function ( aCollectionName, aItemDefinition ){
		var lFiels = [];
		for (var lAttrName in aItemDefinition.attr_types){
			lFiels.push(lAttrName);
		}
		if (lFiels.lastIndexOf("id") < 0){
			lFiels.push("id");
		}
		
		Ext.define(aCollectionName + 'Model', {
			extend: 'Ext.data.Model',
			fields: lFiels,
			idProperty: aItemDefinition.pk_attr
		});
	},
	
	createDynamicStore: function(aCollectionName, aApp){
		var lStore = Ext.create('Ext.data.Store', {
			model: aCollectionName + 'Model',
			autoLoad: true,
			pageSize: 18,
			proxy: Ext.create('Ext.jws.data.Proxy', {
				type: 'jws',
				ns: jws.ItemStoragePlugIn.NS,
				api: {
					read: 'listItems'
				},
				reader: {
					type: 'jws',
					transform: function( aResponse ){
						for (var lIndex = 0; lIndex < aResponse.data.length; lIndex++){
							aResponse.data[lIndex] = aResponse.data[lIndex].attrs;
						}
					}
				},
				transform: function ( aRequest ){
					if ( 'listItems' == aRequest.type){
						if (aApp.itemSearchs[aCollectionName]){
							aRequest.type = 'findItems';
							aRequest.data.attrName = aApp.itemSearchs[aCollectionName].attr;
							aRequest.data.attrValue = aApp.itemSearchs[aCollectionName].value;
						}
						
						aRequest.data.offset = aRequest.data.start;
						aRequest.data.length = aRequest.data.limit;
				
						// setting the target collection name
						aRequest.data.collectionName = aCollectionName;
						
						delete aRequest.data.start;
						delete aRequest.data.limit;
						delete aRequest.data.page;
					}
				}
			})
		});
		
		return lStore;
	}
});

