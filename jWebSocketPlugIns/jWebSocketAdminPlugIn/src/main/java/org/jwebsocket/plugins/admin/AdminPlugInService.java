//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Administration Plug-In
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.plugins.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.AdminConfig;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.AdminConfigHandler;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.config.xml.JWebSocketConfigHandler;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.factory.JWebSocketJarClassLoader;
import org.jwebsocket.factory.JWebSocketLoader;
import org.jwebsocket.filter.TokenFilter;
import org.jwebsocket.filter.TokenFilterChain;
import org.jwebsocket.kit.ChangeType;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.TokenPlugInChain;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.MapToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.DateHandler;

/**
 *
 * @author Marcos Antonio González Huerta (markos0886, UCI)
 */
public class AdminPlugInService {

	private static Logger mLog = Logging.getLogger(AdminPlugInService.class);
	private static Integer mNumberOfDays = null;
	private static String mNamespace;
	private static String mPathLog = null;
	private static String mPathLibs = null;
	private static JWebSocketConfig mJWebSocketConfig = null;
	private static AdminConfig mAdminConfig = null;
	private static TokenServer mServer;
	private static String JWS_MGMT_DESK_PATH = "AdminPlugIn" + 
			System.getProperty("file.separator") + "jwsMgmtDesk.xml";

	/**
	 * Constructor
	 * 
	 * @param aNamespace
	 * @param aNumberOfDays
	 * @param aServer
	 * @param aLog
	 */
	public AdminPlugInService(String aNamespace, Integer aNumberOfDays, 
			TokenServer aServer, Logger aLog) {

		mNumberOfDays = aNumberOfDays;
		mServer = aServer;
		mNamespace = aNamespace;
		mPathLog = JWebSocketConfig.getLogsFolder("adminLog.log");
		mPathLibs = JWebSocketConfig.getLibsFolder("");
		mJWebSocketConfig = JWebSocketConfig.getConfig();
		mAdminConfig = new AdminConfig();
		mLog = aLog;
	}

	/**
	 * Send notification the all client conected
	 *
	 * @param aConnector
	 * @param aAction
	 * @param aResult
	 * @param aMessage
	 */
	private void traceLog(WebSocketConnector aConnector, String aAction, 
			String aResult, String aMessage) {
		if (mLog.isInfoEnabled()) {
			mLog.info("| " + aAction + " | " + aResult + " | " + aMessage);
		}

		if (aConnector != null) {
			Token lResponse = new MapToken(mNamespace, "traceLog");

			FastMap lMap = new FastMap();
			lMap.put("date", DateHandler.getCurrentDate());
			lMap.put("time", DateHandler.getCurrentTime());
			lMap.put("action", aAction);
			lMap.put("result", aResult);
			lMap.put("desc", aMessage);

			lResponse.setMap("log", lMap);
			mServer.sendToken(aConnector, lResponse);
		}
	}

	/**
	 * reload the jWebSocket config temporally.
	 *
	 * @param aConnector
	 */
	private void refreshJWebSocketConfig(WebSocketConnector aConnector) {
		try {
			JWebSocketLoader lLoader = new JWebSocketLoader();
			JWebSocketConfig lConfig = lLoader.loadConfiguration(
					JWebSocketConfig.getConfigPath());

			if (null != lConfig) {
				mJWebSocketConfig = lConfig;
			} else {
				traceLog(aConnector, "Refresh the configuration", "Error", 
						"The settings don't refresh.");
			}
		} catch (Exception ex) {
			traceLog(aConnector, "Refresh the configuration", "Error", 
					ex.getClass().getSimpleName() + " on refreshJWebSocketConfig: " + 
					ex.getMessage());
		}
	}

	/**
	 * load the AdminPlugin config file (jwsMgmtDesk.xml) 
	 * 
	 */
	private void loadMgmtDeskConfig() {
		AdminConfig lConfig = null;
		String lConfigFilePath = JWebSocketConfig.getConfigFolder(JWS_MGMT_DESK_PATH);
		AdminConfigHandler lConfigHandler = new AdminConfigHandler();
		try {
			File lFile = new File(lConfigFilePath);
			FileInputStream lFIS = new FileInputStream(lFile);
			XMLInputFactory lFactory = XMLInputFactory.newInstance();
			XMLStreamReader lStreamReader = null;
			lStreamReader = lFactory.createXMLStreamReader(lFIS);
			lConfig = lConfigHandler.processConfig(lStreamReader);
			mAdminConfig = lConfig;
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " occurred while creating XML stream (" 
					+ lConfigFilePath + ").");
		}
	}

	/**
	 * return the last logs registered. 
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getAdminLogs(WebSocketConnector aConnector, Token aToken) {
		List<Map> lTokenLogs = new FastList();
		Token lResponse = mServer.createResponse(aToken);

		try {
			BufferedReader lBufferedReader = new BufferedReader(new FileReader(mPathLog));

			Long lDeadline = DateHandler.substractDays(new Date(), mNumberOfDays).getTime();
			DateFormat lFormatter = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");

			String lLog = null;
			while ((lLog = lBufferedReader.readLine()) != null) {
				String[] lLogSplit = lLog.split("\\|");
				if (5 == lLogSplit.length) {
					Date lDateTemp = (Date) lFormatter.parse(lLogSplit[0].trim() + 
							lLogSplit[1].trim());

					if (lDeadline <= lDateTemp.getTime()) {
						FastMap lMap = new FastMap();
						lMap.put("date", lLogSplit[0].trim());
						lMap.put("time", lLogSplit[1].trim());
						lMap.put("action", lLogSplit[2].trim());
						lMap.put("result", lLogSplit[3].trim());
						lMap.put("desc", lLogSplit[4].trim());

						lTokenLogs.add(lMap);
					}
				}
			}
			lResponse.setList("logs", lTokenLogs);
			lResponse.setInteger("totalCount", lTokenLogs.size());
			lResponse.setString("msg", "Was obtained the logs of the last " + 
					mNumberOfDays + " days.");
			lBufferedReader.close();
			traceLog(aConnector, "Read Logs", "Successful", 
					"Was obtained the logs of the last " + mNumberOfDays + " days.");

		} catch (FileNotFoundException ex) {
			lResponse.setList("logs", lTokenLogs);
			lResponse.setString("msg", "Has been created a new log file for AdminPlugIn.");
			traceLog(aConnector, "Read Logs", "Successful", 
					"Has been created a new log file for AdminPlugIn.");
		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read Logs", "Error", this.getClass().getSimpleName() + 
					" on getAdminLogs: " + ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * return all plugins config of the PluginChain
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getPlugInsConfig(WebSocketConnector aConnector, Token aToken) {
		List<Map> lTokenPlugIn = new FastList();
		Token lResponse = mServer.createResponse(aToken);

		try {
			for (WebSocketPlugIn lPlugIn : mServer.getPlugInChain().getPlugIns()) {
				PluginConfig lConfig = (PluginConfig) lPlugIn.getPluginConfiguration();

				FastMap lMap = new FastMap();
				lMap.put("id", lConfig.getId());
				lMap.put("name", lConfig.getName());
				lMap.put("namespace", lConfig.getNamespace());
				lMap.put("jar", lConfig.getJar());
				lMap.put("servers", lConfig.getServers());
				lMap.put("enabled", lConfig.getEnabled());

				lTokenPlugIn.add(lMap);
			}
			lResponse.setList("plugins", lTokenPlugIn);
			lResponse.setInteger("totalCount", lTokenPlugIn.size());
			lResponse.setString("msg", "Was obtained the plugins configuration");
			traceLog(aConnector, "Read PlugIns", "Successful", 
					"Was obtained the plugins configuration");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read PlugIns", "Error", this.getClass().getSimpleName() + 
					" on getPlugInsConfig: " + ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * return all filters config of the FilterChain
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getFiltersConfig(WebSocketConnector aConnector, Token aToken) {
		List<Map> lTokenFilter = new FastList();
		Token lResponse = mServer.createResponse(aToken);

		try {
			for (WebSocketFilter lFilter : mServer.getFilterChain().getFilters()) {
				FilterConfig lConfig = (FilterConfig) lFilter.getFilterConfiguration();

				FastMap lMap = new FastMap();
				lMap.put("id", lConfig.getId());
				lMap.put("name", lConfig.getName());
				lMap.put("namespace", lConfig.getNamespace());
				lMap.put("jar", lConfig.getJar());
				lMap.put("servers", lConfig.getServers());
				lMap.put("enabled", lConfig.getEnabled());

				lTokenFilter.add(lMap);
			}
			lResponse.setList("filters", lTokenFilter);
			lResponse.setInteger("totalCount", lTokenFilter.size());
			lResponse.setString("msg", "Was obtained the filters configuration");
			traceLog(aConnector, "Read Filters", "Successful", 
					"Was obtained the filters configuration");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read Filters", "Error", this.getClass().getSimpleName() + 
					" on getFiltersConfig: " + ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * return all name of jars located in the libs folder
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getJars(WebSocketConnector aConnector, Token aToken) {
		List<Map> lJars = new FastList();
		Token lResponse = mServer.createResponse(aToken);

		try {
			File pathOfLibs = new File(mPathLibs);
			if (false == pathOfLibs.exists() || false == pathOfLibs.isDirectory()) {
				throw new IllegalArgumentException(
						"The library path is incorrect. Review the settings of AdminPlugIn");
			}

			for (String lfileName : pathOfLibs.list()) {
				if (lfileName.toLowerCase().endsWith(".jar")) {
					FastMap lMap = new FastMap();
					lMap.put("jar", lfileName);
					lJars.add(lMap);
				}
			}
			lResponse.setList("jars", lJars);
			lResponse.setInteger("totalCount", lJars.size());
			lResponse.setString("msg", "Was obtained the java libraries.");
			traceLog(aConnector, "Read Jar", "Successful", "Was obtained the java libraries.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read Jar", "Error", this.getClass().getSimpleName() + 
					" on getJars: " + ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * return all ids of plugins container in the jar
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getPlugInsByJar(WebSocketConnector aConnector, Token aToken) {
		List<Map> lIdPlugIn = new FastList();
		Token lResponse = mServer.createResponse(aToken);
		String lJar = aToken.getString("jar");

		try {
			if (lJar == null || lJar.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			for (PluginConfig lConfig : mJWebSocketConfig.getPlugins()) {
				if (lConfig.getJar().equals(lJar)) {
					if (null == mServer.getPlugInById(lConfig.getId())) {
						FastMap lMap = new FastMap();
						lMap.put("idPlugIn", lConfig.getId());
						lIdPlugIn.add(lMap);
					}
				}
			}
			//Load the plugins config that has been removed
			loadMgmtDeskConfig();
			for (PluginConfig lConfig : mAdminConfig.getPlugins()) {
				if (lConfig.getJar().equals(lJar)) {
					if (null == mServer.getPlugInById(lConfig.getId())) {
						FastMap lMap = new FastMap();
						lMap.put("idPlugIn", lConfig.getId());
						lIdPlugIn.add(lMap);
					}
				}
			}

			if (lIdPlugIn.isEmpty()) {
				throw new Exception("Can't found any plugin in the jar '" + lJar + "'.");
			}
			
			

			lResponse.setList("plugInsByJar", lIdPlugIn);
			lResponse.setInteger("totalCount", lIdPlugIn.size());
			lResponse.setString("msg", "Was obtained the plugins belonging to the library " + 
					lJar);
			traceLog(aConnector, "Read PlugIns By Jar", "Successful", 
					"Was obtained the plugins belonging to the library " + lJar);

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read PlugIns By Jar", "Error", this.getClass().getSimpleName() + 
					" on getPlugInsByJar: " + ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * return all ids of filters container in the jar 
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getFilterByJar(WebSocketConnector aConnector, Token aToken) {
		List<Map> lIdFilter = new FastList();
		Token lResponse = mServer.createResponse(aToken);
		String lJar = aToken.getString("jar");

		try {
			if (lJar == null || lJar.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			for (FilterConfig lConfig : mJWebSocketConfig.getFilters()) {
				if (lConfig.getJar().equals(lJar)) {
					if (null == mServer.getFilterById(lConfig.getId())) {
						FastMap lMap = new FastMap();
						lMap.put("idFilter", lConfig.getId());
						lIdFilter.add(lMap);
					}
				}
			}

			//Load the plugins config that has been removed
			loadMgmtDeskConfig();
			for (FilterConfig lConfig : mAdminConfig.getFilters()) {
				if (lConfig.getJar().equals(lJar)) {
					if (null == mServer.getFilterById(lConfig.getId())) {
						FastMap lMap = new FastMap();
						lMap.put("idFilter", lConfig.getId());
						lIdFilter.add(lMap);
					}
				}
			}

			if (lIdFilter.isEmpty()) {
				throw new Exception("Can't found any filter in the jar '" + lJar + "'.");
			}

			lResponse.setList("filtersByJar", lIdFilter);
			lResponse.setInteger("totalCount", lIdFilter.size());
			lResponse.setString("msg", "Was obtained the filters belonging to the library " + 
					lJar);
			traceLog(aConnector, "Read Filters By Jar", "Successful", 
					"Was obtained the filters belonging to the library " + lJar);

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read Filters By Jar", "Error", 
					this.getClass().getSimpleName() + " on getFilterByJar: " + ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * return the plugin config by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getPlugInConfigById(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");

		try {
			refreshJWebSocketConfig(aConnector);
			PluginConfig lPlugInConfig = mJWebSocketConfig.getPlugin(lId);

			if (null == lPlugInConfig) {
				lPlugInConfig = mAdminConfig.getPlugin(lId);
			}

			if (lPlugInConfig == null) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			lResponse.setString("name", lPlugInConfig.getName());
			lResponse.setString("namespace", lPlugInConfig.getNamespace());
			lResponse.setList("servers", lPlugInConfig.getServers());

			traceLog(aConnector, "Get PlugIn by Id", "Successful", 
					"Get plugin config by id '" + lId + "'.");
		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Get PlugIn by Id", "Error", 
					this.getClass().getSimpleName() + 
					" on getPluginConfigById: " + ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * return the filter config by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token getFilterConfigById(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");

		try {
			refreshJWebSocketConfig(aConnector);
			FilterConfig lFilterConfig = mJWebSocketConfig.getFilter(lId);

			if (null == lFilterConfig) {
				lFilterConfig = mAdminConfig.getFilter(lId);
			}

			if (lFilterConfig == null) {
				throw new Exception(
						"Has caused an error because the input parameter are wrong.");
			}

			lResponse.setString("id", lFilterConfig.getId());
			lResponse.setString("name", lFilterConfig.getName());
			lResponse.setString("namespace", lFilterConfig.getNamespace());
			lResponse.setList("servers", lFilterConfig.getServers());

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Get filter by Id", "Error", 
					this.getClass().getSimpleName() + " on getFilterConfigById: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Change the order of plugin in the PluginChain. (Up or Down <N> step)
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token changeOrderOfPlugInChain(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		Integer lStepsMove = aToken.getInteger("steps");

		try {
			List<WebSocketPlugIn> lPlugIns = mServer.getPlugInChain().getPlugIns();
			if (lId == null || lId.isEmpty() || lStepsMove == null) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			Integer lPosition = -1;
			for (int i = 0; i < lPlugIns.size(); i++) {
				PluginConfig lConfig = (PluginConfig) 
						lPlugIns.get(i).getPluginConfiguration();
				if (lConfig.getId().equals(lId)) {
					lPosition = i;
					i = lPlugIns.size();
				}
			}

			if (-1 == lPosition || Math.abs(lStepsMove) != 1) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if ((0 == lPosition && -1 == lStepsMove) || 
					(lPlugIns.size() - 1 == lPosition && 1 == lStepsMove)) {
				throw new Exception("This action can not be made, is invalid.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			synchronized (mServer.getPlugInChain()) {
				WebSocketPlugIn aPlugIn = lPlugIns.get(lPosition);
				lPlugIns.set(lPosition, lPlugIns.get(lPosition + lStepsMove));
				lPlugIns.set(lPosition + lStepsMove, aPlugIn);
				lJWSConfig.changeOrderOfPlugInConfig(lId, lStepsMove);
			}

			lResponse.setString("msg", "Changed order of the Plugin chain.");
			traceLog(aConnector, "Change Order of PlugIns", "Successful", 
					"Changed order of the Plugin chain.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Change Order of PlugIns", "Error", 
					this.getClass().getSimpleName() + " on changeOrderOfPlugInChain: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Change the order of filter in the FilterChain. (Up or Down <N> step)
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token changeOrderOfFilterChain(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		Integer lStepsMove = aToken.getInteger("steps");

		try {
			List<WebSocketFilter> lFilters = mServer.getFilterChain().getFilters();
			if (lId == null || lId.isEmpty() || lStepsMove == null) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			Integer lPosition = -1;
			for (int i = 0; i < lFilters.size(); i++) {
				FilterConfig lConfig = (FilterConfig) 
						lFilters.get(i).getFilterConfiguration();
				if (lConfig.getId().equals(lId)) {
					lPosition = i;
					i = lFilters.size();
				}
			}

			if (-1 == lPosition || Math.abs(lStepsMove) != 1) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if ((0 == lPosition && -1 == lStepsMove) || 
					(lFilters.size() - 1 == lPosition && 1 == lStepsMove)) {
				throw new Exception("This action can not be made, is invalid.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			synchronized (mServer.getFilterChain()) {
				WebSocketFilter lFilter = lFilters.get(lPosition);
				lFilters.set(lPosition, lFilters.get(lPosition + lStepsMove));
				lFilters.set(lPosition + lStepsMove, lFilter);
				lJWSConfig.changeOrderOfFilterConfig(lId, lStepsMove);
			}
			lResponse.setString("msg", "Changed order of the Filter chain.");
			traceLog(aConnector, "Change Order of Filters", "Successful", 
					"Changed order of the Filter chain.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Change Order of PlugIns", "Error", 
					this.getClass().getSimpleName() + " on changeOrderOfFilterChain: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Enable a plugin in the PluginChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token enablePlugIn(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter the reason of change are null or empty.");
			}

			WebSocketPlugIn lPlugIn = mServer.getPlugInChain().getPlugIn(lId);

			if (null == lPlugIn) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			String lVersion = lPlugIn.getVersion();

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (!lPlugIn.getEnabled()) {
				lPlugIn.setEnabled(true);
				lJWSConfig.setEnabledPlugIn(lId, true);

				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, 
						ChangeType.ENABLED, lVersion, lReason);
				mServer.broadcastToken(lReasonOfChange);
			}

			lResponse.setString("msg", "The PlugIn is already working.");
			traceLog(aConnector, "Enable PlugIn", "Successful", 
					"The PlugIn is already working.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Enable PlugIn", "Error", 
					this.getClass().getSimpleName() + " on enablePlugIn: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Enable a filter in the FilterChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token enableFilter(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			WebSocketFilter lFilter = mServer.getFilterChain().getFilterById(lId);

			if (null == lFilter) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			String lVersion = lFilter.getVersion();

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (false == lFilter.getEnabled()) {
				lFilter.setEnabled(true);
				lJWSConfig.setEnabledFilter(lId, true);

				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, 
						ChangeType.ENABLED, lVersion, lReason);
				mServer.broadcastToken(lReasonOfChange);
			}


			lResponse.setString("msg", "The Filter is already working.");
			traceLog(aConnector, "Enable Filter", "Successful", 
					"The Filter is already working.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Enable Filter", "Error", 
					this.getClass().getSimpleName() + " on enableFilter: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Disable a plugin in the PluginChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token disablePlugIn(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			WebSocketPlugIn lPlugIn = mServer.getPlugInChain().getPlugIn(lId);

			if (null == lPlugIn) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			String lVersion = lPlugIn.getVersion();

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (true == lPlugIn.getEnabled()) {
				lPlugIn.setEnabled(false);
				lJWSConfig.setEnabledPlugIn(lId, false);

				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, 
						ChangeType.DISABLED, lVersion, lReason);
				mServer.broadcastToken(lReasonOfChange);
			}


			lResponse.setString("msg", "The plugin has been stopped.");
			traceLog(aConnector, "Disable PlugIn", "Successful", 
					"The plugin has been stopped.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Disable PlugIn", "Error", 
					this.getClass().getSimpleName() + " on disablePlugIn: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Disable a filter in the FilterChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token disableFilter(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			WebSocketFilter lFilter = mServer.getFilterChain().getFilterById(lId);

			if (null == lFilter) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			String lVersion = lFilter.getVersion();

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (true == lFilter.getEnabled()) {
				lFilter.setEnabled(false);
				lJWSConfig.setEnabledFilter(lId, false);

				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, 
						ChangeType.DISABLED, lVersion, lReason);
				mServer.broadcastToken(lReasonOfChange);
			}

			lResponse.setString("msg", "The Filter has been stopped.");
			traceLog(aConnector, "Disable Filter", "Successful", 
					"The Filter has been stopped.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Disable Filter", "Error", 
					this.getClass().getSimpleName() + " on disableFilter: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Add a new plugin in the PluginChain
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token addPlugIn(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		Boolean lLoadOfTemp = false;
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			PluginConfig lPlugInConfig = mJWebSocketConfig.getPlugin(lId);

			if (null == lPlugInConfig) {
				lPlugInConfig = mAdminConfig.getPlugin(lId);
				lLoadOfTemp = true;
			}

			if (null == lPlugInConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Plug-in '" + lPlugInConfig.getName() + 
						"' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lPlugInConfig.getJar());
			Class<WebSocketPlugIn> lPlugInClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading plug-in '" + lPlugInConfig.getName() + 
							"' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lPlugInClass = (Class<WebSocketPlugIn>) 
						lClassLoader.reloadClass(lPlugInConfig.getName());
			}

			// if class found try to create an instance
			if (lPlugInClass != null) {
				WebSocketPlugIn lPlugIn = null;

				Constructor<WebSocketPlugIn> lPlugInConstructor = null;
				lPlugInConstructor = lPlugInClass.getConstructor(PluginConfiguration.class);

				if (lPlugInConstructor != null) {
					lPlugInConstructor.setAccessible(true);
					lPlugIn = lPlugInConstructor.newInstance(lPlugInConfig);

					if (lPlugIn != null) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Plug-in '" + lPlugInConfig.getId() + 
									"' successfully instantiated.");
						}

						String lVersion = lPlugIn.getVersion();

						//Try add the settings if they were loaded with the temp
						if (lLoadOfTemp) {
							JWebSocketConfigHandler lJWSConfig = 
									new JWebSocketConfigHandler();
							lJWSConfig.addPlugInConfig(lId);
						}

						//Create reason of change for the jWebSocket Client 
						Token lReasonOfChange = new MapToken();
						((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, 
								ChangeType.ADDED, lVersion, lReason);

						// now add the plugin to plugin chain on server ids
						for (String lServerId : lPlugInConfig.getServers()) {
							WebSocketServer lServerTemp = 
									JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {

								if (false == lLoadOfTemp) {
									Integer lPosition = 
											mJWebSocketConfig.getPlugins().indexOf(lPlugInConfig);
									lServerTemp.getPlugInChain().addPlugIn(lPosition, lPlugIn);
								} else {
									lServerTemp.getPlugInChain().addPlugIn(lPlugIn);
								}
								//Send reason of change for the jWebSocket Client 
								((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
							}
						}
					} else {
						throw new Exception("Couldn't instantiate the plug-in.");
					}
				} else {
					throw new Exception("Plug-in '" + lPlugInConfig.getId() + 
							"' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading plug-in '" + 
						lPlugInConfig.getName() + "' from '" + lJarFilePath);
			}

			lResponse.setString("msg", "The plugin has been instantiated.");
			traceLog(aConnector, "Add PlugIn", "Successful", 
					"The plugin has been instantiated.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Add PlugIn", "Error", 
					this.getClass().getSimpleName() + " on addPlugIn: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Add a new filter in the FilterChain
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token addFilter(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		Boolean lLoadOfTemp = false;
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			FilterConfig lFilterConfig = mJWebSocketConfig.getFilter(lId);

			if (null == lFilterConfig) {
				lFilterConfig = mAdminConfig.getFilter(lId);
				lLoadOfTemp = true;
			}

			if (null == lFilterConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Filter '" + lFilterConfig.getName() + 
						"' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lFilterConfig.getJar());
			Class<WebSocketFilter> lFilterClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading filter '" + lFilterConfig.getName() + 
							"' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lFilterClass = (Class<WebSocketFilter>) 
						lClassLoader.reloadClass(lFilterConfig.getName());
			}

			// if class found try to create an instance
			if (lFilterClass != null) {
				WebSocketFilter lFilter = null;

				Constructor<WebSocketFilter> lFilterConstructor = null;
				lFilterConstructor = lFilterClass.getConstructor(FilterConfiguration.class);

				if (lFilterConstructor != null) {
					lFilterConstructor.setAccessible(true);
					lFilter = lFilterConstructor.newInstance(lFilterConfig);

					if (lFilter != null) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Filter '" + lFilterConfig.getId() + 
									"' successfully instantiated.");
						}

						String lVersion = lFilter.getVersion();

						//Try add the settings if they were loaded with the temp
						if (lLoadOfTemp) {
							JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();
							lJWSConfig.addFilterConfig(lId);
						}

						//Create reason of change for the jWebSocket Client 
						Token lReasonOfChange = new MapToken();
						((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, 
								ChangeType.ADDED, lVersion, lReason);

						// now add the plugin to plugin chain on server ids
						for (String lServerId : lFilterConfig.getServers()) {
							WebSocketServer lServerTemp = 
									JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {
								
								if (false == lLoadOfTemp) {
									Integer lPosition = 
											mJWebSocketConfig.getFilters().indexOf(lFilterConfig);
									lServerTemp.getFilterChain().addFilter(lPosition, lFilter);
								} else {
									lServerTemp.getFilterChain().addFilter(lFilter);
								}
								//Send reason of change for the jWebSocket Client 
								((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
							}
						}
					} else {
						throw new Exception("Couldn't instantiate the filter.");
					}
				} else {
					throw new Exception("Filter '" + lFilterConfig.getId() + 
							"' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading filter '" + 
						lFilterConfig.getName() + "' from '" + lJarFilePath);
			}

			lResponse.setString("msg", "The filter has been instantiated.");
			traceLog(aConnector, "Add Filter", "Successful", 
					"The filter has been instantiated.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Add Filter", "Error", 
					this.getClass().getSimpleName() + " on addFilter: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Remove a plugin by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token removePlugIn(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			WebSocketPlugIn lPlugIn = mServer.getPlugInChain().getPlugIn(lId);

			if (null == lPlugIn) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			String lVersion = lPlugIn.getVersion();

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();
			lPlugIn.setEnabled(false);
			lJWSConfig.removePlugInConfig(lId);

			//Create reason of change for the jWebSocket Client 
			Token lReasonOfChange = new MapToken();
			((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, 
					ChangeType.REMOVED, lVersion, lReason);

			// now add the plugin to plugin chain on server ids
			for (String lServerId : lPlugIn.getPluginConfiguration().getServers()) {
				WebSocketServer lServerTemp = JWebSocketFactory.getServer(lServerId);
				if (null != lServerTemp) {
					((TokenServer) lServerTemp).removePlugIn(lPlugIn);

					//Send reason of change for the jWebSocket Client 
					((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
				}
			}

			lResponse.setString("msg", "The plugin has been removed.");
			traceLog(aConnector, "Remove PlugIn", "Successful", 
					"The plugin has been removed.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Remove PlugIn", "Error", 
					this.getClass().getSimpleName() + " on removePlugIn: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Remove a filter by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token removeFilter(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			WebSocketFilter lFilter = mServer.getFilterChain().getFilterById(lId);

			if (null == lFilter) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			String lVersion = lFilter.getVersion();

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();
			lFilter.setEnabled(false);
			lJWSConfig.removeFilterConfig(lId);

			//Create reason of change for the jWebSocket Client 
			Token lReasonOfChange = new MapToken();
			((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, 
					ChangeType.REMOVED, lVersion, lReason);

			// now add the filter to filter chain on server ids
			for (String lServerId : lFilter.getFilterConfiguration().getServers()) {
				WebSocketServer lServerTemp = JWebSocketFactory.getServer(lServerId);
				if (null != lServerTemp) {
					((TokenServer) lServerTemp).getFilterChain().removeFilter(lFilter);

					//Send reason of change for the jWebSocket Client 
					((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
				}
			}

			lResponse.setString("msg", "The Filter has been removed.");
			traceLog(aConnector, "Remove Filter", "Successful", "The Filter has been removed.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Remove Filter", "Error", 
					this.getClass().getSimpleName() + " on removeFilter: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Reload a plugin by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token reloadPlugIn(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter "
						+ "the reason of change are null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			PluginConfig lPlugInConfig = mJWebSocketConfig.getPlugin(lId);

			if (null == lPlugInConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Plug-in '" + lPlugInConfig.getName() + 
						"' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lPlugInConfig.getJar());
			Class<WebSocketPlugIn> lPlugInClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading plug-in '" + lPlugInConfig.getName() + 
							"' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lPlugInClass = (Class<WebSocketPlugIn>) 
						lClassLoader.reloadClass(lPlugInConfig.getName());
			}

			//if class found try to create an instance
			if (lPlugInClass != null) {
				WebSocketPlugIn lPlugIn = null;

				Constructor<WebSocketPlugIn> lPlugInConstructor = null;
				lPlugInConstructor = lPlugInClass.getConstructor(PluginConfiguration.class);

				if (lPlugInConstructor != null) {
					lPlugInConstructor.setAccessible(true);
					lPlugIn = lPlugInConstructor.newInstance(lPlugInConfig);

					if (lPlugIn != null) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Plug-in '" + lPlugInConfig.getId() + 
									"' successfully instantiated.");
						}

						String lVersion = lPlugIn.getVersion();

						//now add the plugin to plugin chain on server ids
						for (String lServerId : lPlugInConfig.getServers()) {
							WebSocketServer lServerTemp = 
									JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {
								TokenPlugInChain lPlugInChain = 
										(TokenPlugInChain) lServerTemp.getPlugInChain();
								//Create reason of change for the jWebSocket Client
								Token lReasonOfChange = new MapToken();
								if (lPlugInChain.reloadPlugIn(lPlugIn, 
										lReasonOfChange, lVersion, lReason)) {
									//Send reason of change for the jWebSocket Client 
									((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
								} else {
									throw new Exception(""
											+ "Couldn't reload the plug-in on the server " + 
											lServerId);
								}
							}
						}
					} else {
						throw new Exception("Couldn't reload the plug-in.");
					}
				} else {
					throw new Exception("Plug-in '" + lPlugInConfig.getId() + 
							"' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading plug-in '" + 
						lPlugInConfig.getName() + "' from '" + lJarFilePath);
			}


			lResponse.setString("msg", "The plugin has been reload.");
			traceLog(aConnector, "Reload PlugIn", "Successful", "The plugin has been reload.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Reload PlugIn", "Error", 
					this.getClass().getSimpleName() + " on reloadPlugIn: " + 
					ex.getMessage());
		}

		return lResponse;
	}

	/**
	 * Reload a filter by id
	 *
	 * @param aConnector
	 * @param aToken
	 * 
	 * @return Token
	 */
	public Token reloadFilter(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = mServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lReason = aToken.getString("reason");

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input parameter is null or empty.");
			}

			if (lReason == null || lReason.isEmpty()) {
				throw new Exception(
						"Has caused an error because the input "
						+ "parameter the reason of change are null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			FilterConfig lFilterConfig = mJWebSocketConfig.getFilter(lId);

			if (null == lFilterConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Filter '" + lFilterConfig.getName() + 
						"' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lFilterConfig.getJar());
			Class<WebSocketFilter> lFilterClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading filter '" + lFilterConfig.getName() + 
							"' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lFilterClass = (Class<WebSocketFilter>) 
						lClassLoader.reloadClass(lFilterConfig.getName());
			}

			// if class found try to create an instance
			if (lFilterClass != null) {
				WebSocketFilter lFilter = null;

				Constructor<WebSocketFilter> lFilterConstructor = null;
				lFilterConstructor = lFilterClass.getConstructor(FilterConfiguration.class);

				if (lFilterConstructor != null) {
					lFilterConstructor.setAccessible(true);
					lFilter = lFilterConstructor.newInstance(lFilterConfig);

					if (lFilter != null) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Filter '" + lFilterConfig.getId() + 
									"' successfully instantiated.");
						}

						String lVersion = lFilter.getVersion();

						// now add the filter to filter chain on server ids
						for (String lServerId : lFilterConfig.getServers()) {
							WebSocketServer lServerTemp = 
									JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {
								TokenFilterChain lFilterChain = 
										(TokenFilterChain) lServerTemp.getFilterChain();
								//Create reason of change for the jWebSocket Client 
								Token lReasonOfChange = new MapToken();
								if (lFilterChain.reloadFilter(lFilter, 
										lReasonOfChange, lVersion, lReason)) {
									//Send reason of change for the jWebSocket Client 
									((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
								} else {
									throw new Exception(
											"Couldn't reload the filter on the server " + 
											lServerId);
								}
							}
						}
					} else {
						throw new Exception("Couldn't instantiate the filter.");
					}
				} else {
					throw new Exception("Filter '" + lFilterConfig.getId() + 
							"' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading filter '" + 
						lFilterConfig.getName() + "' from '" + lJarFilePath);
			}

			lResponse.setString("msg", "The filter has been reload.");
			traceLog(aConnector, "Reload Filter", "Successful", 
					"The filter has been reload.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Reload Filter", "Error", 
					this.getClass().getSimpleName() + " on reloadFilter: " + 
					ex.getMessage());
		}

		return lResponse;
	}
}
