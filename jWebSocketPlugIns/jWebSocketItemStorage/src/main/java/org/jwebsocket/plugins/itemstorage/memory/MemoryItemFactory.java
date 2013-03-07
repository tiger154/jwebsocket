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
 * @author kyberneees
 */
public class MemoryItemFactory implements IItemFactory {

	private Map<String, IItemDefinition> mDefinitions = new FastMap<String, IItemDefinition>().shared();

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
		for (Iterator<IItemDefinition> lIt = aDefinitions.iterator(); lIt.hasNext();) {
			IItemDefinition lItem = lIt.next();
			registerDefinition(lItem);
		}
	}

	@Override
	public List<IItemDefinition> listDefinitions(int aOffset, int aLength) {
		int lTotalSize = mDefinitions.size();
		Assert.notNull(aOffset, "The offset argument cannot be null!");
		Assert.notNull(aLength, "The length argument cannot be null!");
		Assert.isTrue(aOffset >= 0 && aOffset < lTotalSize,
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