package org.jwebsocket.plugins.itemstorage.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jwebsocket.plugins.itemstorage.api.ILogsManager;

/**
 *
 * @author kyberneees
 */
public class BaseLogsManager implements ILogsManager {

	public static String ATTR_USER = "user";
	public static String ATTR_ID = "id";
	public static String ATTR_ETYPE = "eType";
	public static String ATTR_ACTION = "action";
	public static String ATTR_TIME = "time";
	public static String ATTR_COLLECTION = "collection";
	public static String ETYPE_ITEM = "item";
	public static String ETYPE_COLLECTION = "collection";

	public static Map<String, Object> createActionPrototype(String aElementType, String aId, String aAction, String aUser) {
		HashMap<String, Object> lAction = new HashMap<String, Object>();
		lAction.put(ATTR_USER, aUser);
		lAction.put(ATTR_ID, aId);
		lAction.put(ATTR_ETYPE, aElementType);
		lAction.put(ATTR_ACTION, aAction);
		lAction.put(ATTR_TIME, System.currentTimeMillis());

		return lAction;
	}

	@Override
	public void logAction(Map<String, Object> aAction) throws Exception {
	}

	@Override
	public void clearAll() throws Exception {
	}

	@Override
	public void clearUntil(long aTime) throws Exception {
	}

	@Override
	public void clearItemLogs(String aCollectionName, String aItemPK) throws Exception {
	}

	@Override
	public void clearItemLogs(String aCollectionName) throws Exception {
	}

	@Override
	public void clearCollectionLogs(String aCollectionName) throws Exception {
	}

	@Override
	public List<Map> getItemLogs(String aCollectionName, String aItemPK, int aOffset, int aLength) throws Exception {
		return null;
	}

	@Override
	public List<Map> getCollectionLogs(String aCollectionName, int aOffset, int aLength) throws Exception {
		return null;
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public Long size() {
		return null;
	}

	@Override
	public Long size(String aElementType, String aActionId) {
		return null;
	}

	@Override
	public Long size(String aElementType, String aCollection, String aActionId) {
		return null;
	}
}
