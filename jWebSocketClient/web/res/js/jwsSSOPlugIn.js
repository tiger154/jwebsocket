//	---------------------------------------------------------------------------
//	jWebSocket SSO client PlugIn (Community Edition, CE)
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

// this plug-in is based on: 
// http://tools.ietf.org/html/rfc6749 - The OAuth 2.0 Authorization Framework
// http://tools.ietf.org/html/rfc6750 - The OAuth 2.0 Authorization Framework: Bearer Token Usage
// http://oauth.net/2/ - OAuth 2.0

jws.SSOPlugIn = {
	// namespace for SSO plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.sso",
	APP_URL: null,
	OAUTH_APP_ID: null, // global (static) application id (not instance app_id)!
	OAUTH_APP_SECRET: null, // global (static) application secret (not instance app_secret)!
	JWS_SSO_COOKIE: "JWSSSO",
	SSO_SESSION_COOKIE_NAME: "SSOSESSION",
	DEFAULT_TIMEOUT: 10000,
	XHR_ASYNCHRONOUS: true,
	XHR_SYNCHRONOUS: false,
	OAUTH_HOST: "https://localhost",
	OAUTH_GETSESSION_URL: "/get-smsession",
	OAUTH_AUTHSESSION_URL: "/auth/oauth/v2/token",
	OAUTH_GETUSER_URL: "/use-token",
	OAUTH_REFRESHTOKEN_URL: "/auth/oauth/v2/token",
	OAUTH_INVALIDATETOKEN_URL: "/delete-token",
	//
	processToken: function(aToken) {
		// check if namespace matches
		if (aToken.ns === jws.SSOPlugIn.NS) {
			// here you can handle incoming tokens from the server
			// directy in the plug-in if desired.
//			if( "abc" == aToken.reqType ) {
//				// this is just for demo purposes
//				// don't use blocking calls here which block the communication!
//				// like alert( "jWebSocket Server returned: " + aToken.time );
//				if( this.OnSSOxyz ) {
//					this.OnSSOxyz( aToken );
//				}
//			}
		}
	},
	//
	mGetXHR: function( ) {
		var lXHR;

		// try to get XMLHttpRequest object from the browser
		if (window.XMLHttpRequest) { // Mozilla, Safari, ...
			lXHR = new XMLHttpRequest();
			if (lXHR.overrideMimeType)
				lXHR.overrideMimeType("text/xml");
		}
		else { // IE
			try {
				lXHR = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (lEx) {
			}
			if (typeof httpRequest === "undefined") {
				try {
					lXHR = new ActiveXObject("Microsoft.XMLHTTP");
				} catch (lEx) {
				}
			}
		}
		if (!lXHR) {
			throw "Cannot create an XMLHTTP instance";
			return false;
		}
		return lXHR;
	},
	//
	// checks if there is already a local SSO store 
	// for this instance and creates one if not.
	ssoCheckStore: function() {
		if (!this.sso) {
			this.sso = {};
		}
	},
	//
	ssoSetSessionCookieName: function(aSessionCookie) {
		jws.SSOPlugIn.SSO_SESSION_COOKIE_NAME = aSessionCookie;
	},
	//
	ssoGetSessionCookieName: function() {
		return jws.SSOPlugIn.SSO_SESSION_COOKIE_NAME;
	},
	ssoSetOAuthHost: function(aHost) {
		// just set the base_url property
		jws.SSOPlugIn.OAUTH_HOST = aHost;
	},
	ssoSetAppURL: function(aURL) {
		// just set the application property to get the SM session cookie
		jws.SSOPlugIn.APP_URL = aURL;
	},
	//
	// sets the default timeout for all requests
	ssoSetDefaultTimeout: function(aDefTimeout) {
		// just set the default timeout property
		jws.SSOPlugIn.DEFAULT_TIMEOUT = aDefTimeout;
	},
	//
	// returns the default timeout for all requests
	ssoGetDefaultTimeout: function() {
		return jws.SSOPlugIn.DEFAULT_TIMEOUT;
	},
	//
	// sets the global (static) OAuth application Id
	ssoSetOAuthAppId: function(aAppId) {
		// just set the static app id property
		jws.SSOPlugIn.OAUTH_APP_ID = aAppId;
	},
	//
	// returns the global (static) OAuth application Id
	ssoGetOAuthAppId: function() {
		return jws.SSOPlugIn.OAUTH_APP_ID;
	},
	//
	// sets the global (static) OAuth application secret
	ssoSetOAuthAppSecret: function(aAppSecret) {
		// just set the static app secret property
		jws.SSOPlugIn.OAUTH_APP_SECRET = aAppSecret;
	},
	//
	// returns the global (static) OAuth application secret
	ssoGetOAuthAppSecret: function() {
		return jws.SSOPlugIn.OAUTH_APP_SECRET;
	},
	//
	// returns the current sso user name, if such already exists
	ssoGetUsername: function() {
		return (this.sso && this.sso.username ? this.sso.username : null);
	},
	//
	// returns the current sso access token, if such already exists
	ssoGetAccessToken: function() {
		return (this.sso && this.sso.accessToken ? this.sso.accessToken : null);
	},
	//
	// returns the current sso refresh token, if such already exists
	ssoGetRefreshToken: function() {
		return (this.sso && this.sso.refreshToken ? this.sso.refreshToken : null);
	},
	ssoSetSessionId: function(aSessionId) {
		this.ssoCheckStore();
		this.sso.sessionId = aSessionId;
	},
	ssoSetInstanceId: function(aInstanceId) {
		this.ssoCheckStore();
		this.sso.instanceId = aInstanceId;
	},
	ssoSetInstanceSecret: function(aInstanceSecret) {
		this.ssoCheckStore();
		this.sso.instanceSecret = aInstanceSecret;
	},
	ssoGetSessionId: function() {
		return(
				this.sso ? this.sso.sessionId : null
				);
	},
	//
	// if instance app id is set this will be returned,
	// otherwise the global (static) app id will be returned
	ssoGetAppId: function() {
		return(
				this.sso && this.sso.instanceId
				? this.sso.instanceId
				: jws.SSOPlugIn.OAUTH_APP_ID
				);
	},
	//
	// if instance app secret is set this will be returned,
	// otherwise the global (static) app secret will be returned
	ssoGetAppSecret: function() {
		return(
				this.sso && this.sso.instanceSecret
				? this.sso.instanceSecret
				: jws.SSOPlugIn.OAUTH_APP_SECRET
				);
	},
	//
	// loads the OAuth data from the local OAuth store (the jWS cookie)
	ssoLoadOAuthData: function(aOptions) {
		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			OnSuccess: null,
			OnFailure: null,
			cookie: null
		});
		var lCookie = (aOptions.cookie ? aOptions.cookie : document.cookie);
		// all cookies in the browser are split by semicolons and a space
		var lCookies = lCookie.split("; ");
		var lFound = false;
		// if cookie(s) found...
		if (lCookies) {
			// iterate through them to find the SSO JWS_SSO_COOKIE
			// as well as the session cookie as far as exists
			this.ssoCheckStore();
			// delete this.sso.sessionId;
			for (var lIdx = 0; lIdx < lCookies.length; lIdx++) {
				var lSplitPos = lCookies[ lIdx ].indexOf("=");
				if (lSplitPos > 0) {
					var lKey = lCookies[ lIdx ].substr(0, lSplitPos);
					var lValue = lCookies[ lIdx ].substr(lSplitPos + 1);
					// do we read the jWebSocket SSO cookie?
					if (jws.SSOPlugIn.JWS_SSO_COOKIE === lKey) {
						// TODO: process exception
						var lJSON;
						try {
							lJSON = JSON.parse(lValue);
							// the cookie contains both the access and refresh token 
							this.sso.refreshToken = lJSON.refreshToken;
							this.sso.accessToken = lJSON.accessToken;
							this.sso.expiration = lJSON.expiration;
							this.sso.expires = new Date(lJSON.expires);
							lFound = true;
						} catch (lEx) {
							break;
						}
						// do we read the SSO server cookie?
					} else if (jws.SSOPlugIn.SSO_SESSION_COOKIE_NAME === lKey) {
						this.sso.sessionId = lValue;
						lFound = true;
					}
				}
			}
		}

		if (lFound) {
			if (aOptions.OnSuccess) {
				// prepare success event to be fired
				var lToken = {};
				lToken.refreshToken = this.sso.refreshToken;
				lToken.accessToken = this.sso.accessToken;
				if (this.sso.expires) {
					// expiration is in seconds
					lToken.expiration =
							Math.round(
									(this.sso.expires.getTime() -
											new Date().getTime()) / 1000
									);
					if (lToken.expiration < 0) {
						lToken.expiration = 0;
					}
					lToken.expires = this.sso.expires;
					lToken.expired = (lToken.expires.getTime() - new Date().getTime()) <= 0;
				}
				if (this.sso.username) {
					lToken.username = this.sso.username;
				}
				if (this.sso.sessionId) {
					lToken.sessionId = this.sso.sessionId;
				}
				aOptions.OnSuccess(lToken);
			}
		} else {
			if (aOptions.OnFailure) {
				var lToken = {
					code: -1,
					msg: "No valid SSO/OAuth cookie found."
				};
				aOptions.OnFailure(lToken);
			}
		}
	},
	//
	// saves the OAuth data to the local OAuth store (the jWS cookie)
	ssoSaveOAuthData: function(aOptions) {
		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			OnSuccess: null,
			OnFailure: null
		});

		var lCookie;
		var lSet = false;
		try {
			lCookie = JSON.parse(document.cookie);
		} catch (lEx) {
			// if existing cookie for whatever reason cannot be parsed
			// just create an empty new one.
			lCookie = {};
		}
		if (this.sso && this.sso.refreshToken) {
			lCookie.refreshToken = this.sso.refreshToken;
			lCookie.accessToken = this.sso.accessToken;
			lCookie.expiration = this.sso.expiration;
			lCookie.expires = this.sso.expires;
			lCookie.username = this.sso.username;
			lCookie.email = this.sso.email;
			lCookie.sessionId = this.sso.sessionId;
			var lExpires = new Date();
			lExpires.setTime(new Date().getTime() + 86400 * 1000);
			var lExpValue = lExpires.toGMTString();
			document.cookie = jws.SSOPlugIn.JWS_SSO_COOKIE
					+ "=" + JSON.stringify(lCookie)
					+ "; expires=" + lExpValue;
			if (aOptions.OnSuccess) {
				aOptions.OnSuccess(lCookie);
			}
			lSet = true;
		}

		if (!lSet) {
			if (aOptions.OnFailure) {
				aOptions.OnFailure({
					code: -1,
					msg: "SSO cookie not set due to missing refresh token."
				});
			}
		}
	},
	//
	//
	ssoResetOAuthData: function(aOptions) {
		// pending
	},
	//
	//
	ssoGetSSOSession: function(aUsername, aPassword, aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.OAUTH_HOST + OAUTH_GETSESSION_URL
		});

		// get browser's XHR object
		var lXHR = jws.SSOPlugIn.mGetXHR();

		// save instance of WebSocket client to
		// save OAuth data (username and organization tokens)
		var lInstance = this;
		var hTimeout = null;

		lXHR.open("GET", aOptions.URL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		var lCredentials = Base64.encode(aUsername + ":" + aPassword);
		lXHR.setRequestHeader("Authorization", "Basic " + lCredentials);
		lXHR.setRequestHeader("Cache-Control", "no-cache");

		var lResponseText = "";
		lXHR.onreadystatechange = function() {
			jws.console.debug(
					lXHR.readyState
					+ ", " + lXHR.status
					+ ", " + lXHR.responseText
					);
			if (lXHR.readyState === 3) {
				lResponseText = lXHR.responseText;
			}
			if (lXHR.readyState >= 4) {
				clearTimeout(hTimeout);
				if (lXHR.status === 200) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					lInstance.ssoCheckStore();
					var lJSON;
					try {
						lJSON = JSON.parse(lResponseText);
						if (0 === lJSON.code) {
							if (aOptions.OnSuccess) {
								lInstance.sso.sessionId = lJSON.smsession;
								// getting a new session id invalidates the old one
								lInstance.sso.email = null;
								lInstance.sso.username = null;
								lInstance.sso.accessToken = null;
								lInstance.sso.refreshToken = null;
								lInstance.sso.expiration = null;
								lInstance.sso.expires = null;
								aOptions.OnSuccess({
									code: lJSON.code,
									sessionId: lJSON.smsession,
									msg: "ok"
								});
							}
						} else {
							if (aOptions.OnFailure) {
								aOptions.OnFailure({
									code: lJSON.code,
									sessionId: lJSON.smsession,
									msg: lJSON.smsession
								});
							}
						}
					} catch (lEx) {
						if (aOptions.OnFailure) {
							aOptions.OnFailure({
								code: -1,
								msg: "JSON parse error",
								status: lXHR.status,
								text: lResponseText
							});
						}
					}
					/*
					 if (lResponseText) {
					 lInstance.ssoLoadOAuthData({
					 cookie: lJSON.cookie,
					 });
					 if (lInstance.sso.sessionId) {
					 lInstance.ssoSaveOAuthData();
					 }
					 if (aOptions.OnSuccess) {
					 aOptions.OnSuccess({
					 code: 0,
					 sessionId: lInstance.sso.sessionId,
					 msg: "ok"
					 });
					 }
					 }
					 */
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== ""
									? lXHR.statusText
									: "No response from OAuth server " + aOptions.URL)
						});
					}
				}
			}
		};

		// set POST body according to OAuth settings 
		// to obtain user name and organziation from access_token
		var lPostBody = null;

		hTimeout = setTimeout(function( ) {
			hTimeout = null;
			if (aOptions.OnTimeout) {
				aOptions.OnTimeout({
					code: -1,
					msg: "timeout"
				});
			}
			lXHR.abort();
		}, aOptions.timeout);

		lXHR.send(lPostBody);
	},
	//
	//
	ssoAuthSession: function(aSessionId, aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.OAUTH_HOST + jws.SSOPlugIn.OAUTH_AUTHSESSION_URL
		});
		this.ssoCheckStore();

		// get browser's XHR object
		var lXHR = jws.SSOPlugIn.mGetXHR();

		aSessionId = encodeURIComponent(aSessionId);

		var lURL = aOptions.URL;
		lXHR.open("POST", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);

		lXHR.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		// lXHR.setRequestHeader("Content-Type", "application/xml");
		// lXHR.setRequestHeader("Cache-Control", "no-cache");
		lXHR.setRequestHeader("Authorization",
				"Basic " + Base64.encode(this.ssoGetAppId()
						+ ":" + this.ssoGetAppSecret()));

		// save instance of WebSocket client to
		// save OAuth data (access and refresh tokens)
		var lInstance = this;
		var hTimeout = null;

		var lResponseText = "";
		lXHR.onreadystatechange = function() {
			jws.console.debug(
					lXHR.readyState
					+ ", " + lXHR.status
					+ ", " + lXHR.responseText
					);
			if (lXHR.readyState === 3) {
				lResponseText = lXHR.responseText;
			}
			if (lXHR.readyState >= 4) {
				clearTimeout(hTimeout);
				if (lXHR.status === 200 || lResponseText) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					if (lResponseText) {
						var lJSON;
						try {
							lJSON = JSON.parse(lResponseText);
						} catch (lEx) {
							if (aOptions.OnFailure) {
								aOptions.OnFailure({
									code: -1,
									msg: "JSON parse error",
									status: lXHR.status,
									text: lResponseText
								});
							}
						}
						if (lJSON.refresh_token && lJSON.access_token) {
							if (!lInstance.sso) {
								lInstance.sso = {};
							}
							lInstance.sso.refreshToken = lJSON.refresh_token;
							lInstance.sso.accessToken = lJSON.access_token;
							lInstance.sso.expiration = lJSON.expires_in;
							var lNow = new Date().getTime();
							var lLater = new Date();
							lLater.setTime(lNow + (lJSON.expires_in * 1000));
							lInstance.sso.expires = lLater;
							if (aOptions.OnSuccess) {
								aOptions.OnSuccess({
									code: 0,
									msg: "Ok",
									JSON: lJSON,
									text: lResponseText
								});
							}
						} else {
							if (aOptions.OnFailure) {
								aOptions.OnFailure({
									code: -1,
									msg: lJSON.error_description,
									JSON: lJSON,
									text: lResponseText
								});
							}
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== ""
									? lXHR.statusText
									: "No response from OAuth server " + aOptions.URL)
						});
					}
				}
			}
		};

		// set POST body according to OAuth settings 
		// for direct authentication, also refer to 
		// http://tools.ietf.org/html/rfc6750
		var lPostBody =
				"grant_type=password&" +
				jws.SSOPlugIn.SSO_SESSION_COOKIE_NAME + "=" + aSessionId;

		hTimeout = setTimeout(function( ) {
			hTimeout = null;
			if (aOptions.OnTimeout) {
				aOptions.OnTimeout({
					code: -1,
					msg: "timeout"
				});
			}
			lXHR.abort();
		}, aOptions.timeout);

		lXHR.send(lPostBody);
	},
	//
	//
	ssoAuthDirect: function(aUsername, aPassword, aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.BASE_URL
		});

		// get browser's XHR object
		var lXHR = jws.SSOPlugIn.mGetXHR();

		lXHR.open("POST", aOptions.URL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		lXHR.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;");
		lXHR.setRequestHeader("Cache-Control", "no-cache");

		// save instance of WebSocket client to
		// save OAuth data (access and refresh tokens)
		var lInstance = this;
		var hTimeout = null;

		var lResponseText = "";
		lXHR.onreadystatechange = function() {
			jws.console.debug(
					lXHR.readyState
					+ ", " + lXHR.status
					+ ", " + lXHR.responseText
					);
			if (lXHR.readyState === 3) {
				lResponseText = lXHR.responseText;
			}
			if (lXHR.readyState >= 4) {
				clearTimeout(hTimeout);
				if (lXHR.status === 200 || lResponseText) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					if (lResponseText) {
						var lJSON;
						try {
							lJSON = JSON.parse(lResponseText);
						} catch (lEx) {
							if (aOptions.OnFailure) {
								aOptions.OnFailure({
									code: -1,
									msg: "JSON parse error"
								});
							}
						}
						if (lJSON.refresh_token && lJSON.access_token) {
							if (!lInstance.sso) {
								lInstance.sso = {};
							}
							lInstance.sso.refreshToken = lJSON.refresh_token;
							lInstance.sso.accessToken = lJSON.access_token;
							lInstance.sso.expiration = lJSON.expires_in;
							var lNow = new Date().getTime();
							var lLater = new Date();
							lLater.setTime(lNow + (lJSON.expires_in * 1000));
							lInstance.sso.expires = lLater;
							if (aOptions.OnSuccess) {
								aOptions.OnSuccess({
									code: 0,
									msg: "Ok",
									JSON: lJSON,
									text: lResponseText
								});
							}
						} else {
							if (aOptions.OnFailure) {
								aOptions.OnFailure({
									code: -1,
									msg: lJSON.error_description,
									JSON: lJSON,
									text: lResponseText
								});
							}
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== ""
									? lXHR.statusText
									: "No response from OAuth server " + aOptions.URL)
						});
					}
				}
			}
		};

		// set POST body according to OAuth settings 
		// for direct authentication, also refer to 
		// http://tools.ietf.org/html/rfc6750
		var lPostBody =
				"client_id=ro_client"
				+ "&grant_type=password"
				+ "&username=" + encodeURIComponent(aUsername)
				+ "&password=" + encodeURIComponent(aPassword);

		hTimeout = setTimeout(function( ) {
			hTimeout = null;
			if (aOptions.OnTimeout) {
				aOptions.OnTimeout({
					code: -1,
					msg: "timeout"
				});
			}
			lXHR.abort();
		}, aOptions.timeout);

		lXHR.send(lPostBody);
	},
	//
	//
	ssoGetUser: function(aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.OAUTH_HOST + jws.SSOPlugIn.OAUTH_GETUSER_URL
		});

		if (!(this.sso && this.sso.accessToken)) {
			if (aOptions.OnFailure) {
				aOptions.OnFailure({
					code: -1,
					msg: "No access token available"
				});
			}
			return;
		}

		// get browser's XHR object
		var lXHR = jws.SSOPlugIn.mGetXHR();

		// save instance of WebSocket client to
		// save OAuth data (username and organization tokens)
		var lInstance = this;
		var hTimeout = null;

		var lURL = aOptions.URL;
		lXHR.open("POST", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		lXHR.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;");
		lXHR.setRequestHeader("Cache-Control", "no-cache");

		var lResponseText = "";
		lXHR.onreadystatechange = function() {
			jws.console.debug(
					lXHR.readyState
					+ ", " + lXHR.status
					+ ", " + lXHR.responseText
					);
			if (lXHR.readyState === 3) {
				lResponseText = lXHR.responseText;
			}
			if (lXHR.readyState >= 4) {
				clearTimeout(hTimeout);
				if (lXHR.status === 200 || lResponseText) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					if (lResponseText) {
						if (aOptions.OnSuccess) {
							var lJSON;
							try {
								lJSON = JSON.parse(lResponseText);
							} catch (lEx) {
								if (aOptions.OnFailure) {
									aOptions.OnFailure({
										code: -1,
										msg: "JSON parse error"
									});
								}
							}
							if (!lInstance.sso) {
								lInstance.sso = {};
							}
							lInstance.sso.username = lJSON.login_name;
							lInstance.sso.fullname = lJSON.full_user_name;
							lInstance.sso.email = lJSON.email;
							lInstance.sso.dn = lJSON.dn;
							/*
							 // lInstance.sso.orgname = lJSON.OrgName;
							 lInstance.sso.expiration = lJSON.expires_in;
							 var lNow = new Date().getTime();
							 var lLater = new Date();
							 lLater.setTime(lNow + (lJSON.expires_in * 1000));
							 lInstance.sso.expires = lLater;
							 */
							aOptions.OnSuccess({
								username: lInstance.sso.username,
								fullname: lInstance.sso.fullname,
								email: lInstance.sso.email,
								JSON: lJSON,
								text: lResponseText
							});
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== ""
									? lXHR.statusText
									: "No response from OAuth server " + aOptions.URL)
						});
					}
				}
			}
		};

		// set POST body according to OAuth settings 
		// to obtain user name and organziation from access_token
		var lPostBody =
				"access_token=" + encodeURIComponent(this.sso.accessToken);

		hTimeout = setTimeout(function( ) {
			hTimeout = null;
			if (aOptions.OnTimeout) {
				aOptions.OnTimeout({
					code: -1,
					msg: "timeout"
				});
			}
			lXHR.abort();
		}, aOptions.timeout);

		lXHR.send(lPostBody);
	},
	//
	//
	ssoRefreshAccessToken: function(aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.OAUTH_HOST + jws.SSOPlugIn.OAUTH_REFRESHTOKEN_URL
		});

		this.ssoCheckStore();
		if (!(this.sso.refreshToken)) {
			if (aOptions.OnFailure) {
				aOptions.OnFailure({
					code: -1,
					msg: "No refresh token available"
				});
			}
			return;
		}

		// get browser's XHR object
		var lXHR = jws.SSOPlugIn.mGetXHR();

		// for PI server:
		// lXHR.open("POST", aOptions.URL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		// for CA server
		var lURL = aOptions.URL;
		// + "?grant_type=refresh_token"
		// + "&refresh_token=" + encodeURIComponent(this.sso.refreshToken);
		lXHR.open("POST", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		lXHR.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;");
		// lXHR.setRequestHeader("Content-Type", "application/xml");
		lXHR.setRequestHeader("Cache-Control", "no-cache");
		lXHR.setRequestHeader("Authorization", "Basic "
				+ Base64.encode(this.ssoGetAppId()
						+ ":" + this.ssoGetAppSecret()));

		// save instance of WebSocket client to
		// obtain OAuth data (get refresh token and set new access token)
		var lInstance = this;
		var hTimeout = null;

		var lResponseText = "";
		lXHR.onreadystatechange = function() {
			jws.console.debug(
					lXHR.readyState
					+ ", " + lXHR.status
					+ ", " + lXHR.responseText
					);
			if (lXHR.readyState === 3) {
				lResponseText = lXHR.responseText;
			}
			if (lXHR.readyState >= 4) {
				clearTimeout(hTimeout);
				if (lXHR.status === 200 || lResponseText) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					if (lResponseText) {
						if (aOptions.OnSuccess) {
							var lJSON;
							try {
								// try to parse the JSON result
								lJSON = JSON.parse(lResponseText);
								// setup the SSO sub record
								if (!lInstance.sso) {
									lInstance.sso = {};
								}
								// check if JSON is complete and has access and refresh token
								if (lJSON
										&& lJSON.access_token
										&& lJSON.refresh_token) {
									lInstance.sso.accessToken = lJSON.access_token;
									lInstance.sso.refreshToken = lJSON.refresh_token;
									aOptions.OnSuccess({
										JSON: lJSON,
										text: lResponseText
									});
								} else {
									if (aOptions.OnFailure) {
										aOptions.OnFailure({
											code: -1,
											msg: "No both valid access and resfreh token detected in OAuth result",
											status: lXHR.status,
											json: lJSON,
											text: lResponseText
										});
									}
								}
							} catch (lEx) {
								if (aOptions.OnFailure) {
									aOptions.OnFailure({
										code: -1,
										msg: "JSON parse error",
										status: lXHR.status,
										text: lResponseText
									});
								}
							}
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== ""
									? lXHR.statusText
									: "No response from OAuth server " + aOptions.URL)
						});
					}
				}
			}
		};

		var lPostBody =
				"grant_type=refresh_token"
				+ "&refresh_token=" + encodeURIComponent(this.sso.refreshToken);

		hTimeout = setTimeout(function( ) {
			hTimeout = null;
			if (aOptions.OnTimeout) {
				aOptions.OnTimeout({
					code: -1,
					msg: "timeout"
				});
			}
			lXHR.abort();
		}, aOptions.timeout);

		lXHR.send(lPostBody);
	},
	//
	//
	ssoGetAccessTokenExpiration: function(aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null
		});

		if (this.sso && this.sso.expires) {
			var lRemaining =
					Math.round(
							(this.sso.expires.getTime() -
									new Date().getTime()) / 1000);
			if ((this.sso.expires.getTime() - new Date().getTime()) <= 0) {
				if (aOptions.OnSuccess) {
					aOptions.OnSuccess({
						code: 0,
						msg: "Access token is expired."
					});
				}
			} else {
				if (aOptions.OnSuccess) {
					aOptions.OnSuccess({
						code: 0,
						msg: ""
					});
				}
			}
		} else {
			if (aOptions.OnFailure) {
				aOptions.OnFailure({
					code: -1,
					msg: "No valid access token available."
				});
			}
			return;
		}
	},
	//
	//
	ssoInvalidateAccessToken: function(aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.OAUTH_HOST + jws.SSOPlugIn.OAUTH_INVALIDATETOKEN_URL
		});

		// get browser's XHR object
		var lXHR = jws.SSOPlugIn.mGetXHR();

		var lURL = aOptions.URL + "?access_token=" + encodeURIComponent(this.sso.accessToken);
		lXHR.open("GET", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		lXHR.setRequestHeader("Cache-Control", "no-cache");

		// save instance of WebSocket client to obtain OAuth data 
		// get refresh token and set new access token
		var lInstance = this;
		var hTimeout = null;

		var lResponseText = "";
		lXHR.onreadystatechange = function() {
			jws.console.debug(
					lXHR.readyState
					+ ", " + lXHR.status
					+ ", " + lXHR.responseText
					);
			if (lXHR.readyState === 3) {
				lResponseText = lXHR.responseText;
			}
			if (lXHR.readyState >= 4) {
				clearTimeout(hTimeout);
				if (lXHR.status === 200 || lResponseText) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					if (lResponseText) {
						if (aOptions.OnSuccess) {
							var lJSON;
							try {
								lJSON = JSON.parse(lResponseText);
								aOptions.OnSuccess({
									JSON: lJSON,
									text: lResponseText
								});
							} catch (lEx) {
								if (aOptions.OnFailure) {
									aOptions.OnFailure({
										code: -1,
										msg: "JSON parse error"
									});
								}
							}
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0
									? lXHR.status
									: -1),
							msg: (lXHR.statusText !== ""
									? lXHR.statusText
									: "No response from OAuth server " + aOptions.URL)
						});
					}
				}
			}
		};

		var lPostBody = null;

		hTimeout = setTimeout(function( ) {
			hTimeout = null;
			if (aOptions.OnTimeout) {
				aOptions.OnTimeout({
					code: -1,
					msg: "timeout"
				});
			}
			lXHR.abort();
		}, aOptions.timeout);

		lXHR.send(lPostBody);
	},
	//
	//
	ssoSetRefreshTokenInterval: function(aInterval, aOptions) {
		this.ssoCheckStore();
		if (aInterval <= 0) {
			if (this.sso.hRefrTokenIntv) {
				clearInterval(this.sso.hRefrTokenIntv);
				this.sso.hRefrTokenIntv = null;
			}
		} else {
			var lInstance = this;
			this.sso.hRefrTokenIntv = setInterval(function() {
				if (lInstance.sso.accessToken && lInstance.sso.refreshToken) {
					jws.console.debug("Refreshing access token...");
					lInstance.ssoRefreshAccessToken(aOptions);
				}
			}, aInterval);
		}
	},
	//
	//
	ssoClearRefreshTokenInterval: function() {
		this.ssoSetRefreshTokenInterval(-1, null);
	},
	//
	//
	setSSOCallbacks: function(aListeners) {
		if (!aListeners) {
			aListeners = {};
		}
		if (aListeners.OnRefreshingAccessToken !== undefined) {
			this.OnRefreshingAccessToken = aListeners.OnRefreshingAccessToken;
		}
		if (aListeners.OnRefreshAccessTokenSuccess !== undefined) {
			this.OnRefreshAccessTokenSuccess = aListeners.OnRefreshAccessTokenSuccess;
		}
		if (aListeners.OnRefreshAccessTokenFailure !== undefined) {
			this.OnRefreshAccessTokenFailure = aListeners.OnRefreshAccessTokenFailure;
		}
		if (aListeners.OnRefreshAccessTokenTimeout !== undefined) {
			this.OnRefreshAccessTokenTimeout = aListeners.OnRefreshAccessTokenTimeout;
		}
	},

	//
	//
	ssoGetURL: function(aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: ""
		});

		// get browser's XHR object
		var lXHR = jws.SSOPlugIn.mGetXHR();

		var lURL = aOptions.URL;
		lXHR.open("GET", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);

		// lXHR.setRequestHeader("Cache-Control", "no-cache");

		// save instance of WebSocket client to obtain OAuth data 
		// get refresh token and set new access token
		var lInstance = this;
		var hTimeout = null;

		var lResponseText = "";
		lXHR.onreadystatechange = function() {
			jws.console.debug(
					lXHR.readyState
					+ ", " + lXHR.status
					+ ", " + lXHR.responseText
					);
			if (lXHR.readyState === 3) {
				lResponseText = lXHR.responseText;
			}
			if (lXHR.readyState >= 4) {
				clearTimeout(hTimeout);
				if (lXHR.status === 200 || lResponseText) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					if (lResponseText) {
						if (aOptions.OnSuccess) {
							aOptions.OnSuccess({
								code: 0,
								msg: "ok",
								html: lResponseText
							});
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0
									? lXHR.status
									: -1),
							msg: (lXHR.statusText !== ""
									? lXHR.statusText
									: "No response from host " + aOptions.URL)
						});
					}
				}
			}
		};

		var lPostBody = null;

		hTimeout = setTimeout(function( ) {
			hTimeout = null;
			if (aOptions.OnTimeout) {
				aOptions.OnTimeout({
					code: -1,
					msg: "timeout"
				});
			}
			lXHR.abort();
		}, aOptions.timeout);

		lXHR.send(lPostBody);
	}
	
};

// add the JWebSocket SSO PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.SSOPlugIn);
