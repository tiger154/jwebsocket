//	****************************************************************************
//	jWebSocket Slideshow Viewer Widget (uses jWebSocket Client and Server)
//	(C) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
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
//	****************************************************************************

/*
 * @author vbarzana
 */
$.widget( "jws.viewer", {
    
	_init: function() {
		// ------ DOM ELEMENTS --------
		this.eClients = this.element.find( "#viewers" );
		this.eSlide = this.element.find( "#slide" );
		this.eContainer = $( ".container" );
		
		// ------ VARIABLES --------
		this.mCurrSlide = 1;
		this.mMaxSlides = 22;
		this.mClientCount = 0;
		
		w.viewer    = this;
		w.viewer.registerEvents();
	},
	
	registerEvents: function(){
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function( aEvent, aToken ) {
				w.viewer.onMessage( aEvent, aToken );
			}
		};
		w.viewer.eContainer.auth( lCallbacks );
		
		// Try to logon in the server using the default guest credentials
		// for more information check the public widget wAuth.js
		w.auth.logon( true );
	},
	
	goTo: function( aSlide ) {
		if( w.viewer.mCurrSlide != aSlide ) {
			w.viewer.eSlide.attr( "src" ,"slides/Slide" + 
				jws.tools.zerofill( aSlide, 4 ) + ".gif");
			w.viewer.mCurrSlide = aSlide;
		}
	},
	
	onMessage: function( aEvent, aToken ) {
		if( "slide" == aToken.action ) {
			w.viewer.goTo( aToken.slide );
			w.viewer.eClients.text( aToken.clientCount );
			
		} else if( "event" == aToken.type
			&& "connect" == aToken.name ) {
			w.viewer.eClients.text( aToken.clientCount );
		} else if( "event" == aToken.type
			&& "disconnect" == aToken.name ) {
			w.viewer.eClients.text( aToken.clientCount );
		}
	}
});