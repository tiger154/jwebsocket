//	---------------------------------------------------------------------------
//	jWebSocket - IItemCollection (Community Edition, CE)
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

import java.util.Set;
import org.jwebsocket.api.IMappable;
import org.jwebsocket.api.ITokenizable;

/**
 * The class wraps the item storage mechanism to published via WebSocket API
 *
 * @author Rolando Santamaria Maso
 */
public interface IItemCollection extends ITokenizable, IMappable {

	/**
	 * @return TRUE if the collection is capped, FALSE otherwise
	 */
	boolean isCapped();

	/**
	 * @param aIsCapped
	 */
	void setCapped(boolean aIsCapped);

	/**
	 *
	 * @return The collection creation time
	 */
	Long createdAt();

	/**
	 * A private collection indicates that the collection name will not be
	 * published to the clients in "listing" operations
	 *
	 * @return TRUE if the collection is private, FALSE otherwise
	 */
	Boolean isPrivate();

	/**
	 * Set/Unset the collection as private
	 *
	 * @param aIsPrivate
	 */
	void setPrivate(Boolean aIsPrivate);

	/**
	 *
	 * @return The item storage used to store the items
	 */
	IItemStorage getItemStorage();

	/**
	 * The name of the collection is equivalent to the name of the item storage
	 *
	 * @return The name of the collection.
	 */
	String getName();

	/**
	 * The access password of a collection allows the users to read the
	 * collection content but not write.
	 *
	 * @return The collection access password
	 */
	String getAccessPassword();

	/**
	 * Set the collection access password
	 *
	 * @param aPassword
	 */
	void setAccessPassword(String aPassword);

	/**
	 * The secret password of a collection allows the users to modify the
	 * content of the collection.
	 *
	 * @return The collection secret password
	 */
	String getSecretPassword();

	/**
	 * Set the collection secret password
	 *
	 * @param aPassword
	 */
	void setSecretPassword(String aPassword);

	/**
	 * A collection marked as "system" cannot be removed
	 *
	 * @return TRUE if the collection is system, FALSE otherwise
	 */
	Boolean isSystem();

	/**
	 * Set/Unset the collection as system
	 *
	 * @param aIsSystem
	 */
	void setSystem(boolean aIsSystem);

	/**
	 * The subscribers are the clients interested in receive the collection
	 * activities
	 *
	 * @return The collection subscribers
	 */
	IClientCollection getSubcribers();

	/**
	 * The publishers are the clients authorized to modify the content of the
	 * collection
	 *
	 * @return The collection publishers
	 */
	IClientCollection getPublishers();

	/**
	 * The collection owner is the user that creates the item collection
	 *
	 * @return The collection owner
	 */
	String getOwner();

	/**
	 * Set the collection owner
	 *
	 * @param aUsername
	 */
	void setOwner(String aUsername);

	/**
	 * Restart a collection. When a collection is restarted all publishers and
	 * subscribers are removed from the collection.
	 *
	 * @return
	 * @throws java.lang.Exception
	 */
	Set<String> restart() throws Exception;

	/**
	 * Validates the collection argument's value before save
	 *
	 * @throws Exception
	 */
	void validate() throws Exception;

	/**
	 * Returns the item collection capacity
	 *
	 * @return
	 */
	int getCapacity();

	/**
	 * Set the item collection capacity. Default value is cero "0", it means
	 * unlimited capacity. Once the collection capacity is set, the value cannot
	 * be changed again.
	 *
	 * @param aCapacity
	 * @throws java.lang.Exception
	 */
	void setCapacity(int aCapacity) throws Exception;
}
