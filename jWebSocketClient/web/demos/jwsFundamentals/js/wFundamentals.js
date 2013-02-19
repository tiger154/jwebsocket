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
		w.fund = this;
		// DOM Elements
		this.eMessageBox		= this.element.find( "#message_box_text" );
		this.eBtnEcho			= this.element.find( "#echo_btn" );
		this.eBtnThread			= this.element.find( "#thread_btn" );
		this.eBtnConectivity	= this.element.find( "#connectivity_btn" );
		this.eBtnAuth			= this.element.find( "#auth_btn" );
		this.eBtnDeauth			= this.element.find( "#deauth_btn" );
		this.eBtnGetAuth		= this.element.find( "#get_auth_btn" );
		this.eCbAutoReconn		= this.element.find( "#auto_reconnect" );
		this.eDemoBox			= $( "#demo_box" );
		
		this.mArgumentsOfThread	= [ "This was the passed argument" ];
		
		// Messages to be used
		this.MSG_TypeYourMessage= "Type your message...";
		this.MSG_StartingThread = "Starting method as thread..."
		this.MSG_DemoTitle		= "Fundamentals Demo";
		this.MSG_TypeSthg		= "Please you must type something in the field";
		this.MSG_WebWorker		= "This method was called in a WebWorker " + 
		"thread and returned: ";
	
		//CSS classes
		this.CSS_DARK			= "dark";
		this.CSS_OPAQUE			= "opaque";
		
		this.doWebSocketConnection( );
		this.registerEvents( );
	},
	
	doWebSocketConnection: function( ) {
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
					lDate = new Date( );
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
		w.fund.eDemoBox.auth( lCallbacks );
	},
	
	registerEvents: function( ) {
		//MESSAGE BOX EVENTS
		w.fund.eMessageBox.click( w.fund.messageBoxClick );
		w.fund.eMessageBox.blur( w.fund.messageBoxBlur );
		w.fund.eMessageBox.keypress( w.fund.messageBoxKeyPressed );
		w.fund.eMessageBox.focus( w.fund.messageBoxClick );
		
		w.fund.eCbAutoReconn.change( w.fund.toggleReconnect );
		w.fund.eBtnThread.click( w.fund.thread );
		w.fund.eBtnEcho.click( w.fund.echo );
		w.fund.eBtnConectivity.click( w.fund.showReliabilityOptions );
		w.fund.eBtnAuth.click( w.auth.auth );
		w.fund.eBtnDeauth.click( w.auth.deauth );
		w.fund.eBtnGetAuth.click( w.auth.getAuth );
		
	},

	toggleReconnect: function( ) {
		if( mWSC ) {
			var lReconnect = w.fund.eCbAutoReconn.get(0).checked;
			if ( mLog.isDebugEnabled ) {
				log( "Turning auto-reconnect " + ( lReconnect ? "on" : "off" ) );
			}
			mWSC.setReliabilityOptions( lReconnect ? jws.RO_ON : jws.RO_OFF );
		}
	},
			
	showReliabilityOptions: function( ) {
		if( mWSC ) {
			var lOptions = mWSC.getReliabilityOptions( );
			var lQueue = mWSC.getOutQueue( );
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

	echo: function( ) {
		var lMsg = w.fund.eMessageBox.val( );
		if( lMsg && lMsg != w.fund.MSG_TypeYourMessage ) {
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
		} else {
			dialog( w.fund.MSG_TypeSthg, w.fund.MSG_DemoTitle, true, 
				function( ) {
					w.fund.eMessageBox.focus( );
				});
			
		}
	},

	thread: function( ) {
		if ( mLog.isDebugEnabled ) {
			log( w.fund.MSG_StartingThread );
		}
		var lRes = jws.runAsThread({
			method: function( aOut ) {
				return( w.fund.MSG_WebWorker + aOut );
			},
			args: w.fund.mArgumentsOfThread,
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
		if( $( this ).val( ) == "" ) {
			$( this ).val( w.fund.MSG_TypeYourMessage ).attr( "class", 
				w.fund.CSS_OPAQUE );
		}
	},
	
	messageBoxClick: function( ) {
		if( $( this ).val( ) == w.fund.MSG_TypeYourMessage ) {
			$( this ).val( "" ).attr( "class", w.fund.CSS_DARK );
		}
	},
	
	messageBoxKeyPressed: function( aEvt ) {
		if( aEvt.keyCode == 13 && ( !aEvt.shiftKey ) ) {
			aEvt.preventDefault( );
			w.fund.echo( );
			$( this ).val( "" );
		}
	}
});