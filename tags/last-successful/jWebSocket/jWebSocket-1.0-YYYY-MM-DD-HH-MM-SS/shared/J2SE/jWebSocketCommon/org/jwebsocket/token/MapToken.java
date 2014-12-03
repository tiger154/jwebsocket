//	---------------------------------------------------------------------------
//	jWebSocket MapToken (Community Edition, CE)
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
package org.jwebsocket.token;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.ITokenizable;

/**
 * A token is ...
 *
 * @author Alexander Schulze
 */
public class MapToken extends BaseToken implements Token {

	private Map<String, Object> mData = null;

	/**
	 * Creates a new empty instance of a token. The token does not contain any
	 * items.
	 */
	public MapToken() {
		mData = new FastMap<String, Object>();
	}

	/**
	 *
	 * @param aType
	 */
	public MapToken(String aType) {
		mData = new FastMap<String, Object>();
		setType(aType);
	}

	/**
	 *
	 * @param aMap
	 */
	public MapToken(FastMap<String, Object> aMap) {
		mData = aMap;
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 */
	public MapToken(String aNS, String aType) {
		mData = new FastMap<String, Object>();
		setNS(aNS);
		setType(aType);
	}

	@Override
	public void clear() {
		mData.clear();
	}

	@Override
	public void set(ITokenizable aTokenizable) {
		aTokenizable.writeToToken(this);
	}

	/**
	 *
	 * @param aMap
	 */
	@Override
	public void setMap(Map aMap) {
		if (null != aMap) {
			mData = aMap;
		}
	}

	/**
	 *
	 *
	 * @return
	 */
	@Override
	public Map<String, Object> getMap() {
		return mData;
	}

	private Object getValue(Object aValue) {
		if (aValue instanceof MapToken) {
			aValue = ((MapToken) aValue).getMap();
		} else if (aValue instanceof Collection) {
			List lList = new FastList();
			for (Object lItem : (Collection) aValue) {
				lList.add(getValue(lItem));
			}
			aValue = lList;
		} else if (aValue instanceof Map) {
			Map lMap = new FastMap();
			for (Entry<Object, Object> lItem : ((Map<Object, Object>) aValue).entrySet()) {
				lMap.put(lItem.getKey().toString(), getValue(lItem.getValue()));
			}
			aValue = lMap;
		} else if (aValue instanceof Object[]) {
			List lList = new FastList();
			Object[] lOA = (Object[]) aValue;
			for (int lIdx = 0; lIdx < lOA.length; lIdx++) {
				lList.add(getValue(lOA[lIdx]));
			}
			aValue = lList;
		}
		return aValue;
	}

	/**
	 * puts a new key/value pair into the token, in other words it adds a new
	 * item to the token.
	 *
	 * @param aKey key of the the token item.
	 * @param aValue value of the token item.
	 */
	private void put(String aKey, Object aValue) {
		mData.put(aKey, getValue(aValue));
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	private Object get(String aKey) {
		return mData.get(aKey);
	}

	/**
	 *
	 * @param aKey
	 */
	@Override
	public void remove(String aKey) {
		mData.remove(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public String getString(String aKey, String aDefault) {
		String lResult;
		try {
			lResult = (String) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 */
	@Override
	public void setString(String aKey, String aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public String getString(String aKey) {
		return getString(aKey, null);
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Integer getInteger(String aKey, Integer aDefault) {
		Integer lResult;
		try {
			lResult = (Integer) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Integer getInteger(String aKey) {
		return getInteger(aKey, null);
	}

	@Override
	public void setInteger(String aKey, Integer aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Long getLong(String aKey, Long aDefault) {
		Long lResult;
		try {
			lResult = (Long) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Long getLong(String aKey) {
		return getLong(aKey, null);
	}

	@Override
	public void setLong(String aKey, Long aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Double getDouble(String aKey, Double aDefault) {
		Double lResult;
		try {
			Object lObj = mData.get(aKey);
			if (lObj instanceof String) {
				lResult = Double.parseDouble((String) lObj);
			} else if (lObj instanceof Integer) {
				lResult = (Integer) lObj / 1.0;
			} else {
				lResult = (Double) lObj;
			}
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (NumberFormatException lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Double getDouble(String aKey) {
		return getDouble(aKey, null);
	}

	@Override
	public void setDouble(String aKey, Double aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Boolean getBoolean(String aKey, Boolean aDefault) {
		Boolean lResult;
		try {
			lResult = (Boolean) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aArg
	 * @return
	 */
	@Override
	public Boolean getBoolean(String aArg) {
		return getBoolean(aArg, null);
	}

	/**
	 *
	 * @param aKey
	 */
	@Override
	public void setBoolean(String aKey, Boolean aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public List getList(String aKey) {
		return getList(aKey, null);
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public List getList(String aKey, List aDefault) {
		List lResult;
		try {
			lResult = (List) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @param aList
	 */
	@Override
	public void setList(String aKey, List aList) {
		try {
			mData.put(aKey, aList);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aTokenizable
	 */
	@Override
	public void setToken(String aKey, ITokenizable aTokenizable) {
		if (null == aTokenizable) {
			mData.put(aKey, null);
			return;
		}
		Token lToken = TokenFactory.createToken();
		aTokenizable.writeToToken(lToken);
		setToken(aKey, lToken);
	}

	/**
	 *
	 * @param aKey
	 * @param aToken
	 */
	@Override
	public void setToken(String aKey, Token aToken) {
		try {
			mData.put(aKey, aToken);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Token getToken(String aKey) {
		return getToken(aKey, null);
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Token getToken(String aKey, Token aDefault) {
		Token lResult;
		try {
			lResult = (Token) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Map getMap(String aKey, Map aDefault) {
		Map lResult;
		try {
			lResult = (Map) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception lEx) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Map getMap(String aKey) {
		return getMap(aKey, null);
	}

	/**
	 *
	 * @param aKey
	 */
	@Override
	public void setMap(String aKey, Map aMap) {
		try {
			mData.put(aKey, aMap);
		} catch (Exception lEx) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String toString() {
		return mData.toString();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Iterator<String> getKeyIterator() {
		return mData.keySet().iterator();
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Object getObject(String aKey) {
		Object lObj = null;
		try {
			lObj = mData.get(aKey);
		} catch (Exception lEx) {
			//
		}
		return lObj;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getLogString() {
		StringBuilder lSB = new StringBuilder();
		for (Object lEntry : mData.entrySet()) {
			Map.Entry<String, Object> lFME = (Map.Entry) lEntry;
			String lVal = getExclLogField(lFME.getKey());
			lSB.append(lFME.getKey()).append("=");
			if (null == lVal) {
				lSB.append(lFME.getValue());
			} else {
				lSB.append(lVal);
			}
			lSB.append(", ");
		}
		int lLen = lSB.length();
		if (lLen > 2) {
			lSB.delete(lLen - 2, lLen);
		}
		return lSB.toString();
	}
}
