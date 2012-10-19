//	---------------------------------------------------------------------------
//	jWebSocket - Fragmentation
//	Copyright (c) 2012 Innotrade GmbH
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
	public static final String PACKET_FRAGMENT_PREFIX = "FRAGMENT";
	public static final String PACKET_LAST_FRAGMENT_PREFIX = "LFRAGMENT";
	public static final Integer MIN_FRAME_SIZE = 1024;
	public static final Integer PACKET_TRANSACTION_MAX_BYTES_PREFIXED = 31;
	public static final String PACKET_ID_DELIMETER = ",";
	public static final String PACKET_DELIVERY_ACKNOWLEDGE_PREFIX = "pda";
	private static Integer mUID = 0;

	public static synchronized Integer generateUID() {
		Integer lValue = ++mUID;

		if (lValue.equals(Integer.MAX_VALUE)) {
			mUID = 0;
		}

		return lValue;
	}

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

	public static IPacketDeliveryListener createListener(WebSocketPacket aOriginPacket,
			int aFragmentSize, IPacketDeliveryListener aOriginListener, long aSentTime,
			int aFragmentationId, InFragmentationListenerSender aSender) {
		return new FragmentationListener(aOriginPacket, aFragmentSize, aOriginListener, aSentTime, aFragmentationId, aSender);
	}
}
