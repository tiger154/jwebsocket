package org.jwebsocket.plugins.itemstorage.memory;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.plugins.itemstorage.api.IClientCollection;

/**
 *
 * @author kyberneees
 */
public class MemoryClientCollection implements IClientCollection {

	private List<String> mData = new FastList<String>();

	@Override
	public boolean contains(String aUID) {
		return mData.contains(aUID);
	}

	@Override
	public void add(String aUID) {
		if (!contains(aUID)) {
			mData.add(aUID);
		}
	}

	@Override
	public void remove(String aUID) {
		mData.remove(aUID);
	}

	@Override
	public List<String> getAll() {
		return mData;
	}

	@Override
	public void clear() {
		mData.clear();
	}

	@Override
	public int size() {
		return mData.size();
	}
}
