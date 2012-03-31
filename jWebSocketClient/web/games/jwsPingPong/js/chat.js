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
 * @author armando
 */
$.widget( "jws.chat", {
	_init:function(  ) {
		
		w.chat=this;
		
		w.chat.eChat			   = w.chat.element.find( '#chat' )       
		w.chat.eScenarioChat	   = w.chat.element.find( '#scenario_chat' )       
		w.chat.eWindow			   = w.chat.element.find( "#chat_text" );
		w.chat.eArea			   = w.chat.element.find( "#messages_area" );
		w.chat.eMinimized		   = w.chat.element.find( "#minimized" )
		w.chat.eMinimizeChatText   = w.chat.element.find( "#minimize_chat_text" );
		w.chat.eBallTimeout		   = 0;
		
		w.chat.hidden			   = true;		
		w.chat.minimized		   = false;
						
		w.chat.registerEvents(  );
		w.chat.onMessage(  );
		w.chat.eWindow.hide(  );
		w.chat.eScenarioChat.hide(  );
	}, 
    
	registerEvents:function(  ) {
		w.chat.eChat.bind( 'focus',function(  ) {
			w.chat.messageOnClick(  );
		} );
		w.chat.eChat.bind( 'blur',function(  ) {
			w.chat.messageOnBlur(  );
		} );
		
		w.chat.eChat.keypress( function( aEvt ) {
			if( aEvt.charCode == 13 || aEvt.keyCode == 13 ) {
				w.chat.broadcastMessage( $( this ).val(  ) );
				$( this ).val( "" );
				if( w.chat.hidden ) {
					w.chat.eWindow.fadeIn( 150 );
					w.chat.hidden = false;
				}
				else if( w.chat.minimized ) {
					w.chat.restore(  );
				}
			}
		} );
		w.chat.eMinimizeChatText.click( function(  ) {
			w.chat.minimize(  );
		} );
	},    
	minimize: function(  ) {
		w.chat.eWindow.slideUp(  );
		var elem = $( "<div id='minimized'>Back to chat</div>" ).click( function(  ) {
			w.chat.restore(  );
		} );
		eObjArea.prepend( elem );
		w.chat.minimized = true;
	},
	restore: function(  ) {
		w.chat.eWindow.slideDown(  );
		w.chat.eMinimized.fadeOut( function(  ) {
			$( this ).remove(  );
		} );
		w.chat.minimized = false;
	},
	addText: function( text, user, username ) {
		if( user=="0" ) {
			var $O_p   = $( "<div><p class='sms'><label id='user_name_a'>"+username+": "+"</label>"+text+"</p></div>" );
		}else{
			$O_p   = $( "<div><p class='sms'><label id='user_name_b'>"+username+": "+"</label>"+text+"</p></div>" );
		}
		w.chat.eArea.append( $O_p );       
		w.chat.eArea.scrollTop( w.chat.eArea.get( 0 ).scrollHeight - w.chat.eArea.get( 0 ).clientHeight );
		w.chat.eMinimized.fadeOut( function(  ) {
			$( this ).remove(  );
		} );
	},
	broadcastMessage: function( text ) {
		var lArgs = {
			text: text
		};
		$.jws.submit( "pingpong","sms", lArgs );
	},
	onMessage: function(  ) {
		$.jws.bind( 'pingpong:sms', function( aEvt, aToken ) {
			if( w.chat.hidden ) {
				w.chat.eWindow.fadeIn( 150 );
				w.chat.hidden = false;
			}
			else if( w.chat.minimized ) {
				w.chat.restore(  );
			}
			w.chat.addText( aToken.text,aToken.user,aToken.username );
			if(  w.chat.eBallTimeout ) clearTimeout(  w.chat.eBallTimeout );
			w.chat.eBallTimeout = setTimeout( 'w.chat.minimize()', 5000 );           
		} );
        
	},
	messageOnBlur:function(  ) {		
		if( w.chat.eChat.val(  ) == "" ) {
			w.chat.eChat.val( "Type your message..." ).css( 'color', 'graytext' );
		}
			
	},
	messageOnClick: function(  ) {       
		if(  w.chat.eChat.val(  ) == "Type your message..." ) {
			w.chat.eChat.val( "" ).css( 'color', 'black' );
		} 
	}
} );
