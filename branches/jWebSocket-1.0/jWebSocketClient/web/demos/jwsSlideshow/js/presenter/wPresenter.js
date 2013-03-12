//	****************************************************************************
//	jWebSocket Slideshow presenter Widget (uses jWebSocket Client and Server)
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
$.widget( "jws.presenter", {
    
	_init: function() {
		// ------ DOM ELEMENTS --------
		this.eBtnNext = this.element.find( "#btn_next" );
		this.eBtnPrev = this.element.find( "#btn_prev" );
		this.eBtnFirst = this.element.find( "#btn_first" );
		this.eBtnLast = this.element.find( "#btn_last" );
		this.eClients = this.element.find( "#viewers" );
		this.eSlide = this.element.find( "#slide" );
		this.eContainer = $( ".container" );
		
		// ------ VARIABLES --------
		this.mCurrSlide = 1;
		this.mOldSlide = 0;
		this.mMaxSlides = 22;
		this.mClients = 0;
		
		w.presenter    = this;
		w.presenter.registerEvents();
	},
	
	registerEvents: function(){
		w.presenter.eBtnNext.click( w.presenter.nextSlide );
		w.presenter.eBtnPrev.click( w.presenter.prevSlide );
		w.presenter.eBtnLast.click( w.presenter.lastSlide );
		w.presenter.eBtnFirst.click( w.presenter.firstSlide );
	
		$( document ).keydown( w.presenter.keydown );
		
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function( aEvent, aToken ) {
				w.presenter.onMessage( aEvent, aToken );
			}
		};
		w.presenter.eContainer.auth( lCallbacks );
		
		// Try to logon in the server using the default guest credentials
		// for more information check the public widget wAuth.js
		w.auth.logon( true );
	},
	
	nextSlide: function( ) {
		if( w.presenter.mCurrSlide < w.presenter.mMaxSlides ) {
			w.presenter.mCurrSlide++;
			w.presenter.updateSlide( );
		}
	},
	
	prevSlide: function( ) {
		if( w.presenter.mCurrSlide > 1 ) {
			w.presenter.mCurrSlide--;
			w.presenter.updateSlide( );
		}
	},
	
	lastSlide: function( ) {
		w.presenter.mCurrSlide = w.presenter.mMaxSlides;
		w.presenter.updateSlide( );
	},
	
	firstSlide: function( ) {
		w.presenter.mCurrSlide = 1;
		w.presenter.updateSlide( );
	},
	
	goTo: function( aSlide ) {
		if( w.presenter.mOldSlide != aSlide ) {
			// Don't include slideshow effects or you won't get real time
			//			w.presenter.eSlide.fadeTo( 80, 0.2, function(){
			w.presenter.eSlide.attr( "src" ,"slides/Slide" + 
				jws.tools.zerofill( aSlide, 4 ) + ".gif");
			//			});
			//			w.presenter.eSlide.fadeTo( 40, 1 );
		
			w.presenter.eBtnLast.isDisabled && w.presenter.eBtnLast.enable( );
			w.presenter.eBtnNext.isDisabled && w.presenter.eBtnNext.enable( );
			w.presenter.eBtnFirst.isDisabled && w.presenter.eBtnFirst.enable( );
			w.presenter.eBtnPrev.isDisabled && w.presenter.eBtnPrev.enable( );
		
			if( aSlide == 1 ){
				w.presenter.eBtnPrev.disable( );
				w.presenter.eBtnFirst.disable( );
			}else if( aSlide == w.presenter.mMaxSlides ) {
				w.presenter.eBtnNext.disable( );
				w.presenter.eBtnLast.disable( );
			}
			w.presenter.mOldSlide = aSlide;
		}
	},
	
	updateSlide: function() {
		if( w.presenter.mOldSlide != w.presenter.mCurrSlide ) {
			mWSC.broadcastToken({
				action: "slide",
				slide: w.presenter.mCurrSlide,
				senderIncluded: true,
				responseRequested: false,
				clientCount: w.presenter.mClients
			});
		}
	},
	
	keydown: function( aEvent ) {
		if ( mWSC.isConnected() ) {
			var lKeyCode = aEvent.keyCode || aEvent.keyChar;
			switch( lKeyCode ) {
				case 37: {
					// Left Arrow (Ctrl key pressed takes you to the First slide)
					aEvent.ctrlKey && w.presenter.firstSlide()
					|| w.presenter.prevSlide();	
					aEvent.preventDefault();
					break;
				}
				case 39: {
					// Right Arrow (Ctrl key pressed takes you to the First slide)
					aEvent.ctrlKey && w.presenter.lastSlide()
					|| w.presenter.nextSlide();	
					aEvent.preventDefault();
					break;	
				}
			}
		}
	},
	
	onMessage: function( aEvent, aToken ) {
		console.log(aToken);
		// check if slide has to be updated
		if( aToken.action == "slide" ) {
			w.presenter.mClients = aToken.clientCount;
			w.presenter.eClients.text( w.presenter.mClients );
			if( w.presenter.mOldSlide != aToken.slide ) {
				// Pass the current slide
				w.presenter.goTo( aToken.slide );
				// Log the incoming message
				if( mLog.isDebugEnabled ) {
					log( "<font style='color:#888'>jWebSocket '" + aToken.type 
						+ "' token received, full message: '" + aEvent.data + "</font>" );
				}
			}
		// check if new client connected and send the current for initialization
		} else if( "event" == aToken.type
			&& "connect" == aToken.name ) {
			w.presenter.mClients = aToken.clientCount;
			w.presenter.eClients.text( w.presenter.mClients );
			mWSC.sendToken({
				ns: jws.NS_SYSTEM,
				type: "send",
				targetId: aToken.sourceId,
				action: "slide",
				slide: w.presenter.mCurrSlide,
				clientCount: w.presenter.mClients
			});
		} else if( "event" == aToken.type
			&& "disconnect" == aToken.name ) {
			w.presenter.mClients = aToken.clientCount - 1;
			w.presenter.eClients.text( w.presenter.mClients );
			mWSC.sendToken({
				ns: jws.NS_SYSTEM,
				type: "send",
				targetId: aToken.sourceId,
				action: "slide",
				slide: w.presenter.mCurrSlide,
				clientCount: w.presenter.mClients
			});
		}
	}
});

(function($){
	$.fn.buttons = {};
	$.fn.disable = function( ) {
		var lButton = this;
		var lId = lButton.attr("id");
		lButton.isDisabled = true;
		$.fn.buttons[lId] = lButton.clone();
		var lEvents = ["onmouseover","onmousedown","onmouseup","onmouseout","onclick"];
		$( lEvents ).each(function( aIndex, aElem ){
			lButton.attr( aElem, null );
		});
		lButton.attr( "class", "button onmousedown" );
	};
	$.fn.enable = function( ) {
		var lButton = this;
		var lId = lButton.attr( "id" );
		var lEvents = ["onmouseover","onmousedown","onmouseup","onmouseout","onclick"];
		$( lEvents ).each(function( aIndex, aAttribute ){
			lButton.attr( aAttribute, $.fn.buttons[ lId ].attr( aAttribute ) );
		});
		lButton.attr( "class", "button onmouseout" );
		
		lButton.isDisabled = false;
	};
})(jQuery);