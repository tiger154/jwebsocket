package org.jwebsocket.plugins.itemstorage.memory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemDefinition;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.base.BaseItemStorage;
import org.jwebsocket.plugins.itemstorage.event.ItemStorageEventManager;
import org.springframework.util.Assert;

/**
 *
 * @author kyberneees
 */
public class MemoryItemStorage extends BaseItemStorage {

	private static FastMap<String, FastList> mContainer = new FastMap<String, FastList>();
	private static FastMap<String, String> mTypes = new FastMap<String, String>();
	private FastList<IItem> mData;

	public MemoryItemStorage(String aName, String aType, IItemFactory aItemFactory) {
		super(aName, aType, aItemFactory);
	}

	/**
	 * Release an item storage memory resources destroying all stored data.
	 *
	 * @param aStorageName
	 */
	public static void release(String aStorageName) {
		Assert.notNull(aStorageName, "The storage name argument cannot be null!");

		mContainer.remove(aStorageName).clear();
		mTypes.remove(aStorageName);
	}

	public static Map<String, FastList> getContainer() {
		return Collections.unmodifiableMap(mContainer);
	}

	@Override
	public Set<String> getPKs() throws Exception {
		FastSet<String> lPKs = new FastSet<String>();
		for (Iterator<IItem> lIt = mData.iterator(); lIt.hasNext();) {
			IItem lItem = lIt.next();
			lPKs.add(lItem.getPK());
		}

		return lPKs;
	}

	@Override
	public void save(String aTargetPK, IItem aItem) throws Exception {
		save(aItem);
	}

	@Override
	public void save(IItem aItem) throws Exception {
		Assert.notNull(aItem, "The item argument cannot be null!");
		Assert.isTrue(mType.equals(aItem.getType()), "The item type is not supported on the collection!");
		aItem.validate();

		ItemStorageEventManager.onBeforeSaveItem(aItem, this);

		if (!mData.contains(aItem)) {
			mData.add(aItem);
		}

		ItemStorageEventManager.onItemSaved(aItem, this);
	}

	@Override
	public IItem remove(String aPK) throws Exception {
		for (Iterator<IItem> lIt = mData.iterator(); lIt.hasNext();) {
			IItem lItem = lIt.next();
			if (lItem.getPK().equals(aPK)) {
				lIt.remove();
				ItemStorageEventManager.onItemRemoved(lItem, this);

				return lItem;
			}
		}

		return null;
	}

	@Override
	public List<IItem> list(int aOffset, int aLength) throws Exception {
		Assert.notNull(aOffset, "The offset argument cannot be null!");
		Assert.notNull(aLength, "The length argument cannot be null!");
		Assert.isTrue(aOffset >= 0 && aOffset <= size(),
				"Index out of bound!");
		Assert.isTrue(aLength > 0, "Invalid length value. Expected: length > 0!");

		FastList<IItem> lList = new FastList<IItem>();
		try {
			while (aLength > 0) {
				lList.add(mData.get(aOffset++));
				aLength--;
			}
		} catch (IndexOutOfBoundsException Ex) {
			// this exception is expected ;)
		}

		return lList;
	}

	@Override
	public IItem findByPK(String aPK) throws Exception {
		for (Iterator<IItem> lIt = mData.iterator(); lIt.hasNext();) {
			IItem lItem = lIt.next();
			if (lItem.getPK().equals(aPK)) {
				return lItem;
			}
		}

		return null;
	}

	@Override
	public List<IItem> find(String aAttribute, Object aValue, int aOffset, int aLength) throws Exception {
		IItemDefinition lDef = mItemFactory.getDefinition(mType);
		Assert.isTrue(lDef.containsAttribute(aAttribute),
				"The atribute '" + aAttribute + "' does not exists on item of type '" + mType + "'!");

		Assert.notNull(aOffset, "The offset argument cannot be null!");
		Assert.notNull(aLength, "The length argument cannot be null!");
		Assert.isTrue(aOffset >= 0 && aOffset <= size(),
				"Index out of bound!");
		Assert.isTrue(aLength > 0, "Invalid length value. Expected: length > 0!");

		int lIndex = 0;
		List<IItem> lFound = new FastList<IItem>();
		try {
			while (aLength > 0) {
				boolean lMatch = false;
				Object lAttrValue = mData.get(lIndex).get(aAttribute);

				if (null == lAttrValue && lAttrValue == aValue) {
					// both null match
					lMatch = true;
				} else if (null == aValue) {
					// if null at this point: not match
					lMatch = false;
				} else if (lDef.getAttributeTypes().get(aAttribute).equals("string")) {
					// if string support regular expressions
					if (lAttrValue.toString().matches((String) aValue)) {
						lMatch = true;
					}
				} else if (lAttrValue.equals(aValue)) {
					// if objects use equals
					lMatch = true;
				}

				if (lMatch) {
					lFound.add(mData.get(lIndex));
					aLength--;
				}
				
				lIndex++;
			}
		} catch (IndexOutOfBoundsException Ex) {
			// this exception is expected ;)
		}

		return lFound;
	}

	@Override
	public void clear() throws Exception {
		mData.clear();
		ItemStorageEventManager.onStorageCleaned(this);
	}

	@Override
	public void initialize() throws Exception {
		if (!mContainer.containsKey(mName) || null == mContainer.get(mName)) {
			mContainer.put(mName, new FastList<IItem>());
			mTypes.put(mName, mType);
		}

		Assert.isTrue(mType.equals(mTypes.get(mName)), "Invalid storage 'type' argument. "
				+ "A storage with name '" + mName + "' already exists with a different type!");
		mData = mContainer.get(mName);
	}

	@Override
	public Integer size() {
		return mData.size();
	}

	@Override
	public boolean exists(String aPK) {
		for (Iterator<IItem> lIt = mData.iterator(); lIt.hasNext();) {
			IItem lItem = lIt.next();
			if (lItem.getPK().equals(aPK)) {
				return true;
			}
		}

		return false;
	}
}
