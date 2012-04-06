//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Reporting Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.reporting;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author aschulze
 */
public class ReportingPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private static final String NS_REPORTING = JWebSocketServerConstants.NS_BASE + ".plugins.reporting";
	private static final String VAR_FILES_TO_DELETE = NS_REPORTING + ".filesToDelete";
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;
	
/*
	static {
		try {
			URL lURL;
			ClassLoader lCL = getClass().getClassLoader();
			lURL = new URL("file:///c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/commons-digester-2.1.jar");
			ClassPathUpdater.add(lURL, lCL);
			lURL = new URL("file:///c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/jasperreports-4.5.0.jar");
			ClassPathUpdater.add(lURL, lCL);
			lURL = new URL("file:///c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/jWebSocketReportingPlugIn-1.0.jar");
			ClassPathUpdater.add(lURL, lCL);
		
			// ClassPathUpdater.add("jWebSocketReportingPlugIn-1.0.jar");
			// ClassPathUpdater.add("c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs");
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "adding Jasper libs to class path"));
		}
	}
*/
	
	/**
	 *
	 * @param aConfiguration
	 */
	public ReportingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating reporting plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_REPORTING);
		
/*
		try {
			URL lURL;
			ClassLoader lCL = getClass().getClassLoader();
			lURL = new URL("file:///c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/commons-digester-2.1.jar");
			ClassPathUpdater.add(lURL, lCL);
			lURL = new URL("file:///c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/jasperreports-4.5.0.jar");
			ClassPathUpdater.add(lURL);
			lURL = new URL("file:///c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/jWebSocketReportingPlugIn-1.0.jar");
			ClassPathUpdater.add(lURL, lCL);
		
			// ClassPathUpdater.add("jWebSocketReportingPlugIn-1.0.jar");
			// ClassPathUpdater.add("c:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs");
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "adding Jasper libs to class path"));
		}
*/
		
		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for reporting plug-in, some features may not be available.");
			} else {
				mBeanFactory = getConfigBeanFactory();
				mSettings = (Settings) mBeanFactory.getBean("settings");
				if (mLog.isInfoEnabled()) {
					mLog.info("Reporting plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating reporting plug-in"));
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if ("createReport".equals(lType)) {
				createReport(aConnector, aToken);
			} else if ("getReports".equals(lType)) {
				getReports(aConnector, aToken);
			} else if ("getReportParams".equals(lType)) {
				getReportParams(aConnector, aToken);
			} else if ("uploadReport".equals(lType)) {
				uploadReport(aConnector, aToken);
			} else if ("downloadReport".equals(lType)) {
				downloadReport(aConnector, aToken);
			}
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// cleanup reports once connection is terminated
		List<String> lFiles = (List<String>) aConnector.getVar(VAR_FILES_TO_DELETE);
		if (lFiles != null) {
			for (String lFile : lFiles) {
				FileUtils.deleteQuietly(new File(lFile));
			}
		}
	}

	private String getJRXMLPath(String aReportId) {
		return mSettings.getReportFolder() + aReportId + ".jrxml";
	}

	private String getJasperPath(String aReportId) {
		return mSettings.getReportFolder() + aReportId + ".jasper";
	}

	private String generateReportName(WebSocketConnector aConnector, String aReportName) {
		Map<String, String> lVars = new FastMap<String, String>();
		lVars.put("reportname", aReportName);
		lVars.put("username", aConnector.getUsername());
		lVars.put("timestamp", new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date()));
		aReportName = Tools.expandVars(mSettings.getReportNamePattern(), lVars,
				Tools.EXPAND_CASE_INSENSITIVE);
		return aReportName;
	}

	private void createReport(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse;

		TokenPlugIn lJDBCPlugIn = (TokenPlugIn) lServer.getPlugInById("jws.jdbc");
		if (lJDBCPlugIn == null) {
			// send response to requester
			lResponse = lServer.createErrorToken(aToken, -1, "JDBC plug-in not loaded.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		String lReportId = aToken.getString("reportId");
		if (lReportId == null || lReportId.isEmpty()) {
			lResponse = lServer.createErrorToken(aToken, -1, "No report id passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		String lOutputType = aToken.getString("outputType");
		if (lOutputType == null) {
			lOutputType = "pdf";
		}
		if (!("pdf".equals(lOutputType)
				|| "html".equals(lOutputType))) {
			lResponse = lServer.createErrorToken(aToken, -1, "Invalid report export type passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}
		try {
			FileUtils.forceMkdir(new File(mSettings.getOutputFolder()));
		} catch (Exception ex) {
			lResponse = lServer.createErrorToken(aToken, -1, ex.getClass().getSimpleName() + ": " + ex.getMessage());
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		Map<String, Object> lParams = new FastMap<String, Object>();
		List<Map<String, Object>> lInParams = aToken.getList("params");
		if (lInParams != null) {
			for (Map lParam : lInParams) {
				String lFromType = (String) lParam.get("type");
				if (lFromType != null) {
					String lToType = null;
					if ("datetime".equals(lFromType)) {
						lToType = "timestamp";
					}
					Object lValue = Tools.castGenericToJava(lParam.get("value"), lFromType, lToType);
					if (lValue != null) {
						lParams.put((String) lParam.get("name"), lValue);
					}
				}
			}
		}
		lParams.put("IMAGE_PATH", mSettings.getReportFolder());

		/*
		 * ClassLoader lCL = ClassLoader.getSystemClassLoader(); try { URL lURL;
		 * lURL = new
		 * URL("file:///C:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/commons-digester-2.1.jar");
		 * ClassPathUpdater.add(lURL); lURL = new
		 * URL("file:///C:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs/jasperreports-4.5.0.jar");
		 * ClassPathUpdater.add(lURL); // lParams.put("REPORT_CLASS_LOADER",
		 * JWebSocketXmlConfigInitializer.getClassLoader());
		 * lParams.put("REPORT_CLASS_LOADER", lCL);
		 *
		 * ClassLoader lCL = getClass().getClassLoader();
		 * lParams.put("REPORT_CLASS_LOADER", lCL);
		 */

		// instantiate response token
		lResponse = lServer.createResponse(aToken);

		DataSource lDataSource;
		Connection lConnection = null;
		try {
			Object lObject = Tools.invoke(lJDBCPlugIn, "getNativeDataSource");
			lDataSource = (DataSource) lObject;
			// Caution! 
			// setLoginTimeout are setLoginTimeout is not supported by org.apache.commons.dbcp.BasicDataSource!
			// int lTMO = lDataSource.getLoginTimeout();
			// if (mLog.isDebugEnabled()) {
			//	mLog.debug("Database login timeout is " + lTMO + "s.");
			// }
			lConnection = lDataSource.getConnection();

			/*
			 * JasperReport lReport = (JasperReport)
			 * JRLoader.loadObjectFromFile(getJasperPath(lReportId));
			 * JasperDesign lDesign =
			 * JRXmlLoader.load(getJRXMLPath(getJasperPath)); JasperReport
			 * lReport = JasperCompileManager.compileReport(lDesign);
			 */

			String lJRXMLPath = getJRXMLPath(lReportId);
			// Class lClass = Class.forName("net.sf.jasperreports.engine.xml.JRReportSaxParserFactory");
			/*
			 * JRProperties.setProperty("net.sf.jasperreports.compiler.xml.parser.factory",
			 * "javax.xml.parsers.SAXParserFactory");
			 */
			JasperReport lReport = JasperCompileManager.compileReport(lJRXMLPath);
			JasperPrint lPrint = JasperFillManager.fillReport(lReport,
					lParams, lConnection);
			String lReportName = generateReportName(aConnector, lReportId);
			String lExportFilePath;
			if ("pdf".equals(lOutputType)) {
				lExportFilePath = mSettings.getOutputFolder() + lReportName + ".pdf";
				JasperExportManager.exportReportToPdfFile(lPrint, 
						lExportFilePath);
				lResponse.setString("url", mSettings.getOutputURL() + lReportName + ".pdf");
			} else {
				lExportFilePath = mSettings.getOutputFolder() + lReportName + ".html";
				JasperExportManager.exportReportToHtmlFile(lPrint,
						lExportFilePath);
				lResponse.setString("url", mSettings.getOutputURL() + lReportName + ".html");
			}
			List<String> lFiles = (List<String>) aConnector.getVar(VAR_FILES_TO_DELETE);
			if (lFiles == null) {
				lFiles = new FastList<String>();
				aConnector.setVar(VAR_FILES_TO_DELETE, lFiles);
			}
			lFiles.add(lExportFilePath);
		} catch (Exception lEx) {
			lResponse.setInteger("code", -1);
			String lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			lResponse.setString("msg", lMsg);
			mLog.error(lMsg);
		}
		try {
			// ensure that connection is committed and closed
			if (lConnection != null && !lConnection.isClosed()) {
				lConnection.commit();
				lConnection.close();
			}
		} catch (Exception lEx) {
			mLog.debug("Instantiating reporting plug-in...");
		}
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getReports(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse;

		TokenPlugIn lFileSystemPlugIn = (TokenPlugIn) lServer.getPlugInById("jws.filesystem");
		if (lFileSystemPlugIn == null) {
			// send response to requester
			lResponse = lServer.createErrorToken(aToken, -1, "Filesystem plug-in not loaded.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		// instantiate response token
		lResponse = lServer.createResponse(aToken);

		Token lGetFilelist = TokenFactory.createToken(
				lFileSystemPlugIn.getNamespace(), "getFilelist");
		lGetFilelist.setString("alias", "reportRoot");
		lGetFilelist.setBoolean("recursive", false);
		List<String> lFilemasks = new FastList<String>();
		lFilemasks.add("*.jrxml");
		lGetFilelist.setList("filemasks", lFilemasks);

		Token lFilesToken = lFileSystemPlugIn.invoke(aConnector, lGetFilelist);
		List<Map> lFiles = lFilesToken.getList("files");
		for (Map lItem : lFiles) {
			String lFilename = (String) lItem.get("filename");
			int lIdx = lFilename.lastIndexOf('.');
			String lReportname = lFilename;
			if (lIdx > 0) {
				lReportname = lFilename.substring(0, lIdx);
			}
			lItem.put("reportname", lReportname);
		}
		// lServer.setResponseFields(aToken, lResponse);
		lResponse.setInteger("code", lFilesToken.getInteger("code"));
		lResponse.setString("msg", lFilesToken.getString("msg"));
		lResponse.setList("reports", lFiles);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getReportParams(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lReportId = aToken.getString("reportId");
		if (lReportId == null) {
			lResponse = lServer.createErrorToken(aToken, -1, "No report id passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		try {
			String lReportPath = getJRXMLPath(lReportId);
			JasperReport lReport = JasperCompileManager.compileReport(lReportPath);

			List lParamsList = new FastList<Map>();
			JRParameter[] lParams = lReport.getParameters();
			for (int lIdx = 0; lIdx < lParams.length; lIdx++) {
				JRParameter lParam = lParams[lIdx];
				Map lParamData = new FastMap<String, String>();
				lParamData.put("name", lParam.getName());
				lParamData.put("type", Tools.getGenericTypeStringFromJavaClassname(lParam.getValueClassName()));
				lParamData.put("className", lParam.getValueClassName());
				lParamData.put("description", lParam.getDescription());
				JRExpression lJRExpr = lParam.getDefaultValueExpression();
				if (lJRExpr != null) {
					lParamData.put("default", lJRExpr.getText());
				}
				lParamData.put("isForPrompting", lParam.isForPrompting());
				lParamData.put("properties", lParam.getPropertiesMap());
				lParamsList.add(lParamData);
			}
			lResponse.setList("params", lParamsList);
		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void uploadReport(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void downloadReport(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
