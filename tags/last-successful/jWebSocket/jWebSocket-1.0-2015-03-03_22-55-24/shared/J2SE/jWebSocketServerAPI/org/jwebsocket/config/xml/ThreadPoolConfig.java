// ---------------------------------------------------------------------------
// jWebSocket - ThreadPoolConfig (Community Edition, CE)
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
package org.jwebsocket.config.xml;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Represents the spawnThread configuration information configured via
 * jWebSocket.xml file
 *
 * @author Quentin
 * @version
 */
public class ThreadPoolConfig implements Config {

	private int corePoolSize = Runtime.getRuntime().availableProcessors();
	private int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 2;
	private int keepAliveTime = 60;
	private int blockingQueueSize = 1000;

	/**
	 *
	 * @param aCorePoolSize
	 * @param aMaximumPoolSize
	 * @param aKeepAliveTime
	 * @param aBlockingQueueSize
	 */
	public ThreadPoolConfig(int aCorePoolSize, int aMaximumPoolSize, int aKeepAliveTime, int aBlockingQueueSize) {
		this.corePoolSize = aCorePoolSize;
		this.maximumPoolSize = aMaximumPoolSize;
		this.keepAliveTime = aKeepAliveTime;
		this.blockingQueueSize = aBlockingQueueSize;
	}

	/**
	 *
	 */
	public ThreadPoolConfig() {
	}

	@Override
	public void validate() {
		if ((corePoolSize > 0)
				&& (maximumPoolSize > 0)
				&& (keepAliveTime > 0)
				&& (blockingQueueSize > 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the thread pool configuration, "
				+ "please check your configuration file");
	}

	/**
	 *
	 * @return
	 */
	public int getCorePoolSize() {
		return corePoolSize;
	}

	/**
	 *
	 * @return
	 */
	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	/**
	 *
	 * @return
	 */
	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	/**
	 *
	 * @return
	 */
	public int getBlockingQueueSize() {
		return blockingQueueSize;
	}
}
