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
$.widget( "jws.connected", {
        
	_init: function(  ) {
		
		w.conn					= this;
		 	
		w.conn.ePlayerslist		= w.conn.element.find( '#players_list' );
		w.conn.ePopupOk			= w.conn.element.find( '#popup_ok' );
		
		w.conn.onMessage(  );
		
	},
	onMessage: function(  ) {
		//crear la lista de los que estan conectados
		$.jws.bind( 'pingpong:usser', function( aEvt, aToken ) {
			w.auth.eClientStatus.hide(  ).attr( "class", "" )
			.addClass( "authenticated" ).text( "authenticated" ).show( );
			
			w.conn.ePlayerslist.html( "" );
			for ( i = 0; i < aToken.available.length; i++ ) {
				w.conn.initConnected( aToken.available[i], aToken.state );
			}
            
			for ( i = 0; i < aToken.playing.length; i++ ) {
				w.conn.initPlaying( aToken.playing[i] );
			}
			if( aToken.available && aToken.playing ) {
				if( aToken.available.length == 0 && ( aToken.playing.length == 0 ) ) {
					w.conn.element.append( $( "<div id='no_users'>No users online, please wait for someone!</div>" ) );
				}
				else{
					w.conn.element.find( "#no_users" ).remove(  );
				}
			}
            
		} );
		//recive quien te manda la solicitud de juego
		$.jws.bind( 'pingpong:sendrequest', function( ev, aToken ) {	
			var lButtons = [{
				id: "buttonNo",
				text: "No",
				aFunction: function(  ) {
					w.conn.has_accepted_request( false,aToken.username );
				}
			}, {
				id: "buttonYes",
				text: "Yes",
				aFunction: function(  ) {
					w.conn.has_accepted_request( true, aToken.username );
				}
			}];
			dialog( "Ping Pong Game", "<b>" + aToken.username + "</b>"+' wants to play Ping Pong with you. Would you like to proceed?',true, function(  ) {}, lButtons );
		} );
		$.jws.bind( 'pingpong:deleteusser', function( ev, aToken ) {
			w.conn.ePlayerslist.html( "" );              
		} );
		//recive la respuesta donde dice que no asecta la solicitud		
		$.jws.bind( 'pingpong:submitsequestno', function( ev, aToken ) {
			dialog( "Ping Pong Game", aToken.username+' does not want to start a game',true );
		} );
        
	},
	initConnected: function(  text, state  ) {
		if( !state ) {
			var $text;
			var $O_p   =$(  '<li class="available">'  ).text( text ).click( function(  ) { 
				$text=$( this ).text(  );
				w.conn.send_request( $text );  
				var lButtons = [{
					id: "buttonCancel",
					text: "Cancel",
					aFunction: function(  ) {
						w.conn.has_accepted_request( false,$text );
					}
				}];
				dialog( "Ping Pong Game", ' Waiting for request confirmation',true, function(  ) { }, lButtons );
			} );
		}else{
			$O_p   = $( '<li class="available">' ).text( text );
		}
		w.conn.ePlayerslist.append( $O_p );
	},
	initPlaying: function( text ) {
		var $O_p   = $( '<li class="playing">' ).text( text );
		w.conn.ePlayerslist.append( $O_p );
	},
	//envio la solicitud para jugar
	send_request: function ( username ) {
		var args = {
			username:username
		};
		$.jws.submit( 'pingpong', 'sendrequest', args );
	},//ha aceptado o no la solicitud
	has_accepted_request: function( accepted, username ) {
              
		var args = {
			username: username,
			accepted: accepted
		};
		$.jws.submit( 'pingpong', 'submitsequest', args );        
	}        
} );