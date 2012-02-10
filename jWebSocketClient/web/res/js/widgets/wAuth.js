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
$.widget("jws.auth",{
    
	_init:function(){
		w.auth   = this;
		
		// WEBSOCKET CLIENT
		mWSC = null;
		
		w.auth.eLogoffArea		= w.auth.element.find("#logoff_area");
		w.auth.eLogonArea		= w.auth.element.find("#login_area");
		w.auth.eUsername		= w.auth.element.find("#user_text");
		w.auth.ePassword		= w.auth.element.find("#user_password");
		w.auth.eClientStatus	= w.auth.element.find("#client_status");
		w.auth.eUserInfoName	= w.auth.element.find("#user_info_name");
		w.auth.eWebSocketType	= w.auth.element.find("#websocket_type");
		w.auth.eClientId		= w.auth.element.find("#client_id");
		w.auth.eLoginButton		= w.auth.element.find('#login_button');
		w.auth.eLogoffButton	= w.auth.element.find('#logoff_button');
		w.auth.eConnectButton	= w.auth.element.find('#connect_button');
		w.auth.eDisConnectButton= w.auth.element.find('#disconnect_button');
		
		w.auth.eDisConnectButton.hide();
		w.auth.eLogoffArea.hide();
		
		w.auth.mUsername = null;
		
		w.auth.checkWebSocketSupport();
		
		w.auth.registerEvents();
	},
	checkWebSocketSupport: function(){
		if( jws.browserSupportsWebSockets() ) {
			mWSC = new jws.jWebSocketJSONClient();
		} else {
			//disable all buttons
			//        $( "#login_button" ).attr( "disabled", "disabled" );
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			alert( lMsg );
			log( lMsg );
		}
	},    
	registerEvents: function(){
		//adding click functions
		w.auth.eLoginButton.click(w.auth.logon);
		w.auth.eLogoffButton.click(w.auth.disconnect);
		
		w.auth.eConnectButton.click(w.auth.connect);
		w.auth.eDisConnectButton.click(w.auth.disconnect);
	},
	logon: function(){
		var lURL = (w.auth.options.lURL)?w.auth.options.lURL:jws.getDefaultServerURL();
        
		var lUsername = w.auth.eUsername.val();
		var lPassword = w.auth.ePassword.val();
		if(lUsername == "" || lPassword == ""){
			dialog("Incorrect Data", "User or password are not correct, please check");
			return;
		}
		// optionally reset the password to force
		// re-typing after a disconnect or logout.
		// w.auth.ePassword.value = "";
        
		if(mLog.isDebugEnabled)
			log( "Connecting to " + lURL + " and logging in as '" + lUsername + "'..." );
        
		var lRes = mWSC.logon( lURL, lUsername, lPassword, {

			// OnOpen callback
			OnOpen: function( aEvent ) {
				if(w.auth.options.OnOpen){
					w.auth.options.OnOpen(aEvent);
				}
				if(mLog.isDebugEnabled)
					log( "<font style='color:#888'>jWebSocket connection established.</font>" );
            
				//update statusbar client status
				w.auth.eLogonArea.hide();
				w.auth.eLogoffArea.fadeIn(200);
				w.auth.eClientStatus.hide().removeClass("offline").addClass("online").text("connected").show();
			},
					
			OnWelcome: function( aEvent )  {
				if(w.auth.options.OnWelcome){
					w.auth.options.OnWelcome(aEvent);
				}
				
				if(mLog.isDebugEnabled) {
					log( "<font style='color:red'>jWebSocket Welcome received.</font>" );
				}
			},

			OnGoodBye: function( aEvent ) {
				if (w.auth.options.OnGoodBye) {
					w.auth.options.OnGoodBye(aEvent);
				}
				if (mLog.isDebugEnabled) {
					log("<font style='color:red'>jWebSocket GoodBye received.</font>");
				}
			},

			// OnMessage callback
			OnMessage: function( aEvent, aToken ) {
				console.log("Logging on");
				var lDate = "";
				if( aToken.date_val ) {
					lDate = jws.tools.ISO2Date( aToken.date_val );
				}
				log( "<font style='color:#888'>jWebSocket '" + aToken.type + "' token received, full message: '" + aEvent.data + "' " + lDate + "</font>" );
				if( mWSC.isLoggedIn() ) {
					w.auth.eLogonArea.hide();
					w.auth.eLogoffArea.fadeIn(300);
					
					w.auth.eUserInfoName.text(aToken.username);
					w.auth.mUsername = aToken.username;
					w.auth.eClientId.text("Client-ID: " + ( mWSC.getId()));
					w.auth.eClientStatus.hide().removeClass("offline").removeClass("online").addClass("authenticated").text("authenticated").show();
				} else {
					w.auth.eUserInfoName.text("-");
					w.auth.eClientId.text("Client-ID: -");
					w.auth.eClientStatus.hide().removeClass("authenticated").removeClass("online").addClass("offline").text("disconnected").show();
				}
				
				w.auth.eWebSocketType.text("WebSocket: " + (jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)" ));
				
				if(w.auth.options.OnMessage) {
					w.auth.options.OnMessage(aEvent, aToken);
				}
			},
			// OnClose callback
			OnClose: function( aEvent ) {
				if(mLog.isDebugEnabled)
					log( "<font style='color:#888'>jWebSocket connection closed.</font>" );
				
				w.auth.eLogoffArea.hide();
				w.auth.eLogonArea.fadeIn(200);
				
				w.auth.mUsername = null;
				w.auth.eUserInfoName.text("");
				w.auth.eClientId.text("Client-ID: -");
				w.auth.eWebSocketType.text( "WebSocket: - " );
				w.auth.eClientStatus.removeClass("online").removeClass("online").addClass("offline").text("disconnected");
				w.auth.eUsername.focus();
				
				if(w.auth.options.OnClose){
					w.auth.options.OnClose(aEvent);
				}
			}
		});
		if(mLog.isDebugEnabled)
			log( mWSC.resultToString( lRes ) );
	}, 
	logoff: function(){
		if( mWSC ) {
			if(mLog.isDebugEnabled)
				log( "Logging off " + ( w.auth.mUsername != null ? "'" + w.auth.mUsername + "'" : "" ) + " and disconnecting..." );
			// the timeout below  is optional,
			// if you use it you'll get a good-bye message.
			var lRes = mWSC.logout({
				timeout: 3000
			});
			
			if(mLog.isDebugEnabled)
				log( mWSC.resultToString( lRes ) );
		}
        
	}, 
	connect: function() {
		var lURL = (w.auth.options.lURL)?w.auth.options.lURL:jws.getDefaultServerURL();
		
		log( "Connecting to " + lURL + " ..." );
		
		if( mWSC.isConnected()) {
			log( "Already connected." );
			return;
		}

		try {
			mWSC.open( lURL, {

				subProtocol: jws.WS_SUBPROT_JSON,

				OnOpen: function( aEvent ) {
					if(w.auth.options.OnOpen){
						w.auth.options.OnOpen(aEvent);
					}
					
					w.auth.eConnectButton.hide();
					w.auth.eDisConnectButton.show();
					
					w.auth.eClientStatus.hide().removeClass("offline").removeClass("authenticated").addClass("online").text("online").show();
					log( "jWebSocket connection established." );
				},

				OnWelcome: function( aEvent )  {
					if( mWSC.isLoggedIn() ) {
						w.auth.eUserInfoName.text(aToken.username);
						w.auth.mUsername = aToken.username;
						w.auth.eClientStatus.hide().removeClass("offline").removeClass("online").addClass("authenticated").text("authenticated").show();
					}
					log( "<font style=\"color:red\">jWebSocket Welcome received.</font>" );
					w.auth.eClientId.text("Client-ID: " + ( mWSC.getId()));
					w.auth.eWebSocketType.text("WebSocket: " + (jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)" ));
					
					if(w.auth.options.OnWelcome){
						w.auth.options.OnWelcome(aEvent);
					}
				},

				OnGoodBye: function( aEvent )  {
					if(w.auth.options.OnGoodBye){
						w.auth.options.OnGoodBye(aEvent);
					}
					log( "<font style=\"color:red\">jWebSocket GoodBye received.</font>" );
				},

				OnMessage: function( aEvent, aToken ) {
					log( "jWebSocket message received: '" + aEvent.data + "'" );
					
					if(w.auth.options.OnMessage){
						w.auth.options.OnMessage(aEvent);
					}
				},

				OnClose: function( aEvent ) {
					if(w.auth.options.OnClose){
						w.auth.options.OnClose(aEvent);
					}
					w.auth.eDisConnectButton.hide();
					w.auth.eConnectButton.show();
					
					log( "jWebSocket connection closed." );
					
					w.auth.eUserInfoName.text("");
					w.auth.mUsername = null;
					w.auth.eClientId.text("Client-ID: -");
					w.auth.eWebSocketType.text( "WebSocket: - " );
					w.auth.eClientStatus.removeClass("online").removeClass("online").addClass("offline").text("disconnected");
					w.auth.eUsername.focus();
				}
			});
		} catch( ex ) {
			console.log( ex );
			log( "Exception: " + ex.message );
		}
	}, 
	disconnect: function() {
		if( mWSC ) {
			log( "Disconnecting..." );
			try {
				var lRes = mWSC.close({
					timeout: 3000
				});
				
				if( lRes.code == 0 ) {
				} else {
					log( lRes.msg );
				}
			} catch( ex ) {
				log( "Exception: " + ex.message );
			}
		}
	}
});