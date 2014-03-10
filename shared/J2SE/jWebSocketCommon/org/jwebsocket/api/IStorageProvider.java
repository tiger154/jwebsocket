//	---------------------------------------------------------------------------
//	jWebSocket - IStorageProvider (Community Edition, CE)
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

/**
 * Provides the getStorage method to obtain a persistence storage with a given
 * name.
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public interface IStorageProvider {

	/**
	 * Get a storage instance for a giving a name
	 *
	 * @param aName
	 * @return The storage instance
	 * @throws Exception
	 */
	IBasicStorage<String, Object> getStorage(String aName) throws Exception;

	/**
	 * Remove a storage from a given name
	 *
	 * @param aName
	 * @throws Exception
	 */
	void removeStorage(String aName) throws Exception;
}
