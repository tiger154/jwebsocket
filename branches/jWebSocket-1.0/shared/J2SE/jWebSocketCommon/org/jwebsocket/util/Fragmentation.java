//	---------------------------------------------------------------------------
//	jWebSocket Fragmentation (Community Edition, CE)
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
package org.jwebsocket.util;

import java.util.Arrays;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.InFragmentationListenerSender;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;

/**
 * Utility library to support the fragmentation mechanism
 *
 * @author kyberneees
 */
public class Fragmentation {

	/**
	 * Fragmentation model constants
	 */
	public final static String ARG_MAX_FRAME_SIZE = "maxframesize";
	/**
	 *
	 */
	public static final String PACKET_FRAGMENT_PREFIX = "FRAGMENT";
	/**
	 *
	 */
	public static final String PACKET_LAST_FRAGMENT_PREFIX = "LFRAGMENT";
	/**
	 *
	 */
	public static final Integer MIN_FRAME_SIZE = 1024;
	/**
	 *
	 */
	public static final Integer PACKET_TRANSACTION_MAX_BYTES_PREFIXED = 31;
	/**
	 *
	 */
	public static final String PACKET_ID_DELIMETER = ",";
	/**
	 *
	 */
	public static final String PACKET_DELIVERY_ACKNOWLEDGE_PREFIX = "pda";
	private static Integer mUID = 0;

	/**
	 *
	 * @return
	 */
	public static synchronized Integer generateUID() {
		Integer lValue = ++mUID;

		if (lValue.equals(Integer.MAX_VALUE)) {
			mUID = 0;
		}

		return lValue;
	}

	/**
	 *
	 * @param aByteArray
	 * @param aFragmentationId
	 * @param aIsLast
	 * @return
	 */
	public static WebSocketPacket createFragmentedPacket(byte[] aByteArray, int aFragmentationId, boolean aIsLast) {
		return new RawPacket(((aIsLast) ? Fragmentation.PACKET_LAST_FRAGMENT_PREFIX : Fragmentation.PACKET_FRAGMENT_PREFIX)
				+ String.valueOf(aFragmentationId)
				+ Fragmentation.PACKET_ID_DELIMETER
				+ new String(aByteArray));
	}

	static class FragmentationListener implements IPacketDeliveryListener {

		WebSocketPacket mOriginPacket;
		int mFragmentSize;
		IPacketDeliveryListener mOriginListener;
		int mBytesSent = 0;
		long mTimeout;
		long mSentTime;
		int mFragmentationId;
		InFragmentationListenerSender mSender;

		public FragmentationListener(WebSocketPacket aOriginPacket, int aFragmentSize,
				IPacketDeliveryListener aOriginListener, long aSentTime, int aFragmentationId,
				InFragmentationListenerSender aSender) {
			mOriginPacket = aOriginPacket;
			mFragmentSize = aFragmentSize;
			mOriginListener = aOriginListener;
			mTimeout = aOriginListener.getTimeout();
			mSentTime = aSentTime;
			mFragmentationId = aFragmentationId;
			mSender = aSender;
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
				// send next fragment
				mSender.sendPacketInTransaction(Fragmentation.createFragmentedPacket(lBytes, mFragmentationId, lIsLast), this);
			}
		}

		@Override
		public void OnFailure(Exception lEx) {
			mOriginListener.OnFailure(lEx);
		}
	}

	/**
	 *
	 * @param aOriginPacket
	 * @param aFragmentSize
	 * @param aOriginListener
	 * @param aSentTime
	 * @param aFragmentationId
	 * @param aSender
	 * @return
	 */
	public static IPacketDeliveryListener createListener(WebSocketPacket aOriginPacket,
			int aFragmentSize, IPacketDeliveryListener aOriginListener, long aSentTime,
			int aFragmentationId, InFragmentationListenerSender aSender) {
		return new FragmentationListener(aOriginPacket, aFragmentSize, aOriginListener, aSentTime, aFragmentationId, aSender);
	}
}
