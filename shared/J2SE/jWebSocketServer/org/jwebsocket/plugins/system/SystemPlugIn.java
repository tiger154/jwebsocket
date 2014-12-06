//	---------------------------------------------------------------------------
//	jWebSocket System Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.system;

import java.util.*;
import java.util.Map.Entry;
import javax.jms.MapMessage;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.ISessionManager;
import org.jwebsocket.api.IUserUniqueIdentifierContainer;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketPlugInChain;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.connectors.InternalConnector;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.filters.system.SystemFilter;
import org.jwebsocket.jms.JMSEngine;
import org.jwebsocket.jms.JMSServer;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.TokenPlugInChain;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.session.SessionManager;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.JMSManager;
import org.jwebsocket.util.MessagingControl;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

/**
 * Implements the jWebSocket system core features like login, logout, send,
 * broadcast etc...
 *
 * @author Alexander Schulze
 * @author kybernees
 */
public class SystemPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	// specify name space for system plug-in
	/**
	 *
	 */
	public static final String NS_SYSTEM = JWebSocketServerConstants.NS_BASE + ".plugins.system";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SystemPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket SystemPlugIn - Community Edition";
	// specify token types processed by system plug-in
	private static final String TT_SEND = "send";
	private static final String TT_RESPOND = "respond";
	private static final String TT_BROADCAST = "broadcast";
	private static final String TT_BROADCAST_TO_SHARED_SESSION = "broadcastToSharedSession";
	private static final String TT_WELCOME = "welcome";
	private static final String TT_GOODBYE = "goodBye";
	private static final String TT_HEADER = "header";
	// old future deprecated
	private static final String TT_LOGIN = "login";
	private static final String TT_LOGOUT = "logout";
	// new spring based auth
	/**
	 *
	 */
	public static final String TT_LOGON = "logon";
	private static final String TT_LOGOFF = "logoff";
	private static final String TT_GET_AUTHORITIES = "getAuthorities";
	private static final String TT_CLOSE = "close";
	private static final String TT_GETCLIENTS = "getClients";
	private static final String TT_PING = "ping";
	private static final String TT_ECHO = "echo";
	private static final String TT_WAIT = "wait";
	private static final String TT_ALLOC_CHANNEL = "alloc";
	private static final String TT_DEALLOC_CHANNEL = "dealloc";
	// session CRUD operations
	private static final String TT_SESSION_GET = "sessionGet";
	private static final String TT_SESSION_PUT = "sessionPut";
	private static final String TT_SESSION_HAS = "sessionHas";
	private static final String TT_SESSION_REMOVE = "sessionRemove";
	private static final String TT_SESSION_KEYS = "sessionKeys";
	private static final String TT_SESSION_GETALL = "sessionGetAll";
	private static final String TT_SESSION_GETMANY = "sessionGetMany";
	// session key subfix for public data storage
	// other clients can read public connector's session data
	/**
	 *
	 */
	public static final String SESSION_PUBLIC_KEY_SUBFIX = "public::";
	// specify shared connector variables
	private static final String VAR_GROUP = NS_SYSTEM + ".group";
	private static boolean BROADCAST_OPEN = true;
	private static final String BROADCAST_OPEN_KEY = "broadcastOpenEvent";
	private static boolean BROADCAST_CLOSE = true;
	private static final String BROADCAST_CLOSE_KEY = "broadcastCloseEvent";
	private static boolean BROADCAST_LOGIN = true;
	private static final String BROADCAST_LOGIN_KEY = "broadcastLoginEvent";
	private static boolean BROADCAST_LOGOUT = true;
	private static final String BROADCAST_LOGOUT_KEY = "broadcastLogoutEvent";
	private static final String ALLOW_ANONYMOUS_KEY = "allowAnonymousLogin";
	public static final String ANONYMOUS_USER = "anonymous";
	private static boolean ALLOW_ANONYMOUS_LOGIN = false;
	private static final String ALLOW_AUTO_ANONYMOUS_KEY = "allowAutoAnonymous";
	private static boolean ALLOW_AUTO_ANONYMOUS = false;
	private ProviderManager mAuthProvMgr;
	private ISessionManager mSessionManager;
	/**
	 * Spring authentication session indexes
	 */
	public static final String USERNAME = BaseConnector.VAR_USERNAME;
	/**
	 *
	 */
	public static final String AUTHORITIES = "$authorities";
	/**
	 *
	 */
	public static final String UUID = "$uuid";
	/**
	 *
	 */
	public static final String IS_AUTHENTICATED = WebSocketSession.IS_AUTHENTICATED;
	/**
	 * jWebSocket core spring beans identifiers
	 */
	public static final String BEAN_AUTHENTICATION_MANAGER = "authManager";
	/**
	 *
	 */
	public static final String BEAN_SESSION_MANAGER = "sessionManager";
	/**
	 * Core Spring application context
	 */
	private static ApplicationContext mBeanFactory;

	/**
	 * Constructor with configuration object
	 *
	 * @param aConfiguration
	 */
	public SystemPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating system plug-in...");
		}
		// specify default name space for system plugin
		this.setNamespace(NS_SYSTEM);
		settings();

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for system plug-in, some features may not be available.");
			} else {
				mAuthProvMgr = (ProviderManager) mBeanFactory.getBean(BEAN_AUTHENTICATION_MANAGER);
				// sessionManager bean is not used in embedded mode and should not
				// be declared in this case
				if (mBeanFactory.containsBean(BEAN_SESSION_MANAGER)) {
					mSessionManager = (SessionManager) mBeanFactory.getBean(BEAN_SESSION_MANAGER);
				}

				// give a success message to the administrator
				if (mLog.isInfoEnabled()) {
					mLog.info("System plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating system plug-in"));
		}
	}

	/**
	 *
	 * @return
	 */
	public ProviderManager getAuthProvMgr() {
		return mAuthProvMgr;
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
		return NS_SYSTEM;
	}

	private void settings() {
		// load global settings, default to "true"
		BROADCAST_OPEN = "true".equals(getString(BROADCAST_OPEN_KEY, "true"));
		BROADCAST_CLOSE = "true".equals(getString(BROADCAST_CLOSE_KEY, "true"));
		BROADCAST_LOGIN = "true".equals(getString(BROADCAST_LOGIN_KEY, "true"));
		BROADCAST_LOGOUT = "true".equals(getString(BROADCAST_LOGOUT_KEY, "true"));
		ALLOW_ANONYMOUS_LOGIN = "true".equals(getString(ALLOW_ANONYMOUS_KEY, "false"));
		ALLOW_AUTO_ANONYMOUS = "true".equals(getString(ALLOW_AUTO_ANONYMOUS_KEY, "false"));
		SecurityFactory.setAutoAnonymous(ALLOW_AUTO_ANONYMOUS);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();

		if (lType != null) {
			if (lType.equals(TT_SEND)) {
				send(aConnector, aToken);
			} else if (lType.equals(TT_RESPOND)) {
				respond(aConnector, aToken);
			} else if (lType.equals(TT_HEADER)) {
				getHeaders(aConnector, aToken);
			} else if (lType.equals(TT_BROADCAST)) {
				broadcast(aConnector, aToken);
			} else if (lType.equals(TT_BROADCAST_TO_SHARED_SESSION)) {
				broadcastToSharedSession(aConnector, aToken);
			} else if (lType.equals(TT_LOGIN)) {
				logon(aConnector, aToken);
			} else if (lType.equals(TT_LOGOUT)) {
				logoff(aConnector, aToken);
			} else if (lType.equals(TT_LOGON)) {
				logon(aConnector, aToken);
			} else if (lType.equals(TT_LOGOFF)) {
				logoff(aConnector, aToken);
			} else if (lType.equals(TT_GET_AUTHORITIES)) {
				getAuthorities(aConnector, aToken);
			} else if (lType.equals(TT_CLOSE)) {
				close(aConnector, aToken);
			} else if (lType.equals(TT_GETCLIENTS)) {
				getClients(aConnector, aToken);
			} else if (lType.equals(TT_PING)) {
				ping(aConnector, aToken);
			} else if (lType.equals(TT_ECHO)) {
				echo(aConnector, aToken);
			} else if (lType.equals(TT_WAIT)) {
				wait(aConnector, aToken);
			} else if (lType.equals(TT_ALLOC_CHANNEL)) {
				allocChannel(aConnector, aToken);
			} else if (lType.equals(TT_DEALLOC_CHANNEL)) {
				deallocChannel(aConnector, aToken);
			} else if (lType.equals(TT_SESSION_GET)) {
				sessionGet(aConnector, aToken);
			} else if (lType.equals(TT_SESSION_GETALL)) {
				sessionGetAll(aConnector, aToken);
			} else if (lType.equals(TT_SESSION_GETMANY)) {
				sessionGetMany(aConnector, aToken);
			} else if (lType.equals(TT_SESSION_HAS)) {
				sessionHas(aConnector, aToken);
			} else if (lType.equals(TT_SESSION_KEYS)) {
				sessionKeys(aConnector, aToken);
			} else if (lType.equals(TT_SESSION_PUT)) {
				sessionPut(aConnector, aToken);
			} else if (lType.equals(TT_SESSION_REMOVE)) {
				sessionRemove(aConnector, aToken);
			}
			aResponse.abortChain();
		}
	}

	@Override
	public void engineStarted(final WebSocketEngine aEngine) {
		aEngine.setSystemStoppingNotificationStrategy(new Runnable() {
			@Override
			public void run() {
				if (aEngine instanceof JMSEngine) {
					// don't notify if the jWebSocket server node is part of an active cluster
					if (((JMSEngine) aEngine).getNodesManager().count() > 1) {
						return;
					}
				}

				Token lToken = TokenFactory.createToken(NS_SYSTEM, "event");
				lToken.setString("name", "systemStopping");

				Iterator<WebSocketConnector> lIt = aEngine.getConnectorsIterator();
				while (lIt.hasNext()) {
					WebSocketConnector aConnector = lIt.next();
					if (aConnector.supportTokens()) {
						sendToken(aConnector, lToken);
					}
				}
			}
		});
	}

	/**
	 *
	 * @param aConnector
	 * @param aSession
	 */
	public static void startSession(WebSocketConnector aConnector, WebSocketSession aSession) {
		try {
			Iterator<WebSocketServer> lServers = JWebSocketFactory.getServers().iterator();
			while (lServers.hasNext()) {
				lServers.next().sessionStarted(aConnector, aSession);
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying session started event"), lEx);
		}
	}

	/**
	 *
	 * @param aSession
	 */
	public static void stopSession(WebSocketSession aSession) {
		try {
			Iterator<WebSocketServer> lServers = JWebSocketFactory.getServers().iterator();
			while (lServers.hasNext()) {
				lServers.next().sessionStopped(aSession);
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying session stopped event"), lEx);
		}
	}

	/**
	 *
	 * @return
	 */
	public ISessionManager getSessionManager() {
		return mSessionManager;
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// setting connector encodingFormats container
		aConnector.setVar(JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY, "");

		// Setting the session only if a session manager is defined,
		// ommitting if the session storage was previously setted (embedded mode)
		if (null != mSessionManager) {
			try {
				mSessionManager.connectorStarted(aConnector);
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "initializing connector session"), lEx);
			}
		}

		if (ALLOW_ANONYMOUS_LOGIN && null == aConnector.getUsername()) {
			setUsername(aConnector, ANONYMOUS_USER);
		}

		// initializing connector UUID here to avoid concurrency issues
		aConnector.getSession().getUUID();

		// sending the welcome token
		sendWelcome(aConnector);

		// notify session started
		WebSocketSession lSession = aConnector.getSession();
		if (null != lSession.getStorage() && null == lSession.getCreatedAt()) {
			lSession.setCreatedAt();
			startSession(aConnector, aConnector.getSession());

			// notifying event through the MessageHub
			notifySessionStarted(aConnector);
		}

		// if new connector is active broadcast this event to then network
		if (false == aConnector instanceof InternalConnector) {
			broadcastConnectEvent(aConnector);
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// allowing all connectors for a reconnection
		if (mSessionManager != null) {
			try {
				boolean lSessionShared = !getServer().getSharedSessionConnectors(
						aConnector.getSession().getSessionId()).isEmpty();
				mSessionManager.connectorStopped(aConnector, aCloseReason, lSessionShared);
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping connector session"), lEx);
			}
		}

		// notify other clients that client disconnected
		if (false == aConnector instanceof InternalConnector) {
			broadcastDisconnectEvent(aConnector);
		}
	}

	private void broadcastEvent(WebSocketConnector aConnector, Token aEvent) {
		Iterator<WebSocketConnector> lTargetConnectors = getServer().getAllConnectorsIterator();
		while (lTargetConnectors.hasNext()) {
			WebSocketConnector lConnector = lTargetConnectors.next();
			if (lConnector.getId().equals(aConnector.getId())) {
				// excluding sender connector
				continue;
			}

			// checking per user events notification configuration
			String lExcludedEvents = (String) getConfigParam(lConnector, "events.exclude", "");
			if (lExcludedEvents.contains("," + aEvent.getString("name"))) {
				continue;
			}

			sendToken(lConnector, aEvent);
		}
	}

	/**
	 *
	 *
	 * @param aConnector
	 */
	public void broadcastConnectEvent(WebSocketConnector aConnector) {
		// only broadcast if corresponding global plugin setting is "true"sendChu
		if (BROADCAST_OPEN) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting connect...");
			}
			// broadcast connect event to other clients of the jWebSocket network
			Token lEvent = TokenFactory.createToken(NS_SYSTEM, BaseToken.TT_EVENT);
			lEvent.setString("name", "connect");
			lEvent.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lEvent.setString("unid", lNodeId);
			}
			if (false == getServer() instanceof JMSServer) {
				// exclude if running in a cluster, since the connectors data 
				// stored in database
				lEvent.setLong("clientCount", getConnectorsCount());
			}

			// broadcast to all except source
			broadcastEvent(aConnector, lEvent);
		}

		// notifying event through the MessageHub
		notifyConnectorStarted(aConnector);
	}

	/**
	 *
	 *
	 * @param aConnector
	 */
	public void broadcastDisconnectEvent(WebSocketConnector aConnector) {
		// only broadcast if corresponding global plugin setting is "true"
		if (BROADCAST_CLOSE
				&& !aConnector.getBool("noDisconnectBroadcast")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting disconnect...");
			}
			// broadcast connect event to other clients of the jWebSocket network
			Token lEvent = TokenFactory.createToken(NS_SYSTEM, BaseToken.TT_EVENT);
			lEvent.setString("name", "disconnect");
			lEvent.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lEvent.setString("unid", lNodeId);
			}
			if (false == getServer() instanceof JMSServer) {
				// exclude if running in a cluster, since the connectors data 
				// stored in database
				lEvent.setLong("clientCount", getConnectorsCount());
			}

			// broadcast to all except source
			broadcastEvent(aConnector, lEvent);
		}

		// notifying event through the MessageHub
		notifyConnectorStopped(aConnector);
	}

	private void sendWelcome(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending welcome...");
		}
		// send "welcome" token to client
		Token lWelcome = TokenFactory.createToken(NS_SYSTEM, TT_WELCOME);
		lWelcome.setString("vendor", JWebSocketCommonConstants.VENDOR_CE);
		lWelcome.setString("version", JWebSocketServerConstants.VERSION_STR);

		Map<String, WebSocketEngine> lEngineMap = getServer().getEngines();
		List<Map<String, String>> lEngines = new FastList<Map<String, String>>();
		for (Map.Entry<String, WebSocketEngine> lEntry : lEngineMap.entrySet()) {
			Map lEngineItem = new FastMap<String, String>();
			lEngineItem.put("id", lEntry.getValue().getId());
			lEngineItem.put("class", lEntry.getValue().getClass().getName());
			lEngines.add(lEngineItem);
		}
		lWelcome.setList("engines", lEngines);

		lWelcome.setString("sourceId", aConnector.getId());
		lWelcome.setInteger(MessagingControl.PROPERTY_MAX_FRAME_SIZE, aConnector.getMaxFrameSize());
		// if a unique node id is specified for the client include that
		String lNodeId = aConnector.getNodeId();
		if (lNodeId != null) {
			lWelcome.setString("unid", lNodeId);
		}
		lWelcome.setInteger("timeout", aConnector.getEngine().getConfiguration().getTimeout());
		String lUsername = aConnector.getUsername();
		if (lUsername != null) {
			lWelcome.setString("username", lUsername);
		}
		if (lNodeId != null) {
			lWelcome.setString("unid", lNodeId);
		}
		// to let the client know about the negotiated protocol
		lWelcome.setInteger("protocolVersion", aConnector.getVersion());
		// and negotiated sub protocol
		// TODO: The client does not get anything here!
		lWelcome.setString("subProtocol", aConnector.getSubprot());

		// sending to the client supported encoding formats
		lWelcome.setList(JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY, SystemFilter.getSupportedEncodings());

		// if anoymous user allowed send corresponding flag for 
		// clarification that auto anonymous may have been applied.
		if (ALLOW_ANONYMOUS_LOGIN && ALLOW_AUTO_ANONYMOUS) {
			lWelcome.setBoolean(
					"anonymous",
					null != ANONYMOUS_USER
					&& ANONYMOUS_USER.equals(lUsername));
		}
		sendToken(aConnector, aConnector, lWelcome);
	}

	/**
	 *
	 */
	private void broadcastLoginEvent(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying 'logon' operation to plug-ins...");
		}
		for (WebSocketServer lServer : JWebSocketFactory.getServers()) {
			if (lServer instanceof TokenServer) {
				WebSocketPlugInChain lChain = lServer.getPlugInChain();
				((TokenPlugInChain) lChain).processLogon(aConnector);
			}
		}
		// only broadcast if corresponding global plugin setting is "true"
		if (BROADCAST_LOGIN) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting login event...");
			}
			// broadcast login event to other clients of the jWebSocket network
			Token lEvent = TokenFactory.createToken(NS_SYSTEM, BaseToken.TT_EVENT);
			lEvent.setString("name", "login");
			lEvent.setString("username", getUsername(aConnector));
			if (false == getServer() instanceof JMSServer) {
				// exclude if running in a cluster, since the connectors data 
				// stored in database
				lEvent.setLong("clientCount", getConnectorsCount());
			}
			lEvent.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lEvent.setString("unid", lNodeId);
			}
			// broadcast to all except source
			broadcastEvent(aConnector, lEvent);
		}

		// notifying event through the MessageHub
		notifyLogon(aConnector);
	}

	/**
	 *
	 */
	private void broadcastLogoutEvent(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying 'logoff' operation to plug-ins...");
		}
		for (WebSocketServer lServer : JWebSocketFactory.getServers()) {
			if (lServer instanceof TokenServer) {
				WebSocketPlugInChain lChain = lServer.getPlugInChain();
				((TokenPlugInChain) lChain).processLogoff(aConnector);
			}
		}

		// only broadcast if corresponding global plugin setting is "true"
		if (BROADCAST_LOGOUT) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting logout event...");
			}
			// broadcast login event to other clients of the jWebSocket network
			Token lEvent = TokenFactory.createToken(NS_SYSTEM, BaseToken.TT_EVENT);
			lEvent.setString("name", "logout");
			lEvent.setString("username", getUsername(aConnector));
			if (false == getServer() instanceof JMSServer) {
				// exclude if running in a cluster, since the connectors data 
				// stored in database
				lEvent.setLong("clientCount", getConnectorsCount());
			}
			lEvent.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lEvent.setString("unid", lNodeId);
			}
			// broadcast to all except source
			broadcastEvent(aConnector, lEvent);
		}

		// notifying event through the MessageHub
		notifyLogoff(aConnector);
	}

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	private void sendGoodBye(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending good bye...");
		}
		// send "goodBye" token to client
		Token lGoodBye = TokenFactory.createToken(TT_GOODBYE);
		lGoodBye.setString("ns", getNamespace());
		lGoodBye.setString("vendor", JWebSocketCommonConstants.VENDOR_CE);
		lGoodBye.setString("version", JWebSocketServerConstants.VERSION_STR);
		lGoodBye.setString("sourceId", aConnector.getId());
		if (aCloseReason != null) {
			lGoodBye.setString("reason", aCloseReason.toString().toLowerCase());
		}

		// don't send session-id on good bye, neither required nor desired
		sendToken(aConnector, aConnector, lGoodBye);
	}

	private void send(WebSocketConnector aConnector, Token aToken) {
		String lAction = aToken.getString("action");

		if (!"forward.json".equals(lAction)) {
			// check if user is allowed to run 'send' command
			if (!hasAuthority(aConnector, NS_SYSTEM + ".send")) {
				sendToken(aConnector, aConnector, createAccessDenied(aToken));
				return;
			}
		}

		Token lResponse = createResponse(aToken);

		WebSocketConnector lTargetConnector;
		String lTargetId = aToken.getString("unid");
		Boolean lIsResponseRequested = aToken.getBoolean("responseRequested", true);
		String lTargetType;
		if (lTargetId != null) {
			lTargetConnector = getNode(lTargetId);
			lTargetType = "node-id";
		} else {
			// get the target
			lTargetId = aToken.getString("targetId");
			lTargetConnector = getConnector(lTargetId);
			lTargetType = "endpoint-id";
		}

		/*
		 * if (getUsername(aConnector) != null) {
		 */
		if (lTargetConnector != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing 'send'"
						+ (null == lAction ? "" : ", action='" + lAction + "'")
						+ " (username='"
						+ getUsername(aConnector)
						+ "') from '" + aConnector
						+ "' to " + lTargetId + "...");
			}
			if ("forward.json".equals(lAction)) {
				if (null == getServer().getConnector(lTargetId)) {
					String lMsg = "No target connector with endpoint-id'"
							+ lTargetId + "' found.";
					mLog.warn(lMsg);
					lResponse.setInteger("code", -1);
					lResponse.setString("msg", lMsg);
					sendToken(aConnector, aConnector, lResponse);
					return;
				}
				WebSocketPacket lPacket = new RawPacket(aToken.getString("data"));
				Token lToken = JSONProcessor.packetToToken(lPacket);
				String lGatewayId = lTargetConnector.getString("$gatewayId");
				if (null != lGatewayId) {
					lToken.setString("gatewayId", lGatewayId);
				}
				sendToken(aConnector, lTargetConnector, lToken);
			} else {
				aToken.setString("sourceId", aConnector.getId());
				sendToken(aConnector, lTargetConnector, aToken);
			}
			// if a response is requested, not explicitely suppressed, send it
			if (lIsResponseRequested) {
				aToken.remove("responseRequested");
				sendToken(aConnector, aConnector, lResponse);
			}
		} else {
			// respond with error message (target connector not found)
			String lMsg = "No target connector with "
					+ lTargetType + " '"
					+ lTargetId + "' found.";
			mLog.warn(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	private void respond(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		WebSocketConnector lTargetConnector;
		String lTargetId = aToken.getString("unid");
		String lTargetType;
		if (lTargetId != null) {
			lTargetConnector = getNode(lTargetId);
			lTargetType = "node-id";
		} else {
			// get the target
			lTargetId = aToken.getString("targetId");
			lTargetConnector = getConnector(lTargetId);
			lTargetType = "endpoint-id";
		}

		if (lTargetConnector != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing 'respond' (username='"
						+ getUsername(aConnector)
						+ "') from '" + aConnector
						+ "' to " + lTargetId + "...");
			}
			aToken.setType("response");
			aToken.setString("sourceId", aConnector.getId());
			sendToken(aConnector, lTargetConnector, aToken);
		} else {
			String lMsg = "No target connector with "
					+ lTargetType + " '"
					+ lTargetId + "' found.";
			mLog.warn(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	private void broadcast(WebSocketConnector aConnector, Token aToken) {

		// check if user is allowed to run 'broadcast' command
		if (!hasAuthority(aConnector, NS_SYSTEM + ".broadcast")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		Token lResponse = createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'broadcast' (username='"
					+ getUsername(aConnector)
					+ "') from '" + aConnector + "'...");
		}
		/*
		 * if (getUsername(aConnector) != null) {
		 */
		aToken.setString("sourceId", aConnector.getId());
		// keep senderIncluded beging false as default, apps rely on this!
		Boolean lIsSenderIncluded = aToken.getBoolean("senderIncluded", false);
		Boolean lIsResponseRequested = aToken.getBoolean("responseRequested", true);

		// remove further non target related fields
		aToken.remove("senderIncluded");
		aToken.remove("responseRequested");

		// broadcast the token
		broadcastToken(aConnector, aToken,
				new BroadcastOptions(lIsSenderIncluded, lIsResponseRequested));

		// check if response was requested
		if (lIsResponseRequested) {
			sendToken(aConnector, aConnector, lResponse);
		}
		/*
		 * } else { lResponse.put("code", -1); lResponse.put("msg", "not logged
		 * in"); sendToken(aConnector, lResponse); }
		 */
	}

	private void close(WebSocketConnector aConnector, Token aToken) {
		int lTimeout = aToken.getInteger("timeout", 0);

		Boolean lNoGoodBye = aToken.getBoolean("noGoodBye", false);
		Boolean lNoLogoutBroadcast = aToken.getBoolean("noLogoutBroadcast", false);
		Boolean lNoDisconnectBroadcast = aToken.getBoolean("noDisconnectBroadcast", false);

		// only send a good bye message if timeout is > 0 and not to be noed
		if (lTimeout > 0 && !lNoGoodBye) {
			sendGoodBye(aConnector, CloseReason.CLIENT);
		}
		// if logged in...
		if (getUsername(aConnector) != null && !lNoLogoutBroadcast) {
			// broadcast the logout event.
			broadcastLogoutEvent(aConnector);
		}

		// TODO: Send notification to shared clients?
		// TODO: Clear the session?
		// cleaning the client session 
		// aConnector.getSession().getStorage().clear();
		if (mLog.isDebugEnabled()) {
			mLog.debug("Closing client "
					+ (lTimeout > 0
							? "with timeout " + lTimeout + "ms"
							: "immediately") + "...");
		}

		// don't send a response here! We're about to close the connection!
		// broadcasts disconnect event to other clients
		// if not explicitely noed
		aConnector.setBoolean("noDisconnectBroadcast", lNoDisconnectBroadcast);
		aConnector.setStatus(WebSocketConnectorStatus.DOWN);
		aConnector.stopConnector(CloseReason.CLIENT);
	}

	/**
	 *
	 * @param aToken
	 */
	private void echo(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		final String lEchoTestData = "jWebSocket Echo Simulation Data - ";
		Integer lEchoTestSize = aToken.getInteger("echoTestSize", -1);
		StringBuilder lSB = new StringBuilder();
		int lPos = 0;
		if (lEchoTestSize >= 0) {
			for (int lIdx = 0; lIdx < lEchoTestSize; lIdx++) {
				lSB.append(lEchoTestData.charAt(lPos));
				lPos++;
				if (lPos >= lEchoTestData.length()) {
					lPos = 0;
				}
			}
		}

		String lData = aToken.getString("data");
		Integer lDelay = aToken.getInteger("delay", -1);
		if (lData != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("echo " + lData);
			}
			lResponse.setString("data", lData);
			if (lSB.length() > 0) {
				lResponse.setInteger("echoTestSize", lEchoTestSize);
				lResponse.setString("echoTestData", lSB.toString());
			}
			if (lDelay > 0) {
				lResponse.setInteger("delayed", lDelay);
				try {
					Thread.sleep(lDelay);
				} catch (InterruptedException ex) {
				}
			}
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "missing 'data' argument for 'echo' command");
		}
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void ping(WebSocketConnector aConnector, Token aToken) {
		Boolean lEcho = aToken.getBoolean("echo", Boolean.TRUE);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'Ping' (echo='" + lEcho
					+ "') from '" + aConnector + "'...");
		}

		if (lEcho) {
			Token lResponse = createResponse(aToken);
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	/**
	 * simply waits for a certain amount of time and does not perform any _
	 * operation. This feature is used for debugging and simulation purposes _
	 * only and is not related to any business logic.
	 *
	 * @param aToken
	 */
	private void wait(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		Integer lDuration = aToken.getInteger("duration", 0);
		Boolean lIsResponseRequested = aToken.getBoolean("responseRequested", true);
		if (lDuration != null && lDuration >= 0) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Waiting (duration: " + lDuration + "ms)...");
			}
			try {
				Thread.sleep(lDuration);
			} catch (InterruptedException lEx) {
				// ignore potential exception here!
			}
			lResponse.setInteger("duration", lDuration);
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "missing or invalid 'duration' argument for 'wait' command");
		}

		// for test purposes we need to optionally suppress a response
		// to simulate this error condition
		if (lIsResponseRequested) {
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	/**
	 * Gets the client headers and put them into connector variables
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getHeaders(WebSocketConnector aConnector, Token aToken) {
		aConnector.setVar("clientType", aToken.getString("clientType"));
		aConnector.setVar("clientName", aToken.getString("clientName"));
		aConnector.setVar("clientVersion", aToken.getString("clientVersion"));
		aConnector.setVar("clientInfo", aToken.getString("clientInfo"));
		aConnector.setVar("jwsType", aToken.getString("jwsType"));
		aConnector.setVar("jwsVersion", aToken.getString("jwsVersion"));
		aConnector.setVar("jwsInfo", aToken.getString("jwsInfo"));
		List lUserFormats = aToken.getList(JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY);
		List lEncodingFormats = new FastList();
		if (null != lUserFormats && !lUserFormats.isEmpty()) {
			lEncodingFormats.addAll(lUserFormats);
		}

		// getting the string format of the user supported encodings
		String lEncFormats = StringUtils.join(CollectionUtils
				.intersection(lEncodingFormats, SystemFilter.getSupportedEncodings()), ",");
		aConnector.setVar(JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY, lEncFormats);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getHeaders' from connector '"
					+ aConnector.getId() + "'...");
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getClients(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getClients' from '"
					+ aConnector + "'...");
		}

		if (getUsername(aConnector) != null) {
			String lGroup = aToken.getString("group");
			Integer lMode = aToken.getInteger("mode", 0);
			FastMap<String, Object> lFilter = new FastMap<String, Object>();
			lFilter.put(BaseConnector.VAR_USERNAME, ".*");
			List<String> listOut = new FastList<String>();
			for (WebSocketConnector lConnector : getServer()
					.selectConnectors(lFilter).values()) {
				listOut.add(getUsername(lConnector) + "@" + lConnector.getId());
			}
			lResponse.setList("clients", listOut);
			lResponse.setInteger("count", listOut.size());
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "not logged in");
		}

		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * allocates a "non-interruptable" communication channel between two
	 * clients.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void allocChannel(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'allocChannel' from '"
					+ aConnector + "'...");
		}
	}

	/**
	 * deallocates a "non-interruptable" communication channel between two
	 * clients.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void deallocChannel(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'deallocChannel' from '"
					+ aConnector + "'...");
		}
	}

	/**
	 * Logon a user given the username and password by using the Spring Security
	 * module
	 *
	 * @param aConnector
	 * @param aToken The token with the username and password
	 */
	void logon(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		if (aConnector.getSession().isAuthenticated()) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1, "is authenticated"));
			return;
		}

		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting authentication ...");
		}

		Authentication lAuthRequest = new UsernamePasswordAuthenticationToken(lUsername, lPassword);
		Authentication lAuthResult;
		try {
			lAuthResult = getAuthProvMgr().authenticate(lAuthRequest);
		} catch (AuthenticationException ex) {
			String lMsg = ex.getClass().getSimpleName() + ": " + ex.getMessage();
			Token lResponse = getServer().createErrorToken(aToken, -1, lMsg);
			lResponse.setString("username", lUsername);
			sendToken(aConnector, aConnector, lResponse);
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			return; // stop the execution flow
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug(
					"Authentication successful. Updating the user session (id: "
					+ (null != aConnector.getSession()
							? aConnector.getSession().getSessionId()
							: "[null]")
					+ ", storage: "
					+ (null != aConnector.getSession()
							? aConnector.getSession().getStorage()
							: "[null]")
					+ ")...");
		}

		// getting the session
		Map<String, Object> lSession = aConnector.getSession().getStorage();

		// setting the is_authenticated flag
		lSession.put(IS_AUTHENTICATED, lAuthResult.isAuthenticated());

		// setting the connector username
		aConnector.setUsername(lUsername);

		// setting the uuid
		String lUUID;
		Object lDetails = lAuthResult.getDetails();
		if (null != lDetails && lDetails instanceof IUserUniqueIdentifierContainer) {
			lUUID = ((IUserUniqueIdentifierContainer) lDetails).getUUID();
		} else {
			lUUID = lUsername;
		}
		lSession.put(UUID, lUUID);

		// setting the authorities
		String lAuthorities = "";
		for (GrantedAuthority lGA : lAuthResult.getAuthorities()) {
			lAuthorities = lAuthorities.concat(lGA.getAuthority() + " ");
		}

		// storing the user authorities as a string to avoid serialization problems
		lSession.put(AUTHORITIES, lAuthorities);

		// creating the response
		Token lResponse = createResponse(aToken);
		lResponse.setString("uuid", lUUID);
		lResponse.setString("username", lUsername);
		lResponse.setList("authorities", Tools.parseStringArrayToList(lAuthorities.split(" ")));

		// sending the response to requester
		sendToken(aConnector, lResponse);
		
		// sending response to clients that share the requester session
		getServer().broadcastToSharedSession(aConnector.getId(),
				aConnector.getSession().getSessionId(),
				lResponse,
				false);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Logon process finished successfully!");
		}

		// if successfully logged in...
		if (lUsername != null) {
			// broadcast "login event" to other clients
			broadcastLoginEvent(aConnector);
		}
	}

	void logoff(WebSocketConnector aConnector, Token aToken) {
		if (!SecurityHelper.isUserAuthenticated(aConnector)) {
			getServer().sendToken(aConnector, getServer().createNotAuthToken(aToken));
			return;
		}
		String lUsername = aConnector.getUsername();
		Token lResponse = createResponse(aToken);
		// if anoymous user allowed send corresponding flag for 
		// clarification that auto anonymous may have been applied.
		if (ALLOW_ANONYMOUS_LOGIN && ALLOW_AUTO_ANONYMOUS) {
			lResponse.setBoolean(
					"anonymous",
					null != ANONYMOUS_USER
					&& ANONYMOUS_USER.equals(lUsername));
		}
		// sending the response to requester
		sendToken(aConnector, lResponse);
		
		// broadcasting response to all sharing session connectors
		getServer().broadcastToSharedSession(aConnector.getId(),
				aConnector.getSession().getSessionId(),
				lResponse,
				false);

		// broadcast the logout event
		broadcastLogoutEvent(aConnector);

		// clearing the session
		aConnector.getSession().getStorage().clear();

		// log successful logout operation
		if (mLog.isInfoEnabled()) {
			mLog.info("User '" + lUsername
					+ "' successfully logged out from "
					+ aConnector.getRemoteHost() + " ("
					+ aConnector.getId() + ").");
		}
	}

	void getAuthorities(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		if (!SecurityHelper.isUserAuthenticated(aConnector)) {
			sendToken(aConnector, aConnector, lServer.createNotAuthToken(aToken));
			return;
		}

		String lUsername = aConnector.getUsername();
		Map<String, Object> lSessionParams = aConnector.getSession().getStorage();
		String lAuthorities = (String) lSessionParams.get(AUTHORITIES);

		// Creating the response
		Token lResponse = createResponse(aToken);
		lResponse.setString("username", lUsername);
		lResponse.setList("authorities", Tools.parseStringArrayToList(lAuthorities.split(" ")));

		// Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	void broadcastToSharedSession(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Broadcasting token to connectors that share the same session...");
		}
		aToken.setString("sourceId", aConnector.getId());
		boolean lSenderIncluded = aToken.getBoolean("senderIncluded", false);

		getServer().broadcastToSharedSession(aConnector.getId(),
				aConnector.getSession().getSessionId(),
				aToken,
				lSenderIncluded);

		// sending processing confirmation to calling client
		sendToken(aConnector, createResponse(aToken));
	}

	void sessionGet(WebSocketConnector aConnector, Token aToken) {
		String lConnectorId = aToken.getString("clientId", aConnector.getId());
		boolean lConnectionStorage = aToken.getBoolean("connectionStorage", false);
		boolean lPublic = true;
		if (lConnectorId.equals(aConnector.getId())) {
			lPublic = aToken.getBoolean("public", false);
		}
		// getting the key
		String lKey = aToken.getString("key", null);
		if (null == lKey) {
			sendErrorToken(aConnector, aToken, -1, "Argument 'key' is required!");
			return;
		}

		Map<String, Object> lStorage;
		try {
			if (lConnectionStorage) {
				lStorage = mSessionManager.getStorageProvider().getStorage(lConnectorId);
			} else {
				lStorage = getServer().getConnector(lConnectorId).getSession().getStorage();
			}
		} catch (Exception lEx) {
			sendErrorToken(aConnector, aToken, -1, "Client with id '" + lConnectorId + "' does not exists!");
			return;
		}
		// setting the key as public if required
		lKey = (lPublic) ? SESSION_PUBLIC_KEY_SUBFIX + lKey : lKey;
		if (!lStorage.containsKey(lKey)) {
			sendErrorToken(aConnector, aToken, -1, "The key '" + lKey
					+ "' does not exists in the targeted client session storage!");
			return;
		}

		// getting the value
		Object lValue = lStorage.get(lKey);

		Token lResponse = createResponse(aToken);
		Map lMap = new HashMap();
		lMap.put("value", lValue);
		lMap.put("key", lKey);
		lResponse.setMap("data", lMap);

		getServer().sendToken(aConnector, lResponse);
	}

	void sessionPut(WebSocketConnector aConnector, Token aToken) {
		boolean lPublic = aToken.getBoolean("public", false);
		boolean lConnectionStorage = aToken.getBoolean("connectionStorage", false);
		String lConnectorId = aConnector.getId();

		// getting the key
		String lKey = aToken.getString("key", null);
		if (null == lKey) {
			sendErrorToken(aConnector, aToken, -1, "Argument 'key' is required!");
			return;
		}

		lKey = (lPublic) ? SESSION_PUBLIC_KEY_SUBFIX + lKey : lKey;
		// protect system session entries
		if (!lConnectionStorage) {
			if (lKey.equals(UUID) || lKey.equals(USERNAME) || lKey.equals(AUTHORITIES)
					|| lKey.equals(WebSocketSession.CREATED_AT)) {
				sendErrorToken(aConnector, aToken, -1, "The given key '" + lKey + "', target a read only value!");
				return;
			}
		}

		// getting the value
		Object lValue = aToken.getObject("value");
		if (null == lValue) {
			sendErrorToken(aConnector, aToken, -1, "Argument 'value' is required!");
			return;
		}

		Map<String, Object> lStorage;
		try {
			if (lConnectionStorage) {
				lStorage = mSessionManager.getStorageProvider().getStorage(lConnectorId);
			} else {
				lStorage = aConnector.getSession().getStorage();
			}
		} catch (Exception lEx) {
			sendErrorToken(aConnector, aToken, -1, "Error getting the client session storage!");
			return;
		}

		lStorage.put(lKey, lValue);

		getServer().sendToken(aConnector, createResponse(aToken));
	}

	void sessionHas(WebSocketConnector aConnector, Token aToken) {
		String lConnectorId = aToken.getString("clientId", aConnector.getId());
		boolean lConnectionStorage = aToken.getBoolean("connectionStorage", false);
		boolean lPublic = true;

		if (lConnectorId.equals(aConnector.getId())) {
			lPublic = aToken.getBoolean("public", false);
		}

		// getting the key
		String lKey = aToken.getString("key", null);
		if (null == lKey) {
			sendErrorToken(aConnector, aToken, -1, "Argument 'key' is required!");
			return;
		}

		Map<String, Object> lStorage;
		try {
			if (lConnectionStorage) {
				lStorage = mSessionManager.getStorageProvider().getStorage(lConnectorId);
			} else {
				lStorage = aConnector.getSession().getStorage();
			}
		} catch (Exception lEx) {
			sendErrorToken(aConnector, aToken, -1, "Client with id '" + lConnectorId + "' does not exists!");
			return;
		}

		lKey = (lPublic) ? SESSION_PUBLIC_KEY_SUBFIX + lKey : lKey;
		boolean lExists = lStorage.containsKey(lKey);

		Token lResponse = createResponse(aToken);
		Map<String, Object> lMap = new FastMap<String, Object>();
		lMap.put("key", lKey);
		lMap.put("exists", lExists);
		lResponse.setMap("data", lMap);

		getServer().sendToken(aConnector, lResponse);
	}

	void sessionRemove(WebSocketConnector aConnector, Token aToken) {
		boolean lPublic = aToken.getBoolean("public", false);
		boolean lConnectionStorage = aToken.getBoolean("connectionStorage", false);

		// getting the key
		String lKey = aToken.getString("key", null);
		if (null == lKey) {
			sendErrorToken(aConnector, aToken, -1, "Argument 'key' is required!");
			return;
		}

		lKey = (lPublic) ? SESSION_PUBLIC_KEY_SUBFIX + lKey : lKey;
		// protect system session entries
		if (!lConnectionStorage) {
			if (lKey.equals(UUID) || lKey.equals(USERNAME) || lKey.equals(AUTHORITIES)
					|| lKey.equals(WebSocketSession.CREATED_AT)) {
				sendErrorToken(aConnector, aToken, -1, "The given key '" + lKey + "', target a read only value!");
				return;
			}
		}

		Map<String, Object> lStorage;
		try {
			if (lConnectionStorage) {
				lStorage = mSessionManager.getStorageProvider().getStorage(aConnector.getId());
			} else {
				lStorage = aConnector.getSession().getStorage();
			}
		} catch (Exception lEx) {
			sendErrorToken(aConnector, aToken, -1, "Error getting the client session storage!");
			return;
		}

		if (!lStorage.containsKey(lKey)) {
			sendErrorToken(aConnector, aToken, -1, "The key '" + lKey
					+ "' does not exists!");
			return;
		}

		// removing the session entry
		Object lValue = lStorage.remove(lKey);

		Token lResponse = createResponse(aToken);
		Map<String, Object> lMap = new FastMap<String, Object>();
		lMap.put("key", lKey);
		lMap.put("value", lValue);
		lResponse.setMap("data", lMap);

		getServer().sendToken(aConnector, lResponse);
	}

	void sessionKeys(WebSocketConnector aConnector, Token aToken) {
		String lConnectorId = aToken.getString("clientId", aConnector.getId());
		boolean lConnectionStorage = aToken.getBoolean("connectionStorage", false);
		boolean lPublic = true;

		if (lConnectorId.equals(aConnector.getId())) {
			lPublic = aToken.getBoolean("public", false);
		}

		Map<String, Object> lStorage;
		try {
			if (lConnectionStorage) {
				lStorage = mSessionManager.getStorageProvider().getStorage(aConnector.getId());
			} else {
				lStorage = aConnector.getSession().getStorage();
			}
		} catch (Exception lEx) {
			sendErrorToken(aConnector, aToken, -1, "Client with id '" + lConnectorId + "' does not exists!");
			return;
		}

		Iterator<String> lKeySet = lStorage.keySet().iterator();
		List<String> lKeys = new LinkedList<String>();

		while (lKeySet.hasNext()) {
			String lKey = lKeySet.next();
			if (lPublic && !lKey.startsWith(SESSION_PUBLIC_KEY_SUBFIX)) {
				continue;
			} else {
				lKeys.add(lKey);
			}
		}

		Token lResponse = createResponse(aToken);
		lResponse.setList("data", lKeys);

		getServer().sendToken(aConnector, lResponse);
	}

	void sessionGetAll(WebSocketConnector aConnector, Token aToken) {
		String lConnectorId = aToken.getString("clientId", aConnector.getId());
		boolean lConnectionStorage = aToken.getBoolean("connectionStorage", false);
		boolean lPublic = true;
		if (lConnectorId.equals(aConnector.getId())) {
			lPublic = aToken.getBoolean("public", false);
		}

		Map<String, Object> lStorage;
		try {
			if (lConnectionStorage) {
				lStorage = mSessionManager.getStorageProvider().getStorage(aConnector.getId());
			} else {
				lStorage = aConnector.getSession().getStorage();
			}
		} catch (Exception lEx) {
			sendErrorToken(aConnector, aToken, -1, "Client with id '" + lConnectorId + "' does not exists!");
			return;
		}

		// getting entries
		Iterator<Entry<String, Object>> lEntries = lStorage.entrySet().iterator();
		Map<String, Object> lResult = new HashMap<String, Object>();
		while (lEntries.hasNext()) {
			Entry<String, Object> lEntry = lEntries.next();
			if (lPublic && !lEntry.getKey().startsWith(SESSION_PUBLIC_KEY_SUBFIX)) {
				continue;
			} else {
				lResult.put(lEntry.getKey(), lEntry.getValue());
			}
		}

		// creating the response
		Token lResponse = createResponse(aToken);
		lResponse.setMap("data", lResult);

		// sending entries to the client
		getServer().sendToken(aConnector, lResponse);
	}

	void sessionGetMany(WebSocketConnector aConnector, Token aToken) {
		boolean lConnectionStorage = aToken.getBoolean("connectionStorage", false);
		List lClients = aToken.getList("clients");
		List lKeys = aToken.getList("keys");
		if (null == lClients || lClients.isEmpty()) {
			sendErrorToken(aConnector, aToken, -1, "Argument 'clients' is required!");
			return;
		}
		if (null == lKeys || lKeys.isEmpty()) {
			sendErrorToken(aConnector, aToken, -1, "Argument 'keys' is required!");
			return;
		}

		Map<String, Object> lResult = new HashMap<String, Object>();
		for (Object lConnectorId : lClients) {
			Map<String, Object> lStorage;
			try {
				if (lConnectionStorage) {
					lStorage = mSessionManager.getStorageProvider().getStorage(aConnector.getId());
				} else {
					lStorage = aConnector.getSession().getStorage();
				}
				Map<String, Object> lVars = new HashMap<String, Object>();
				for (Object lKey : lKeys) {
					try {
						lVars.put(lKey.toString(), lStorage.get(SESSION_PUBLIC_KEY_SUBFIX + lKey));
					} catch (Exception lEx) {
					}
				}

				lResult.put(lConnectorId.toString(), lVars);
			} catch (Exception lEx) {
				continue;
			}
		}

		// creating the response
		Token lResponse = createResponse(aToken);
		lResponse.setMap("data", lResult);

		// sending entries to the client
		getServer().sendToken(aConnector, lResponse);
	}

	void notifySessionStarted(WebSocketConnector aConnector) {
		try {
			// getting the message hub
			JMSManager lMessageHub = getServer().getJMSManager();

			// creating the event message to be sent
			MapMessage lMsg = lMessageHub.buildMessage(getNamespace(), "sessionStarted");
			lMsg.setStringProperty("connectorId", aConnector.getId());

			// sending event
			lMessageHub.send(lMsg);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying 'sessionStarted' "
					+ "event through the MessageHub"), lEx);
		}
	}

	void notifySessionStopped(WebSocketSession aSession) {
		try {
			// getting the message hub
			JMSManager lMessageHub = getServer().getJMSManager();

			// creating the event message to be sent
			MapMessage lMsg = lMessageHub.buildMessage(getNamespace(), "sessionStopped");
			lMsg.setStringProperty("username", aSession.getUsername());
			lMsg.setStringProperty("uuid", aSession.getUUID());
			lMsg.setBooleanProperty("authenticated", aSession.isAuthenticated());
			lMsg.setStringProperty("authorities", (String) aSession.getStorage().get(SystemPlugIn.AUTHORITIES));

			// sending event
			lMessageHub.send(lMsg);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying 'sessionStopped' "
					+ "event through the MessageHub"), lEx);
		}
	}

	void notifyConnectorStarted(WebSocketConnector aConnector) {
		try {
			// getting the message hub
			JMSManager lMessageHub = getServer().getJMSManager();

			// creating the event message to be sent
			MapMessage lMsg = lMessageHub.buildMessage(getNamespace(), "connectorStarted");
			lMsg.setStringProperty("connectorId", aConnector.getId());

			// sending event
			lMessageHub.send(lMsg);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying 'connectorStarted' "
					+ "event through the MessageHub"), lEx);
		}
	}

	void notifyConnectorStopped(WebSocketConnector aConnector) {
		try {
			// getting the message hub
			JMSManager lMessageHub = getServer().getJMSManager();

			// creating the event message to be sent
			MapMessage lMsg = lMessageHub.buildMessage(getNamespace(), "connectorStopped");
			lMsg.setStringProperty("connectorId", aConnector.getId());

			// sending event
			lMessageHub.send(lMsg);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying 'connectorStopped' "
					+ "event through the MessageHub"), lEx);
		}
	}

	void notifyLogon(WebSocketConnector aConnector) {
		try {
			// getting the message hub
			JMSManager lMessageHub = getServer().getJMSManager();

			// creating the event message to be sent
			MapMessage lMsg = lMessageHub.buildMessage(getNamespace(), "logon");
			lMsg.setStringProperty("connectorId", aConnector.getId());
			lMsg.setStringProperty("username", aConnector.getUsername());

			// sending event
			lMessageHub.send(lMsg);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying 'logon' "
					+ "event through the MessageHub"), lEx);
		}
	}

	void notifyLogoff(WebSocketConnector aConnector) {
		try {
			// getting the message hub
			JMSManager lMessageHub = getServer().getJMSManager();

			// creating the event message to be sent
			MapMessage lMsg = lMessageHub.buildMessage(getNamespace(), "logoff");
			lMsg.setStringProperty("connectorId", aConnector.getId());
			lMsg.setStringProperty("username", aConnector.getUsername());

			// sending event
			lMessageHub.send(lMsg);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying 'logoff' "
					+ "event through the MessageHub"), lEx);
		}
	}
}
