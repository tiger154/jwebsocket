//	---------------------------------------------------------------------------
//	jWebSocket SMS Client Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
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

//:package:*:jws
//:class:*:jws.SMSPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.SMSPlugIn[/tt] class.
jws.SMSPlugIn = {
	//:const:*:NS:String:org.jwebsocket.plugins.sms (jws.NS_BASE + ".plugins.sms")
	//:d:en:Namespace for the [tt]SMSPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.sms",
	processToken: function(aToken) {
		// check if namespace matches
		if (aToken.ns == jws.SMSPlugIn.NS) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if ("generateReport" == aToken.reqType) {
				if (this.OnReport) {
					this.OnReport(aToken);
				}
			}
			else if ("sendSMS" == aToken.reqType) {
				if (this.OnSentSMS) {
					this.OnSentSMS(aToken);
				}
			}
		}
	},
	//:m:*:smsGenerateReport
	//:d:en:Generate a report with the user SMS activity
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:a:en::aOptions.username:String:Optional argument for admin users that allows to generate other users report.
	//:r:*:::void:none
	smsGenerateReport: function(aOptions) {
		var lRes = this.checkConnected();
		if (0 == lRes.code) {
			if (!aOptions) {
				aOptions = {};
			}
			var lToken = {
				ns: jws.SMSPlugIn.NS,
				type: 'generateReport',
				username: aOptions['username']
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	//:m:*:smsSend
	//:d:en:Send an SMS
	//:a:en::aTo:String:The SMS destinatary 
	//:a:en::aFrom:String:The SMS sender
	//:a:en::aMessage:String:The SMS message
	//:a:en::aState:String:The SMS type
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	smsSend: function(aTo, aFrom, aMessage, aState, aOptions) {
		var lRes = this.checkConnected();
		if (0 == lRes.code) {
			var lToken = {
				ns: jws.SMSPlugIn.NS,
				type: "sendSMS",
				to: aTo,
				from: aFrom,
				message: aMessage,
				state: aState
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	//:m:*:setSMSCallbacks
	//:d:en:Set the sms plug-in lifecycle callbacks
	//:a:en::aListeners:Object:JSONObject containing the sms plug-in lifecycle callbacks
	//:a:en::aListeners.OnSentSMS:Function:Called when an SMS message has been sent.
	//:a:en::aListeners.OnReport:Function:Called when an report has been generated.
	//:r:*:::void:none
	setSMSCallbacks: function(aListeners) {
		if (!aListeners) {
			aListeners = {};
		}
		if (aListeners.OnSentSMS !== undefined) {
			this.OnSentSMS = aListeners.OnSentSMS;
		}
		if (aListeners.OnReport !== undefined) {
			this.OnReport = aListeners.OnReport;
		}
	}
};

// add the JWebSocket SMS PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.SMSPlugIn);
