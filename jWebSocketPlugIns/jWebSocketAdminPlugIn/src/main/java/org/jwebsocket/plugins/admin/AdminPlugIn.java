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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
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
 * @author aschulze
 */
public class AdminPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(AdminPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_ADMIN = JWebSocketServerConstants.NS_BASE + ".plugins.admin";
	private static boolean mExternalShutDownAllowed = false;

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

		mExternalShutDownAllowed = "true".equalsIgnoreCase(
				aConfiguration.getString("allowShutdown"));

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
}
