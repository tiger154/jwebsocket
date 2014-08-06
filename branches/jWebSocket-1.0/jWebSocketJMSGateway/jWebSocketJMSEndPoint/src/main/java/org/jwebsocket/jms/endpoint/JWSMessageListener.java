//	---------------------------------------------------------------------------
//	jWebSocket - High Level EndPoint MessageListener (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import org.apache.log4j.Logger;
import org.jwebsocket.token.Token;

/**
 * This class implements the IJWSMessage listener interface, this listeners
 * listen on particular messages in the jWebSocket conventions only.
 *
 * @author Alexander Schulze
 */
public class JWSMessageListener implements IJWSMessageListener {

	static final Logger mLog = Logger.getLogger(JWSMessageListener.class);
	private final JWSEndPointSender mSender;

	/**
	 *
	 * @param aSender
	 */
	@Deprecated
	public JWSMessageListener(JWSEndPointSender aSender) {
		mSender = aSender;
	}

	/**
	 *
	 * @param aEndPoint
	 */
	public JWSMessageListener(JWSEndPoint aEndPoint) {
		mSender = aEndPoint.getSender();
	}

	@Override
	public void processMessage(
			String aFrom, String aTo,
			String aNS, String aType, String[] aArgs,
			String aPayload) {
	}

	@Override
	public void processToken(String aSourceId, Token aToken) {
		// to be overridden by the actual listener implementation
	}

	@Override
	public void sendToken(String aTargetId, Token aToken,
			JWSResponseTokenListener aResponseListener, long aTimeOut) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending token to '" 
					+ aTargetId + "' with explicit timeout listener (" 
					+ aTimeOut + " ms): '"
					+ (JMSLogging.isFullTextLogging()
					? aToken.toString()
					: aToken.getLogString()));
		}
		mSender.sendToken(aTargetId, aToken, aResponseListener, aTimeOut);
	}

	@Override
	public void sendToken(String aTargetId, Token aToken,
			JWSResponseTokenListener aResponseListener) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending token to '" 
					+ aTargetId + "' with default timeout listener: '"
					+ (JMSLogging.isFullTextLogging()
					? aToken.toString()
					: aToken.getLogString()));
		}
		mSender.sendToken(aTargetId, aToken, aResponseListener);
	}

	@Override
	public void sendToken(String aTargetId, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending token to '" 
					+ aTargetId + "' w/o timeout listener: '"
					+ (JMSLogging.isFullTextLogging()
					? aToken.toString()
					: aToken.getLogString()));
		}
		mSender.sendToken(aTargetId, aToken);
	}
}
