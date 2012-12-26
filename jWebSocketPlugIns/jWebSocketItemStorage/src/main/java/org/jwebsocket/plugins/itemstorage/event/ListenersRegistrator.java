package org.jwebsocket.plugins.itemstorage.event;

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageListener;

/**
 *
 * @author kyberneees
 */
public class ListenersRegistrator {

	private List<IItemStorageListener> mListeners = new FastList<IItemStorageListener>();

	public List<IItemStorageListener> getListeners() {
		return mListeners;
	}

	public void setListeners(List<IItemStorageListener> aListeners) {
		mListeners = aListeners;
	}

	public void registerAll() {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			IItemStorageListener lListener = lIt.next();
			ItemStorageEventManager.addListener(lListener);
		}
	}
}
