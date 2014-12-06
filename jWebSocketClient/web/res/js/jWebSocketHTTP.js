//	---------------------------------------------------------------------------
//	jWebSocket HTTPWebSocket class (Community Edition, CE)
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

// @author Rolando Santamaria Maso

(function() {

	HTTPWebSocket = function(aUrl, aSubProtocol) {
		var self = this;
		this.url = (aUrl.substr(0, 2) === "ws") ? "http" + aUrl.substr(2) : aUrl;
		this.subProtocol = aSubProtocol;
		this.readyStateValues = {
			CONNECTING: 0,
			OPEN: 1,
			CLOSING: 2,
			CLOSED: 3
		};

		this.readyState = this.readyStateValues.CONNECTING;
		this.__events = {};
		this.__ableToSend = true;
		this.__messagesQueue = [];

		HTTPWebSocket.prototype.addEventListener = function(aType, aListener) {
			if (!(aType in this.__events)) {
				this.__events[aType] = [];
			}
			this.__events[aType].push(aListener);
		};

		HTTPWebSocket.prototype.removeEventListener = function(aType, aListener, aUseCapture) {
			if (!(aType in this.__events))
				return;
			var lEvents = this.__events[aType];
			for (var lIndex = lEvents.length - 1; lIndex >= 0; --lIndex) {
				if (lEvents[lIndex] === aListener) {
					lEvents.splice(lIndex, 1);
					break;
				}
			}
		};

		HTTPWebSocket.prototype.dispatchEvent = function(aEvent) {
			var lEvents = this.__events[aEvent.type] || [];
			for (var lIndex = 0; lIndex < lEvents.length; ++lIndex) {
				lEvents[lIndex](aEvent);
			}
			var lHandler = this["on" + aEvent.type];
			if (lHandler)
				lHandler(aEvent);
		};

		HTTPWebSocket.prototype.send = function(aData) {
			this.__messagesQueue.push(aData);

			if (true === this.__ableToSend) {
				this.__sendMessage(this.__messagesQueue.shift());
			}
		};

		HTTPWebSocket.prototype.close = function() {
			if (this.readyState === this.readyStateValues.CLOSING)
				throw "The connection is being closed";
			else if (this.readyState === this.readyStateValues.CLOSED)
				throw "The connection is already closed";
			else {
				try {
					var lXHR = this.__getXHRTransport();
					lXHR.open("GET", this.url + "&action=close", true);
					lXHR.setRequestHeader("Content-Type", "application/x-javascript;");

					this.readyState = this.readyStateValues.CLOSING;
					lXHR.onreadystatechange = function() {

						if (lXHR.readyState >= 4 && lXHR.status === 200) {
							self.readyState = self.readyStateValues.CLOSED;
							self.__handleEvent({
								type: "close"
							});
						}
					};

					lXHR.send();
				} catch (lEx) {
				}
			}
		};

		this.__handleEvent = function(aXHREvent) {
			var lEvent;
			if (aXHREvent.type === "close" || aXHREvent.type === "open" || aXHREvent.type === "error") {
				lEvent = this.__createSimpleEvent(aXHREvent.type);
			} else if (aXHREvent.type === "message") {
				lEvent = this.__createMessageEvent("message", aXHREvent.data);
			} else {
				throw "unknown event type: " + aXHREvent.type;
			}

			this.dispatchEvent(lEvent);
		};

		this.__createSimpleEvent = function(lType) {
			return {
				type: lType,
				bubbles: false,
				cancelable: false
			};
		};

		this.__createMessageEvent = function(aType, aData) {
			return {
				type: aType,
				data: aData,
				bubbles: false,
				cancelable: false
			};
		};

		this.__checkMessageQueue = function() {
			if (this.__messagesQueue.length > 0) {
				var lData = this.__messagesQueue.shift();
				this.__sendMessage(lData);
			}
		};

		this.open = function() {
			if (this.readyState === this.readyStateValues.OPEN)
				throw "The connection is already opened";

			var lXHR = this.__getXHRTransport();

			lXHR.open("GET", this.url + "&action=open", true);
			lXHR.setRequestHeader("Content-Type", "application/x-javascript;");

			lXHR.onreadystatechange = function() {
				if (lXHR.readyState >= 4) {
					if (lXHR.status === 200) {
						setTimeout(function() {
							self.__checkConnectionState(self.readyStateValues.OPEN);
							self.sync(false);
						}, 0);
					}
				}
			};

			lXHR.send();
		};

		this.sync = function(aInvoked) {
			if (self.readyStateValues.OPEN !== self.readyState)
				return;

			var lXHR = this.__getXHRTransport();

			lXHR.open("GET", this.url + "&action=sync", true);
			lXHR.setRequestHeader("Content-Type", "application/x-javascript;");

			lXHR.onreadystatechange = function() {
				if (lXHR.readyState >= 4) {
					if (lXHR.status === 200) {
						if (lXHR.responseText) {
							var lMessages = JSON.parse(lXHR.responseText);
							setTimeout(function() {
								for (var lIndex = 0; lIndex < lMessages.length; lIndex++) {
									var lMsg = lMessages[lIndex];
									if ("http.command.close" === lMsg) {
										self.readyState = self.readyStateValues.CLOSED;
										self.__handleEvent({
											type: "close"
										});
									} else {
										self.__handleEvent({
											type: "message",
											data: lMsg
										});
									}
								}
							}, 0);

							// IMPORTANT: check the connection status
							if (!aInvoked && self.readyStateValues.OPEN === self.readyState) {
								setTimeout(function() {
									self.sync(false);
								}, 1000 * 5);
							}
						}
					}
				}
			};

			lXHR.send();
		};

		this.__sendMessage = function(aData) {
			if (this.readyState === this.readyStateValues.CONNECTING) {
				throw "The connection has not been stablished";
			} else if (this.readyState === this.readyStateValues.CLOSED) {
				throw "The connection has been closed, the message can not be sent to the server";
			} else if (this.__ableToSend === true) {
				// basic synchronism
				this.__ableToSend = false;

				var lXHR = this.__getXHRTransport();

				lXHR.open("GET", this.url + "&action=send&data=" + aData, true);
				lXHR.setRequestHeader("Content-Type", "application/x-javascript;");

				lXHR.onreadystatechange = function() {
					if (lXHR.readyState >= 4 && lXHR.status === 200) {
						// the channel is released
						self.__handleEvent({
							type: "message",
							data: lXHR.responseText
						});
						self.__ableToSend = true;
						self.__checkMessageQueue();
					}
				};

				lXHR.send();
			} else {
				this.__messagesQueue.push(aData);
			}

		};

		this.__checkConnectionState = function(aReadyState) {
			if (this.readyState === this.readyStateValues.CONNECTING
					&& aReadyState === this.readyStateValues.OPEN) {
				// require to affect the readyState flag before call the onopen callback
				this.readyState = aReadyState;

				this.__handleEvent({
					type: "open"
				});
			}
			this.readyState = aReadyState;

			if (this.readyState === 2 || this.readyState === 3) {
				this.__handleEvent({
					type: "close"
				});
			}
		};

		this.__getXHRTransport = function() {
			var lXHR;
			if (window.XMLHttpRequest) { // Mozilla, Safari, ...
				ie = 0;
				lXHR = new XMLHttpRequest();
				if (lXHR.overrideMimeType)
					lXHR.overrideMimeType('text/xml');
			}
			else { // IE
				ie = 1;
				try {
					lXHR = new ActiveXObject("Msxml2.XMLHTTP");
				}
				catch (e) {
				}
				if (typeof (httpRequest) === 'undefined') {
					try {
						lXHR = new ActiveXObject("Microsoft.XMLHTTP");
					}
					catch (f) {
					}
				}
			}
			if (!lXHR) {
				throw "Cannot create an XMLHTTP instance";
				return false;
			}

			return lXHR;
		};

		this.open();
	};

})();

