//	---------------------------------------------------------------------------
//	jWebSocket - MemoryItemFactory (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.memory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemDefinition;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.item.Item;
import org.jwebsocket.plugins.itemstorage.item.ItemDefinition;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class MemoryItemFactory implements IItemFactory {

	private final Map<String, IItemDefinition> mDefinitions = new FastMap<String, IItemDefinition>().shared();

	@Override
	public IItem getItemPrototype(String aType) throws Exception {
		IItemDefinition lDef = getDefinition(aType);
		Assert.notNull(lDef, "Item definition for type '" + aType + "'not found!");

		return new Item(lDef);
	}

	@Override
	public IItemDefinition getDefinitionPrototype() {
		return new ItemDefinition();
	}

	@Override
	public IItemDefinition getDefinition(String aType) {
		return mDefinitions.get(aType);
	}

	@Override
	public Boolean supportsType(String aType) {
		return mDefinitions.containsKey(aType);
	}

	@Override
	public void registerDefinition(IItemDefinition aDefinition) throws Exception {
		aDefinition.validate();
		mDefinitions.put(aDefinition.getType(), aDefinition);
	}

	@Override
	public IItemDefinition removeDefinition(String aType) {
		return mDefinitions.remove(aType);
	}

	@Override
	public void setDefinitions(Set<IItemDefinition> aDefinitions) throws Exception {
		for (IItemDefinition lItem : aDefinitions) {
			registerDefinition(lItem);
		}
	}

	@Override
	public List<IItemDefinition> listDefinitions(int aOffset, int aLength) {
		int lTotalSize = mDefinitions.size();
		Assert.notNull(aOffset, "The offset argument cannot be null!");
		Assert.notNull(aLength, "The length argument cannot be null!");
		Assert.isTrue(aOffset >= 0 && aOffset <= lTotalSize,
				"Index out of bound!");
		Assert.isTrue(aLength > 0, "Invalid length value. Expected: length > 0!");

		FastList<IItemDefinition> lList = new FastList<IItemDefinition>();
		Iterator<IItemDefinition> lItems = mDefinitions.values().iterator();
		while (lItems.hasNext() && aOffset > 0) {
			lItems.next();
			aOffset--;
		}
		while (lItems.hasNext() && aLength > 0) {
			lList.add(lItems.next());
			aOffset--;
		}

		return lList;
	}

	@Override
	public int size() throws Exception {
		return mDefinitions.size();
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}
}
