//  ---------------------------------------------------------------------------
//  jWebSocket - High Level EndPoint MessageListener (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import org.apache.log4j.Logger;
import org.jwebsocket.token.Token;

/**
 * This class implements the IJWSMessage listener interface, this listeners
 * listen on particular messages in the jWebSocket conventions only.
 *
 * @author aschulze
 */
public class JWSMessageListener implements IJWSMessageListener {

	static final Logger mLog = Logger.getLogger(JWSMessageListener.class);
	private JWSEndPointSender mSender;

	public JWSMessageListener(JWSEndPointSender aSender) {
		mSender = aSender;
	}

	@Override
	public void processMessage(
			String aFrom, String aTo,
			String aNS, String aType, String[] aArgs,
			String aPayload) {
	}

	@Override
	public void processToken(String aSourceId, Token aToken) {
		// tp be overridden by the actual listener implementation
	}

	@Override
	public void sendToken(String aTargetId, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending token (ns: '" + aToken.getNS()
					+ "', type:'" + aToken.getType() + "' to '" + aTargetId + "'");
		}
		mSender.sendToken(aTargetId, aToken);
	}
}
