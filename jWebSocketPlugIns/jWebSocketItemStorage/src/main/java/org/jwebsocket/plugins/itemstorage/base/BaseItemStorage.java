package org.jwebsocket.plugins.itemstorage.base;

import java.util.List;
import org.apache.commons.lang.math.RandomUtils;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.springframework.util.Assert;

/**
 *
 * @author kyberneees
 */
abstract public class BaseItemStorage implements IItemStorage {

	protected String mName;
	protected String mType;
	protected IItemFactory mItemFactory;

	public BaseItemStorage(String aName, String aType, IItemFactory aItemFactory) {
		this.mName = aName;
		this.mType = aType;
		this.mItemFactory = aItemFactory;

		Assert.notNull(aName, "The storage name argument cannot be null!");
		Assert.notNull(aType, "The storage type argument cannot be null!");
		Assert.notNull(aItemFactory, "The storage item factory argument cannot be null!");
		Assert.isTrue(mItemFactory.supportsType(aType), "Item type not supported!");
	}

	@Override
	public IItem findRandom() throws Exception {
		if (0 == size()) {
			return null;
		}
		if (1 == size()) {
			return list(0, 1).get(0);
		} else {
			return list(RandomUtils.nextInt(size() - 1), 1).get(0);
		}
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public String getItemType() {
		return mType;
	}

	@Override
	public List<IItem> list() throws Exception {
		return list(0, size());
	}

	@Override
	public List<IItem> list(int aOffset) throws Exception {
		return list(aOffset, size());
	}

	@Override
	public List<IItem> find(String aAttribute, Object aValue) throws Exception {
		return find(aAttribute, aValue, 0, size());
	}

	@Override
	public IItemFactory getItemFactory() {
		return mItemFactory;
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}
}
