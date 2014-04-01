//	---------------------------------------------------------------------------
//	jWebSocket Reporting Client Plug-in (Community Edition, CE)
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

//:package:*:jws
//:class:*:jws.ReportingPlugIn
//:ancestor:*:-
//:d:en:Implementation of the [tt]jws.ReportingPlugIn[/tt] class.
jws.ReportingPlugIn = {
	//:const:*:NS:String:org.jwebsocket.plugins.reporting (jws.NS_BASE + ".plugins.reporting")
	//:d:en:Namespace for the [tt]ReportingPlugIn[/tt] class.
	// if namespace is changed update server plug-in accordingly!
	NS: jws.NS_BASE + ".plugins.reporting",
	processToken: function(aToken) {
		// check if namespace matches
		if (aToken.ns == jws.ReportingPlugIn.NS) {
			// here you can handle incomimng tokens from the server
			// directy in the plug-in if desired.
			if ("generateReport" == aToken.reqType) {
				if (this.OnReport) {
					this.OnReport(aToken);
				}
			} else if ("getReports" == aToken.reqType) {
				if (this.OnReports) {
					this.OnReports(aToken);
				}
			}
			else if ("uploadTemplate" == aToken.reqType) {
				if (this.OnUploadTemplate) {
					this.OnUploadTemplate(aToken);
				}
			}
		}
	},
	reportingGenerateReport: function(aReportName, aParams, aFields, aOptions) {
		var lRes = this.checkConnected();
		if (0 == lRes.code) {
			if (!aOptions) {
				aOptions = {};
			}

			var lToken = {
				ns: jws.ReportingPlugIn.NS,
				type: "generateReport",
				reportName: aReportName,
				reportFields: aFields,
				reportParams: aParams,
				reportOutputType: aOptions.outputType || "pdf",
				useJDBCConnection: aOptions.useJDBCConnection || false,
				nameJDBCCOnnection:aOptions.nameConnection || ""
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	reportingGetReports: function(aOptions) {
		var lRes = this.checkConnected();
		if (0 == lRes.code) {
			var lToken = {
				ns: jws.ReportingPlugIn.NS,
				type: "getReports"
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	reportingUploadTemplate: function(aTemplatePath, aOptions) {
		var lRes = this.checkConnected();
		if (0 == lRes.code) {
			var lToken = {
				ns: jws.ReportingPlugIn.NS,
				type: "uploadTemplate",
				templatePath: aTemplatePath
			};
			this.sendToken(lToken, aOptions);
		}
		return lRes;
	},
	setReportingCallbacks: function(aListeners) {
		if (!aListeners) {
			aListeners = {};
		}
		if (aListeners.OnReport !== undefined) {
			this.OnReport = aListeners.OnReport;
		}
		if (aListeners.OnReports !== undefined) {
			this.OnReports = aListeners.OnReports;
		}
		if (aListeners.OnUploadTemplate !== undefined) {
			this.OnUploadTemplate = aListeners.OnUploadTemplate;
		}
	}
};

// add the JWebSocket Reporting PlugIn into the TokenClient class
jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.ReportingPlugIn);