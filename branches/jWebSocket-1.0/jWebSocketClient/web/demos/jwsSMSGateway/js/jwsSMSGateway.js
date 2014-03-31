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
		NS_SMS = jws.NS_BASE + ".plugins.sms";
		NS_JCAPTCHA = jws.NS_BASE + ".plugins.jcaptcha";
		NS_QUOTA = jws.NS_BASE + ".plugins.quota";
		this.ePhoneNumber = this.element.find("#phoneNumberInput");
		this.eInputFrom = this.element.find("#fromInput");
		this.eInputSMS = this.element.find("#smsInput");
		this.eTextCaptcha = this.element.find("#captchaText");
		this.eJCaptcha = this.element.find("#jcaptcha");
		this.eBtnUpdate = this.element.find("#update");
		this.eBtnSend = this.element.find("#send_button");
		this.eBtnReport = this.element.find("#report_button");
		this.eRSMS = this.element.find("#rsms");
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
		w.SMSGateway = this;
		// Update the counter of characters for if any change in the html
		w.SMSGateway.countCharacters();
		w.SMSGateway.doWebSocketConnection();
		w.SMSGateway.registerEvents();

	},
	doWebSocketConnection: function() {
		// Each demo will configure its own callbacks to be passed to the login widget
		// Default callbacks { OnOpen | OnClose | OnMessage | OnWelcome | OnGoodBye}
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnWelcome: function(aToken) {
				if (aToken.username !== "anonymous") {
					// Ask for a new captcha image
					w.SMSGateway.getCaptcha();
					w.SMSGateway.remainingSMS(w.auth.mUsername);
				} else {
					w.SMSGateway.disableAll();
				}
			},
			OnClose: function(aEvent) {
				w.SMSGateway.eImg.attr("src", "css/images/blank.png");
				$("#remining_sms").hide();
				w.SMSGateway.disableAll();
			},
			OnLogon: function() {
				w.SMSGateway.enableAll();
				w.SMSGateway.remainingSMS(lData.username);
			},
			OnLogoff: function() {
				$("#remining_sms").hide();
				w.SMSGateway.disableAll();
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
				w.SMSGateway.eImg.attr("src", "data:image/jpg;base64," + aToken.image);
				w.SMSGateway.eTextCaptcha.focus();
			},
			OnFailure: function(aToken) {
				jwsDialog(aToken.msg, "Error from the server", true, "information");
			}
		});
	},
	registerEvents: function() {
		w.SMSGateway.eBtnUpdate.click(function() {
			if (!$(this).attr("disabled")) {
				w.SMSGateway.getCaptcha();
			}
		});
		w.SMSGateway.eTextCaptcha.bind({
			'click | focus': function() {
				if ($(this).val() === w.SMSGateway.mTXT_CAPTCHA) {
					$(this).val("");
				}
			},
			blur: function() {
				if ($(this).val() === "") {
					$(this).val(w.SMSGateway.mTXT_CAPTCHA);
				}
			}
		});
		w.SMSGateway.eBtnReport.click(function() {
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

		w.SMSGateway.eBtnSend.click(function() {
			if (!$(this).attr("disabled")) {
				var lSMSToken = {
					ns: NS_SMS,
					type: "sendSMS",
					to: w.SMSGateway.ePhoneNumber.val(),
					from: w.SMSGateway.eInputFrom.val(),
					message: w.SMSGateway.eInputSMS.val(),
					state: $('input[name=messageRadio]:checked').val(),
					captcha: w.SMSGateway.eTextCaptcha.val()
				};
				log("Sending SMS...");
				var lCallbacks = {
					OnSuccess: function(aToken) {
						//function dialog(aTitle, aMessage, aIsModal, aCloseFunction)
						jwsDialog(w.SMSGateway.mMSG_SMS_SENT, "SMS sent correctly",
								true, "alert", function() {
									w.SMSGateway.remainingSMS(w.auth.mUsername);
								});
						w.SMSGateway.getCaptcha();
					},
					OnFailure: function(aToken) {
						jwsDialog(w.SMSGateway.mMSG_ERROR + aToken.msg,
								"Error sending the SMS", true, "alert", function() {
									$("#imgCaptcha").effect("shake", {
										times: 3
									}, 100);
									w.SMSGateway.getCaptcha();
									w.SMSGateway.eTextCaptcha.val("").focus();
								});
					}
				};
				mWSC.sendToken(lSMSToken, lCallbacks);
			}
		});
		// Handle keydown and keyup of the textarea to count the characters
		w.SMSGateway.eInputSMS.keydown(w.SMSGateway.updateCounter);
		w.SMSGateway.eInputSMS.keyup(w.SMSGateway.updateCounter);
	},
	countCharacters: function() {
		var lCount = w.SMSGateway.eInputSMS.val().length;
		w.SMSGateway.mCount = lCount;
		var lValue = w.SMSGateway.MAX_COUNT - lCount;
		// Update the counter
		w.SMSGateway.eCCounter.text(lValue > 0 ? lValue : 0);
	},
	// Updates the counter when a key is pressed
	updateCounter: function(aEvent) {
		w.SMSGateway.countCharacters();
		if (w.SMSGateway.mCount >= w.SMSGateway.MAX_COUNT) {
			w.SMSGateway.eInputSMS.val(w.SMSGateway.eInputSMS.val().substr(
					0, w.SMSGateway.MAX_COUNT));
			w.SMSGateway.eCCounterArea.attr("class", "error");
		} else {
			w.SMSGateway.eCCounterArea.attr("class", "");
		}
	},
	remainingSMS: function(aUsername) {

		if (aUsername === null) {
			aUsername = mWSC.getUsername();
		}

		//not request for a quota if the user login exist in mQuotaNoApply array
		if (-1 !== w.SMSGateway.mQuotaNoApply.indexOf(aUsername)) {
			$("#remining_sms").hide();
			return;
		}

		var me = this;

		var lQUOTAToken = {
			ns: NS_QUOTA,
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
				$("#remining_sms").show();
			},
			OnFailure: function(aToken) {
				$("#remining_sms").hide();
			}
		};
		mWSC.sendToken(lQUOTAToken, lCallbacks);
	},
	updateReminingSMS: function(aValue) {
		$("#remaining_quota").html(aValue);
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
		w.SMSGateway.disableButton(w.SMSGateway.eBtnSend);
		w.SMSGateway.disableButton(w.SMSGateway.eBtnReport);
		w.SMSGateway.eBtnUpdate.attr("disabled", true);
		w.SMSGateway.eBtnUpdate.addClass("disabled");
		w.SMSGateway.eImg.attr("src", "");
		w.SMSGateway.gritterDialog = $.gritter.add({
			// (string | mandatory) the heading of the notification
			title: 'Welcome to jWebSocket SMS Gateway Demo',
			// (string | mandatory) the text inside the notification
			text: 'You must <a href="https://enapso.com/products/userClient#login" target="_parent">login</a> ' +
					'or <a href="https://enapso.com/products/userClient#register" target="_parent">register</a> ' +
					'first using our Website header buttons to be able to use this demo.',
			class_name: 'gritter-light gritter-top_align',
			sticky: true,
			image: '../../res/img/information.png' // you can use warning.png, important.png, alert.png, error.png
		});
	},
	enableAll: function() {
		w.SMSGateway.enableButton(w.SMSGateway.eBtnSend);
		w.SMSGateway.enableButton(w.SMSGateway.eBtnReport);
		w.SMSGateway.eBtnUpdate.attr("disabled", false);
		w.SMSGateway.eBtnUpdate.removeClass("disabled");
		if (w.SMSGateway.gritterDialog) {
			$.gritter.remove(w.SMSGateway.gritterDialog);
		}
	}
});
