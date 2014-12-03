//	---------------------------------------------------------------------------
//	jWebSocket SMS Gateway (Community Edition, CE)
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

/*
 * @author mayra, vbarzana, aschulze
 */
$.widget("jws.SMSGateway", {
	_init: function() {
		this.NS = jws.NS_BASE + ".plugins.sms";
		this.NS_QUOTA = jws.NS_BASE + ".plugins.quota";
		this.ePhoneNumber = this.element.find("#phoneNumberInput");
		this.eInputFrom = this.element.find("#fromInput");
		this.eInputSMS = this.element.find("#smsInput");
		this.eTextCaptcha = this.element.find("#captchaText");
		this.eJCaptcha = this.element.find("#jcaptcha");
		this.eBtnUpdate = this.element.find("#update");
		this.eBtnSend = this.element.find("#send_button");
		this.eBtnReport = this.element.find("#report_button");
		this.eRSMS = this.element.find("#rsms");
		this.eRemainingSMS = this.element.find("#remaining_sms");
		this.eRemainingQuota = this.element.find("#remaining_quota");
		this.eBSMS = this.element.find("#bsms");
		this.eImg = this.element.find('#img');
		this.eLoginArea = this.element.find('#login_area');
		this.eCCounterArea = this.element.find('#character_counter');
		this.eCCounter = this.element.find('#character_counter .count');
		this.MAX_COUNT = 160;
		this.mCount = 0;
		this.mQuotaNoApply = ["anonymous"];
		this.mTXT_CAPTCHA = "Type the words here...";
		this.mMSG_CAPTCHA_ERROR = "Error found in the Captcha, the server will" +
				" send you other captcha, please try again!";
		this.mMSG_ERROR = "The following error has been encoutered: ";
		this.mMSG_SMS_SENT = "Congratulations!, you have sent a free SMS" +
				" using jWebSocket Framework";
		w.SMS = this;
		// Update the counter of characters for if any change in the html
		w.SMS.countCharacters();
		w.SMS.doWebSocketConnection();
		w.SMS.registerEvents();

	},
	doWebSocketConnection: function() {
		// Each demo will configure its own callbacks to be passed to the login widget
		// Default callbacks { OnOpen | OnClose | OnMessage | OnWelcome | OnGoodBye}
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnWelcome: function(aToken) {
				if (aToken.username !== "anonymous") {
					// Ask for a new captcha image
					w.SMS.remainingSMS(w.auth.mUsername);
				} else {
					w.SMS.disableAll();
				}
			},
			OnClose: function(aEvent) {
				w.SMS.eImg.attr("src", "css/images/blank.png");
				w.SMS.eRemainingSMS.hide();
				w.SMS.disableAll();
			},
			OnLogon: function(aToken) {
				w.SMS.remainingSMS(aToken.username);
				w.SMS.enableAll();
			},
			OnLogoff: function() {
				w.SMS.eRemainingSMS.hide();
				w.SMS.disableAll();
			},
			OnMessage: function(aEvent, aToken) {
				// Listening to logon event broadcasting from useradmin plug-in,
				// if we want to have this global for all the demos we just have 
				// to add these lines to the OnMessage from the widget 
				// wAuth.js under ../../res/js/widgets/wAuth.js
			}
		};
		// To automatically handle all the logon, logoff events for all demos
		$("#demo_box").auth(lCallbacks);
		// Opening the connection automatically
		w.auth.connect();
	},
	getCaptcha: function() {
		mWSC.captchaGenerate("jpg", {
			OnSuccess: function(aToken) {
				log("<b style='color:green;'>Getting a new captcha</b>");
				w.SMS.eImg.attr("src", "data:image/jpg;base64," + aToken.image);
				w.SMS.eTextCaptcha.focus();
				w.SMS.eTextCaptcha.val("");
			},
			OnFailure: function(aToken) {
				jwsDialog(aToken.msg, "Error from the server", true, "information");
			}
		});
	},
	registerEvents: function() {
		w.SMS.eBtnUpdate.click(function() {
			if (!$(this).attr("disabled")) {
				w.SMS.getCaptcha();
			}
		});
		w.SMS.eTextCaptcha.bind({
			'click | focus': function() {
				if ($(this).val() === w.SMS.mTXT_CAPTCHA) {
					$(this).val("");
				}
			},
			blur: function() {
				if ($(this).val() === "") {
					$(this).val(w.SMS.mTXT_CAPTCHA);
				}
			}
		});
		w.SMS.eBtnReport.click(function() {
			if (!$(this).attr("disabled")) {
				mWSC.smsGenerateReport();
			}
		});
		mWSC.setSMSCallbacks({
			OnReport: function(aToken) {
				mWSC.fileLoad(aToken.path, jws.FileSystemPlugIn.ALIAS_PRIVATE, {
					OnSuccess: function(aToken) {
						window.open("data:application/pdf;base64," + aToken.data, "_blank");
					}
				});
			}
		});

		w.SMS.eBtnSend.click(function() {
			if (!$(this).attr("disabled")) {
				var lPhoneNr = w.SMS.ePhoneNumber.val();
				if(lPhoneNr){
					lPhoneNr = lPhoneNr.trim().replace(" ", "");
				}
				var lSMSToken = {
					ns: w.SMS.NS,
					type: "sendSMS",
					to: lPhoneNr,
					from: w.SMS.eInputFrom.val(),
					message: w.SMS.eInputSMS.val(),
					state: $('input[name=messageRadio]:checked').val(),
					captcha: w.SMS.eTextCaptcha.val()
				};
				log("Sending SMS...");
				var lCallbacks = {
					OnSuccess: function(aToken) {
						//function dialog(aTitle, aMessage, aIsModal, aCloseFunction)
						jwsDialog(w.SMS.mMSG_SMS_SENT, "SMS sent correctly",
								true, "alert", function() {
									w.SMS.remainingSMS(w.auth.mUsername);
								});
						w.SMS.getCaptcha();
					},
					OnFailure: function(aToken) {
						jwsDialog(w.SMS.mMSG_ERROR + aToken.msg,
								"Error sending the SMS", true, "alert", function() {
									$("#imgCaptcha").effect("shake", {
										times: 3
									}, 100);
									w.SMS.getCaptcha();
									w.SMS.eTextCaptcha.val("").focus();
								});
					}
				};
				mWSC.sendToken(lSMSToken, lCallbacks);
			}
		});
		// Handle keydown and keyup of the textarea to count the characters
		w.SMS.eInputSMS.keydown(w.SMS.updateCounter);
		w.SMS.eInputSMS.keyup(w.SMS.updateCounter);
	},
	countCharacters: function() {
		var lCount = w.SMS.eInputSMS.val().length;
		w.SMS.mCount = lCount;
		var lValue = w.SMS.MAX_COUNT - lCount;
		// Update the counter
		w.SMS.eCCounter.text(lValue > 0 ? lValue : 0);
	},
	// Updates the counter when a key is pressed
	updateCounter: function(aEvent) {
		w.SMS.countCharacters();
		if (w.SMS.mCount >= w.SMS.MAX_COUNT) {
			w.SMS.eInputSMS.val(w.SMS.eInputSMS.val().substr(
					0, w.SMS.MAX_COUNT));
			w.SMS.eCCounterArea.attr("class", "error");
		} else {
			w.SMS.eCCounterArea.attr("class", "");
		}
	},
	remainingSMS: function(aUsername) {

		if (aUsername === null) {
			aUsername = mWSC.getUsername();
		}

		//not request for a quota if the user login exist in mQuotaNoApply array
		if (-1 !== w.SMS.mQuotaNoApply.indexOf(aUsername)) {
			w.SMS.eRemainingSMS.hide();
			return;
		}

		var me = this;

		var lQUOTAToken = {
			ns: w.SMS.NS_QUOTA,
			type: 'getQuota',
			identifier: 'CountDown',
			namespace: 'org.jwebsocket.plugins.sms',
			instance: aUsername,
			actions: 'sendSMS',
			instance_type: 'User'
		};

		var lCallbacks = {
			OnSuccess: function(aToken) {
				me.updateReminingSMS(aToken.value);
				w.SMS.eRemainingSMS.show();
			},
			OnFailure: function(aToken) {
				w.SMS.eRemainingSMS.hide();
			}
		};
		mWSC.sendToken(lQUOTAToken, lCallbacks);
	},
	updateReminingSMS: function(aValue) {
		w.SMS.eRemainingQuota.html(aValue);
	},
	disableButton: function(aButton) {
		aButton.attr("disabled", "disabled")
				.attr("class", "button onmousedown")
				.attr("onmouseover", "")
				.attr("onmousedown", "")
				.attr("onmouseup", "")
				.attr("onmouseout", "")
				.attr("onclick", "");
	},
	enableButton: function(aButton) {
		aButton.attr("disabled", false)
				.attr("class", "button onmouseup")
				.attr("onmouseover", "this.className='button onmouseover'")
				.attr("onmousedown", "this.className='button onmousedown'")
				.attr("onmouseup", "this.className='button onmouseup'")
				.attr("onmouseout", "this.className='button onmouseout'")
				.attr("onclick", "this.className='button onmouseover'");
	},
	disableAll: function() {
		var lUrl = window.location.origin + "/products/userClient";
		w.SMS.disableButton(w.SMS.eBtnSend);
		w.SMS.disableButton(w.SMS.eBtnReport);
		w.SMS.eBtnUpdate.attr("disabled", true);
		w.SMS.eBtnUpdate.addClass("disabled");
		w.SMS.eImg.attr("src", "");

		if (window.location.origin.indexOf("localhost") < 0) {
			w.SMS.gritterDialog = $.gritter.add({
				// (string | mandatory) the heading of the notification
				title: 'Welcome to jWebSocket SMS Gateway Demo',
				// (string | mandatory) the text inside the notification
				text: 'You must login or <a href="' + lUrl + '#register" target="_parent">register</a> ' +
						'first using our Website header buttons to be able to use this demo.',
				class_name: 'gritter-light gritter-top_align',
				sticky: true,
				image: '../../res/img/information.png' // you can use warning.png, important.png, alert.png, error.png
			});
		}
	},
	enableAll: function() {
		w.SMS.enableButton(w.SMS.eBtnSend);
		w.SMS.enableButton(w.SMS.eBtnReport);
		w.SMS.eBtnUpdate.attr("disabled", false);
		w.SMS.eBtnUpdate.removeClass("disabled");
		w.SMS.getCaptcha();
		if (w.SMS.gritterDialog) {
			$.gritter.remove(w.SMS.gritterDialog);
		}
	}
});
