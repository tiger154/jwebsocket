//	---------------------------------------------------------------------------
//	jWebSocket STOMPWebSocket class (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------

// @author kyberneees 

(function() {

	STOMPWebSocket = function(aUrl, aSubprotocol, aUsername, aPassword) {
		var self = this;
		var mUsername = aUsername;
		var mPassword = aPassword;
		var mReplySelector = jws.tools.createUUID();
		var mReconnectionAttempts = 0;
		var lQuery = jws.tools.parseQuery(aUrl);

		var mSessionId = lQuery["sessionId"];
		if (!mSessionId) {
			mSessionId = cookie.get("JWSSESSIONID", jws.tools.createUUID());
			cookie.set("JWSSESSIONID", mSessionId);
		}

		self.url = aUrl.split("?")[0];
		self.destination = "/topic/" + lQuery["cluster"];
		self.subPrcol = aSubprotocol;
		self.readyStateValues = {
			CONNECTING: 0,
			OPEN: 1,
			CLOSING: 2,
			CLOSED: 3
		};

		self.readyState = self.readyStateValues.CONNECTING;
		var mEvents = {};

		STOMPWebSocket.prototype.addEventListener = function(aType, aListener) {
			if (!(aType in mEvents)) {
				mEvents[aType] = [];
			}
			mEvents[aType].push(aListener);
		};

		STOMPWebSocket.prototype.removeEventListener = function(aType, aListener, aUseCapture) {
			if (!(aType in mEvents))
				return;
			var lEvents = mEvents[aType];
			for (var lIndex = lEvents.length - 1; lIndex >= 0; --lIndex) {
				if (lEvents[lIndex] === aListener) {
					lEvents.splice(lIndex, 1);
					break;
				}
			}
		};

		STOMPWebSocket.prototype.dispatchEvent = function(aEvent) {
			var lEvents = mEvents[aEvent.type] || [];
			for (var lIndex = 0; lIndex < lEvents.length; ++lIndex) {
				lEvents[lIndex](aEvent);
			}
			var lHandler = self["on" + aEvent.type];
			if (lHandler) {
				lHandler(aEvent);
			}
		};

		STOMPWebSocket.prototype.send = function(aData) {
			try {
				// supporting message delivery acknowledge on a LB scenario
				var lMessage = JSON.parse(aData);
				if (typeof (lMessage) === "object" && lMessage[ "jwsWrappedMsg" ]) {
					if ("info" === lMessage.type && "ack" === lMessage.name) {
						self.stomp.send(self.destination, {
							msgType: "ACK",
							msgId: jws.tools.createUUID(),
							nodeId: lMessage.data.split("-")[ 0 ],
							data: aData,
							replySelector: mReplySelector
						});
						return;
					}
				}
			} catch (lError) {
				// ommit it, not JSON format
			}

			self.stomp.send(self.destination, {
				msgType: "MESSAGE",
				data: aData,
				replySelector: mReplySelector,
				msgId: jws.tools.createUUID()
			});
		};

		STOMPWebSocket.prototype.close = function() {
			self.readyState = self.readyStateValues.CLOSING;
			self.stomp.disconnect(function() {
				self.readyState = self.readyStateValues.CLOSED;
				handleEvent({
					type: "close"
				});
			});
		};

		var handleEvent = function(aEvent) {
			var lEvent;
			if (aEvent.type === "close"
					|| aEvent.type === "open"
					|| aEvent.type === "error") {
				lEvent = createSimpleEvent(aEvent.type);
			} else if (aEvent.type === "message") {
				lEvent = createMessageEvent("message", aEvent.data);
			} else {
				throw "Unknown event type: " + aEvent.type;
			}
			self.dispatchEvent(lEvent);
		};

		var createSimpleEvent = function(lType) {
			if (document.createEvent && window.Event) {
				var lEvent = document.createEvent("Event");
				lEvent.initEvent(lType, false, false);

				return lEvent;
			} else {
				return {
					type: lType,
					bubbles: false,
					cancelable: false
				};
			}
		};

		var createMessageEvent = function(aType, aData) {
			return {
				type: aType,
				data: aData,
				bubbles: false,
				cancelable: false
			};
		};

		STOMPWebSocket.prototype.open = function() {
			if (self.readyState === self.readyStateValues.OPEN)
				throw new Error("Already connected!");

			self.stomp = Stomp.client(self.url);
			self.stomp.debug = function() {
			};
			self.stomp.connect({
				login: mUsername,
				passcode: mPassword
			},
			function() {
				self.stomp.subscribe(
						// the target connection destination
						self.destination,
						// callback
								function(aMessage) {
									if ("DISCONNECTION" === aMessage.headers["msgType"]) {
										self.readyState = self.readyStateValues.CLOSING;
										self.stomp.disconnect(function() {
											self.readyState = self.readyStateValues.CLOSED;
											handleEvent({
												type: "close",
												data: aMessage.data
											});
										});
									} else {
										handleEvent({
											type: "message",
											data: aMessage.body
										});
									}
								}, {
							selector: "replySelector='" + mReplySelector + "' OR isBroadcast=true"
						});

						self.stomp.send(self.destination, {
							msgType: "CONNECTION",
							replySelector: mReplySelector,
							msgId: jws.tools.createUUID(),
							sessionId: mSessionId
						});

						self.readyState = self.readyStateValues.OPEN;
						// notify 'open' if not from reconnection
						if (0 === mReconnectionAttempts) {
							handleEvent({
								type: "open"
							});
						}
						mReconnectionAttempts = 0;
					},
					function() {
						if (self.readyState === self.readyStateValues.OPEN) {
							self.readyState = self.readyStateValues.CONNECTING;
							// perform reconnection
							if (mReconnectionAttempts < 5) {
								mReconnectionAttempts++;
								setTimeout(function() {
									self.open();
								}, mReconnectionAttempts * 100);

								return;
							}
						}
						self.readyState = self.readyStateValues.CLOSED;
						handleEvent({
							type: "close"
						});
					});
		};
		this.open();
	};
})();

