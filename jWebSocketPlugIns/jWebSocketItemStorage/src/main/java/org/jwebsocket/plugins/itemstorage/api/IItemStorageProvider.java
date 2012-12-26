package org.jwebsocket.plugins.itemstorage.api;

import org.jwebsocket.api.IInitializable;

/**
 *
 * @author kyberneees
 */
public interface IItemStorageProvider extends IInitializable {

	IItemFactory getItemFactory();

	IItemStorage getItemStorage(String aName, String aType) throws Exception;

	void removeItemStorage(String aName) throws Exception;
}
