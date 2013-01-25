//	---------------------------------------------------------------------------
//	jWebSocket jQuery PlugIn ( uses jWebSocket Client and Server )
//	( C ) 2011 Innotrade GmbH, jWebSocket.org, Herzogenrath
//	---------------------------------------------------------------------------
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
//	---------------------------------------------------------------------------
// authors: Victor and Carlos

w = { };
w.mobile = { };
w.mobile.NS_SYSTEM		= "org.jwebsocket.plugins.system";
w.mobile.NS_STREAMING_PLUGIN = "org.jwebsocket.plugins.streaming";

$( document ).bind({
	// Loaded second
	"ready": function() {
		w.mobile.eClientStatus		= $( "#client_status" );
		w.mobile.eClientId			= $( "#client_id" );
	},
	// Loaded first
	"mobileinit": function( ) {
		// Open jWebSocket connection using jQueryMobile Plug-in
		$.jws.open( );
	
		// Registering onOpen function, using jqueryPlugIn for 
		// jWebSocket the basic mechanism is easier to handle
		$.jws.bind({ 
			"open": function( aToken ) {
				w.mobile.eClientId.text( "Client-ID: - " );
				w.mobile.eClientStatus.attr( "class", "online").text("connected");
			},
			"close": function( ) {
				w.mobile.eClientId.text( "Client-ID: - " );
				w.mobile.eClientStatus.attr( "class", "offline").text("disconnected");
				$( "#time" ).text( "yyyy-mm-dd hh:mm:ss" ).css({
					"text-align":"center"
				});
			}
		});
    
		//BINDING THIS EVENT IS MORE RECOMMENDED THAN $( document ).ready( )
		$( "#mainPage" ).live( "pagecreate", function( aEvt ) {
		
			$( "#time" ).text( "yyyy-mm-dd hh:mm:ss" ).css({
				"text-align":"center"
			});
		
			// Every when a new message comes from the server, jQuery plug-in
			// fires an event with the structure "namespace:tokentype"
			$.jws.bind( w.mobile.NS_SYSTEM + ":welcome", function( aEvt, aToken ) {
				$.jws.submit( w.mobile.NS_SYSTEM, "login", {
					username: jws.GUEST_USER_LOGINNAME,
					password: jws.GUEST_USER_PASSWORD
				}, {
					// Authenticated successfully, handle statusbar information
					success: function( aToken ) {
						w.mobile.eClientId.text("Client-ID: " + aToken.sourceId);
						w.mobile.eClientStatus.attr( "class", "authenticated").text("authenticated");
					}
				});
			});		
		
			$.jws.bind( w.mobile.NS_SYSTEM + ":response", function( aEvt, aToken ) {
				if( "login" == aToken.reqType && 0 == aToken.code ) {
					$.jws.submit( w.mobile.NS_STREAMING_PLUGIN, "register",
					{
						stream: "timeStream"
					});
				}
			});
		
			$.jws.bind( w.mobile.NS_STREAMING_PLUGIN + ":event", function( aEvt, aToken ) {
				$( "#time" )
				.text( 
					aToken.year
					+ "-" + jws.tools.zerofill( aToken.month, 2 ) 
					+ "-" + jws.tools.zerofill( aToken.day, 2 )
					+ " " + jws.tools.zerofill( aToken.hours, 2 )
					+ ":" + jws.tools.zerofill( aToken.minutes, 2 ) 
					+ ":" + jws.tools.zerofill( aToken.seconds, 2 ) )
				.css({
					"text-align":"center"
				});
			});
		});
	}
});
