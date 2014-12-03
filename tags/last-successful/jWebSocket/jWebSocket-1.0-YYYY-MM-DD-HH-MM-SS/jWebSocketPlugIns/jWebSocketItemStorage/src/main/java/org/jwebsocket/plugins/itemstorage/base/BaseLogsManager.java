//	---------------------------------------------------------------------------
//	jWebSocket - BaseLogsManager (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jwebsocket.plugins.itemstorage.api.ILogsManager;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class BaseLogsManager implements ILogsManager {

	/**
	 *
	 */
	public static final String ATTR_USER = "user";

	/**
	 *
	 */
	public static final String ATTR_ID = "id";

	/**
	 *
	 */
	public static final String ATTR_ETYPE = "eType";

	/**
	 *
	 */
	public static final String ATTR_ACTION = "action";

	/**
	 *
	 */
	public static final String ATTR_TIME = "time";

	/**
	 *
	 */
	public static final String ATTR_INFO = "info";

	/**
	 *
	 */
	public static final String ATTR_COLLECTION = "collection";

	/**
	 *
	 */
	public static final String ETYPE_ITEM = "item";

	/**
	 *
	 */
	public static final String ETYPE_COLLECTION = "collection";

	/**
	 *
	 * @param aElementType
	 * @param aId
	 * @param aAction
	 * @param aUser
	 * @param aInfo
	 * @return
	 */
	public static Map<String, Object> createActionPrototype(String aElementType, String aId, String aAction, String aUser, String aInfo) {
		HashMap<String, Object> lAction = new HashMap<String, Object>();
		lAction.put(ATTR_USER, aUser);
		lAction.put(ATTR_ID, aId);
		lAction.put(ATTR_ETYPE, aElementType);
		lAction.put(ATTR_ACTION, aAction);
		lAction.put(ATTR_TIME, System.currentTimeMillis());
		lAction.put(ATTR_INFO, aInfo);

		return lAction;
	}

	/**
	 *
	 * @param aElementType
	 * @param aId
	 * @param aAction
	 * @param aUser
	 * @return
	 */
	public static Map<String, Object> createActionPrototype(String aElementType, String aId, String aAction, String aUser) {
		return createActionPrototype(aElementType, aId, aAction, aUser, "");
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
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
	public List<Map> getItemLogs(String aCollectionName, String aItemPK,
			int aOffset, int aLength) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<Map> getCollectionLogs(String aCollectionName,
			int aOffset, int aLength) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Long size() throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Long size(String aElementType, String aActionId) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Long size(String aElementType, String aCollection, String aActionId) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
