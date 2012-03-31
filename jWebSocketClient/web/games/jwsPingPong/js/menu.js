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
$.widget( "jws.menu", {
        
	_init: function(  ) {    
		w.menu =this;
        
		w.menu.mIsPaused			= false;
		w.menu.mIsSoundActive		= true;
		
		w.menu.eSendPpause			= w.menu.element.find( '#send_pause' );
		w.menu.eSound   			= w.menu.element.find( '#sound' );
		w.menu.eHelp				= w.menu.element.find( '#help' );
		w.menu.eNewGame			    = w.menu.element.find( '#new_game' );
		w.menu.ePause				= w.menu.element.find( '#pause' );
		w.menu.eEndGame			    = w.menu.element.find( '#end_game' );
		w.menu.eScenarioMenu		= w.menu.element.find( '#scenario_menu' );
		
		w.menu.ePause.hide(  );
    	w.menu.eScenarioMenu.hide(  );
		w.menu.initMenu(  );
		w.menu.onMessage(  );
	},
	onMessage: function(  ) {
		$.jws.bind( 'pingpong:sendnewgame', function( aEvt, aToken ) {
			w.menu.sendNewGame(  );
			w.menu.eSendPpause.html( "" );
		} );
		$.jws.bind( 'pingpong:pause', function( aEvt, aToken ) {
			console.log( aToken.pause );
			if(  aToken.pause == "pause"  ) {
				w.menu.ePause.find( "#play_pause" ).attr( "class", "play" );
				w.menu.mIsPaused = true;
			} else {
				w.menu.ePause.find( "#play_pause" ).attr( "class", "pause" );
				w.menu.mIsPaused = false;
			}
			w.menu.eSendPpause.html( aToken.pause );
		} );
	},
	initMenu: function(  ) {
		w.menu.eHelp.click( w.menu.showHelp );
		
		w.menu.eNewGame.click( function(  ) {
			w.menu.newGame(  );
			w.menu.eSendPpause.html( "" );
		} );
		w.menu.ePause.click( function(  ) {
			w.menu.pause(  );
		} );
		w.menu.eEndGame.click( function(  ) {
			w.menu.endGame(  );
		} ); 
		w.menu.eSound.click( function(  ) {
			w.menu.Sound(  );
		} );
		w.menu.eSound.mouseover( function(  ) {
			if(  w.menu.mIsSoundActive  ) {
				$( this ).attr( "class", "button onmouseover" );
			}
		} );
		w.menu.eSound.mouseout( function(  ) {
			if(  w.menu.mIsSoundActive  ) {
				$( this ).attr( "class", "button onmouseout" );
			}
		} );
	},
	newGame: function(  ) {
		$.jws.submit( 'pingpong','sendnewgame' );       
	},
	pause: function(  ) {
		$.jws.submit( 'pingpong','pause' );
	},
	endGame: function(  ) {
		$.jws.submit( 'pingpong','endgame' );
	},
	Sound: function(  ) {
		$.jws.submit( 'pingpong','sound' );
		if(  w.menu.mIsSoundActive  ) {
			w.menu.mIsSoundActive = false;
			w.menu.eSound.find( "#audio_on_off" ).attr( "class", "audio_off" );
			w.menu.eSound.attr( "class", "button onmousedown" );
		} else {
			w.menu.mIsSoundActive = true;
			w.menu.eSound.find( "#audio_on_off" ).attr( "class", "audio_on" );
			w.menu.eSound.attr( "class", "button onmouseup" );
		}
	},
	sendNewGame: function(  ) {
		var lButtons = [{
			id: "buttonNo",
			text: "No",
			aFunction: function(  ) {
				var args={
					newgame: false
				}; 
				$.jws.submit( 'pingpong','newgame',args );
			}
		}, {
			id: "buttonYes",
			text: "Yes",
			aFunction: function(  ) {
				var args={
					newgame:true
				}; 
				$.jws.submit( 'pingpong','newgame',args );
			}
		}];
		dialog( "Ping Pong Game", 'You received a New Game request.<b> Would you like to proceed?</b>', true, null, lButtons );
	},
	showHelp: function(  ) {
		var aMessage = "<h1>How to play Ping Pong Game?</h1>\n\
		<h2>Moving the ball</h2>\n\
		<img src='css/images/keys.png' align='left'></img><p> To \n\
		move the ball you just need to use the arrows up and \n\
		down from your keyboard.</p>\n\
		<p> Also you can move the mouse wheel \n\
		<img src='css/images/mouse-wheel.png' align='right'></img> to up \n\
		or to down depending where you have to move and your racket will\n\
	    also move.</p>\n\
		<h2>Rules of the game</h2>\n\
		<ol>\n\
			<li>To start a game you have to click the button new game, \n\
			and then wait for the other user to accept your <b>new game request</b></li>\n\
			<li>The game is up to 10 points</li>\n\
			<li>In the left side of the screen will be shown a ranking of the best users</li>\n\
			<li>To play with other person you have to select the option <b>End Game</b></li>\n\
		</ol>\n\
		<h2>Good luck and enjoy jWebSocket!</h2>";
		
		dialog( "Ping Pong Help", aMessage, true, null, null, 500 );
	}
} );
