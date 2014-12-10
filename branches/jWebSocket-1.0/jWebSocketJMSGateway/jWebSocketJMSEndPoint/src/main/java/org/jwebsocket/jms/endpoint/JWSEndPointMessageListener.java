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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class JWSEndPointMessageListener extends JMSEndPointMessageListener {

	private static final Logger mLog = Logger.getLogger(JWSEndPointMessageListener.class);
	Map<String, IJWSMessageListener> mRequestListeners = new FastMap<String, IJWSMessageListener>();
	Map<String, IJWSMessageListener> mResponseListeners = new FastMap<String, IJWSMessageListener>();
	List<IJWSMessageListener> mMessageListeners = new FastList<IJWSMessageListener>();

	/**
	 *
	 * @param aJMSClient
	 */
	public JWSEndPointMessageListener(JMSEndPoint aJMSClient) {
		super(aJMSClient);
	}

	@Override
	public void onBytesMessage(BytesMessage aMessage) {
		try {
			String lSourceId = aMessage.getStringProperty("sourceId");
			int lLen = (int) aMessage.getBodyLength();
			byte[] lBA = new byte[lLen];
			aMessage.readBytes(lBA, lLen);
			String lText = new String(lBA);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Received bytes from '" + lSourceId + "': "
						+ (JMSLogging.isFullTextLogging()
								? lText
								: "[content suppressed, length: " + lText.length() + " bytes]]"));
			}
			processText(aMessage, lText);
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on getting bytes message.");
		}
	}

	@Override
	public void onTextMessage(TextMessage aMessage) {
		try {
			String lSourceId = aMessage.getStringProperty("sourceId");
			String lText = aMessage.getText();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Received text from '" + lSourceId + "': "
						+ (JMSLogging.isFullTextLogging()
								? lText
								: "[content suppressed, length: " + lText.length() + " bytes]]"));
			}
			processText(aMessage, lText);
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on getting text message.");
		}
	}

	private void processText(Message aMessage, String aText) {
		try {
			String lSourceId = aMessage.getStringProperty("sourceId");
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing text from '" + lSourceId + "': "
						+ (JMSLogging.isFullTextLogging()
								? aText
								: "[content suppressed, length: " + aText.length() + " bytes]"));
			}
			String lPayload = aText;
			Token lToken = JSONProcessor.JSONStringToToken(lPayload);
			
			// fields for requests
			String lNS = lToken.getNS();
			String lType = lToken.getType();

			// is it a response?
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
				// is it an event?
			} else if ("event".equals(lType)) {
			} else {
				// check for "ping" request with in the gateway's name space
				if ("org.jwebsocket.jms.gateway".equals(lNS)) {
					String lHostname, lCanonicalHostName, lIPAddress;
					try {
						lIPAddress = InetAddress.getLocalHost().getHostAddress();
					} catch (UnknownHostException ex) {
						lIPAddress = null;
					}
					try {
						lHostname = InetAddress.getLocalHost().getHostName();
					} catch (UnknownHostException ex) {
						lHostname = null;
					}
					try {
						lCanonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
					} catch (UnknownHostException ex) {
						lCanonicalHostName = null;
					}
					if ("ping".equals(lType)) {
						String lGatewayId = lToken.getString("gatewayId");
						if (mLog.isInfoEnabled()) {
							mLog.info("Responding to ping from '" + lSourceId + "'"
									+ " " + (null != lGatewayId ? "via '" + lGatewayId + "'" : "directly")
									+ "...");
						}
						String lData = "{\"ns\":\"org.jwebsocket.jms.gateway\""
								+ ",\"type\":\"response\",\"reqType\":\"ping\""
								+ ",\"code\":0,\"msg\":\"pong\",\"utid\":" + lToken.getInteger("utid")
								+ ",\"sourceId\":\"" + getSender().getEndPointId() + "\""
								+ (null != lHostname ? ",\"hostname\":\"" + lHostname + "\"" : "")
								+ (null != lCanonicalHostName ? ",\"canonicalHostname\":\"" + lCanonicalHostName + "\"" : "")
								+ (null != lIPAddress ? ",\"ip\":\"" + lIPAddress + "\"" : "")
								+ (null != lGatewayId ? ",\"gatewayId\":\"" + lGatewayId + "\"" : "")
								+ "}";
						if (null != lGatewayId) {
							getSender().sendText(lGatewayId,
									"{\"ns\":\"org.jwebsocket.plugins.system\",\"action\":\"forward.json\","
									+ "\"type\":\"send\",\"sourceId\":\"" + lToken.getString("targetId") + "\","
									+ "\"targetId\":\"" + lToken.getString("sourceId") + "\",\"responseRequested\":false,"
									+ "\"data\": \"" + lData.replace("\"", "\\\"") + "\"}");
						} else {
							getSender().sendText(lToken.getString("sourceId"), lData);
						}
					} else if ("identify".equals(lType)) {
						String lGatewayId = lToken.getString("gatewayId");
						if (mLog.isInfoEnabled()) {
							mLog.info("Responding to identify from '" + lSourceId + "'"
									+ " " + (null != lGatewayId ? "via '" + lGatewayId + "'" : "directly")
									+ "...");
						}
						String lData = "{\"ns\":\"org.jwebsocket.jms.gateway\""
								+ ",\"type\":\"response\",\"reqType\":\"identify\""
								+ ",\"code\":0,\"msg\":\"ok\",\"utid\":" + lToken.getInteger("utid")
								+ ",\"sourceId\":\"" + getSender().getEndPointId() + "\""
								+ (null != lHostname ? ",\"hostname\":\"" + lHostname + "\"" : "")
								+ (null != lCanonicalHostName ? ",\"canonicalHostname\":\"" + lCanonicalHostName + "\"" : "")
								+ (null != lIPAddress ? ",\"ip\":\"" + lIPAddress + "\"" : "")
								+ (null != lGatewayId ? ",\"gatewayId\":\"" + lGatewayId + "\"" : "")
								+ "}";
						if (null != lGatewayId) {
							getSender().sendText(lGatewayId,
									"{\"ns\":\"org.jwebsocket.plugins.system\",\"action\":\"forward.json\","
									+ "\"type\":\"send\",\"sourceId\":\"" + lToken.getString("targetId") + "\","
									+ "\"targetId\":\"" + lToken.getString("sourceId") + "\",\"responseRequested\":false,"
									+ "\"data\":\"" + lData.replace("\"", "\\\"") + "\"}");
						} else {
							getSender().sendText(lToken.getString("sourceId"), lData);
						}
					}
				}

				// listeners for all messages
				IJWSMessageListener lListener;
				Iterator<IJWSMessageListener> lIterator = mMessageListeners.iterator();
				while (lIterator.hasNext()) {
					lListener = lIterator.next();
					try {
						lListener.processToken(lSourceId, lToken);
					} catch (Exception lEx) {
						mLog.error(lEx.getClass().getSimpleName() + " processing message.");
					}
				}

				// listeners for all requests
				lListener = mRequestListeners.get(lNS + "." + lType);
				if (null != lListener) {
					try {
						lListener.processToken(lSourceId, lToken);
					} catch (Exception lEx) {
						if (getSender() instanceof JWSEndPointSender) {
							((JWSEndPointSender) getSender()).respondPayload(
									lToken,
									-1, // return code
									lEx.getClass().getSimpleName() + ": "
									+ lEx.getMessage(), // return message
									null,
									null);
						}
					}
				}
			}

		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on getting text message.");
		}
	}

	/**
	 * Deprecated, don't use this method anymore!
	 *
	 * @param aNS
	 * @param aType
	 * @param aListener
	 *
	 * @deprecated
	 */
	@Deprecated
	public void onRequest(String aNS, String aType, IJWSMessageListener aListener) {
		addRequestListener(aNS, aType, aListener);
	}

	/**
	 * Adds a new listener to listen to requests from other endpoints. You can
	 * only assign one request listener per name space / type combination. If
	 * you register a listener that already exists, this will overwrite the
	 * previous one. A request token usually contains a name space, a type, a
	 * unique token-id an additional arbitrary payload. The entire token is
	 * passed to the listener to process it according to the application needs.
	 *
	 * @param aNS the name space of the request to listen on
	 * @param aType the type of the request to listen on
	 * @param aListener the actual listener instance, must implement the
	 * IJWSMessageListener interface
	 */
	public void addRequestListener(String aNS, String aType,
			IJWSMessageListener aListener) {
		mRequestListeners.put(aNS + "." + aType, aListener);
	}

	/**
	 * Removes an existing request listener from the endpoint. After that the
	 * listener is not invoked anymore, such that the application will not react
	 * anymore on the specified requests.
	 *
	 * @param aNS the name space of the request to listen on
	 * @param aType the type of the request to listen on
	 * @return the reference the listener object if such, otherwise
	 * <tt>null</tt>.
	 */
	public IJWSMessageListener removeRequestListener(String aNS, String aType) {
		return mRequestListeners.remove(aNS + "." + aType);
	}

	/**
	 * Checks if a request listener for a certain name space / type combination
	 * is already registered for the endpoint.
	 *
	 * @param aNS the name space of the request to listen on
	 * @param aType the type of the request to listen on
	 * @return <tt>true</tt>if already a listener exists with the given name
	 * space / type combination, otherwise <tt>false</tt>.
	 */
	public boolean hasRequestListener(String aNS, String aType) {
		return mRequestListeners.containsKey(aNS + "." + aType);
	}

	/**
	 * Deprecated, don't use this method anymore!
	 *
	 * @param aNS
	 * @param aReqType
	 * @param aListener
	 *
	 * @deprecated
	 */
	@Deprecated
	public void onResponse(String aNS, String aReqType, IJWSMessageListener aListener) {
		addResponseListener(aNS, aReqType, aListener);
	}

	/**
	 * Adds a new listener to listen to responses from other endpoints. You can
	 * only assign one response listener per name space / type combination. If
	 * you register a listener that already exists, this will overwrite the
	 * previous one. A response token usually contains a name space, a reqType,
	 * a unique token-id, the type "response" and an additional arbitrary
	 * payload. The entire token is passed to the listener to process it
	 * according to the application needs.
	 *
	 * @param aNS the name space of the request to listen on
	 * @param aReqType the type of the response (type of the previous request)
	 * to listen on
	 * @param aListener the actual listener instance, must implement the
	 * IJWSMessageListener interface
	 */
	public void addResponseListener(String aNS, String aReqType, IJWSMessageListener aListener) {
		mResponseListeners.put(aNS + "." + aReqType, aListener);
	}

	/**
	 * Removes an existing response listener from the endpoint. After that the
	 * listener is not invoked anymore, such that the application will not react
	 * anymore on the specified responses.
	 *
	 * @param aNS the name space of the request to listen on
	 * @param aReqType the type of the response (type of the previous request)
	 * to listen on
	 * @return the reference the listener object if such, otherwise
	 * <tt>null</tt>.
	 */
	public IJWSMessageListener removeResponseListener(String aNS, String aReqType) {
		return mResponseListeners.remove(aNS + "." + aReqType);
	}

	/**
	 * Checks if a response listener for a certain name space / type combination
	 * is already registered for the endpoint.
	 *
	 * @param aNS the name space of the request to listen on
	 * @param aReqType the type of the request to listen on
	 * @return <tt>true</tt>if already a listener exists with the given name
	 * space / type combination, otherwise <tt>false</tt>.
	 */
	public boolean hasResponseListener(String aNS, String aReqType) {
		return mResponseListeners.containsKey(aNS + "." + aReqType);
	}

	/**
	 * Adds a new listener to listen on any message from other endpoints. It is
	 * up to the application to process the message, which can be a request, a
	 * response or any other asynchronous message, e.g. a progress event. The
	 * incoming token is passed to the listener instance.
	 *
	 * @param aListener the actual listener instance, must implement the
	 * IJWSMessageListener interface
	 */
	public void addMessageListener(IJWSMessageListener aListener) {
		mMessageListeners.add(aListener);
	}

	/**
	 * Sends a token to the endpoint addressed by the given target-id.
	 *
	 * @param aTargetId the id of the target endpoint.
	 * @param aToken the token to be send to the target endpoint.
	 */
	public void sendToken(String aTargetId, Token aToken) {
		getSender().sendText(aTargetId, JSONProcessor.tokenToPacket(aToken).getUTF8());
	}

}
