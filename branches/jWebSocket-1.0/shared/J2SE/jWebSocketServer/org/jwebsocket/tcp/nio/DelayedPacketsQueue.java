//	---------------------------------------------------------------------------
//	jWebSocket - DelayedPacketsQueue
//	Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.tcp.nio;

import java.util.Collection;
import java.util.Iterator;
import javolution.util.FastSet;

/**
 *
 * @author kyberneees
 */
public class DelayedPacketsQueue {

	private Collection<IDelayedPacketNotifier> mDelayedPackets = new FastSet().shared();

	/**
	 * Enqueue a delayed packets to be processed by the workers when the
	 * connector be ready
	 *
	 * @param aDelayedPacket
	 */
	public void addDelayedPacket(IDelayedPacketNotifier aDelayedPacket) {
		mDelayedPackets.add(aDelayedPacket);
	}

	/**
	 *
	 * @return The top available delayed packet to be processed by the workers
	 */
	public synchronized IDelayedPacketNotifier pop() {
		IDelayedPacketNotifier lResult = null;

		if (mDelayedPackets.isEmpty()) {
			return lResult;
		}

		for (Iterator<IDelayedPacketNotifier> it = mDelayedPackets.iterator(); it.hasNext();) {
			IDelayedPacketNotifier lDelayedPacket = it.next();

			if (lDelayedPacket.getConnector().getWorkerId() == -1) {
				lResult = lDelayedPacket;
				mDelayedPackets.remove(lDelayedPacket);
				break;
			}
		}

		return lResult;
	}

	/**
	 * Clear the delayed packets queue
	 */
	public void clear() {
		mDelayedPackets.clear();
	}
}
