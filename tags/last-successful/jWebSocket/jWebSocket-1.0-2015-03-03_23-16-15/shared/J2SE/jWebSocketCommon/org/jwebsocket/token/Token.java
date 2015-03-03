//	---------------------------------------------------------------------------
//	jWebSocket Token (Community Edition, CE)
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jwebsocket.api.ITokenizable;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public interface Token {

	/**
	 *
	 * @return TRUE if the token has expired, FALSE otherwise
	 */
	Boolean hasExpired();

	/**
	 *
	 * @return TRUE if the token is a logical chunk, FALSE otherwise
	 */
	Boolean isChunk();

	/**
	 *
	 * @return TRUE if the token is the last chunk, FALSE otherwise
	 */
	Boolean isLastChunk();

	/**
	 * Chunk type is an optional attribute used to identify each chunk.
	 *
	 * @return
	 */
	String getChunkType();

	/**
	 * Set the chunk type attribute value.
	 *
	 * @param aChunkType
	 */
	void setChunkType(String aChunkType);

	/**
	 * Set/Unset the token as a logical chunk
	 *
	 * @param aIsChunk
	 */
	void setChunk(Boolean aIsChunk);

	/**
	 * Set/Unset the token as a last chunk
	 *
	 * @param aIsLastChunk
	 */
	void setLastChunk(Boolean aIsLastChunk);

	/**
	 * resets all fields of the token. After this operation the token is empty.
	 */
	void clear();

	/**
	 * copies all fields of the given ITokenizable to the token. Existing fields
	 * do not get deleted but overwritten in case of naming conflicts. Use the
	 * <tt>clear</tt> method to explicitely reset the token if desired.
	 *
	 * @param aTokenizable
	 */
	void set(ITokenizable aTokenizable);

	/**
	 *
	 *
	 * @return
	 */
	Map getMap();

	/**
	 * copies all fields from a Map into the Token. A check has to be made by
	 * the corresponding implementations that only such data types are passed
	 * that are supported by the Token abstraction.
	 *
	 *
	 * @param aMap
	 */
	void setMap(Map aMap);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Object getObject(String aKey);

	/**
	 * The token code is an special attribute used to indicate the server
	 * response status
	 *
	 * @return The token code
	 */
	Integer getCode();

	/**
	 * Set the token code
	 *
	 * @param aCode
	 */
	void setCode(Integer aCode);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	String getString(String aKey, String aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	String getString(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setString(String aKey, String aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Integer getInteger(String aKey, Integer aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Integer getInteger(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setInteger(String aKey, Integer aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Long getLong(String aKey, Long aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Long getLong(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setLong(String aKey, Long aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Double getDouble(String aKey, Double aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Double getDouble(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setDouble(String aKey, Double aValue);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setDouble(String aKey, Float aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Boolean getBoolean(String aKey, Boolean aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Boolean getBoolean(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setBoolean(String aKey, Boolean aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	List getList(String aKey, List aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	List getList(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aList
	 */
	void setList(String aKey, List aList);

	// TODO: Add list access methods
	/**
	 *
	 * @param aKey
	 * @param aTokenizable
	 */
	void setToken(String aKey, ITokenizable aTokenizable);

	/**
	 *
	 * @param aKey
	 * @param aToken
	 */
	void setToken(String aKey, Token aToken);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Token getToken(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Token getToken(String aKey, Token aDefault);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Map getMap(String aKey, Map aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Map getMap(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aMap
	 */
	void setMap(String aKey, Map aMap);

	// TODO: Add map access methods
	// TODO: Add date/time fields
	/**
	 *
	 * @param aKey
	 */
	void remove(String aKey);

	/**
	 *
	 * @return
	 */
	String getType();

	/**
	 *
	 * @param aType
	 */
	void setType(String aType);

	/**
	 *
	 * @return
	 */
	String getNS();

	/**
	 *
	 * @param aNS
	 */
	void setNS(String aNS);

	/**
	 * validates the passed objects and uses the appropriate assignment method
	 *
	 * @param aKey
	 * @param aObj
	 * @return true if value could be set otherwise false
	 */
	boolean setValidated(String aKey, Object aObj);

	/**
	 *
	 * @return
	 */
	Iterator<String> getKeyIterator();

	/**
	 *
	 * @return
	 */
	String getLogString();
}
