//	---------------------------------------------------------------------------
//	jWebSocket - Chat Plug-In
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
$.widget( "jws.chat", {
	_init:function( ) {
		w.chat = this;
		
		w.chat.mIsPublicActive = false;
		w.chat.mUserColors = {
			// Color RED for the system logs
			SYS: "#FF0000",
			// Color BLUE for the personal logs
			USR: "#0000FF"
		};
		w.chat.mSelectionStart = null;
		w.chat.mSelectionEnd = null;
		
		w.chat.mOnlineClients = [];
		w.chat.mPrivateClients = {};
		
		w.chat.mPrivateUnreadMessages = 0;
		
		w.chat.mEmoticons = {
			// Faces with 2 chars
			":)": "smile", 
			":(": "sad",
			":o": "surprise",
			":d": "smile-big",
			"<3": "heart",
			":p": "raspberry",
			":]": "embarrassed",
			":{": "sick",
			"+1": "opinion-agree",
			"-1": "opinion-disagree",
			";)": "wink",
			
			// Faces with three chars
			"</3": "heart-broken",
			"(r)": "rose",
			":;(": "crying",
			":?": "confused",
			">(-": "jWebSocketLogo"
		};
		w.chat.mEmoticonsPath = "css/images/emoticons/";
		
		// Here will be stored the current ID that you are chatting with
		w.chat.mPrivateChatWith = null;
		
		w.chat.mAuthenticatedUser = null;
		
		// Default namespace for demos org.jwebsocket.plugins.chat
		w.chat.NS = jws.NS_BASE + ".plugins.chat";
		w.chat.mNextWindowId = 1;
		
		w.chat.MSG_NOT_CONNECTED_USERS = "There are not connected users";
		
		
		
		// MESSAGEBOX
		w.chat.eMessageBoxArea	= w.chat.element.find( "#message_box" );
		w.chat.eMessageBox		= w.chat.element.find( "#message_box_text" );
		
		w.chat.eChatTitle		= w.chat.element.find( "#chat_box_header" );
		
		w.chat.ePublicUsersBox	= w.chat.element.find( "#public_users_box_body" );
		w.chat.ePrivateUsersBox = w.chat.element.find( "#users_box_body" );
		
		w.chat.eLogPrivate		= w.chat.element.find( "#chat_box_history .private" );
		w.chat.eLogPublic		= w.chat.element.find( "#chat_box_history .public" );
		
		// Area to show the new incoming messages
		w.chat.eUnreadMessages  = w.chat.element.find( "#new_message_notification" );
		
		// BUTTONS
		w.chat.eBtnPrivateChat	= w.chat.element.find( "#btn_private_chat" );
		w.chat.eBtnPublicChat	= w.chat.element.find( "#btn_public_chat" );
		w.chat.eBtnBroadcast	= w.chat.element.find( "#message_box_broadcast_btn" );
		w.chat.eBtnClear		= w.chat.element.find( "#message_box_clear_btn" );
		w.chat.eBtnNewChatWindow= w.chat.element.find( "#new_box_chat_btn" );
		
		// EMOTICONS
		w.chat.eEmoticonsWindow	= w.chat.element.find( "#select_emoticon_window" );
		w.chat.eBtnActiveEmoticon = w.chat.element.find( "#selected_emoticon" );
		w.chat.eBtnMoreEmoticons  = w.chat.element.find( "#show_more_emoticons" );
		w.chat.eBtnCloseEmoticonWindow  = w.chat.element.find( "#close_emoticon_window" );
		
		// Executing some init functions
		//		w.chat.switchPrivate( );
		w.chat.switchPublic( );
		
		w.chat.eUnreadMessages.hide();
		w.chat.eEmoticonsWindow.hide();
		w.chat.loadEmoticons();
		
		w.chat.registerEvents( );
	},
    
	registerEvents: function( ) {
		//MESSAGE BOX EVENTS
		w.chat.eMessageBox.click( w.chat.messageBoxClick );
		w.chat.eMessageBox.blur( w.chat.messageBoxBlur );
		w.chat.eMessageBox.keypress( w.chat.messageBoxKeyPressed );
		w.chat.eMessageBox.focus( w.chat.messageBoxClick );
		
		//CHAT BOX SWITCHER
		w.chat.eBtnPrivateChat.click( w.chat.switchPrivate );
		w.chat.eBtnPublicChat.click( w.chat.switchPublic );
		
		// BUTTONS ACTIONS
		w.chat.eBtnBroadcast.click( w.chat.sendMessage );
		w.chat.eBtnClear.click( w.chat.clearChatLog );
		
		w.chat.eBtnNewChatWindow.click( w.chat.openNewChatWindow );
		
		// EMOTICONS
		w.chat.eBtnMoreEmoticons.click( function(){
			w.chat.eEmoticonsWindow.show();
		});
		
		// Disable all elements in the chat window
		//		w.chat.eMessageBoxArea.children().each(function(){
		//			$( this ).attr( "disabled", true );
		//		});
		
		w.chat.eBtnActiveEmoticon.click( w.chat.addEmoticon );
		
		w.chat.eBtnCloseEmoticonWindow.click( function(){
			w.chat.eEmoticonsWindow.hide();
		});
		
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnOpen: function( aEvent ) {
				// Enabling all elements in the chat window again
				w.chat.eMessageBoxArea.children().each(function(){
					$( this ).attr("disabled", false);
				});
			},
			OnWelcome: function( aEvent )  {
			},
			OnGoodBye: function( aEvent )  {
			},
			OnMessage: function( aEvent, aToken ) {
				w.chat.processToken( aEvent, aToken );
			},
			OnClose: function( aEvent ) {
				w.chat.cleanAll();
				w.chat.eMessageBoxArea.children().each(function(){
					$( this ).attr("disabled", true);
				});
			}
		};
		$("#demo_box").auth(lCallbacks);
	},
	
	openNewChatWindow: function(){
		window.open(
			// "http://www.jwebsocket.org/demos/jwsChat/jwsChat.htm"
			"jwsChat.htm",
			"chatWindow" + w.chat.mNextWindowId,
			"width=720,height=700,left=" + (50 + w.chat.mNextWindowId * 30) + ", top=" + (50 + w.chat.mNextWindowId * 25)
			);
		w.chat.mNextWindowId++;
		if( w.chat.mNextWindowId > 10 ) {
			w.chat.mNextWindowId = 1;
		}
	},
	sendMessage: function( ) {
		if( mWSC.isConnected() ){
			if(w.chat.mOnlineClients.length > 0){
				var lMessage = w.chat.eMessageBox.val();
				if( lMessage &&  "Type your message..." != lMessage ){
					if( w.chat.mIsPublicActive ){
						w.chat.sendBroadcastMessage( lMessage );
					} else {
						w.chat.sendPrivateMessage( w.chat.mPrivateChatWith, lMessage );
					}
				}
				w.chat.eMessageBox.val("").focus();
			}else {
				dialog("You can not send messages. All users are disconnected", 
					"No users online", false, null, null, "alert");
			}
		} else{
			dialog("Sorry, you are not connected with jWebSocket server, \n\
						try clicking in the login button!", 
				"Not connected", false, null, null, "alert");
		}
	},
	
	sendBroadcastMessage: function( aMessage ) {
		// Preparing broadcast Token
		var lBroadcastToken = {
			ns: w.chat.NS,
			type: "broadcast",
			msg: aMessage
		};
		// If no message is defined an error comes from the server
		var lCallbacks = {
			OnFailure: function( aToken ){
				dialog( aToken.msg, "Empty Message" );
			}
		};
		// Sending the broadcast message to the server
		mWSC.sendToken( lBroadcastToken, lCallbacks );
	},
	
	sendPrivateMessage: function( aTargetId, aMessage ) {
		if( aTargetId ){
			var lArray = w.chat.mPrivateChatWith.split("@");
			var lTargetId = lArray[ 1 ];
			
			if( w.chat.isUserOnline( lTargetId ) ){
				if( aMessage != null || aMessage == ""){
					// Preparing broadcast Token
					var lMessageToken = {
						ns: w.chat.NS,
						type: "messageTo",
						msg: aMessage,
						targetId: lTargetId
					};
					// If no message is defined an error comes from the server
					var lCallbacks = {
						OnFailure: function( aToken ){
							dialog( aToken.msg, "The message could not be sent", 
								false, null, null, "alert" );
						}
					};
					// Sending the broadcast message to the server
					mWSC.sendToken( lMessageToken, lCallbacks );
					// w.chat.logChatMessage( aClientId, aMessage, aIsPrivate );
					var lOnlineId = mWSC.getUsername() + "@" + mWSC.getId();
					w.chat.logChatMessage( lOnlineId, aMessage, true );
				}
			}else {
				dialog( "The user you are chatting with is offline", 
					"User offline",
					false, null, null, "alert" );
			}
			
			
		}
	},
	
	processToken: function( aEvent, aToken ) {
		if( aToken ) {
			// is it a response from a previous request of this client?
			if( aToken.type == "response" ) {
				// figure out of which request
				if( aToken.reqType == "login" ) {
					if( aToken.code == 0 ) {
						// logChatMessage( aID, aString )
						w.chat.logChatMessage( "SYS", "Welcome '" + aToken.username + "'" );
						w.chat.mAuthenticatedUser = aToken.username + "@" + aToken.sourceId;
						// Sending a register token to the server
						var lRegister = {
							ns: w.chat.NS,
							type: "register"
						};
						mWSC.sendToken( lRegister );
						
						// select message field for convenience
						w.chat.eMessageBox.focus();
					}
				}
			// is it an event w/o a previous request ?
			} else if( aToken.type == "getChatClients" ) {
				w.chat.setClients( aToken.clients );
			} else if( aToken.type == "event" ) {
				if( "logout" == aToken.name || "disconnect" == aToken.name ){
					w.chat.removeClient( aToken.sourceId );
				}
			} else if( aToken.type == "goodBye" ) {
				w.chat.logChatMessage( "SYS", "Chat PlugIn says good bye (reason: " + aToken.reason + ")!" );
				
			// is it any token from another client
			} else if( aToken.type == "broadcast" ) {
				if( aToken.msg && aToken.sourceId ) {
					//logChatMessage( aID, aString )
					w.chat.logChatMessage( aToken.sourceId, aToken.msg );
				}
			} else if( aToken.type == "newClientConnected" ){
				w.chat.addClient( aToken.sourceId );
			} else if( aToken.type == "messageTo" ) {
				w.chat.onPrivateMessage( aToken );
			}
		}
	},
	
	onPrivateMessage: function( aToken ){
		// If the user is in the public area, hint new message incoming
		if( w.chat.mIsPublicActive ){
			var lQuantity = w.chat.eUnreadMessages.text();
			
			w.chat.eUnreadMessages.text( parseInt(parseInt(lQuantity) + 1) )
			.fadeIn( 300 ).fadeOut(100).fadeIn(300).fadeOut(100).fadeIn(100);
			
			w.chat.addPrivateTab( aToken.sourceId );
			
		} else{
			// If the user is standing in the private area
			// add a Class to the unread tabs messages
			w.chat.addPrivateTab( aToken.sourceId );
			w.chat.notifyTabWithMessage( aToken.sourceId );
		}
		w.chat.logChatMessage( aToken.sourceId, aToken.msg, true );
	},
	
	addPrivateTab: function( aSourceId ){
		var lMsgNoUsersOnline = w.chat.ePrivateUsersBox.find('.no_users_online');
		if( lMsgNoUsersOnline ){
			lMsgNoUsersOnline.remove();
		}
		
		var lFullId  = aSourceId.split("@");
		var lId  = lFullId[1];
		
		var lTab = null;
		w.chat.ePrivateUsersBox.children().each( function(){
			if( "_" + lId == $( this ).attr( "id" )){
				lTab = $( this );
			}
		});
		
		// If exists the tab, don't create it
		if( !lTab ){
			var lPrivateItem = $('<div class="online" id="_'+ lId + '">'+ 
				'<div class="client_id">' + aSourceId +'</div></div>');
			
			var lCloseButton = $('<div class="remove_private_client">x</div>');
			lCloseButton.click(function(){
				$( '.tooltip' ).hide().remove();
				var lNext = lPrivateItem.next();
				var lPrev = lPrivateItem.prev();
				
				if ( lNext.get(0) ){
					w.chat.setTabActive( lNext.find('.client_id').text() );
				} else if( lPrev.get(0) ){
					w.chat.setTabActive( lPrev.find('.client_id').text() );
				} else{
					w.chat.startConversationWith( null );
				}
				lPrivateItem.remove();
			});
			
			lPrivateItem.append( lCloseButton );
			
			lPrivateItem.click( function(){
				w.chat.startConversationWith( aSourceId );
			} );
			
			var lColor = w.chat.getClientColor( aSourceId );
			lPrivateItem.css({
				color: lColor
			});
		
			lPrivateItem.click( function(){
				w.chat.addPrivateTab( aSourceId );
				w.chat.startConversationWith( aSourceId );
			});
			
			w.chat.ePrivateUsersBox.append( lPrivateItem );
		}
	
	},
	
	setTabActive: function( aClientId ){
		var lFullId  = aClientId.split("@");
		var lId  = lFullId[1];
		
		w.chat.ePrivateUsersBox.children().each( function(){
			if( "_" + lId == $( this ).attr( "id" )){
				$( this ).addClass( "active" ).removeClass("new_message");
			} else{
				$( this ).removeClass( "active" );
			}
		});
	},
	
	notifyTabWithMessage: function( aSourceId ){
		if( aSourceId != w.chat.mPrivateChatWith ){
			var lFullId  = aSourceId.split("@");
			var lId  = lFullId[1];
		
			var lTab = null;
			w.chat.ePrivateUsersBox.children().each( function(){
				if( "_" + lId == $( this ).attr( "id" )){
					lTab = $( this );
				}
			});
			if( lTab.hasClass( "new_message" ) ){
				lTab.fadeTo(300, 0.25).fadeTo( 300, 0.80 )
				.fadeTo(300, 0.25).fadeTo( 300, 1 );
			}else {
				lTab.addClass( "new_message" );
			}
		} else{
			
		}
		
	},
	
	startConversationWith: function( aClientId ){
		if ( aClientId ){
			w.chat.mPrivateChatWith = aClientId;
		
			w.chat.eChatTitle.html( "User: " + "<b>" + aClientId + "</b>");
			
			// Change to the private area
			if( w.chat.mIsPublicActive ){
				w.chat.switchPrivate();
			}
			
			// Load the history for this user
			w.chat.switchHistory( aClientId );
			
			// Enable the tab of the client you want to chat with
			w.chat.setTabActive( aClientId );
			
			// Set the cursor to start typing
			w.chat.eMessageBox.focus();
		} else {
			w.chat.eChatTitle.html( "User: ?" ); 
			w.chat.switchHistory( null );
		}
	},
	
	isUserOnline: function( lTargetId ){
		for( var lIndex in w.chat.mOnlineClients){
			if( w.chat.mOnlineClients[ lIndex ] == lTargetId ){
				return true;
			}
		}
		return false;
	},
	
	/**
	 * Changes the history of the chat window between two users
	 **/
	switchHistory: function( aClientId ){
		if( aClientId ){
			var lHistory = w.chat.mPrivateClients[ aClientId ];
		
			if( lHistory ){
				w.chat.eLogPrivate.html( lHistory );
			} else{
				w.chat.eLogPrivate.html("");
			}
		} else{
			w.chat.eLogPrivate.html("");
		}
	},
	
	/**
	 * Logs a message in the public window
	 **/
	logChatMessage: function( aClientId, aMessage, aIsPrivate ) {
		// set a default user name if not yet logged in
		if( !aClientId ) {
			aClientId = mWSC.getUsername() + "@" + mWSC.getId();
		}
		var lHistoryItem = $( '<div class="history"></div>' );
		
		var lEUsername = $( '<div class="title">'+ aClientId + ": " + '</div>' );
		
		var lColor = w.chat.getClientColor( aClientId );
		
		if( lColor != null ){
			lEUsername.css({
				"color": lColor
			});
		}
		
		lHistoryItem.append( lEUsername );
		
		var lParsedMessage = w.chat.parseEmoticons( aMessage );
		
		lHistoryItem.append( '<div class="text">'+ lParsedMessage +'</div>' );
		
		// Save the history of all private messages
		if( aIsPrivate ){
			var lPrivateHistory = $( '<div></div>' ).append( lHistoryItem );
			if( w.chat.mPrivateChatWith == aClientId || 
				aClientId == w.chat.mAuthenticatedUser ){
				
				w.chat.eLogPrivate.append( lPrivateHistory );
				// Save conversation in history
				if( w.chat.mPrivateClients[ w.chat.mPrivateChatWith ] ){
					w.chat.mPrivateClients[ w.chat.mPrivateChatWith ] += 
					lPrivateHistory.html();
				} else{
					w.chat.mPrivateClients[ w.chat.mPrivateChatWith ] = 
					lPrivateHistory.html();
				}
			} else{
				// If you just pressed send button or the message 
				// comes from someone you are chatting with
				// Save conversation in history
				if( w.chat.mPrivateClients[ aClientId ] ){
					w.chat.mPrivateClients[ aClientId ] += lPrivateHistory.html();
				} else{
					w.chat.mPrivateClients[ aClientId ] = lPrivateHistory.html();
				}
			}
		} else{
			w.chat.eLogPublic.append( lHistoryItem );
		}
	},
	
	/**
	 * Sets the list of connected clients
	 * @param aClients 
	 *  The list of clients to be shown to the users
	 **/
	setClients: function( aClients ){
		w.chat.ePublicUsersBox.html( "" );
		
		if( aClients.length > 0 ){
			$( aClients ).each(function( aIndex, aClientId ){
				var lFullId  = aClientId.split("@");
				var lId  = lFullId[1];
			
				var lPublicItem = $('<div class="public_online" id="'+ lId +
					'">'+ aClientId +'</div>');
				
				// Use a default color for each client
				var lColor = w.chat.getClientColor( aClientId );
				
				lPublicItem.css({
					color: lColor
				});
				
				lPublicItem.click( function(){
					// Load the tab
					w.chat.addPrivateTab( aClientId );
					// Start a new conversation with this client
					w.chat.startConversationWith( aClientId );
				} );
				
				w.chat.ePublicUsersBox.append( lPublicItem );
				
				w.chat.mOnlineClients.push(lId);
			});
		} else {
			w.chat.ePublicUsersBox.html("").append(
				'<div class="no_users_online">' + w.chat.MSG_NOT_CONNECTED_USERS + 
				'</div>');
		}
	},
	
	/**
	 * Sets the list of connected clients
	 * @param aClients 
	 *  The list of clients to be shown to the users
	 **/
	addClient: function( aClientId ){
		// if there are not users online remove the sign no_users_online
		var lENoUsersOnline = w.chat.ePublicUsersBox.find('.no_users_online');
		
		if( lENoUsersOnline ){
			lENoUsersOnline.remove();	
		}
		var lArray = aClientId.split("@");
		var lClientId = lArray[1];
		
		var lPublicItem = $("<div class='public_online' id='"+ lClientId +"'>" + 
			aClientId +"</div>");
		
		// Registering clicks of elements
		var lColor = w.chat.getClientColor( aClientId );
		
		lPublicItem.css({
			color: lColor
		});
		
		lPublicItem.click( function(){
			// Load the tab
			w.chat.addPrivateTab( aClientId );
			// Start a new conversation with this client
			w.chat.startConversationWith( aClientId );
		} );
		
		w.chat.ePublicUsersBox.append( lPublicItem );
		
		w.chat.mOnlineClients.push( lClientId );
	},
	
	/**
	 * Removes a client from the clients list
	 * @param aClients 
	 *  The list of clients to be shown to the users
	 **/
	removeClient: function( aClientId ){
		if( w.chat.mOnlineClients ) {
			for( var lIdx = 0; lIdx < w.chat.mOnlineClients.length; lIdx++ ) {
				if( aClientId  ==  w.chat.mOnlineClients[ lIdx ] ) {
					w.chat.mOnlineClients.splice( lIdx, 1 );
					break;
				}
			}

			w.chat.ePublicUsersBox.children().each(function(){
				if( $( this ).attr("id") == aClientId ){
					$( this ).remove();
				}
			});
			
			w.chat.ePrivateUsersBox.children().each(function(){
				if( $( this ).attr("id") == "_" + aClientId ){
					$( this ).removeClass( "online" ).addClass( "offline" )
					.attr( "title", "User offline" );
					refreshTooltips();
				}
			});
			
			if(w.chat.mOnlineClients.length <= 0){
				w.chat.ePublicUsersBox.html("").append( 
					'<div class="no_users_online">'+ w.chat.MSG_NOT_CONNECTED_USERS + 
					'</div>' );
			}
		}
	},
	
	/**
	 * Removes all elements from the clients list
	 **/
	cleanAll: function(){
		if( w.chat.mOnlineClients ) {
			w.chat.mOnlineClients = [];
		}
		w.chat.ePublicUsersBox.html("").append(
			'<div class="no_users_online">'+ w.chat.MSG_NOT_CONNECTED_USERS +'</div>');
		
		w.chat.ePrivateUsersBox.html("").append(
			'<div class="no_users_online">'+ w.chat.MSG_NOT_CONNECTED_USERS +'</div>');
			
		w.chat.eLogPublic.html("");
		w.chat.eLogPrivate.html("");
	},
	
	clearChatLog: function(){
		if( w.chat.mIsPublicActive ){
			w.chat.eLogPublic.html("");
		} else {
			w.chat.eLogPrivate.html("");
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
				lColor = w.chat.getRandomColor( 12 );
			} while( w.chat.isColorUsed( lColor ) );
			
			w.chat.mUserColors[ aClientID ] = w.chat.mUserColors[ aClientID ] || lColor;
			
			return w.chat.mUserColors[ aClientID ];
		}
		return null;
	},
	
	/**
	 * Removes a defined color for a given clientID
	 * @param aClientID
	 **/
	removeClientColor: function( aClientID ){
		if( w.chat.mUserColors ) {
			for( var lIdx = 0; lIdx < w.chat.mUserColors.length; lIdx++ ) {
				for(var lElem in w.chat.mUserColors[ lIdx ]){
					if( aClientID ==  lElem) {
						w.chat.mUserColors.splice( lIdx, 1 );
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
		$( w.chat.mUserColors ).each( function( aIndex, aElem ){
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
		
		var lRed = lColorChars[ w.chat.getRandomNumber( aIntensity ) ] + 
		lColorChars[ w.chat.getRandomNumber( aIntensity ) ];
	
		var lGreen = lColorChars[ w.chat.getRandomNumber( aIntensity ) ] + 
		lColorChars[ w.chat.getRandomNumber( aIntensity ) ];
	
		var lBlue = lColorChars[ w.chat.getRandomNumber( aIntensity ) ] + 
		lColorChars[ w.chat.getRandomNumber( aIntensity ) ];

		return "#"+lRed + lGreen + lBlue;
	},
	getRandomNumber: function( aNumber ){
		return Math.floor(Math.random(aNumber) * aNumber);
		
	},
	
	/**
	 * Replaces all faces( emoticons ) by their respective image
	 * @param aMessage
	 */
	parseEmoticons: function( aMessage ){
		// TODO: Implement a better structure for this
		var lParsedMessage = "";
		for( var lIndex = 0; lIndex < aMessage.length; lIndex++ ){
			var lImage = null;
			if( ( aMessage.length - lIndex ) >= 2 ){
				var l2CharsSymbol = ( aMessage[ lIndex ] + aMessage[ lIndex + 1 ] )
				.toLowerCase();
				lImage = w.chat.mEmoticons[ l2CharsSymbol ];
				
				var l3CharsSymbol = null;
				
				if( !lImage ){
					if( (aMessage.length - lIndex ) >= 3 ){
						l3CharsSymbol = ( aMessage[ lIndex ] + aMessage[ lIndex + 1 ] 
							+ aMessage[ lIndex + 2 ]).toLowerCase();
						lImage = w.chat.mEmoticons[ l3CharsSymbol ];
						if( lImage ){
							lIndex += 2;
						}
					}
				} else{
					lIndex++;
				}
			}
			if( lImage ){
				lParsedMessage += "<img src='" + w.chat.mEmoticonsPath + 
				lImage + ".png" + "' title='"+ lImage + "'>";
			} else{
				lParsedMessage += aMessage[ lIndex ];
			}
		}
		return lParsedMessage;
	},
	
	loadEmoticons: function(){
		//		w.chat.eEmoticonsWindow.hide();
		w.chat.eEmoticonsWindow.children().each( function(){
			var lId = $( this ).attr( "id" );
			if( $( this ).hasClass("emoticon_btn") ){
				var lNormalStyle = {
					"background": "url(css/images/emoticons/" + lId +
					".png) no-repeat"
				};
				var lHoverStyle = {
					"background": "url(css/images/emoticons/" + lId +
					"_h.png) no-repeat"
				};
			
				var lPressedStyle = {
					"background": "url(css/images/emoticons/" + lId +
					"_p.png) no-repeat"
				};
				$( this ).css(lNormalStyle);
				$( this ).mouseover(function(){
					$( this ).css(lHoverStyle);
				});
				$( this ).mouseout(function(){
					$( this ).css(lNormalStyle);
				});
				$( this ).mousedown(function(){
					$( this ).css(lPressedStyle);
				});
				$( this ).click( function(){
					w.chat.eBtnActiveEmoticon.css( lNormalStyle ).attr( "title", lId );
					w.chat.eBtnActiveEmoticon.click();
					w.chat.eEmoticonsWindow.hide();
				});
			
			}
		});
	},
	
	addEmoticon: function( ){
		if( mWSC.isConnected() ){
			if( !$( this ).attr( "title" ) ){
				$( this ).attr( "title", "wink" );
			}
			var lSymbol = null;
			for( var lKey in w.chat.mEmoticons ){
				if( w.chat.mEmoticons[ lKey ] == $( this ).attr( "title" ) ){
					lSymbol = lKey;
					break;
				}
			}
			if( lSymbol ){
				var lMessage = w.chat.eMessageBox.val();
				if( lMessage == "Type your message..."){
					w.chat.eMessageBox.val( lSymbol );
				} else{
					if( w.chat.mSelectionStart >= 0 && w.chat.mSelectionEnd >= 0 ){
						var lFirst = lMessage.slice( 0, w.chat.mSelectionStart );
						var lEnd = lMessage.slice( w.chat.mSelectionEnd, lMessage.length );
						var lJoin = lFirst + lSymbol + lEnd;
						w.chat.eMessageBox.val( lJoin );
					}
				}
				w.chat.eMessageBox.focus( );
			}
		}
	},
	
	messageBoxBlur : function( aEvent ) {
		if( $( this ).val() == "" ) {
			$( this ).val("Type your message...").attr( "class", "opaque" );
		}
		var lTarget = aEvent.target;
		if( lTarget ){
			w.chat.mSelectionStart = lTarget.selectionStart;
			w.chat.mSelectionEnd = lTarget.selectionEnd;
		}
	},
	
	messageBoxClick: function( aEvent ) { 
		if( $( this ).val( ) == "Type your message..." ) {
			$( this ).val( "" ).attr( "class", "dark" );
		}
	},
	
	messageBoxKeyPressed: function( aEvt ) {
		if( aEvt.keyCode == 13 && ( !aEvt.shiftKey ) ) {
			aEvt.preventDefault( );
			
			w.chat.sendMessage( $( this ).val( ) );
			$( this ).val( "" );
		}
	},
	
	switchPrivate: function( ) {
		
		var lQuantity = parseInt(w.chat.eUnreadMessages.text());
		if (lQuantity > 0 ){
			w.chat.eUnreadMessages.fadeOut( 500 ).text(0);
			if( w.chat.ePrivateUsersBox.children().length == 1 ){
				var lActiveClient = w.chat.ePrivateUsersBox.first()
				.find('.client_id').text();
				
				w.chat.startConversationWith( lActiveClient );
			}
		}
		// Show the privateLogArea
		w.chat.eLogPrivate.show();
		w.chat.eLogPublic.hide();
		
		w.chat.eChatTitle.text( "User: " + 
			(w.chat.mPrivateChatWith || "?") );
		
		// Show the privateUsersBox
		w.chat.ePublicUsersBox.hide();
		w.chat.ePrivateUsersBox.show();
		
		w.chat.eBtnPublicChat.attr("class", "");
		w.chat.eBtnPrivateChat.attr("class", "active");
		w.chat.mIsPublicActive = false;
	},
	
	switchPublic: function( ) {
		// Show the publicLogArea
		w.chat.eLogPrivate.hide();
		w.chat.eLogPublic.show();
		
		w.chat.eChatTitle.text( "Public chat" );
		
		// Show the publicUsersBox
		w.chat.ePrivateUsersBox.hide();
		w.chat.ePublicUsersBox.show();
		
		w.chat.eBtnPublicChat.attr("class", "active");
		w.chat.eBtnPrivateChat.attr("class", "");
		w.chat.mIsPublicActive = true;
	}
});