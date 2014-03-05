//	---------------------------------------------------------------------------
//	jWebSocket - ClusterSender (Community Edition, CE)
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
package org.jwebsocket.plugins.cluster;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 *
 * @author alexanderschulze
 */
public class ClusterSender {

	private static final Logger mLog = Logging.getLogger();
	private final MessageProducer mProducer;
	private String mNodeId;
	private Session mSession;
	private Destination mDestination;

	/**
	 *
	 * @param aSession
	 * @param aProducer
	 * @param aNodeId
	 * @param aDestination
	 */
	public ClusterSender(Session aSession, MessageProducer aProducer,
			Destination aDestination, String aNodeId) {
		mSession = aSession;
		mProducer = aProducer;
		mDestination = aDestination;
		mNodeId = aNodeId;
	}

	/**
	 *
	 * @param aJSON
	 */
	public void send(final String aJSON) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending JSON '" + aJSON + "'...");
		}
		try {
			Message lMsg = mSession.createTextMessage(aJSON);
			lMsg.setJMSDestination(mDestination);
			lMsg.setJMSCorrelationID(mNodeId);
			mProducer.send(lMsg);
		} catch (JMSException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending JSON"));
		}
	}

	/**
	 *
	 * @param aToken
	 */
	public void sendToken(final Token aToken) {
		String lJSON = JSONProcessor.tokenToPacket(aToken).getUTF8();
		send(lJSON);
	}

	/**
	 * @return the Nodeid
	 */
	public String getNodeId() {
		return mNodeId;
	}
}
