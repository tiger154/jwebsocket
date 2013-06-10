//  ---------------------------------------------------------------------------
//  jWebSocket - JMS Client Listener (Community Edition, CE)
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
package org.jwebsocket.jms.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 *
 * @author alexanderschulze
 */
class JMSClientListener implements MessageListener {

	private JMSClientSender mSender = null;

	public JMSClientListener(JMSClientSender aSender) {
		mSender = aSender;
	}
	
	@Override
	public void onMessage(Message aMsg) {
		ActiveMQTextMessage lMQMsg = (ActiveMQTextMessage) aMsg;
		System.out.println("###### " + aMsg);
		try {
			String lJSON = lMQMsg.getText();
			Token lToken = JSONProcessor.JSONStringToToken(lJSON);
			// fields for requests
			String lNS = lToken.getNS();
			String lType = lToken.getType();
			// fields for responses
			String lReqType = lToken.getString("reqType", "");
			String lMsg = lToken.getString("msg", "[no error message provided]");
			Integer lCode = lToken.getInteger("code", -1);

			// System.out.println("###### " + lJSON);

			// the server accepted the new JMS client, so login now...
			if ("org.jwebsocket.jms.bridge".equals(lNS)) {
				if ("welcome".equals(lType)) {
					System.out.println("Connection successful, logging-in...");
					// mSender.setCorrelationId(lToken.getString("correlationId"));
					mSender.send(
							"{\"ns\":\"org.jwebsocket.plugins.system\""
							+ ", \"type\":\"login\""
							+ ", \"username\":\"root\""
							+ ", \"password\":\"root\"}");
				} else {
					System.out.println("Unknown JMS command: " + lJSON);
				}
				// the server login was successful, so upload the file now...
			} else if ("org.jwebsocket.plugins.system".equals(lNS)) {
				if ("login".equals(lReqType)) {
					if (0 == lCode) {
						System.out.println("Log-in-successful, uploading file...");
						mSender.send(
								"{\"ns\": \"org.jwebsocket.plugins.filesystem\""
								+ ", \"type\": \"save\""
								+ ", \"scope\": \"private\""
								+ ", \"encoding\": \"save\""
								+ ", \"encode\": false"
								+ ", \"notify\": false"
								+ ", \"data\": \"This is just another test content\""
								+ ", \"filename\": \"test.txt\"}");
					} else {
						System.out.println("Log-in failure: " + lMsg);
					}
				} else if ("broadcast".equals(lType)) {
					System.out.println("Received broadcast: " + lJSON);
				} else {
					System.out.println("Unknown system command: " + lJSON);
				}
			} else if ("org.jwebsocket.plugins.filesystem".equals(lNS)) {
				if ("save".equals(lReqType)) {
					if (0 == lCode) {
						System.out.println("File upload successful, continuing process...");
					} else {
						System.out.println("File upload failure: " + lMsg);
					}
				} else {
					System.out.println("Unknown filesystem command: " + lJSON);
				}
			} else {
				System.out.println("Received (but ignored): " + lJSON);
			}
		} catch (JMSException lEx) {
			System.out.println("Exception: " + lEx.getMessage());
		}
	}

}
