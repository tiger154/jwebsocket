//	---------------------------------------------------------------------------
//	jWebSocket SSO client PlugIn (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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

jws.SSOPlugIn = {

	// namespace for SSO plugin
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.sso",
	BASE_URL: null,
	CLIENT_SECRET: null,
	COOKIE_NAME: "JWSSSO",

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns === jws.SSOPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
//			if( "requestServerTime" == aToken.reqType ) {
//				// this is just for demo purposes
//				// don't use blocking calls here which block the communication!
//				// like alert( "jWebSocket Server returned: " + aToken.time );
//				if( this.OnSamplesServerTime ) {
//					this.OnSamplesServerTime( aToken );
//				}
//			}
		}
	},

	mGetXHR: function( ) {
		var lXHR;
		
		if( window.XMLHttpRequest ) { // Mozilla, Safari, ...
			lXHR = new XMLHttpRequest();
			if( lXHR.overrideMimeType ) 
				lXHR.overrideMimeType( 'text/xml' );
		}
		else { // IE
			try {
				lXHR = new ActiveXObject( "Msxml2.XMLHTTP" );
			} catch ( lEx ) {
			}
			if ( typeof httpRequest === 'undefined' ) {
				try {
					lXHR = new ActiveXObject( "Microsoft.XMLHTTP" );
				} catch( lEx ) {
				}
			}
		}
		if( !lXHR ) {
			throw "Cannot create an XMLHTTP instance";
			return false;
		}
		return lXHR;
	},

	ssoSetHost: function( aURL ) {
		jws.SSOPlugIn.BASE_URL = aURL;
	},

	ssoSetSecret: function( aSecret ) {
		jws.SSOPlugIn.CLIENT_SECRET = aSecret;
	},

	ssoLoadOAuthData: function( aOptions ) {
		var lCookie = document.cookie;
		var lCookies = lCookie.split( "; " );
		if( lCookies ) {
			for( var lIdx in lCookies ) {
				var lKeyVal = lCookies[ lIdx ].split( "=" );
				if( lKeyVal && lKeyVal.length && lKeyVal.length >= 2 ) {
					var lKey = lKeyVal[ 0 ];
					var lValue = lKeyVal[ 1 ];
					if( jws.SSOPlugIn.COOKIE_NAME === lKey ) {
						var lJSON = JSON.parse( lValue );
						if( aOptions.OnResponse ) {
							if( !this.sso ) {
								this.sso = { };
							}
							this.sso.refreshToken = lJSON.refreshToken;
							this.sso.accessToken = lJSON.accessToken;
							var lToken = {};
							lToken.refreshToken = lJSON.refreshToken;
							lToken.accessToken = lJSON.accessToken;
							aOptions.OnResponse( lToken );
							break;
						}
					}	
				}	
			}
		}	
	},

	ssoSaveOAuthData: function( aOptions ) {
		var lCookie;
		try {
			lCookie = JSON.parse( document.cookie );
		} catch( lEx ) {
			lCookie = {};
		}
		if( this.sso && this.sso.refreshToken ) {
			lCookie.refreshToken = this.sso.refreshToken;
			lCookie.accessToken = this.sso.accessToken;
			var lExpires = new Date();
			debugger;
			lExpires.setTime( new Date().getTime() + 86400 * 1000 );
			var lExpValue = lExpires.toGMTString();
			document.cookie = jws.SSOPlugIn.COOKIE_NAME 
					+ "=" + JSON.stringify( lCookie ) 
					+ "; expires=" + lExpValue;
			if( aOptions.OnResponse ) {
				aOptions.OnResponse( lCookie );
			}
		}
	},

	ssoAuthDirect: function( aUsername, aPassword, aOptions ) {
		var lXHR = jws.SSOPlugIn.mGetXHR();
		
		lXHR.open( "POST", jws.SSOPlugIn.BASE_URL, true );
		lXHR.setRequestHeader( "Content-Type", "application/x-www-form-urlencoded;" );

		var lInstance = this;
		lXHR.onreadystatechange = function(){
			if( lXHR.readyState >= 4 
					&& lXHR.status === 200 ) {
				if( lXHR.responseText ) {
					if( aOptions ) {
						if( aOptions.OnResponse ) {
							var lJSON = JSON.parse( lXHR.responseText );
							if( !lInstance.sso ) {
								lInstance.sso = { };
							}
							lInstance.sso.refreshToken = lJSON.refresh_token;
							lInstance.sso.accessToken = lJSON.access_token;
							aOptions.OnResponse({
								JSON: lJSON,
								text: lXHR.responseText
							});
						}
					}
				}	      
			}
		};
		
		var lPostBody = 
				"client_id=ro_client"
				+ "&grant_type=password"
				+ "&username=" + encodeURIComponent( aUsername )
				+ "&password=" + encodeURIComponent( aPassword );
		
		lXHR.send( lPostBody );
	},

	ssoGetUser: function( aOptions ) {
		var lXHR = jws.SSOPlugIn.mGetXHR();
		
		lXHR.open( "POST", jws.SSOPlugIn.BASE_URL, true );
		lXHR.setRequestHeader( "Content-Type", "application/x-www-form-urlencoded;" );

		var lInstance = this;
		lXHR.onreadystatechange = function(){
			if( lXHR.readyState >= 4 
					&& lXHR.status === 200 ) {
				if( lXHR.responseText ) {
					if( aOptions ) {
						if( aOptions.OnResponse ) {
							var lJSON = JSON.parse( lXHR.responseText );
							if( !lInstance.sso ) {
								lInstance.sso = { };
							}
							lInstance.sso.username = lJSON.access_token.username;
							lInstance.sso.orgname = lJSON.access_token.OrgName;
							aOptions.OnResponse({
								username: lInstance.sso.username,
								JSON: lJSON,
								text: lXHR.responseText
							});
						}
					}
				}	      
			}
		};
		
		var lPostBody = 
				"client_id=rs_client"
				+ "&client_secret=" + jws.SSOPlugIn.CLIENT_SECRET
				+ "&grant_type=" + "urn:pingidentity.com:oauth2:grant_type:validate_bearer"
				+ "&token=" + this.sso.accessToken;
		
		lXHR.send( lPostBody );
	},

	ssoRefreshAccessToken: function( aOptions ) {
		var lXHR = jws.SSOPlugIn.mGetXHR();
		
		lXHR.open( "POST", jws.SSOPlugIn.BASE_URL, true );
		lXHR.setRequestHeader( "Content-Type", "application/x-www-form-urlencoded;" );

		var lInstance = this;
		lXHR.onreadystatechange = function(){
			if( lXHR.readyState >= 4 
					&& lXHR.status === 200 ) {
				if( lXHR.responseText ) {
					if( aOptions ) {
						if( aOptions.OnResponse ) {
							var lJSON = JSON.parse( lXHR.responseText );
							if( !lInstance.sso ) {
								lInstance.sso = { };
							}
							lInstance.sso.accessToken = lJSON.access_token;
							aOptions.OnResponse({
								JSON: lJSON,
								text: lXHR.responseText
							});
						}
					}
				}	      
			}
		};

		var lPostBody = 
				"client_id=ro_client"
				+ "&grant_type=" + "refresh_token"
				+ "&refresh_token=" + this.sso.refreshToken;
		
		lXHR.send( lPostBody );
	},

	setSamplesCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnSamplesServerTime !== undefined ) {
			this.OnSamplesServerTime = aListeners.OnSamplesServerTime;
		}
	}

};

// add the JWebSocket Samples PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.SSOPlugIn );
