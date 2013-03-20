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
 * @author vbarzana
 */
$.widget( "jws.streaming", {
	_init:function( ) {
		// DOM Elements
		this.eBtnRegister	= this.element.find( "#register_btn" );
		this.eBtnUnregister	= this.element.find( "#unregister_btn" );
		this.eChbKeepAlive	= this.element.find( "#schkKeepAlive" );
		
		w.streaming = this;
		w.streaming.doWebSocketConnection( );
		w.streaming.registerEvents( );
	},
	
	doWebSocketConnection: function( ) {
		// Each widget uses the same authentication mechanism, please refer
		// to the public widget ../../res/js/widgets/wAuth.js
		var lCallbacks = {
			OnOpen: function( aEvent ) {
			},
			OnClose: function( aEvent ) {
				
			},
			OnMessage: function( aEvent ) {
				if( mLog.isDebugEnabled ) {
					log( "<font style='color:#888'>jWebSocket message received: '" + 
						aEvent.data + "'</font>" );
				}
			},
			OnWelcome: function( aEvent ) {
			},
			OnGoodBye: function( aEvent ) {
			}
		};
		// this widget will be accessible from the global variable w.auth
		$( "#demo_box" ).auth( lCallbacks );
	},
	
	registerEvents: function( ) {
		//BUTTON EVENTS
		w.streaming.eChbKeepAlive.click( w.streaming.toogleKeepAlive );
		w.streaming.eBtnUnregister.click( w.streaming.unregisterStream );
		w.streaming.eBtnRegister.click( w.streaming.registerStream );
	},
	
	registerStream: function( ) {
		if( mWSC.isConnected( ) ) {
			var lStream = w.streaming.element
			.find( "input[name=streaming]:checked" ).val( ); // "timeStream";
			log( "Registering at stream '" + lStream + "'..." );
			var lRes = mWSC.streaming.registerStream( lStream );
			log( mWSC.resultToString( lRes ) );
		}
		else{
			jwsDialog( "Sorry, you are not connected to the server, you can't" + 
				" execute this action", "jWebSocket Error", true, "error" );
		}
	},
	
	unregisterStream: function( ) {
		if( mWSC.isConnected( ) ) {
			var lStream = w.streaming.element
			.find( "input[name=streaming]:checked" ).val( ); // "timeStream";
			log( "Unregistering from stream '" + lStream + "'..." );
			var lRes = mWSC.streaming.unregisterStream( lStream );
			log( mWSC.resultToString( lRes ) );
		}
		else{
			jwsDialog( "Sorry, you are not connected to the server, you can't" + 
				" execute this action", "jWebSocket Error", true, "error" );
		}
	},
	
	toogleKeepAlive: function( ) {
		if( w.streaming.eChbKeepAlive.get( 0 ).checked ) {
			mWSC.startKeepAlive({
				interval: 30000
			});
		} else {
			mWSC.stopKeepAlive( );
		}
	}
});