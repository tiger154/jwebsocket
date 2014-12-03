//	---------------------------------------------------------------------------
//	jWebSocket - JMS Engine (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.gateway;

import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.logging.Logging;

/**
 * JMS Engine implementation to treat JMS endpoints in the same way like
 * jWebSocket clients
 *
 * @author Alexander Schulze
 */
// http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/jms_tutorialTOC.html
public class JMSEngine extends BaseEngine {

	private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aConfiguration
	 */
	public JMSEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JMSEngine...");
		}
		// ...
		if (mLog.isInfoEnabled()) {
			mLog.info("JMSEngine successfully instantiated.");
		}
	}

	@Override
	public void processPacket(WebSocketConnector aConnector,
			WebSocketPacket aDataPacket) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing packet: "
					+ "[content supressed]" /* + aDataPacket.getString() */);
			// be careful with sensitive data packets in logs!
		}
		super.processPacket(aConnector, aDataPacket);
	}

	@Override
	public void sendPacket(WebSocketConnector aConnector,
			WebSocketPacket aDataPacket) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending packet: "
					+ "[content supressed]" /* + aDataPacket.getString() */);
			// be careful with sensitive data packets in logs!
		}
	}
}
