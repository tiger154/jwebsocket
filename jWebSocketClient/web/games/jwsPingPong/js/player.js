//	****************************************************************************
//	jWebSocket Hello World ( uses jWebSocket Client and Server )
//	( C ) 2010 Alexander Schulze,  jWebSocket.org,  Innotrade GmbH,  Herzogenrath
//	****************************************************************************
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License,  or ( at your
//	option ) any later version.
//	This program is distributed in the hope that it will be useful,  but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not,  see <http: //www.gnu.org/licenses/lgpl.html>.
//	****************************************************************************

/* 
 * @author armando
 */
$.widget( "jws.player",  {
        
	_init: function(  ) {
		w.player			    = this;
       
		w.player.eAracket		= w.player.element.find( '#playerA_racket' );
		w.player.eBracket		= w.player.element.find( '#playerB_racket' );
		w.player.eAname		    = w.player.element.find( '#playerA_name' );
		w.player.eBname		    = w.player.element.find( '#playerB_name' );
		w.player.eApoints		= w.player.element.find( '#playerA_points' );
		w.player.eBpoints		= w.player.element.find( '#playerB_points' );
		
		//	w.player.registerEvents(  );
		w.player.onMessage(  );
	}, 
	
	//Sends the event that has been moved,  the number and if was the keyboard 
	//or the mouse to variate the speed
	broadcastPlayer: function( aKeyNumber,  aIdentifier ) {
        
		var lArgs = {
			e: parseInt( aKeyNumber ), 
			v: aIdentifier
		};            
		$.jws.submit( 'pingpong', 'moveplayer',  lArgs );
	}, 
	
	//all incoming messages from the server
	onMessage: function(  ) {
		//Initializing rackets of both players
		$.jws.bind( 'pingpong:submitrequest',  function( aEvt,  aToken ) {
			w.player.initPlayer( aToken.player, aToken.width, aToken.Heigth, aToken.posX, aToken.posY );          
		} ); 
		//To update the scores
		$.jws.bind( 'pingpong:score',  function( aEvt,  aToken ) {
			w.player.scoreUpdate( aToken.username1, aToken.score1, aToken.username2, aToken.score2 );          
		} );
	}, 
	initPlayer: function( aPlayer, aWidth, aHeight, aPosX, aPosY ) {
		if( aPlayer == "playLeft" ) {
			w.player.eAracket.css( { 
				'top': aPosY+'px', 
				'left': aPosX+'px'
			} );
		}else if( aPlayer == "playRight" ) {
			w.player.eBracket.css( { 
				'width': aWidth+'px',  
				'height': aHeight+'px', 
				'top': aPosY +'px', 
				'left': aPosX+'px'        
			} );
		}else{
            
			w.player.eAracket.css( { 
				'top': aPosY+'px', 
				'left': aPosX+'px'
        
			} );
			w.player.eAracket.css( { 
				'width': aWidth+'px',  
				'height': aHeight+'px', 
				'top': aPosY +'px', 
				'left': aPosX+'px'        
			} );
		}
	}, 
	registerEvents: function(  ) {
		$( 'html' ).bind( {
			'keydown': function( e ) {
				if( e.keyCode == 38 ||e.keyCode == 40 ) {                           
					w.player.broadcastPlayer( e.keyCode,  "k" ); 
					e.stopPropagation(  );
					e.preventDefault(  );
				}
			}, 
			'mousewheel DOMMouseScroll': function(  aEvt  ) {
				if( (  aEvt.originalEvent.wheelDelta > 0 || aEvt.originalEvent.detail < 0 ) ) {                
					w.player.broadcastPlayer(  38,  "m"  ); 
				} else {
					w.player.broadcastPlayer( 40, "m" ); 
				}
				aEvt.stopPropagation(   );
				aEvt.preventDefault(  );
			}            
		} );  
	}, 
	decouplingEvent: function(  ) { 
		$( 'html' ).unbind( 'keydown' );
		$( 'html' ).unbind( 'mousewheel' );       
	}, 
	scoreUpdate: function( aUsername1, aScore1, aUsername2, aScore2 ) {
		w.player.eAname.text( aUsername1 ); 
		w.player.eBname.text( aUsername2 );
		w.player.eApoints.text( aScore1 );
		w.player.eBpoints.text( aScore2 );
	}    
} );