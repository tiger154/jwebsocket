/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jms.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.springframework.jms.core.JmsTemplate;

/**
 *
 * @author alexanderschulze
 */
class JMSClientListener implements MessageListener {

	private JmsTemplate mJmsTemplate = null;

	@Override
	public void onMessage(Message aMsg) {
		ActiveMQTextMessage lMQMsg = (ActiveMQTextMessage) aMsg;
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

			// the server accepted the new JMS client, so login now...
			if ("org.jwebsocket.jms.bridge".equals(lNS)) {
				if ("accepted".equals(lType)) {
					System.out.println("Connection successful, logging-in...");
					mJmsTemplate.convertAndSend(
							"{\"ns\":\"org.jwebsocket.plugins.system\""
							+ ", \"type\":\"login\""
							+ ", \"username\":\"root\""
							+ ", \"password\":\"root\"}");
				}
				// the server login was successful, so upload the file now...
			} else if ("org.jwebsocket.plugins.system".equals(lNS)) {
				if ("login".equals(lReqType)) {
					if (0 == lCode) {
						System.out.println("Log-in-successful, uploading file...");
						mJmsTemplate.convertAndSend(
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
				}
			} else if ("org.jwebsocket.plugins.filesystem".equals(lNS)) {
				if ("save".equals(lReqType)) {
					if (0 == lCode) {
						System.out.println("File upload successful, continuing process...");
					} else {
						System.out.println("File upload failure: " + lMsg);
					}
				}
			} else {
				System.out.println("Received (but ignored): " + lJSON);
			}
		} catch (JMSException lEx) {
			System.out.println("Exception: " + lEx.getMessage());
		}
	}

	/**
	 * @return the mJmsTemplate
	 */
	public JmsTemplate getJmsTemplate() {
		return mJmsTemplate;
	}

	/**
	 * @param mJmsTemplate the mJmsTemplate to set
	 */
	public void setJmsTemplate(JmsTemplate aJmsTemplate) {
		mJmsTemplate = aJmsTemplate;
	}
}
