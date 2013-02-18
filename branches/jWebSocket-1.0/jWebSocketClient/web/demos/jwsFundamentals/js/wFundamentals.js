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
 * @author vbarzana, ashulze
 */
$.widget( "jws.fundamentals", {
	_init:function( ) {
		w.fundamentals = this;
		// DOM Elements
		this.eMessageBox		= this.element.find( "#message_box_text" );
		this.eBtnEcho			= this.element.find( "#echo_btn" );
		this.eBtnThread			= this.element.find( "#thread_btn" );
		this.eBtnConectivity	= this.element.find( "#connectivity_btn" );
		this.eBtnAuth			= this.element.find( "#auth_btn" );
		this.eBtnDeauth			= this.element.find( "#deauth_btn" );
		this.eBtnGetAuth		= this.element.find( "#get_auth_btn" );
		this.eCbAutoReconn		= this.element.find( "#auto_reconnect" );
		
		this.doWebSocketConnection( );
		this.registerEvents( );
	},
	
	doWebSocketConnection: function(){
		// Each widget utilizes the same authentication mechanism, please refer
		// to the public widget ../../res/js/widgets/wAuth.js
		var lCallbacks = {
			OnOpen: function( aEvent ) {
			},
			OnClose: function( aEvent ) {
			},
			OnMessage: function( aEvent, aToken ) {
				var lDate;
				if( aToken.date_val ) {
					lDate = jws.tools.ISO2Date( aToken.date_val );
				} else {
					lDate = new Date();
				}
				
				if( mLog.isDebugEnabled ) {
					log( "<font style='color:#888'>jWebSocket '" + aToken.type 
						+ "' token received, full message: '" + aEvent.data + "' " 
						+ lDate + "</font>" );
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
		//MESSAGE BOX EVENTS
		w.fundamentals.eMessageBox.click( w.fundamentals.messageBoxClick );
		w.fundamentals.eMessageBox.blur( w.fundamentals.messageBoxBlur );
		w.fundamentals.eMessageBox.keypress( w.fundamentals.messageBoxKeyPressed );
		w.fundamentals.eMessageBox.focus( w.fundamentals.messageBoxClick );
		
		w.fundamentals.eCbAutoReconn.change( w.fundamentals.toggleReconnect );
		w.fundamentals.eBtnThread.click( w.fundamentals.thread );
		w.fundamentals.eBtnEcho.click( w.fundamentals.echo );
		w.fundamentals.eBtnConectivity.click( w.fundamentals.showReliabilityOptions );
		w.fundamentals.eBtnAuth.click( w.auth.auth );
		w.fundamentals.eBtnDeauth.click( w.auth.deauth );
		w.fundamentals.eBtnGetAuth.click( w.auth.getAuth );
		
	},

	toggleReconnect: function() {
		if( mWSC ) {
			var lReconnect = w.fundamentals.eCbAutoReconn.get(0).checked;
			if ( mLog.isDebugEnabled ) {
				log( "Turning auto-reconnect " + ( lReconnect ? "on" : "off" ) );
			}
			mWSC.setReliabilityOptions( lReconnect ? jws.RO_ON : jws.RO_OFF );
		}
	},
			
	showReliabilityOptions: function() {
		if( mWSC ) {
			var lOptions = mWSC.getReliabilityOptions();
			var lQueue = mWSC.getOutQueue();
			if ( mLog.isDebugEnabled ) {
				log( "Reliability Options: " 
					+ ( lQueue ? lQueue.length : "no" ) + " items in queue"
					+ ", auto-reconnect: " + lOptions.autoReconnect
					+ ", reconnectDelay: " + lOptions.reconnectDelay
					+ ", queueItemLimit: " + lOptions.queueItemLimit
					+ ", queueSizeLimit: " + lOptions.queueSizeLimit
					);
			}
		}
	},

	echo: function() {
		var lMsg = w.fundamentals.eMessageBox.val();
		if ( mLog.isDebugEnabled ) {
			log( "Sending '" + lMsg + "', waiting for echo..." );
		}
		try {
			var lRes = mWSC.echo( lMsg );
			if( lRes.code == 0 ) {
				if ( mLog.isDebugEnabled ) {
					log( "Message sent." );
				}
			} else {
				if ( mLog.isDebugEnabled ) {
					log( lRes.msg );
				}
			}
		} catch( ex ) {
			console.log( ex.message );
			if ( mLog.isDebugEnabled ) {
				log( "Exception: " + ex.message );
			}
		}
	},

	thread: function() {
		if ( mLog.isDebugEnabled ) {
			log( "Starting method as thread..." );
		}
		var lRes = jws.runAsThread({
			method: function( aOut ) {
				return( "This method was called in a WebWorker thread and returned: " + aOut );
			},
			args: [ "This was the passed argument" ],
			OnMessage: function( aToken ) {
				var lData = aToken.data;
				if ( mLog.isDebugEnabled ) {
					log( "Result: " + lData );
				}
			},
			OnError: function( aToken ) {
				if ( mLog.isDebugEnabled ) {
					log( "Error: " + aToken.message );
				}
			}
		});
		if ( mLog.isDebugEnabled ) {
			log( lRes.msg );
		}
	},
	
	// ------------- EVENTS ---------------------------
	messageBoxBlur : function( ) {
		if( $( this ).val() == "" ) {
			$( this ).val("Type your message...").attr( "class", "opaque" );
		}
	},
	
	messageBoxClick: function( ) { 
		if( $( this ).val( ) == "Type your message..." ) {
			$( this ).val( "" ).attr( "class", "dark" );
		}
	},
	
	messageBoxKeyPressed: function( aEvt ) {
		if( aEvt.keyCode == 13 && ( !aEvt.shiftKey ) ) {
			aEvt.preventDefault( );
			w.fundamentals.echo( );
			$( this ).val( "" );
		}
	}
});