//	---------------------------------------------------------------------------
//	jWebSocket Administration Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.admin;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.security.User;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 * @author Alexander Schulze
 * @author Marcos Antonio Gonzalez Huerta
 */
public class AdminPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private static final String NS_ADMIN = JWebSocketServerConstants.NS_BASE + ".plugins.admin";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket AdminPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket AdminPlugIn - Community Edition";
	private static boolean mExternalShutDownAllowed = false;
	private static final int DEF_NUMBER_OF_DAYS = 30;
	private static Integer mNumberOfDays = null;
	private static AdminPlugInService mService = null;

	/**
	 * Constructor that takes configuration object
	 *
	 * @param aConfiguration
	 */
	public AdminPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating admin plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_ADMIN);

		mExternalShutDownAllowed = "true".equalsIgnoreCase(aConfiguration.getString("allowShutdown"));

		String lNumberOfDays = aConfiguration.getString("numberOfDays");
		try {
			mNumberOfDays = Integer.parseInt(lNumberOfDays);
		} catch (NumberFormatException lEx) {
			mLog.warn("Number of day value invalid or not defined in config file, defaulted to " + DEF_NUMBER_OF_DAYS + ".");
			mNumberOfDays = DEF_NUMBER_OF_DAYS;
		}

		// give a success message to the administrator
		if (mLog.isDebugEnabled()) {
			mLog.debug("Admin plug-in successfully loaded.");
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_ADMIN;
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null) {
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

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();

		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (lType.equals("getAdminLogs")) {
			return mService.getAdminLogs(aConnector, aToken);
		} else if (lType.equals("getPlugInsConfig")) {
			return mService.getPlugInsConfig(aConnector, aToken);
		} else if (lType.equals("getFiltersConfig")) {
			return mService.getFiltersConfig(aConnector, aToken);
		} else if (lType.equals("getPlugInConfigById")) {
			return mService.getPlugInConfigById(aConnector, aToken);
		} else if (lType.equals("getFilterConfigById")) {
			return mService.getFilterConfigById(aConnector, aToken);
		} else if (lType.equals("changeOrderOfPlugInChain")) {
			return mService.changeOrderOfPlugInChain(aConnector, aToken);
		} else if (lType.equals("changeOrderOfFilterChain")) {
			return mService.changeOrderOfFilterChain(aConnector, aToken);
		} else if (lType.equals("enablePlugIn")) {
			enablePlugIn(aConnector, aToken);
		} else if (lType.equals("enableFilter")) {
			return mService.enableFilter(aConnector, aToken);
		} else if (lType.equals("disablePlugIn")) {
			return mService.disablePlugIn(aConnector, aToken);
		} else if (lType.equals("disableFilter")) {
			return mService.disableFilter(aConnector, aToken);
		} else if (lType.equals("removePlugIn")) {
			return mService.removePlugIn(aConnector, aToken);
		} else if (lType.equals("removeFilter")) {
			return mService.removeFilter(aConnector, aToken);
		} else if (lType.equals("addPlugIn")) {
			return mService.addPlugIn(aConnector, aToken);
		} else if (lType.equals("addFilter")) {
			return mService.addFilter(aConnector, aToken);
		} else if (lType.equals("reloadPlugIn")) {
			return mService.reloadPlugIn(aConnector, aToken);
		} else if (lType.equals("reloadFilter")) {
			return mService.reloadFilter(aConnector, aToken);
		}

		return null;
	}

	@Override
	public List<String> invokeMethodList() {
		FastList<String> lMethodList = new FastList<String>();
		lMethodList.add("getAdminLogs");
		lMethodList.add("getPlugInsConfig");
		lMethodList.add("getFiltersConfig");
		lMethodList.add("getPlugInConfigById");
		lMethodList.add("getFilterConfigById");
		lMethodList.add("changeOrderOfPlugInChain");
		lMethodList.add("changeOrderOfFilterChain");
		lMethodList.add("enablePlugIn");
		lMethodList.add("enableFilter");
		lMethodList.add("disablePlugIn");
		lMethodList.add("disableFilter");
		lMethodList.add("removePlugIn");
		lMethodList.add("removeFilter");
		lMethodList.add("addPlugIn");
		lMethodList.add("addFilter");
		lMethodList.add("reloadPlugIn");
		lMethodList.add("reloadFilter");

		return lMethodList;
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
		if (!hasAuthority(aConnector, NS_ADMIN + ".shutdown")) {
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

		// check if user is allowed to run 'gc' command
		// should be limited to administrators
		if (!hasAuthority(aConnector, NS_ADMIN + ".gc")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		System.gc();

		lServer.sendToken(aConnector, createResponse(aToken));
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
		if (!hasAuthority(aConnector, NS_ADMIN + ".getConnections")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		Token lResponse = lServer.createResponse(aToken);
		try {
			List<Map> lResultList = new FastList<Map>();
			Iterator<WebSocketConnector> lAll = lServer.getAllConnectorsIterator();
			while (lAll.hasNext()) {
				WebSocketConnector lConnector = lAll.next();

				Map lResultItem = new FastMap<String, Object>();
				lResultItem.put("host", lConnector.getRemoteHost());
				lResultItem.put("port", lConnector.getRemotePort());
				// Caution! This method may only be granted to administrators!
				lResultItem.put("usid", lConnector.getSession().getSessionId());
				lResultItem.put("unid", lConnector.getNodeId());
				lResultItem.put("username", lConnector.getUsername());
				lResultItem.put("isToken", lConnector.supportTokens());
				lResultItem.put("isCluster", !lConnector.isLocal());
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
		if (!hasAuthority(aConnector, NS_ADMIN + ".getUserRights")) {
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
		if (!hasAuthority(aConnector, NS_ADMIN + ".getUserRoles")) {
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

	/**
	 * return the last logs registered.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getAdminLogs(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getAdminLogs'...");
		}

		getServer().sendToken(aConnector, mService.getAdminLogs(aConnector, aToken));
	}

	/**
	 * return all plugins config of the PluginChain
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getPlugInsConfig(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPlugInsConfig'...");
		}

		getServer().sendToken(aConnector, mService.getPlugInsConfig(aConnector, aToken));
	}

	/**
	 * return all filters config of the FilterChain
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getFiltersConfig(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFiltersConfig'...");
		}

		getServer().sendToken(aConnector, mService.getFiltersConfig(aConnector, aToken));
	}

	/**
	 * return all name of jars located in the libs folder
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getJars(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getJars'...");
		}

		getServer().sendToken(aConnector, mService.getJars(aConnector, aToken));
	}

	/**
	 * return all ids of plugins container in the jar
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getPlugInsByJar(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPlugInsByJar'...");
		}

		getServer().sendToken(aConnector, mService.getPlugInsByJar(aConnector, aToken));
	}

	/**
	 * return all ids of filters container in the jar
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getFilterByJar(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFilterByJar'...");
		}

		getServer().sendToken(aConnector, mService.getFilterByJar(aConnector, aToken));
	}

	/**
	 * return the plugin config by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getPlugInConfigById(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getPlugInConfigById'...");
		}

		getServer().sendToken(aConnector, mService.getPlugInConfigById(aConnector, aToken));
	}

	/**
	 * return the filter config by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getFilterConfigById(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getFilterConfigById'...");
		}

		getServer().sendToken(aConnector, mService.getFilterConfigById(aConnector, aToken));
	}

	/**
	 * Change the order of plugin in the PluginChain. (Up or Down <N> step)
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void changeOrderOfPlugInChain(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'changeOrderOfPlugInChain'...");
		}

		getServer().sendToken(aConnector, mService.changeOrderOfPlugInChain(aConnector, aToken));
	}

	/**
	 * Change the order of filter in the FilterChain. (Up or Down <N> step)
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void changeOrderOfFilterChain(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'changeOrderOfFilterChain'...");
		}

		getServer().sendToken(aConnector, mService.changeOrderOfFilterChain(aConnector, aToken));
	}

	/**
	 * Enable a plugin in the PluginChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void enablePlugIn(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'enablePlugIn'...");
		}

		getServer().sendToken(aConnector, mService.enablePlugIn(aConnector, aToken));
	}

	/**
	 * Enable a filter in the FilterChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void enableFilter(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'enableFilter'...");
		}

		getServer().sendToken(aConnector, mService.enableFilter(aConnector, aToken));
	}

	/**
	 * Disable a plugin in the PluginChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void disablePlugIn(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'disablePlugIn'...");
		}

		getServer().sendToken(aConnector, mService.disablePlugIn(aConnector, aToken));
	}

	/**
	 * Disable a filter in the FilterChain by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void disableFilter(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'disableFilter'...");
		}

		getServer().sendToken(aConnector, mService.disableFilter(aConnector, aToken));
	}

	/**
	 * Add a new plugin in the PluginChain
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void addPlugIn(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'addPlugIn'...");
		}

		getServer().sendToken(aConnector, mService.addPlugIn(aConnector, aToken));
	}

	/**
	 * Add a new filter in the FilterChain
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void addFilter(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'addFilter'...");
		}

		getServer().sendToken(aConnector, mService.addFilter(aConnector, aToken));
	}

	/**
	 * Remove a plugin by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void removePlugIn(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'removePlugIn'...");
		}

		getServer().sendToken(aConnector, mService.removePlugIn(aConnector, aToken));
	}

	/**
	 * Remove a filter by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void removeFilter(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'removeFilter'...");
		}

		getServer().sendToken(aConnector, mService.removeFilter(aConnector, aToken));
	}

	/**
	 * Reload a plugin by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void reloadPlugIn(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'reloadPlugIn'...");
		}

		getServer().sendToken(aConnector, mService.reloadPlugIn(aConnector, aToken));
	}

	/**
	 * Reload a filter by id
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void reloadFilter(WebSocketConnector aConnector, Token aToken) {
		if (mService == null) {
			mService = new AdminPlugInService(NS_ADMIN, mNumberOfDays, getServer());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'reloadFilter'...");
		}

		getServer().sendToken(aConnector, mService.reloadFilter(aConnector, aToken));
	}
}
