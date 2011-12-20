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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.instance.JWebSocketInstance;
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
 */
public class AdminPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(AdminPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_ADMIN = JWebSocketServerConstants.NS_BASE + ".plugins.admin";
	private static boolean mExternalShutDownAllowed = false;
	private static Integer mNumberOfDays = null;
	private static String mPathLog = null;
	private static String mPathLibs = null;

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

		mPathLog = JWebSocketConfig.getLogsFolder("AdminLog.log");

		mPathLibs = JWebSocketConfig.getLibsFolder("");

		// give a success message to the administrator
		if (mLog.isInfoEnabled()) {
			mLog.info("Admin plug-in successfully loaded.");
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
			} else if (lType.equals("readAdminLogs")) {
				getAdminLogs(aConnector, aToken);
			} else if (lType.equals("readPlugIns")) {
				getPlugInsConfig(aConnector, aToken);
			} else if (lType.equals("readFilters")) {
				getFiltersConfig(aConnector, aToken);
			} else if (lType.equals("readJars")) {
				getJars(aConnector, aToken);
			} else if (lType.equals("readPlugInsByJar")) {
				getPlugInsByJar(aConnector, aToken);
			} else if (lType.equals("readFiltersByJar")) {
				getFilterByJar(aConnector, aToken);
			} else if (lType.equals("changeOrderOfPlugInChain")) {
				changeOrderOfPlugInChain(aConnector, aToken);
			} else if (lType.equals("changeOrderOfFilterChain")) {
				changeOrderOfFilterChain(aConnector, aToken);
			}
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

	private void getAdminLogs(WebSocketConnector aConnector, Token aToken) {
		List<Token> lTokenLogs = new FastList();
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
						Token lTokenLog = new MapToken();
						lTokenLog.setString("date", lLogSplit[0].trim());
						lTokenLog.setString("time", lLogSplit[1].trim());
						lTokenLog.setString("action", lLogSplit[2].trim());
						lTokenLog.setString("result", lLogSplit[3].trim());
						lTokenLog.setString("desc", lLogSplit[4].trim());

						lTokenLogs.add(lTokenLog);
					}
				}
			}
			lResponse.setList("logs", lTokenLogs);
			lResponse.setInteger("totalCount", lTokenLogs.size());
			lBufferedReader.close();
			traceLog(aConnector, "Read Logs", "Successful", "Was obtained the logs of the last " + mNumberOfDays + " days.");

		} catch (FileNotFoundException ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getAdminLogs: " + ex.getMessage());
			lResponse.setList("logs", lTokenLogs);
			traceLog(aConnector, "Read Logs", "Successful", "Has been created a new log file for AdminPlugIn.");
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getAdminLogs: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Read Logs", "Error", ex.getMessage());
		}
		
		getServer().sendToken(aConnector, lResponse);
	}

	private void traceLog(WebSocketConnector aConnector, String aAction, String aResult, String aMessage) {
		mLog.fatal(" | " + aAction + " | " + aResult + " | " + aMessage);

		Token lResponse = new MapToken(NS_ADMIN, "traceLog");

		Token lTokenLog = new MapToken();
		lTokenLog.setString("date", DateHandler.getCurrentDate());
		lTokenLog.setString("time", DateHandler.getCurrentTime());
		lTokenLog.setString("action", aAction);
		lTokenLog.setString("result", aResult);
		lTokenLog.setString("desc", aMessage);

		lResponse.setToken("log", lTokenLog);
		getServer().sendToken(aConnector, lResponse);
	}

	private void getPlugInsConfig(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Token> lTokenPlugIn = new FastList();
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPlugInsConfig'...");
		}

		try {
			for (WebSocketPlugIn lPlugIn : lServer.getPlugInChain().getPlugIns()) {
				PluginConfig lConfig = (PluginConfig) lPlugIn.getPluginConfiguration();

				Token lTokenConfig = new MapToken();
				lTokenConfig.setString("id", lConfig.getId());
				lTokenConfig.setString("name", lConfig.getName());
				lTokenConfig.setString("namespace", lConfig.getNamespace());
				lTokenConfig.setString("jar", lConfig.getJar());
				lTokenConfig.setList("servers", lConfig.getServers());
				lTokenConfig.setBoolean("enabled", true);//TODO: Falta incluir el atributo enable

				lTokenPlugIn.add(lTokenConfig);
			}
			lResponse.setList("plugins", lTokenPlugIn);
			lResponse.setInteger("totalCount", lTokenPlugIn.size());
			traceLog(aConnector, "Read PlugIns", "Successful", "Was obtained the plugins configuration");

		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getPlugInsConfig: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Read PlugIns", "Error", ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getFiltersConfig(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Token> lTokenFilter = new FastList();
		Token lResponse = lServer.createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFiltersConfig'...");
		}

		try {
			for (WebSocketFilter lFilter : lServer.getFilterChain().getFilters()) {
				FilterConfig lConfig = (FilterConfig) lFilter.getFilterConfiguration();

				Token lTokenConfig = new MapToken();
				lTokenConfig.setString("id", lConfig.getId());
				lTokenConfig.setString("name", lConfig.getName());
				lTokenConfig.setString("namespace", lConfig.getNamespace());
				lTokenConfig.setString("jar", lConfig.getJar());
				lTokenConfig.setList("servers", lConfig.getServers());
				lTokenConfig.setBoolean("enabled", true);//TODO: Falta incluir el atributo enable

				lTokenFilter.add(lTokenConfig);
			}
			lResponse.setList("filters", lTokenFilter);
			lResponse.setInteger("totalCount", lTokenFilter.size());
			traceLog(aConnector, "Read Filters", "Successful", "Was obtained the filters configuration");

		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getFiltersConfig: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Read Filters", "Error", ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getJars(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Token> lJars = new FastList();
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
					Token lToken = new MapToken();
					lToken.setString("jar", lfileName);
					lJars.add(lToken);
				}
			}
			lResponse.setList("jars", lJars);
			lResponse.setInteger("totalCount", lJars.size());
			traceLog(aConnector, "Read Jar", "Successful", "Was obtained the java libraries.");
			
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getJars: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Read Jar", "Error", ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getPlugInsByJar(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Token> lIdPlugIn = new FastList();
		Token lResponse = lServer.createResponse(aToken);
		String lJar = aToken.getString("jar");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPlugInsByJar'...");
		}

		try {
			if (lJar == null || lJar.isEmpty()) {
				throw new Exception(this.getClass().getSimpleName() + " on getPlugInsByJar: " + "Has caused an error because the input parameter is null or empty.");
			}

			for (WebSocketPlugIn lPlugIn : lServer.getPlugInChain().getPlugIns()) {
				PluginConfig lConfig = (PluginConfig) lPlugIn.getPluginConfiguration();

				if (lConfig.getJar().equals(lJar)) {
					Token lToken = new MapToken();
					lToken.setString("idPlugIn", lConfig.getId());
					lIdPlugIn.add(lToken);
				}
			}
			lResponse.setList("plugInsByJar", lIdPlugIn);
			lResponse.setInteger("totalCount", lIdPlugIn.size());
			traceLog(aConnector, "Read PlugIns By Jar", "Successful", "Was obtained the plugins belonging to the library " + lJar);

		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getPlugInsByJar: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Read PlugIns By Jar", "Error", ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void getFilterByJar(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		List<Token> lIdFilter = new FastList();
		Token lResponse = lServer.createResponse(aToken);
		String lJar = aToken.getString("jar");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFilterByJar'...");
		}

		try {
			if (lJar == null || lJar.isEmpty()) {
				throw new Exception(this.getClass().getSimpleName() + " on getFilterByJar: " + "Has caused an error because the input parameter is null or empty.");
			}

			for (WebSocketFilter lFilter : lServer.getFilterChain().getFilters()) {
				FilterConfig lConfig = (FilterConfig) lFilter.getFilterConfiguration();

				if (lConfig.getJar().equals(lJar)) {
					Token lToken = new MapToken();
					lToken.setString("idFilter", lConfig.getId());
					lIdFilter.add(lToken);
				}
			}
			lResponse.setList("filtersByJar", lIdFilter);
			lResponse.setInteger("totalCount", lIdFilter.size());
			traceLog(aConnector, "Read Filters By Jar", "Successful", "Was obtained the filters belonging to the library " + lJar);

		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on getFilterByJar: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Read Filters By Jar", "Error", ex.getMessage());
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
				throw new Exception(this.getClass().getSimpleName() + " on changeOrderOfPlugInChain: " + "Has caused an error because the input parameter is null or empty.");
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
				throw new Exception(this.getClass().getSimpleName() + " on changeOrderOfPlugInChain: " + "Has caused an error because the input parameter are wrong.");
			}

			if ((0 == lPosition && -1 == lStepsMove) || (lPlugIns.size() - 1 == lPosition && 1 == lStepsMove)) {
				throw new Exception(this.getClass().getSimpleName() + " on changeOrderOfPlugInChain: " + "This action can not be made, is invalid.");
			}

			synchronized (lServer.getPlugInChain()) {
				WebSocketPlugIn aPlugIn = lPlugIns.get(lPosition);
				lPlugIns.set(lPosition, lPlugIns.get(lPosition + lStepsMove));
				lPlugIns.set(lPosition + lStepsMove, aPlugIn);
			}
			lResponse.setInteger("code", 0);
			traceLog(aConnector, "Change Order of PlugIns", "Successful", "Changed order of the Plugin chain.");

		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on changeOrderOfPlugInChain: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Change Order of PlugIns", "Error", ex.getMessage());
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
				throw new Exception(this.getClass().getSimpleName() + " on changeOrderOfFilterChain: " + "Has caused an error because the input parameter is null or empty.");
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
				throw new Exception(this.getClass().getSimpleName() + " on changeOrderOfFilterChain: " + "Has caused an error because the input parameter are wrong.");
			}

			if ((0 == lPosition && -1 == lStepsMove) || (lFilters.size() - 1 == lPosition && 1 == lStepsMove)) {
				throw new Exception(this.getClass().getSimpleName() + " on changeOrderOfFilterChain: " + "This action can not be made, is invalid.");
			}

			synchronized (lServer.getFilterChain()) {
				WebSocketFilter lFilter = lFilters.get(lPosition);
				lFilters.set(lPosition, lFilters.get(lPosition + lStepsMove));
				lFilters.set(lPosition + lStepsMove, lFilter);
			}

			lResponse.setInteger("code", 0);
			traceLog(aConnector, "Change Order of Filters", "Successful", "Changed order of the Filter chain.");

		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + " on changeOrderOfFilterChain: " + ex.getMessage());
			lResponse.setInteger("code", -1);
			traceLog(aConnector, "Change Order of PlugIns", "Error", ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}

	private void enablePlugIn(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void enableFilter(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void disablePlugIn(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void disableFilter(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void addPlugIn(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void addFilter(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void removePlugIn(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void removeFilter(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void reloadPlugIn(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}

	private void reloadFilter(WebSocketConnector aConnector, Token aToken) {
		//TODO: By implement
	}
}
