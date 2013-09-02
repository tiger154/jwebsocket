//	---------------------------------------------------------------------------
//	jWebSocket STOMPWebSocket class (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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

	STOMPWebSocket = function(aUrl, aSubprotocol) {
		var self              = this;
		var lUrlParts = aUrl.split('/'); 
		self.url              = lUrlParts[0] + "//" + lUrlParts[2] + '/stomp';
		self.destination	  = '/topic/' + lUrlParts[3];
		self.subPrcol         = aSubprotocol;
		self.readyStateValues = {
			CONNECTING:0, 
			OPEN:1, 
			CLOSING:2, 
			CLOSED:3
		}
        
		self.readyState = self.readyStateValues.CONNECTING;
		var mEvents = {};
        
		STOMPWebSocket.prototype.addEventListener = function(aType, aListener){
			if (!(aType in mEvents)){
				mEvents[aType] = [];
			}
			mEvents[aType].push(aListener);
		};
        
		STOMPWebSocket.prototype.removeEventListener = function(aType, aListener, aUseCapture) {
			if (!(aType in mEvents)) return;
			var lEvents = mEvents[aType];
			for (var lIndex = lEvents.length - 1; lIndex >= 0; --lIndex) {
				if (lEvents[lIndex] === aListener) {
					lEvents.splice(lIndex, 1);
					break;
				}
			}
		}
        
		STOMPWebSocket.prototype.dispatchEvent = function(aEvent) {
			var lEvents = mEvents[aEvent.type] || [];
			for (var lIndex = 0; lIndex < lEvents.length; ++lIndex) {
				lEvents[lIndex](aEvent);
			}
			var lHandler = self['on' + aEvent.type];
			if (lHandler) {
				lHandler(aEvent);
			}
		}
        
		STOMPWebSocket.prototype.send = function(aData){
			try {
				// supporting message delivery acknowledge on a LB scenario
				var lMessage = JSON.parse(aData);
				if (typeof(lMessage) == 'object' && lMessage['i$WrappedMsg']){
					if ('info' == lMessage.type && 'ack' == lMessage.name){
						self.stomp.send(self.destination, {
							msgType: 'ACK',
							msgId: jws.tools.createUUID(),
							nodeId: lMessage.data.split('-')[0],
							data: aData
						});
						
						return;
					}
				}
			} catch (lError){
			// ommit it, not JSON format
			}
			
			self.stomp.send(self.destination, {
				msgType: 'MESSAGE', 
				data: aData,
				msgId: jws.tools.createUUID()
			})
		}
        
		STOMPWebSocket.prototype.close = function(){
			self.readyState = self.readyStateValues.CLOSING;
			self.stomp.disconnect(function(){
				self.readyState = self.readyStateValues.CLOSED;
				handleEvent({
					type: 'close'
				});
			});
		}    
    
		var handleEvent = function(aEvent){
			var lEvent;
			if ( aEvent.type == 'close' || aEvent.type == 'open' || aEvent.type == 'error') {
				lEvent = createSimpleEvent(aEvent.type);
			} else if (aEvent.type == 'message') {
				lEvent = createMessageEvent('message', aEvent.data);
			} else {
				throw 'Unknown event type: ' + aEvent.type;
			}

			self.dispatchEvent(lEvent);
		}
        
    
		var createSimpleEvent = function(lType) {
			if (document.createEvent && window.Event) {
				var lEvent = document.createEvent('Event');
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
			if (document.createEvent && window.MessageEvent && !window.opera) {
				var lEvent = document.createEvent('MessageEvent');
				lEvent.initMessageEvent('message', false, false, aData, null, null, window, null);
				return lEvent;
			} else {
				// IE and Opera, the latter one truncates the data parameter after any 0x00 bytes.
				return {
					type: aType, 
					data: aData, 
					bubbles: false, 
					cancelable: false
				};
			}
		};
       
		STOMPWebSocket.prototype.open = function(){
			if (self.readyState == self.readyStateValues.OPEN)
				throw new Error('Already connected!');
			
			self.stomp = Stomp.client(self.url);
			self.stomp.debug = function(){};
			self.stomp.connect(
				'', // @TODO username
				'', // @TODO password
				function(){
					var lReplySelector = jws.tools.createUUID();
					self.stomp.subscribe(
						// the target connection destination
						self.destination,  
						// callback
						function( aMessage ) {
							if ('DISCONNECTION' == aMessage.headers['msgType']){
								self.readyState = self.readyStateValues.CLOSING;
								self.stomp.disconnect(function(){
									self.readyState = self.readyStateValues.CLOSED;
									handleEvent({
										type: 'close',
										data: aMessage.data
									});
								});
							} else {
								handleEvent({
									type: 'message',
									data: aMessage.body
								});
							}
						}, {
							selector: "replySelector='" + lReplySelector + "'"
						});
						
					self.stomp.send(self.destination, {
						msgType: 'CONNECTION', 
						replySelector: lReplySelector,
						msgId: jws.tools.createUUID()
					});
					
					self.readyState = self.readyStateValues.OPEN;
					handleEvent({
						type:'open'
					}); 
				},
				function(){
					self.readyState = self.readyStateValues.CLOSED;
					handleEvent({
						type:'close'
					});
				});
		}
               
		this.open();
	}
})();

