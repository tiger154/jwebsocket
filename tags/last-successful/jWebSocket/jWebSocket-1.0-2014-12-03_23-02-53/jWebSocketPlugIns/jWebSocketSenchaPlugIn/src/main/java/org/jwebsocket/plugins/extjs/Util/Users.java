//	---------------------------------------------------------------------------
//	jWebSocket - Customers for ExtJS plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.extjs.Util;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Osvaldo Aguilar Lauzurique Lauzurique, Alexander Rojas Hernandez
 */
public class Users {

	private LinkedList<User> mUsers;
	private Integer mCount;

	/**
	 *
	 */
	public Users() {
		mCount = 0;
		mUsers = new LinkedList<User>();
		try {
			add(new User(mCount, "Alexander", "arojash@uci.cu", 24));
			add(new User(mCount, "Alexander", "a.schulze@jwebsocket.org", 40));
			add(new User(mCount, "Anuradha", "galianuradha@gmail.com", 25));
			add(new User(mCount, "Armando", "alsimon@uci.cu", 26));
			add(new User(mCount, "Carlos", "carlosfeyt@hab.uci.cu", 25));
			add(new User(mCount, "Carlos", "ckcespedes@uci.cu", 25));
			add(new User(mCount, "Daimi", "dmederos@hab.uci.cu", 24));
			add(new User(mCount, "Dariel", "dnoa@uci.cu", 25));
			add(new User(mCount, "Eduardo", "ebourzach@uci.cu", 26));

			add(new User(mCount, "Johannes", "johannes.schoenborn@gmail.com", 25));
			add(new User(mCount, "Johannes", "j.smutny@gmail.com", 25));
			add(new User(mCount, "Lester", "lzaila@hab.uci.cu", 25));
			add(new User(mCount, "Lisdey", "lperez@hab.uci.cu", 23));
			add(new User(mCount, "Merly", "mlopez@hab.uci.cu", 24));
			add(new User(mCount, "Marcos ", "magonzalez@hab.uci.cu", 25));
			add(new User(mCount, "Marta ", "mrodriguez@hab.uci.cu", 25));
			add(new User(mCount, "Mayra", "memaranon@hab.uci.cu", 25));
			add(new User(mCount, "Orlando", "omiranda@uci.cu", 25));
			add(new User(mCount, "Osvaldo", "oaguilar@uci.cu", 25));

			add(new User(mCount, "Prashant", "prashantkhanal@gmail.com", 25));
			add(new User(mCount, "Puran", "mailtopuran@gmail.com", 25));
			add(new User(mCount, "Quentin", "quentin.ambard@gmail.com", 25));
			add(new User(mCount, "Rebecca", "r.schulze@jwebsocket.org", 23));
			add(new User(mCount, "Rolando", "rbetancourt@hab.uci.cu", 24));
			add(new User(mCount, "Rolando", "rsantamaria@hab.uci.cu", 26));
			add(new User(mCount, "Roylandi", "rgpujol@hab.uci.cu", 25));
			add(new User(mCount, "Unni", "unnivm@gmail.com", 25));
			add(new User(mCount, "Victor", "vbarzana@uci.cu", 25));
			add(new User(mCount, "Yamila", "yvigil@hab.uci.cu", 27));

			add(new User(mCount, "Yasmany", "ynbosh@hab.uci.cu", 24));
		} catch (Exception ex) {
		}

	}

	/**
	 *
	 * @param aCustomer
	 * @throws Exception
	 */
	public final void add(User aCustomer) throws Exception {
		for (User lCustomer : mUsers) {
			if (lCustomer.getEmail().equals(aCustomer.getEmail())) {
				throw new Exception("customer duplicated");
			}
		}
		mUsers.add(aCustomer);
		mCount++;
	}

	/**
	 *
	 * @return
	 */
	public LinkedList<User> getCustomers() {
		return mUsers;
	}

	/**
	 *
	 * @param aStart
	 * @param aLimit
	 * @return
	 */
	public List<User> getSubList(int aStart, int aLimit) {
		if (aLimit > mUsers.size()) {
			aLimit -= aLimit - mUsers.size();
		}

		return mUsers.subList(aStart, aLimit);

	}

	/**
	 *
	 * @return
	 */
	public Integer getSize() {
		return mUsers.size();
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public User getCustomer(Integer aId) {
		User lCustomer = null;
		for (User lCustDef : mUsers) {
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
		for (User lCustDef : mUsers) {
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
		for (User lCustDef : mUsers) {
			if (lCustDef.getId().equals(aId)) {
				mUsers.remove(lCustDef);
				return true;
			}
		}
		return false;
	}
}
