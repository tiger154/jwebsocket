//	---------------------------------------------------------------------------
//	jWebSocket Shared Canvas Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------

/*
 * @author daimi, vbarzana
 */
$.widget( "jws.switcher", {
    
	_init:function( ) {
		this.eBoard			= this.element.find( "#paint_area" );
		this.eImage			= this.element.find( "#image_area" );
		this.eBtnImg		= this.element.find( "#tab_insert_img" );
		this.eBtnPaint		= this.element.find( "#tab_paint" );
		this.eClientStatus	= this.element.find( "#client_status" );
		
		this.eImage.hide( );
		this.eBoard.show( );
		w.switcher   = this;
		w.switcher.registerEvents( );
		w.switcher.doOpen( );
	},
	
	registerEvents: function( ) {
		w.switcher.eBtnImg.click( w.switcher.showImageArea );
		w.switcher.eBtnPaint.click( w.switcher.showPaintArea );
	},
	
	doOpen: function( ) {
		// check if WebSockets are supported by the browser
		if( jws.browserSupportsWebSockets( ) ) {
			// instaniate new TokenClient, either JSON, CSV or XML
			mWSC = new jws.jWebSocketJSONClient( { } );
			// adjust this URL to your jWebSocket server
			// try to establish connection to jWebSocket server
			mWSC.logon( jws.JWS_SERVER_URL, "guest", "guest", {
				// OnOpen callback
				OnOpen: function( aEvent ) {
					
					$.jws.setTokenClient( mWSC );
					
					$.jws.submit( NS, "register" );
					
					// start keep alive if user selected that option
					mWSC.startKeepAlive({
						interval: 30000
					});
					w.switcher.eClientStatus
					.attr( "class", "authenticated" )
					.text( "authenticated" );
				},

				// OnMessage callback
				OnMessage: function( aEvent, aToken ) {
				},

				// OnClose callback
				OnClose: function( aEvent ) {
					jws.console.log( 
						"Sorry, there is no connection with the server" );
					mWSC.stopKeepAlive( );
					w.switcher.eClientStatus
					.attr( "class", "offline" )
					.text( "disconnected" );
				}
					
			});
		} else {
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			alert( lMsg );
		}
	},

	doClose: function( ) {
		// disconnect automatically logs out!
		mWSC.stopKeepAlive( );
		var lRes = mWSC.close({
			// wait a maximum of 3 seconds for server good bye message
			timeout: 3000
		});
	},

	exitPage: function( ) {
		// this allows the server to release the current session
		// immediately w/o waiting on the timeout.
		if( mWSC ) {
			mWSC.close({
				// force immediate client side disconnect
				timeout: 0
			});
		}
		mWSC.canvasClose( CANVAS_ID );
	},
	
	showImageArea: function( ) {
		w.switcher.eBtnImg.attr( "class", "" ).addClass( "enabled" );
		w.switcher.eBtnPaint.attr( "class", "" );
		w.switcher.eBoard.hide( );
		w.switcher.eImage.show( );
	},
	
	showPaintArea: function( ) {
		w.switcher.eBtnImg.attr( "class", "" );
		w.switcher.eBtnPaint.attr( "class", "" ).addClass( "enabled" );
		w.switcher.eImage.hide( );
		w.switcher.eBoard.show( );
	}
});