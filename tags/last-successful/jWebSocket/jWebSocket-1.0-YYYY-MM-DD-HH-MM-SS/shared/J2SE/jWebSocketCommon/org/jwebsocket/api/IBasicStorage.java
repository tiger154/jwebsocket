//	---------------------------------------------------------------------------
//	jWebSocket - IBasicStorage (Community Edition, CE)
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
package org.jwebsocket.api;

import java.util.Collection;
import java.util.Map;

/**
 * A storage is a named key/value list. This is the basic interface for all 
 * higher level implementations and persistence engines, like EhCache or MongoDB
 * 
 * @param <K> 
 * @param <V> 
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public interface IBasicStorage<K, V> extends Map<K, V>, IInitializable {

	/**
	 * 
	 * @return the IBasicStorage name
	 */
	String getName();

	/**
	 * 
	 * @param aName the IBasicStorage name to set
	 * @throws Exception
	 */
	void setName(String aName) throws Exception;

	/**
	 * 
	 * @param aKeys
	 * @return 
	 */
	Map<K, V> getAll(Collection<K> aKeys);
}
