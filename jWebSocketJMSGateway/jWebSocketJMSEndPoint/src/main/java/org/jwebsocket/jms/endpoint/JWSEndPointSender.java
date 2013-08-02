//  ---------------------------------------------------------------------------
//  jWebSocket - JWSEndPointSender (Community Edition, CE)
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

import java.util.Map;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author aschulze
 */
public class JWSEndPointSender extends JMSEndPointSender {

	private JMSEndPoint mEndPoint = null;
	private static int mUTID = 0;

	/**
	 *
	 * @param aEndPoint
	 */
	public JWSEndPointSender(JMSEndPoint aEndPoint) {
		super(aEndPoint.getSession(), aEndPoint.getProducer(),
				aEndPoint.getEndPointId());
		mEndPoint = aEndPoint;
	}

	/**
	 * Sends a JSON token as text message to the given target endpoint.
	 *
	 * @param aTargetId
	 * @param aToken
	 */
	public void sendToken(String aTargetId, Token aToken) {
		// on the JMS Gateway we per defintion/specification only exchange JSON tokens
		sendText(aTargetId, JSONProcessor.tokenToPacket(aToken).getUTF8());
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aUTID
	 * @param aForwardId
	 * @param aArgs
	 * @param aPayload
	 */
	public void sendPayload(String aTargetId, String aNS, String aType,
			Integer aUTID, String aOriginId, Map aArgs, String aPayload) {
		Token lToken = TokenFactory.createToken();
		lToken.setMap(aArgs);
		lToken.setNS(aNS);
		lToken.setType(aType);
		lToken.setString("sourceId", mEndPoint.getEndPointId());
		lToken.setInteger("utid", aUTID);
		lToken.setString("payload", aPayload);
		if (null != aOriginId) {
			lToken.setString("originId", aOriginId);
		}
		sendText(aTargetId, JSONProcessor.tokenToPacket(lToken).getUTF8());
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aArgs
	 * @param aPayload
	 */
	public void sendPayload(String aTargetId, String aNS, String aType,
			Map aArgs, String aPayload) {
		mUTID++;
		sendPayload(aTargetId, aNS, aType, mUTID, null, aArgs, aPayload);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aOriginId The original endpoint of the message to be responded to
	 * @param aArgs
	 * @param aPayload
	 */
	public void forwardPayload(String aTargetId, String aNS, String aType,
			String aOriginId, Map aArgs, String aPayload) {
		mUTID++;
		sendPayload(aTargetId, aNS, aType, mUTID, aOriginId, aArgs, aPayload);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aReqType
	 * @param aUTID
	 * @param aArgs
	 * @param aCode
	 * @param aMsg
	 * @param aPayload
	 */
	public void respondPayload(String aTargetId, String aNS, String aReqType,
			Integer aUTID, int aCode, String aMsg, Map aArgs, String aPayload) {

		Token lResponse = TokenFactory.createToken();
		// set args map first, since setMap overwrites all other values
		lResponse.setMap(aArgs);
		// now start to set the actual values
		lResponse.setNS(aNS);
		lResponse.setType("response");
		lResponse.setString("reqType", aReqType);
		lResponse.setInteger("utid", aUTID);
		lResponse.setInteger("code", aCode);
		lResponse.setString("msg", aMsg);
		lResponse.setString("payload", aPayload);

		Token lEnvelope = TokenFactory.createToken(
				"org.jwebsocket.plugins.system", "send");
		lEnvelope.setString("sourceId", mEndPoint.getEndPointId());
		lEnvelope.setString("targetId", aTargetId);
		lEnvelope.setString("action", "forward.json");
		lEnvelope.setBoolean("responseRequested", false);
		lEnvelope.setString("data", JSONProcessor.tokenToPacket(lResponse).getUTF8());

		// send the message in the envelope to the jWebSocket gateway
		sendToken(mEndPoint.getGatewayId(), lEnvelope);
	}

	public void respondPayload(String aTargetId, Token aRequest, int aCode, String aMsg,
			Map aArgs, String aPayload) {

		Token lResponse = TokenFactory.createToken();
		// set args map first, since setMap overwrites all other values
		lResponse.setMap(aArgs);
		// now start to set the actual values
		lResponse.setNS(aRequest.getNS());
		lResponse.setType("response");
		lResponse.setString("reqType", aRequest.getType());
		lResponse.setInteger("utid", aRequest.getInteger("utid"));
		lResponse.setInteger("code", aCode);
		lResponse.setString("msg", aMsg);
		lResponse.setString("payload", aPayload);

		String lSourceId = aRequest.getString("sourceId");
		if (null != lSourceId) {
			lResponse.setString("sourceId", lSourceId);
		}
		String lOriginId = aRequest.getString("originId");
		if (null != lOriginId) {
			lResponse.setString("originId", lOriginId);
		}

		Token lEnvelope = TokenFactory.createToken(
				"org.jwebsocket.plugins.system", "send");
		lEnvelope.setString("sourceId", mEndPoint.getEndPointId());
		lEnvelope.setString("targetId", aTargetId);

		lEnvelope.setString("action", "forward.json");
		lEnvelope.setBoolean("responseRequested", false);
		lEnvelope.setString("data", JSONProcessor.tokenToPacket(lResponse).getUTF8());

		// send the message in the envelope to the jWebSocket gateway
		sendToken(mEndPoint.getGatewayId(), lEnvelope);
	}

	/**
	 * @return the mEndPoint
	 */
	public JMSEndPoint getEndPoint() {
		return mEndPoint;
	}
}
