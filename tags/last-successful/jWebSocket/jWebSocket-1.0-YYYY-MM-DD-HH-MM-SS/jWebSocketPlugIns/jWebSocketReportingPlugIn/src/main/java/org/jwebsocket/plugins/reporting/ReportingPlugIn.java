//	---------------------------------------------------------------------------
//	jWebSocket - Reporting Plug-in (Community Edition, CE)
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

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.annotations.RequirePlugIn;
import org.jwebsocket.plugins.annotations.RequirePlugIns;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.reporting.api.IJasperReportService;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 *
 * @author Alexander Schulze
 * @author Javier Alejandro Puentes
 */
public class ReportingPlugIn extends ActionPlugIn {

	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ReportingPlugin";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ReportingPlugin - Community Edition";
	private static final Logger mLog = Logging.getLogger();
	private static final String NS_REPORTING = JWebSocketServerConstants.NS_BASE + ".plugins.reporting";
	private IJasperReportService mJasperReportService;
	private static ApplicationContext mBeanFactory;

	/**
	 * Gets the spring configuration for the plug-in settings
	 *
	 * @param aConfiguration
	 */
	public ReportingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(NS_REPORTING);

		try {
			mBeanFactory = getConfigBeanFactory(NS_REPORTING);
			Assert.notNull(mBeanFactory, "No or invalid spring configuration for "
					+ "reporting plugin, some features may no be availables");
			mJasperReportService = (IJasperReportService) mBeanFactory.getBean("jrService");
			if (mLog.isInfoEnabled()) {
				mLog.info("Reporting plugin succesfully instantiated");
			}
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating ReportingPlugIn"));
		}
	}

	/**
	 * Gets the plug-in version
	 *
	 * @return
	 */
	@Override
	public String getVersion() {
		return VERSION;
	}

	/**
	 * Gets the plug-in label
	 *
	 * @return
	 */
	@Override
	public String getLabel() {
		return LABEL;
	}

	/**
	 * Gets the plug-in description
	 *
	 * @return
	 */
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	/**
	 * Gets the plug-in vendor
	 *
	 * @return
	 */
	@Override
	public String getVendor() {
		return VENDOR;
	}

	/**
	 * Gets the plug-in copyright
	 *
	 * @return
	 */
	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	/**
	 * Gets the plug-in license
	 *
	 * @return
	 */
	@Override
	public String getLicense() {
		return LICENSE;
	}

	/**
	 * Load the JDBCPlugIn and initialize the connection
	 *
	 * @throws Exception
	 */
	@Override
	public void systemStarted() throws Exception {
	}

	/**
	 * Define how can invoke the Reporting PlugIn
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);
		try {
			if (NS_REPORTING.equals(aToken.getNS())) {
				if ("getReports".equals(aToken.getType())) {
					lResponse.setList("data", mJasperReportService.getReportNames());
				} else if ("generateReport".equals(aToken.getType())) {
					lResponse = generateReport(aConnector, aToken);
				}
			}
		} catch (Exception lEx) {
			lResponse.setCode(-1);
			lResponse.setString("msg", lEx.getMessage());
		}

		return lResponse;
	}

	/**
	 * Generates a report
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 * @throws Exception
	 */
	@RequirePlugIns(ids = {"jws.filesystem", "jws.jdbc"})
	public Token generateReport(WebSocketConnector aConnector, Token aToken) throws Exception {
		// getting the report name
		String lReportName = aToken.getString("reportName");
		// getting the report paramas
		Map<String, Object> lReportParams = aToken.getMap("reportParams", new HashMap());
		// getting the reports fields
		List<Map<String, Object>> lReportFields = aToken.getList("reportFields", new ArrayList());
		// checking JDBCplug-in is loaded
		boolean lUseJDBC = aToken.getBoolean("useJDBCConnection", false);
//		String lConnectionAlias = aToken.getString("connectionAlias", "default");
		String lConnectionAlias = mJasperReportService.getConnectionAlias();
		// getting the connection object, only from S2S
		Connection lConnection = (Connection) aToken.getObject("connection");
		if (lUseJDBC) {
			if (null == lConnection) {
				lConnection = getConnection(lConnectionAlias);
			}
		}
		// getting the report format to export
		String lReportFormat = aToken.getString("reportOutputType");

		// crating a response token for client
		Token lResponse = createResponse(aToken);
		String lUserHome = getUserHome(aConnector);
		String lReportPath;
		try {
			lReportPath = mJasperReportService.generateReport(lUserHome, lReportName,
					lReportParams, lReportFields, lConnection, lReportFormat);

			// returning the path of the generated report
			lResponse.setString("path", lReportPath);

			return lResponse;

		} catch (Exception lEx) {
			lResponse.setCode(-1);
			lResponse.setString("error", lEx.getMessage());
		}

		return lResponse;

	}

	/**
	 * Call the generateReport function and return a response to client
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_REPORTING + ".generateReport")
	public void generateReportAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		Token lResponse = generateReport(aConnector, aToken);

		sendToken(aConnector, lResponse);
	}

	/**
	 * Gets all the reports names
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_REPORTING + ".getReports")
	public void getReportsAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		// restricting the client to send a 'connection' parametters
		aToken.remove("connection");
		Token lResponse = createResponse(aToken);
		lResponse.setList("data", mJasperReportService.getReportNames());
		sendToken(aConnector, lResponse);
	}

	/**
	 * Gets the user home directory
	 *
	 * @param aConnector
	 * @return
	 */
	String getUserHome(WebSocketConnector aConnector) {
		// creating invoke request for FSP
		Token lRequest = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE
				+ ".plugins.filesystem", "getAliasPath");
		lRequest.setString("alias", "privateDir");

		// getting the method execution result
		Token lResult = invokePlugIn("jws.filesystem", aConnector, lRequest);
		Assert.notNull(lResult, "Unable to communicate with the FileSystem plug-in "
				+ "to retrieve the client private directory!");

		// getting the user private directory
		String lUserHome = lResult.getString("aliasPath");

		return lUserHome;
	}

	/**
	 * Gets the JDBC connection
	 *
	 * @param aAlias
	 * @return
	 * @throws Exception
	 */
	public Connection getConnection(String aAlias) throws Exception {
		TokenPlugIn lJDBCPlugIn = (TokenPlugIn) getServer().getPlugInById("jws.jdbc");
		Assert.notNull(lJDBCPlugIn, "The ReportingPlugin required JDBC plug-in enabled!");

		Token lRequest = TokenFactory.createToken();
		lRequest.setString("alias", aAlias);

		Object lObject = Tools.invoke(lJDBCPlugIn, "getNativeDataSource", new Class[]{Token.class}, lRequest);
		DataSource lDataSource = (DataSource) lObject;

		return lDataSource.getConnection();
	}

	/**
	 * Upload a report template
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_REPORTING + ".uploadTemplate")
	@RequirePlugIn(id = "jws.filesystem")
	public void uploadTemplateAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lTemplatePath = aToken.getString("templatePath");
		Assert.notNull(lTemplatePath, "The 'templatePath' argument cannot be null!");
		Assert.isTrue(lTemplatePath.endsWith(".jrxml"), "Invalid template extension!");

		String lUserPath = getUserHome(aConnector);
		File lTemplate = new File(lUserPath + File.separator + lTemplatePath);
		Assert.isTrue(lTemplate.exists() && lTemplate.canWrite(), "The target file does not exists!");

		FileUtils.copyFileToDirectory(lTemplate,
				new File(mJasperReportService.getSettings().getReportFolder()), true);
		lTemplate.delete();
		// sending default acknowledge response
		sendToken(aConnector, createResponse(aToken));
	}
}
