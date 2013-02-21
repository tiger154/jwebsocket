//	****************************************************************************
//	jWebSocket Hello World ( uses jWebSocket Client and Server )
//	( C ) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or ( at your
//	option ) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	****************************************************************************

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