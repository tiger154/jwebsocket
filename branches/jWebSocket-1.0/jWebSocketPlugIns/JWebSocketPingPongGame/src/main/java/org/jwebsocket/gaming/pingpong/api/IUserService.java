/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.api;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 *
 * @author armando
 */
public interface IUserService {

	/**
	 * Create a new user account
	 * 
	 * @param aUser 
	 */
	boolean create(DBObject aUser);

	/**
	 * 
	 * @param un
	 * @return TRUE if the username is used by other user, FALSE otherwise
	 */
	boolean containsUn(String aUserName);

	/**
	 * @param uuid
	 * @param aPwd
	 * 
	 * @return TRUE if the password match the active user password
	 */
	boolean isPwdCorrect(String aUserName, String aPwd);

	/**
	 * 
	 * @param username
	 * @return The user profile
	 */
	DBCursor getProfileList();

	/**
	 * 
	 * @param username 
	 * @param data The new/modified entries to update in the user profile
	 */
	void updateValue(String aUserName, int aWins, int aLost);

	/**
	 * 
	 * @param username
	 * @return The user profile
	 */
	DBObject getProfile(String aUserName);

	/**
	 * 
	 * @param day
	 */
	void removeUser(int aDay);
}
