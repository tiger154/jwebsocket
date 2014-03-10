//	---------------------------------------------------------------------------
//	jWebSocket - BaseSubscriberStore (Community Edition, CE)
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
package org.jwebsocket.plugins.channels;

import org.apache.log4j.Logger;
import org.jwebsocket.api.IStorageProvider;
import org.jwebsocket.logging.Logging;

/**
 * Storage based implementation of the <tt>SubscriberStore</tt>
 *
 * @author Alexander Schulze, Puran Singh, Rolando Santamaria Maso
 */
public class BaseSubscriberStore implements SubscriberStore {

	/**
	 * logger object
	 */
	private static final Logger mLog = Logging.getLogger();
	/* properties */
	private final IStorageProvider mStorageProdiver;

	/**
	 * default constructor
	 *
	 * @param aStorageProvider
	 */
	public BaseSubscriberStore(IStorageProvider aStorageProvider) {
		mStorageProdiver = aStorageProvider;
	}

	@Override
	public Subscriber getSubscriber(String aId) throws Exception {
		return new Subscriber(aId, mStorageProdiver.getStorage(ChannelManager.CHANNEL_SUBSCRIBERS_STORAGE_PREFIX + aId));
	}

	@Override
	public void removeSubscriber(String aId) throws Exception {
		mStorageProdiver.removeStorage(ChannelManager.CHANNEL_SUBSCRIBERS_STORAGE_PREFIX + aId);
	}
}
