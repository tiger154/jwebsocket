package org.jwebsocket.plugins.itemstorage.collection;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.item.ItemDefinition;
import org.jwebsocket.token.Token;
import org.springframework.util.Assert;

/**
 *
 * @author kyberneees
 */
public class ItemCollection implements IItemCollection {

	private IItemStorage mStorage;
	private Map<String, Object> mData = new FastMap<String, Object>().shared();
	public static final String ATTR_ACCESS_PASSWORD = "accessPassword";
	public static final String ATTR_SECRET_PASSWORD = "secretPassword";
	public static final String ATTR_IS_SYSTEM = "system";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_IS_PRIVATE = "private";
	public static final String ATTR_SUBSCRIBERS = "subscribers";
	public static final String ATTR_PUBLISHERS = "publishers";
	public static final String ATTR_CREATED_AT = "createdAt";
	public static final String ATTR_OWNER = "owner";
	public static final String ATTR_CAPACITY = "capacity";
	public static final String ATTR_CAPPED = "capped";
	public static final String COLLECTION_NAME_REGEXP = "^[a-zA-Z0-9]+(.[_a-zA-Z0-9-]+)*";
	public static final Integer MAX_PASSWORD_SIZE = 100;

	public ItemCollection(long aCreationTime, IItemStorage aItemStorage) {
		mStorage = aItemStorage;
		Assert.notNull(aItemStorage, "The collection item storage cannot be null!");

		mData.put(ATTR_CREATED_AT, aCreationTime);

		init();
	}

	public ItemCollection(IItemStorage aItemStorage) {
		mStorage = aItemStorage;
		Assert.notNull(aItemStorage, "The collection item storage cannot be null!");

		init();
	}

	private void init() {
		mData.put(ATTR_CAPPED, false);
		mData.put(ATTR_PUBLISHERS, new FastList<String>());
		mData.put(ATTR_SUBSCRIBERS, new FastList<String>());
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
		mData.put(ATTR_CAPPED, aIsCapped);
	}

	@Override
	public int getCapacity() {
		return (Integer) mData.get(ATTR_CAPACITY);
	}

	@Override
	public void setCapacity(int aCapacity) {
		Assert.isTrue(getCapacity() == 0, "The collection capacity has been previously assigned!");
		Assert.isTrue(aCapacity > 0, "Invalid item collection capacity! Expected value: capacity > 0");
		mData.put(ATTR_CAPACITY, aCapacity);
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
	public List<String> getSubcribers() {
		return (List<String>) mData.get(ATTR_SUBSCRIBERS);
	}

	@Override
	public List<String> getPublishers() {
		return (List<String>) mData.get(ATTR_PUBLISHERS);
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
	public Set<String> restart() {
		Set<String> lClients = new HashSet<String>();
		lClients.addAll(this.getPublishers());
		lClients.addAll(this.getSubcribers());

		this.getSubcribers().clear();
		this.getPublishers().clear();

		return lClients;
	}

	@Override
	public void validate() throws Exception {
		Assert.notNull(getAccessPassword(), "The access password argument cannot be null!");
		Assert.isTrue(getAccessPassword().length() <= MAX_PASSWORD_SIZE, "The access password is too large. "
				+ "Max length allowed: " + MAX_PASSWORD_SIZE + "!");

		Assert.notNull(getSecretPassword(), "The secret password argument cannot be null!");
		Assert.isTrue(getSecretPassword().length() <= MAX_PASSWORD_SIZE, "The secret password is too large. "
				+ "Max length allowed: " + MAX_PASSWORD_SIZE + "!");

		Assert.notNull(getOwner(), "The owner argument cannot be null!");
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
		getPublishers().addAll((List) aMap.get(ATTR_PUBLISHERS));
		getSubcribers().addAll((List) aMap.get(ATTR_SUBSCRIBERS));
		mData.put(ATTR_CREATED_AT, (Long) aMap.get(ATTR_CREATED_AT));
		mData.put(ATTR_CAPACITY, (Integer) aMap.get(ATTR_CAPACITY));
		setOwner((String) aMap.get(ATTR_OWNER));
		setCapped((Boolean) aMap.get(ATTR_CAPPED));
	}

	@Override
	public void toMap(Map<String, Object> aMap) {
		aMap.put(ATTR_ACCESS_PASSWORD, getAccessPassword());
		aMap.put(ATTR_SECRET_PASSWORD, getSecretPassword());
		aMap.put(ATTR_NAME, getItemStorage().getName());
		aMap.put(ATTR_IS_SYSTEM, isSystem());
		aMap.put(ATTR_IS_PRIVATE, isPrivate());
		aMap.put(ATTR_SUBSCRIBERS, getSubcribers());
		aMap.put(ATTR_PUBLISHERS, getPublishers());
		aMap.put(ATTR_CREATED_AT, createdAt());
		aMap.put(ATTR_CAPACITY, getCapacity());
		aMap.put(ATTR_OWNER, getOwner());
		aMap.put(ATTR_CAPPED, isCapped());
		aMap.put(ItemDefinition.ATTR_TYPE, getItemStorage().getItemType());
	}
}
