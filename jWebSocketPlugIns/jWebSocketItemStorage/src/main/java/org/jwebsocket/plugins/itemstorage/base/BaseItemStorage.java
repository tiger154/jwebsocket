//	---------------------------------------------------------------------------
//	jWebSocket - BaseItemStorage (Community Edition, CE)
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

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.math.RandomUtils;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
abstract public class BaseItemStorage implements IItemStorage {

	/**
	 *
	 */
	protected String mName;

	/**
	 *
	 */
	protected String mType;

	/**
	 *
	 */
	protected IItemFactory mItemFactory;

	/**
	 *
	 * @param aName
	 * @param aType
	 * @param aItemFactory
	 */
	public BaseItemStorage(String aName, String aType, IItemFactory aItemFactory) {
		this.mName = aName;
		this.mType = aType;
		this.mItemFactory = aItemFactory;

		// validating
		Assert.notNull(aName, "The storage name argument cannot be null!");
		Assert.notNull(aType, "The storage type argument cannot be null!");
		Assert.notNull(aItemFactory, "The storage item factory argument cannot be null!");
	}

	@Override
	public IItem findRandom() throws Exception {
		if (0 == size()) {
			return null;
		}
		if (1 == size()) {
			return list(0, 1).get(0);
		} else {
			return list(RandomUtils.nextInt(size() - 1), 1).get(0);
		}
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public String getItemType() {
		return mType;
	}

	@Override
	public List<IItem> list() throws Exception {
		return list(0, size());
	}

	@Override
	public List<IItem> list(int aOffset) throws Exception {
		return list(aOffset, size());
	}

	@Override
	public List<IItem> find(String aAttribute, Object aValue) throws Exception {
		return find(aAttribute, aValue, 0, size());
	}

	@Override
	public List<IItem> find(String aAttribute, Object aValue, Map<String, Boolean> aOrderBy, int aOffset, int aLength) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<IItem> find(Map<String, Object> aAttrsValues, Map<String, Boolean> aOrderBy, int aOffset, int aLength) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public IItemFactory getItemFactory() {
		return mItemFactory;
	}

	@Override
	public Integer size(Map<String, Object> aAttrsValues) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void initialize() throws Exception {
		Assert.isTrue(mItemFactory.supportsType(getItemType()), "Item type not supported!");
	}

	@Override
	public void shutdown() throws Exception {
	}
}
