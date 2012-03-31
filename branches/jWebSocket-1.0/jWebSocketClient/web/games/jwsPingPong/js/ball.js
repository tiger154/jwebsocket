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
$.widget( "jws.ball", {
    _init: function(  ) {
        w.ball        = this; 
		
		w.ball.eBall   = w.ball.element.find( '#ball' );
        w.ball.eBall.hide(  );
	    w.ball.onMessage(  );
    }, 
    onMessage: function(  ) {
     
        $.jws.bind( 'pingpong:ball', function( aEvt, aToken ) {
            w.ball.updateBall( aToken.width, aToken.height );           
        } ); 
        $.jws.bind( 'pingpong:moveball', function( aEvt, aToken ) {
           w.ball.moveBall( aToken.posX,aToken.posY );           
        } );
        $.jws.bind( 'pingpong:sound', function( aEvt, aToken ) {
            $( "#sound" )[0].play(  );
        } );
    },
    updateBall: function( aWidth, aHeight ) {
        w.ball.eBall.css( { 
            'width': aWidth + 2 +'px', 
            'height': aHeight +3 +'px'                        
        } );        
    },
    moveBall: function( posX,posY ) {
       w.ball.eBall.css( { 
            'left' :posX+'px', 
            'top' :posY+'px'                        
        } );   
    }
} );