//	---------------------------------------------------------------------------
//	jWebSocket - JMS Bridge JMSListener (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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

/**
 * JMS Gateway Message listener
 *
 * @author Alexander Schulze
 */
public class JMSListener implements MessageListener {

	private static Logger mLog = Logging.getLogger();
	private JMSSender mJMSSender = null;
	private JMSEngine mEngine = null;

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
	public void onMessage(Message aMsg) {
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
			String lNS = lToken.getNS();
			String lType = lToken.getType();
			if ("org.jwebsocket.jms.gateway".equals(lNS)) {
				/*
				 if ("connect".equals(lType)) {
				 if (mEngine.getConnectors().size() <= 0) {
				 mConnector = new JMSConnector(mEngine, mJMSSender, "-");
				 mEngine.addConnector(mConnector);
				 }
				 if (mLog.isInfoEnabled()) {
				 mLog.info("Registered new JMS client (Id = '" + lCorrelationId + "').");
				 }
				 String lPacket = "{\"ns\":\"org.jwebsocket.jms.bridge\",\"type\":\"accepted\"}";
				 mJMSSender.convertAndSend(lPacket);
				 } else {
				 mLog.warn("JMS bridge command '" + lType + "' ignored!");
				 }
				 */
				mLog.warn("JMS Gateway command '" + lType + "' ignored!");
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

	/**
	 * @return the mEngine
	 */
	public JMSEngine getEngine() {
		return mEngine;
	}
}
