//	---------------------------------------------------------------------------
//	jWebSocket - Item (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javolution.util.FastMap;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemDefinition;
import org.jwebsocket.token.Token;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class Item implements IItem {

	private final Map<String, Object> mAttrs
			= new FastMap<String, Object>().shared();

	/**
	 *
	 */
	public static final String ATTR_ATTRS = "attrs";

	/**
	 *
	 */
	public static final String ATTR_TARGET_PK = "targetPK";

	/**
	 *
	 */
	public static final String ATTR_PK = "pk";

	/**
	 *
	 */
	public static final String ATTR_INTERNAL_ID = ItemDefinition.ATTR_UNIQUE_ID;
	private final IItemDefinition mDefinition;
	private final Map<String, Object> mUpdate
			= new FastMap<String, Object>().shared();

	/**
	 *
	 * @param aDefinition
	 */
	public Item(IItemDefinition aDefinition) {
		Assert.notNull(aDefinition, "Item definition cannot be null!");
		mDefinition = aDefinition;
	}

	@Override
	public String getType() {
		return mDefinition.getType();
	}

	@Override
	public String getPK() {
		return (String) mAttrs.get(getDefinition().getPrimaryKeyAttribute());
	}

	@Override
	public IItem set(String aAttribute, Object aValue) {
		// executing type checks
		beforeSet(aAttribute, aValue);
		// saving attribute
		mAttrs.put(aAttribute, aValue);

		return this;
	}

	@Override
	public IItem setAll(Map<String, Object> aMap) {
		for (Map.Entry<String, Object> lEntry : aMap.entrySet()) {
			set(lEntry.getKey(), lEntry.getValue());
		}

		return this;
	}

	@Override
	public Object get(String aAttribute) {
		return mAttrs.get(aAttribute);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap(mAttrs);
	}

	@Override
	public IItemDefinition getDefinition() {
		return mDefinition;
	}

	@Override
	public void writeToToken(Token aToken) {
		toMap(aToken.getMap());
	}

	@Override
	public void readFromToken(Token aToken) {
		fromMap(aToken.getMap());
	}

	private void beforeSet(String aAttribute, Object aValue) {
		String lType = "";
		if (aValue instanceof String) {
			lType = "string";
		} else if (aValue instanceof Integer) {
			lType = "integer";
		} else if (aValue instanceof Double) {
			lType = "double";
		} else if (aValue instanceof Long) {
			lType = "long";
		} else if (aValue instanceof Boolean) {
			lType = "boolean";
		}

		String lRequiredAttrType = getDefinition().getAttributeTypes().get(aAttribute);
		Assert.notNull(lRequiredAttrType, "Missing '" + aAttribute + "' attribute definition!");
		Assert.isTrue(lRequiredAttrType.startsWith(lType), "Invalid value type for '"
				+ aAttribute + "' attribute. Expected type '" + lRequiredAttrType + "'!");
	}

	/**
	 * Adjust JSON numeric types conversion according to the item definition
	 *
	 * @param aAttrs
	 * @param aDef
	 */
	public static void adjustJSONTypes(Map<String, Object> aAttrs, IItemDefinition aDef) {
		for (String lKey : aDef.getAttributeTypes().keySet()) {
			if (aDef.getAttributeTypes().get(lKey).startsWith("double")) {
				if (aAttrs.get(lKey) instanceof Integer) {
					aAttrs.put(lKey, ((Integer) aAttrs.get(lKey)).doubleValue());
				}
			} else if (aDef.getAttributeTypes().get(lKey).startsWith("long")) {
				if (aAttrs.get(lKey) instanceof Integer) {
					aAttrs.put(lKey, ((Integer) aAttrs.get(lKey)).longValue());
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Item{" + "attrs=" + mAttrs + '}';
	}

	@Override
	public void validate() {
		// setting internal unique id if missing
		if (!mAttrs.containsKey(ATTR_INTERNAL_ID)
				&& getDefinition().getPrimaryKeyAttribute().equals(ATTR_INTERNAL_ID)) {
			mAttrs.put(ATTR_INTERNAL_ID, UUID.randomUUID().toString());
		}

		// checking for missing PK
		Assert.notNull(getPK(), "The item PK argument value cannot be null!");
	}

	@Override
	public void toMap(Map<String, Object> aMap) {
		aMap.put(ATTR_ATTRS, mAttrs);
		aMap.put(ItemDefinition.ATTR_TYPE, getDefinition().getType());
		aMap.put(ATTR_PK, getPK());
	}

	@Override
	public void fromMap(Map<String, Object> aMap) {
		mAttrs.putAll((Map) aMap.get(ATTR_ATTRS));
	}

	@Override
	public Map<String, Object> getUpdate() {
		Map<String, Object> lUpdate = new HashMap<String, Object>();
		toMap(lUpdate);
		if (mUpdate.containsKey(ATTR_ATTRS)) {
			lUpdate.put(ATTR_ATTRS, mUpdate.get(ATTR_ATTRS));
		} else {
			lUpdate.remove(ATTR_ATTRS);
		}

		return lUpdate;
	}

	@Override
	public void setUpdate(Map<String, Object> aUpdate) {
		mUpdate.put(ATTR_ATTRS, aUpdate);
	}
}
