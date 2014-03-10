//	---------------------------------------------------------------------------
//	jWebSocket - ItemCollection (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.collection;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javolution.util.FastMap;
import org.jwebsocket.plugins.itemstorage.api.IClientCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.item.ItemDefinition;
import org.jwebsocket.token.Token;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ItemCollection implements IItemCollection {

	private IItemStorage mStorage;
	private final Map<String, Object> mData = new FastMap<String, Object>().shared();

	/**
	 *
	 */
	public static final String ATTR_ACCESS_PASSWORD = "accessPassword";

	/**
	 *
	 */
	public static final String ATTR_SECRET_PASSWORD = "secretPassword";

	/**
	 *
	 */
	public static final String ATTR_IS_SYSTEM = "system";

	/**
	 *
	 */
	public static final String ATTR_NAME = "name";

	/**
	 *
	 */
	public static final String ATTR_IS_PRIVATE = "private";

	/**
	 *
	 */
	public static final String ATTR_SUBSCRIBERS = "subscribers";

	/**
	 *
	 */
	public static final String ATTR_PUBLISHERS = "publishers";

	/**
	 *
	 */
	public static final String ATTR_CREATED_AT = "createdAt";

	/**
	 *
	 */
	public static final String ATTR_OWNER = "owner";

	/**
	 *
	 */
	public static final String ATTR_CAPACITY = "capacity";

	/**
	 *
	 */
	public static final String ATTR_CAPPED = "capped";

	/**
	 *
	 */
	public static final String COLLECTION_NAME_REGEXP = "^[a-zA-Z0-9]+([_]([a-zA-Z])+)*";

	/**
	 *
	 */
	public static final Integer MAX_PASSWORD_SIZE = 100;
	private IClientCollection mSubscribers;
	private IClientCollection mPublishers;

	/**
	 *
	 * @param aCreationTime
	 * @param aItemStorage
	 * @param aSubscribers
	 * @param aPublishers
	 */
	public ItemCollection(long aCreationTime, IItemStorage aItemStorage,
			IClientCollection aSubscribers, IClientCollection aPublishers) {
		mData.put(ATTR_CREATED_AT, aCreationTime);

		init(aItemStorage, aSubscribers, aPublishers);
	}

	/**
	 *
	 * @param aItemStorage
	 * @param aSubscribers
	 * @param aPublishers
	 */
	public ItemCollection(IItemStorage aItemStorage, IClientCollection aSubscribers, IClientCollection aPublishers) {
		init(aItemStorage, aSubscribers, aPublishers);
	}

	private void init(IItemStorage aItemStorage, IClientCollection aSubscribers, IClientCollection aPublishers) {
		Assert.notNull(aItemStorage, "The collection item storage cannot be null!");
		mStorage = aItemStorage;
		mSubscribers = aSubscribers;
		mPublishers = aPublishers;

		mData.put(ATTR_CAPPED, false);
		mData.put(ATTR_IS_PRIVATE, false);
		mData.put(ATTR_IS_SYSTEM, false);
		mData.put(ATTR_CAPACITY, 0);
	}

	@Override
	public boolean isCapped() {
		return (Boolean) mData.get(ATTR_CAPPED);
	}

	@Override
	public void setCapped(boolean aIsCapped) {
		Assert.isTrue(getCapacity() > 0, "The collection capacity is unlimited. The capped property can't be set!");
		mData.put(ATTR_CAPPED, aIsCapped);
	}

	@Override
	public int getCapacity() {
		return (Integer) mData.get(ATTR_CAPACITY);
	}

	@Override
	public void setCapacity(int aCapacity) throws Exception {
		Assert.isTrue(aCapacity >= 0, "Invalid item collection capacity! Expected value: capacity >= 0");
		if (aCapacity > 0) {
			Assert.isTrue(aCapacity >= getItemStorage().size(), "Invalid item collection capacity! Expected value: capacity > size");
		}
		mData.put(ATTR_CAPACITY, aCapacity);

		if (0 == aCapacity) {
			mData.put(ATTR_CAPPED, false);
		}
	}

	@Override
	public Boolean isPrivate() {
		return (Boolean) mData.get(ATTR_IS_PRIVATE);
	}

	@Override
	public void setPrivate(Boolean aIsPrivate) {
		mData.put(ATTR_IS_PRIVATE, aIsPrivate);
	}

	@Override
	public IItemStorage getItemStorage() {
		return mStorage;
	}

	@Override
	public Long createdAt() {
		return (Long) mData.get(ATTR_CREATED_AT);
	}

	@Override
	public String getName() {
		return mStorage.getName();
	}

	@Override
	public String getAccessPassword() {
		return (String) mData.get(ATTR_ACCESS_PASSWORD);
	}

	@Override
	public void setAccessPassword(String aPassword) {
		mData.put(ATTR_ACCESS_PASSWORD, aPassword);
	}

	@Override
	public String getSecretPassword() {
		return (String) mData.get(ATTR_SECRET_PASSWORD);
	}

	@Override
	public void setSecretPassword(String aPassword) {
		mData.put(ATTR_SECRET_PASSWORD, aPassword);
	}

	@Override
	public Boolean isSystem() {
		return (Boolean) mData.get(ATTR_IS_SYSTEM);
	}

	@Override
	public void setSystem(boolean aIsSystem) {
		mData.put(ATTR_IS_SYSTEM, aIsSystem);
	}

	@Override
	public void writeToToken(Token aToken) {
		toMap(aToken.getMap());
	}

	@Override
	public IClientCollection getSubcribers() {
		return mSubscribers;
	}

	@Override
	public IClientCollection getPublishers() {
		return mPublishers;
	}

	@Override
	public void readFromToken(Token aToken) {
		fromMap(aToken.getMap());
	}

	@Override
	public String getOwner() {
		return (String) mData.get(ATTR_OWNER);
	}

	@Override
	public void setOwner(String aOwner) {
		mData.put(ATTR_OWNER, aOwner);
	}

	@Override
	public Set<String> restart() throws Exception {
		Set<String> lClients = new HashSet<String>();
		lClients.addAll(this.getPublishers().getAll());
		lClients.addAll(this.getSubcribers().getAll());

		this.getSubcribers().clear();
		this.getPublishers().clear();

		return lClients;
	}

	@Override
	public void validate() throws Exception {
	}

	@Override
	public String toString() {
		return "ItemCollection{" + "data=" + mData + '}';
	}

	@Override
	public void fromMap(Map<String, Object> aMap) {
		setAccessPassword((String) aMap.get(ATTR_ACCESS_PASSWORD));
		setSecretPassword((String) aMap.get(ATTR_SECRET_PASSWORD));
		setSystem((Boolean) aMap.get(ATTR_IS_SYSTEM));
		setPrivate((Boolean) aMap.get(ATTR_IS_PRIVATE));
		mData.put(ATTR_CREATED_AT, (Long) aMap.get(ATTR_CREATED_AT));
		mData.put(ATTR_CAPACITY, (Integer) aMap.get(ATTR_CAPACITY));
		setOwner((String) aMap.get(ATTR_OWNER));
		mData.put(ATTR_CAPPED, (Boolean) aMap.get(ATTR_CAPPED));
	}

	@Override
	public void toMap(Map<String, Object> aMap) {
		aMap.put(ATTR_ACCESS_PASSWORD, getAccessPassword());
		aMap.put(ATTR_SECRET_PASSWORD, getSecretPassword());
		aMap.put(ATTR_NAME, getItemStorage().getName());
		aMap.put(ATTR_IS_SYSTEM, isSystem());
		aMap.put(ATTR_IS_PRIVATE, isPrivate());
		aMap.put(ATTR_CREATED_AT, createdAt());
		aMap.put(ATTR_CAPACITY, getCapacity());
		aMap.put(ATTR_OWNER, getOwner());
		aMap.put(ATTR_CAPPED, isCapped());
		aMap.put(ItemDefinition.ATTR_TYPE, getItemStorage().getItemType());
	}
}
