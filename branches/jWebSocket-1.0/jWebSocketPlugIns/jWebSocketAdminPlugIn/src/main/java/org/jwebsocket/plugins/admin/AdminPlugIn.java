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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.config.xml.JWebSocketConfigHandler;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.factory.JWebSocketJarClassLoader;
import org.jwebsocket.factory.JWebSocketLoader;
import org.jwebsocket.filter.TokenFilter;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.ChangeType;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.security.User;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.util.DateHandler;
import org.jwebsocket.token.MapToken;
import org.jwebsocket.token.Token;

/**
 * @author aschulze
 * @author markos0886
 */
public class AdminPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(AdminPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_ADMIN = JWebSocketServerConstants.NS_BASE + ".plugins.admin";
	private static boolean mExternalShutDownAllowed = false;
	private static Integer mNumberOfDays = null;
	private static String mPathLog = null;
	private static String mPathLibs = null;
	private static JWebSocketConfig mJWebSocketConfig = null;

	/**
	 * Constructor that takes configuration object
	 */
	public AdminPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating admin plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_ADMIN);

		mExternalShutDownAllowed = "true".equalsIgnoreCase(aConfiguration.getString("allowShutdown"));

		mNumberOfDays = Integer.parseInt(aConfiguration.getString("numberOfDays"));

		mPathLog = JWebSocketConfig.getLogsFolder("adminLog.log");

		mPathLibs = JWebSocketConfig.getLibsFolder("");

		mJWebSocketConfig = JWebSocketConfig.getConfig();

		// give a success message to the administrator
		if (mLog.isDebugEnabled()) {
			mLog.debug("Admin plug-in successfully loaded.");
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			// remote shut down server
			if (lType.equals("shutdown")) {
				shutdown(aConnector, aToken);
			} else if (lType.equals("gc")) {
				gc(aConnector, aToken);
			} else if (lType.equals("getConnections")) {
				getConnections(aConnector, aToken);
			} else if (lType.equals("getUserRights")) {
				getUserRights(aConnector, aToken);
			} else if (lType.equals("getUserRoles")) {
				getUserRoles(aConnector, aToken);
			} else if (lType.equals("getAdminLogs")) {
				getAdminLogs(aConnector, aToken);
			} else if (lType.equals("getPlugInsConfig")) {
				getPlugInsConfig(aConnector, aToken);
			} else if (lType.equals("getFiltersConfig")) {
				getFiltersConfig(aConnector, aToken);
			} else if (lType.equals("getJars")) {
				getJars(aConnector, aToken);
			} else if (lType.equals("getPlugInsByJar")) {
				getPlugInsByJar(aConnector, aToken);
			} else if (lType.equals("getFilterByJar")) {
				getFilterByJar(aConnector, aToken);
			} else if (lType.equals("getPlugInConfigById")) {
				getPlugInConfigById(aConnector, aToken);
			} else if (lType.equals("getFilterConfigById")) {
				getFilterConfigById(aConnector, aToken);
			} else if (lType.equals("changeOrderOfPlugInChain")) {
				changeOrderOfPlugInChain(aConnector, aToken);
			} else if (lType.equals("changeOrderOfFilterChain")) {
				changeOrderOfFilterChain(aConnector, aToken);
			} else if (lType.equals("enablePlugIn")) {
				enablePlugIn(aConnector, aToken);
			} else if (lType.equals("enableFilter")) {
				enableFilter(aConnector, aToken);
			} else if (lType.equals("disablePlugIn")) {
				disablePlugIn(aConnector, aToken);
			} else if (lType.equals("disableFilter")) {
				disableFilter(aConnector, aToken);
			} else if (lType.equals("removePlugIn")) {
				removePlugIn(aConnector, aToken);
			} else if (lType.equals("removeFilter")) {
				removeFilter(aConnector, aToken);
			} else if (lType.equals("addPlugIn")) {
				addPlugIn(aConnector, aToken);
			} else if (lType.equals("addFilter")) {
				addFilter(aConnector, aToken);
			} else if (lType.equals("reloadPlugIn")) {
				reloadPlugIn(aConnector, aToken);
			} else if (lType.equals("reloadFilter")) {
				reloadFilter(aConnector, aToken);
			}

			aResponse.abortChain();
		}
	}

	/**
	 * shutdown server
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void shutdown(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'shutdown'...");
		}

		// check if 'shutdown' request from client command is allowed at all
		if (!mExternalShutDownAllowed) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// check if user is allowed to run 'shutdown' command
		// should be limited to administrators
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_ADMIN + ".shutdown")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// notify all connected clients about pending shutdown
		Token lResponseToken = lServer.createResponse(aToken);
		lResponseToken.setString("msg", "Shutdown in progress...");
		lServer.broadcastToken(lResponseToken);

		JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
	}

	/**
	 * shutdown server
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void gc(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'Garbage Collection '...");
		}

		// check if user is allowed to run 'shutdown' command
		// should be limited to administrators
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_ADMIN + ".gc")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		System.gc();

		// notify all connected clients about pending shutdown
		Token lResponse = lServer.createResponse(aToken);
		lResponse.setString("msg", "Garbage Collection in progress...");
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * return all sessions
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getConnections(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getConnections'...");
		}

		// check if user is allowed to run 'getConnections' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_ADMIN + ".getConnections")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		Token lResponse = lServer.createResponse(aToken);
		try {
			List<Map> lResultList = new FastList<Map>();
			Map lConnectorMap = lServer.getAllConnectors();
			Collection<WebSocketConnector> lConnectors = lConnectorMap.values();
			for (WebSocketConnector lConnector : lConnectors) {
				Map lResultItem = new FastMap<String, Object>();
				lResultItem.put("host", lConnector.getRemoteHost());
				lResultItem.put("port", lConnector.getRemotePort());
				// Caution! This method may only be granted to administrators!
				lResultItem.put("usid", lConnector.getSession().getSessionId());
				lResultItem.put("unid", lConnector.getNodeId());
				lResultItem.put("username", lConnector.getUsername());
				lResultItem.put("isToken", lConnector.getBoolean(TokenServer.VAR_IS_TOKENSERVER));
				lResultList.add(lResultItem);
			}
			lResponse.setList("connections", lResultList);
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getConnections: " + ex.getMessage());
		}
		lServer.sendToken(aConnector, lResponse);
	}

	private void getUserRights(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getUserRights'...");
		}

		// check if user is allowed to run 'getUserRights' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_ADMIN + ".getUserRights")) {
			// TODO: create right in jWebSocket.xml!
			// lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		String lUsername = aToken.getString("username");
		if (lUsername == null || lUsername.isEmpty()) {
			lUsername = aConnector.getUsername();
		}
		Token lResponse = lServer.createResponse(aToken);
		try {
			User lUser = SecurityFactory.getUser(lUsername);
			if (lUser != null) {
				List<String> lRightIds = new FastList(lUser.getRightIdSet());
				lResponse.setList("rights", lRightIds);
			} else {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "invalid user");
			}
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getUserRights: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getUserRoles(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getUserRoles'...");
		}

		// check if user is allowed to run 'getUserRoles' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_ADMIN + ".getUserRoles")) {
			// TODO: create right in jWebSocket.xml!
			// lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		String lUsername = aToken.getString("username");
		if (lUsername == null || lUsername.isEmpty()) {
			lUsername = aConnector.getUsername();
		}
		Token lResponse = lServer.createResponse(aToken);
		try {
			List<String> lRoleIds = new FastList(SecurityFactory.getRoleIdSet(lUsername));
			if (lRoleIds != null) {
				lResponse.setList("roles", lRoleIds);
			} else {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "invalid user");
			}
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getUserRoles: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void traceLog(WebSocketConnector aConnector, String aAction, String aResult, String aMessage) {
		if (mLog.isInfoEnabled()) {
			mLog.info("| " + aAction + " | " + aResult + " | " + aMessage);
		}

		Token lResponse = new MapToken(NS_ADMIN, "traceLog");

		FastMap lMap = new FastMap();
		lMap.put("date", DateHandler.getCurrentDate());
		lMap.put("time", DateHandler.getCurrentTime());
		lMap.put("action", aAction);
		lMap.put("result", aResult);
		lMap.put("desc", aMessage);

		lResponse.setMap("log", lMap);
		getServer().sendToken(aConnector, lResponse);
	}

	private void getAdminLogs(WebSocketConnector aConnector, Token aToken) {
		List<Map> lTokenLogs = new FastList();
		Token lResponse = getServer().createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getAdminLogs'...");
		}

		try {
			BufferedReader lBufferedReader = new BufferedReader(new FileReader(mPathLog));

			Long lDeadline = DateHandler.substractDays(new Date(), mNumberOfDays).getTime();
			DateFormat lFormatter = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");

			String lLog = null;
			while ((lLog = lBufferedReader.readLine()) != null) {
				String[] lLogSplit = lLog.split("\\|");
				if (5 == lLogSplit.length) {
					Date lDateTemp = (Date) lFormatter.parse(lLogSplit[0].trim() + lLogSplit[1].trim());

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
			lResponse.setString("msg", "Was obtained the logs of the last " + mNumberOfDays + " days.");
			lBufferedReader.close();
			traceLog(aConnector, "Read Logs", "Successful", "Was obtained the logs of the last " + mNumberOfDays + " days.");

		} catch (FileNotFoundException ex) {
			lResponse.setList("logs", lTokenLogs);
			lResponse.setString("msg", "Has been created a new log file for AdminPlugIn.");
			traceLog(aConnector, "Read Logs", "Successful", "Has been created a new log file for AdminPlugIn.");
		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read Logs", "Error", this.getClass().getSimpleName() + " on getAdminLogs: " + ex.getMessage());
		}

		getServer().sendToken(aConnector, lResponse);
	}

	private void refreshJWebSocketConfig(WebSocketConnector aConnector) {
		try {
			JWebSocketLoader lLoader = new JWebSocketLoader();
			JWebSocketConfig lConfig = lLoader.loadConfiguration(JWebSocketConfig.getConfigurationPath());

			if (null != lConfig) {
				mJWebSocketConfig = lConfig;
			} else {
				traceLog(aConnector, "Refresh the configuration", "Error", "The settings don't refresh.");
			}
		} catch (Exception ex) {
			traceLog(aConnector, "Refresh the configuration", "Error", ex.getClass().getSimpleName() + " on refreshJWebSocketConfig: " + ex.getMessage());
		}
	}

	private void getPlugInsConfig(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Map> lTokenPlugIn = new FastList();
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPlugInsConfig'...");
		}

		try {
			for (WebSocketPlugIn lPlugIn : lServer.getPlugInChain().getPlugIns()) {
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
			traceLog(aConnector, "Read PlugIns", "Successful", "Was obtained the plugins configuration");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read PlugIns", "Error", this.getClass().getSimpleName() + " on getPlugInsConfig: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getFiltersConfig(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Map> lTokenFilter = new FastList();
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFiltersConfig'...");
		}

		try {
			for (WebSocketFilter lFilter : lServer.getFilterChain().getFilters()) {
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
			traceLog(aConnector, "Read Filters", "Successful", "Was obtained the filters configuration");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read Filters", "Error", this.getClass().getSimpleName() + " on getFiltersConfig: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getJars(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Map> lJars = new FastList();
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getJars'...");
		}

		try {
			File pathOfLibs = new File(mPathLibs);
			if (false == pathOfLibs.exists() || false == pathOfLibs.isDirectory()) {
				throw new IllegalArgumentException("The library path is incorrect. Review the settings of AdminPlugIn");
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
			traceLog(aConnector, "Read Jar", "Error", this.getClass().getSimpleName() + " on getJars: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getPlugInsByJar(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Map> lIdPlugIn = new FastList();
		Token lResponse = lServer.createResponse(aToken);
		String lJar = aToken.getString("jar");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPlugInsByJar'...");
		}

		try {
			if (lJar == null || lJar.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			for (PluginConfig lConfig : mJWebSocketConfig.getPlugins()) {
				if (lConfig.getJar().equals(lJar)) {
					FastMap lMap = new FastMap();
					lMap.put("idPlugIn", lConfig.getId());
					lIdPlugIn.add(lMap);
				}
			}
			lResponse.setList("plugInsByJar", lIdPlugIn);
			lResponse.setInteger("totalCount", lIdPlugIn.size());
			lResponse.setString("msg", "Was obtained the plugins belonging to the library " + lJar);
			traceLog(aConnector, "Read PlugIns By Jar", "Successful", "Was obtained the plugins belonging to the library " + lJar);

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read PlugIns By Jar", "Error", this.getClass().getSimpleName() + " on getPlugInsByJar: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getFilterByJar(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Map> lIdFilter = new FastList();
		Token lResponse = lServer.createResponse(aToken);
		String lJar = aToken.getString("jar");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFilterByJar'...");
		}

		try {
			if (lJar == null || lJar.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			for (FilterConfig lConfig : mJWebSocketConfig.getFilters()) {
				if (lConfig.getJar().equals(lJar)) {
					FastMap lMap = new FastMap();
					lMap.put("idFilter", lConfig.getId());
					lIdFilter.add(lMap);
				}
			}
			lResponse.setList("filtersByJar", lIdFilter);
			lResponse.setInteger("totalCount", lIdFilter.size());
			lResponse.setString("msg", "Was obtained the filters belonging to the library " + lJar);
			traceLog(aConnector, "Read Filters By Jar", "Successful", "Was obtained the filters belonging to the library " + lJar);

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Read Filters By Jar", "Error", this.getClass().getSimpleName() + " on getFilterByJar: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getPlugInConfigById(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPluginConfigById'...");
		}

		try {
			refreshJWebSocketConfig(aConnector);
			PluginConfig lPlugInConfig = mJWebSocketConfig.getPlugin(lId);

			if (lPlugInConfig == null) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			lResponse.setString("name", lPlugInConfig.getName());
			lResponse.setString("namespace", lPlugInConfig.getNamespace());
			lResponse.setList("servers", lPlugInConfig.getServers());

			traceLog(aConnector, "Get PlugIn by Id", "Successful", "Get plugin config by id '" + lId + "'.");
		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Get PlugIn by Id", "Error", this.getClass().getSimpleName() + " on getPluginConfigById: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getFilterConfigById(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFilterConfigById'...");
		}

		try {
			refreshJWebSocketConfig(aConnector);
			FilterConfig lFilterConfig = mJWebSocketConfig.getFilter(lId);

			if (lFilterConfig == null) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			lResponse.setString("id", lFilterConfig.getId());
			lResponse.setString("name", lFilterConfig.getName());
			lResponse.setString("namespace", lFilterConfig.getNamespace());
			lResponse.setList("servers", lFilterConfig.getServers());

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Get filter by Id", "Error", this.getClass().getSimpleName() + " on getFilterConfigById: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void changeOrderOfPlugInChain(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		Integer lStepsMove = aToken.getInteger("steps");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'changeOrderOfPlugInChain'...");
		}

		try {
			List<WebSocketPlugIn> lPlugIns = lServer.getPlugInChain().getPlugIns();
			if (lId == null || lId.isEmpty() || lStepsMove == null) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			Integer lPosition = -1;
			for (int i = 0; i < lPlugIns.size(); i++) {
				PluginConfig lConfig = (PluginConfig) lPlugIns.get(i).getPluginConfiguration();
				if (lConfig.getId().equals(lId)) {
					lPosition = i;
					i = lPlugIns.size();
				}
			}

			if (-1 == lPosition || Math.abs(lStepsMove) != 1) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if ((0 == lPosition && -1 == lStepsMove) || (lPlugIns.size() - 1 == lPosition && 1 == lStepsMove)) {
				throw new Exception("This action can not be made, is invalid.");
			}

			synchronized (lServer.getPlugInChain()) {
				WebSocketPlugIn aPlugIn = lPlugIns.get(lPosition);
				lPlugIns.set(lPosition, lPlugIns.get(lPosition + lStepsMove));
				lPlugIns.set(lPosition + lStepsMove, aPlugIn);
			}
			
			lResponse.setString("msg", "Changed order of the Plugin chain.");
			traceLog(aConnector, "Change Order of PlugIns", "Successful", "Changed order of the Plugin chain.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Change Order of PlugIns", "Error", this.getClass().getSimpleName() + " on changeOrderOfPlugInChain: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void changeOrderOfFilterChain(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		Integer lStepsMove = aToken.getInteger("steps");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'changeOrderOfFilterChain'...");
		}

		try {
			List<WebSocketFilter> lFilters = lServer.getFilterChain().getFilters();
			if (lId == null || lId.isEmpty() || lStepsMove == null) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			Integer lPosition = -1;
			for (int i = 0; i < lFilters.size(); i++) {
				FilterConfig lConfig = (FilterConfig) lFilters.get(i).getFilterConfiguration();
				if (lConfig.getId().equals(lId)) {
					lPosition = i;
					i = lFilters.size();
				}
			}

			if (-1 == lPosition || Math.abs(lStepsMove) != 1) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if ((0 == lPosition && -1 == lStepsMove) || (lFilters.size() - 1 == lPosition && 1 == lStepsMove)) {
				throw new Exception("This action can not be made, is invalid.");
			}

			synchronized (lServer.getFilterChain()) {
				WebSocketFilter lFilter = lFilters.get(lPosition);
				lFilters.set(lPosition, lFilters.get(lPosition + lStepsMove));
				lFilters.set(lPosition + lStepsMove, lFilter);
			}
			lResponse.setString("msg", "Changed order of the Filter chain.");
			traceLog(aConnector, "Change Order of Filters", "Successful", "Changed order of the Filter chain.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Change Order of PlugIns", "Error", this.getClass().getSimpleName() + " on changeOrderOfFilterChain: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void enablePlugIn(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'enablePlugIn'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			WebSocketPlugIn lPlugIn = lServer.getPlugInChain().getPlugIn(lId);

			if (null == lPlugIn) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (!lPlugIn.getEnabled()) {
				lPlugIn.setEnabled(true);
				lJWSConfig.setEnabledPlugIn(lId, true);

				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, ChangeType.ENABLED, lVersion, lReason);
				lServer.broadcastToken(lReasonOfChange);
			}

			lResponse.setString("msg", "The PlugIn is already working.");
			traceLog(aConnector, "Enable PlugIn", "Successful", "The PlugIn is already working.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Enable PlugIn", "Error", this.getClass().getSimpleName() + " on enablePlugIn: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void enableFilter(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'enableFilter'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			WebSocketFilter lFilter = lServer.getFilterChain().getFilterById(lId);

			if (null == lFilter) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (false == lFilter.getEnabled()) {
				lFilter.setEnabled(true);
				lJWSConfig.setEnabledFilter(lId, true);

				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, ChangeType.ENABLED, lVersion, lReason);
				lServer.broadcastToken(lReasonOfChange);
			}


			lResponse.setString("msg", "The Filter is already working.");
			traceLog(aConnector, "Enable Filter", "Successful", "The Filter is already working.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Enable Filter", "Error", this.getClass().getSimpleName() + " on enableFilter: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void disablePlugIn(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'disablePlugIn'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			WebSocketPlugIn lPlugIn = lServer.getPlugInChain().getPlugIn(lId);

			if (null == lPlugIn) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (true == lPlugIn.getEnabled()) {
				lPlugIn.setEnabled(false);
				lJWSConfig.setEnabledPlugIn(lId, false);

				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, ChangeType.DISABLED, lVersion, lReason);
				lServer.broadcastToken(lReasonOfChange);
			}


			lResponse.setString("msg", "The plugin has been stopped.");
			traceLog(aConnector, "Disable PlugIn", "Successful", "The plugin has been stopped.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Disable PlugIn", "Error", this.getClass().getSimpleName() + " on disablePlugIn: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void disableFilter(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'disableFilter'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			WebSocketFilter lFilter = lServer.getFilterChain().getFilterById(lId);

			if (null == lFilter) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();

			if (true == lFilter.getEnabled()) {
				lFilter.setEnabled(false);
				lJWSConfig.setEnabledFilter(lId, false);
				
				//Send reason of change for the jWebSocket Client 
				Token lReasonOfChange = new MapToken();
				((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, ChangeType.DISABLED, lVersion, lReason);
				lServer.broadcastToken(lReasonOfChange);
			}

			lResponse.setString("msg", "The Filter has been stopped.");
			traceLog(aConnector, "Disable Filter", "Successful", "The Filter has been stopped.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Disable Filter", "Error", this.getClass().getSimpleName() + " on disableFilter: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void addPlugIn(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'addPlugIn'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			PluginConfig lPlugInConfig = mJWebSocketConfig.getPlugin(lId);

			if (null == lPlugInConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Plug-in '" + lPlugInConfig.getName() + "' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lPlugInConfig.getJar());
			Class<WebSocketPlugIn> lPlugInClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading plug-in '" + lPlugInConfig.getName() + "' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lPlugInClass = (Class<WebSocketPlugIn>) lClassLoader.reloadClass(lPlugInConfig.getName());
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
							mLog.debug("Plug-in '" + lPlugInConfig.getId() + "' successfully instantiated.");
						}

						//Create reason of change for the jWebSocket Client 
						Token lReasonOfChange = new MapToken();
						((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, ChangeType.ADD, lVersion, lReason);

						// now add the plugin to plugin chain on server ids
						for (String lServerId : lPlugInConfig.getServers()) {
							WebSocketServer lServerTemp = JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {
								lServerTemp.getPlugInChain().addPlugIn(lPlugIn);

								//Send reason of change for the jWebSocket Client 
								((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
							}
						}
					} else {
						throw new Exception("Couldn't instantiate the plug-in.");
					}
				} else {
					throw new Exception("Plug-in '" + lPlugInConfig.getId() + "' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading plug-in '" + lPlugInConfig.getName() + "' from '" + lJarFilePath);
			}

			lResponse.setString("msg", "The plugin has been instantiated.");
			traceLog(aConnector, "Add PlugIn", "Successful", "The plugin has been instantiated.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Add PlugIn", "Error", this.getClass().getSimpleName() + " on addPlugIn: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void addFilter(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'addFilter'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			FilterConfig lFilterConfig = mJWebSocketConfig.getFilter(lId);

			if (null == lFilterConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Filter '" + lFilterConfig.getName() + "' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lFilterConfig.getJar());
			Class<WebSocketFilter> lFilterClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading filter '" + lFilterConfig.getName() + "' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lFilterClass = (Class<WebSocketFilter>) lClassLoader.reloadClass(lFilterConfig.getName());
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
							mLog.debug("Filter '" + lFilterConfig.getId() + "' successfully instantiated.");
						}

						//Create reason of change for the jWebSocket Client 
						Token lReasonOfChange = new MapToken();
						((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, ChangeType.ADD, lVersion, lReason);

						// now add the plugin to plugin chain on server ids
						for (String lServerId : lFilterConfig.getServers()) {
							WebSocketServer lServerTemp = JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {
								lServerTemp.getFilterChain().addFilter(lFilter);

								//Send reason of change for the jWebSocket Client 
								((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
							}
						}
					} else {
						throw new Exception("Couldn't instantiate the filter.");
					}
				} else {
					throw new Exception("Filter '" + lFilterConfig.getId() + "' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading filter '" + lFilterConfig.getName() + "' from '" + lJarFilePath);
			}

			lResponse.setString("msg", "The filter has been instantiated.");
			traceLog(aConnector, "Add Filter", "Successful", "The filter has been instantiated.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Add Filter", "Error", this.getClass().getSimpleName() + " on addFilter: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void removePlugIn(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'removePlugIn'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			WebSocketPlugIn lPlugIn = lServer.getPlugInChain().getPlugIn(lId);

			if (null == lPlugIn) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();
			lPlugIn.setEnabled(false);
			lJWSConfig.removePlugInConfig(lId);

			//Create reason of change for the jWebSocket Client 
			Token lReasonOfChange = new MapToken();
			((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, ChangeType.REMOVE, lVersion, lReason);

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
			traceLog(aConnector, "Remove PlugIn", "Successful", "The plugin has been removed.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Remove PlugIn", "Error", this.getClass().getSimpleName() + " on removePlugIn: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void removeFilter(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'removeFilter'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			WebSocketFilter lFilter = lServer.getFilterChain().getFilterById(lId);

			if (null == lFilter) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			JWebSocketConfigHandler lJWSConfig = new JWebSocketConfigHandler();
			lFilter.setEnabled(false);
			lJWSConfig.removeFilterConfig(lId);

			//Create reason of change for the jWebSocket Client 
			Token lReasonOfChange = new MapToken();
			((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, ChangeType.REMOVE, lVersion, lReason);

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
			traceLog(aConnector, "Remove Filter", "Error", this.getClass().getSimpleName() + " on removeFilter: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void reloadPlugIn(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'reloadPlugIn'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			PluginConfig lPlugInConfig = mJWebSocketConfig.getPlugin(lId);

			if (null == lPlugInConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Plug-in '" + lPlugInConfig.getName() + "' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lPlugInConfig.getJar());
			Class<WebSocketPlugIn> lPlugInClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading plug-in '" + lPlugInConfig.getName() + "' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lPlugInClass = (Class<WebSocketPlugIn>) lClassLoader.reloadClass(lPlugInConfig.getName());
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
							mLog.debug("Plug-in '" + lPlugInConfig.getId() + "' successfully instantiated.");
						}

						//Create reason of change for the jWebSocket Client
						Token lReasonOfChange = new MapToken();
						((TokenPlugIn) lPlugIn).createReasonOfChange(lReasonOfChange, ChangeType.UPDATE, lVersion, lReason);

						//now add the plugin to plugin chain on server ids
						for (String lServerId : lPlugInConfig.getServers()) {
							WebSocketServer lServerTemp = JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {
								if (lServerTemp.getPlugInChain().reloadPlugIn(lPlugIn)) {
									//Send reason of change for the jWebSocket Client 
									((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
								} else {
									throw new Exception("Couldn't reload the plug-in on the server " + lServerId);
								}
							}
						}
					} else {
						throw new Exception("Couldn't reload the plug-in.");
					}
				} else {
					throw new Exception("Plug-in '" + lPlugInConfig.getId() + "' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading plug-in '" + lPlugInConfig.getName() + "' from '" + lJarFilePath);
			}


			lResponse.setString("msg", "The plugin has been reload.");
			traceLog(aConnector, "Reload PlugIn", "Successful", "The plugin has been reload.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Reload PlugIn", "Error", this.getClass().getSimpleName() + " on reloadPlugIn: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void reloadFilter(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);
		String lId = aToken.getString("id");
		String lVersion = aToken.getString("version");
		String lReason = aToken.getString("reason");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'reloadFilter'...");
		}

		try {
			if (lId == null || lId.isEmpty()) {
				throw new Exception("Has caused an error because the input parameter is null or empty.");
			}

			if (lVersion == null || lVersion.isEmpty() || lReason == null || lReason.isEmpty()) {
				throw new Exception("Has caused an error because the input parameters the reason of change are null or empty.");
			}

			refreshJWebSocketConfig(aConnector);

			FilterConfig lFilterConfig = mJWebSocketConfig.getFilter(lId);

			if (null == lFilterConfig) {
				throw new Exception("Has caused an error because the input parameter are wrong.");
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Filter '" + lFilterConfig.getName() + "' trying to load from file...");
			}

			JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
			String lJarFilePath = JWebSocketConfig.getLibsFolder(lFilterConfig.getJar());
			Class<WebSocketFilter> lFilterClass = null;

			//lJarFilePath may be null if .jar is included in server bundle
			if (lJarFilePath != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading filter '" + lFilterConfig.getName() + "' from '" + lJarFilePath + "'...");
				}
				lClassLoader.addFile(lJarFilePath);
				lFilterClass = (Class<WebSocketFilter>) lClassLoader.reloadClass(lFilterConfig.getName());
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
							mLog.debug("Filter '" + lFilterConfig.getId() + "' successfully instantiated.");
						}

						//Create reason of change for the jWebSocket Client 
						Token lReasonOfChange = new MapToken();
						((TokenFilter) lFilter).createReasonOfChange(lReasonOfChange, ChangeType.ADD, lVersion, lReason);

						// now add the filter to filter chain on server ids
						for (String lServerId : lFilterConfig.getServers()) {
							WebSocketServer lServerTemp = JWebSocketFactory.getServer(lServerId);
							if (null != lServerTemp) {
								if (lServerTemp.getFilterChain().reloadFilter(lFilter)) {
									//Send reason of change for the jWebSocket Client 
									((TokenServer) lServerTemp).broadcastToken(lReasonOfChange);
								} else {
									throw new Exception("Couldn't reload the filter on the server " + lServerId);
								}
							}
						}
					} else {
						throw new Exception("Couldn't instantiate the filter.");
					}
				} else {
					throw new Exception("Filter '" + lFilterConfig.getId() + "' could not be instantiated due to invalid constructor.");
				}
			} else {
				throw new ClassNotFoundException("Couldn't loading filter '" + lFilterConfig.getName() + "' from '" + lJarFilePath);
			}

			lResponse.setString("msg", "The filter has been reload.");
			traceLog(aConnector, "Reload Filter", "Successful", "The filter has been reload.");

		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getMessage());
			traceLog(aConnector, "Reload Filter", "Error", this.getClass().getSimpleName() + " on reloadFilter: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}
}
