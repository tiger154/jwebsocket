//	---------------------------------------------------------------------------
//	jWebSocket - Base Connector Implementation (Community Edition, CE)
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
package org.jwebsocket.connectors;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.kit.*;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.MessagingControl;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * Provides the basic implementation of the jWebSocket connectors. The
 * {@literal BaseConnector} is supposed to be used as ancestor for the connector
 * implementations like e.g. the {@literal TCPConnector} or the {@literal TomcatConnector
 * }.
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 * @author Rolando Betancourt Toucet
 */
public class BaseConnector implements WebSocketConnector {

	/**
	 * Default reserved name for shared custom variable <tt>username</tt>.
	 */
	public final static String VAR_USERNAME = WebSocketSession.USERNAME;
	/**
	 * Default reserved name for shared custom variable <tt>subprot</tt>.
	 */
	public final static String VAR_SUBPROT = "$subprot";
	/**
	 * Default reserved name for shared custom variable <tt>version</tt>.
	 */
	public final static String VAR_VERSION = "$version";
	/**
	 * Default name for shared custom variable <tt>nodeid</tt>.
	 */
	public final static String VAR_NODEID = "$nodeid";
	/**
	 * Is connector using SSL encryption?
	 */
	private boolean mIsSSL = false;
	/*
	 * Status of the connector.
	 */
	private volatile WebSocketConnectorStatus mStatus = WebSocketConnectorStatus.DOWN;
	/**
	 * The WebSocket protocol version.
	 */
	private int mVersion = JWebSocketCommonConstants.WS_VERSION_DEFAULT;
	/**
	 * Backward reference to the engine of this connector.
	 */
	private WebSocketEngine mEngine = null;
	/**
	 * Backup of the original request header and it's fields. TODO: maybe
	 * obsolete for the future
	 */
	private RequestHeader mHeader = null;
	/**
	 * Session object for the WebSocket connection.
	 */
	private final WebSocketSession mSession = new WebSocketSession();
	/**
	 * Shared Variables container for this connector.
	 */
	protected Map<String, Object> mCustomVars = new FastMap<String, Object>();
	private final Object mWriteLock = new Object();
	private final Object mReadLock = new Object();
	Logger mLog = Logging.getLogger();
	/**
	 * Variables for the packet delivery mechanism
	 */
	private static final Map<String, IPacketDeliveryListener> mPacketDeliveryListeners
			= new FastMap<String, IPacketDeliveryListener>().shared();

	/**
	 *
	 * @param aEngine
	 */
	public BaseConnector(WebSocketEngine aEngine) {
		mEngine = aEngine;
	}

	@Override
	public void startConnector() {
		if (getMaxFrameSize() < 1024 * 2) {
			// minimum frame size is required to establish a connection
			// reject connection at this point
			stopConnector(CloseReason.SERVER);
			return;
		}

		// sending agreed max frame size to the client
		sendMessage(MessagingControl.buildMaxFrameSizeMessage(getMaxFrameSize()), null);

		// notifying connector started
		if (mEngine != null) {
			mEngine.connectorStarted(this);
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mEngine != null) {
			mEngine.connectorStopped(this, aCloseReason);
		}
	}

	/**
	 * Returns the current status for the connector. Please refer to the
	 * WebSocketConnectorStatus enumeration.
	 *
	 * @return
	 */
	@Override
	public WebSocketConnectorStatus getStatus() {
		return mStatus;
	}

	/**
	 * Sets the current status for the connector. Please refer to the
	 * WebSocketConnectorStatus enumeration.
	 *
	 * @param aStatus
	 */
	@Override
	public void setStatus(WebSocketConnectorStatus aStatus) {
		mStatus = aStatus;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Object getWriteLock() {
		return mWriteLock;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Object getReadLock() {
		return mReadLock;
	}

	@Override
	public boolean supportTokens() {
		if (null == mSupportTokens) {
			String lFormat = mHeader.getFormat();
			mSupportTokens = (lFormat != null)
					&& (lFormat.equals(JWebSocketCommonConstants.WS_FORMAT_JSON)
					|| lFormat.equals(JWebSocketCommonConstants.WS_FORMAT_XML)
					|| lFormat.equals(JWebSocketCommonConstants.WS_FORMAT_CSV));
		}

		return mSupportTokens;
	}
	private Boolean mSupportTokens = null;

	@Override
	public void processPacket(WebSocketPacket aDataPacket) {
		// if the connector is internal, deliver packet directly
		if (this.isInternal()) {
			((InternalConnector) this).handleIncomingPacket(aDataPacket);
			return;
		}

		if (!WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())) {
			Token lMessage = JSONProcessor.packetToToken(aDataPacket);

			// process only if is control message
			if (null != lMessage && lMessage.getBoolean(MessagingControl.PROPERTY_IS_WRAPPED_MESSAGE, false)) {
				if (MessagingControl.TYPE_MESSAGE.equals(lMessage.getString(MessagingControl.PROPERTY_TYPE))) {
					String lMsgId = lMessage.getString(MessagingControl.PROPERTY_MESSAGE_ID);

					// supporting message delivery notification if required
					if (lMessage.getBoolean(MessagingControl.PROPERTY_IS_ACK_REQUIRED, false)) {
						sendMessage(MessagingControl.buildAckMessage(lMsgId), null);
					}

					// supporting fragmentation
					if (lMessage.getBoolean(MessagingControl.PROPERTY_IS_FRAGMENT, false)) {
						// getting the session storage to allocate fragments
						Map<String, Object> lStorage = getSession().getStorage();
						// getting the fragmentation key for session storage
						String lFragmentationId = "FRAGMENTS" + lMessage.getString(MessagingControl.PROPERTY_FRAGMENTATION_ID);
						// getting the data
						String lData = lMessage.getString(MessagingControl.PROPERTY_DATA);

						if (lMessage.getBoolean(MessagingControl.PROPERTY_IS_LAST_FRAGMENT, false)) {
							// setting new packet value
							aDataPacket.setString(lStorage.remove(lFragmentationId) + lData);
						} else {
							if (!lStorage.containsKey(lFragmentationId)) {
								lStorage.put(lFragmentationId, lData);
							} else {
								lStorage.put(lFragmentationId, lStorage.get(lFragmentationId) + lData);
							}

							return;
						}
					} else {
						// setting new packet value
						aDataPacket.setString(lMessage.getString(MessagingControl.PROPERTY_DATA));
					}

				} else if (MessagingControl.TYPE_INFO.equals(lMessage.getString(MessagingControl.PROPERTY_TYPE))) {
					String lName = lMessage.getString(MessagingControl.PROPERTY_NAME);

					// processing packet delivery acknowledge from the client
					if (MessagingControl.NAME_MESSAGE_DELIVERY_ACKNOWLEDGE.equals(lName)) {
						IPacketDeliveryListener lListener = mPacketDeliveryListeners
								.remove(lMessage.getString(MessagingControl.PROPERTY_DATA));
						if (null != lListener) {
							try {
								lListener.OnSuccess();
							} catch (Exception lEx) {
								mLog.info(Logging.getSimpleExceptionMessage(lEx, "calling packet delivery callback"));
							}
						}
					}

					return;
				}
			}
		}

		// notifying engine
		if (mEngine != null) {
			mEngine.processPacket(this, aDataPacket);
		}
	}

	@Override
	public void processPing(WebSocketPacket aDataPacket) {
		/*
		 * if (mEngine != null) { mEngine.processPing(this,
		 * aDataPacket); }
		 */
	}

	@Override
	public void processPong(WebSocketPacket aDataPacket) {
		/*
		 * if (mEngine != null) { mEngine.processPong(this,
		 * aDataPacket); }
		 */
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener) {
		sendPacketInTransaction(aDataPacket, getMaxFrameSize(), aListener);
	}

	@Override
	public void sendPacketInTransaction(final WebSocketPacket aDataPacket,
			final Integer aFragmentSize, final IPacketDeliveryListener aListener) {

		// if the connector is internal, deliver the packet directly
		if (isInternal()) {
			((InternalConnector) this).handleIncomingPacket(aDataPacket);
			aListener.OnSuccess();
			return;
		}

		// ommiting control frames
		if (WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())
				|| WebSocketFrameType.TEXT.equals(aDataPacket.getFrameType())) {

			try {
				Assert.isTrue(aDataPacket.size() <= getMaxFrameSize(),
						"The packet size exceeds the max frame size supported by the client!");

				Assert.isTrue(aFragmentSize > 0 && aFragmentSize <= getMaxFrameSize(), "Invalid 'fragment size' argument! "
						+ "Expected: fragment_size > 0 && fragment_size <= MAX_FRAME_SIZE");

				// processing fragmentation
				if (aFragmentSize < aDataPacket.size() && aFragmentSize < getMaxFrameSize()) {
					// first fragment is never the last
					boolean lIsLast = false;

					// unique fragmentation id, allows multiplexing
					final String lFragmentationId = MessagingControl.getFragmentationID();

					// creating a special fragment message
					Token lFragment = MessagingControl.buildFragmentMessage(
							JWebSocketConfig.getConfig().getNodeId(),
							lFragmentationId, lIsLast, Arrays
							.copyOfRange(aDataPacket.getByteArray(), 0, aFragmentSize));

					// sending fragments
					final long lSentTime = System.currentTimeMillis();
					sendMessage(lFragment, new IPacketDeliveryListener() {
						private int mBytesSent = 0;

						@Override
						public long getTimeout() {
							long lTimeout = lSentTime + aListener.getTimeout() - System.currentTimeMillis();
							if (lTimeout < 0) {
								lTimeout = 0;
							}

							return lTimeout;
						}

						@Override
						public void OnTimeout() {
							aListener.OnTimeout();
						}

						@Override
						public void OnSuccess() {
							// updating bytes sent
							mBytesSent += aFragmentSize;
							if (mBytesSent >= aDataPacket.size()) {
								// calling success if the packet was transmitted complete
								aListener.OnSuccess();
							} else {
								// prepare to sent a next fragment
								int lLength = (aFragmentSize + mBytesSent <= aDataPacket.size())
										? aFragmentSize
										: aDataPacket.size() - mBytesSent;

								byte[] lBytes = Arrays.copyOfRange(aDataPacket.getByteArray(), mBytesSent, mBytesSent + lLength);
								boolean lIsLast = (lLength + mBytesSent == aDataPacket.size());

								// send next fragment
								Token lMessage = MessagingControl.buildFragmentMessage(
										JWebSocketConfig.getConfig().getNodeId(),
										lFragmentationId, lIsLast, lBytes);

								sendMessage(lMessage, this);
							}
						}

						@Override
						public void OnFailure(Exception lEx) {
							aListener.OnFailure(lEx);
						}
					});

					// stop flow at this point
					return;
				}

				// normal send
				sendMessage(MessagingControl.buildMessage(
						JWebSocketConfig.getConfig().getNodeId(),
						aDataPacket.getByteArray()),
						aListener);

			} catch (Exception lEx) {
				aListener.OnFailure(lEx);
			}
		} else {
			aListener.OnFailure(new Exception("Control frames cannot be sent in transaction!"));
		}
	}

	private void sendMessage(Token aMessage, IPacketDeliveryListener aListener) {
		if (null != aListener) {
			aMessage.setBoolean(MessagingControl.PROPERTY_IS_ACK_REQUIRED, true);
			aMessage.setBoolean(MessagingControl.PROPERTY_IS_WRAPPED_MESSAGE, true);
			final String lMsgId = aMessage.getString(MessagingControl.PROPERTY_MESSAGE_ID);
			mPacketDeliveryListeners.put(lMsgId, aListener);

			// schedule the timer task
			try {
				Tools.getTimer().schedule(new JWSTimerTask() {
					@Override
					public void runTask() {
						Tools.getThreadPool().submit(new Runnable() {
							@Override
							public void run() {
								IPacketDeliveryListener lListener = mPacketDeliveryListeners.remove(lMsgId);
								if (null != lListener) {
									lListener.OnTimeout();
								}
							}
						});
					}
				}, aListener.getTimeout());
			} catch (IllegalStateException lEx) {
				// nothing, task was cancelled
			}
		}

		sendPacket(JSONProcessor.tokenToPacket(aMessage));
	}

	@Override
	public void sendPacket(WebSocketPacket aDataPacket) {
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
		return null;
	}

	@Override
	public WebSocketEngine getEngine() {
		return mEngine;
	}

	@Override
	public RequestHeader getHeader() {
		return mHeader;
	}

	/**
	 * @param aHeader the header to set
	 */
	@Override
	public void setHeader(RequestHeader aHeader) {
		// TODO: the sub protocol should be a connector variable! not part of
		// the header!
		this.mHeader = aHeader;
		// TODO: this can be improved, maybe distinguish between header and URL
		// args!
		Map lArgs = aHeader.getArgs();
		if (lArgs != null) {
			String lNodeId = (String) lArgs.get("unid");
			if (lNodeId != null) {
				setNodeId(lNodeId);
				lArgs.remove("unid");
			}
		}
	}

	@Override
	public Object getVar(String aKey) {
		return mCustomVars.get(aKey);
	}

	@Override
	public void setVar(String aKey, Object aValue) {
		mCustomVars.put(aKey, aValue);
	}

	@Override
	public Boolean getBoolean(String aKey) {
		return (Boolean) getVar(aKey);
	}

	@Override
	public boolean getBool(String aKey) {
		Boolean lBool = getBoolean(aKey);
		return (lBool != null && lBool);
	}

	@Override
	public void setBoolean(String aKey, Boolean aValue) {
		setVar(aKey, aValue);
	}

	@Override
	public String getString(String aKey) {
		return (String) getVar(aKey);
	}

	@Override
	public void setString(String aKey, String aValue) {
		setVar(aKey, aValue);
	}

	@Override
	public Integer getInteger(String aKey) {
		return (Integer) getVar(aKey);
	}

	@Override
	public void setInteger(String aKey, Integer aValue) {
		setVar(aKey, aValue);
	}

	@Override
	public void removeVar(String aKey) {
		mCustomVars.remove(aKey);
	}

	@Override
	public String generateUID() {
		return null;
	}

	@Override
	public int getRemotePort() {
		return -1;
	}

	@Override
	public InetAddress getRemoteHost() {
		return null;
	}
	private String mUniqueId = null;
	private static final AtomicLong mCounter = new AtomicLong(0);

	@Override
	public String getId() {
		if (null == mUniqueId) {
			mCounter.compareAndSet(Long.MAX_VALUE, 0);
			String lNodeId = JWebSocketConfig.getConfig().getNodeId();
			mUniqueId = ((lNodeId != null && lNodeId.length() > 0) ? lNodeId + "." : "")
					+ String.valueOf(getRemotePort())
					+ "." + mCounter.incrementAndGet();
		}
		return mUniqueId;
	}

	@Override
	public WebSocketSession getSession() {
		return mSession;
	}

	/**
	 *
	 * @return
	 */
	public Map<String, Object> getVars() {
		return mCustomVars;
	}

	// some convenience methods to easier process username (login-status)
	// and configured unique node id for clusters (independent from tcp port)
	@Override
	public String getUsername() {
		return getSession().getUsername();
	}

	@Override
	public void setUsername(String aUsername) {
		Map<String, Object> lStorage = getSession().getStorage();
		if (null != lStorage) {
			lStorage.put(VAR_USERNAME, aUsername);
		}
	}

	@Override
	public void removeUsername() {
		Map<String, Object> lStorage = getSession().getStorage();
		if (null != lStorage) {
			lStorage.remove(VAR_USERNAME);
		}
	}

	// some convenience methods to easier process subprot (login-status)
	// and configured unique node id for clusters (independent from tcp port)
	@Override
	public String getSubprot() {
		return getString(BaseConnector.VAR_SUBPROT);
	}

	@Override
	public void setSubprot(String aSubprot) {
		setString(BaseConnector.VAR_SUBPROT, aSubprot);
	}

	@Override
	public int getVersion() {
		return mVersion;
	}

	@Override
	public void setVersion(int aVersion) {
		mVersion = aVersion;
	}

	@Override
	public void removeSubprot() {
		removeVar(BaseConnector.VAR_SUBPROT);
	}

	@Override
	public boolean isLocal() {
		// TODO: This has to be updated for the cluster approach
		return true;
	}

	@Override
	public boolean isInternal() {
		return false;
	}

	@Override
	public String getNodeId() {
		return getString(BaseConnector.VAR_NODEID);
	}

	@Override
	public void setNodeId(String aNodeId) {
		setString(BaseConnector.VAR_NODEID, aNodeId);
	}

	@Override
	public void removeNodeId() {
		removeVar(BaseConnector.VAR_NODEID);
	}

	@Override
	public boolean isSSL() {
		return mIsSSL;
	}

	@Override
	public void setSSL(boolean aIsSSL) {
		mIsSSL = aIsSSL;
	}

	@Override
	public boolean isHixie() {
		return WebSocketProtocolAbstraction.isHixieVersion(getVersion());
	}

	@Override
	public boolean isHybi() {
		return WebSocketProtocolAbstraction.isHybiVersion(getVersion());
	}

	@Override
	public Integer getMaxFrameSize() {
		Integer lMaxFrameSize = mEngine.getConfiguration().getMaxFramesize();
		try {
			Map lArgs = mHeader.getArgs();
			if (null != lArgs) {
				Integer lArgMaxFrameSize = Integer.parseInt((String) mHeader.getArgs().get(MessagingControl.PROPERTY_MAX_FRAME_SIZE));
				if (lArgMaxFrameSize <= lMaxFrameSize) {
					return lArgMaxFrameSize;
				}
			}
		} catch (NumberFormatException lEx) {
		}
		return lMaxFrameSize;
	}

	/**
	 *
	 * @param aPacket
	 * @throws Exception
	 */
	protected void checkBeforeSend(WebSocketPacket aPacket) throws Exception {
		Assert.isTrue(getMaxFrameSize() >= aPacket.size(),
				"The packet size exceeds the connector supported 'max frame size' value!");
	}
}
