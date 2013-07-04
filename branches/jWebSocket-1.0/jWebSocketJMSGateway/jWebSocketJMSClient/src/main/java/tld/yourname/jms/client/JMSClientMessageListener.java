//  ---------------------------------------------------------------------------
//  jWebSocket - JMSClientMessageListener (Community Edition, CE)
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
package tld.yourname.jms.client;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.jms.endpoint.JMSEndPointMessageListener;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 * JMS Gateway Demo Listener This is a simple state machine, which
 * asynchronously processes incoming messages and answers them.
 *
 * @author Alexander Schulze
 */
public class JMSClientMessageListener extends JMSEndPointMessageListener {

	static final Logger mLog = Logger.getLogger(JMSClientMessageListener.class);

	/**
	 *
	 * @param aJMSClient
	 */
	public JMSClientMessageListener(JMSEndPoint aJMSClient) {
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
						/*
						 // try to upload a file via the jWebSocket file system plug-in
						 sendText(lSourceId,
						 "{\"ns\": \"org.jwebsocket.plugins.filesystem\""
						 + ",\"type\": \"save\""
						 + ",\"scope\": \"private\""
						 + ",\"encoding\": \"save\""
						 + ",\"encode\": false"
						 + ",\"notify\": false"
						 + ",\"data\": \"This is just another test content\""
						 + ",\"filename\": \"test.txt\"}");
						 */

						// send a JSON message to the JMS Server Endpoint
						String lJSONPayload =
								"{\"ns\":\"tld.target.json\""
								+ ",\"type\":\"json_demo\""
								+ ",\"data\":\"Here we can pass any JSON to the target\"}";
						if (mLog.isInfoEnabled()) {
							mLog.info("Sending JSON payload via Gateway (" + lJSONPayload + ")...");
						}
						sendJSONviaGateway("JMSServer", lJSONPayload);

						// send a XML message to the JMS Server Endpoint
						String lXMLPayload =
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
								+ "<payload>"
								+ "		<content>"
								+ "			<data>1</data>"
								+ "			<data>2</data>"
								+ "			<data>3</data>"
								+ "		</content>"
								+ "</payload>";
						if (mLog.isInfoEnabled()) {
							mLog.info("Sending XML payload via Gateway (" + lXMLPayload + ")...");
						}
						sendXMLviaGateway("JMSServer", lXMLPayload);

						try {
							// give a second to process answer
							// TODO: this needs to be implemented more clean!
							Thread.sleep(1000);
						} catch (InterruptedException ex) {
						}
						// shutdown the client, is just for one request
						getJMSEndPoint().shutdown();

					} else {
						mLog.error("Log-in failure: " + lMsg);
					}
				} else if ("broadcast".equals(lType)) {
					if (mLog.isInfoEnabled()) {
						mLog.info("Processing broadcast: " + lJSON);
					}
				} else if ("send".equals(lReqType)) {
					if (0 == lCode) {
						if (mLog.isInfoEnabled()) {
							mLog.info("Send operation was successful");
						}
					} else {
						mLog.warn("Send operation was not successful");
					}
				} else {
					mLog.warn("Received unknown system command: " + lJSON);
				}
			} else if ("org.jwebsocket.plugins.filesystem".equals(lNS)) {
				if ("save".equals(lReqType)) {
					if (0 == lCode) {
						if (mLog.isInfoEnabled()) {
							mLog.info("File upload successful, shutting down...");
						}
						getJMSEndPoint().shutdown();
					} else {
						mLog.error("File upload failure: " + lMsg);
					}
				} else {
					mLog.warn("Received unknown filesystem command: " + lJSON);
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
