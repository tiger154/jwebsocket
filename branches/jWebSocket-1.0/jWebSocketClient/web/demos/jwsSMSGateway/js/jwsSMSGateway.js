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
		w.SMSGateway.eImg           = w.SMSGateway.element.find('#img');
		w.SMSGateway.eLoginArea     = w.SMSGateway.element.find('#login_area');
				
		// Each demo will configure its own callbacks to be passed to the login widget
		// Default callbacks { OnOpen | OnClose | OnMessage | OnWelcome | OnGoodBye}
		var lCallbacks = {
			OnOpen: function(aEvent){
				w.SMSGateway.getCaptcha();
				mWSC.addPlugIn( w.SMSGateway );
			}
		};
	
		$("#demo_box").auth(lCallbacks);
				
		w.SMSGateway.registerEvents();
	},
	
	getCaptcha: function(){
		var lToken = {
			ns: "org.jwebsocket.plugins.jcaptcha",
			type: "getcaptcha",
			args: {
				imagetype: "jpg"
			}
		};
		
		var lCallbacks = {
			OnSuccess: function(aToken) {
				log("<b style='color:green;'>Getting a new captcha</b>");
				w.SMSGateway.eImg.attr("src", "data:image/jpg;base64," + aToken.image );
			}
		};
		mWSC.sendToken(lToken, lCallbacks);
	},
    
	registerEvents: function(){
		w.SMSGateway.eBtnUpdate.click( function() {
			w.SMSGateway.getCaptcha();
		});
       
		w.SMSGateway.eBtnSend.click(function(){
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
					log("Success in the captcha validation...");
					var lSMSToken = {
						ns: "org.jwebsocket.plugins.sms",
						type: "sms",
						to: w.SMSGateway.ePhoneNumber.val(),
						from: w.SMSGateway.eInputFrom.val(),
						message: w.SMSGateway.eInputSMS.val(),
						state: $('input[name=messageRadio]:checked').val()
					};
					log("Sending SMS...");
					var lCallbacks = {
						OnSuccess: function(aToken){
							//function dialog(aTitle, aMessage, aIsModal, aCloseFunction)
							dialog("SMS sent correctly", "Congratulations!, you have sent a free SMS using jWebSocket Framework");
						},
						OnFailure: function(aToken){
							dialog("Error sending the SMS", "The following error has been encoutered: " + aToken.msg, true);
						}
					};
					mWSC.sendToken(lSMSToken, lCallbacks);
				},
				
				OnFailure: function( aToken ) {
					$("#jCaptchaDiv").effect("shake", {
						times:3
					}, 100);
					
					log("<b style='color:red;'>Wrong captcha validation, try another captcha</b>");
					var lGetNewCaptcha = function(){
						w.SMSGateway.getCaptcha();
						w.SMSGateway.eTextCaptcha.val("").focus();
					};
					//function dialog(aTitle, aMessage, aIsModal, aCloseFunction)
					dialog("Captcha error", "Error found in the Captcha, the server will send you other captcha, please try again!", true, lGetNewCaptcha);
				}
			};
			mWSC.sendToken(lToken, lOptions);
		});
       
	},
    
	// process all incoming tokens
	processToken: function( aToken ){
		
	}
});