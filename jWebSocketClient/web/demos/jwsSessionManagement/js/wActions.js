$.widget("jws.actions",{
	_init:function(){
		w.actions=this;
        
		//--------------- BUTTONS --------------------------------
		w.actions.eBtnPut = w.actions.element.find("#put_btn");
		w.actions.eBtnGet = w.actions.element.find("#get_btn");
		w.actions.eBtnHas = w.actions.element.find("#has_btn");
		w.actions.eBtnKeys = w.actions.element.find("#keys_btn");
		w.actions.eBtnRemove = w.actions.element.find("#remove_btn");
		w.actions.eBtnGetAll = w.actions.element.find("#getall_btn");
		w.actions.eBtnGetMany = w.actions.element.find("#getmany_btn");
		
		w.actions.registerEvents();
	},
    
	registerEvents: function(){
		//registering events
		w.actions.eBtnPut.click(w.actions.put);
		w.actions.eBtnGet.click(w.actions.get);
		w.actions.eBtnHas.click(w.actions.has);
		w.actions.eBtnKeys.click(w.actions.keys);
		w.actions.eBtnRemove.click(w.actions.remove);
		w.actions.eBtnGetAll.click(w.actions.getAll);
		w.actions.eBtnGetMany.click(w.actions.getMany);
	},
	
	put: function(){
		var lKey = $("#put_key").attr("value");
		var lValue = $("#put_value").attr("value");
		var lPublic = ($("#put_public").attr("checked")) ? true : false;
		
		mWSC.sessionPut(lKey, lValue, lPublic);
	},
	get: function(){
		var lClientId = $("#get_client").attr("value");
		var lKey = $("#get_key").attr("value");
		var lPublic = ($("#get_public").attr("checked")) ? true : false;
		
		mWSC.sessionGet(lClientId, lKey, lPublic);
	},
	has: function(){
		var lClientId = $("#has_client").attr("value");
		var lKey = $("#has_key").attr("value");
		var lPublic = ($("#has_public").attr("checked")) ? true : false;
		
		mWSC.sessionHas(lClientId, lKey, lPublic);
	},
	keys: function(){
		var lClientId = $("#keys_client").attr("value");
		var lPublic = ($("#keys_public").attr("checked")) ? true : false;
		
		mWSC.sessionKeys(lClientId, lPublic);
	},
	remove: function(){
		var lKey = $("#remove_key").attr("value");
		var lPublic = ($("#remove_public").attr("checked")) ? true : false;
		
		mWSC.sessionRemove(lKey, lPublic);
	},
	getAll: function(){
		var lClientId = $("#getall_client").attr("value");
		var lPublic = ($("#getall_public").attr("checked")) ? true : false;
		
		mWSC.sessionGetAll(lClientId, lPublic);
	},
	getMany: function(){
		var lClients = $("#getmany_client").attr("value").replace(" ", "").split(",");
		var lKeys = $("#getmany_key").attr("value").replace(" ", "").split(",");
		
		mWSC.sessionGetMany(lClients, lKeys);
	}
});
