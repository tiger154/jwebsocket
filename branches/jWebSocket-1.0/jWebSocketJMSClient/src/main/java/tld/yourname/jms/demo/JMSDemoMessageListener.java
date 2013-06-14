//  ---------------------------------------------------------------------------
//  jWebSocket - JMSDemoMessageListener (Community Edition, CE)
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
package tld.yourname.jms.demo;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.jms.client.JMSBaseMessageListener;
import org.jwebsocket.jms.client.JMSClient;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 * JMS Gateway Demo Listener
 *
 * @author Alexander Schulze
 */
public class JMSDemoMessageListener extends JMSBaseMessageListener {

	static final Logger mLog = Logger.getLogger(JMSDemoMessageListener.class);
	private JMSClient mJMSClient;
	
	// TODO: For demo purposes this demo still logs all messages in plain text
	// TODO: this needs to be replaced by secured token logging in production!

	/**
	 *
	 * @param aJMSClient
	 */
	public JMSDemoMessageListener(JMSClient aJMSClient) {
		mJMSClient = aJMSClient;
	}

	@Override
	public void onTextMessage(TextMessage aMessage) {
		try {
			mLog.info("Received text: " + aMessage.getText());
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
			if ("org.jwebsocket.jms.bridge".equals(lNS)) {
				if ("welcome".equals(lType)) {
					mLog.info("Connection successful, logging-in...");
					// mSender.setCorrelationId(lToken.getString("correlationId"));
					sendText(
							"{\"ns\":\"org.jwebsocket.plugins.system\""
							+ ", \"type\":\"login\""
							+ ", \"username\":\"root\""
							+ ", \"password\":\"root\"}");
				} else {
					mLog.info("Unknown JMS command: " + lJSON);
				}
				// the server login was successful, so upload the file now...
			} else if ("org.jwebsocket.plugins.system".equals(lNS)) {
				if ("login".equals(lReqType)) {
					if (0 == lCode) {
						mLog.info("Log-in successful, uploading file...");
						sendText(
								"{\"ns\": \"org.jwebsocket.plugins.filesystem\""
								+ ", \"type\": \"save\""
								+ ", \"scope\": \"private\""
								+ ", \"encoding\": \"save\""
								+ ", \"encode\": false"
								+ ", \"notify\": false"
								+ ", \"data\": \"This is just another test content\""
								+ ", \"filename\": \"test.txt\"}");
					} else {
						mLog.info("Log-in failure: " + lMsg);
					}
				} else if ("broadcast".equals(lType)) {
					mLog.info("Processing broadcast: " + lJSON);
				} else {
					mLog.info("Received unknown system command: " + lJSON);
				}
			} else if ("org.jwebsocket.plugins.filesystem".equals(lNS)) {
				if ("save".equals(lReqType)) {
					if (0 == lCode) {
						mLog.info("File upload successful, shutting down...");
						// mJMSClient.shutdown();
					} else {
						mLog.info("File upload failure: " + lMsg);
					}
				} else {
					mLog.info("Received unknown filesystem command: " + lJSON);
				}
			} else {
				mLog.info("Received (but ignored): " + lJSON);
			}
		} catch (JMSException lEx) {
			mLog.info(lEx.getClass().getSimpleName()
					+ " on getting text message.");
		}
	}
}
