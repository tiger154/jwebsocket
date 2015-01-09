//	---------------------------------------------------------------------------
//	jWebSocket - JMS Bridge JMSListener (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.gateway;

import java.util.Map;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 * JMS Gateway Message listener
 *
 * @author Alexander Schulze
 */
public class JMSListener implements MessageListener {

	private static final Logger mLog = Logging.getLogger();
	final private JMSSender mJMSSender;
	final private JMSEngine mEngine;

	/**
	 *
	 * @param aEngine
	 * @param aJMSSender
	 */
	public JMSListener(JMSEngine aEngine, JMSSender aJMSSender) {
		mEngine = aEngine;
		mJMSSender = aJMSSender;
	}

	/**
	 *
	 * @return
	 */
	public JMSSender getJMSSender() {
		return mJMSSender;
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	@SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
	public void onMessage(final Message aMsg) {
		Tools.getThreadPool().submit(new Runnable() {

			@Override
			public void run() {
				String lJSON = null;
				String lSourceId = null;

				try {
					if (aMsg instanceof ActiveMQTextMessage) {
						ActiveMQTextMessage lTextMsg = (ActiveMQTextMessage) aMsg;
						lJSON = lTextMsg.getText();
						lSourceId = (String) lTextMsg.getProperty("sourceId");
					} else if (aMsg instanceof ActiveMQBytesMessage) {
						ActiveMQBytesMessage lBytesMsg = (ActiveMQBytesMessage) aMsg;
						lJSON = new String(lBytesMsg.getContent().getData(), "UTF-8");
						lSourceId = (String) lBytesMsg.getProperty("sourceId");
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx,
							"getting " + aMsg.getClass().getSimpleName() + " message"));
				}
				// TODO: and what happens if none of the above types?

				try {
					Token lToken = JSONProcessor.JSONStringToToken(lJSON);
					if (mLog.isDebugEnabled()) {
						mLog.debug("JMS Gateway incoming message: '" + Logging.getTokenStr(lToken) + "'");
					}
					String lNS = lToken.getNS();
					String lType = lToken.getType();
					if ("org.jwebsocket.jms.gateway".equals(lNS)) {
						String lHostname, lCanonicalHostName, lIpAddress, lEndPointId, lConnectionId;

						lEndPointId = System.getProperty("org.jwebsocket.plugins.jms.gateway.endPointId");
						lConnectionId = System.getProperty("org.jwebsocket.plugins.jms.gateway.connectionId");
						lHostname = System.getProperty("org.jwebsocket.plugins.jms.gateway.hostname");
						lCanonicalHostName = System.getProperty("org.jwebsocket.plugins.jms.gateway.canonicalHostname");
						lIpAddress = System.getProperty("org.jwebsocket.plugins.jms.gateway.ipAddress");

						if ("ping".equals(lType)) {
							String lGatewayId = lToken.getString("gatewayId");
							if (mLog.isInfoEnabled()) {
								mLog.info("Responding to ping from '" + lSourceId + "'"
										+ " " + (null != lGatewayId ? "via '" + lGatewayId + "'" : "directly")
										+ "...");
							}
							String lData = "{\"ns\":\"org.jwebsocket.jms.gateway\""
									+ ",\"type\":\"response\",\"reqType\":\"ping\""
									+ ",\"code\":0,\"msg\":\"pong\",\"utid\":" + lToken.getInteger("utid")
									+ ",\"sourceId\":\"" + lEndPointId + "\""
									+ (null != lHostname ? ",\"hostname\":\"" + lHostname + "\"" : "")
									+ (null != lConnectionId ? ",\"connectionId\":\"" + lConnectionId + "\"" : "")
									+ (null != lCanonicalHostName ? ",\"canonicalHostName\":\"" + lCanonicalHostName + "\"" : "")
									+ (null != lIpAddress ? ",\"ip\":\"" + lIpAddress + "\"" : "")
									+ (null != lGatewayId ? ",\"gatewayId\":\"" + lGatewayId + "\"" : "")
									+ "}";
							if (null != lGatewayId) {
								mJMSSender.sendText(lGatewayId,
										"{\"ns\":\"org.jwebsocket.plugins.system\",\"action\":\"forward.json\","
										+ "\"type\":\"send\",\"sourceId\":\"" + lToken.getString("targetId") + "\","
										+ "\"targetId\":\"" + lToken.getString("sourceId") + "\",\"responseRequested\":false,"
										+ "\"data\":\"" + lData.replace("\"", "\\\"") + "\"}");
							} else {
								mJMSSender.sendText(lToken.getString("sourceId"), lData);
							}
						} else if ("response".equals(lType)) {
							if ("identify".equals(lToken.getString("reqType"))) {
								String lClientConnectionId = lToken.getString("connectionId");
								String lClientEndPointId = lClientConnectionId.split("-", 2)[0];
								if (null != lClientEndPointId && null == mEngine.getConnectorById(lClientEndPointId)) {
									JMSConnector lConnector = new JMSConnector(mEngine, mJMSSender, lClientConnectionId, lClientEndPointId);
									lConnector.startConnector();
									if (mLog.isInfoEnabled()) {
										mLog.info("Remote client '" + lClientEndPointId + "' reconnected successfully!");
									}
								}
							}
						} else if ("identify".equals(lType)) {
							String lGatewayId = lToken.getString("gatewayId");
							if (mLog.isInfoEnabled()) {
								mLog.info("Responding to identify from '" + lSourceId + "'"
										+ " " + (null != lGatewayId ? "via '" + lGatewayId + "'" : "directly")
										+ "...");
							}
							String lData = "{\"ns\":\"org.jwebsocket.jms.gateway\""
									+ ",\"type\":\"response\",\"reqType\":\"identify\""
									+ ",\"code\":0,\"msg\":\"ok\",\"utid\":" + lToken.getInteger("utid")
									+ ",\"sourceId\":\"" + lEndPointId + "\""
									+ (null != lHostname ? ",\"hostname\":\"" + lHostname + "\"" : "")
									+ (null != lConnectionId ? ",\"connectionId\":\"" + lConnectionId + "\"" : "")
									+ (null != lCanonicalHostName ? ",\"canonicalHostName\":\"" + lCanonicalHostName + "\"" : "")
									+ (null != lIpAddress ? ",\"ip\":\"" + lIpAddress + "\"" : "")
									+ (null != lGatewayId ? ",\"gatewayId\":\"" + lGatewayId + "\"" : "")
									+ "}";
							if (null != lGatewayId) {
								mJMSSender.sendText(lGatewayId,
										"{\"ns\":\"org.jwebsocket.plugins.system\",\"action\":\"forward.json\","
										+ "\"type\":\"send\",\"sourceId\":\"" + lToken.getString("targetId") + "\","
										+ "\"targetId\":\"" + lToken.getString("sourceId") + "\",\"responseRequested\":false,"
										+ "\"data\":\"" + lData.replace("\"", "\\\"") + "\"}");
							} else {
								mJMSSender.sendText(lToken.getString("sourceId"), lData);
							}
						}
					} else {
						// here the incoming packets from the JMS bridge are processed
						WebSocketConnector lConnector = null;
						if (null != lSourceId) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Processing JMS packet from '"
										+ lSourceId
										+ "' [content suppressed, length="
										+ (null != lJSON ? lJSON.length() : "0")
										+ " bytes]...");
								// don't log JSON text packet here, it could contain sensitive data!
							}
							Map<String, WebSocketConnector> lConnectors = mEngine.getConnectors();
							if (null != lConnectors) {
								lConnector = lConnectors.get(lSourceId);
								if (null == lConnector) {
									mLog.warn("No connector with Endpoint Id '" + lSourceId + "'.");
								}
							}
						} else {
							mLog.warn("Processing JMS packet with out source-id.");
						}
						if (null != lConnector) {
							WebSocketPacket lPacket = new RawPacket(lJSON);
							lConnector.processPacket(lPacket);
						}
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "getting JMS text message"));
				}
			}
		});
	}

	/**
	 * @return the mEngine
	 */
	public JMSEngine getEngine() {
		return mEngine;
	}
}
