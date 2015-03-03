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
 * @author Javier Alejandro Puentes
 */

$.widget("jws.reporting", {
    _init: function() {
        // ------------- VARIABLES -------------
        this.eBtnGetReports = this.element.find("#get_reports_btn");
        this.eCbReportList = jws.$("report_list_cb");
        this.eBtnUpTemplate = this.element.find("#upload_template_btn");
        this.eFCTemplatePath = jws.$("jws_upload_template_f");
        //		this.eCbReportFormats = jws.$("jws_reporting_formats_cmb");
        this.eBtnCreateReport = this.element.find("#create_report_btn");
        this.eTxaReportFields = jws.$("jws_reporting_fields_txa");
        this.eTxaReportParams = jws.$("jws_reporting_params_txa");
        this.eGenContentBox = jws.$("gen_content_box");
        this.eTxaReportFields.disabled = false;
        this.eTxaReportParams.disabled = false;
        this.eReportFormat = "pdf";
        this.eUseConnection = false;

        $("#pdf_area").hide();

        // DEFAULT MESSAGES
        this.MSG_NOTCONNECTED = "Sorry, you are not connected to the " +
                "server, try updating your browser or clicking the login button";
        this.MSG_NOTAUTHENTICATED = "Sorry, you are not authenticated on the " +
                "server, try clicking the login button";
        // Keeping a reference of the widget, when a websocket message
        // comes from the server the scope "this" doesnt exist anymore
        w.reporting = this;
        w.reporting.registerEvents();
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
            },
            OnLogoff: function() {
                $("#pdf_area").hide();
                w.reporting.eTxaReportFields.value = "";
                w.reporting.eTxaReportParams.value = "";
                while (w.reporting.eCbReportList.options.length > 0) {
                    w.reporting.eCbReportList.options.remove(w.reporting.eCbReportList.options.length - 1);
                }
            }
        };
        // Registering click events of DOM elements
        w.reporting.eBtnGetReports.click(w.reporting.getReports);
        w.reporting.eBtnUpTemplate.click(w.reporting.uploadTemplate);
        w.reporting.eBtnCreateReport.click(w.reporting.createReport);
        // DOM component events

        $("#report_list_cb").change(function() {
            if (w.reporting.eCbReportList.value == "JDBCExampleReport") {
                w.reporting.eTxaReportFields.disabled = true;
                w.reporting.eTxaReportParams.disabled = true;
                w.reporting.eTxaReportFields.value = "";
                w.reporting.eTxaReportParams.value = "";
                w.reporting.eUseConnection = true;
            }
            else {
                w.reporting.eTxaReportFields.disabled = false;
                w.reporting.eTxaReportParams.disabled = false;
                w.reporting.eUseConnection = false;
                setDefaults();
            }
        });
        function setDefaults() {
            w.reporting.eTxaReportFields.value = "[{name: 'Alexander', lastName: 'Schulze', age: 40, email: 'a.schulze@jwebsocket.org'},\n\
			{name: 'Rolando', lastName: 'Santamaria Maso', age: 27, email: 'rsantamaria@jwebsocket.org'},	\n\
			{name: 'Lisdey', lastName: 'Perez', age: 27, email: 'lperez@jwebsocket.org'},	\n\
			{name: 'Marcos', lastName: 'Gonzalez', age: 27, email: 'mgonzalez@jwebsocket.org'},\n\
			{name: 'Osvaldo', lastName: 'Aguilar', age: 27, email: 'oaguilar@jwebsocket.org'},\n\
			{name: 'Victor', lastName: 'Barzana', age: 27, email: 'vbarzana@jwebsocket.org'},\n\
			{name: 'Javier Alejandro', lastName: 'Puentes Serrano', age: 27, email: 'jpuentes@jwebsocket.org'}]";
            w.reporting.eTxaReportParams.value = "{reportTitle: 'JWebSocket Contact Report'}";
        }

        $("#demo_box").auth(lCallbacks);
    },
    /**
     * Executed every time the server sends a message to the client
     * @param aEvent
     * @param aToken
     **/
    onMessage: function(aEvent, aToken) {
    },
    getReports: function() {
        if (mWSC.isConnected()) {
            if ("anonymous" != mWSC.getUsername() || null == mWSC.getUsername()) {
                log("Retreiving list of reports via jWebSocket...");
                mWSC.reportingGetReports({
                    OnSuccess: function(aToken) {
                        log("Reports " + aToken.data);

                        while (w.reporting.eCbReportList.options.length > 0) {
                            w.reporting.eCbReportList.removeChild();
                        }

                        w.reporting.eCbReportList.appendChild(new Option("jWebSocketContactReport", "jWebSocketContactReport"));
                        w.reporting.eCbReportList.appendChild(new Option("JDBCExampleReport", "JDBCExampleReport"));
                        w.reporting.eTxaReportFields.value = "[{name: 'Alexander', lastName: 'Schulze', age: 40, email: 'a.schulze@jwebsocket.org'},\n\
			{name: 'Rolando', lastName: 'Santamaria Maso', age: 27, email: 'rsantamaria@jwebsocket.org'},	\n\
			{name: 'Lisdey', lastName: 'Perez', age: 27, email: 'lperez@jwebsocket.org'},	\n\
			{name: 'Marcos', lastName: 'Gonzalez', age: 27, email: 'mgonzalez@jwebsocket.org'},\n\
			{name: 'Osvaldo', lastName: 'Aguilar', age: 27, email: 'oaguilar@jwebsocket.org'},\n\
			{name: 'Victor', lastName: 'Barzana', age: 27, email: 'vbarzana@jwebsocket.org'},\n\
			{name: 'Javier Alejandro', lastName: 'Puentes Serrano', age: 27, email: 'jpuentes@jwebsocket.org'}]";
                        w.reporting.eTxaReportParams.value = "{reportTitle: 'JWebSocket Contact Report'}";
                    }
                });
            }
            else {
                jwsDialog(w.reporting.MSG_NOTAUTHENTICATED, "jWebSocket info",
                        true, "alert");
            }
        } else {
            jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket info",
                    true, "alert");
        }
    },
    uploadTemplate: function() {
        if (mWSC.isConnected()) {
            if ("anonymous" != mWSC.getUsername() || null == mWSC.getUsername()) {
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
                jwsDialog(w.reporting.MSG_NOTAUTHENTICATED, "jWebSocket info",
                        true, "alert");
            }
        } else {
            jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket info",
                    true, "alert");
        }
    },
    createReport: function() {
        // if the Create Report form is empty, create and launch a default report
        if ((w.reporting.eCbReportList.value == "" && w.reporting.eTxaReportFields.value == "")
                && w.reporting.eTxaReportParams.value == "") {
            if ((mWSC.isConnected())) {
                if ("anonymous" != mWSC.getUsername() || null == mWSC.getUsername()) {

                    var lReportName = "jWebSocketContactReport";
                    var lParams = {
                        reportTitle: 'JWebSocket Contact Report',
                        name: 'name'
                    };
                    var lFields = [
                        {
                            name: 'Alexander',
                            lastName: 'Schulze',
                            age: 40,
                            email: 'a.schulze@jwebsocket.org'
                        },
                        {
                            name: 'Rolando',
                            lastName: 'Santamaria Maso',
                            age: 27,
                            email: 'r.santamaria@jwebsocket.org'
                        },
                        {
                            name: 'Lisdey',
                            lastName: 'Perez',
                            age: 27,
                            email: 'l.perez@jwebsocket.org'
                        },
                        {
                            name: 'Marcos',
                            lastName: 'Gonzalez',
                            age: 27,
                            email: 'm.gonzalez@jwebsocket.org,'
                        },
                        {
                            name: 'Osvaldo',
                            lastName: 'Aguilar',
                            age: 27,
                            email: 'o.aguilar@jwebsocket.org,'
                        },
                        {
                            name: 'Victor',
                            lastName: 'Barzana',
                            age: 27,
                            email: 'v.barzana@jwebsocket.org,'
                        },
                        {
                            name: 'Javier Alejandro',
                            lastName: 'Puentes Serrano',
                            age: 27,
                            email: 'j.puentes@jwebsocket.org'
                        }];
                    var lFormat = w.reporting.eReportFormat;
                    log("Creating Report...");
                    mWSC.reportingGenerateReport(
                            lReportName,
                            lParams,
                            lFields,
                            {
                                useJDBCConnection: false,
                                outputType: lFormat
                            }
                    );
                } else {
                    jwsDialog(w.reporting.MSG_NOTAUTHENTICATED, "jWebSocket info",
                            true, "alert");
                }
            } else {
                jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket info",
                        true, "alert");
            }
        }
        // if Create Report form have data use report's arguments
        else
        {
            if (mWSC.isConnected()) {
                if ("anonymous" != mWSC.getUsername() || null == mWSC.getUsername()) {
                    lReportName = w.reporting.eCbReportList.value;
                    lFormat = w.reporting.eReportFormat;
                    // check if the requested report no needs a jdbc connection
                    if (w.reporting.eCbReportList.value == "jWebSocketContactReport") {
                        if (!w.reporting.eTxaReportFields.value) {
                            jwsDialog("Enter the report fields [{id:'value'},{id:'value'},{id:'value'}]", "jWebSocket info",
                                    true, "alert");
                        }
                        if (!w.reporting.eTxaReportParams.value) {
                            jwsDialog("Enter the report params {id:'value'}", "jWebSocket info",
                                    true, "alert");
                        }
                        eval("var lParams =" + w.reporting.eTxaReportParams.value);
                        eval("var lFields =" + w.reporting.eTxaReportFields.value);
                    }
                    // generating the report with the necesary arguments 
                    log("Creating Report...");
                    mWSC.reportingGenerateReport(
                            lReportName,
                            lParams,
                            lFields,
                            {
                                useJDBCConnection: w.reporting.eUseConnection,
                                outputType: lFormat
                            }
                    );
                } else {
                    jwsDialog(w.reporting.MSG_NOTAUTHENTICATED, "jWebSocket info",
                            true, "alert");
                }
            } else {
                jwsDialog(w.reporting.MSG_NOTCONNECTED, "jWebSocket info",
                        true, "alert");
            }
        }
    },
    handleReport: function(aToken) {
        if (aToken.error) {
            jwsDialog(aToken.error, "jWebSocket error",
                    true, "alert");
        } else {
            mWSC.fileLoad(aToken.path, jws.FileSystemPlugIn.ALIAS_PRIVATE, {
                decode: true,
                OnSuccess: function(aToken) {
                    var lBlob = jws.tools.b64toBlob(aToken.data, "application/pdf");
                    var lURL = (window.URL || window.webkitURL).createObjectURL(lBlob);
                    if (!jws.isIExplorer()) {
                        $("#pdf_area").show("slow");
                        document.getElementById("pdf_area").src = lURL;
                    }
                    else {
                        jwsDialog("Your browser does not support JavaScript PDF embedding", "ERROR",
                                true, "error");
                    }
                }
            });
        }
    },
    handleReports: function(aToken) {

    },
    handleUpload: function(aToken) {
        if (aToken.code == 0) {
            jwsDialog("The template was succesfully uploaded", "jWebSocket info",
                    true, "alert");
        }
    }
});