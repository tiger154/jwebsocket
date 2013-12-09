//  ---------------------------------------------------------------------------
//  jWebSocket - ClusterListener (Community Edition, CE)
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
package org.jwebsocket.plugins.cluster;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 *
 * @author alexanderschulze
 */
class ClusterListener implements MessageListener {

	private static final Logger mLog = Logging.getLogger();
	private ClusterSender mSender = null;

	public ClusterListener(ClusterSender aClusterSender) {
		mSender = aClusterSender;
	}

	@Override
	public void onMessage(Message aMsg) {

		ActiveMQTextMessage lMQMsg = (ActiveMQTextMessage) aMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Message received: '" + aMsg + "'.");
		}

		try {
			String lJSON = lMQMsg.getText();
			Token lToken = JSONProcessor.JSONStringToToken(lJSON);
			// fields for requests
			String lNS = lToken.getNS();
			String lType = lToken.getType();

			// processing org.jwebsocket.cluster messages...
			if ("ojc".equals(lNS)) {
				// a register request from a new node...
				if ("register".equals(lType)) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("New node registering...");
					}
					mSender.send(
							"{\"ns\":\"ojc\""
							+ ",\"type\":\"welcome\""
							+ ",\"sourceId\":\"" + mSender.getNodeId() + "\""
							+ ",\"clientIds\":[]}");
					ClusterService.addEngine(lToken.getString("sourceId"), lToken.getString("sourceId"));
				} else if ("welcome".equals(lType)) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Processing cluster node's welcome...");
					}
				} else if ("connected".equals(lType)) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Processing cluster node's client connect...");
					}
				} else if ("disconnected".equals(lType)) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Processing cluster node's client disconnect...");
					}
				} else {
					mLog.warn("Unknown cluster command: " + lJSON);
				}
			} else {
				mLog.warn("Received (but ignored): " + lJSON);
			}
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "receiving cluster message"));
		}
	}

	/**
	 * @return the mSender
	 */
	public ClusterSender getSender() {
		return mSender;
	}

	/**
	 * @param mSender the mSender to set
	 */
	public void setSender(ClusterSender aSender) {
		mSender = aSender;
	}
}
