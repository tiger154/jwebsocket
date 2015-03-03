//	---------------------------------------------------------------------------
//	jWebSocket - OrmLiteStorage (Community Edition, CE)
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
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.TableUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.storage.BaseStorage;
import org.jwebsocket.util.MapAppender;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class OrmLiteStorage extends BaseStorage<String, Object> {

	private final Dao<EntryEntity, String> mEntries;
	private final String mName;
	Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aName
	 * @param aEntries
	 */
	public OrmLiteStorage(String aName, Dao<EntryEntity, String> aEntries) {
		mEntries = aEntries;
		mName = aName;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public void setName(String aName) throws Exception {
		UpdateBuilder<EntryEntity, String> lUEntries = mEntries.updateBuilder();
		lUEntries.updateColumnValue(EntryEntity.FIELD_STORAGE_NAME, aName)
				.where().eq(EntryEntity.FIELD_STORAGE_NAME, mName);
		mEntries.updateBuilder().update();
	}

	@Override
	public Object get(Object aKey) {
		Assert.notNull(aKey, "The 'key', argument cannot be null!");

		try {
			EntryEntity lQuery = mEntries.queryBuilder().where().eq(EntryEntity.FIELD_STORAGE_NAME, mName)
					.and().eq(EntryEntity.FIELD_KEY, aKey)
					.queryForFirst();

			return entityToValue(lQuery);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "retrieving data from database..."));
			//TODO: Should we silence this exception: 
			Assert.isTrue(false, lEx.getMessage());
			return null;
		}
	}

	@Override
	public Object put(String aKey, Object aValue) {
		Assert.notNull(aKey, "The 'key', argument cannot be null!");

		EntryEntity lEntry;
		try {
			if (containsKey(aKey)) {
				lEntry = mEntries.queryBuilder().where().eq(EntryEntity.FIELD_STORAGE_NAME, mName)
						.and().eq(EntryEntity.FIELD_KEY, aKey)
						.queryForFirst();
			} else {
				lEntry = new EntryEntity();
				lEntry.setKey(aKey);
				lEntry.setStorageName(mName);
			}
			lEntry.setValue(valueToJSON(aValue));

			mEntries.createOrUpdate(lEntry);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "storing data to database..."));
			//TODO: Should we silence this exception: 
			Assert.isTrue(false, lEx.getMessage());
		}

		return aValue;
	}

	@Override
	public boolean containsKey(Object aKey) {
		Assert.notNull(aKey, "The 'key', argument cannot be null!");

		long lCount = 0;
		try {
			lCount = mEntries.queryBuilder().where().eq(EntryEntity.FIELD_STORAGE_NAME, mName)
					.and().eq(EntryEntity.FIELD_KEY, aKey)
					.countOf();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "retrieving data from database..."));
			//TODO: Should we silence this exception: 
			Assert.isTrue(false, lEx.getMessage());
		}

		return lCount > 0;
	}

	@Override
	public Object remove(Object aKey) {
		Assert.notNull(aKey, "The 'key', argument cannot be null!");

		try {
			if (containsKey(aKey)) {
				Object lValue = get(aKey);

				DeleteBuilder<EntryEntity, String> lDEntries = mEntries.deleteBuilder();
				lDEntries.where().eq(EntryEntity.FIELD_STORAGE_NAME, mName)
						.and().eq(EntryEntity.FIELD_KEY, aKey);
				lDEntries.delete();

				return lValue;
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "removing data from database..."));
			//TODO: Should we silence this exception: 
			Assert.isTrue(false, lEx.getMessage());
		}

		return null;
	}

	@Override
	public Set<String> keySet() {
		List<EntryEntity> lQuery = null;
		try {
			lQuery = mEntries.queryBuilder().selectColumns(EntryEntity.FIELD_KEY)
					.where().eq(EntryEntity.FIELD_STORAGE_NAME, mName)
					.query();

		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "retrieving data from database..."));
			//TODO: Should we silence this exception: 
			Assert.isTrue(false, lEx.getMessage());
		}

		Set<String> lSet = new HashSet<String>();
		if (null != lQuery && !lQuery.isEmpty()) {
			for (EntryEntity lEntry : lQuery) {
				lSet.add(lEntry.getKey());
			}
		}

		return lSet;
	}

	@Override
	public void clear() {
		try {
			DeleteBuilder<EntryEntity, String> lDEntries = mEntries.deleteBuilder();
			lDEntries.where().eq(EntryEntity.FIELD_STORAGE_NAME, mName);
			lDEntries.delete();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "clearing data from database..."));
			//TODO: Should we silence this exception: 
			Assert.isTrue(false, lEx.getMessage());
		}
	}

	@Override
	public void initialize() throws Exception {
		Assert.notNull(mName, "The 'name', argument cannot be null!");
		Assert.notNull(mEntries, "The 'entries', argument cannot be null!");

		if (!mEntries.isTableExists()) {
			TableUtils.createTable(mEntries.getConnectionSource(), EntryEntity.class);
		}
	}

	private String valueToJSON(Object aValue) {
		if (null == aValue) {
			return null;
		}
		StringBuffer lBuffer = new StringBuffer();
		JSONProcessor.objectToJSONString(new MapAppender().append("value", aValue).getMap(), lBuffer);

		return lBuffer.toString();
	}

	private Object entityToValue(EntryEntity lQuery) {
		if (null == lQuery || null == lQuery.getValue()) {
			return null;
		}

		String JSON = lQuery.getValue();
		return JSONProcessor.JSONStringToToken(JSON).getObject("value");
	}
}
