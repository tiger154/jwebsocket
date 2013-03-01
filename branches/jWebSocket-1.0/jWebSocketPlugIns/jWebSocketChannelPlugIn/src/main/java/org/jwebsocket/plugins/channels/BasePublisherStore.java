//  ---------------------------------------------------------------------------
//  jWebSocket - BasePublisherStore (Community Edition, CE)
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
package org.jwebsocket.plugins.channels;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.logging.Logging;

/**
 * Storage based implementation of the <tt>PublisherStore</tt>
 *
 * @author aschulze
 */
public class BasePublisherStore implements PublisherStore {

	/* logger object */
	private static Logger mLog = Logging.getLogger(BaseSubscriberStore.class);
	/* properties */
	private static final String ID = "id";
	private static final String CHANNELS = "channels";
	private IBasicStorage mStorage = null;

	/**
	 * default constructor
	 *
	 * @param aStorage
	 */
	public BasePublisherStore(IBasicStorage aStorage) {
		setStorage(aStorage);
	}

	/**
	 *
	 * @param aStorage
	 */
	public final void setStorage(IBasicStorage aStorage) {
		mStorage = aStorage;
	}

	/**
	 *
	 * @return
	 */
	public final IBasicStorage getStorage() {
		return mStorage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aId
	 */
	@Override
	public Publisher getPublisher(String aId) {
		JSONObject lPublisherObject = null;
		try {
			String lStr = (String) mStorage.get(aId);
			lPublisherObject = new JSONObject(lStr);
		} catch (Exception lEx) {
		}
		if (lPublisherObject == null) {
			return null;
		}
		Publisher lPublisher = null;
		// TODO: fix: if PublisherObject == null => exception
		try {
			lPublisher = new Publisher(aId);
			JSONArray lChannels = lPublisherObject.getJSONArray(CHANNELS);
			if (lChannels != null) {
				for (int lIdx = 0; lIdx < lChannels.length(); lIdx++) {
					JSONObject lObj = lChannels.getJSONObject(lIdx);
					String lChannelId = lObj.getString(ID);
					lPublisher.addChannel(lChannelId);
				}
			}
		} catch (JSONException lEx) {
			mLog.error("Error parsing json response from the channel repository:", lEx);
		}
		return lPublisher;
	}

	@Override
	public boolean storePublisher(Publisher aPublisher) {
		JSONObject lPublisherObject = new JSONObject();
		try {
			lPublisherObject.put(ID, aPublisher.getId());
			JSONArray lJSONArray = new JSONArray();
			for (String lChannel : aPublisher.getChannels()) {
				JSONObject lChannelObject = new JSONObject();
				lChannelObject.put(ID, lChannel);
				lJSONArray.put(lChannelObject);
			}
			lPublisherObject.put(CHANNELS, lJSONArray);
			// TODO: updated by Alex: PublisherObject.toString() instead of PublisherObject (JSONObject is not serializable!)
			// TODO: Need to think about how to return potential error (Exception?)
			mStorage.put(aPublisher.getId(), lPublisherObject.toString());
			return true;
		} catch (JSONException lEx) {
			mLog.error("Error constructing JSON data for the given Publisher '"
					+ aPublisher.getId() + "'", lEx);
			return false;
		}
	}

	@Override
	public void removePublisher(String aId) {
		mStorage.remove(aId);
	}

	@Override
	public void clearPublishers() {
		mStorage.clear();
	}

	@Override
	public int getPublisherStoreSize() {
		return mStorage.size();
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aPublisherId
	 */
	@Override
	public boolean hasPublisher(String aPublisherId) {
		return mStorage.containsKey(aPublisherId);
	}
}
