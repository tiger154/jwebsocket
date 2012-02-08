//	****************************************************************************
//	jWebSocket Hello World (uses jWebSocket Client and Server)
//	(C) 2010 Alexander Schulze, jWebSocket.org, Innotrade GmbH, Herzogenrath
//	****************************************************************************
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
//	****************************************************************************

/*
 * @author mayra, vbarzana, aschulze
 */
$.widget("jws.SMSGateway",{
    
	_init: function(){
		w.SMSGateway = this;
		w.SMSGateway.ePhoneNumber   = w.SMSGateway.element.find("#phoneNumberInput");
		w.SMSGateway.eInputFrom     = w.SMSGateway.element.find("#fromInput");
		w.SMSGateway.eInputSMS      = w.SMSGateway.element.find("#smsInput");
		w.SMSGateway.eTextCaptcha   = w.SMSGateway.element.find("#captchaText");
		w.SMSGateway.eJCaptcha      = w.SMSGateway.element.find("#jcaptcha");
		w.SMSGateway.eBtnUpdate     = w.SMSGateway.element.find("#update");
		w.SMSGateway.eBtnSend       = w.SMSGateway.element.find("#send_button");
		w.SMSGateway.eRSMS          = w.SMSGateway.element.find("#rsms");
		w.SMSGateway.eBSMS          = w.SMSGateway.element.find("#bsms");
		w.SMSGateway.eBtnSubmit     = w.SMSGateway.element.find('#submit');
		w.SMSGateway.eImg           = w.SMSGateway.element.find('#img');
        
		mWSC.addPlugIn( w.SMSGateway );
		w.SMSGateway.registerEvents();
	},
    
	registerEvents: function(){
		var lToken = {
			ns: "org.jwebsocket.plugins.jcaptcha",
			type: "getcaptcha",
			args: {
				imagetype: "jpg"
			}
		};
		mWSC.sendToken(lToken);
                    
		w.SMSGateway.eBtnUpdate.click( function() {
			lToken = {
				ns:   "org.jwebsocket.plugins.jcaptcha",
				type: "getcaptcha",
				args: {
					imagetype: "jpg"
				}
			};
			mWSC.sendToken(lToken);
		});
       
		w.SMSGateway.eBtnSend.click(function(){
			var lSMSToken = {
				ns: "org.jwebsocket.plugins.sms",
				type: "sms",
				to: w.SMSGateway.ePhoneNumber.val(),
				from: w.SMSGateway.eInputFrom.val(),
				message: w.SMSGateway.eInputSMS.val(),
				state: $('input[name=messageRadio]:checked').val()
			};
			mWSC.sendToken(lSMSToken);
		});
        
		w.SMSGateway.eBtnSubmit.click( function() {
			var lToken = {
				ns:   "org.jwebsocket.plugins.jcaptcha",
				type: "validate",
				inputChars: w.SMSGateway.eTextCaptcha.val()
			};
            
			var lOptions = {
				
				args: {
					inputChars: w.SMSGateway.eTextCaptcha.val()
				},
				
				OnSuccess: function( aToken ) {
					alert( "success" );
					w.SMSGateway.eJCaptcha.fadeOut( 1000, function() {
						$(this).html("<h1>Correct</h1>").fadeIn( 500 )
					});
					//aqui es el problema
					w.SMSGateway.eRSMS.fadeOut( 1000, function() {
						$(this).html("<h1>Correct</h1>").fadeIn( 500 )
					});
					w.SMSGateway.eBSMS.fadeOut( 1000, function() {
						$(this).html("<h1>Wrong</h1>").fadeIn( 500 )
					});
				},
				
				OnFailure: function( aToken ) {
					alert( "failure" );
					//incorrect validation ask for a new captcha
					w.SMSGateway.eJCaptcha.fadeOut(300).fadeIn(100).fadeOut(100).fadeIn(50).fadeOut(100).fadeIn(50).fadeOut(100).fadeIn(50);
					var lGetCaptchaToken = {
						ns:   "org.jwebsocket.plugins.jcaptcha",
						type: "getcaptcha",
						args: {
							imagetype: "jpg"
						}
					};
					mWSC.sendToken(lGetCaptchaToken);
					w.SMSGateway.eTextCaptcha.val("").focus();
				}
                
			};
			
			mWSC.sendToken(lToken, lOptions);
		});
	},
    
	// process incoming token with captcha image to be display in UI
	processToken: function( aToken ){
		if( aToken.ns == "org.jwebsocket.plugins.jcaptcha" ){
			if( aToken.type == "getcaptcha" ){
				w.SMSGateway.eImg.attr(
					"src", "data:image/jpg;base64," + aToken.image );
			}
            
		}
	}
});