//	---------------------------------------------------------------------------
//	jWebSocket - Base Connector Implementation (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javolution.util.FastMap;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.InFragmentationListenerSender;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.kit.*;
import org.jwebsocket.util.Fragmentation;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * Provides the basic implementation of the jWebSocket connectors. The {@code BaseConnector}
 * is supposed to be used as ancestor for the connector implementations like
 * e.g. the
 * {@code TCPConnector} or the {@code TomcatConnector }.
 *
 * @author aschulze
 * @author kyberneees
 * @author rbetancourt
 */
public class BaseConnector implements WebSocketConnector {

	/**
	 * Default reserved name for shared custom variable <tt>username</tt>.
	 */
	public final static String VAR_USERNAME = "$username";
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
	/*
	 *
	 */
	private final String SESSION_UID = "session_uid";
	/**
	 * Shared Variables container for this connector.
	 */
	private final Map<String, Object> mCustomVars = new FastMap<String, Object>();
	private final Object mWriteLock = new Object();
	private final Object mReadLock = new Object();
	/**
	 * Variables for the packet delivery mechanism
	 */
	private static Map<Integer, IPacketDeliveryListener> mPacketDeliveryListeners =
		new FastMap<Integer, IPacketDeliveryListener>().shared();

	/**
	 *
	 * @param aEngine
	 */
	public BaseConnector(WebSocketEngine aEngine) {
		mEngine = aEngine;
	}

	@Override
	public void startConnector() {
		if (getMaxFrameSize() < Fragmentation.MIN_FRAME_SIZE) {
			// minimum frame size is required to establish a connection
			// reject connection at this point
			stopConnector(CloseReason.SERVER);
			return;
		}

		// sending agreed max frame size to the client
		sendPacket(new RawPacket(Fragmentation.ARG_MAX_FRAME_SIZE + getMaxFrameSize()));

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
			if ((lFormat != null)
				&& (lFormat.equals(JWebSocketCommonConstants.WS_FORMAT_JSON)
				|| lFormat.equals(JWebSocketCommonConstants.WS_FORMAT_XML)
				|| lFormat.equals(JWebSocketCommonConstants.WS_FORMAT_CSV))) {
				mSupportTokens = true;
			} else {
				mSupportTokens = false;
			}
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
			// processing packet delivery acknowledge from the client
			//MAX_INTEGER_TO_STRING_SIZE + PREFIX_SIZE
			if (aDataPacket.size() <= (10 + Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX.length())) {
				String lContent = aDataPacket.getString();
				if (lContent.startsWith(Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX)) {
					try {
						// getting the delivered packet id
						Integer lPID = Integer.parseInt(lContent.replace(Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX, ""));
						IPacketDeliveryListener lListener = mPacketDeliveryListeners.remove(lPID);
						if (null != lListener) {
							lListener.OnSuccess();
						}
					} catch (NumberFormatException lEx) {
					}
					// do not process the packet because it is a delivery acknowledge
					return;
				}
			}

			// shared temp vars
			String lData = aDataPacket.getString();
			int lPos;
			String lPID;

			// supporting packet delivery acknowledge to the client
			lPos = lData.indexOf(Fragmentation.PACKET_ID_DELIMETER);

			if (lPos >= 0 && lPos <= 10) {
				try {
					Integer lPacketId = Integer.parseInt(lData.substring(0, lPos));
					aDataPacket.setString(lData.substring(lPos + 1));

					// sending acknowledge packet
					sendPacket(new RawPacket(Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX + lPacketId.toString()));
				} catch (NumberFormatException lEx) {
				}
			}

			// supporting fragmentation
			lData = aDataPacket.getString();
			String lFragmentContent;
			Map<String, Object> lStorage;
			if (lData.startsWith(Fragmentation.PACKET_FRAGMENT_PREFIX)) {
				lStorage = getSession().getStorage();
				lPos = lData.indexOf(Fragmentation.PACKET_ID_DELIMETER);
				lPID = lData.substring(0, lPos);
				lFragmentContent = lData.substring(lPos + 1);

				// storing the packet fragment
				// lPID = PACKET_FRAGMENT_PREFIX + FRAGMENTATION_ID
				if (!lStorage.containsKey(lPID)) {
					lStorage.put(lPID, lFragmentContent);
				} else {
					lStorage.put(lPID, lStorage.get(lPID) + lFragmentContent);
				}

				// do not process fragment packets
				return;
			} else if (lData.startsWith(Fragmentation.PACKET_LAST_FRAGMENT_PREFIX)) {
				lStorage = getSession().getStorage();
				lPos = lData.indexOf(Fragmentation.PACKET_ID_DELIMETER);
				lPID = lData.substring(Fragmentation.PACKET_LAST_FRAGMENT_PREFIX.length(), lPos);
				lFragmentContent = lData.substring(lPos + 1);

				// getting the complete packet content
				// lPID = PACKET_FRAGMENT_PREFIX + FRAGMENTATION_ID
				aDataPacket.setString(lStorage.remove(Fragmentation.PACKET_FRAGMENT_PREFIX + lPID) + lFragmentContent);
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
		Integer aFragmentSize, final IPacketDeliveryListener aListener) {

		// if the connector is internal, deliver the packet directly
		if (isInternal()) {
			((InternalConnector) this).handleIncomingPacket(aDataPacket);
			aListener.OnSuccess();
			return;
		}

		// getting packet identifier
		final Integer lPacketId = Fragmentation.generateUID();

		// ommiting control frames
		if (WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())
			|| WebSocketFrameType.TEXT.equals(aDataPacket.getFrameType())) {

			String lPacketPrefix = lPacketId.toString() + Fragmentation.PACKET_ID_DELIMETER;

			try {
				Assert.isTrue(lPacketPrefix.length() + aDataPacket.size() <= getMaxFrameSize(),
					"The packet size exceeds the max frame size supported by the client! "
					+ "Consider that the packet has been prefixed with " + lPacketPrefix.length()
					+ " bytes for transaction.");

				Assert.isTrue(aFragmentSize > 0 && aFragmentSize <= getMaxFrameSize(), "Invalid 'fragment size' argument! "
					+ "Expected: fragment_size > 0 && fragment_size <= MAX_FRAME_SIZE");

				// processing fragmentation
				if (aFragmentSize < aDataPacket.size() && aFragmentSize < getMaxFrameSize()) {
					// first fragment is never the last
					boolean lIsLast = false;

					// fragmentation id, allows multiplexing
					int lFragmentationId = Fragmentation.generateUID();

					// creating a special packet for fragmentation
					WebSocketPacket lPacketFragmented = Fragmentation.createFragmentedPacket(
						Arrays.copyOfRange(aDataPacket.getByteArray(), 0, aFragmentSize),
						lFragmentationId, lIsLast);

					// checking the new packet size
					Assert.isTrue(lPacketFragmented.size() + lPacketPrefix.length() <= getMaxFrameSize(),
						"The packet size exceeds the max frame size supported by the client! "
						+ "Consider that the packet has been prefixed with "
						+ (lPacketFragmented.size() + lPacketPrefix.length() - aDataPacket.size())
						+ " bytes for fragmented transaction.");

					// process fragmentation
					final BaseConnector lSender = this;
					sendPacketInTransaction(
						lPacketFragmented,
						// passing special listener for fragmentation
						Fragmentation.createListener(
						aDataPacket,
						aFragmentSize,
						aListener,
						System.currentTimeMillis(),
						lFragmentationId,
						new InFragmentationListenerSender() {

							@Override
							public void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener) {
								lSender.sendPacketInTransaction(aDataPacket, aListener);
							}
						}));
					return;
				}

				// preparing to send in transaction
				byte[] lPrefixBytes = lPacketPrefix.getBytes();
				// adding the prefix to the packet content
				ByteBuffer lBuffer = ByteBuffer.allocate(lPrefixBytes.length + aDataPacket.size());
				lBuffer.put(lPrefixBytes);
				lBuffer.put(aDataPacket.getByteArray());

				// setting the new packet content
				aDataPacket.setByteArray(lBuffer.array());

				// saving the delivery listener
				mPacketDeliveryListeners.put(lPacketId, aListener);

				// sending the packet to the client
				sendPacket(aDataPacket);

				// schedule the timer task
				try {
					Tools.getTimer().schedule(new TimerTask() {

						@Override
						public void run() {
							Tools.getThreadPool().submit(new Runnable() {

								@Override
								public void run() {
									IPacketDeliveryListener lListener = mPacketDeliveryListeners.remove(lPacketId);
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
			} catch (Exception lEx) {
				mPacketDeliveryListeners.remove(lPacketId);
				aListener.OnFailure(lEx);
			}
		} else {
			aListener.OnFailure(new Exception("Control frames cannot be sent in transaction!"));
		}
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
	private static AtomicLong mCounter = new AtomicLong(0);

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
		return getString(BaseConnector.VAR_USERNAME);
	}

	@Override
	public void setUsername(String aUsername) {
		setString(BaseConnector.VAR_USERNAME, aUsername);
	}

	@Override
	public void removeUsername() {
		removeVar(BaseConnector.VAR_USERNAME);
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
			Integer lArgMaxFrameSize = Integer.parseInt((String) mHeader.getArgs().get(Fragmentation.ARG_MAX_FRAME_SIZE));
			if (lArgMaxFrameSize <= lMaxFrameSize) {
				return lArgMaxFrameSize;
			}
		} catch (Exception lEx) {
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

	@Override
	public String getSessionUID() {
		if (getSession().getStorage().containsKey(SESSION_UID)) {
			return getSession().getStorage().get(SESSION_UID).toString();
		} else {
			String lUUID = UUID.randomUUID().toString();
			getSession().getStorage().put(SESSION_UID, lUUID);
			return lUUID;
		}
	}
}
