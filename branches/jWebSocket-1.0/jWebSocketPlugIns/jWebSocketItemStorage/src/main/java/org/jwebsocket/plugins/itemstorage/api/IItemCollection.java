package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;
import java.util.Set;
import org.jwebsocket.api.IMappable;
import org.jwebsocket.api.ITokenizable;

/**
 * The class wraps the item storage mechanism to published via WebSocket API
 *
 * @author kyberneees
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
	 * collection it content but not write.
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
	 * content of it.
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
	List<String> getSubcribers();

	/**
	 * The publishers are the clients authorized to modify the content of the
	 * collection
	 *
	 * @return The collection publishers
	 */
	List<String> getPublishers();

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
	 */
	Set<String> restart();

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
	 */
	void setCapacity(int aCapacity);
}
