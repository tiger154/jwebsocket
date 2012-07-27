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
		var lClientId = mWSC.getId();
		var lKey = "myVar1";
		var lValue = "myVar1Value";
		var lPublic = true;

		mWSC.sessionPut(lKey, lValue, lPublic);
	},
	get: function(){
		var lClientId = mWSC.getId();
		var lKey = "myVar1";
		var lPublic = true;
		
		mWSC.sessionGet(lClientId, lKey, lPublic);
	},
	has: function(){
		var lClientId = mWSC.getId();
		var lKey = "myVar1";
		var lPublic = true;
		
		mWSC.sessionHas(lClientId, lKey, lPublic);
	},
	keys: function(){
		var lClientId = mWSC.getId();
		var lPublic = true;
		
		mWSC.sessionKeys(lClientId, lPublic);
	},
	remove: function(){
		var lKey = "myVar1";
		var lPublic = true;
		
		mWSC.sessionRemove(lKey, lPublic);
	},
	getAll: function(){
		var lClientId = mWSC.getId();
		var lPublic = true;
		
		mWSC.sessionGetAll(lClientId, lPublic);
	},
	getMany: function(){
		var lClients = [mWSC.getId()];
		var lKeys = ["myVar1"];
		var lPublic = true;
		
		mWSC.sessionGetMany(lClients, lKeys, lPublic);
	}
});
