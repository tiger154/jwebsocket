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
		this.TITLE = "Viewer window ";
		// ------ DOM ELEMENTS --------
		this.ePresenters = this.element.find("#presenters");
		this.eViewers = this.element.find("#viewers");
		this.eSlide = this.element.find("#slide");
		this.eContainer = $(".container");
		this.eBtnFullScreen = this.element.find("#fullscreen_btn_viewer");
		this.eFullScreenArea = this.element.find("#fullscreen");
		this.eStatusbarArea = this.element.find("#demo_box_statusbar");
		this.eBtnNewViewer = this.element.find("#new_viewer_window_btn");
		this.mNextWindowId = 1;
		// ------ VARIABLES --------
		this.mCurrSlide = 1;
		this.mOldSlide = 0;
		this.mPresenters = 0;
		this.mViewers = 0;
		this.mChannelId = "jWebSocketSlideShowDemo";
		this.mChannelAccessKey = "5l1d35h0w";
		this.TT_SEND = "send";
		this.TT_SLIDE = "slide";
		this.mIsFS = false;
		this.mPresentersList = {};
		this.mClientId = "";

		w.viewer = this;
		w.viewer.registerEvents();
	},
	registerEvents: function() {
		$(document).keydown(w.viewer.keydown);
		w.viewer.eBtnFullScreen.click(w.viewer.toggleFullScreen);
		w.viewer.eBtnFullScreen.fadeTo(400, 0);
		w.viewer.eSlide.bind({
			mousemove: function( ) {
				if (w.viewer.mIsFS) {
					w.viewer.eStatusbarArea.stop(true, true).show(300);
					clearInterval(w.viewer.mInterval);
					w.viewer.mInterval = setInterval(function() {
						w.viewer.eStatusbarArea.stop(true, true).hide(800);
					}, 4000);
				}
			},
			mouseover: function() {
				w.viewer.eBtnFullScreen.fadeTo(100, 0.5);
			},
			mouseout: function(aEvent) {
				if (aEvent.relatedTarget == w.viewer.eBtnFullScreen.get(0)) {
					return false;
				}
				w.viewer.eBtnFullScreen.stop(true, true).fadeTo(400, 0.1);
			}
		})

		$(document).bind('webkitfullscreenchange mozfullscreenchange fullscreenchange', function() {
			if (!w.viewer.isFullScreen()) {
				w.viewer.mIsFS = false;
				clearInterval(w.viewer.mInterval);
				w.viewer.eStatusbarArea.show();
			}
		});
		w.viewer.eBtnNewViewer.click(w.viewer.openViewerWindow);
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function(aEvent, aToken) {
				w.viewer.onMessage(aEvent, aToken);
			},
			OnWelcome: function(aToken) {
				// Registering the callbacks for the channels
				mWSC.setChannelCallbacks({
					// When any unsubscription arrives from the server
					OnChannelUnsubscription: w.viewer.onChannelUnsubscription
				});
				w.viewer.mClientId = aToken.sourceId;
			}
		};
		w.viewer.eContainer.auth(lCallbacks);
		AUTO_USER_AND_PASSWORD = true;
		w.auth.logon();
	},
	onMessage: function(aEvent, aToken) {
		if (aToken.type === "response" && aToken.reqType === "login") {
			mWSC.sessionPut("viewer", aToken.sourceId, true, {
				OnSuccess: function(aToken) {
					mWSC.channelSubscribe(w.viewer.mChannelId,
							w.viewer.mChannelAccessKey);
				}
			});
		}
		if (aToken.ns === w.viewer.NS_CHANNELS) {
			if (mLog.isDebugEnabled) {
				log(" <b>" + w.viewer.TITLE + " new message received: </b>" +
						JSON.stringify(aToken));
			}
			// When information is published in the channel the data is sent
			// in a map inside the token and the type of the token comes in 
			// the key "data"
			if (aToken.type === "data") {
				switch (aToken.data) {
					case w.viewer.TT_SLIDE:
						// Pass the current slide
						w.viewer.goTo(aToken.map.slide);
						break;
					case w.viewer.TT_SEND:
						w.viewer.updateUsers(aToken.map);
						break;
				}
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
		aData = aData || {};
		aData.currslide && w.viewer.goTo(aData.currslide);
		w.viewer.mCurrSlide = aData.currslide || w.viewer.mCurrSlide;
		w.viewer.mOldSlide = aData.oldslide || w.viewer.mOldSlide;
		w.viewer.mViewers = aData.viewers || w.viewer.mViewers;
		w.viewer.eViewers.text(w.viewer.mViewers);
		// copying the presenters elements to our list
		for (var lIdx in aData.presentersList) {
			w.viewer.mPresentersList[lIdx] = aData.presentersList[lIdx];
		}
		var lCounter = 0;
		// Counting how many presenters we have
		for (var lIdx in w.viewer.mPresentersList) {
			lCounter++;
		}
		w.viewer.mPresenters = lCounter;
		w.viewer.ePresenters.text(w.viewer.mPresenters);
	},
	onChannelUnsubscription: function(aToken) {
		var lUnsubscriber = aToken.subscriber;
		if (w.viewer.mPresentersList[lUnsubscriber]) {
			delete w.viewer.mPresentersList[lUnsubscriber];
			w.viewer.mPresenters > 0 && w.viewer.mPresenters--;
		} else {
			w.viewer.mViewers > 0 && w.viewer.mViewers--;
		}
		w.viewer.updateUsers();
	},
	toggleFullScreen: function() {
		if (w.viewer.isFullScreen()) {
			w.viewer.exitFullScreen(document);
			w.viewer.mIsFS = false;
		} else {
			w.viewer.initFullScreen(w.viewer.eFullScreenArea.get(0));
			w.viewer.mIsFS = true;
		}
		return false;
	},
	isFullScreen: function() {
		return  (document.fullScreen && document.fullScreen != null) ||
				(document.mozFullScreen || document.webkitIsFullScreen);
	},
	exitFullScreen: function(aElement) {
		// Exit full-screen mode, supported by Firefox 9 || + or Chrome 15 || +
		var lNativeMethod = aElement.cancelFullScreen ||
				aElement.webkitCancelFullScreen ||
				aElement.mozCancelFullScreen ||
				aElement.exitFullscreen;
		if (lNativeMethod) {
			lNativeMethod.call(aElement);
			// Support for IE old versions
		} else if (typeof window.ActiveXObject !== "undefined") {
			var lAXScript = new ActiveXObject("WScript.Shell");
			if (lAXScript !== null) {
				lAXScript.SendKeys("{F11}");
			}
		}
	},
	initFullScreen: function(aElement) {
		// Full-screen mode, supported by Firefox 9 || + or Chrome 15 || +
		var lNativeMethod = aElement.requestFullScreen ||
				aElement.webkitRequestFullScreen ||
				aElement.mozRequestFullScreen ||
				aElement.msRequestFullScreen;

		if (lNativeMethod) {
			console.log("native");
			console.log(aElement);
			lNativeMethod.call(aElement);
		} else if (typeof window.ActiveXObject !== "undefined") { // Older IE.
			var lAXScript = new ActiveXObject("WScript.Shell");
			if (lAXScript !== null) {
				lAXScript.SendKeys("{F11}");
			}
		}
		return false;
	},
	openViewerWindow: function( ) {
		window.open(
				// "http://www.jwebsocket.org/demos/jwsSlideshow/viewerIframe.htm"
				"viewerIframe.htm",
				"viewerWindow" + w.viewer.mNextWindowId,
				"width=400,height=400,left=" +
				(50 + w.viewer.mNextWindowId * 30) + ", top=" +
				(50 + w.viewer.mNextWindowId * 25));
		w.viewer.mNextWindowId++;
		if (w.viewer.mNextWindowId > 10) {
			w.viewer.mNextWindowId = 1;
		}
	}
});