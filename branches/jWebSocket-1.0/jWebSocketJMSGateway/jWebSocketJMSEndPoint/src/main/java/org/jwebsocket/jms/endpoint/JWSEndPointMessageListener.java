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

import java.util.Map;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class JWSEndPointMessageListener extends JMSEndPointMessageListener {

	static final Logger mLog = Logger.getLogger(JWSEndPointMessageListener.class);
	Map<String, IJWSMessageListener> mRequestListeners = new FastMap<String, IJWSMessageListener>();
	Map<String, IJWSMessageListener> mResponseListeners = new FastMap<String, IJWSMessageListener>();

	/**
	 *
	 * @param aJMSClient
	 */
	public JWSEndPointMessageListener(JMSEndPoint aJMSClient) {
		super(aJMSClient);
	}

	@Override
	public void onTextMessage(TextMessage aMessage) {
		try {
			String lSourceId = aMessage.getStringProperty("sourceId");
			if (mLog.isInfoEnabled()) {
				mLog.info("Received text from '" + lSourceId
						+ "': " + aMessage.getText());
			}
			String lPayload = aMessage.getText();
			Token lToken = JSONProcessor.JSONStringToToken(lPayload);

			// fields for requests
			String lNS = lToken.getNS();
			String lType = lToken.getType();

			if ("response".equals(lType)) {
				// fields for responses
				String lReqType = lToken.getString("reqType", "");
				IJWSMessageListener lListener = mResponseListeners.get(lNS + "." + lReqType);
				if (null != lListener) {
					String lFrom = lToken.getString("sourceId");
					String lTo = lToken.getString("targetId");
					lListener.processMessage(
							lFrom, lTo,
							lNS, lType, null,
							lPayload);
					lListener.processToken(lSourceId, lToken);
				}
			} else if ("event".equals(lType)) {
			} else {
				// check for "ping" request with in the gateway's name space
				if ("org.jwebsocket.jms.gateway".equals(lNS)) {
					if ("ping".equals(lType)) {
						if (mLog.isInfoEnabled()) {
							mLog.info("Responding to ping from '" + lSourceId + "'...");
							getSender().sendText(lToken.getString("sourceId"),
									"{\"ns\":\"org.jwebsocket.jms.gateway\","
									+ "\"type\":\"response\",\"reqType\":\"ping\","
									+ "\"code\":0,\"msg\":\"pong\"}");
						}
					} else if ("identify".equals(lType)) {
						if (mLog.isInfoEnabled()) {
							mLog.info("Responding to identify from '" + lSourceId + "'...");
							getSender().sendText(lToken.getString("sourceId"),
									"{\"ns\":\"org.jwebsocket.jms.gateway\","
									+ "\"type\":\"response\",\"reqType\":\"identify\","
									+ "\"code\":0,\"msg\":\"ok\"," 
									+ "\"endpointId\":\"" + getJMSEndPoint().getEndPointId() + "\"}");
						}
					}
				}
				IJWSMessageListener lListener = mRequestListeners.get(lNS + "." + lType);
				if (null != lListener) {
					lListener.processToken(lSourceId, lToken);
				}
			}

		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on getting text message.");
		}
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @param aListener
	 */
	public void onRequest(String aNS, String aType, IJWSMessageListener aListener) {
		mRequestListeners.put(aNS + "." + aType, aListener);
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @param aListener
	 */
	public void unRequest(String aNS, String aType, IJWSMessageListener aListener) {
		mRequestListeners.remove(aNS + "." + aType);
	}

	/**
	 *
	 * @param aNS
	 * @param aReqType
	 * @param aListener
	 */
	public void onResponse(String aNS, String aReqType, IJWSMessageListener aListener) {
		mResponseListeners.put(aNS + "." + aReqType, aListener);
	}

	/**
	 *
	 * @param aNS
	 * @param aReqType
	 * @param aListener
	 */
	public void unResponse(String aNS, String aReqType, IJWSMessageListener aListener) {
		mResponseListeners.remove(aNS + "." + aReqType);
	}
}