//  ---------------------------------------------------------------------------
//  jWebSocket - ILogsManager (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
