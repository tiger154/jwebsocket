//	---------------------------------------------------------------------------
//	jWebSocket - JWSEndPointSender (Community Edition, CE)
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Alexander Schulze
 */
public class JWSEndPointSender extends JMSEndPointSender {

	private static final Logger mLog = Logger.getLogger(JWSEndPointSender.class);
	private static final AtomicInteger mUTID = new AtomicInteger(1);

	private int getUTID() {
		mUTID.compareAndSet(Integer.MAX_VALUE, 1);
		return mUTID.getAndIncrement();
	}

	/**
	 *
	 * @param aEndPoint
	 */
	public JWSEndPointSender(JMSEndPoint aEndPoint) {
		super(aEndPoint);
	}

	/**
	 * Sends a JSON token as text message to the given target endpoint.
	 *
	 * @param aTargetId
	 * @param aToken
	 */
	public void sendToken(String aTargetId, Token aToken) {
		sendToken(aTargetId, aToken, null);
	}

	/**
	 * Sends a JSON token as text message to the given target endpoint.
	 *
	 * @param aTargetId
	 * @param aToken
	 * @param aResponseListener
	 */
	public void sendToken(String aTargetId, Token aToken, JWSResponseTokenListener aResponseListener) {
		sendToken(aTargetId, aToken, aResponseListener, 1000 * 10);
	}

	/**
	 * Sends a JSON token as text message to the given target endpoint.
	 *
	 * Example: 	 <code>
	 * sendToken("some id", aToken, new JWSResponseTokenListener(){
	 *
	 * @param aTimeout
	 * @Override public void onSuccess(Token aReponse) {
	 * super.onSuccess(aReponse); }
	 *
	 * @Override public void onFailure(Token aReponse) {
	 * super.onFailure(aReponse); }
	 *
	 * @Override public void onTimeout() { super.onTimeout(); } }, 5000);
	 *
	 * </code>
	 * @param aTargetId
	 * @param aToken
	 * @param aResponseListener
	 */
	public void sendToken(String aTargetId, Token aToken,
			JWSResponseTokenListener aResponseListener, long aTimeout) {
		int lUTID = aToken.getInteger("utid", mUTID.getAndIncrement());
		aToken.setInteger("utid", lUTID);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending token to '" + aTargetId + "': "
					+ (JMSLogging.isFullTextLogging()
					? aToken.toString()
					: aToken.getLogString()));
		}
		// on the JMS Gateway we per defintion/specification only exchange JSON tokens
		sendText(aTargetId, String.valueOf(lUTID),
				JSONProcessor.tokenToPacket(aToken).getUTF8(),
				aResponseListener, aTimeout);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aUTID
	 * @param aOriginId
	 * @param aArgs
	 * @param aPayload
	 * @param aResponseListener
	 */
	public void sendPayload(String aTargetId, String aNS, String aType,
			Integer aUTID, String aOriginId, Map<String, Object> aArgs, String aPayload,
			JWSResponseTokenListener aResponseListener) {
		sendPayload(aTargetId, aNS, aType, aUTID, aOriginId, aArgs, aPayload, aResponseListener, 1000 * 10);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aUTID
	 * @param aOriginId
	 * @param aArgs
	 * @param aPayload
	 * @param aResponseListener
	 * @param aTimeout
	 */
	public void sendPayload(String aTargetId, String aNS, String aType,
			Integer aUTID, String aOriginId, Map<String, Object> aArgs, String aPayload,
			JWSResponseTokenListener aResponseListener, long aTimeout) {

		Token lToken = TokenFactory.createToken();
		lToken.setMap(aArgs);
		lToken.setNS(aNS);
		lToken.setType(aType);
		lToken.setString("sourceId", getEndPoint().getEndPointId());
		lToken.setInteger("utid", aUTID);
		lToken.setString("payload", aPayload);
		if (null != aOriginId) {
			lToken.setString("originId", aOriginId);
		}

		sendToken(aTargetId, lToken, aResponseListener, aTimeout);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aUTID
	 * @param aOriginId
	 * @param aArgs
	 * @param aPayload
	 */
	public void sendPayload(String aTargetId, String aNS, String aType,
			Integer aUTID, String aOriginId, Map<String, Object> aArgs, String aPayload) {
		sendPayload(aTargetId, aNS, aType, aUTID, aOriginId, aArgs, aPayload, null);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aArgs
	 * @param aPayload
	 * @param aResponseListener
	 * @param aTimeout
	 */
	public void sendPayload(String aTargetId, String aNS, String aType,
			Map<String, Object> aArgs, String aPayload,
			JWSResponseTokenListener aResponseListener, long aTimeout) {

		Token lToken = TokenFactory.createToken();
		lToken.setMap(aArgs);
		lToken.setNS(aNS);
		lToken.setType(aType);
		lToken.setString("sourceId", getEndPoint().getEndPointId());
		Integer lUTID = getUTID();
		lToken.setInteger("utid", lUTID);
		lToken.setString("payload", aPayload);
		sendToken(aTargetId, lToken, aResponseListener, aTimeout);
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
			Map<String, Object> aArgs, String aPayload) {
		sendPayload(aTargetId, aNS, aType, getUTID(), null, aArgs, aPayload);
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
			String aOriginId, Map<String, Object> aArgs, String aPayload) {
		sendPayload(aTargetId, aNS, aType, getUTID(), aOriginId, aArgs, aPayload);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aOriginId The original endpoint of the message to be responded to
	 * @param aArgs
	 * @param aPayload
	 * @param aResponseListener
	 * @param aTimeout
	 */
	public void forwardPayload(String aTargetId, String aNS, String aType,
			String aOriginId, Map<String, Object> aArgs, String aPayload,
			JWSResponseTokenListener aResponseListener, long aTimeout) {
		sendPayload(aTargetId, aNS, aType, getUTID(), aOriginId, aArgs,
				aPayload, aResponseListener, aTimeout);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aNS
	 * @param aType
	 * @param aOriginId The original endpoint of the message to be responded to
	 * @param aArgs
	 * @param aPayload
	 * @param aResponseListener
	 */
	public void forwardPayload(String aTargetId, String aNS, String aType,
			String aOriginId, Map<String, Object> aArgs, String aPayload,
			JWSResponseTokenListener aResponseListener) {
		sendPayload(aTargetId, aNS, aType, getUTID(), aOriginId, aArgs,
				aPayload, aResponseListener, 1000 * 10);
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
			Integer aUTID, int aCode, String aMsg, Map<String, Object> aArgs,
			String aPayload) {

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
		lEnvelope.setString("sourceId", getEndPoint().getEndPointId());
		lEnvelope.setString("targetId", aTargetId);
		lEnvelope.setString("action", "forward.json");
		lEnvelope.setBoolean("responseRequested", false);
		lEnvelope.setString("data", JSONProcessor.tokenToPacket(lResponse).getUTF8());

		// send the message in the envelope to the jWebSocket gateway
		sendToken(getEndPoint().getGatewayId(), lEnvelope);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aRequest
	 * @param aCode
	 * @param aMsg
	 * @param aArgs
	 * @param aPayload
	 */
	public void respondPayload(String aTargetId, Token aRequest, int aCode,
			String aMsg, Map<String, Object> aArgs, String aPayload) {

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
		lResponse.setString("endPointId", getEndPointId());

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
		// the sourceId of this message is the endpoint id of this instance
		lEnvelope.setString("sourceId", getEndPoint().getEndPointId());
		// the target id is the endpoint id of the target (websocket or MQ)
		lEnvelope.setString("targetId", aTargetId);

		lEnvelope.setString("action", "forward.json");
		lEnvelope.setBoolean("responseRequested", false);
		lEnvelope.setString("data", JSONProcessor.tokenToPacket(lResponse).getUTF8());

		// send the message in the envelope to the jWebSocket gateway
		String lTargetId = aRequest.getString("gatewayId");
		if (null == lTargetId) {
			lTargetId = getEndPoint().getGatewayId();
		}

		sendToken(lTargetId, lEnvelope);
	}

	/**
	 *
	 * @param aRequest
	 * @param aCode
	 * @param aMsg
	 * @param aArgs
	 * @param aPayload
	 */
	public void respondPayload(Token aRequest, int aCode, String aMsg,
			Map<String, Object> aArgs, String aPayload) {
		respondPayload(aRequest.getString("sourceId"), aRequest, aCode,
				aMsg, aArgs, aPayload);
	}

	/**
	 *
	 * @param aTargetId
	 */
	public void ping(String aTargetId) {
		sendPayload(aTargetId,
				"org.jwebsocket.jms.gateway", // name space
				"ping", // type
				getUTID(), // utid
				null, // origin id
				null, // args
				null // payload
		);
	}

	/**
	 *
	 * @param aTargetId
	 */
	public void getIdentification(String aTargetId) {
		sendPayload(aTargetId,
				"org.jwebsocket.jms.gateway", // name space
				"identify", // type
				getUTID(), // utid
				null, // origin id
				null, // args
				null // payload
		);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aRequest
	 * @param aPercent
	 * @param aCode
	 * @param aMessage
	 * @param aArgs
	 */
	public void sendProgress(String aTargetId, Token aRequest, double aPercent,
			int aCode, String aMessage, Map aArgs) {
		Token lResponse = TokenFactory.createToken();
		// set args map first, since setMap overwrites all other values
		lResponse.setMap(aArgs);
		// now start to set the actual values
		lResponse.setNS(aRequest.getNS());
		lResponse.setType("event");
		lResponse.setString("name", "progress");
		// lResponse.setInteger("utid", -aRequest.getInteger("utid", 0));
		lResponse.setInteger("utid", aRequest.getInteger("utid", 0));
		lResponse.setDouble("percent", aPercent);
		lResponse.setString("msg", aMessage);
		lResponse.setInteger("code", aCode);
		lResponse.setString("endPointId", getEndPointId());

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
		// the sourceId of this message is the endpoint id of this instance
		lEnvelope.setString("sourceId", getEndPoint().getEndPointId());
		// the target id is the endpoint id of the target (websocket or MQ)
		lEnvelope.setString("targetId", aTargetId);

		lEnvelope.setString("action", "forward.json");
		lEnvelope.setBoolean("responseRequested", false);
		lEnvelope.setString("data", JSONProcessor.tokenToPacket(lResponse).getUTF8());

		// send the message in the envelope to the jWebSocket gateway
		String lTargetId = aRequest.getString("gatewayId");
		if (null == lTargetId) {
			lTargetId = getEndPoint().getGatewayId();
		}

		sendToken(lTargetId, lEnvelope);
	}

	/**
	 *
	 * @param aRequest
	 * @param aPercent
	 * @param aCode
	 * @param aMessage
	 * @param aArgs
	 */
	public void sendProgress(Token aRequest, double aPercent,
			int aCode, String aMessage, Map aArgs) {
		if (null != aRequest) {
			String lTargetId = aRequest.getString("sourceId");
			if (null != lTargetId) {
				sendProgress(lTargetId, aRequest, aPercent,
						aCode, aMessage, aArgs);
			}
		}
	}
}
