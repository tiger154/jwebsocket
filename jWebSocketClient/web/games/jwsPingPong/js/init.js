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

function init(  ) {
	w						= { };
	mLog					= { };
	mLog.isDebugEnabled		= true;
	
	$( "#demo_box" ).authentication(  );	
	$.jws.bind( 'open', function( aEvt, aToken ) {
		$( '#board' ).ball(  );
		$( '#scenario_body' ).stage(  ); 
		$( '#board' ).player(  );
		$( '#online' ).connected(  );
		$( '#main_content' ).menu(  );
		$( '#main_content' ).chat(  );
		$( '#scenario_body' ).ranking(  );
	} );
	
	$.jws.bind("org.jwebsocket.plugins.system:welcome", function(aEvt, aToken){
		//Change status offline by online
		$( "#client_status" ).hide(  ).attr( "class", "" ).addClass( "online" ).text( "online" ).show(  );
		$( "#client_id" ).text("Client-ID: " + aToken.sourceId);
	});
	
	$.jws.bind( 'close', function( aEvt, aToken ) {
		//Change the status online by offline
		$( "#client_status" ).hide(  ).attr( "class", "" ).addClass( "offline" ).text( "disconnected" ).show(  );
		$( "#client_id" ).text("Client-ID: - ");
		
		dialog( 'Ping Pong Game', 'There is no connection with the server', true );
		
		//modifying the dialog style
		$( ".ui-widget-overlay" ).css( {
			'background': '#eeeeee !important',
			'opacity':'.80',
			'filter':'Alpha( Opacity=80 )'
		} );
	} );
	
	$.jws.bind( 'pingpong:databaseError', function( aEvt, aToken ) {
		dialog( 'Ping Pong Game', "<div style='color:red;'>" + aToken.msg + "</div>", true );
		//modifying the dialog style
		$( ".ui-widget-overlay" ).css( {
			'background': '#eeeeee !important',
			'opacity':'.80',
			'filter':'Alpha( Opacity=80 )'
		} );
	} );
	
	//Set WebSocket type
	$('#websocket_type').text("WebSocket: " + (jws.browserSupportsNativeWebSockets ? "(native)" : "(flashbridge)" ));
	$( '#scenario_menu' ).hide(  );
	$( '#obj_area' ).hide(  );
	$( '#scenario_chat' ).hide(  );
}


$( document ).ready( function(  ) {
	init(  );
} );