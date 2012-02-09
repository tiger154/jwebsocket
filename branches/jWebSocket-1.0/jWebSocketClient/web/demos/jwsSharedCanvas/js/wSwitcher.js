//	****************************************************************************
//	jWebSocket Hello World (uses jWebSocket Client and Server)
//	(C) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	****************************************************************************

/*
 * @author daimi
 */
$.widget("jws.switcher",{
    
	_init:function(){
		switcher   = this;
		switcher.eBoard		= switcher.element.find("#paint_area");
		switcher.eImage		= switcher.element.find("#image_area");
		
		switcher.eBtnImg		= switcher.element.find("#tab_insert_img");
		switcher.eBtnPaint	= switcher.element.find("#tab_paint");
		switcher.eClientStatus= switcher.element.find("#client_status");
		
		switcher.eImage.hide();
		switcher.eBoard.show();
		
		switcher.registerEvents();
		switcher.doOpen();
	},
	
	registerEvents: function(){
		switcher.eBtnImg.click(switcher.showImageArea);
		switcher.eBtnPaint.click(switcher.showPaintArea);
	},
	
	doOpen: function() {
		// check if WebSockets are supported by the browser
		if( jws.browserSupportsWebSockets() ) {
			// instaniate new TokenClient, either JSON, CSV or XML
			mWSC = new jws.jWebSocketJSONClient({ });
			// adjust this URL to your jWebSocket server
			var lURL = jws.getDefaultServerURL();// + ( frameElement.id ? ";unid=" + frameElement.id : "");

			// try to establish connection to jWebSocket server
			mWSC.logon( lURL, "Guest", "guest", {
				// OnOpen callback
				OnOpen: function( aEvent ) {
					
					$.jws.setTokenClient(mWSC);
					
					$.jws.submit(NS, "register");
					
					// start keep alive if user selected that option
					mWSC.startKeepAlive({
						interval: 30000
					});
					switcher.eClientStatus.attr("class", "").addClass("authenticated").text("authenticated");
					mIsConnected = true;
				},

				// OnMessage callback
				OnMessage: function( aEvent, aToken ) {
//					console.log(aToken);
				},

				// OnClose callback
				OnClose: function( aEvent ) {
					console.log("disconnected");
					mIsConnected = false;
					mWSC.stopKeepAlive();
					switcher.eClientStatus.attr("class", "").addClass("offline").text("disconnected");
				}
					
			});
		} else {
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			alert( lMsg );
		}
	},

	doClose: function() {
		// disconnect automatically logs out!
		mWSC.stopKeepAlive();
		var lRes = mWSC.close({
			// wait a maximum of 3 seconds for server good bye message
			timeout: 3000
		});
	},

	exitPage: function() {
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
	
	showImageArea: function(){
		switcher.eBtnImg.attr("class", "").addClass("enabled");
		switcher.eBtnPaint.attr("class", "");;
		
		switcher.eBoard.hide();
		switcher.eImage.show();
	},
	
	showPaintArea: function(){
		switcher.eBtnImg.attr("class", "");
		switcher.eBtnPaint.attr("class", "").addClass("enabled");
		switcher.eImage.hide();
		switcher.eBoard.show();
	}
});