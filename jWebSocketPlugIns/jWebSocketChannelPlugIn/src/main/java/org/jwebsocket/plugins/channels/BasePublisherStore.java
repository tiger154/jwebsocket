//  ---------------------------------------------------------------------------
//  jWebSocket - BasePublisherStore
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.channels;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.logging.Logging;

/**
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
	 */
	public BasePublisherStore(IBasicStorage aStorage) {
		setStorage(aStorage);
	}

	public final void setStorage(IBasicStorage aStorage) {
		mStorage = aStorage;
	}

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
}
