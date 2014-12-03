//	---------------------------------------------------------------------------
//	jWebSocket - DelayedPacketsQueue (Community Edition, CE)
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
package org.jwebsocket.tcp.nio;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import javolution.util.FastMap;

/**
 * Dispatch incoming packets for the NIO workers
 *
 * @author Rolando Santamaria Maso
 */
public class DelayedPacketsQueue {

	private final Map<NioTcpConnector, Queue<IDelayedPacketNotifier>> mDelayedPackets = new FastMap<NioTcpConnector, Queue<IDelayedPacketNotifier>>().shared();

	/**
	 * Enqueue a delayed packet to be processed by the workers
	 *
	 * @param aDelayedPacket
	 */
	public void addDelayedPacket(IDelayedPacketNotifier aDelayedPacket) {
		if (!mDelayedPackets.containsKey(aDelayedPacket.getConnector())) {
			mDelayedPackets.put(aDelayedPacket.getConnector(), new LinkedBlockingQueue<IDelayedPacketNotifier>());
		}
		mDelayedPackets.get(aDelayedPacket.getConnector()).offer(aDelayedPacket);
	}

	/**
	 *
	 * @return The top available delayed packet to be processed by the workers
	 * @throws InterruptedException
	 */
	public synchronized IDelayedPacketNotifier take() throws InterruptedException {
		while (true) {
			Iterator<NioTcpConnector> lKeys = mDelayedPackets.keySet().iterator();
			while (lKeys.hasNext()) {
				NioTcpConnector lConnector = lKeys.next();
				if (lConnector.getWorkerId() == -1 && !mDelayedPackets.get(lConnector).isEmpty()) {
					try {
						IDelayedPacketNotifier lPacket = mDelayedPackets.get(lConnector).remove();
						lConnector.setWorkerId(Thread.currentThread().hashCode());

						return lPacket;
					} catch (Exception lEx) {
						// ignore it. the connector was stopped in the middle
					}
				}
			}
			// CPU release
			Thread.sleep(5);
		}
	}

	/**
	 *
	 * @return
	 */
	public Map<NioTcpConnector, Queue<IDelayedPacketNotifier>> getDelayedPackets() {
		return mDelayedPackets;
	}
}
