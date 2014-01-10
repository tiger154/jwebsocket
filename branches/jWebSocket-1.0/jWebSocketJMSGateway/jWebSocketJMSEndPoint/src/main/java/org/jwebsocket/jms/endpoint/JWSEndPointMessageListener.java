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
 * @author aschulze
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
				lListener = mRequestListeners.get(lNS + "." + lType);
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
	 *
	 * @deprecated
	 */
	@Deprecated
	public void onRequest(String aNS, String aType, IJWSMessageListener aListener) {
		addRequestListener(aNS, aType, aListener);
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @param aListener
	 */
	public void addRequestListener(String aNS, String aType, IJWSMessageListener aListener) {
		mRequestListeners.put(aNS + "." + aType, aListener);
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @return
	 */
	public IJWSMessageListener removeRequestListener(String aNS, String aType) {
		return mRequestListeners.remove(aNS + "." + aType);
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @return
	 */
	public boolean hasRequestListener(String aNS, String aType) {
		return mRequestListeners.containsKey(aNS + "." + aType);
	}

	/**
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
	 *
	 * @param aNS
	 * @param aReqType
	 * @param aListener
	 */
	public void addResponseListener(String aNS, String aReqType, IJWSMessageListener aListener) {
		mResponseListeners.put(aNS + "." + aReqType, aListener);
	}

	/**
	 *
	 * @param aNS
	 * @param aReqType
	 * @return
	 */
	public IJWSMessageListener removeResponseListener(String aNS, String aReqType) {
		return mResponseListeners.remove(aNS + "." + aReqType);
	}

	/**
	 *
	 * @param aNS
	 * @param aReqType
	 * @return
	 */
	public boolean hasResponseListener(String aNS, String aReqType) {
		return mResponseListeners.containsKey(aNS + "." + aReqType);
	}

	/**
	 *
	 * @param aListener
	 */
	public void addMessageListener(IJWSMessageListener aListener) {
		mMessageListeners.add(aListener);
	}
}
