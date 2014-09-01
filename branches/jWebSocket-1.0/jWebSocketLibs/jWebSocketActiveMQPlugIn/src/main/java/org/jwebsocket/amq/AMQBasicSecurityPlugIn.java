//	---------------------------------------------------------------------------
//	jWebSocket - AMQBasicSecurityPlugIn (Community Edition, CE)
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
package org.jwebsocket.amq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows/Reject client connections based on basic white/black lists
 *
 * Lists require to be defined inside <tt>${ACTIVEMQ_CONF}</tt> folder, example:
 * <tt>security/whitelist.properties</tt> and
 * <tt>security/blacklist.properties</tt> file.
 *
 * List entry example:
 * <tt>org.jwebsocket.*=*.jwebsocket.org,*.jwebsocket.com</tt>
 *
 * PlugIn configuration example for AMQ:
 * <plugins>
 * <bean xmlns="http://www.springframework.org/schema/beans"
 * id="jwsBasicSecurityPlugIn"
 * class="org.jwebsocket.amq.AMQBasicSecurityPlugIn">
 * <constructor-arg value="security/whitelist.properties"/>
 * <constructor-arg value="security/blacklist.properties"/>
 * </bean>
 * </<plugins>
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public class AMQBasicSecurityPlugIn implements BrokerPlugin {

	private static final Logger mLog = LoggerFactory.getLogger(AMQBasicSecurityPlugIn.class);
	private final Properties mWhiteList = new Properties();
	private final Properties mBlackList = new Properties();
	private static final String NameInAMQLog = "jWebSocket Security Plug-in";

	public AMQBasicSecurityPlugIn(String aWhiteListFilePath, String aBlackListFilePath) {
		try {
			mLog.info("Instantiating " + NameInAMQLog + "...");
			// loading white list data
			File lFile = new File(System.getenv("ACTIVEMQ_CONF") + File.separator + aWhiteListFilePath);
			mWhiteList.load(new FileInputStream(lFile));

			// loading black list data
			lFile = new File(System.getenv("ACTIVEMQ_CONF") + File.separator + aBlackListFilePath);
			mBlackList.load(new FileInputStream(lFile));
		} catch (IOException lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public Broker installPlugin(final Broker aBroker) throws Exception {
		mLog.info("Installing " + NameInAMQLog + "...");
		return new BrokerFilter(aBroker) {

			@Override
			public void addConnection(ConnectionContext aContext, ConnectionInfo aInfo) throws Exception {
				// getting the client id
				String lClientId = aContext.getClientId();
				// getting raw remotehost
				String lRemoteHost = aContext.getConnection().getRemoteAddress();
				int lStartPos = lRemoteHost.indexOf("://") + 3;
				// getting IP address
				String lIpAddress = lRemoteHost.substring(lStartPos, lRemoteHost.indexOf(":", lStartPos));
				// getting hostname
				String lHostName = InetAddress.getByName(lIpAddress).getHostName();

				mLog.info(NameInAMQLog + " checking host '" + lHostName
						+ "', IP '" + lIpAddress
						+ "', endpoint-id '" + lClientId + "'...");

				// checking black list
				Enumeration<Object> lKeys = mBlackList.keys();
				while (lKeys.hasMoreElements()) {
					// key
					String lWildcard = (String) lKeys.nextElement();
					// value
					String[] lHosts = ((String) mBlackList.getProperty(lWildcard)).split(",");
					if (Tools.wildCardMatch(lClientId, lWildcard)
							&& (Tools.wildCardMatch(lHosts, lHostName)
							|| Tools.wildCardMatch(lHosts, lIpAddress))) {
						throw new SecurityException("Endpoint '" + lClientId
								+ "', IP '" + lIpAddress
								+ "', hostname '" + lHostName
								+ "' rejected due to black list restriction!");
					}
				}

				// checking white list
				lKeys = mWhiteList.keys();
				boolean lAuthorized = false;
				while (lKeys.hasMoreElements()) {
					// key
					String lWildcard = (String) lKeys.nextElement();
					// value
					String[] lHosts = ((String) mWhiteList.getProperty(lWildcard)).split(",");
					if (Tools.wildCardMatch(lClientId, lWildcard)
							&& (Tools.wildCardMatch(lHosts, lHostName)
							|| Tools.wildCardMatch(lHosts, lIpAddress))) {
						lAuthorized = true;
						break;
					}
				}
				if (!lAuthorized) {
					throw new SecurityException("Endpoint '" + lClientId
							+ "', IP '" + lIpAddress
							+ "', hostname '" + lHostName
							+ "' rejected due to white list restriction!");
				}

				mLog.info(NameInAMQLog + " accepted host '" + lHostName
						+ "', IP '" + lIpAddress
						+ "', endpoint-id : '" + lClientId + "'.");
				// adding the client connection
				super.addConnection(aContext, aInfo);
			}

		};
	}

}
