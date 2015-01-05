//	---------------------------------------------------------------------------
//	jWebSocket - Server Configuration Constants (Community Edition, CE)
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
package org.jwebsocket.config;

/**
 * Provides a global shared container for the jWebSocket configuration settings.
 *
 * @author Alexander Schulze
 */
public final class JWebSocketServerConstants {

	/**
	 * Current version string of the jWebSocket package.
	 */
	public static final String VERSION_STR = "1.0.0 RC3 (build 50105)";
	/**
	 * Name space base for tokens and plug-ins.
	 */
	public static final String NS_BASE = "org.jwebsocket";
	/**
	 * Constant for default server host name
	 */
	public static final String DEFAULT_HOSTNAME = "localhost";
	/**
	 * Constant for JWEBSOCKET_HOME environment variable
	 */
	public static final String JWEBSOCKET_HOME = "JWEBSOCKET_HOME";
	/**
	 * Constant for bootstrap.xml configuration file
	 */
	public static final String BOOTSTRAP_XML = "bootstrap.xml";
	/**
	 * Constant for jWebSocket.xml configuration file
	 */
	public static final String JWEBSOCKET_XML = "jWebSocket.xml";
	/**
	 * Constant for jWebSocketDevTemplate.xml configuration file
	 */
	public static final String JWEBSOCKET_DEV_TEMPLATE_XML = "jWebSocketDevTemplate.xml";
	/**
	 * Constant for jWebSocket.ks key store file
	 */
	public static final String JWEBSOCKET_KEYSTORE = "jWebSocket.ks";
	/**
	 * Default password for demo key store
	 */
	public static final String JWEBSOCKET_KS_DEF_PWD = "jWebSocket";
	/**
	 * Default engine for jWebSocket server.
	 */
	public static String DEFAULT_ENGINE = "tcp";
	/**
	 * Default node id if non given in jWebSocket.xml. Empty means do not use
	 * node-id for single stand-alone systems
	 */
	public static String DEFAULT_NODE_ID = "";
	/**
	 * the default maximum number of connections allowed by an engine
	 */
	public static final int DEFAULT_MAX_CONNECTIONS = 10000;
	/**
	 * The default "on max connections reached" strategy {wait, close, reject,
	 * redirect}
	 */
	public static final String DEFAULT_ON_MAX_CONNECTIONS_STRATEGY = "reject";
	/**
	 * Default 'notifySystemStopping' engine configuration value.
	 */
	public static final boolean DEFAULT_NOTIFY_SYSTEM_STOPPING = false;
	/**
	 * The jWebSocket server side ConnectionManager bean id
	 *
	 * @see conf/bootstrap.xml
	 */
	public static final String CONNECTION_MANAGER_BEAN_ID = "org.jwebsocket.server.connection_manager";
	
	public static final boolean KEEP_ALIVE_CONNECTORS = false;
	
	public static final Integer KEEP_ALIVE_CONNECTORS_INTERVAL = 20000;
	
	public static final Integer KEEP_ALIVE_CONNECTORS_TIMEOUT = 10000;

}
