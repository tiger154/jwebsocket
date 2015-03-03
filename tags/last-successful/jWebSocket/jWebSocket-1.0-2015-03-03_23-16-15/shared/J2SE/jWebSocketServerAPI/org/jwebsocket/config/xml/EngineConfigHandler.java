// ---------------------------------------------------------------------------
// jWebSocket - EngineConfigHandler (Community Edition, CE)
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
package org.jwebsocket.config.xml;

import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.config.Config;
import org.jwebsocket.config.ConfigHandler;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.util.Tools;

/**
 * Handles the engine configuration
 *
 * @author puran
 * @version $Id: EngineConfigHandler.java 624 2010-07-06 12:28:44Z
 * fivefeetfurther $
 */
public class EngineConfigHandler implements ConfigHandler {

	private static final String ELEMENT_ENGINE = "engine";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String JAR = "jar";
	private static final String CONTEXT = "context";
	private static final String SERVLET = "servlet";
	private static final String PORT = "port";
	private static final String HOSTNAME = "hostname";
	private static final String SSL_PORT = "sslport";
	private static final String KEYSTORE = "keystore";
	private static final String KEYSTORE_PASSWORD = "password";
	private static final String TIMEOUT = "timeout";
	private static final String MAXFRAMESIZE = "maxframesize";
	private static final String DOMAINS = "domains";
	private static final String NOTIFY_SYSTEM_STOPPING = "notifySystemStopping";
	private static final String DOMAIN = "domain";
	private static final String MAX_CONNECTIONS = "maxconnections";
	private static final String ON_MAX_CONNECTIONS = "onmaxconnections";
	private static final String KEEP_ALIVE_CONNECTORS = "keepAliveConnectors";
	private static final String KEEP_ALIVE_CONNECTORS_INTERVAL = "keepAliveConnectorsInterval";
	private static final String KEEP_ALIVE_CONNECTORS_TIMEOUT = "keepAliveConnectorsTimeout";

	/**
	 * {@inheritDoc}
	 *
	 * @param aStreamReader
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader aStreamReader)
			throws XMLStreamException {
		String lId = "", lName = "", lJar = "", lContext = "", lServlet = "",
				lHostname = null,
				lKeyStore = JWebSocketServerConstants.JWEBSOCKET_KEYSTORE,
				lKeyStorePassword = JWebSocketServerConstants.JWEBSOCKET_KS_DEF_PWD,
				lOnMaxConnectionsStrategy = JWebSocketServerConstants.DEFAULT_ON_MAX_CONNECTIONS_STRATEGY;
		int lPort = 0, lSSLPort = 0, lTimeout = 60 * 60, lFramesize = 0;
		Integer lMaxConnections = JWebSocketServerConstants.DEFAULT_MAX_CONNECTIONS;
		Map<String, Object> lSettings = new FastMap();
		boolean lNotifySystemStopping = JWebSocketServerConstants.DEFAULT_NOTIFY_SYSTEM_STOPPING;
		boolean lKeepAliveConnectors = JWebSocketServerConstants.KEEP_ALIVE_CONNECTORS;
		Integer lKeepAliveConnectorsInterval = JWebSocketServerConstants.KEEP_ALIVE_CONNECTORS_INTERVAL;
		Integer lKeepAliveConnectorsTimeout = JWebSocketServerConstants.KEEP_ALIVE_CONNECTORS_TIMEOUT;

		List<String> lDomains = null;
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ID)) {
					aStreamReader.next();
					lId = aStreamReader.getText();
				} else if (lElementName.equals(NAME)) {
					aStreamReader.next();
					lName = aStreamReader.getText();
				} else if (lElementName.equals(JAR)) {
					aStreamReader.next();
					lJar = aStreamReader.getText();
				} else if (lElementName.equals(CONTEXT)) {
					aStreamReader.next();
					lContext = aStreamReader.getText();
				} else if (lElementName.equals(SERVLET)) {
					aStreamReader.next();
					lServlet = aStreamReader.getText();
				} else if (lElementName.equals(PORT)) {
					aStreamReader.next();
					lPort = Tools.stringToInt(aStreamReader.getText(), -1);
				} else if (lElementName.equals(SSL_PORT)) {
					aStreamReader.next();
					lSSLPort = Tools.stringToInt(aStreamReader.getText(), -1);
				} else if (lElementName.equals(KEYSTORE)) {
					aStreamReader.next();
					lKeyStore = aStreamReader.getText();
				} else if (lElementName.equals(KEYSTORE_PASSWORD)) {
					aStreamReader.next();
					lKeyStorePassword = aStreamReader.getText();
				} else if (lElementName.equals(HOSTNAME)) {
					aStreamReader.next();
					lHostname = aStreamReader.getText();
				} else if (lElementName.equals(TIMEOUT)) {
					aStreamReader.next();
					lTimeout = Integer.parseInt(aStreamReader.getText());
				} else if (lElementName.equals(DOMAINS)) {
					lDomains = getDomains(aStreamReader);
				} else if (lElementName.equals(MAXFRAMESIZE)) {
					aStreamReader.next();
					lFramesize = Integer.parseInt(aStreamReader.getText());
				} else if (lElementName.equals(NOTIFY_SYSTEM_STOPPING)) {
					aStreamReader.next();
					lNotifySystemStopping = ("true".equals(aStreamReader.getText().toLowerCase()));
				} else if (lElementName.equals(MAX_CONNECTIONS)) {
					aStreamReader.next();
					lMaxConnections = Integer.parseInt(aStreamReader.getText());
				} else if (lElementName.equals(ON_MAX_CONNECTIONS)) {
					aStreamReader.next();
					lOnMaxConnectionsStrategy = aStreamReader.getText();
				} else if (lElementName.equals(JWebSocketConfigHandler.SETTINGS)) {
					lSettings = JWebSocketConfigHandler.getSettings(aStreamReader);
				} else if (lElementName.equals(KEEP_ALIVE_CONNECTORS)) {
					aStreamReader.next();
					lKeepAliveConnectors = ("true".equals(aStreamReader.getText().toLowerCase()));
				} else if (lElementName.equals(KEEP_ALIVE_CONNECTORS_INTERVAL)) {
					aStreamReader.next();
					lKeepAliveConnectorsInterval = Integer.parseInt(aStreamReader.getText());
				} else if (lElementName.equals(KEEP_ALIVE_CONNECTORS_TIMEOUT)) {
					aStreamReader.next();
					lKeepAliveConnectorsTimeout = Integer.parseInt(aStreamReader.getText());
				} else {
					//ignore
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ENGINE)) {
					break;
				}
			}
		}
		return new EngineConfig(lId, lName, lJar,
				lPort, lSSLPort, lHostname, lKeyStore, lKeyStorePassword,
				lContext, lServlet,
				lTimeout, lFramesize, lDomains, lMaxConnections,
				lOnMaxConnectionsStrategy,
				lNotifySystemStopping, lSettings,
				lKeepAliveConnectors,
				lKeepAliveConnectorsInterval,
				lKeepAliveConnectorsTimeout);
	}

	/**
	 * Read the list of domains
	 *
	 * @param aStreamReader the stream reader object
	 * @return the list of domains for the engine
	 * @throws XMLStreamException in case of stream exception
	 */
	private List<String> getDomains(XMLStreamReader aStreamReader)
			throws XMLStreamException {
		List<String> lDomains = new FastList<String>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(DOMAIN)) {
					aStreamReader.next();
					lDomains.add(aStreamReader.getText());
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(DOMAINS)) {
					break;
				}
			}
		}
		return lDomains;
	}
}
