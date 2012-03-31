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
$.widget( "jws.ranking", {
        
    _init:function(  ) {
        w.Ranking				= this;
		
        w.Ranking.eRankingDiv	= w.Ranking.element.find( '#ranking div.ranking' ); 
		
        w.Ranking.onMessage(  );         
    },
    onMessage: function(  ) {        
        $.jws.bind( 'pingpong:ranking', function( aEvt, aToken ) {
            
            w.Ranking.eRankingDiv.html( "" );
            for ( var i = 0; i < aToken.username.length; i++ ) {
                w.Ranking.initRanking( aToken.username[i], aToken.wins[i], aToken.lost[i] );
            }                       
        } );    
        $.jws.bind( 'pingpong:deleteranking', function( aEvt, aToken ) {
            w.Ranking.eRankingDiv.html( "" );                                
        } );     
    },
    initRanking:function( aUsername, aWins, aLosts ) {
        var $O_p   =$( '<div class="name">' ).text( aUsername ).append( '</div>' );
        var $O_pp   =$( '<div class="points">' ).text( aWins+" - "+aLosts ).append( '</div>' );
        w.Ranking.eRankingDiv.append( $O_p );
        w.Ranking.eRankingDiv.append( $O_pp );       
    }
} );
