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
$.widget( "jws.stage", {
        
	_init:function(  ) {
		eStage			= this;
		$eStage			= this.element;         
		eObjArea		= $eStage.find( '#obj_area' );
		eGameOver		= $eStage.find( '#game_over' );
		eSendPause		= $eStage.find( '#send_pause' );
		eMessagesArea	= $eStage.find( '#messages_area' );
		eBoard			= $eStage.find( '#board' );
		eCounter		= $eStage.find( '#counter' );
		
		$.jws.submit( 'pingpong','stage' );
		
		eStage.onMessage(  );
		eObjArea.hide(  );
		eGameOver.hide(  );
                
	},
	
	onMessage: function(  ) {
		//Creating the scenario
		$.jws.bind( 'pingpong:stage', function( aEvt, aToken ) {
			eStage.initStage( aToken.width, aToken.height, aToken.gameBorder );
		} );
		$.jws.bind( 'pingpong:gameover', function( aEvt, aToken ) { 
			eStage.gameOver( aToken.gameover, aToken.message );           
		} );
		//Enable or disable the main area with all objects inside
		$.jws.bind( 'pingpong:objarea', function( aEvt, aToken ) {
			eStage.objArea( aToken.objarea );   
		} ); 
		//Enable or disable the counter
		$.jws.bind( 'pingpong:counter', function( aEvt, aToken ) {
			//show button pause because game already started
			w.menu.ePause.show(  );
			eStage.counter( aToken.counter );           
		} );
		$.jws.bind( 'pingpong:sendexit', function( aEvt, aToken ) {
			dialog( "Ping Pong Game", aToken.username + ' has left the game', true );
		} );
	},
	initStage:function( aWidth, aHeight, aGameBorder ) {
		eBoard.css( {
			'width': aWidth - aGameBorder * 2 + 'px', 
			'height': aHeight- aGameBorder * 2 + 'px'
		} );
	},
	gameOver: function( aGameOver, aMessage ) {
		if( aGameOver ) {  
			eGameOver.text( aMessage );
			eGameOver.show(  );
			w.ball.eBall.hide(  );
		}else{
			eGameOver.hide(  );
		}
	},
	objArea:function( aObjArea ) {
		if( aObjArea ) {
			eObjArea.show(  );
			w.menu.eScenarioMenu.show(  );
			w.chat.eScenarioChat.show(  );
			w.ball.eBall.hide(  );
			eCounter.hide(  );
			w.player.registerEvents(  );           
		}else{
			eObjArea.hide(  );
			eGameOver.hide(  );
			w.menu.eScenarioMenu.hide(  );
			w.chat.eScenarioChat.hide(  );
			w.player.decouplingEvent(  );
		}
		closeDialog(  );
		eSendPause.html( "" );
		eMessagesArea.html( "" );
		w.chat.minimize(  );
	},
	counter:function( aCounter ) {
		if( aCounter == 0 ) {
			eCounter.fadeOut( 200 );
			w.ball.eBall.fadeIn( 100 );
		}else{
			w.ball.eBall.hide(  );
			eCounter.show(  );
			eCounter.html( aCounter );
		}
	}
} );