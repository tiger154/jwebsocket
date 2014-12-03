//	---------------------------------------------------------------------------
//	jWebSocket - ReportingPlugIn (Community Edition, CE)
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
package org.jwebsocket.client.plugins.reporting;

import java.util.List;
import java.util.Map;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;

/**
 *
 * @author Javier Alejandro Puentes Serrano
 */
public class ReportingPlugIn extends BaseClientTokenPlugIn {

	/**
	 *
	 */
	public static final String REPORT_FORMAT_PDF = "pdf";
	/**
	 *
	 */
	public static final String REPORT_FORMAT_HTML = "html";

	/**
	 *
	 * @param aClient
	 */
	public ReportingPlugIn(WebSocketTokenClient aClient) {
		super(aClient, JWebSocketClientConstants.NS_REPORTING);
	}

	/**
	 *
	 * @param aClient
	 * @param aNS
	 */
	public ReportingPlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
	}

	/**
	 * Gets a list of available reports
	 *
	 * @param aListener
	 * @throws java.lang.Exception
	 */
	public void getReports(WebSocketResponseTokenListener aListener) throws Exception {
		Token lRequest = TokenFactory.createToken(getNS(), "getReports");

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Upload a report template
	 *
	 * @param aTemplatePath
	 * @param aListener
	 * @throws java.lang.Exception
	 */
	public void uploadTemplate(String aTemplatePath, WebSocketResponseTokenListener aListener) throws Exception {
		Token lRequest = TokenFactory.createToken(getNS(), "uploadTemplate");
		lRequest.setString("templatePath", aTemplatePath);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Generate a report
	 *
	 * @param aReportName
	 * @param aParams
	 * @param aFields
	 * @param aFormat
	 * @param aUseJDBCConnection
	 * @param aListener
	 * @throws java.lang.Exception
	 */
	public void generateReport(String aReportName,
			Map<String, Object> aParams, List<Map<String, Object>> aFields,
			String aFormat, boolean aUseJDBCConnection, WebSocketResponseTokenListener aListener) throws Exception {
		Token lRequest = TokenFactory.createToken(getNS(), "generateReport");
		lRequest.setString("reportName", aReportName);
		lRequest.setList("reportFields", aFields);
		lRequest.setMap("reportParams", aParams);
		lRequest.setString("reportOutputType", aFormat);
		lRequest.setBoolean("useJDBCConnection", aUseJDBCConnection);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Generate a report
	 *
	 * @param aReportName
	 * @param aParams
	 * @param aFields
	 * @param aListener
	 * @throws java.lang.Exception
	 */
	public void generateReport(String aReportName,
			Map<String, Object> aParams, List<Map<String, Object>> aFields,
			WebSocketResponseTokenListener aListener) throws Exception {
		generateReport(aReportName, aParams, aFields, REPORT_FORMAT_PDF, false, aListener);
	}

	/**
	 * Generate a report
	 *
	 * @param aReportName
	 * @param aParams
	 * @param aUseJDBCConnection
	 * @param aListener
	 * @throws java.lang.Exception
	 */
	public void generateReport(String aReportName,
			Map<String, Object> aParams, boolean aUseJDBCConnection,
			WebSocketResponseTokenListener aListener) throws Exception {
		generateReport(aReportName, aParams, null, REPORT_FORMAT_PDF, aUseJDBCConnection, aListener);
	}
}
