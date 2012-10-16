//	---------------------------------------------------------------------------
//	jWebSocket - Base Connector Implementation
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.connectors;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import javolution.util.FastMap;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.kit.*;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * Provides the basic implementation of the jWebSocket connectors. The
 * {@code BaseConnector} is supposed to be used as ancestor for the connector
 * implementations like e.g. the {@code TCPConnector} or the
 * {@code TomcatConnector }.
 *
 * @author aschulze
 * @author kyberneees
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
	 * Fragmentation model constants 
	 */
	public final static String ARG_MAX_FRAME_SIZE = "maxframesize";
	public static final String PACKET_FRAGMENT_PREFIX = "FRAGMENT";
	public static final String PACKET_LAST_FRAGMENT_PREFIX = "LFRAGMENT";
	public static final Integer MIN_FRAME_SIZE = 1024;
	public static final Integer PACKET_TRANSACTION_MAX_BYTES_PREFIXED = 31;
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
	private final Map<String, Object> mCustomVars = new FastMap<String, Object>();
	private final Object mWriteLock = new Object();
	private final Object mReadLock = new Object();
	/**
	 * Variables for the packet delivery mechanism
	 */
	private static Map<Integer, IPacketDeliveryListener> mPacketDeliveryListeners = new FastMap<Integer, IPacketDeliveryListener>().shared();
	private static Map<Integer, TimerTask> mPacketDeliveryTimerTasks = new FastMap<Integer, TimerTask>().shared();
	private static AtomicInteger mPacketCounter = new AtomicInteger(0);
	public static final String PACKET_ID_DELIMETER = ",";
	public static final String PACKET_DELIVERY_ACKNOWLEDGE_PREFIX = "pda";
	private final Object mPacketDeliveryListenersLock = new Object();

	/**
	 * Variables for the packet fragmentation mechanism
	 */
	/**
	 *
	 * @param aEngine
	 */
	public BaseConnector(WebSocketEngine aEngine) {
		mEngine = aEngine;
	}

	@Override
	public void startConnector() {
		if (getMaxFrameSize() < MIN_FRAME_SIZE) {
			// minimum frame size is required to establish a connection
			// reject connection at this point
			stopConnector(CloseReason.SERVER);
			return;
		}

		// sending agreed max frame size to the client
		sendPacket(new RawPacket(ARG_MAX_FRAME_SIZE + getMaxFrameSize()));

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
	public void processPacket(WebSocketPacket aDataPacket) {
		// processing packet delivery acknowledge from the client
		//MAX_INTEGER_TO_STRING_SIZE + PREFIX_SIZE
		if (aDataPacket.size() <= (10 + PACKET_DELIVERY_ACKNOWLEDGE_PREFIX.length())) {
			String lContent = aDataPacket.getString();
			if (lContent.startsWith(PACKET_DELIVERY_ACKNOWLEDGE_PREFIX)) {
				try {
					// getting the delivered packet id
					Integer lPID = Integer.parseInt(lContent.replace(PACKET_DELIVERY_ACKNOWLEDGE_PREFIX, ""));

					synchronized (mPacketDeliveryListenersLock) {
						if (mPacketDeliveryListeners.containsKey(lPID)) {
							// canceling timeout timer task
							mPacketDeliveryTimerTasks.remove(lPID).cancel();
							// calling the success callback on the delivery listener
							mPacketDeliveryListeners.remove(lPID).OnSuccess();
						}
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
		lPos = lData.indexOf(PACKET_ID_DELIMETER);

		if (lPos >= 0 && lPos < 10) {
			try {
				Integer lPacketId = Integer.parseInt(lData.substring(0, lPos));
				aDataPacket.setString(lData.substring(lPos + 1));

				// sending acknowledge packet
				sendPacket(new RawPacket(PACKET_DELIVERY_ACKNOWLEDGE_PREFIX + lPacketId.toString()));
			} catch (NumberFormatException lEx) {
			}
		}

		// supporting fragmentation
		lData = aDataPacket.getString();
		String lFragmentContent;
		Map<String, Object> lStorage;
		if (lData.startsWith(PACKET_FRAGMENT_PREFIX)) {
			lStorage = getSession().getStorage();
			lPos = lData.indexOf(PACKET_ID_DELIMETER);
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
		} else if (lData.startsWith(PACKET_LAST_FRAGMENT_PREFIX)) {
			lStorage = getSession().getStorage();
			lPos = lData.indexOf(PACKET_ID_DELIMETER);
			lPID = lData.substring(PACKET_LAST_FRAGMENT_PREFIX.length(), lPos);
			lFragmentContent = lData.substring(lPos + 1);

			// getting the complete packet content
			// lPID = PACKET_FRAGMENT_PREFIX + FRAGMENTATION_ID
			aDataPacket.setString(lStorage.remove(PACKET_FRAGMENT_PREFIX + lPID) + lFragmentContent);
		}

		// notifying engine
		if (mEngine != null) {
			mEngine.processPacket(this, aDataPacket);
		}
	}

	@Override
	public void processPing(WebSocketPacket aDataPacket) {
		/*
		 if (mEngine != null) {
		 mEngine.processPing(this, aDataPacket);
		 }
		 */
	}

	@Override
	public void processPong(WebSocketPacket aDataPacket) {
		/*
		 if (mEngine != null) {
		 mEngine.processPong(this, aDataPacket);
		 }
		 */
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener) {
		sendPacketInTransaction(aDataPacket, getMaxFrameSize(), aListener);
	}

	class FragmentationListener implements IPacketDeliveryListener {

		WebSocketPacket mOriginPacket;
		int mFragmentSize;
		IPacketDeliveryListener mOriginListener;
		int mNumberOfFragments;
		int mBytesSent = 0;
		long mTimeout;
		long mSentTime;
		int mFragmentationId;

		public FragmentationListener(WebSocketPacket aOriginPacket, int aFragmentSize,
				IPacketDeliveryListener aOriginListener, long aSentTime, int aFragmentationId) {
			mOriginPacket = aOriginPacket;
			mFragmentSize = aFragmentSize;
			mOriginListener = aOriginListener;
			mTimeout = aOriginListener.getTimeout();
			mSentTime = aSentTime;
			mFragmentationId = aFragmentationId;
		}

		@Override
		public long getTimeout() {
			long lTimeout = mSentTime + mOriginListener.getTimeout() - System.currentTimeMillis();
			if (lTimeout < 0) {
				lTimeout = 0;
			}

			return lTimeout;
		}

		@Override
		public void OnTimeout() {
			mOriginListener.OnTimeout();
		}

		@Override
		public void OnSuccess() {
			// updating bytes sent
			mBytesSent += mFragmentSize;
			if (mBytesSent >= mOriginPacket.size()) {
				// calling success if the packet was transmitted complete
				mOriginListener.OnSuccess();
			} else {
				// prepare to sent a next fragment
				int lLength = (mFragmentSize + mBytesSent <= mOriginPacket.size())
						? mFragmentSize
						: mOriginPacket.size() - mBytesSent;

				byte[] lBytes = Arrays.copyOfRange(mOriginPacket.getByteArray(), mBytesSent, mBytesSent + lLength);
				boolean lIsLast = (lLength + mBytesSent == mOriginPacket.size()) ? true : false;
				// send fragment
				sendPacketInTransaction(__createFragmentedPacket(lBytes, mFragmentationId, lIsLast), this);
			}
		}

		@Override
		public void OnFailure(Exception lEx) {
			mOriginListener.OnFailure(lEx);
		}
	}

	private WebSocketPacket __createFragmentedPacket(byte[] aByteArray, int aFragmentationId, boolean aIsLast) {
		return new RawPacket(((aIsLast) ? PACKET_LAST_FRAGMENT_PREFIX : PACKET_FRAGMENT_PREFIX)
				+ String.valueOf(aFragmentationId)
				+ PACKET_ID_DELIMETER
				+ new String(aByteArray));
	}

	@Override
	public void sendPacketInTransaction(final WebSocketPacket aDataPacket,
			Integer aFragmentSize, final IPacketDeliveryListener aListener) {
		// getting packet identifier
		final Integer lPacketId = mPacketCounter.incrementAndGet();
		// reset packet counter if arrive to the max value
		if (lPacketId.equals(Integer.MAX_VALUE)) {
			mPacketCounter.set(0);
		}

		// ommiting control frames
		if (WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())
				|| WebSocketFrameType.TEXT.equals(aDataPacket.getFrameType())) {

			String lPacketPrefix = lPacketId.toString() + PACKET_ID_DELIMETER;

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
					int lFragmentationId = mPacketCounter.incrementAndGet();

					// creating a special packet for fragmentation
					WebSocketPacket lPacketFragmented = __createFragmentedPacket(
							Arrays.copyOfRange(aDataPacket.getByteArray(), 0, aFragmentSize),
							lFragmentationId, lIsLast);

					// checking the new packet size
					Assert.isTrue(lPacketFragmented.size() + lPacketPrefix.length() <= getMaxFrameSize(),
							"The packet size exceeds the max frame size supported by the client! "
							+ "Consider that the packet has been prefixed with "
							+ (lPacketFragmented.size() + lPacketPrefix.length() - aDataPacket.size())
							+ " bytes for fragmented transaction.");

					// process fragmentation
					sendPacketInTransaction(
							lPacketFragmented,
							// passing special listener for fragmentation
							new FragmentationListener(
							aDataPacket,
							aFragmentSize,
							aListener,
							System.currentTimeMillis(),
							lFragmentationId));
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
				lBuffer = null;

				// saving the delivery listener
				mPacketDeliveryListeners.put(lPacketId, aListener);

				// setting the timer task for timeout support
				TimerTask lTT = new TimerTask() {
					@Override
					public void run() {
						synchronized (mPacketDeliveryListenersLock) {
							if (mPacketDeliveryListeners.containsKey(lPacketId)) {
								// cleaning expired data and calling timeout
								mPacketDeliveryTimerTasks.remove(lPacketId);
								mPacketDeliveryListeners.remove(lPacketId).OnTimeout();
							}
						}
					}
				};

				// saving the timer task reference
				mPacketDeliveryTimerTasks.put(lPacketId, lTT);

				// sending the packet to the client
				sendPacket(aDataPacket);

				// schedule the timer task
				Tools.getTimer().schedule(lTT, aListener.getTimeout());
			} catch (Exception lEx) {
				synchronized (mPacketDeliveryListenersLock) {
					if (mPacketDeliveryListeners.containsKey(lPacketId)) {
						// cleaning expired data and calling OnFailure 
						if (mPacketDeliveryTimerTasks.containsKey(lPacketId)) {
							mPacketDeliveryTimerTasks.remove(lPacketId).cancel();
						}
						mPacketDeliveryListeners.remove(lPacketId);
					}
				}
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
	private static Integer mCounter = 0;

	@Override
	public String getId() {
		synchronized (mCounter) {
			if (null == mUniqueId) {
				String lNodeId = JWebSocketConfig.getConfig().getNodeId();
				mUniqueId = ((lNodeId != null && lNodeId.length() > 0) ? lNodeId + "." : "")
						+ String.valueOf(getRemotePort())
						+ "." + mCounter++;
			}
			return mUniqueId;
		}
	}

	@Override
	public WebSocketSession getSession() {
		return mSession;
	}

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
			Integer lArgMaxFrameSize = Integer.parseInt((String) mHeader.getArgs().get(ARG_MAX_FRAME_SIZE));
			if (lArgMaxFrameSize <= lMaxFrameSize) {
				return lArgMaxFrameSize;
			}
		} catch (Exception lEx) {
		}

		return lMaxFrameSize;
	}

	protected void checkBeforeSend(WebSocketPacket aPacket) throws Exception {
		Assert.isTrue(getMaxFrameSize() >= aPacket.size(),
				"The packet size exceeds the connector supported 'max frame size' value!");
	}
}
