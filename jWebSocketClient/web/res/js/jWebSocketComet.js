//	---------------------------------------------------------------------------
//	jWebSocket Comet PlugIn (uses jWebSocket Client and Server)
//	(C) 2012 Innotrade GmbH, Herzogenrath
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

// author Osvaldo Aguilar Lauzurique @email osvaldo2627@hab.uci.cu

(function() {

	XHRWebSocket = function(url, subprotocol) {
		var self              = this;
		self.url              = url;
		self.subPrcol         = subprotocol;
		self.readyStateValues = {
			CONNECTING:0, 
			OPEN:1, 
			CLOSING:2, 
			CLOSED:3
		}
        
		self.readyState= this.readyStateValues.CONNECTING;
		self.bufferedAmount=0;
		self.__events = {};
        
		self.__ableToSend = true;
		self.__pendingMessages = [];
		XHRWebSocket.prototype.__already = false;
        
        
		XHRWebSocket.prototype.addEventListener = function(type, listener){
			if (!(type in this.__events)){
				this.__events[type] = [];
			}
			this.__events[type].push(listener);
		};
        
        
		XHRWebSocket.prototype.removeEventListener = function(type, listener, useCapture) {
			if (!(type in this.__events)) return;
			var events = this.__events[type];
			for (var i = events.length - 1; i >= 0; --i) {
				if (events[i] === listener) {
					events.splice(i, 1);
					break;
				}
			}
		}
        
        
		XHRWebSocket.prototype.dispatchEvent = function(event) {
            
			var events = this.__events[event.type] || [];
			for (var i = 0; i < events.length; ++i) {
				events[i](event);
			}
			var handler = this["on" + event.type];
			if (handler) handler(event);
		}
        
        
		XHRWebSocket.prototype.send=function(sdata){
			self.__pendingMessages.push(sdata);
			if (self.__ableToSend == true)
				self.__sendMessage(self.__pendingMessages.shift());
                
		}
        
		XHRWebSocket.prototype.close=function(){
			if(this.readyState==this.readyStateValues.CONNECTING)
				throw "The websocket connection is closing";
			else if (this.readyState == this.readyStateValues.CLOSED)
				throw "The websocket connection is already closing";
			else{
				var message = this.__messageFactory({
					cometType:"message",
					readyState:3
				});
				var messageString = JSON.stringify(message);
                 
				self.__handleEvent({
					type:"close"
				});
                    
				var request = this.__getXHRTransport();
				request.open("POST", this.url, true);
				request.setRequestHeader("Content-Type", "application/x-javascript;");
      
				request.onreadystatechange = function(){

					if (request.readyState >= 4 && request.status == 200) {				
						if (request.responseText) {
							self.readyState=WebSocket.CLOSING;
							self.__handleEvent({
								type:"close"
							});
						}	      
					}
				};
				request.send(messageString);
			}
		}    
        
    
		self.__handleEvent = function(xhrWsEventType){
			var event;
			if ( xhrWsEventType.type == "close" || xhrWsEventType.type == "open" || xhrWsEventType.type == "error") {
				event = this.__createSimpleEvent(xhrWsEventType.type);
			} else if (xhrWsEventType.type == "message") {
				event = this.__createMessageEvent("message", xhrWsEventType.data);
			} else {
				throw "unknown event type: " + xhrWsEventType.type;
			}

			this.dispatchEvent(event);
		}
        
    
		self.__createSimpleEvent = function(type) {
			if (document.createEvent && window.Event) {
				var event = document.createEvent("Event");
				event.initEvent(type, false, false);
				return event;
			} else {
				return {
					type: type, 
					bubbles: false, 
					cancelable: false
				};
			}
		};
        
    
		self.__createMessageEvent = function(type, data) {
			if (document.createEvent && window.MessageEvent && !window.opera) {
				var event = document.createEvent("MessageEvent");
				event.initMessageEvent("message", false, false, data, null, null, window, null);
				return event;
			} else {
				// IE and Opera, the latter one truncates the data parameter after any 0x00 bytes.
				return {
					type: type, 
					data: data, 
					bubbles: false, 
					cancelable: false
				};
			}
		};
        
		self.__checkPendingMessage = function(){
			if (self.__pendingMessages.length > 0){
				var sdata = self.__pendingMessages.shift()
				self.__sendMessage(sdata);
			}
		}
       
		this.open = function(){
			if (self.readyState == self.readyStateValues.OPEN)
				throw "the connection is already opening";
			else
				self.__handlerConnectionChannel();
		}
    
		self.keepConnection = function(){
			self.__handlerConnectionChannel();
		}

		self.__handlerConnectionChannel = function(){
            
			var request = this.__getXHRTransport();
			this.activeConnectionRequest = request;
            
			request.open("POST", this.url, true);
			request.setRequestHeader("Content-Type", "application/x-javascript;");

			request.onreadystatechange = function(){

				if (request.readyState >= 4 && request.status == 200) {				
					if (request.responseText) {
						var response    = JSON.parse(request.responseText);
						if (response.data != ""){
							self.__handleEvent({
								type:"message",
								data:JSON.stringify(response.data)
							});
						}
          
						self.managementConnectionState(response);
					}	      
				}
			};
			var message = this.__messageFactory({
				cometType:"connection"
			});
			var messageString = JSON.stringify(message);
			request.send(messageString);
		}
                
		self.__objectMessageBasePrototype = function(){
			var message = {
				subPl: "json", //Make this for the other three sub protocols that jwebsocket's suport'
				cometType: undefined,
				data: undefined,
				readyState: self.readyState
                
			}
			return message;
		} 
        
		self.__sendMessage = function(sdata){
			if(self.readyState==self.readyStateValues.CONNECTING)
				throw "The websocket connection has not been stablished";
			else if (self.readyState == self.readyStateValues.CLOSED)
				throw "The websocket connection has been closed, the message can not be sent to the server";
			else if (self.__ableToSend == true){  
				var message = this.__messageFactory({
					cometType:"message",
					data:sdata
				});
				var messageString = JSON.stringify(message);
				var request = this.__getXHRTransport();
            
				request.open("POST", this.url, true);
				request.setRequestHeader("Content-Type", "application/x-javascript;");
            
				request.onreadystatechange = function(){
                                       
					if (request.readyState >= 4 && request.status == 200) {				
						if (request.responseText) {
							var response  = JSON.parse(request.responseText)
							if (response.data != ""){
								self.__handleEvent({
									type:"message",
									data:JSON.stringify(response.data)
								});
							}
						}
						self.__ableToSend = true;
						self.__checkPendingMessage();
					}
					else if (request.status == 500)
						self.__ableToSend = true;
					else if (request.status == 404)
						self.__ableToSend = true;
				};
				request.send(messageString);
				self.__ableToSend = false;
			}else{
				self.__pendingMessages.push(sdata);
			}

		}
        
		self.__messageFactory = function(args){
            
			var message = self.__objectMessageBasePrototype();
			if (args != undefined)
				if (args.cometType == undefined)
					throw "Error up, type message not found";
				else{
					message.cometType = args.cometType;
					if (args.data != undefined)
						message.data = args.data;
					else
						message.data = undefined;
					if (args.readyState != undefined)
						message.readyState = args.readyState;
				}
                
			return message;
		}
    
 
        
		self.managementConnectionState = function(response){
			if (    self.readyState == self.readyStateValues.CONNECTING 
				&& response.readyState == self.readyStateValues.OPEN){
				self.__handleEvent({
					type:"open"
				}); 
			}
			if (response.readyState)
				self.readyState = response.readyState;
			else
				console.log("the server not response with readyState value");

            
			if (self.readyState == 2 || self.readyState == 3){
				self.__handleEvent({
					type:"close"
				}); 
			}else{
				self.keepConnection();            
				self.__checkPendingMessage();
			}
		}
        
        
		self.__getXHRTransport = function(){

			var httpRequest;
			if (window.XMLHttpRequest) { // Mozilla, Safari, ...
				ie = 0;
				httpRequest = new XMLHttpRequest();
				if (httpRequest.overrideMimeType) 
					httpRequest.overrideMimeType('text/xml');
			}
			else { // IE
				ie = 1;
				try {
					httpRequest = new ActiveXObject("Msxml2.XMLHTTP");
				}
				catch (e) {}
				if ( typeof httpRequest == 'undefined' ) {
					try {
						httpRequest = new ActiveXObject("Microsoft.XMLHTTP");
					}
					catch (f) {}
				}
			}
			if (!httpRequest) {
				throw "Cannot create an XMLHTTP instance";
				return false;
			}
			else 
				return httpRequest ;
		}
               
		self.open();
	}

})();


window.WebSocket = XHRWebSocket;
