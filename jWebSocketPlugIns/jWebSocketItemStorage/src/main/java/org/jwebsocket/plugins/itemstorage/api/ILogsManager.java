package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;
import java.util.Map;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author kyberneees
 */
public interface ILogsManager extends IInitializable {

	void logAction(Map<String, Object> aAction) throws Exception;

	void clearAll() throws Exception;

	void clearUntil(long aTime) throws Exception;

	void clearItemLogs(String aCollectionName, String aItemPK) throws Exception;

	void clearItemLogs(String aCollectionName) throws Exception;

	void clearCollectionLogs(String aCollectionName) throws Exception;

	List<Map> getItemLogs(String aCollectionName, String aItemPK, int aOffset, int aLength) throws Exception;

	List<Map> getCollectionLogs(String aCollectionName, int aOffset, int aLength) throws Exception;

	Long size() throws Exception;

	Long size(String aElementType, String aActionId) throws Exception;

	Long size(String aElementType, String aCollection, String aActionId) throws Exception;
}
