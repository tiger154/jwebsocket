
$.widget( "jws.sessionManagement",{
	_init:function( ) {
		// ------------- DOM ELEMENTS --------------------------
		this.ePutKey		= this.element.find( "#put_key" );
		this.ePutValue		= this.element.find( "#put_value" );
		this.ePutPublic		= this.element.find( "#put_public" );
		this.eGetClient		= this.element.find( "#get_client" );
		this.eGetKey		= this.element.find( "#get_key" );
		this.eGetPublic		= this.element.find( "#get_public" );
		this.eHasClient		= this.element.find( "#has_client" );
		this.eHasKey		= this.element.find( "#has_key" );
		this.eHasPublic		= this.element.find( "#has_public" );
		this.eKeysClient	= this.element.find( "#keys_client" );
		this.eKeysPublic	= this.element.find( "#keys_public" );
		this.eRemoveKey		= this.element.find( "#remove_key" );
		this.eRemovePublic	= this.element.find( "#remove_public" );
		this.eGetAllClient	= this.element.find( "#getall_client" );
		this.eGetAllPublic	= this.element.find( "#getall_public" );
		this.eGetManyClient	= this.element.find( "#getmany_client" );
		this.eGetManyKey	= this.element.find( "#getmany_key" );
		
		//--------------- BUTTONS --------------------------------
		this.eBtnPut		= this.element.find( "#put_btn" );
		this.eBtnGet		= this.element.find( "#get_btn" );
		this.eBtnHas		= this.element.find( "#has_btn" );
		this.eBtnKeys		= this.element.find( "#keys_btn" );
		this.eBtnRemove		= this.element.find( "#remove_btn" );
		this.eBtnGetAll		= this.element.find( "#getall_btn" );
		this.eBtnGetMany	= this.element.find( "#getmany_btn" );
		
		w.SM = this;
		w.SM.registerEvents( );
	},
    
	registerEvents: function( ) {
		//registering events
		w.SM.eBtnPut.click( w.SM.put );
		w.SM.eBtnGet.click( w.SM.get );
		w.SM.eBtnHas.click( w.SM.has );
		w.SM.eBtnKeys.click( w.SM.keys );
		w.SM.eBtnRemove.click( w.SM.remove );
		w.SM.eBtnGetAll.click( w.SM.getAll );
		w.SM.eBtnGetMany.click( w.SM.getMany );
	},
	
	put: function( ){
		var lKey = w.SM.ePutKey.val( ),
		lValue = w.SM.ePutValue.val( ),
		lPublic = ( w.SM.ePutPublic.attr( "checked" ) ) ? true : false;
		mWSC.sessionPut( lKey, lValue, lPublic );
	},
	get: function( ){
		var lClientId = w.SM.eGetClient.val( ),
		lKey = w.SM.eGetKey.val( ),
		lPublic = ( w.SM.eGetPublic.attr( "checked" ) ) ? true : false;
		
		mWSC.sessionGet( lClientId, lKey, lPublic );
	},
	has: function( ){
		var lClientId = w.SM.eHasClient.val( );
		var lKey = w.SM.eHasKey.val( );
		var lPublic = ( w.SM.eHasPublic.attr( "checked" ) ) ? true : false;
		
		mWSC.sessionHas( lClientId, lKey, lPublic );
	},
	keys: function( ){
		var lClientId = w.SM.eKeysClient.val( );
		var lPublic = ( w.SM.eKeysPublic.attr( "checked" ) ) ? true : false;
		
		mWSC.sessionKeys( lClientId, lPublic );
	},
	remove: function( ){
		var lKey = w.SM.eRemoveKey.val( );
		var lPublic = ( w.SM.eRemovePublic.attr( "checked" ) ) ? true : false;
		
		mWSC.sessionRemove( lKey, lPublic );
	},
	getAll: function( ){
		var lClientId = w.SM.eGetAllClient.val( );
		var lPublic = ( w.SM.eGetAllPublic.attr( "checked" ) ) ? true : false;
		
		mWSC.sessionGetAll( lClientId, lPublic );
	},
	getMany: function( ){
		var lClients = w.SM.eGetManyClient.val( ).replace( " ", "" ).split( "," );
		var lKeys = w.SM.eGetManyKey.val( ).replace( " ", "" ).split( "," );
		
		mWSC.sessionGetMany( lClients, lKeys );
	}
});
