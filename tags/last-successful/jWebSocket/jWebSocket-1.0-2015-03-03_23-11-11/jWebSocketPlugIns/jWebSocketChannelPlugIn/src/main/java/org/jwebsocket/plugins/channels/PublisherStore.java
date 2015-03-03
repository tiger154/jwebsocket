//	---------------------------------------------------------------------------
//	jWebSocket - PublisherStore (Community Edition, CE)
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
package org.jwebsocket.plugins.channels;

/**
 * Base interface that defines the store operations for publishers
 *
 * @author Rolando Santamaria Maso, Puran Singh
 */
public interface PublisherStore {

	/**
	 * Returns the publisher information for the given publisher id
	 *
	 * @param aId the publisher id to fetch
	 * @return the publisher object
	 * @throws Exception
	 */
	Publisher getPublisher(String aId) throws Exception;

	/**
	 * Removes the publisher from the store based on given id
	 *
	 *
	 * @param aId
	 * @throws Exception
	 */
	void removePublisher(String aId) throws Exception;
}
