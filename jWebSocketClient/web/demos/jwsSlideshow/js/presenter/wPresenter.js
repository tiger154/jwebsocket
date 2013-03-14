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
$.widget("jws.presenter", {
	_init: function() {
		this.NS_CHANNELS = jws.NS_BASE + '.plugins.channels';
		this.NS_SYSTEM = jws.NS_BASE + '.plugins.system';
		this.TITLE = "Presenter window";
		// ------ DOM ELEMENTS --------
		this.eBtnNext = this.element.find("#btn_next");
		this.eBtnPrev = this.element.find("#btn_prev");
		this.eBtnFirst = this.element.find("#btn_first");
		this.eBtnLast = this.element.find("#btn_last");
		this.ePresenters = this.element.find("#presenters");
		this.eViewers = this.element.find("#viewers");
		this.eSlide = this.element.find("#slide");
		this.eContainer = $(".container");
		// ------ VARIABLES --------
		this.mCurrSlide = 1;
		this.mOldSlide = 0;
		this.mMaxSlides = 22;
		this.mPresenters = 0;
		this.mViewers = 0;
		this.mChannelId = "jWebSocketSlideShowDemo";
		this.mChannelAccessKey = "5l1d35h0w";
		this.mChannelSecretKey = "5l1d35h0w53cr3t!";
		this.TT_SEND = "send";
		this.TT_SLIDE = "slide";
		this.mClientId = "";
		this.mPresentersList = {};
		this.mViewersList = {};
		w.presenter = this;
		w.presenter.registerEvents();
	},
	registerEvents: function() {
		w.presenter.eBtnNext.click(w.presenter.nextSlide);
		w.presenter.eBtnPrev.click(w.presenter.prevSlide);
		w.presenter.eBtnLast.click(w.presenter.lastSlide);
		w.presenter.eBtnFirst.click(w.presenter.firstSlide);
		$(document).keydown(w.presenter.keydown);
		// When closing the window notify the other clients about who is 
		// leaving the conference room
		$(window).bind('beforeunload', function() {
			jws.channelUnsubscribe(w.presenter.mChannelId);
		});
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnMessage: function(aEvent, aToken) {
				w.presenter.onMessage(aEvent, aToken);
			},
			OnWelcome: function(aToken) {
				// Registering the callbacks for the channels
				mWSC.setChannelCallbacks({
					// When any subscription arrives from the server
					OnChannelUnsubscription: w.presenter.onChannelUnsubscription,
					OnChannelSubscription: w.presenter.onChannelSubscription
				});
				w.presenter.mClientId = aToken.sourceId;
			}
		};
		w.presenter.eContainer.auth(lCallbacks);
		AUTO_USER_AND_PASSWORD = true;
		w.auth.logon();
	},
	onMessage: function(aEvent, aToken) {
		if (aToken.type === "response" && aToken.reqType === "login") {
			mWSC.sessionPut("presenter", aToken.sourceId, true);
			mWSC.channelSubscribe(w.presenter.mChannelId,
					w.presenter.mChannelAccessKey, {
				OnSuccess: function() {
					w.presenter.authenticateChannel();
				}
			});
		}
		if (aToken.ns === w.presenter.NS_CHANNELS) {
			if (mLog.isDebugEnabled) {
				log(" <b>" + w.presenter.TITLE + " new message received: </b>" +
						JSON.stringify(aToken));
			}
			// When the channel authorizes the user to publish on it
			if (aToken.reqType === "authorize") {
				if (aToken.code === 0) {
					var lData = {
						value: "presenter_" + mWSC.getUsername() + "@" +
								w.presenter.mClientId
					};
					// We send a notification trough the channel to inform about
					// a new presenter online
					w.presenter.publish(w.presenter.TT_USER_REGISTER, lData);
				}
				// When information is published in the channel the data is sent
				// in a map inside the token and the type of the token comes in 
				// the key "data"
			} else if (aToken.type === "data") {
				switch (aToken.data) {
					// When the slides pass
					case w.presenter.TT_SLIDE:
						// Pass the current slide
						w.presenter.goTo(aToken.map.slide);
						break;
					// Whenever a new user goes subscribed on the channel
					// through the channel is published the number of presenters
					// the number of viewers and the current slide, so the 
					// new registered client or publisher will automatically  
					// synchronize itself the existing clients
					case w.presenter.TT_SEND:
						w.presenter.updateUsers(aToken.map);
						break;
				}
			}
		}
	},
	nextSlide: function() {
		if (w.presenter.mCurrSlide < w.presenter.mMaxSlides) {
			w.presenter.mCurrSlide++;
			w.presenter.updateSlide();
		}
	},
	prevSlide: function() {
		if (w.presenter.mCurrSlide > 1) {
			w.presenter.mCurrSlide--;
			w.presenter.updateSlide();
		}
	},
	lastSlide: function() {
		w.presenter.mCurrSlide = w.presenter.mMaxSlides;
		w.presenter.updateSlide();
	},
	firstSlide: function() {
		w.presenter.mCurrSlide = 1;
		w.presenter.updateSlide();
	},
	goTo: function(aSlide) {
		if (w.presenter.mOldSlide != aSlide) {
			w.presenter.eSlide.attr("src", "slides/Slide" +
					jws.tools.zerofill(aSlide, 4) + ".gif");
			w.presenter.eBtnLast.isDisabled && w.presenter.eBtnLast.enable();
			w.presenter.eBtnNext.isDisabled && w.presenter.eBtnNext.enable();
			w.presenter.eBtnFirst.isDisabled && w.presenter.eBtnFirst.enable();
			w.presenter.eBtnPrev.isDisabled && w.presenter.eBtnPrev.enable();
			if (aSlide == 1) {
				w.presenter.eBtnPrev.disable();
				w.presenter.eBtnFirst.disable();
			} else if (aSlide == w.presenter.mMaxSlides) {
				w.presenter.eBtnNext.disable();
				w.presenter.eBtnLast.disable();
			}
			w.presenter.mOldSlide = aSlide;
		}
	},
	updateSlide: function() {
		if (w.presenter.mOldSlide !== w.presenter.mCurrSlide) {
			w.presenter.publish(w.presenter.TT_SLIDE, {
				slide: w.presenter.mCurrSlide,
				presenter: w.presenter.mClientId
			});
		}
	},
	updateUsers: function(aData) {
		aData = aData || {};
		aData.currslide && w.presenter.goTo(aData.currslide);
		w.presenter.mCurrSlide = aData.currslide || w.presenter.mCurrSlide;
		w.presenter.mViewers = aData.viewers || w.presenter.mViewers;
		w.presenter.eViewers.text(w.presenter.mViewers);
		// copying the presenters elements to our list
		for (var lIdx in aData.presentersList) {
			w.presenter.mPresentersList[lIdx] = aData.presentersList[lIdx];
		}
		var lCounter = 0;
		// Counting how many presenters we have
		for (var lIdx in w.presenter.mPresentersList) {
			lCounter++;
		}
		w.presenter.mPresenters = lCounter;
		w.presenter.ePresenters.text(w.presenter.mPresenters);

	},
	onChannelSubscription: function(aToken) {
		var lSubscriber = aToken.subscriber;
		mWSC.sessionGetAll(lSubscriber, true, {
			OnSuccess: function(aToken) {
				var lId = "";
				var lIsPresenter = false, lIsViewer = false;
				for (var lKey in aToken.data) {
					lId = lKey.split("::")[1];
					if (lId === 'presenter' && lSubscriber === aToken.data[lKey]) {
						lIsPresenter = true;
					}
					if (lId === 'viewer' && lSubscriber === aToken.data[lKey]) {
						lIsViewer = true;
					}
				}

				var lData = {
					currslide: w.presenter.mCurrSlide,
					viewers: lIsViewer ? w.presenter.mViewers + 1 :
							w.presenter.mViewers
				};

				if (lIsPresenter) {
					w.presenter.mPresenters++;
					lData.presenters = w.presenter.mPresenters;
					w.presenter.mPresentersList[lSubscriber] = true;
					lData.presentersList = w.presenter.mPresentersList;
					// In case that the presenter arrives after the viewers and there 
					// are no more presenters to send him the information he should
					// request for the clients from the server
					if (w.presenter.mPresenters === 1) {
						mWSC.channelGetSubscribers(w.presenter.mChannelId,
								w.presenter.mChannelAccessKey, {
							OnSuccess: function(aToken) {
								var lViewers = aToken.subscribers.length -
										w.presenter.mPresenters;
								lData.viewers = lViewers > 0 ? lViewers :
										w.presenter.mViewers;
								if (lViewers + w.presenter.mPresenters ===
										aToken.subscribers.length) {
									// The presenter will only be able to publish 
									// information when there are not more 
									// presenters in the conference and his 
									// information is correct
									w.presenter.publish(w.presenter.TT_SEND, lData);
								}
							}
						});
					} else {
						w.presenter.publish(w.presenter.TT_SEND, lData);
					}
				} else if (lIsViewer) {
					lData.presenters = w.presenter.mPresenters;
					lData.presentersList = w.presenter.mPresentersList;
					w.presenter.publish(w.presenter.TT_SEND, lData);
				}
			}
		});
	},
	onChannelUnsubscription: function(aToken) {
		var lUnsubscriber = aToken.subscriber;
		if (w.presenter.mPresentersList[lUnsubscriber]) {
			delete w.presenter.mPresentersList[lUnsubscriber];
			w.presenter.mPresenters > 0 && w.presenter.mPresenters--;
		} else {
			w.presenter.mViewers > 0 && w.presenter.mViewers--;
		}
		w.presenter.updateUsers();
	},
	keydown: function(aEvent) {
		if (mWSC.isConnected()) {
			var lKeyCode = aEvent.keyCode || aEvent.keyChar;
			switch (lKeyCode) {
				case 37:
					{
						// Left Arrow (Ctrl key pressed takes you to the First slide)
						aEvent.ctrlKey && w.presenter.firstSlide()
								|| w.presenter.prevSlide();
						aEvent.preventDefault();
						break;
					}
				case 39:
					{
						// Right Arrow (Ctrl key pressed takes you to the First slide)
						aEvent.ctrlKey && w.presenter.lastSlide()
								|| w.presenter.nextSlide();
						aEvent.preventDefault();
						break;
					}
			}
		}
	},
	// try to authenticate against the channel to publish data
	authenticateChannel: function() {
		// use access key and secret key for this channel to authenticate
		// required to publish data only
		var lRes = mWSC.channelAuth(w.presenter.mChannelId,
				w.presenter.mChannelAccessKey, w.presenter.mChannelSecretKey);
	},
	publish: function(aType, aData) {
		mWSC.channelPublish(w.presenter.mChannelId, aType, aData);
	}
});
(function($) {
	$.fn.buttons = {};
	$.fn.disable = function() {
		var lButton = this;
		var lId = lButton.attr("id");
		lButton.isDisabled = true;
		$.fn.buttons[lId] = lButton.clone();
		var lEvents = ["onmouseover", "onmousedown", "onmouseup", "onmouseout", "onclick"];
		$(lEvents).each(function(aIndex, aElem) {
			lButton.attr(aElem, null);
		});
		lButton.attr("class", "button onmousedown");
	};
	$.fn.enable = function() {
		var lButton = this;
		var lId = lButton.attr("id");
		var lEvents = ["onmouseover", "onmousedown", "onmouseup", "onmouseout", "onclick"];
		$(lEvents).each(function(aIndex, aAttribute) {
			lButton.attr(aAttribute, $.fn.buttons[ lId ].attr(aAttribute));
		});
		lButton.attr("class", "button onmouseout");
		lButton.isDisabled = false;
	};
})(jQuery);