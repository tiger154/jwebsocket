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
		ActiveMQTextMessage lMsg = (ActiveMQTextMessage) aMsg;
		try {
			String lJSON = lMsg.getText();
			Token lToken = JSONProcessor.JSONStringToToken(lJSON);
			String lNS = lToken.getNS();
			String lType = lToken.getType();
			if ("org.jwebsocket.jms.bridge".equals(lNS)) {
				if ("accepted".equals(lType)) {
					System.out.println("jWebSocket Server accepted JMS client, logging-in...");
					mJmsTemplate.convertAndSend("{\"ns\":\"org.jwebsocket.plugins.system\", \"type\":\"login\", \"username\":\"root\", \"password\":\"root\"}");
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
