//	---------------------------------------------------------------------------
//	jWebSocket Mail PlugIn (uses jWebSocket Client and Server)
//	(C) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH, Herzogenrath
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


//	---------------------------------------------------------------------------
//  jWebSocket Mail Client Plug-In
//	---------------------------------------------------------------------------

//:package:*:jws
//:class:*:jws.MailPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.MailPlugIn[/tt] class.
jws.MailPlugIn = {

	//:const:*:NS:String:org.jwebsocket.plugins.mail (jws.NS_BASE + ".plugins.mail")
	//:d:en:Namespace for the [tt]MailPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.mail",
	HTML_MAIL: true,
	TEXT_MAIL: false,

	processToken: function( aToken ) {
		// check if namespace matches
		if( aToken.ns == jws.MailPlugIn.NS ) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if( "sendMail" == aToken.reqType ) {
				if( this.OnMailSent ) {
					this.OnMailSent( aToken );
				}
			}
		}
	},

	sendMail: function( aId, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.MailPlugIn.NS,
				type: "sendMail",
				id: aId
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	createMail: function( aFrom, aTo, aCC, aBCC, aSubject, aBody, aIsHTML, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.MailPlugIn.NS,
				type: "createMail",
				from: aFrom,
				to: aTo,
				cc: aCC,
				bcc: aBCC,
				subject: aSubject,
				body: aBody,
				isHTML: aIsHTML
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	dropMail: function( aId, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lToken = {
				ns: jws.MailPlugIn.NS,
				type: "dropMail",
				id: aId
			};
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
	},

	addAttachment: function( aId, aFilename, aData, aOptions ) {
		var lRes = this.checkConnected();
		if( 0 == lRes.code ) {
			var lEncoding = "base64";
			var lSuppressEncoder = false;
			var lScope = jws.SCOPE_PRIVATE;
			var lVolumeSize = null;
			var lArchiveName = null;
			if( aOptions ) {
				if( aOptions.scope != undefined ) {
					lScope = aOptions.scope;
				}
				if( aOptions.encoding != undefined ) {
					lEncoding = aOptions.encoding;
				}
				if( aOptions.suppressEncoder != undefined ) {
					lSuppressEncoder = aOptions.suppressEncoder;
				}
				if( aOptions.volumeSize != undefined ) {
					lVolumeSize = aOptions.volumeSize;
				}
				if( aOptions.archiveName != undefined ) {
					lArchiveName = aOptions.archiveName;
				}
			}
			if( !lSuppressEncoder ) {
				if( lEncoding == "base64" ) {
					aData = Base64.encode( aData );
				}
			}
			var lToken = {
				ns: jws.MailPlugIn.NS,
				type: "addAttachment",
				encoding: lEncoding,
				id: aId,
				data: aData,
				filename: aFilename
			};
			if( lVolumeSize ) {
				lToken.volumeSize = lVolumeSize;
			}
			if( lArchiveName ) {
				lToken.archiveName = lArchiveName;
			}
			this.sendToken( lToken,	aOptions );
		}
		return lRes;
		
	},

	removeAttachment: function( aId, aOptions ) {
		
	},

	getMail: function( aId, aOptions ) {
		
	},

	moveMail: function( aId, aOptions ) {
		
	},

	setMailCallbacks: function( aListeners ) {
		if( !aListeners ) {
			aListeners = {};
		}
		if( aListeners.OnMailSent !== undefined ) {
			this.OnMailSent = aListeners.OnMailSent;
		}
	}

}

// add the JWebSocket Mail PlugIn into the TokenClient class
jws.oop.addPlugIn( jws.jWebSocketTokenClient, jws.MailPlugIn );
