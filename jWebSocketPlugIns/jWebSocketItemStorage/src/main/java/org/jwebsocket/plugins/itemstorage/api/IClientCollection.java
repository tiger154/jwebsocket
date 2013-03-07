package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;

/**
 *
 * @author kyberneees
 */
public interface IClientCollection {

	/**
	 *
	 * @param aUID
	 * @return
	 */
	boolean contains(String aUID) throws Exception;

	/**
	 *
	 * @param aUID
	 */
	void add(String aUID) throws Exception;

	/**
	 *
	 * @param aUID
	 */
	void remove(String aUID) throws Exception;

	/**
	 *
	 * @return
	 */
	List<String> getAll() throws Exception;

	/**
	 *
	 */
	void clear() throws Exception;

	/**
	 * 
	 * @return 
	 */
	int size() throws Exception;
}
