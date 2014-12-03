//	---------------------------------------------------------------------------
//	jWebSocket - TestReporting Plug-in (Community Edition, CE)
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
 * @author Javier Alejandro Puentes Serrano
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

	/**
	 *
	 * @param aConfiguration
	 */
	public TestReportingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(NS);
	}

	@Override
	public void systemStarted() throws Exception {
		mReportingPlugIn = (TokenPlugIn) getServer().getPlugInById("jws.reporting");
		Assert.notNull(mReportingPlugIn, "The TestReportingPlugIn required ReportingPlugIn  enabled!");
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void testGetReportAction(WebSocketConnector aConnector, Token aToken) {
		Token lRequest = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE
				+ ".plugins.reporting", "getReports");
		Token lResult = mReportingPlugIn.invoke(aConnector, lRequest);
		Token lResponse = createResponse(aToken);
		lResponse.setList("data", lResult.getList("data"));

		sendToken(aConnector, lResponse);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void testGenerateReportAction(WebSocketConnector aConnector, Token aToken) {
		Token lRequest = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE
				+ ".plugins.reporting", "generateReport");

		// The report name
		String lreportName = "FullUserDataList";
//		String lreportName = "UserRolesRights";
//		String lreportName = "UserList";		

		// The report paramas
		Map<String, Object> lreportParams = new HashMap<String, Object>();

		// The reports fields
		List<Map<String, Object>> lReportFields = new ArrayList<Map<String, Object>>();

		// A map<String,Object> with the data for the generation of the report
		Map<String, Object> lUser = new HashMap<String, Object>();

		// Lists to use as rights and subscriptions
		List<String> lList1 = new ArrayList<String>();
		List<String> lList2 = new ArrayList<String>();
		List<String> lList3 = new ArrayList<String>();

		lList1.add("1111111111111111111111111111");
		lList1.add("2222222222222222222222222222");
		lList1.add("3333333333333333333333333333");

		lList2.add("4444444444444444444444444444");
		lList2.add("5555555555555555555555555555");
		lList2.add("6666666666666666666666666666");

		lList3.add("7777777777777777777777777777");
		lList3.add("8888888888888888888888888888");
		lList3.add("9999999999999999999999999999");
		lList3.add("1010101010101010101010101010");
		lList3.add("1111111111111111111111111111");

//		// For UserRolesRights report
//		// Establish user data as params of the report
//		lreportParams.put("user_name", "japuentes");
//		lreportParams.put("full_name", "Javier Alejandro Puentes Serrano");
//
//		// Aggregate rol & rights as fields of the report
//		lUser.put("name", "Admin");
//		lUser.put("roles", lList1);
//		lReportFields.add(lUser);
//
//		lUser = new HashMap<String, Object>();
//
//		lUser.put("name", "User");
//		lUser.put("roles", lList2);
//		lReportFields.add(lUser);
//
//		lUser = new HashMap<String, Object>();
//
//		lUser.put("name", "Guest");
//		lUser.put("roles", lList3);
//		lReportFields.add(lUser);
		// For FullUserDataList and UserList report
		lUser.put("id", 1);
		lUser.put("username", "japuentes");
		lUser.put("firstname", "Javier");
		lUser.put("lastname", "Puentes");
		lUser.put("telephone", "234234234");
		lUser.put("mobile", "234234234");
		lUser.put("email", "japuentes@mail.org");
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
		lUser.put("subscriptions", lList1);
		lReportFields.add(lUser);

		lUser = new HashMap<String, Object>();
		lUser.put("id", 2);
		lUser.put("username", "mcasanova");
		lUser.put("firstname", "Madisleidys");
		lUser.put("lastname", "Casanova");
		lUser.put("telephone", "234234234");
		lUser.put("mobile", "234234234");
		lUser.put("email", "mcasanova@mail.org");
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
		lUser.put("subscriptions", lList2);
		lReportFields.add(lUser);

		lUser = new HashMap<String, Object>();
		lUser.put("id", 3);
		lUser.put("username", "aschulze");
		lUser.put("firstname", "Alexander");
		lUser.put("lastname", "Schulze");
		lUser.put("telephone", "234234234");
		lUser.put("mobile", "234234234");
		lUser.put("email", "aschulze@mail.org");
		lUser.put("fax", "234234234");
		lUser.put("securityQuestion", "Mention your favotite ice cream flavor");
		lUser.put("myInterest", "Download bibliography");
		lUser.put("subsciptions", "jWebSocket news");
		lUser.put("enabled", true);
		lUser.put("active", true);
		lUser.put("registrationDate", new Long("1384837986066"));
		lUser.put("activationDate", new Long("1384837986066"));
		lUser.put("lastActivityDate", new Long("1384837986066"));
		lUser.put("loginCount", 65);
		lUser.put("subscriptions", lList3);
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
