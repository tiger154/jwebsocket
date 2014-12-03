//	---------------------------------------------------------------------------
//	jWebSocket - OrmLiteProvider (Community Edition, CE)
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
package org.jwebsocket.storage.ormlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.IStorageProvider;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class OrmLiteStorageProvider implements IStorageProvider, IInitializable {

	private Dao<EntryEntity, String> mEntries;

	/**
	 *
	 * @param aEntries
	 */
	public OrmLiteStorageProvider(Dao<EntryEntity, String> aEntries) {
		Assert.notNull(aEntries, "The 'entries', argument cannot be null!");
		this.mEntries = aEntries;
	}

	@Override
	public void initialize() throws Exception {
		if (!mEntries.isTableExists()) {
			TableUtils.createTable(mEntries.getConnectionSource(), EntryEntity.class);
		}
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public IBasicStorage<String, Object> getStorage(String aName) throws Exception {
		OrmLiteStorage lStorage = new OrmLiteStorage(aName, mEntries);
		lStorage.initialize();

		return lStorage;
	}

	@Override
	public void removeStorage(String aName) throws Exception {
		getStorage(aName).clear();
	}
}
