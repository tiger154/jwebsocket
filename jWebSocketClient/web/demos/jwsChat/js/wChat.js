$.widget( "jws.chat", {
	_init:function( ) {
		w.chat = this;
		w.chat.NS = jws.NS_BASE;
		
		//Elements
		w.chat.eMessageBox = w.chat.element.find( "#message_box_text" );
		w.chat.ePublicUsersBox = w.chat.element.find( "#public_users_box_body" );
		w.chat.ePrivateUsersBox = w.chat.element.find( "#users_box_body" );
		
		//Buttons
		w.chat.eBtnPrivateChat = w.chat.element.find( "#btn_private_chat" );
		w.chat.eBtnPublicChat = w.chat.element.find( "#btn_public_chat" );
		w.chat.eBtnBroadcast = w.chat.element.find( "#message_box_broadcast_btn" );


		w.chat.ePublicUsersBox.hide( );
		
		w.chat.registerEvents( );
	},
    
	registerEvents: function( ) {
		//MESSAGE BOX EVENTS
		w.chat.eMessageBox.click( w.chat.messageBoxClick );
		w.chat.eMessageBox.blur( w.chat.messageBoxBlur );
		w.chat.eMessageBox.keypress( w.chat.messageBoxKeyPressed );
		w.chat.eMessageBox.focus( w.chat.messageBoxClick );
		
		//CHAT BOX SWITCHER
		w.chat.eBtnPrivateChat.click( w.chat.switchPrivate );
		w.chat.eBtnPublicChat.click( w.chat.switchPublic );
		
	},
	
	sendMessage: function( ) {
		
	},
	
	broadcast: function( ) {
		
	},
	
	processToken: function( aToken ) {
		if (  aToken.ns ){
			
		}
		if( aToken ) {
			// is it an event w/o a previous request ?
			if( aToken.type == "event" ) {
			// interpret the event name
			} else if( aToken.type == "goodBye" ) {
				log( SYS, IN, lJWSID + " says good bye (reason: " + aToken.reason + ")!" );
				doFocus( eUsername );
			// is it any token from another client
			} else if( aToken.type == "broadcast" ) {
				if( aToken.data ) {
					log( aToken.sender, IN, aToken.data );
				}
			}
		}
	},
	
	switchPrivate: function( ) {
		w.chat.ePublicUsersBox.hide();
		w.chat.ePrivateUsersBox.show();
	},
	
	switchPublic: function( ) {
		w.chat.ePrivateUsersBox.hide();
		w.chat.ePublicUsersBox.show();
	},
	
	messageBoxBlur : function( ) {
		if( $( this ).val() == "" ) {
			$( this ).val("Type your message...").attr( "class", "" ).addClass( "opaque" );;
		}
	},
	
	messageBoxClick: function( ) { 
		if( $( this ).val( ) == "Type your message..." ) {
			$( this ).val( "" ).attr( "class", "" ).addClass( "dark" );
		}
	},
	
	messageBoxKeyPressed: function( aEvt ) {
		if( aEvt.keyCode == 13 && ( !aEvt.shiftKey ) ) {
			$( this ).val( "" );
			aEvt.preventDefault();
		}
	}
});

function logPrivateMessage( aSender, aMessage, aPlace ) {
	console.log(aSender);
	console.log(aMessage);
	console.log(aPlace);
}

function logPublicMessage( aSender, aMessage, aPlace ) {
	console.log(aSender);
	console.log(aMessage);
	console.log(aPlace);
}