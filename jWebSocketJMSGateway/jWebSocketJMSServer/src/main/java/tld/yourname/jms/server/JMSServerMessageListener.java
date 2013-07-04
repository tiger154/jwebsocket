//  ---------------------------------------------------------------------------
//  jWebSocket - JMSServerMessageListener (Community Edition, CE)
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
package tld.yourname.jms.server;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.jms.endpoint.JMSEndPointMessageListener;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 * JMS Gateway Server Listener, this is a simple state machine, which
 * asynchronously processes incoming messages and answers them.
 *
 * @author Alexander Schulze
 */
public class JMSServerMessageListener extends JMSEndPointMessageListener {

	static final Logger mLog = Logger.getLogger(JMSServerMessageListener.class);

	/**
	 *
	 * @param aJMSClient
	 */
	public JMSServerMessageListener(JMSEndPoint aJMSClient) {
		super(aJMSClient);
	}

	@Override
	public void onTextMessage(TextMessage aMessage) {
		try {
			String lSourceId = aMessage.getStringProperty("sourceId");
			if (mLog.isInfoEnabled()) {
				mLog.info("Received text from '" + lSourceId
						+ "': " + aMessage.getText());
			}
			String lJSON = aMessage.getText();
			Token lToken = JSONProcessor.JSONStringToToken(lJSON);
			// fields for requests
			String lNS = lToken.getNS();
			String lType = lToken.getType();
			// fields for responses
			String lReqType = lToken.getString("reqType", "");
			String lMsg = lToken.getString("msg", "[no error message provided]");
			Integer lCode = lToken.getInteger("code", -1);

			// the server accepted the new JMS client, so login now...
			if ("org.jwebsocket.jms.gateway".equals(lNS)) {
				if ("welcome".equals(lType)) {
					if (mLog.isInfoEnabled()) {
						mLog.info("Connection successful, logging-in...");
					}
					sendText(lSourceId,
							"{\"ns\":\"org.jwebsocket.plugins.system\""
							+ ", \"type\":\"login\""
							+ ", \"username\":\"root\""
							+ ", \"password\":\"root\"}");
				} else {
					mLog.warn("Unknown JMS command: " + lJSON);
				}
				// the server login was successful, so upload the file now...
			} else if ("org.jwebsocket.plugins.system".equals(lNS)) {
				if ("login".equals(lReqType)) {
					if (0 == lCode) {
						if (mLog.isInfoEnabled()) {
							mLog.info("Authentication against jWebSocket successful.");
						}
					} else if ("broadcast".equals(lType)) {
						if (mLog.isInfoEnabled()) {
							mLog.info("Processing broadcast: " + lJSON);
						}
					} else {
						mLog.warn("Received unknown system command: " + lJSON);
					}
				} else if ("send".equals(lType)) {
					// here in the field data the actual payload is wrapped
					String lPayload = lToken.getString("data");
					String lFormat = lToken.getString("format");
					if (null != lPayload) {
						if ("json".equals(lFormat)) {
							if (mLog.isInfoEnabled()) {
								mLog.info("Received JSON payload: " + lPayload);
							}
							// here you can process your JSON payload
						} else if ("xml".equals(lFormat)) {
							if (mLog.isInfoEnabled()) {
								mLog.info("Received XML payload: " + lPayload);
							}
							// here you can process your XML payload
						} else {
							if (mLog.isInfoEnabled()) {
								mLog.info("Received payload of unspecified format: " + lPayload);
							}
							// here you can process your unspecified payload
						}
					}
				}
			} else {
				mLog.warn("Received (but ignored): " + lJSON);
			}
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on getting text message.");
		}
	}
}
