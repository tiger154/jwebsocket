//	---------------------------------------------------------------------------
//	jWebSocket - ReportingPlugInListener (Community Edition, CE)
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

import org.jwebsocket.api.WebSocketClientTokenPlugInListener;
import org.jwebsocket.token.Token;

/**
 * The ReportingPlugIn listener
 *
 * @author Javier Alejandro Puentes Serrano
 */
public class ReportingPlugInListener implements WebSocketClientTokenPlugInListener {

	@Override
	public void processToken(Token aToken) {
		if ("getReports".equals(aToken.getString("reqType"))) {
			if (0 == aToken.getCode()) {
				OnReports(aToken);
			} else {
				OnReportsError(aToken);
			}
		} else if ("generateReport".equals(aToken.getString("reqType"))) {
			if (0 == aToken.getCode()) {
				OnGenerateReport(aToken);
			} else {
				OnGenerateReportError(aToken);
			}
		}
	}

	/**
	 * Is called when a list of reports has been obtained
	 *
	 * @param aToken
	 */
	private void OnReports(Token aToken) {
	}

	/**
	 * Is called when 'getReports' method return an empty list
	 *
	 * @param aToken
	 */
	private void OnReportsError(Token aToken) {
	}

	/**
	 * Is called when a report has been generated
	 *
	 * @param aToken
	 */
	private void OnGenerateReportError(Token aToken) {
	}

	/**
	 * Is called when a report has been generated
	 *
	 * @param aToken
	 */
	private void OnGenerateReport(Token aToken) {
	}
}
