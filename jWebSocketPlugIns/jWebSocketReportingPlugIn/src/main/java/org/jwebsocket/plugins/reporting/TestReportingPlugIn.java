//	---------------------------------------------------------------------------
//	jWebSocket - TestReporting Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
package org.jwebsocket.plugins.reporting;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.util.Assert;

/**
 *
 * @author javier alejandro
 */
public class TestReportingPlugIn extends ActionPlugIn {

	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ReportingPlugin";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ReportingPlugin - Community Edition";
	private final static String NS = JWebSocketServerConstants.NS_BASE + ".plugins.testreporting";
	private TokenPlugIn mReportingPlugIn;
	private static final Logger mLog = Logging.getLogger();

	public TestReportingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(NS);
	}

	@Override
	public void systemStarted() throws Exception {
		mReportingPlugIn = (TokenPlugIn) getServer().getPlugInById("jws.reporting");
		Assert.notNull(mReportingPlugIn, "The TestReportingPlugIn required ReportingPlugIn  enabled!");
	}

	public void testGetReportAction(WebSocketConnector aConnector, Token aToken) {
		Token lRequest = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE
				+ ".plugins.reporting", "getReports");
		Token lResult = mReportingPlugIn.invoke(aConnector, lRequest);
		Token lResponse = createResponse(aToken);
		lResponse.setList("data", lResult.getList("data"));

		sendToken(aConnector, lResponse);
	}

	public void testGenerateReportAction(WebSocketConnector aConnector, Token aToken) {
		Token lRequest = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE
				+ ".plugins.reporting", "generateReport");

		// The report name
		String lreportName = "JWebAnotherUserData";

		// The report paramas
		Map<String, Object> lreportParams = new HashMap<String, Object>();
		lreportParams.put("reportTitle", "Testing ReportingPlugIn");

		// getting the reports fields
		List<Map<String, Object>> lReportFields = new ArrayList<Map<String, Object>>();

		// for jWebSocket Another User Data report
		Map<String, Object> lUser = new HashMap<String, Object>();
		lUser.put("username", "japuentes");
		lUser.put("firstname", "Javier");
		lUser.put("lastname", "Puentes");
		lUser.put("telephone", "234234234");
		lUser.put("mobile", "234234234");
		lUser.put("fax", "234234234");
		lUser.put("securityQuestion", "Mention your favotite football team");
		lUser.put("myInterest", "Download bibliography");
		lUser.put("subsciptions", "jWebSocket news");
		lUser.put("enabled", true);
		lUser.put("active", true);
		lUser.put("registrationDate", new Long("1384837986066"));
		lUser.put("activationDate", new Long("1384837986066"));
		lUser.put("lastActivityDate", new Long("1384837986066"));
		lUser.put("loginCount", 10);
		lReportFields.add(lUser);

		lUser = new HashMap<String, Object>();
		lUser.put("username", "mcasanova");
		lUser.put("firstname", "Madisleidys");
		lUser.put("lastname", "Casanova");
		lUser.put("telephone", "234234234");
		lUser.put("mobile", "234234234");
		lUser.put("fax", "234234234");
		lUser.put("securityQuestion", "Mention your favotite ice cream flavor");
		lUser.put("myInterest", "Download bibliography");
		lUser.put("subsciptions", "jWebSocket news");
		lUser.put("enabled", true);
		lUser.put("active", true);
		lUser.put("registrationDate", new Long("1384837986066"));
		lUser.put("activationDate", new Long("1384837986066"));
		lUser.put("lastActivityDate", new Long("1384837986066"));
		lUser.put("loginCount", 55);
		lReportFields.add(lUser);

		lUser = new HashMap<String, Object>();
		lUser.put("username", "aschulze");
		lUser.put("firstname", "Alexander");
		lUser.put("lastname", "Schulze");
		lUser.put("telephone", "234234234");
		lUser.put("mobile", "234234234");
		lUser.put("fax", "234234234");
		lUser.put("securityQuestion", "Mention your favotite ice cream flavor");
		lUser.put("myInterest", "Download bibliography");
		lUser.put("subsciptions", "jWebSocket news");
		lUser.put("enabled", true);
		lUser.put("active", true);
		lUser.put("registrationDate", new Long("1384837986066"));
		lUser.put("activationDate", new Long("1384837986066"));
		lUser.put("lastActivityDate", new Long("1384837986066"));
		lUser.put("loginCount", 55);
		lReportFields.add(lUser);

		lUser = new HashMap<String, Object>();
		lUser.put("username", "mcasanova");
		lUser.put("firstname", "Madisleidys");
		lUser.put("lastname", "Casanova");
		lUser.put("telephone", "234234234");
		lUser.put("mobile", "123123123");
		lUser.put("fax", "234234234");
		lUser.put("securityQuestion", "Mention your favotite ice cream flavor");
		lUser.put("myInterest", "Download bibliography");
		lUser.put("subsciptions", "jWebSocket news");
		lUser.put("enabled", true);
		lUser.put("active", true);
		lUser.put("registrationDate", new Long("1384837986066"));
		lUser.put("activationDate", new Long("1384837986066"));
		lUser.put("lastActivityDate", new Long("1384837986066"));
		lUser.put("loginCount", 55);
		lReportFields.add(lUser);

		lUser = new HashMap<String, Object>();
		lUser.put("username", "aschulze");
		lUser.put("firstname", "Alexander");
		lUser.put("lastname", "Schulze");
		lUser.put("telephone", "32234234");
		lUser.put("mobile", "234234234");
		lUser.put("fax", "234234234");
		lUser.put("securityQuestion", "Mention your favotite ice cream flavor");
		lUser.put("myInterest", "Download bibliography");
		lUser.put("subsciptions", "jWebSocket news");
		lUser.put("enabled", true);
		lUser.put("active", true);
		lUser.put("registrationDate", new Long("1384837986066"));
		lUser.put("activationDate", new Long("1384837986066"));
		lUser.put("lastActivityDate", new Long("1384837986066"));
		lUser.put("loginCount", 55);
		lReportFields.add(lUser);

		// checking JDBCplug-in is loaded
		boolean lUseJDBC = false;
		// provide a Connection instance here and set 'lUseJDBC = true'
		Connection lConnection = null;

		// getting the report format to export
		String lReportFormat = "pdf";

		lRequest.setString("reportName", lreportName);
		lRequest.setMap("reportParams", lreportParams);
		lRequest.setList("reportFields", lReportFields);
		lRequest.setBoolean("useJDBCConnection", lUseJDBC);
		lRequest.setString("reportOutputType", lReportFormat);
		lRequest.getMap().put("connection", lConnection);

		Token lResult = mReportingPlugIn.invoke(aConnector, lRequest);

		sendToken(aConnector, lResult);
	}
}
