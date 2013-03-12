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
$.widget("jws.viewer", {
	_init: function() {
		this.NS_CHANNELS = jws.NS_BASE + '.plugins.channels';
		this.NS_SYSTEM = jws.NS_BASE + '.plugins.system';
		// ------ DOM ELEMENTS --------
		this.ePresenters = this.element.find("#presenters");
		this.eViewers = this.element.find("#viewers");
		this.eSlide = this.element.find("#slide");
		this.eContainer = $(".container");

		// ------ VARIABLES --------
		this.mCurrSlide = 1;
		this.mOldSlide = 0;
		this.mPresenters = 0;
		this.mViewers = 0;
		this.mChannelId = "jWebSocketSlideShowDemo";
		this.mChannelAccessKey = "5l1d35h0w";
		this.TT_SEND = "send";
		this.TT_SLIDE = "slide";

		this.mClientId = "";

		w.viewer = this;
		w.viewer.registerEvents();
	},
	registerEvents: function() {
		$(document).keydown(w.viewer.keydown);
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function(aEvent, aToken) {
				w.viewer.onMessage(aEvent, aToken);
			},
			OnWelcome: function(aToken) {
				// Registering the callbacks for the channels
				mWSC.setChannelCallbacks({
					// When any subscription arrives from the server
					OnChannelSubscription: w.viewer.onChannelSubscription
				});
				w.viewer.mClientId = aToken.sourceId;
			}
		};
		w.viewer.eContainer.auth(lCallbacks);
		AUTO_USER_AND_PASSWORD = true;
		w.auth.logon();
	},
	onMessage: function(aEvent, aToken) {
		if (aToken.reqType === "login" && aToken.code === 0) {
			// Subscribe to a certain channel
			mWSC.channelSubscribe(w.viewer.mChannelId,
					w.viewer.mChannelAccessKey);
		} else if (aToken.ns === w.viewer.NS_CHANNELS) {
			// When information is published in the channel the data is sent
			// in a map inside the token and the type of the token comes in 
			// the key "data"
			if (aToken.type === "data") {
				switch (aToken.data) {
					case w.viewer.TT_SLIDE:
						// Pass the current slide
						w.viewer.goTo(aToken.map.slide);
						break;
					case w.viewer.TT_USER_UNREGISTER:
						w.viewer.userUnregistered(aToken.map);
						break;
				}
			}
		} else if (aToken.ns === w.viewer.NS_SYSTEM) {
			if (aToken.type === w.viewer.TT_SEND) {
				console.log( aToken );
				w.viewer.updateUsers(aToken.data);
				w.viewer.goTo(aToken.data.currslide);
			}
		}
	},
	goTo: function(aSlide) {
		if (w.viewer.mOldSlide !== aSlide) {
			w.viewer.eSlide.attr("src", "slides/Slide" +
					jws.tools.zerofill(aSlide, 4) + ".gif");
			w.viewer.mOldSlide = aSlide;
		}
	},
	updateUsers: function(aData) {
		w.viewer.mPresenters = aData.presenters;
		w.viewer.mViewers = aData.viewers;
		w.viewer.ePresenters.text(w.viewer.mPresenters);
		w.viewer.eViewers.text(w.viewer.mViewers);
	},
	userUnregistered: function(aData) {
		var lUser = aData.value.trim().split("_");
		if (lUser[0] == "presenter") {
			w.viewer.mPresenters > 0 && w.viewer.mPresenters--;
		} else {
			w.viewer.mViewers > 0 && w.viewer.mViewers--;
		}
		w.viewer.updateUsers({
			presenters: w.viewer.mPresenters,
			viewers: w.viewer.mViewers
		})
	}
});