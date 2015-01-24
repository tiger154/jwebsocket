//	---------------------------------------------------------------------------
//	jWebSocket JMS Plug-in  (Community Edition, CE)
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

//:author:*:Alexander Schulze, Johannes Smutny

//:package:*:jws
//:class:*:jws.JMSPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.JMSPlugIn[/tt] class. This _
//:d:en:plug-in provides the methods to subscribe and unsubscribe at certain _
//:d:en:channel on the server.
jws.JMSPlugIn = {
	// :const:*:NS:String:org.jwebsocket.plugins.channels (jws.NS_BASE +
	// ".plugins.jms")
	// :d:en:Namespace for the [tt]ChannelPlugIn[/tt] class.
	// if namespace changes update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.jms",
	NS_JMS_GATEWAY: "org.jwebsocket.jms.gateway",
	NS_JMS_DEMO: "org.jwebsocket.jms.demo",
	JMS_GATEWAY_ID: "org.jwebsocket.jms.gateway",
	JMS_GATEWAY_TOPIC: "org.jwebsocket.jms.gateway",
	SEND_TEXT: "sendJmsText",
	SEND_TEXT_MESSAGE: "sendJmsTextMessage",
	SEND_MAP: "sendJmsMap",
	SEND_MAP_MESSAGE: "sendJmsMapMessage",
	LISTEN: "listenJms",
	LISTEN_MESSAGE: "listenJmsMessage",
	UNLISTEN: "unlistenJms",
	PING: "ping",
	IDENTIFY: "identify",
	IS_BROKER_CONNECTED: "isBrokerConnected",
	
	processToken: function (aToken) {
		// check if namespace matches
		if (aToken.ns === jws.JMSPlugIn.NS_JMS_GATEWAY) {
			if ("response" === aToken.type) {
				if ("ping" === aToken.reqType) {
					if (this.OnPing) {
						this.OnPing(aToken);
					}
				} else if ("identify" === aToken.reqType) {
					if (this.OnIdentify) {
						this.OnIdentify(aToken);
					}
				}
			}
		} else if (aToken.ns === jws.JMSPlugIn.NS) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
			if ("event" === aToken.type) {
				if ("BrokerException" === aToken.name) {
					if (this.OnBrokerTransportException) {
						this.OnBrokerTransportException(aToken);
					}
				} else if ("BrokerTransportInterrupted" === aToken.name) {
					if (this.OnBrokerTransportInterupted) {
						this.OnBrokerTransportInterupted(aToken);
					}
				} else if ("BrokerTransportResumed" === aToken.name) {
					if (this.OnBrokerTransportResumed) {
						this.OnBrokerTransportResumed(aToken);
					}
				} else if ("endPointDisconnected" === aToken.name) {
					if (this.OnEndPointDisconnected) {
						this.OnEndPointDisconnected(aToken);
					}
				} else if ("endPointConnected" === aToken.name) {
					if (this.OnEndPointConnected) {
						this.OnEndPointConnected(aToken);
					}
				} else if ("handleJmsText" === aToken.name) {
					if (this.OnHandleJmsText) {
						this.OnHandleJmsText(aToken);
					}
				} else if ("handleJmsTextMessage" === aToken.name) {
					if (this.OnHandleJmsTextMessage) {
						this.OnHandleJmsTextMessage(aToken);
					}
				} else if ("handleJmsMap" === aToken.name) {
					if (this.OnHandleJmsMap) {
						this.OnHandleJmsMap(aToken);
					}
				} else if ("handleJmsMapMessage" === aToken.name) {
					if (this.OnHandleJmsMapMessage) {
						this.OnHandleJmsMapMessage(aToken);
					}
				}
			}
		}
	},
	jmsPing: function (aTargetId, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.PING,
				targetId: aTargetId
			}, aOptions);
		}
		return lRes;
		/*		
		 var lRes = this.checkConnected();
		 if (0 === lRes.code) {
		 // aTarget, aNS, aType, aArgs, aJSON, aOptions
		 this.forwardJSON(
		 aTargetId,
		 jws.JMSPlugIn.NS_JMS_GATEWAY,
		 jws.JMSPlugIn.PING,
		 {},
		 "",
		 aOptions
		 );
		 }
		 return lRes;
		 */
	},
	jmsIsBrokerConnected: function (aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.IS_BROKER_CONNECTED,
			}, aOptions);
		}
		return lRes;
	},
	jmsIdentify: function (aTargetId, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.IDENTIFY,
				targetId: aTargetId
			}, aOptions);
		}
		return lRes;
	},
	jmsEcho: function (aTargetId, aPayload, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.forwardJSON(
					aTargetId,
					jws.JMSPlugIn.NS_JMS_DEMO,
					"echo",
					{},
					aPayload,
					aOptions
					);
		}
		return lRes;
	},
	listenJms: function (aConnectionFactoryName, aDestinationName,
			aPubSubDomain, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.LISTEN,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				pubSubDomain: aPubSubDomain
			}, aOptions);
		}
		return lRes;
	},
	listenJmsMessage: function (aConnectionFactoryName, aDestinationName,
			aPubSubDomain, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.LISTEN_MESSAGE,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				pubSubDomain: aPubSubDomain
			}, aOptions);
		}
		return lRes;
	},
	unlistenJms: function (aConnectionFactoryName, aDestinationName,
			aPubSubDomain, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.UNLISTEN,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				pubSubDomain: aPubSubDomain
			}, aOptions);
		}
		return lRes;
	},
	sendJmsText: function (aConnectionFactoryName, aDestinationName,
			aPubSubDomain, aText, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.SEND_TEXT,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				pubSubDomain: aPubSubDomain,
				msgPayLoad: aText
			}, aOptions);
		}
		return lRes;
	},
	sendJmsTextMessage: function (aConnectionFactoryName, aDestinationName,
			aPubSubDomain, aText, aJmsHeaderProperties, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.SEND_TEXT_MESSAGE,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				pubSubDomain: aPubSubDomain,
				msgPayLoad: aText,
				jmsHeaderProperties: aJmsHeaderProperties
			}, aOptions);
		}
		return lRes;
	},
	sendJmsMap: function (aConnectionFactoryName, aDestinationName,
			aPubSubDomain, aMap, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.SEND_MAP,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				pubSubDomain: aPubSubDomain,
				msgPayLoad: aMap
			}, aOptions);
		}
		return lRes;
	},
	sendJmsMapMessage: function (aConnectionFactoryName, aDestinationName,
			aPubSubDomain, aMap, aJmsHeaderProperties, aOptions) {
		var lRes = this.checkConnected();
		if (0 === lRes.code) {
			this.sendToken({
				ns: jws.JMSPlugIn.NS,
				type: jws.JMSPlugIn.SEND_MAP_MESSAGE,
				connectionFactoryName: aConnectionFactoryName,
				destinationName: aDestinationName,
				pubSubDomain: aPubSubDomain,
				msgPayLoad: aMap,
				jmsHeaderProperties: aJmsHeaderProperties
			}, aOptions);
		}
		return lRes;
	},
	setJMSCallbacks: function (aListeners) {
		if (!aListeners) {
			aListeners = {};
		}
		if (aListeners.OnHandleJmsText !== undefined) {
			this.OnHandleJmsText = aListeners.OnHandleJmsText;
		}
		if (aListeners.OnHandleJmsTextMessage !== undefined) {
			this.OnHandleJmsTextMessage = aListeners.OnHandleJmsTextMessage;
		}
		if (aListeners.OnHandleJmsMap !== undefined) {
			this.OnHandleJmsMap = aListeners.OnHandleJmsMap;
		}
		if (aListeners.OnHandleJmsMapMessage !== undefined) {
			this.OnHandleJmsMapMessage = aListeners.OnHandleJmsMapMessage;
		}
		if (aListeners.OnPing !== undefined) {
			this.OnPing = aListeners.OnPing;
		}
		if (aListeners.OnIdentify !== undefined) {
			this.OnIdentify = aListeners.OnIdentify;
		}
		if (aListeners.OnBrokerTransportInterupted !== undefined) {
			this.OnBrokerTransportInterupted = aListeners.OnBrokerTransportInterupted;
		}
		if (aListeners.OnBrokerTransportResumed !== undefined) {
			this.OnBrokerTransportResumed = aListeners.OnBrokerTransportResumed;
		}
		if (aListeners.OnBrokerTransportException !== undefined) {
			this.OnBrokerTransportException = aListeners.OnBrokerTransportException;
		}
		if (aListeners.OnEndPointConnected !== undefined) {
			this.OnEndPointConnected = aListeners.OnEndPointConnected;
		}
		if (aListeners.OnEndPointDisconnected !== undefined) {
			this.OnEndPointDisconnected = aListeners.OnEndPointDisconnected;
		}
	}

};
// add the JMSPlugIn PlugIn into the jWebSocketTokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.JMSPlugIn);
