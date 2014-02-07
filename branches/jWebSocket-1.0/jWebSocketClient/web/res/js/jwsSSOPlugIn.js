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
	BASE_URL: null,
	APP_URL: null,
	CLIENT_SECRET: null,
	JWS_SSO_COOKIE: "JWSSSO",
	SMSESSION_COOKIE: "SMSESSION",
	DEFAULT_TIMEOUT: 10000,
	XHR_ASYNCHRONOUS: true,
	XHR_SYNCHRONOUS: false,
	
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
	
	ssoSetDefaultTimeout: function(aDefTimeout) {
		// just set the base_url property
		jws.SSOPlugIn.DEFAULT_TIMEOUT = aDefTimeout;
	},
	
	ssoSetHost: function(aURL) {
		// just set the base_url property
		jws.SSOPlugIn.BASE_URL = aURL;
	},
	
	ssoSetAppURL: function(aURL) {
		// just set the application property to get the SM session cookie
		jws.SSOPlugIn.APP_URL = aURL;
	},
	
	ssoSetSecret: function(aSecret) {
		// just set the client_secret property
		jws.SSOPlugIn.CLIENT_SECRET = aSecret;
	},
	
	ssoGetUsername: function() {
		return (this.sso && this.sso.username ? this.sso.username : null);
	},
	
	ssoGetAccessToken: function() {
		return (this.sso && this.sso.accessToken ? this.sso.accessToken : null);
	},
	
	ssoGetRefreshToken: function() {
		return (this.sso && this.sso.refreshToken ? this.sso.refreshToken : null);
	},
	
	ssoLoadOAuthData: function(aOptions) {
		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			OnSuccess: null,
			OnFailure: null,
			cookie: null
		});
		var lCookie = ( aOptions.cookie ? aOptions.cookie : document.cookie );
		// all cookies in the browser are split by semicolons and a space
		var lCookies = lCookie.split("; ");
		var lFound = false;
		// if cookie(s) found...
		if (lCookies) {
			// iterate through them to find the SSO JWS_SSO_COOKIE
			// as well as the SMSESSION cookie as far as exist
			if (!this.sso) {
				this.sso = {};
			}
			delete this.sso.smsession;
			for (var lIdx = 0; lIdx < lCookies.length; lIdx++) {
				var lSplitPos = lCookies[ lIdx ].indexOf("=");
				if (lSplitPos > 0) {
					var lKey = lCookies[ lIdx ].substr(0, lSplitPos);
					var lValue = lCookies[ lIdx ].substr(lSplitPos + 1);
					if (jws.SSOPlugIn.JWS_SSO_COOKIE === lKey) {
						// TODO: process exception
						var lJSON;
						try {
							lJSON = JSON.parse(lValue);
						} catch (lEx) {
							break;
						}
						// the cookie contains both the access and refresh token 
						this.sso.refreshToken = lJSON.refreshToken;
						this.sso.accessToken = lJSON.accessToken;
						this.sso.expiration = lJSON.expiration;
						this.sso.expires = new Date(lJSON.expires);
						lFound = true;
					} else if (!this.sso.smsession
							&& jws.SSOPlugIn.SMSESSION_COOKIE === lKey) {
						this.sso.smsession = lValue;
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
				} else {
					lToken.expiration = 0;
					lToken.expires = new Date();
				}
				lToken.expired = (lToken.expires.getTime() - new Date().getTime()) <= 0;
				if (this.sso.username) {
					lToken.username = this.sso.username;
				}
				if (this.sso.smsession) {
					lToken.smsession = this.sso.smsession;
				}
				aOptions.OnSuccess(lToken);
			}
		} else {
			if (aOptions.OnFailure) {
				aOptions.OnFailure({
					code: -1,
					msg: "No valid OAuth cookie found."
				});
			}
		}
	},
	
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
			lCookie = {};
		}
		if (this.sso && this.sso.refreshToken) {
			lCookie.refreshToken = this.sso.refreshToken;
			lCookie.accessToken = this.sso.accessToken;
			lCookie.expiration = this.sso.expiration;
			lCookie.expires = this.sso.expires;
			lCookie.username = this.sso.username;
			lCookie.smsession = this.sso.smsession;
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
	
	ssoResetOAuthData: function(aOptions) {
		// pending
	},
	
	ssoGetSSOSession: function(aOptions) {

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

		// save instance of WebSocket client to
		// save OAuth data (username and organization tokens)
		var lInstance = this;
		var hTimeout = null;

		lXHR.open("POST", aOptions.URL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		var lCredentials = Base64.encode("aschulze:Div#2014");
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
				if (lXHR.status === 200 || lResponseText) {
					if (lXHR.responseText) {
						lResponseText = lXHR.responseText;
					}
					if (lResponseText) {
						lInstance.ssoLoadOAuthData();
						if (lInstance.sso.smsession) {
							lInstance.ssoSaveOAuthData();
						}
						if (aOptions.OnSuccess) {
							aOptions.OnSuccess({
								code: 0,
								smsession: lInstance.sso.smsession,
								msg: "ok"
							});
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== "" ? lXHR.statusText : "failure")
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
	
	ssoAuthSession: function(aSession, aClientID, aClientSecret, aOptions) {

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
		var lURL = aOptions.URL + "?grant_type=password";
		lXHR.open("POST", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		
		// for PI servers:
		lXHR.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;");
		// for CA servers:
		// lXHR.setRequestHeader("Content-Type", "application/xml");
		// lXHR.setRequestHeader("Cache-Control", "no-cache");
		lXHR.setRequestHeader("Authorization", "Basic " + Base64.encode(aClientID + ":" + aClientSecret));
		// debugger;
		// lXHR.setDisableHeaderCheck(true);
		// lXHR.setRequestHeader("Cookie", "SMSESSION=" + aSession);

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
									response: lResponseText
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
							msg: (lXHR.statusText !== "" ? lXHR.statusText : "failure")
						});
					}
				}
			}
		};

		// set POST body according to OAuth settings 
		// for direct authentication, also refer to 
		// http://tools.ietf.org/html/rfc6750
		console.log("Session: " + aSession);
		var lPostBody = // null;
				// "grant_type=password&";
				"smsession=" + encodeURIComponent(aSession);
				// "client_id=ro_client"

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
							msg: (lXHR.statusText !== "" ? lXHR.statusText : "failure")
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
	
	ssoGetUser: function(aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.BASE_URL
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

		// for PI server:
		// lXHR.open("POST", jws.SSOPlugIn.BASE_URL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		// lXHR.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;");
		// for CA server:
		var lURL = aOptions.URL + "?access_token=" + encodeURIComponent(this.sso.accessToken);
		lXHR.open("GET", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		lXHR.setRequestHeader("Content-Type", "application/json");
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
				debugger;
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
							lInstance.sso.username = lJSON.access_token.username;
							lInstance.sso.orgname = lJSON.access_token.OrgName;
							lInstance.sso.expiration = lJSON.expires_in;
							var lNow = new Date().getTime();
							var lLater = new Date();
							lLater.setTime(lNow + (lJSON.expires_in * 1000));
							lInstance.sso.expires = lLater;
							aOptions.OnSuccess({
								username: lInstance.sso.username,
								JSON: lJSON,
								text: lResponseText
							});
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== "" ? lXHR.statusText : "failure")
						});
					}
				}
			}
		};

		// set POST body according to OAuth settings 
		// to obtain user name and organziation from access_token
		// for PI server:
		/*
		var lPostBody =
				"client_id=rs_client"
				+ "&client_secret=" + jws.SSOPlugIn.CLIENT_SECRET
				+ "&grant_type=" + "urn:pingidentity.com:oauth2:grant_type:validate_bearer"
				+ "&token=" + this.sso.accessToken;
		*/
		// for CA server:
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
	
	ssoRefreshAccessToken: function(aOptions) {

		// set default options for OAuth call
		aOptions = jws.getOptions(aOptions, {
			timeout: jws.SSOPlugIn.DEFAULT_TIMEOUT,
			OnSuccess: null,
			OnFailure: null,
			OnTimeout: null,
			URL: jws.SSOPlugIn.BASE_URL
		});

		if (!(this.sso && this.sso.refreshToken)) {
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
		// lXHR.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;");
		// for CA server
		var lURL = aOptions.URL
				+ "?grant_type=refresh_token" 
				+ "&refresh_token=" + encodeURIComponent(this.sso.refreshToken);
		lXHR.open("POST", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		lXHR.setRequestHeader("Content-Type", "application/json");
		lXHR.setRequestHeader("Cache-Control", "no-cache");
		lXHR.setRequestHeader("Authorization", "Basic " + Base64.encode("nvidia:secret"));

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
							lInstance.sso.accessToken = lJSON.access_token;
							if( lJSON.refresh_token ) {
								lInstance.sso.refreshToken = lJSON.refresh_token;
							}
							aOptions.OnSuccess({
								JSON: lJSON,
								text: lResponseText
							});
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== "" ? lXHR.statusText : "failure")
						});
					}
				}
			}
		};

		// set POST body according to OAuth settings 
		// to obtain new access token based on saved refresh token
		// for PI server
		/*
		var lPostBody =
				"client_id=ro_client"
				+ "&grant_type=" + "refresh_token"
				+ "refresh_token=" + this.sso.refreshToken;
		*/
	   // for CA server
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
	
	ssoGetSMSessionCookie: function(aOptions) {

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

		var lURL = aOptions.URL;
		lXHR.open("GET", lURL, jws.SSOPlugIn.XHR_ASYNCHRONOUS);
		// lXHR.setRequestHeader("Content-Type", "application/json");
		// lXHR.setRequestHeader("Cache-Control", "no-cache");
		// lXHR.setRequestHeader("Authorization", "Basic " + Base64.encode("nvidia:secret"));
		lXHR.setRequestHeader("Authorization", "Basic " + Base64.encode("aschulze:Div#2014"));

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
							debugger;
							aOptions.OnSuccess({
								JSON: lJSON,
								text: lResponseText
							});
						}
					}
				} else {
					if (aOptions.OnFailure) {
						aOptions.OnFailure({
							code: (lXHR.status !== 0 ? lXHR.status : -1),
							msg: (lXHR.statusText !== "" ? lXHR.statusText : "failure")
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
	/*
		var lCookie = document.cookie;
		jws.console.debug(lCookie);
		if (aOptions.OnSuccess) {
			aOptions.OnSuccess({
				cookie: lCookie
			});
		}
		return lCookie;
		*/
	},
	
	setSSOCallbacks: function(aListeners) {
		if (!aListeners) {
			aListeners = {};
		}
//		if( aListeners.OnSamplesServerTime !== undefined ) {
//			this.OnSamplesServerTime = aListeners.OnSamplesServerTime;
//		}
	}

};

// add the JWebSocket SSO PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.SSOPlugIn);
