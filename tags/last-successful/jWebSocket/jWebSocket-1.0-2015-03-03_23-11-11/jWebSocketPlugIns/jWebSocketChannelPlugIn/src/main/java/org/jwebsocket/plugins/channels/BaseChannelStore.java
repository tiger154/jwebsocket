//	---------------------------------------------------------------------------
//	jWebSocket - BaseChannelStore (Community Edition, CE)
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

import java.util.Map;
import java.util.Set;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.IStorageProvider;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;

/**
 * Base JDBC based implementation of the <tt>ChannelStore</tt>
 *
 * @author Alexander Schulze, Puran Singh
 * @version $Id: BaseChannelStore.java 1101 2010-10-19 12:36:12Z
 * fivefeetfurther$
 */
public class BaseChannelStore implements ChannelStore {

	/**
	 * logger object
	 */
	private static final Logger logger = Logging.getLogger(BaseChannelStore.class);
	/**
	 *
	 */
	public static final String ID = "id";
	/**
	 *
	 */
	public static final String NAME = "name";
	/**
	 *
	 */
	public static final String PRIVATE = "isPrivate";
	/**
	 *
	 */
	public static final String SYSTEM = "isSystem";
	/**
	 *
	 */
	public static final String SECRET_KEY = "secret_key";
	/**
	 *
	 */
	public static final String ACCESS_KEY = "access_key";
	/**
	 *
	 */
	public static final String OWNER = "owner";
	/**
	 *
	 */
	public static final String STATE = "state";
	/**
	 *
	 */
	public static final String SUBSCRIBERS = "subscribers";
	/**
	 *
	 */
	public static final String PUBLISHERS = "publishers";
	/**
	 *
	 */
	public static final String SERVER_ID = "token_server";
	private final IBasicStorage<String, Object> mStorage;
	private final IStorageProvider mStorageProvider;

	/**
	 *
	 * @param aStorage
	 * @param aStorageProvider
	 */
	public BaseChannelStore(IBasicStorage<String, Object> aStorage, IStorageProvider aStorageProvider) {
		mStorage = aStorage;
		mStorageProvider = aStorageProvider;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aId
	 * @throws Exception
	 */
	@Override
	public Channel getChannel(String aId) throws Exception {
		Object lObj = mStorage.get(aId);
		String lJSONString = (String) lObj;
		if (null == lJSONString) {
			return null;
		}
		return json2Channel(lJSONString);
	}

	private Channel json2Channel(String lJSONStr) throws Exception {
		Channel lChannel = null;
		try {
			JSONObject lJSONObj = new JSONObject(lJSONStr);
			String lChannelId = lJSONObj.getString(ID);
			String lChannelName = lJSONObj.getString(NAME);
			boolean lPrivate = lJSONObj.getBoolean(PRIVATE);
			boolean lSystem = lJSONObj.getBoolean(SYSTEM);

			String lSecretKey = null, lAccessKey = null;
			if (lJSONObj.has(SECRET_KEY)) {
				lSecretKey = lJSONObj.getString(SECRET_KEY);
			}
			if (lJSONObj.has(ACCESS_KEY)) {
				lAccessKey = lJSONObj.getString(ACCESS_KEY);
			}

			String lOwner = lJSONObj.getString(OWNER);
			String lStateValue = lJSONObj.getString(STATE);
			String lServerId = lJSONObj.getString(SERVER_ID);
			// construct the channel object
			lChannel = new Channel(lChannelId, lChannelName,
					lPrivate, lSystem,
					lAccessKey, lSecretKey,
					lOwner, Channel.ChannelState.valueOf(lStateValue),
					(TokenServer) JWebSocketFactory.getServer(lServerId),
					mStorageProvider.getStorage(ChannelManager.CHANNEL_SUBSCRIBERS_STORAGE_PREFIX + lChannelId),
					mStorageProvider.getStorage(ChannelManager.CHANNEL_PUBLISHERS_STORAGE_PREFIX + lChannelId));
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
	public Map<String, Channel> getChannels() throws Exception {
		Set<String> lKeys = mStorage.keySet();
		Map<String, Channel> lRes = new FastMap<String, Channel>();
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
	 *
	 * @param aChannelId
	 */
	@Override
	public boolean hasChannel(String aChannelId) {
		return mStorage.containsKey(aChannelId);
	}
}
