/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.stockticker.api;

import java.util.List;

/**
 *
 * @author Roy
 */
public interface IUser {

	/**
	 *
	 * @param aUser
	 */
	void setUser(String aUser);

	/**
	 *
	 * @param aPass
	 */
	void setPass(String aPass);

	/**
	 *
	 * @return
	 */
	String getUser();

	/**
	 *
	 * @return
	 */
	String getPass();

}
