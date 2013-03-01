//  ---------------------------------------------------------------------------
//  jWebSocket - BaseSubscriberStore (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
 * Storage based implementation of the <tt>SubscriberStore</tt>
 *
 * @author puran, aschulze
 * @version $Id: BaseSubscriberStore.java 1592 2011-02-20 00:49:48Z
 * fivefeetfurther $
 */
public class BaseSubscriberStore implements SubscriberStore {

	/**
	 * logger object
	 */
	private static Logger mLog = Logging.getLogger(BaseSubscriberStore.class);
	private static final String ID = "id";
	private static final String CHANNELS = "channels";
	private IBasicStorage mStorage = null;

	/**
	 * default constructor
	 */
	public BaseSubscriberStore(IBasicStorage aStorage) {
		setStorage(aStorage);
	}

	public final void setStorage(IBasicStorage aStorage) {
		mStorage = aStorage;
	}

	public final IBasicStorage getStorage() {
		return mStorage;
	}

	@Override
	public Subscriber getSubscriber(String aId) {
		JSONObject lSubscriberObject = null;
		try {
			String lStr = (String) mStorage.get(aId);
			lSubscriberObject = new JSONObject(lStr);
		} catch (Exception lEx) {
		}
		if (lSubscriberObject == null) {
			return null;
		}
		Subscriber lSubscriber = null;
		// TODO: fix: if subscriberObject == null => exception
		try {
			lSubscriber = new Subscriber(aId);
			JSONArray lChannels = lSubscriberObject.getJSONArray(CHANNELS);
			if (lChannels != null) {
				for (int lIdx = 0; lIdx < lChannels.length(); lIdx++) {
					JSONObject lObj = lChannels.getJSONObject(lIdx);
					String lChannelId = lObj.getString(ID);
					lSubscriber.addChannel(lChannelId);
				}
			}
		} catch (JSONException lEx) {
			mLog.error("Error parsing json response from the channel repository:", lEx);
		}
		return lSubscriber;
	}

	@Override
	public boolean storeSubscriber(Subscriber aSubscriber) {
		JSONObject lSubscriberObject = new JSONObject();
		try {
			lSubscriberObject.put(ID, aSubscriber.getId());
			JSONArray lJSONArray = new JSONArray();
			for (String lChannel : aSubscriber.getChannels()) {
				JSONObject lChannelObject = new JSONObject();
				lChannelObject.put(ID, lChannel);
				lJSONArray.put(lChannelObject);
			}
			lSubscriberObject.put(CHANNELS, lJSONArray);
			// TODO: updated by Alex: subscriberObject.toString() instead of subscriberObject (JSONObject is not serializable!)
			// TODO: Need to think about how to return potential error (Exception?)
			mStorage.put(aSubscriber.getId(), lSubscriberObject.toString());
			return true;
		} catch (JSONException lEx) {
			mLog.error("Error constructing JSON data for the given subscriber '"
					+ aSubscriber.getId() + "'", lEx);
			return false;
		}
	}

	@Override
	public void removeSubscriber(String id) {
		mStorage.remove(id);
	}

	@Override
	public void clearSubscribers() {
		mStorage.clear();
	}

	@Override
	public int getSubscribersStoreSize() {
		return mStorage.size();
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean hasSubscriber(String aSubscriberId) {
		return mStorage.containsKey(aSubscriberId);
	}
}
