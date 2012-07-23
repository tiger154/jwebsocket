//	---------------------------------------------------------------------------
//	jWebSocket - channels Plug-In
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
 * jWebSocket Channels Widget
 * @author vbarzana
 */

$.widget( "jws.channels", {
	_init:function( ) {
		w.channels = this;
		
		w.channels.registerEvents( );
	},
    
	registerEvents: function( ) {
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			// OnOpen: function( aEvent ){},
			OnWelcome: function( aEvent ){
				// Registering the callbacks for the channels
				mWSC.setChannelCallbacks({
					OnChannelCreated: w.channels.onChannelCreatedObs,
					OnChannelRemoved: w.channels.onChannelRemovedObs,
					OnChannelsReceived: w.channels.onChannelsReceivedObs
				});
				w.channels.getChannels();
			},
			OnClose: function( ){
				w.channels.destroy();
			},
			OnGoodBye: function( aToken ){
				w.channels.removeClient( aToken.sourceId );
			},
			OnMessage: function( aEvent, aToken ) {
				w.channels.onMessage( aEvent, aToken );
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
			if( aEvent.channels[ lIdx ].id == w.channels.mChannelId ){
				lChannelExists = true;
			}
		}
		// If the channel has not being created yet, create it
		if( !lChannelExists ){
			w.channels.createChannel();
		}
		// Subscribe to a certain channel
		w.channels.subscribeChannel();
		// Authenticate against the channel to publish information on it
		w.channels.auth();
		
		log( "<font style='color:#888'>jWebSocket channels received: '" 
			+ JSON.stringify( aEvent.channels ) + "'</font>" );
	},
	
	// try to create a new channel on the server
	// on success the OnChannelCreated event is fired
	createChannel: function() {
		log( "Creating channel '" + w.channels.mChannelId + "'..." );
		var lRes = mWSC.channelCreate(
			w.channels.mChannelId,
			w.channels.mChannelName,
			{
				isPrivate: w.channels.mChannelIsPrivate,
				isSystem: w.channels.mChannelIsSystem,
				accessKey: w.channels.mChannelAccessKey,
				secretKey: w.channels.mChannelSecretKey
			});
		log( mWSC.resultToString( lRes ) );
	},
	
	//Suscribe to the channel created for channels
	subscribeChannel: function() {
		log( "Subscribing at channel '" + w.channels.mChannelName + "'..." );
		var lRes = mWSC.channelSubscribe( w.channels.mChannelId, w.channels.mChannelAccessKey );
		log( mWSC.resultToString( lRes ) );
	},
	
	// try to authenticate against the channel to publish data
	auth: function() {
		log( "Authenticating against channel '" + w.channels.mChannelId + "'..." );
		// use access key and secret key for this channel to authenticate
		// required to publish data only
		var lRes = mWSC.channelAuth( w.channels.mChannelId, 
			w.channels.mChannelAccessKey, w.channels.mChannelSecretKey );
		log( mWSC.resultToString( lRes ) );
	},
	
	// try to obtain all subscribers for a certain channel
	getSubscribers: function() {
		log( "Trying to obtain subscribers for channel '" + w.channels.mChannelId + "'..." );
		var lRes = mWSC.channelGetSubscribers(w.channels.mChannelId, w.channels.mChannelAccessKey);
		log( mWSC.resultToString( lRes ) );
	},
	
	publish: function( aData, aMessage ){
		mWSC.channelPublish( w.channels.mChannelId, aMessage || "jws-channels", aData );
	},
	
	/**
	 * Executed every time the server sends a message to the client
	 * @param aEvent
	 * @param aToken
	 **/
	onMessage: function( aEvent, aToken ) {
		if( w.channels.eDebug.get(0).checked ) {
			log( "<font style='color:#888'>" + aEvent.data + "</font>" );
		}
		if( aToken ) {
			//			console.log(aToken);
			// is it a response from a previous request of this client?
			if( aToken.type == "response" ) {
				if( aToken.reqType == "login" ) {
					// If successfully logged in
					if( aToken.code == 0 ) {
						
						w.channels.mAuthenticatedUser = aToken.sourceId;
						// Getting the coordinates of the position of the player
						var lX = w.channels.getRandomNumber( 400 );
						var lY = w.channels.getRandomNumber( 150 );
						
						w.channels.mPlayer.loc_x = lX;
						w.channels.mPlayer.loc_y = lY;
						
						w.channels.addGreenClient( w.channels.mAuthenticatedUser, lX, lY );
						
					//						w.channels.startMovingRandom();
					}
				// If the request is an authorization from the channels
				} else if( aToken.reqType == "authorize" ){
					if( aToken.code == 0 ){
						w.channels.getSubscribers();
					}
				} else if( aToken.reqType == "getSubscribers"){
					w.channels.loadClientsList( aToken.subscribers );
				} else if( aToken.type == "event" ) {
					if( "logout" == aToken.name || "disconnect" == aToken.name ){
						w.channels.removeClient( aToken.sourceId );
					}
				}
			}
		}
	}
});