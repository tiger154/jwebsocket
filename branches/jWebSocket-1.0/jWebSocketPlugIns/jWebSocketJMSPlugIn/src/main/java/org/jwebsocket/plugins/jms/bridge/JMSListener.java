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
package org.jwebsocket.plugins.jms.bridge;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author alexanderschulze
 */
public class JMSListener implements MessageListener {

	private static Logger mLog = Logging.getLogger();
	JmsTemplate mJMSTemplate = null;
	private JMSEngine mEngine = null;
	WebSocketConnector mConnector = null;

	public JmsTemplate getJMSTemplate() {
		return mJMSTemplate;
	}

	public void setJMSTemplate(JmsTemplate aJMSTemplate) {
		mJMSTemplate = aJMSTemplate;
	}

	@Override
	public void onMessage(Message aMsg) {
		ActiveMQTextMessage lMsg = (ActiveMQTextMessage) aMsg;
		try {
			String lJSON = lMsg.getText();
			Token lToken = JSONProcessor.JSONStringToToken(lJSON);
			String lNS = lToken.getNS();
			String lType = lToken.getType();
			if ("org.jwebsocket.jms.bridge".equals(lNS)) {
				if ("connect".equals(lType)) {
					if (mEngine.getConnectors().size() <= 0) {
						mConnector = new JMSConnector(mEngine, mJMSTemplate);
						mEngine.addConnector(mConnector);
					}
					if (mLog.isInfoEnabled()) {
						mLog.info("Registered new JMS client.");
					}
					String lPacket = "{\"ns\":\"org.jwebsocket.jms.bridge\",\"type\":\"accepted\"}";
					mJMSTemplate.convertAndSend(lPacket);
				} else {
					mLog.warn("JMS bridge command '" + lType + "' ignored!");
				}
			} else {
				if (null != mConnector) {
					WebSocketPacket lPacket = new RawPacket(lJSON);
					mConnector.processPacket(lPacket);
				}
			}
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "getting JMS text message"));
		}
	}

	/**
	 * @return the mEngine
	 */
	public JMSEngine getEngine() {
		return mEngine;
	}

	/**
	 * @param aEngine the mEngine to set
	 */
	public void setEngine(JMSEngine aEngine) {
		mEngine = aEngine;
	}
}
