//	---------------------------------------------------------------------------
//	jWebSocket jQuery PlugIn (uses jWebSocket Client and Server)
//	(C) 2011 Innotrade GmbH, jWebSocket.org, Herzogenrath
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
// authors: Victor and Carlos

$(document).bind("mobileinit", function(){
	$.jws.open();
    
	//BINDING THIS EVENT IS MORE RECOMMENDED THAN $(document).ready()
	$( "#mainPage" ).live( "pagecreate", function( aEvt ){
		$( "#time" ).text( "yyyy-mm-dd hh:mm:ss" ).css({
			"text-align":"center"
		});
		
		$.jws.bind( "org.jwebsocket.plugins.system:welcome", function( aEvt, aToken ) {
			$.jws.submit(
				"org.jwebsocket.plugins.system",
				"login",
				{	username: jws.GUEST_USER_LOGINNAME,
					password: jws.GUEST_USER_PASSWORD
				}
			);
		});		
		
		$.jws.bind( "org.jwebsocket.plugins.system:response", function( aEvt, aToken ) {
			if( "login" == aToken.reqType && 0 == aToken.code ) {
				$.jws.submit(
					"org.jwebsocket.plugins.streaming",
					"register",
					{	stream: "timeStream"
					}
				);
			}
		});
		
		$.jws.bind( "org.jwebsocket.plugins.streaming:event", function( aEvt, aToken) {
			$( "#time" )
				.text(
					aToken.year
						+ "-" + jws.tools.zerofill( aToken.month, 2 ) 
						+ "-" + jws.tools.zerofill( aToken.day, 2 )
						+ " " + jws.tools.zerofill( aToken.hours, 2 )
						+ ":" + jws.tools.zerofill( aToken.minutes, 2 ) 
						+ ":"	+ jws.tools.zerofill( aToken.seconds, 2 ))
				.css({ "text-align":"center" }
			);
		});
	});
	
	
﻿
});
