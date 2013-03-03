//	---------------------------------------------------------------------------
//	jWebSocket - Customers for ExtJS plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.extjs;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernandez
 */
public class Customers {

	private LinkedList<CustomerDef> mCustomers;
	private Integer mCount;

	/**
	 *
	 */
	public Customers() {
		mCount = 0;
		mCustomers = new LinkedList<CustomerDef>();
		try {
			add(new CustomerDef(mCount, "Alexander", "arojash@uci.cu", 24));
			add(new CustomerDef(mCount, "Alexander", "a.schulze@jwebsocket.org", 40));
			add(new CustomerDef(mCount, "Anuradha", "galianuradha@gmail.com", 25));
			add(new CustomerDef(mCount, "Armando", "alsimon@uci.cu", 26));
			add(new CustomerDef(mCount, "Carlos", "carlosfeyt@hab.uci.cu", 25));
			add(new CustomerDef(mCount, "Carlos", "ckcespedes@uci.cu", 25));
			add(new CustomerDef(mCount, "Daimi", "dmederos@hab.uci.cu", 24));
			add(new CustomerDef(mCount, "Dariel", "dnoa@uci.cu", 25));
			add(new CustomerDef(mCount, "Eduardo", "ebourzach@uci.cu", 26));

			add(new CustomerDef(mCount, "Johannes", "johannes.schoenborn@gmail.com", 25));
			add(new CustomerDef(mCount, "Johannes", "j.smutny@gmail.com", 25));
			add(new CustomerDef(mCount, "Lester", "lzaila@hab.uci.cu", 25));
			add(new CustomerDef(mCount, "Lisdey", "lperez@hab.uci.cu", 23));
			add(new CustomerDef(mCount, "Merly", "mlopez@hab.uci.cu", 24));
			add(new CustomerDef(mCount, "Marcos ", "magonzalez@hab.uci.cu", 25));
			add(new CustomerDef(mCount, "Marta ", "mrodriguez@hab.uci.cu", 25));
			add(new CustomerDef(mCount, "Mayra", "memaranon@hab.uci.cu", 25));
			add(new CustomerDef(mCount, "Orlando", "omiranda@uci.cu", 25));
			add(new CustomerDef(mCount, "Osvaldo", "oaguilar@uci.cu", 25));

			add(new CustomerDef(mCount, "Prashant", "prashantkhanal@gmail.com", 25));
			add(new CustomerDef(mCount, "Puran", "mailtopuran@gmail.com", 25));
			add(new CustomerDef(mCount, "Quentin", "quentin.ambard@gmail.com", 25));
			add(new CustomerDef(mCount, "Rebecca", "r.schulze@jwebsocket.org", 23));
			add(new CustomerDef(mCount, "Rolando", "rbetancourt@hab.uci.cu", 24));
			add(new CustomerDef(mCount, "Rolando", "rsantamaria@hab.uci.cu", 26));
			add(new CustomerDef(mCount, "Roylandi", "rgpujol@hab.uci.cu", 25));
			add(new CustomerDef(mCount, "Unni", "unnivm@gmail.com", 25));
			add(new CustomerDef(mCount, "Victor", "vbarzana@uci.cu", 25));
			add(new CustomerDef(mCount, "Yamila", "yvigil@hab.uci.cu", 27));

			add(new CustomerDef(mCount, "Yasmany", "ynbosh@hab.uci.cu", 24));
		} catch (Exception ex) {
		}

	}

	/**
	 *
	 * @param aCustomer
	 * @throws Exception
	 */
	public void add(CustomerDef aCustomer) throws Exception {
		for (CustomerDef lCustomer : mCustomers) {
			if (lCustomer.getEmail().equals(aCustomer.getEmail())) {
				throw new Exception("customer duplicated");
			}
		}
		mCustomers.add(aCustomer);
		mCount++;
	}

	/**
	 *
	 * @return
	 */
	public LinkedList<CustomerDef> getCustomers() {
		return mCustomers;
	}

	/**
	 *
	 * @param aStart
	 * @param aLimit
	 * @return
	 */
	public List<CustomerDef> getSubList(int aStart, int aLimit) {
		if (aLimit > mCustomers.size()) {
			aLimit -= aLimit - mCustomers.size();
		}

		return mCustomers.subList(aStart, aLimit);

	}

	/**
	 *
	 * @return
	 */
	public Integer getSize() {
		return mCustomers.size();
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public CustomerDef getCustomer(Integer aId) {
		CustomerDef lCustomer = null;
		for (CustomerDef lCustDef : mCustomers) {
			if (lCustDef.getId().equals(aId)) {
				return lCustDef;
			}
		}
		return lCustomer;
	}

	/**
	 *
	 * @param aCount
	 */
	public void setCount(Integer aCount) {
		this.mCount = aCount;
	}

	/**
	 *
	 * @return
	 */
	public Integer getCount() {
		return mCount;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public boolean findCustomer(Integer aId) {
		for (CustomerDef lCustDef : mCustomers) {
			if (lCustDef.getId().equals(aId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public boolean deleteCustomer(Integer aId) {
		for (CustomerDef lCustDef : mCustomers) {
			if (lCustDef.getId().equals(aId)) {
				mCustomers.remove(lCustDef);
				return true;
			}
		}
		return false;
	}
}
