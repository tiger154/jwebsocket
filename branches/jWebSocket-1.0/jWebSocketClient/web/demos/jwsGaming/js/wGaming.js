//	---------------------------------------------------------------------------
//	jWebSocket - gaming Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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

/**
 * jWebSocket Gaming Widget
 * @author vbarzana
 */

$.widget( "jws.gaming", {
	_init:function( ) {
		w.gaming = this;
		
		// Default namespace for demos org.jwebsocket.plugins.gaming
		w.gaming.NS = jws.NS_BASE + ".plugins.gaming";
		
		w.gaming.mChannelId = "jws-gaming-ch";
		w.gaming.mChannelName = "jWebSocketGaming"
		w.gaming.mChannelIsPrivate = false;
		w.gaming.mChannelIsSystem = false;
		w.gaming.mChannelAccessKey = "play1";
		w.gaming.mChannelSecretKey = "t0ps3cr3t!";
		
		w.gaming.mAuthenticatedUser = null;
		w.gaming.mOnlineClients = {};
		
		// Represents each connected user using a different color
		w.gaming.mUserColors = {
		//	01.38649.1: "#0000FF"
		};
		
		// this is the location of the green player
		w.gaming.mPlayer = {
			loc_x: 100,
			loc_y: 100,
			speed: 10
		};
		
		w.gaming.ePlayGround  = w.gaming.element.find( "#players_area" );
		w.gaming.eTotalPlayers  = w.gaming.element.find( "#total_red" );
		w.gaming.eTotalGreen  = w.gaming.element.find( "#total_green" );
		w.gaming.eDebug  = w.gaming.element.find( "#debug" );
		
		w.gaming.registerEvents( );
	},
    
	registerEvents: function( ) {
		$( document ).keydown( w.gaming.processKeyDown );
		$( w.gaming.ePlayGround ).mousemove( w.gaming.playGroundMouseMove );
		
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			// OnOpen: function( aEvent ){},
			OnWelcome: function( aEvent ){
				// Registering the callbacks for the channels
				mWSC.setChannelCallbacks({
					OnChannelCreated: w.gaming.onChannelCreatedObs,
					OnChannelRemoved: w.gaming.onChannelRemovedObs,
					OnChannelsReceived: w.gaming.onChannelsReceivedObs
				});
				w.gaming.getChannels();
			},
			OnClose: function( ){
				w.gaming.destroy();
			},
			OnGoodBye: function( aToken ){
				w.gaming.removeClient( aToken.sourceId );
			},
			OnMessage: function( aEvent, aToken ) {
				w.gaming.onMessage( aEvent, aToken );
			}
		};
		$( "#demo_box" ).auth( lCallbacks );
	},
	
	// Trying to obtain all available channels on the server
	getChannels: function() {
		log( "Trying to obtain channels..." );
		var lRes = mWSC.channelGetIds();
		log( mWSC.resultToString( lRes ) );
	},
	
	// this method is called when a new channel has been created on the server
	onChannelCreatedObs: function( aEvent ) {
		log( "The following channel has been created for the game: { name: '" 
			+ aEvent.channelName + "', id: '" + aEvent.channelId  + "'}");
	},

	// this method is called when a channel has been removed from the server
	onChannelRemovedObs: function( aEvent ) {
	// Nothing to do here
	},
	
	onChannelsReceivedObs: function( aEvent ) {
		var lChannelExists = false;
		// Add all channels from event
		for( var lIdx = 0, lCnt = aEvent.channels.length; lIdx < lCnt; lIdx++ ) {
			if( aEvent.channels[ lIdx ].id == w.gaming.mChannelId ){
				lChannelExists = true;
			}
		}
		// If the channel has not being created yet, create it
		if( !lChannelExists ){
			w.gaming.createChannel();
		}
		// Subscribe to a certain channel
		w.gaming.subscribeChannel();
		// Authenticate against the channel to publish information on it
		w.gaming.auth();
		
		log( "<font style='color:#888'>jWebSocket channels received: '" 
			+ JSON.stringify( aEvent.channels ) + "'</font>" );
	},
	
	// try to create a new channel on the server
	// on success the OnChannelCreated event is fired
	createChannel: function() {
		log( "Creating channel '" + w.gaming.mChannelId + "'..." );
		var lRes = mWSC.channelCreate(
			w.gaming.mChannelId,
			w.gaming.mChannelName,
			{
				isPrivate: w.gaming.mChannelIsPrivate,
				isSystem: w.gaming.mChannelIsSystem,
				accessKey: w.gaming.mChannelAccessKey,
				secretKey: w.gaming.mChannelSecretKey
			});
		log( mWSC.resultToString( lRes ) );
	},
	
	//Suscribe to the channel created for gaming
	subscribeChannel: function() {
		log( "Subscribing at channel '" + w.gaming.mChannelName + "'..." );
		var lRes = mWSC.channelSubscribe( w.gaming.mChannelId, w.gaming.mChannelAccessKey );
		log( mWSC.resultToString( lRes ) );
	},
	
	// try to authenticate against the channel to publish data
	auth: function() {
		log( "Authenticating against channel '" + w.gaming.mChannelId + "'..." );
		// use access key and secret key for this channel to authenticate
		// required to publish data only
		var lRes = mWSC.channelAuth( w.gaming.mChannelId, 
			w.gaming.mChannelAccessKey, w.gaming.mChannelSecretKey );
		log( mWSC.resultToString( lRes ) );
	},
	
	// try to obtain all subscribers for a certain channel
	getSubscribers: function() {
		log( "Trying to obtain subscribers for channel '" + w.gaming.mChannelId + "'..." );
		var lRes = mWSC.channelGetSubscribers(w.gaming.mChannelId, w.gaming.mChannelAccessKey);
		log( mWSC.resultToString( lRes ) );
	},
	
	publish: function( aData, aMessage ){
		mWSC.channelPublish( w.gaming.mChannelId, aMessage || "jws-gaming", aData );
	},
	
	playGroundMouseMove: function(){
		var lClient = $('.ui-draggable-dragging');
		// If the element is been dragged
		if( lClient.get(0) ){
			var lTop = lClient.css( "top" );
			var lLeft = lClient.css( "left" );
			w.gaming.mPlayer.loc_x = parseInt( lLeft.substr( 0, lLeft.length - 1 ) );
			w.gaming.mPlayer.loc_y = parseInt( lTop.substr( 0, lTop.length - 1 ) );
		
			w.gaming.notifyMovement( w.gaming.mAuthenticatedUser, 
				w.gaming.mPlayer.loc_x,
				w.gaming.mPlayer.loc_y );
		}
	},
	
	startMovingRandom: function(){
		// return if not (yet) connected
		if( !mWSC.isLoggedIn() ) {
			// TODO: provide reasonable result here!
			return;
		}
		var lInterval = 300;
		var lImmediate = true;
		
		if( lImmediate ) {
			// start moving immediately, if requested
			w.gaming.moveGreenPlayerRandom();
		}
		// and then initiate interval...
		w.gaming.hSimulation = setInterval(
			function() {
				if( mWSC.isLoggedIn() ) {
					w.gaming.moveGreenPlayerRandom();
				} else {
					w.gaming.stopMovingRandom();
				}
			},
			lInterval
			);
	},
	
	stopMovingRandom: function(){
		clearInterval( w.gaming.hSimulation );
		w.gaming.hSimulation = null;
	},
	
	destroy: function(){
		if( w.gaming.hSimulation ){
			w.gaming.stopMovingRandom();
		}
		
		for( var lKey in w.gaming.mOnlineClients ) {
			w.gaming.mOnlineClients[ lKey ].detach().remove();
		}
		
		w.gaming.mOnlineClients = {};
		
		w.gaming.eTotalPlayers.text( 0 );
		w.gaming.eTotalGreen.text( 0 );
	},
	
	moveGreenPlayerRandom: function( aSpeed ){
		aSpeed = aSpeed || 4;
		var lPlusMinus = (w.gaming.getRandomNumber( 5 ) <= 4)?"+":"-";
		
		var lX = eval( lPlusMinus + parseInt( w.gaming.getRandomNumber( aSpeed ) ) );
		var lY = eval( lPlusMinus + parseInt( w.gaming.getRandomNumber( aSpeed ) ) );
		
		w.gaming.mPlayer.loc_x += lX;
		w.gaming.mPlayer.loc_y += lY;
		
		w.gaming.notifyMovement( w.gaming.mAuthenticatedUser, 
			w.gaming.mPlayer.loc_x, w.gaming.mPlayer.loc_y );
	},
	
	/**
	 * Executed every time the server sends a message to the client
	 * @param aEvent
	 * @param aToken
	 **/
	onMessage: function( aEvent, aToken ) {
		if( w.gaming.eDebug.get(0).checked ) {
			log( "<font style='color:#888'>" + aEvent.data + "</font>" );
		}
		if( aToken ) {
			//			console.log(aToken);
			// is it a response from a previous request of this client?
			if( aToken.type == "response" ) {
				if( aToken.reqType == "login" ) {
					// If successfully logged in
					if( aToken.code == 0 ) {
						
						w.gaming.mAuthenticatedUser = aToken.sourceId;
						// Getting the coordinates of the position of the player
						var lX = w.gaming.getRandomNumber( 400 );
						var lY = w.gaming.getRandomNumber( 150 );
						
						w.gaming.mPlayer.loc_x = lX;
						w.gaming.mPlayer.loc_y = lY;
						
						w.gaming.addGreenClient( w.gaming.mAuthenticatedUser, lX, lY );
						
					//						w.gaming.startMovingRandom();
					}
				// If the request is an authorization from the channels
				} else if( aToken.reqType == "authorize" ){
					if( aToken.code == 0 ){
						w.gaming.getSubscribers();
					}
				} else if( aToken.reqType == "getSubscribers"){
					w.gaming.loadClientsList( aToken.subscribers );
				} else if( aToken.type == "event" ) {
					if( "logout" == aToken.name || "disconnect" == aToken.name ){
						w.gaming.removeClient( aToken.sourceId );
					}
				}
			}
		}
		
	/*
			
				// figure out of which request
						 + "@" + aToken.;
						// Sending a register token to the server
						var lRegister = {
							x: lX,
							y: lY,
							ns: w.gaming.NS,
							type: "register"
						};
						mWSC.sendToken( lRegister );
					}
				}
			// is it an event w/o a previous request ?
			} else if( aToken.type == "getGamingClients" ) {
				w.gaming.loadClientsList( aToken.clients );
			} else if( aToken.type == "event" ) {
				if( "logout" == aToken.name || "disconnect" == aToken.name ){
					w.gaming.removeClient( aToken.sourceId );
				}
			// is it any token from another client
			} else if( aToken.type == "moveTo" ) {
				w.gaming.moveTo( aToken.sourceId, aToken.x, aToken.y );
			} else if( aToken.type == "newClientConnected" ){
				w.gaming.addRedClient( aToken.sourceId, aToken.x, aToken.y );
			}
			console.log(aToken);
		}
			 */
	},
	
	processKeyDown: function( aEvent ) {
		if( w.gaming.canMove() ){
			// here the "gaming" event is broadcasted to all other clients
			// console.log( "keydown: " + aEvent.keyCode );
			//some firefox versions need keyChar event
			var lCode = aEvent.keyCode || aEvent.keyChar;
			var lCanMove = false;
			switch( lCode ) {
				case 13:
					if( !mWSC.isLoggedIn() ){
						w.auth.logon();
					}
					return;
				case 38: // up
					if( w.gaming.canMove( "up" ) ){
						w.gaming.mPlayer.loc_y -= w.gaming.mPlayer.speed;
						lCanMove = true;
					}
					break;
				case 40: // down
					if( w.gaming.canMove( "down" ) ){
						w.gaming.mPlayer.loc_y += w.gaming.mPlayer.speed;
						lCanMove = true;
					}
					break;
				case 37: // left
					if( w.gaming.canMove( "left" ) ){
						w.gaming.mPlayer.loc_x -= w.gaming.mPlayer.speed;
						lCanMove = true;
					}
					break;
				case 39: // right
					if( w.gaming.canMove( "right" ) ){
						w.gaming.mPlayer.loc_x += w.gaming.mPlayer.speed;
						lCanMove = true;
					}
					break;
				default:
					return;
			}
			if( lCanMove ){
				w.gaming.notifyMovement( w.gaming.mAuthenticatedUser, 
					w.gaming.mPlayer.loc_x, 
					w.gaming.mPlayer.loc_y );
			}
			
			aEvent.preventDefault();
		
		// This action requires no server side PlugIn, 
		// It broadcasts all movements of a client but all 
		// demos in the client are notified here
		/*lWSC.broadcastGamingEvent({
				keycode: aEvent.keyCode,
				x: w.gaming.mPlayer.loc_x,
				y: w.gaming.mPlayer.loc_x
			});*/
		}
	},
	
	canMove: function( aLocation ){
		var lClient = w.gaming.mOnlineClients[ w.gaming.mAuthenticatedUser ];
		var lX = w.gaming.mPlayer.loc_x;
		var lY = w.gaming.mPlayer.loc_y;
		
		var lCanMove = true;
		
		switch( aLocation ){
			case "up":
				var lTop = lClient.css("top");
				//				console.log(lTop);
				break;
			case "down":
				break;
			case "left":
				break;
			case "right":
				break;
		}
		return lCanMove;
	},
	
	/**
		 * adds the green client automatically
		 * @param aClientId 
		 * Identifier of the client guest@01.38649.1
		 * @param aX
		 * position of the x axis
		 * @param aY
		 * position of the y axis
		 * 
		 **/
	addGreenClient: function( aClientId, aX, aY ){
		if( aClientId ){
			var lPlayer = $('<div class="player green_player" id="'+ aClientId +
				'">'+ aClientId +'</div>');
				
			// Use a default color for each client
			var lColor = w.gaming.getClientColor( aClientId );
				
			lPlayer.css({
				color: lColor,
				left: aX + "px",
				top: aY + "px"
			});
			
			// jQuery-ui method, starts dragging an element
			lPlayer.draggable();
				
			// Add the user in a random location in the playGround
			w.gaming.ePlayGround.append( lPlayer );
			
			// Saving the client in the onlineClients array
			w.gaming.mOnlineClients[ aClientId ] = lPlayer;
			
			// Notify others of the new player and it's location
			w.gaming.notifyMovement( aClientId, aX, aY );
			w.gaming.eTotalGreen.text( parseInt( w.gaming.eTotalGreen.text()) + 1 );
		}
	},
	
	notifyMovement: function( aClientId, aX, aY ) {
		if( mWSC.isConnected() ){
			var lData = {
				sourceId: aClientId,
				x: aX,
				y: aY
			};
			w.gaming.publish( lData );
		//			
		//			//			
		//			// Preparing Token to notify movement
		//			var lMovementToken = {
		//				ns: w.gaming.NS,
		//				type: "moveTo",
		//				x: aX,
		//				y: aY,
		//				sourceId: aClientId
		//			};
		//			
		//			// Sending the notification to all registered users
		//			mWSC.sendToken( lMovementToken );
		} else{
			dialog("Sorry, you are not connected with jWebSocket server, \n\
						try clicking in the login button!", 
				"Not connected", false, null, null, "alert");
		}
	},
	
	/**
		 * Moves a client in the current playGround
		 **/
	moveTo: function( aClientId, aX, aY ){
		var lClient = $('.ui-draggable-dragging');
		// don't move it if is been dragged by jQuery, 
		// the drag event is responsible for that
		if( !lClient.get(0) ){
			w.gaming.mOnlineClients[ aClientId ].css({
				"left": aX + "px",
				"top": aY + "px"
			});
		
			if( aClientId == w.gaming.mAuthenticatedUser ){
				var lHeight = w.gaming.mOnlineClients[ aClientId ].get( 0 ).height;
				lHeight = ( lHeight )?lHeight.substr( 0, lHeight.length ):70;
			
				var lWidth = w.gaming.mOnlineClients[ aClientId ].get( 0 ).width;
				lWidth = ( lWidth )?lWidth.substr( 0, lWidth.length ):65;
			
				w.gaming.ePlayGround.scrollTop( eval( aY + lHeight) - 
					w.gaming.ePlayGround.get( 0 ).clientHeight );
		
				w.gaming.ePlayGround.scrollLeft( eval( aX + lWidth) - 
					w.gaming.ePlayGround.get( 0 ).clientWidth );
			}
		}
	},
	
	/**
		 * Shows connected clients in the playground
		 * @param aSubscribers 
		 *  The list of clients to be shown to the users
		 **/
	loadClientsList: function( aSubscribers ){
		console.log(aSubscribers);
		return;
		// Remove green player from the subscribers list
		for(var lElem in aSubscribers ){
			if( w.gaming.mAuthenticatedUser ==  aSubscribers[ lElem ].id ) {
				delete aSubscribers[ lElem ];
				break;
			}
		}
	
		w.gaming.eTotalPlayers.text( aSubscribers.length );
		if( aSubscribers.length > 0 ){
			
			$( aSubscribers ).each(function( aIndex, aClient ){
				var lClientId = aClient;
				
				var lPlayerItem = $('<div class="player red_player" id="'+ lClientId +
					'">'+ lClientId + '</div>');
				
				// Use a default color for each client
				var lColor = w.gaming.getClientColor( lClientId );
				
				lPlayerItem.css({
					color: lColor,
					left: aClient.x +"px",
					top: aClient.y + "px"
				});
				
				// Add the user in a random location in the playGround
				w.gaming.ePlayGround.append( lPlayerItem );
				
				// Saving the client in the onlineClients array
				w.gaming.mOnlineClients[ lClientId ] = lPlayerItem;
			});
		}
	},
	
	/**
		 * Adds a new incoming client to the playGround
		 * @param aClientId 
		 * Identifier of the client guest@01.38649.1
		 * @param aX
		 * position of the x axis
		 * @param aY
		 * position of the y axis
		 * 
		 **/
	addRedClient: function( aClientId, aX, aY ){
		var lArr = aClientId.split( "@" );
		var lPlayerItem = $('<div class="player red_player" id="'+ aClientId +
			'">'+ lArr[0]+"<br/>"+ lArr[1] +'</div>');
				
		// Use a default color for each client
		var lColor = w.gaming.getClientColor( aClientId );
				
		lPlayerItem.css({
			color: lColor,
			left: aX + "px",
			top: aY + "px"
		});
				
		// Add the user in a random location in the playGround
		w.gaming.ePlayGround.append( lPlayerItem );
				
		// Saving the client in the onlineClients array
		w.gaming.mOnlineClients[ aClientId ] = lPlayerItem;
		w.gaming.eTotalPlayers.text( parseInt( w.gaming.eTotalPlayers.text()) + 1 );
	},
	
	/**
		 * Removes a client from the playGround
		 * @param aClients 
		 *  The list of clients to be shown to the users
		 **/
	removeClient: function( aClientId ){
		if( w.gaming.mOnlineClients ) {
			var lFoundKey = null;
			for( var lKey in w.gaming.mOnlineClients ) {
				var lId = lKey.split( "@" );
				
				if( aClientId  ==  lId[1] ) {
					lFoundKey = lKey;
					w.gaming.mOnlineClients[ lKey ].detach().remove();
					delete w.gaming.mOnlineClients[ lFoundKey ];
					w.gaming.eTotalPlayers.text( parseInt( w.gaming.eTotalPlayers.text()) - 1 );
					return;
				}
			}
			
		}
	},
	
	/**
		 * Gets a color for a given clientID
		 * @param aClientID
		 **/
	getClientColor: function( aClientID ){
		var lColor = null;
		if( aClientID != undefined ){
			do{
				// Change the color if it is assigned to other client
				lColor = w.gaming.getRandomColor( 12 );
			} while( w.gaming.isColorUsed( lColor ) );
			
			w.gaming.mUserColors[ aClientID ] = 
			w.gaming.mUserColors[ aClientID ] || lColor;
			
			return w.gaming.mUserColors[ aClientID ];
		}
		return null;
	},
	
	/**
		 * Removes a defined color for a given clientID
		 * @param aClientID
		 **/
	removeClientColor: function( aClientID ){
		if( w.gaming.mUserColors ) {
			for( var lIdx = 0; lIdx < w.gaming.mUserColors.length; lIdx++ ) {
				for(var lElem in w.gaming.mUserColors[ lIdx ]){
					if( aClientID ==  lElem) {
						w.gaming.mUserColors.splice( lIdx, 1 );
						break;
					}
				}
			}
		}
	},
	
	/**
		 * Determines wether a given color is assigned to a user
		 * @param aColor
		 **/
	isColorUsed: function( aColor ){
		var lFound = false;
		$( w.gaming.mUserColors ).each( function( aIndex, aElem ){
			for ( var aKey in aElem ){
				if( aElem[ aKey ] == aColor ){
					lFound = true;
					break;
				}
			}
		});
		return lFound;
	},
	
	/**
		 * Returns a color using a given intensity
		 * @param aIntensity
		 * 0 -  5  low intensity	(dark)
		 * 5 -  10 medium intensity (half dark)
		 * 10 - 15 high intensity   (light)
		 * default intensity = 10
		 */
	getRandomColor: function( aIntensity ){
		var lColorChars = [
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
		];
		
		aIntensity = aIntensity || 10;
		
		var lRed = lColorChars[ w.gaming.getRandomNumber( aIntensity ) ] + 
		lColorChars[ w.gaming.getRandomNumber( aIntensity ) ];
	
		var lGreen = lColorChars[ w.gaming.getRandomNumber( aIntensity ) ] + 
		lColorChars[ w.gaming.getRandomNumber( aIntensity ) ];
	
		var lBlue = lColorChars[ w.gaming.getRandomNumber( aIntensity ) ] + 
		lColorChars[ w.gaming.getRandomNumber( aIntensity ) ];

		return "#"+lRed + lGreen + lBlue;
	},
	getRandomNumber: function( aNumber ){
		return Math.floor(Math.random(aNumber) * aNumber);
	}
});