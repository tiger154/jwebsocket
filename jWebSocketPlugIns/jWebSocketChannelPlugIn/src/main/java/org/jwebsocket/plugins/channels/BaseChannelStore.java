//  ---------------------------------------------------------------------------
//  jWebSocket - BaseChannelStore (Community Edition, CE)
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;

/**
 * Base JDBC based implementation of the <tt>ChannelStore</tt>
 *
 * @author puran, aschulze
 * @version $Id: BaseChannelStore.java 1101 2010-10-19 12:36:12Z
 * fivefeetfurther$
 */
public class BaseChannelStore implements ChannelStore {

	/**
	 * logger object
	 */
	private static Logger logger = Logging.getLogger(BaseChannelStore.class);
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String PRIVATE = "isPrivate";
	public static final String SYSTEM = "isSystem";
	public static final String SECRET_KEY = "secret_key";
	public static final String ACCESS_KEY = "access_key";
	public static final String OWNER = "owner";
	public static final String STATE = "state";
	public static final String SUBSCRIBERS = "subscribers";
	public static final String PUBLISHERS = "publishers";
	public static final String SERVER_ID = "token_server";
	private IBasicStorage mStorage = null;

	public BaseChannelStore(IBasicStorage aStorage) {
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
	public Channel getChannel(String aId) {
		Object lObj = mStorage.get(aId);
		String lJSONString = (String) lObj;
		if (null == lJSONString) {
			return null;
		}
		return json2Channel(lJSONString);
	}

	private Channel json2Channel(String lJSONStr) {
		Channel lChannel = null;
		try {
			JSONObject lJSONObj = new JSONObject(lJSONStr);
			String lChannelId = lJSONObj.getString(ID);
			String lChannelName = lJSONObj.getString(NAME);
			boolean lPrivate = lJSONObj.getBoolean(PRIVATE);
			boolean lSystem = lJSONObj.getBoolean(SYSTEM);
			String lSecretKey = lJSONObj.getString(SECRET_KEY);
			String lAccessKey = lJSONObj.getString(ACCESS_KEY);
			String lOwner = lJSONObj.getString(OWNER);
			String lStateValue = lJSONObj.getString(STATE);
			JSONArray lJSSubscribers = lJSONObj.getJSONArray(SUBSCRIBERS);
			JSONArray lJSPublishers = lJSONObj.getJSONArray(PUBLISHERS);
			String lServerId = lJSONObj.getString(SERVER_ID);
			// construct the channel object
			lChannel = new Channel(lChannelId, lChannelName,
					lPrivate, lSystem,
					lAccessKey, lSecretKey,
					lOwner, Channel.ChannelState.valueOf(lStateValue),
					(TokenServer) JWebSocketFactory.getServer(lServerId));
			List lSubscribers = new FastList<String>();
			List lPublishers = new FastList<String>();
			for (int i = 0; i < lJSSubscribers.length(); i++) {
				lSubscribers.add(lJSSubscribers.getString(i));
			}
			for (int i = 0; i < lJSPublishers.length(); i++) {
				lPublishers.add(lJSPublishers.getString(i));
			}

			lChannel.setSubscribers(lSubscribers);
			lChannel.setPublishers(lPublishers);
		} catch (JSONException lEx) {
			logger.error("Error parsing JSON response from the channel store:", lEx);
		}
		return lChannel;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aChannel
	 */
	@Override
	public boolean storeChannel(Channel aChannel) {
		JSONObject lJSON = new JSONObject();
		try {
			lJSON.put(ID, aChannel.getId());
			lJSON.put(NAME, aChannel.getName());
			lJSON.put(PRIVATE, aChannel.isPrivate());
			lJSON.put(SYSTEM, aChannel.isSystem());
			lJSON.put(SECRET_KEY, aChannel.getSecretKey());
			lJSON.put(ACCESS_KEY, aChannel.getAccessKey());
			lJSON.put(OWNER, aChannel.getOwner());
			lJSON.put(STATE, aChannel.getState().name());
			JSONArray lSubscribers = new JSONArray(aChannel.getSubscribers());
			lJSON.put(SUBSCRIBERS, lSubscribers);
			JSONArray lPublishers = new JSONArray(aChannel.getPublishers());
			lJSON.put(PUBLISHERS, lPublishers);
			lJSON.put(SERVER_ID, aChannel.getServer().getId());

			// now save
			// TODO: Need to think about how to return potential error (Exception?)
			mStorage.put(aChannel.getId(), lJSON.toString());
			return true;
		} catch (JSONException e) {
			logger.error("Error constructing JSON data for the given channel '"
					+ aChannel.getName() + "'", e);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeChannel(String id) {
		mStorage.remove(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearChannels() {
		mStorage.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getChannelStoreSize() {
		return mStorage.size();
	}

	@Override
	public Map<String, Channel> getChannels() {
		Set lKeys = mStorage.keySet();
		Map lRes = new FastMap<String, Channel>();
		if (lKeys != null) {
			for (Object lKey : lKeys) {
				Object lValue = mStorage.get((String) lKey);
				Channel lChannel = json2Channel((String) lValue);
				lRes.put(lChannel.getId(), lChannel);
			}
		}
		return lRes;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean hasChannel(String aChannelId) {
		return mStorage.containsKey(aChannelId);
	}
}
