//	---------------------------------------------------------------------------
//	jWebSocket - IClientCollection (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IClientCollection {

	/**
	 *
	 * @param aUID
	 * @return
	 * @throws java.lang.Exception
	 */
	boolean contains(String aUID) throws Exception;

	/**
	 *
	 * @param aUID
	 * @throws java.lang.Exception
	 */
	void add(String aUID) throws Exception;

	/**
	 *
	 * @param aUID
	 * @throws java.lang.Exception
	 */
	void remove(String aUID) throws Exception;

	/**
	 *
	 * @return @throws java.lang.Exception
	 */
	List<String> getAll() throws Exception;

	/**
	 *
	 * @throws java.lang.Exception
	 */
	void clear() throws Exception;

	/**
	 *
	 * @return @throws java.lang.Exception
	 */
	int size() throws Exception;
}
