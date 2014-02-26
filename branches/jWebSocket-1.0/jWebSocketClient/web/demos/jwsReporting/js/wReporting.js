//	---------------------------------------------------------------------------
//	jWebSocket Reporting Plug-in (Community Edition, CE)
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


/**
 * jWebSocket Reporting Widget
 * @author vbarzana
 */

$.widget("jws.reporting", {
	_init: function() {
// ------------- VARIABLES -------------
		this.eBtnGetReports = this.element.find("#get_reports_btn");
		this.eCbReportList = jws.$("report_list_cb");
		this.eBtnUpTemplate = this.element.find("#upload_template_btn");
		this.eFCTemplatePath = jws.$("jws_upload_template_f");
		this.eCbReportFormats = jws.$("jws_reporting_formats_cmb");
		this.eChbxUseConnection = jws.$("jws_reporting_connection_chk");
		this.eBtnCreateReport = this.element.find("#create_report_btn");
		this.eTxaReportFields = jws.$("jws_reporting_fields_txa");
		this.eTxaReportParams = jws.$("jws_reporting_params_txa");
		this.eGenContentBox = jws.$("gen_content_box");
		$("#gen_content_box").hide();
		$("#help_footer").hide();
		this.eFormIsShow = false;
		this.eHelpIsShow = false;

//		this.eCbReportType = this.element.find("#report_type_cb");
//		this.ePdfObject = this.element.find("#pdf");
		// DEFAULT MESSAGES
		this.MSG_NOTCONNECTED = "Sorry, you are not connected to the " +
				"server, try updating your browser or clicking the login button";
		// Keeping a reference of the widget, when a websocket message
		// comes from the server the scope "this" doesnt exist anymore
		w.reporting = this;
		w.reporting.registerEvents( );
	},
	/**
	 * Registers all callbacks, and assigns all buttons and dom elements actions
	 * also starts up some specific things needed at the begining
	 **/
	registerEvents: function() {
		// Registers all callbacks for jWebSocket basic connection
		// For more information, check the file ../../res/js/widget/wAuth.js
		var lCallbacks = {
			OnOpen: function(aEvent) {
				mWSC.setReportingCallbacks({
					OnReports: w.reporting.handleReports,
					OnReport: w.reporting.handleReport,
					OnUploadTemplate: w.reporting.handleUpload
				});
			},
			OnWelcome: function(aEvent) {
				
			},
			OnClose: function() {
			},
			OnGoodBye: function(aToken) {
			},
			OnMessage: function(aEvent, aToken) {
				if (mLog.isDebugEnabled) {
					log("<font style='color:#888'>jWebSocket '" + aToken.type
							+ "' token received, full message: '" + aEvent.data + "' "
							+ "</font>");
				}
				w.reporting.onMessage(aEvent, aToken);
			},
			OnLogon: function() {
				// Filling the formats combobox
				w.reporting.eCbReportFormats.appendChild(new Option("PDF", "pdf"));
				w.reporting.eCbReportFormats.appendChild(new Option("HTML", "html"));
			},
			OnLogoff: function() {
				while (w.reporting.eCbReportFormats.options.length > 0) {
					w.reporting.eCbReportFormats.remove('option');
				}
			}
		};
		// Registering click events of DOM elements
		w.reporting.eBtnGetReports.click(w.reporting.getReports);
		w.reporting.eBtnUpTemplate.click(w.reporting.uploadTemplate);
		w.reporting.eBtnCreateReport.click(w.reporting.createReport);

		$("#show_icon").click(function() {
			if (w.reporting.eFormIsShow) {
				$("#gen_content_box").hide("slow");
				w.reporting.eFormIsShow = false;
			}
			else {
				$("#gen_content_box").show("slow");
				w.reporting.eFormIsShow = true;
			}
		});
		
		$("#show_icon_help").click(function() {
			if (w.reporting.eHelpIsShow ) {
				$("#help_footer").hide("slow");
				w.reporting.eHelpIsShow = false;
			}
			else {
				$("#help_footer").show("slow");
				w.reporting.eHelpIsShow  = true;
			}
		});
		
		w.reporting.eTxaReportFields.value = "[{name: 'Alexander', lastName: 'Schulze', age: 40, email: 'a.schulze@jwebsocket.org'},\n\
			{name: 'Rolando', lastName: 'Santamaria Maso', age: 27, email: 'r.santamaria@jwebsocket.org'},	\n\
			{name: 'Lisdey', lastName: 'Perez', age: 27, email: 'l.perez@jwebsocket.org'},	\n\
			{name: 'Marcos', lastName: 'Gonzalez', age: 27, email: 'm.gonzalez@jwebsocket.org'},\n\
			{name: 'Osvaldo', lastName: 'Aguilar', age: 27, email: 'o.aguilar@jwebsocket.org'},\n\
			{name: 'Victor', lastName: 'Barzana', age: 27, email: 'v.barzana@jwebsocket.org'},\n\
			{name: 'Javier Alejandro', lastName: 'Puentes Serrano', age: 27, email: 'j.puentes@jwebsocket.org'}]";
		w.reporting.eTxaReportParams.value = "{reportTitle: 'JWebSocket Contact Report'}";
		
		$("#demo_box").auth(lCallbacks);
	},
	/**
	 * Executed every time the server sends a message to the client
	 * @param aEvent
	 * @param aToken
	 **/
	onMessage: function(aEvent, aToken) {
//		if (aToken) {
//			// is it a response from a previous request of this client?
//			if (aToken.type == "response") {
//				// If the login data is ok
//				if (aToken.reqType == "login" && aToken.code == 0) {
////					var lSuccess = new PDFObject({
////						url: "jwsReportSample.pdf"
////					}).
////							embed("pdf");
//				}
//				// If anything went wrong in the server show information error
//				if (aToken.code == -1) {
//					jwsDialog(aToken.msg, "jWebSocket error", true, null, null, "error");
//				}
//			}
//		}
	},
	getReports: function() {
		if (mWSC.isConnected()) {
			log("Retreiving list of reports via jWebSocket...");
			mWSC.reportingGetReports({
				OnResponse: function(aToken) {
					log("Reports " + aToken.data);
				}
			});
		} else {
			jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket info",
					true, "alert");
		}
	},
	uploadTemplate: function() {
		if (mWSC.isConnected()) {
			var lFile = w.reporting.eFCTemplatePath.files[0];
			if (!lFile) {
				jwsDialog("Select a report template first!", "jWebSocket info",
						true, "alert");
				return;
			}
			if (!confirm('Are you sure to upload the selected template ? If\n\
						the template already exists it will get replaced!')) {
				return;
			}
			log("Uploading template report to the user home directory...");
			var lFR = new FileReader();
			lFR.onload = function(aEvt) {
				var lFileContent = aEvt.currentTarget.result;
				mWSC.fileSave(lFile.name, lFileContent, {
					encode: false,
					OnSuccess: function() {
						mWSC.reportingUploadTemplate(
								lFile.name
								);
					},
					OnFailure: function() {
						log("An error ocurred trying to upload the required template");
					}
				});
			};
			lFR.readAsDataURL(lFile);
		} else {
			jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket error",
					true, "alert");
		}
	},
	createReport: function() {		
		if (!w.reporting.eFormIsShow) {
			if ((mWSC.isConnected())) {
				var lReportName = "person";
				var lParams = {reportTitle: 'JWebSocket Contact Report'};
				var lFields = [
					{name: 'Alexander', lastName: 'Schulze', age: 40, email: 'a.schulze@jwebsocket.org'},
					{name: 'Rolando', lastName: 'Santamaria Maso', age: 27, email: 'r.santamaria@jwebsocket.org'},
					{name: 'Lisdey', lastName: 'Perez', age: 27, email: 'l.perez@jwebsocket.org'},
					{name: 'Marcos', lastName: 'Gonzalez', age: 27, email: 'm.gonzalez@jwebsocket.org,'},
					{name: 'Osvaldo', lastName: 'Aguilar', age: 27, email: 'o.aguilar@jwebsocket.org,'},
					{name: 'Victor', lastName: 'Barzana', age: 27, email: 'v.barzana@jwebsocket.org,'},
					{name: 'Javier Alejandro', lastName: 'Puentes Serrano', age: 27, email: 'j.puentes@jwebsocket.org'}];
				log("Creating Report...");
				mWSC.reportingGenerateReport(
						lReportName,
						lParams,
						lFields,
						{
							useConection: false,
							outputType: "pdf"
						}
				);
			}
			else {
				jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket error",
						true, "alert");
			}
		}
		else
		{
			log("Creating Report...");
			if (mWSC.isConnected()) {
				var lReportName = w.reporting.eCbReportList.value;
				var lFormat = w.reporting.eCbReportFormats.value;

				if (!w.reporting.eTxaReportFields.value) {
					jwsDialog("Enter the report fields [{id:'value'},{id:'value'},{id:'value'}]", "jWebSocket info",
							true, "alert");
				}
				else if (!w.reporting.eTxaReportParams.value) {
					jwsDialog("Enter the report params {id:'value'}", "jWebSocket info",
							true, "alert");
				}
				else if (!w.reporting.eCbReportList.value) {
					jwsDialog("Please select an available report from the list", "jWebSocket info",
							true, "alert");
				}
				eval("var lParams =" + w.reporting.eTxaReportParams.value);
				eval("var lFields =" + w.reporting.eTxaReportFields.value);

				mWSC.reportingGenerateReport(
						lReportName,
						lParams,
						lFields,
						{
							useConection: w.reporting.eChbxUseConnection.checked,
							outputType: lFormat
						}
				);
			} else {
				jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket error",
						true, "alert");
			}
		}


	},
	handleReport: function(aToken) {
		mWSC.fileLoad(aToken.path, jws.FileSystemPlugIn.ALIAS_PRIVATE, {
			OnSuccess: function(aToken) {
				if (w.reporting.eCbReportFormats.value == "pdf")
					window.open("data:application/pdf;base64," + aToken.data, "_blank");
				else
					window.open("data:application/zip;base64," + aToken.data, "_blank");
			}
		});
	},
	handleReports: function(aToken) {
		while (w.reporting.eCbReportList.options.length > 0) {
			w.reporting.eCbReportList.removeChild();
		}
		var lReports = aToken.data;
		for (i = 0; i < lReports.length; i++) {
			w.reporting.eCbReportList.appendChild(new Option(lReports[i], lReports[i]));
		}
	},
	handleUpload: function(aToken) {

	}
});