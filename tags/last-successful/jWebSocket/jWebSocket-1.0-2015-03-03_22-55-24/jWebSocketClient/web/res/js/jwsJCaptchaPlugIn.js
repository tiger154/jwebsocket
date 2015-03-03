//	---------------------------------------------------------------------------
//	jWebSocket JCaptcha Client Plug-in (Community Edition, CE)
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
//:class:*:jws.JCaptchaPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.JCaptchaPlugIn[/tt] class.
jws.JCaptchaPlugIn = {
	//:const:*:NS:String:org.jwebsocket.plugins.jcaptcha (jws.NS_BASE + ".plugins.jcaptcha")
	//:d:en:Namespace for the [tt]JCaptchaPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.jcaptcha",
	processToken: function(aToken) {
		// check if namespace matches
		if (aToken.ns == jws.JCaptchaPlugIn.NS) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if ("getcaptcha" == aToken.reqType) {
				if (this.OnCaptcha) {
					this.OnCaptcha(aToken);
				}
			}
		}
	},
	//:m:*:captchaGenerate
	//:d:en:Generate a new cpatcha image
	//:a:en::aOptions:Object:Optional arguments for the raw client sendToken method.
	//:r:*:::void:none
	captchaGenerate: function(aImageType, aOptions) {
		var lRes = this.checkConnected();
		if (0 == lRes.code) {
			if (!aOptions) {
				aOptions = {};
			}
			var lToken = {
				ns: jws.JCaptchaPlugIn.NS,
				type: 'getcaptcha',
				imageType: aImageType
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	//:m:*:setCaptchaCallbacks
	//:d:en:Set the sms plug-in lifecycle callbacks
	//:a:en::aListeners:Object:JSONObject containing the jcaptcha plug-in lifecycle callbacks
	//:a:en::aListeners.OnCaptcha:Function:Called when a new captcha is obtained from the server
	//:r:*:::void:none
	setCaptchaCallbacks: function(aListeners) {
		if (!aListeners) {
			aListeners = {};
		}
		if (aListeners.OnCaptcha !== undefined) {
			this.OnCaptcha = aListeners.OnCaptcha;
		}
	}
};

// add the JWebSocket SMS PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.JCaptchaPlugIn);
