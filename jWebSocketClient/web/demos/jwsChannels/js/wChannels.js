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
 * @author Victor Antonio Barzana Crespo
 */

$.widget( "jws.channels", {
	_init: function( ) {
		// ------------- VARIABLES -------------
		// Persists the current selected channel
		this.mChannelsList = {};
		
		this.mSelectedChannel = null;
		this.mSelectedRow = null;
		
		// Messages to show in the demo
		this.MSG_NOCHANNELS = "There are not channels to show.";
		this.MSG_NOSUBSCRIPTIONS = "There are not subscriptions to show.";
		this.MSG_NOSUBSCRIBERS = "There are no subscribers for this channel yet.";
		this.MSG_ACCESSKEY = "access key";
		this.MSG_SECRETKEY = "secret key";
		this.MSG_CHANNELID = "channel id";
		this.MSG_CHANNELNAME = "channel name";
		this.MSG_MESSAGE = "message";
		this.MSG_PUBLISHMESSAGE = "Type your message...";
		this.MSG_PRIVATE = "private";
		this.MSG_PUBLIC = "public";
		this.MSG_NOTCONNECTED = "Sorry, you are not connected to the " +
		"server, try updating your browser or clicking the login button";
		
		// Class names used in the demo
		this.CLS_TH = "table_header";
		this.CLS_ACTIVE = "active";
		this.CLS_NOCHANNELS = "no_channels";
		this.CLS_ON = "bullet_on";
		this.CLS_OFF = "bullet_off";
		this.CLS_HOVER = "hover";
		this.CLS_STRIPE = "gray";
		
		// ------------- TEXT FIELDS -------------
		// Text field elements
		this.eTxtAccessKey = this.element.find("#txt_access_key");
		this.eTxtChannelId = this.element.find("#txt_channel_id");
		this.eTxtSecretKey = this.element.find("#txt_secret_key");
		this.eTxtChannelName = this.element.find("#txt_channel_name");
		this.eTxtMessage = this.element.find("#txt_message");
		
		// ------------- BUTTONS -------------
		this.eBtnSwitchChannels = this.element.find("#switch_channels_btn");
		this.eBtnSwitchSubscriptions = this.element.find("#switch_subscriptions_btn");
		this.eBtnSwitchSubscribers = this.element.find("#switch_subscribers_btn");
		this.eBtnGetChannels = this.element.find("#getchannels_btn");
		this.eBtnSubscribe = this.element.find("#subscribe_btn");
		this.eBtnUnsubscribe = this.element.find("#unsubscribe_btn");
		this.eBtnAuthenticate = this.element.find("#authenticate_btn");
		this.eBtnCreateChannel = this.element.find("#createchannel_btn");
		this.eBtnDeleteChannel = this.element.find("#deletechannel_btn");
		this.eBtnPublish = this.element.find("#publish_btn");
		
		// Radio elements
		this.eRbtnPublish = this.element.find("input[name=visibility]");
		
		// Combobox elements
		this.eCbSystem = this.element.find("input[name=system]");
		
		// Some other elements
		this.eChannelsArea = this.element.find("#channels_area");
		this.eSubscriptionsArea = this.element.find("#subscriptions_area");
		this.eSubscribersArea = this.element.find("#subscribers_area");
		this.eChannelsTable = this.element.find("#channels_table");
		this.eSubscriptionsTable = this.element.find("#subscriptions_table");
		this.eSubscribersTable = this.element.find("#subscribers_table");
		
		// Keeping a reference of the widget, when a websocket message
		// comes from the server the scope "this" doesnt exist anymore
		w.channels = this;
		w.channels.registerEvents( );
	},
	
	/**
	 * Registers all callbacks, and assigns all buttons and dom elements actions
	 * also starts up some specific things needed at the begining
	 **/
	registerEvents: function( ) {
		// The first time load by default channels area
		w.channels.switchChannelsArea( );
		
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnOpen: function( aEvent ) {},
			OnWelcome: function( aEvent ) {
				// Registering the callbacks for the channels
				mWSC.setChannelCallbacks({
					OnChannelCreated: w.channels.onChannelCreated,
					OnChannelRemoved: w.channels.onChannelRemoved,
					OnChannelsReceived: w.channels.onChannelsReceived,
					OnChannelUnsubscription: w.channels.onChannelUnsubscription,
					// When any subscription arrives from the server
					OnChannelSubscription: w.channels.onChannelSubscription
				});
				w.channels.getChannels( );
			},
			OnClose: function( ) {
				w.channels.destroy( );
			},
			OnGoodBye: function( aToken ) {
				w.channels.destroy( );
			},
			OnMessage: function( aEvent, aToken ) {
				if( mLog.isDebugEnabled ) {
					log( "<font style='color:#888'>jWebSocket '" + aToken.type 
						+ "' token received, full message: '" + aEvent.data + "' " 
						+ "</font>" );
				}
				w.channels.onMessage( aEvent, aToken );
			}
		};
		$( "#demo_box" ).auth( lCallbacks );
		
		// This function loads all click, blur and focus events for text fields
		w.channels.registerElementsEvents( );
		
		w.channels.eBtnSwitchChannels.click( w.channels.switchChannelsArea );
		w.channels.eBtnSwitchSubscriptions.click( w.channels.switchSubscriptionsArea );
		w.channels.eBtnSwitchSubscribers.click( w.channels.getSubscribers );
		
		w.channels.eBtnGetChannels.click( w.channels.getChannels );
		w.channels.eBtnCreateChannel.click( w.channels.createChannel );
		w.channels.eBtnDeleteChannel.click( w.channels.removeChannel );
		w.channels.eBtnSubscribe.click( w.channels.subscribeChannel );
		w.channels.eBtnUnsubscribe.click( w.channels.unsubscribeChannel );
		w.channels.eBtnAuthenticate.click( w.channels.auth );
		w.channels.eBtnPublish.click( w.channels.publish );
		w.channels.eTxtMessage.keypress( w.channels.messageKeypress );
	},
	
	/**
	 * Tries to obtain all available channels on the server, after the 
	 * request comes, the event w.channels.onChannelsReceived is fired
	 **/
	getChannels: function( ) {
		if( mWSC.isConnected( ) ) {
			log( "Trying to obtain channels..." );
			var lRes = mWSC.channelGetIds( );
			log( mWSC.resultToString( lRes ) );
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", 
				true, null, null, "error" );
		}
	},
	
	/**
	 * Try to create a new channel if have, after that the callback 
	 * w.channels.onChannelCreated is fired
	 **/ 
	createChannel: function( ) {
		if( mWSC.isConnected( ) ) {
			// Getting the data to create new channels
			var lChannelId = w.channels.eTxtChannelId.val( ),
			lChannelName = w.channels.eTxtChannelName.val( ),
			lAccessKey = w.channels.eTxtAccessKey.val( ),
			lSecretKey = w.channels.eTxtSecretKey.val( ),
			lIsSystem = (w.channels.eCbSystem.attr( "checked" ) == "checked" )?true:false,
			lIsPrivate = null;
		
			// Getting the radiobutton
			w.channels.eRbtnPublish.each( function( ) {
				if( $( this ).attr( "checked" ) ){
					lIsPrivate = ($( this ).val( ) == "private") ?true:false;
				}
			});
		
			// Validating the data to create the channel
			var lError = "";
			if( lChannelId == null || lChannelId == "" || 
				lChannelId == w.channels.MSG_CHANNELID ) {
				lError = w.channels.MSG_CHANNELID;
			}
			else if( lChannelName == null || lChannelName == "" || 
				lChannelName == w.channels.MSG_CHANNELNAME ) {
				lError = w.channels.MSG_CHANNELNAME;
			}
			else if( lSecretKey == null || lSecretKey == "" || 
				lSecretKey == w.channels.MSG_SECRETKEY ) {
				lError = w.channels.MSG_SECRETKEY;
			}
			else if( lAccessKey == null || lAccessKey == "" || 
				lAccessKey == w.channels.MSG_ACCESSKEY ) {
				lError = w.channels.MSG_ACCESSKEY;
			}
		
			if( lError != "" ) {
				dialog( " The field <b>" +
					lError + "</b> is required", 
					"jWebSocket error", true, null, null, "error" );
			} else{
				log( "Creating channel '" + lChannelId + "'..." );
				var lRes = mWSC.channelCreate(
					lChannelId,
					lChannelName,
					{
						isPrivate: lIsPrivate,
						isSystem: lIsSystem,
						accessKey: lAccessKey,
						secretKey: lSecretKey
					});
				log( mWSC.resultToString( lRes ) );
			}
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", 
				true, null, null, "error" );
		}
	},
	
	/**
	 * Removes a channel if have permission for that
	 * after that, the callback w.channels.onChannelRemoved is fired
	 **/
	removeChannel: function( ) {
		if( mWSC.isConnected( ) ) {
			var lChannelId = w.channels.eTxtChannelId.val( ),
			lAccessKey = w.channels.eTxtAccessKey.val( ),
			lSecretKey = w.channels.eTxtSecretKey.val( ),
			lError = null;
			
			// Validating the data to subscribe to a channel
			if( lChannelId == null || lChannelId == "" || 
				lChannelId == w.channels.MSG_CHANNELID ) {
				lError = w.channels.MSG_CHANNELID;
			} else if( lAccessKey == null || lAccessKey == "" || 
				lAccessKey == w.channels.MSG_ACCESSKEY ) {
				lError = w.channels.MSG_ACCESSKEY;
			} else if( lSecretKey == null || lSecretKey == "" || 
				lSecretKey == w.channels.MSG_SECRETKEY ) {
				lError = w.channels.MSG_SECRETKEY;
			}
			
			if( lError == null ) {
				log( "Removing channel '" + lChannelId + "'..." );
				var lRes = mWSC.channelRemove( 
					lChannelId,
					{
						accessKey: lAccessKey,
						secretKey: lSecretKey
					}
					);
				log( mWSC.resultToString( lRes ) );
			} else {
				dialog( "Incorrect value for <b>" + lError + "</b>. Please, check again",
					"jWebSocket error", true, null, null, "error" );
			}
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", 
				true, null, null, "error" );
		}
	},
	
	//Suscribe to the channel created for channels
	subscribeChannel: function( ) {
		if( mWSC.isConnected( ) ) {
			var lChannelId = w.channels.eTxtChannelId.val( ),
			lChannelName = w.channels.eTxtChannelName.val( ),
			lAccessKey = w.channels.eTxtAccessKey.val( ),
			lError = null;
			
			// Validating the data to subscribe to a channel
			if( lChannelId == null || lChannelId == "" || 
				lChannelId == w.channels.MSG_CHANNELID ) {
				lError = w.channels.MSG_CHANNELID;
			}
			else if( lAccessKey == null || lAccessKey == "" || 
				lAccessKey == w.channels.MSG_ACCESSKEY ) {
				lError = w.channels.MSG_ACCESSKEY;
			}
			
			if( lError == null ) {
				log( "Subscribing at channel '" + lChannelName + "'..." );
				var lRes = mWSC.channelSubscribe( lChannelId, lAccessKey );
				log( mWSC.resultToString( lRes ) );
			} else {
				dialog( "Incorrect value for <b>" + lError + "</b>. Please, check again",
					"jWebSocket error", true, null, null, "error" );
			}
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", 
				true, null, null, "error" );
		}
	},
	
	unsubscribeChannel: function( ) {
		if( mWSC.isConnected( ) ) {
			var lChannelId = w.channels.eTxtChannelId.val( );
			
			// Validating the data to subscribe to a channel
			if( lChannelId != null && lChannelId != "" && 
				lChannelId != w.channels.MSG_CHANNELID ) {
				log( "Unsubscribing from channel '" + lChannelId + "'..." );
				var lRes = mWSC.channelUnsubscribe( lChannelId );
				log( mWSC.resultToString( lRes ) );
			} else {
				dialog( "Incorrect value for <b>" + w.channels.MSG_CHANNELID +
					"</b>. Please, check again", "jWebSocket error", true, 
					null, null, "error" );
			}
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", true, 
				null, null, "error" );
		}
	},
	
	
	/**
	 * Try to authenticate against the channel to publish data
	 **/ 
	auth: function( ) {
		if( mWSC.isConnected( ) ) {
			var lChannelId = w.channels.eTxtChannelId.val( ),
			lAccessKey = w.channels.eTxtAccessKey.val( ),
			lSecretKey = w.channels.eTxtSecretKey.val( ),
			lError = null;
			
			// Validating the data to subscribe to a channel
			if( lChannelId == null || lChannelId == "" || 
				lChannelId == w.channels.MSG_CHANNELID ) {
				lError = w.channels.MSG_CHANNELID;
			} else if( lAccessKey == null || lAccessKey == "" || 
				lAccessKey == w.channels.MSG_ACCESSKEY ) {
				lError = w.channels.MSG_ACCESSKEY;
			} else if( lSecretKey == null || lSecretKey == "" || 
				lSecretKey == w.channels.MSG_SECRETKEY ) {
				lError = w.channels.MSG_SECRETKEY;
			}
			
			// If the user has typed the correct data, authenticate 
			// against the channel to publish information on it
			if( lError == null ) {
				log( "Authenticating against channel '" + lChannelId + "'..." );
				// use access key and secret key for this channel to authenticate
				// required to publish data only
				var lRes = mWSC.channelAuth( lChannelId, lAccessKey, lSecretKey );
				log( mWSC.resultToString( lRes ) );
			} else {
				dialog( "Incorrect value for <b>" + lError + "</b>. Please, check again",
					"jWebSocket error", true, null, null, "error" );
			}
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", true, 
				null, null, "error" );
		}
	},
	
	/**
	 * Try to obtain all subscribers for a certain channel
	 **/ 
	getSubscribers: function( ) {
		if( mWSC.isConnected( ) ) {
			var lChannelId = w.channels.eTxtChannelId.val( ),
			lAccessKey = w.channels.eTxtAccessKey.val( ),
			lError = null;
			
			// Validating the data to subscribe to a channel
			if( lChannelId == null || lChannelId == "" || 
				lChannelId == w.channels.MSG_CHANNELID ) {
				lError = w.channels.MSG_CHANNELID;
			} else if( lAccessKey == null || lAccessKey == "" || 
				lAccessKey == w.channels.MSG_ACCESSKEY ) {
				lError = w.channels.MSG_ACCESSKEY;
			}
			if( lError == null ){
				log( "Trying to obtain subscribers for channel '" 
					+ w.channels.mSelectedChannel + "'..." );
				var lRes = mWSC.channelGetSubscribers(
					lChannelId, lAccessKey );
				log( mWSC.resultToString( lRes ) );
			} else {
				dialog( "Incorrect value for <b>" + lError +
					"</b>. Please, check again", "jWebSocket error", true, 
					null, null, "error" );
			}
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", true, 
				null, null, "error" );
		}
	},
	
	publish: function( ) {
		if( mWSC.isConnected( ) ) {
			var lChannelId = w.channels.eTxtChannelId.val( ),
			lMessage = w.channels.eTxtMessage.val( ),
			lError = null;
			
			// Validating the data to subscribe to a channel
			if( lChannelId == null || lChannelId == "" || 
				lChannelId == w.channels.MSG_CHANNELID ) {
				lError = w.channels.MSG_CHANNELID;
			} else if( lMessage == null || lMessage == "" ) {
				lError = w.channels.MSG_MESSAGE;
			}
			if( lError == null ){
				mWSC.channelPublish( lChannelId, lMessage );
				w.channels.eTxtMessage.val( "" ).focus( );
			} else {
				dialog( "Incorrect value for <b>" + lError +
					"</b>. Please, check again", "jWebSocket error", true, 
					null, null, "error" );
			}
		} else{
			dialog( w.channels.MSG_NOTCONNECTED, "jWebSocket error", true, 
				null, null, "error" );
		}
	},
	
	// this method is called when a new channel has been created on the server
	onChannelCreated: function( aEvent ) {
		// TODO: when channel is created only arrives channelId and channelName 
		// in the event, so, still have to be implemented to send from the server 
		// all data from the created channel		
		w.channels.getChannels( );
	},

	// this method is called when a channel has been removed from the server
	onChannelRemoved: function( aEvent ) {
		w.channels.removeChannelFromTable( aEvent.channelId );
	},
	
	onChannelsReceived: function( aEvent ) {
		w.channels.destroy();
		w.channels.mChannelsList = {};
		// Put all channels in the table
		for( var lIdx = 0, lCnt = aEvent.channels.length; lIdx < lCnt; lIdx++ ) {
			w.channels.addChannelToTable( aEvent.channels[ lIdx ], w.channels.eChannelsTable );
			w.channels.mChannelsList[ aEvent.channels[ lIdx ].id ] = aEvent.channels[ lIdx ];
		}
	},
	
	/**
	 * Fired when a user subscribes to a channel you are subscribed already
	 **/
	onChannelSubscription: function( aEvent ) {
		w.channels.addSubscriberToTable( aEvent );
		w.channels.addChannelToTable( w.channels.mChannelsList[ aEvent.channelId ], w.channels.eSubscriptionsTable );
		w.channels.switchSubscriptionsArea( );
	},
	
	/**
	 * Fired when a user unsubscribes from a channel you are subscribed already
	 **/
	onChannelUnsubscription: function( aEvent ) {
		w.channels.removeSubscriberFromTable( aEvent );
		w.channels.removeSubscriptionFromTable( aEvent.channelId );
	},

	onChannelSubscribers: function( aToken ) {
		w.channels.clearSubscribersTable( );
		var lRow = null, lEnd = aToken.subscribers.length;
		if( lEnd > 0 ) {
			w.channels.eSubscribersTable
			.find( "." + w.channels.CLS_NOCHANNELS ).remove( );
			for( var i =0; i < lEnd; i++ ) {
				lRow = $( "<tr></tr>" ).append( "<td>" + 
					aToken.subscribers[ i ].id + "</td>");
				w.channels.eSubscribersTable.append( lRow );
			}
		}
		
		w.channels.eSubscribersTable.stripe( );
		w.channels.switchSubscribersArea( );
	},
	
	/**
	 * Executed every time the server sends a message to the client
	 * @param aEvent
	 * @param aToken
	 **/
	onMessage: function( aEvent, aToken ) {
		if( aToken ) {
			// is it a response from a previous request of this client?
			if( aToken.type == "response" ) {
				// If the request is an authorization from the channels
				if( aToken.reqType == "authorize" && aToken.code == 0) {
					
				} else if( aToken.reqType == "event" ) {
					if( /^(logout|disconnect)$/.test( aToken.name ) ) {
						w.channels.destroy( );
					}
				} else if ( aToken.reqType == "getSubscribers" ){
					w.channels.onChannelSubscribers( aToken );
				} else if ( aToken.reqType == "subscribe" ){
					w.channels.onChannelSubscription( aToken );
				} else if ( aToken.reqType == "unsubscribe" ){
					w.channels.onChannelUnsubscription( aToken );
				}
				// If anything went wrong in the server show information error
				if( aToken.code == -1 ){
					dialog( aToken.msg, "jWebSocket error", true, null, null, "error" );
				}
			}
		}
	},
	
	registerElementsEvents: function( ) {
		// On click, focus and blur for text fields
		w.channels.eTxtAccessKey.bind({
			"click focus": function( ) {
				if( $( this ).val( ) == w.channels.MSG_ACCESSKEY ) {
					$( this ).val( "" );
				}
			},
			"blur": function( ) {
				if( $( this ).val( ) == "" ) {
					$( this ).val( w.channels.MSG_ACCESSKEY );
				}
			}
		});
		w.channels.eTxtChannelId.bind({
			"click focus": function( ) {
				if( $( this ).val( ) == w.channels.MSG_CHANNELID ) {
					$( this ).val( "" );
				}
			},
			"blur": function( ) {
				if( $( this ).val( ) == "" ) {
					$( this ).val( w.channels.MSG_CHANNELID );
				}
			}
		});
		w.channels.eTxtSecretKey.bind({
			"click focus": function( ) {
				if( $( this ).val( ) == w.channels.MSG_SECRETKEY ) {
					$( this ).val( "" );
				}
			},
			"blur": function( ) {
				if( $( this ).val( ) == "" ) {
					$( this ).val( w.channels.MSG_SECRETKEY );
				}
			}
		});
		w.channels.eTxtChannelName.bind({
			"click focus": function( ) {
				if( $( this ).val( ) == w.channels.MSG_CHANNELNAME ) {
					$( this ).val( "" );
				}
			},
			"blur": function( ) {
				if( $( this ).val( ) == "" ) {
					$( this ).val( w.channels.MSG_CHANNELNAME );
				}
			}
		});
		w.channels.eTxtMessage.bind({
			"click focus": function( ) {
				if( $( this ).val( ) == w.channels.MSG_PUBLISHMESSAGE ) {
					$( this ).val( "" );
				}
			},
			"blur": function( ) {
				if( $( this ).val( ) == "" ) {
					$( this ).val( w.channels.MSG_PUBLISHMESSAGE );
				}
			}
		});
	},
	
	switchChannelsArea: function( ) {
		// Add and remove active class to the switch buttons
		w.channels.eBtnSwitchSubscriptions.attr( "class", "" );
		w.channels.eBtnSwitchSubscribers.attr( "class", "" );
		w.channels.eBtnSwitchChannels.attr( "class", w.channels.CLS_ACTIVE );
		
		// Hide and show areas
		w.channels.eSubscriptionsArea.fadeOut( 100 );
		w.channels.eSubscribersArea.fadeOut( 100 );
		w.channels.eChannelsArea.fadeIn( 100 );
	},
	
	switchSubscriptionsArea: function( ) {
		// Add and remove active class to the switch buttons
		w.channels.eBtnSwitchChannels.attr( "class", "" );
		w.channels.eBtnSwitchSubscribers.attr( "class", "" );
		w.channels.eBtnSwitchSubscriptions.attr( "class", w.channels.CLS_ACTIVE );
		
		// Hide and show areas
		w.channels.eChannelsArea.fadeOut( 100 );
		w.channels.eSubscribersArea.fadeOut( 100 );
		w.channels.eSubscriptionsArea.fadeIn( 100 );
	},
	
	switchSubscribersArea: function( ) {
		// Add and remove active class to the switch buttons
		w.channels.eBtnSwitchSubscriptions.attr( "class", "" );
		w.channels.eBtnSwitchChannels.attr( "class", "" );
		w.channels.eBtnSwitchSubscribers.attr( "class", w.channels.CLS_ACTIVE );
		
		// Hide and show areas
		w.channels.eChannelsArea.fadeOut( 100 );
		w.channels.eSubscriptionsArea.fadeOut( 100 );
		w.channels.eSubscribersArea.fadeIn( 100 );
	},
	
	/**
	 * Adds a row with the new channel to the channelsTable
	 * @param aTable The table that will receive the channel
	 * @param aChannel represents the channel with the following structure
	 * Example: 
	 * { 
	 *   id: "publicB", 
	 *   isPrivate: false, 
	 *   isSystem: false, 
	 *   name: "Public B" 
	 * }
	 **/
	addChannelToTable: function( aChannel, aTable ) {
		// Getting the last row of the channels table
		var lLastRow = aTable.find( "tr:last" ),
		lNewRow = $( "<tr></tr>" );

		if( lLastRow.attr( "class" ) ==  w.channels.CLS_NOCHANNELS ) {
			lLastRow.remove( );
		}

		lNewRow.append( $( "<td>" + aChannel.name + "</td>" ) );
		lNewRow.append( $( "<td>" + aChannel.id + "</td>" ) );
		lNewRow.append( $( "<td>" + 
			( ( aChannel.isPrivate )?
				w.channels.MSG_PRIVATE:w.channels.MSG_PUBLIC ) + "</td>" ) );
		lNewRow.append( $("<td></td>" )
			.attr( "class", ( aChannel.isSystem )?
				w.channels.CLS_ON:w.channels.CLS_OFF ) );
		
		// Registering click of each row
		lNewRow.click( function( ) {
			// Getting the text of the selected channel id
			var lChannelId = $( this ).children( ).first( ).next( ).text( ),
			lChannelName = $( this ).children( ).first( ).text( ),
			lType = $( this ).children( ).last( ).prev( ).text( ),
			lSystem = ($( this ).children( ).last( ).attr( "class" ) == 
				w.channels.CLS_ON)?true:false;
			
			// Remove any hover class from all tr elements except the header
			aTable.find( "tr" ).each( function( ) {
				if( $( this ).attr( "class" ) != w.channels.CLS_TH ) {
					if( $( this ).hasClass( w.channels.CLS_HOVER ) )
						$( this ).removeClass( w.channels.CLS_HOVER );
				}
			});
			// Adding class hover to the selected row
			$( this ).attr( "class", w.channels.CLS_HOVER );
			
			aTable.stripe( );
			
			// Updating text fields with the information of the selected channel
			w.channels.eTxtChannelName.val( lChannelName );
			w.channels.eTxtChannelId.val( lChannelId );
			w.channels.eRbtnPublish.each(function(){
				if( $( this ).val( ) == lType ){
					$( this ).attr( "checked", true );
				}
			});
			w.channels.eCbSystem.attr( "checked", lSystem );
			w.channels.mSelectedChannel = lChannelId;
		});
		
		// Adding the information row to the table
		aTable.append( lNewRow );
		aTable.stripe( );
	},
	
	addSubscriberToTable: function( aSubscriber ) {
		// Getting the last row of the table
		var lLastRow = w.channels.eSubscribersTable.find( "tr:last" ),
		lNewRow = $( "<tr></tr>" );

		if( lLastRow.attr( "class" ) ==  w.channels.CLS_NOCHANNELS ) {
			lLastRow.remove( );
		}

		lNewRow.append( $( "<td>" + aSubscriber.subscriber + "</td>" ) );
		
		// Adding the information row to the table
		w.channels.eSubscribersTable.append( lNewRow );
		w.channels.eSubscribersTable.stripe( );
	},
	
	/**
	 * Removes a channel in the channels table
	 */
	removeChannelFromTable: function( aChannelId ) {
		var lChannelId = null,
		lRow = null;
		w.channels.eChannelsTable.find( "tr" ).each( function( ) {
			lRow = $( this );
			// Don't check in the header of the table
			if( !lRow.hasClass( w.channels.CLS_TH ) ) {
				// Getting the channel id cell
				lChannelId = lRow.children( ).first( ).next( ).text( );
				if( lChannelId == aChannelId ) {
					lRow.remove( );
					w.channels.eChannelsTable.stripe( );
					return;
				}
			}
		});
	},
	
	/**
	 * Removes a subscription in the subscriptions table
	 */
	removeSubscriptionFromTable: function( aChannelId ) {
		var lChannelId = null,
		lRow = null, lAllRows = w.channels.eSubscriptionsTable.find( "tr" );
		lAllRows.each( function( ) {
			lRow = $( this );
			// Don't check in the header of the table
			if( !lRow.hasClass( w.channels.CLS_TH ) ) {
				// Getting the channel id cell
				lChannelId = lRow.children( ).first( ).next( ).text( );
				if( lChannelId == aChannelId ) {
					lRow.remove( );
					w.channels.eSubscriptionsTable.stripe( );
				}
			}
		});
		lAllRows = w.channels.eSubscriptionsTable.find( "tr" );
		if( lAllRows.length <= 1 ) {
			var lNoSubscriptionsRow = $( "<tr class='" + 
				w.channels.CLS_NOCHANNELS + "'></tr>" );
			lNoSubscriptionsRow.append( $( "<td rowspan='4'>" + 
				w.channels.MSG_NOSUBSCRIPTIONS + "</td>" ) );
			w.channels.eSubscriptionsTable.append( lNoSubscriptionsRow );
		}
	},
	
	/**
	 * Removes a subscriber in the subscribers table
	 */
	removeSubscriberFromTable: function( aSubscriber ) {
		if( aSubscriber.subscriberId ) {
			var lSubscriberId = null,
			lRow = null, lAllRows = w.channels.eSubscribersTable.find( "tr" );
			lAllRows.each( function( ) {
				lRow = $( this );
				// Don't check in the header of the table
				if( !lRow.hasClass( w.channels.CLS_TH ) ) {
					// Getting the channel id cell
					lSubscriberId = lRow.children( ).first( ).text( );
					if( lSubscriberId == aSubscriber.subscriberId ) {
						lRow.remove( );
						w.channels.eSubscribersTable.stripe( );
					}
				}
			});
		} else {
			var lChannelId = aSubscriber.channelId;
			w.channels.eSubscribersTable.find( "#" + lChannelId ).remove( );
		}
		lAllRows = w.channels.eSubscribersTable.find( "tr" );
		if( lAllRows.length <= 1 ) {
			var lNoSubscribersRow = $( "<tr class='" + 
				w.channels.CLS_NOCHANNELS + "'></tr>" );
			lNoSubscribersRow.append( $( "<td rowspan='4'>" + 
				w.channels.MSG_NOSUBSCRIBERS + "</td>" ) );
			w.channels.eSubscribersTable.append( lNoSubscribersRow );
		}
	},
	
	messageKeypress: function( aKeyEvent ) {
		if( aKeyEvent ) {
			if( aKeyEvent.keyCode ) {
				if( aKeyEvent.keyCode == 13 ) {
					w.channels.publish( );
				}
			} else if( aKeyEvent.keyChar ) {
				if( aKeyEvent.keyChar == 13 ) {
					w.channels.publish( );
				}
			}
		}
	},
	
	/**
	 * Removes a subscription in the subscriptions table
	 */
	removeSubscriptionFromTable: function( aChannelId ) {
		var lChannelId = null,
		lRow = null, lAllRows = w.channels.eSubscriptionsTable.find( "tr" );
		lAllRows.each( function( ) {
			lRow = $( this );
			// Don't check in the header of the table
			if( !lRow.hasClass( w.channels.CLS_TH ) ) {
				// Getting the channel id cell
				lChannelId = lRow.children( ).first( ).next( ).text( );
				if( lChannelId == aChannelId ) {
					lRow.remove( );
					w.channels.eSubscriptionsTable.stripe( );
				}
			}
		});
		lAllRows = w.channels.eSubscriptionsTable.find( "tr" );
		if( lAllRows.length <= 1 ) {
			var lNoSubscriptionsRow = $( "<tr class='" + 
				w.channels.CLS_NOCHANNELS + "'></tr>" );
			lNoSubscriptionsRow.append( $( "<td rowspan='4'>" + 
				w.channels.MSG_NOSUBSCRIPTIONS + "</td>" ) );
			w.channels.eSubscriptionsTable.append( lNoSubscriptionsRow );
		}
	},
	
	/**
	 * Removes a subscriber in the subscribers table
	 */
	removeSubscriberFromTable: function( aSubscriber ) {
		var lSubscriberId = null,
		lRow = null, lAllRows = w.channels.eSubscribersTable.find( "tr" );
		lAllRows.each( function( ) {
			lRow = $( this );
			// Don't check in the header of the table
			if( !lRow.hasClass( w.channels.CLS_TH ) ) {
				// Getting the channel id cell
				lSubscriberId = lRow.children( ).first( ).text( );
				if( lSubscriberId == aSubscriber ) {
					lRow.remove( );
					w.channels.eSubscribersTable.stripe( );
				}
			}
		});
		lAllRows = w.channels.eSubscribersTable.find( "tr" );
		if( lAllRows.length <= 1 ) {
			var lNoSubscribersRow = $( "<tr class='" + 
				w.channels.CLS_NOCHANNELS + "'></tr>" );
			lNoSubscribersRow.append( $( "<td rowspan='4'>" + 
				w.channels.MSG_NOSUBSCRIBERS + "</td>" ) );
			w.channels.eSubscribersTable.append( lNoSubscribersRow );
		}
	},
	
	clearChannelTable: function( ) {
		w.channels.eChannelsTable.find( "tr" ).each( function( ) {
			if( $( this ).attr( "class" ) != w.channels.CLS_TH ) {
				$( this ).remove( );
			}
		});
		
		var lNoChannelsRow = null;
		lNoChannelsRow = $( "<tr class='" + w.channels.CLS_NOCHANNELS + 
			"'></tr>" );
		lNoChannelsRow.append( $( "<td rowspan='4'>" + 
			w.channels.MSG_NOCHANNELS + "</td>" ) );
		
		w.channels.eChannelsTable.append( lNoChannelsRow );
		w.channels.eCbSystem.attr( "checked", false );
		w.channels.mSelectedChannel = null;
	},
	
	clearSubscribersTable: function( ) {
		w.channels.eSubscribersTable.find( "tr" ).each( function( ) {
			if( $( this ).attr( "class" ) != w.channels.CLS_TH ) {
				$( this ).remove( );
			}
		});
		
		var lNoSubscribersRow = null;
		lNoSubscribersRow = $( "<tr class='" + 
			w.channels.CLS_NOCHANNELS + "'></tr>" );
		lNoSubscribersRow.append( $( "<td rowspan='4'>" + 
			w.channels.MSG_NOSUBSCRIBERS + "</td>" ) );
		
		w.channels.eSubscribersTable.append( lNoSubscribersRow );
	},
	
	clearTextFields: function( ) {
		w.channels.eTxtAccessKey.val( w.channels.MSG_ACCESSKEY );
		w.channels.eTxtChannelId.val( w.channels.MSG_CHANNELID );
		w.channels.eTxtSecretKey.val( w.channels.MSG_SECRETKEY );
		w.channels.eTxtChannelName.val( w.channels.MSG_CHANNELNAME );
		w.channels.eTxtMessage.val( w.channels.MSG_PUBLISHMESSAGE );
	},
	
	destroy: function( ) {
		w.channels.clearChannelTable( );
		w.channels.clearTextFields( );
	}
});

/**
 * Creating an extension to stripe tables
 * The simplest way to use is $("#anytable").stripe( );
 * Note: this is especially adapted to the structure of the tables of this demo
 */
(function($){
	$.fn.stripe = function( ) {
		var lTable = this,
		lRow = null,
		lWhiteStripe = true;
		
		lTable.find( "tr" ).each( function( ) {
			lRow = $( this );
			if( !lRow.hasClass( w.channels.CLS_TH ) && 
				!lRow.hasClass( w.channels.CLS_HOVER ) ) {
				if( lWhiteStripe ) {
					lRow.attr( "class", "" );
					lWhiteStripe = false;
				} else {
					lRow.attr( "class", w.channels.CLS_STRIPE );
					lWhiteStripe = true;
				}
			}
		});
	};
})(jQuery);
