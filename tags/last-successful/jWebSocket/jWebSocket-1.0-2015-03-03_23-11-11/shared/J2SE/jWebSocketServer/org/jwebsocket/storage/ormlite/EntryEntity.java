//	---------------------------------------------------------------------------
//	jWebSocket - EntryEntity (Community Edition, CE)
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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 *
 * @author Rolando Santamaria Maso
 */
@DatabaseTable(tableName = "jws_storages")
public class EntryEntity {

	@DatabaseField(generatedId = true)
	private Long id;
	@DatabaseField(columnName = FIELD_KEY)
	private String key;
	@DatabaseField(columnName = FIELD_VALUE, dataType = DataType.LONG_STRING)
	private String value;
	@DatabaseField(index = true, columnName = FIELD_STORAGE_NAME)
	private String storageName;
	/**
	 *
	 */
	public static final String FIELD_STORAGE_NAME = "storageName";
	/**
	 *
	 */
	public static final String FIELD_KEY = "key";
	/**
	 *
	 */
	public static final String FIELD_VALUE = "value";

	/**
	 *
	 * @return
	 */
	public Long getId() {
		return id;
	}

	/**
	 *
	 * @param aId
	 */
	public void setId(Long aId) {
		this.id = aId;
	}

	/**
	 *
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 *
	 * @param aKey
	 */
	public void setKey(String aKey) {
		key = aKey;
	}

	/**
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 *
	 * @param aValue
	 */
	public void setValue(String aValue) {
		value = aValue;
	}

	/**
	 *
	 * @return
	 */
	public String getStorageName() {
		return storageName;
	}

	/**
	 *
	 * @param aStorageName
	 */
	public void setStorageName(String aStorageName) {
		storageName = aStorageName;
	}
}
