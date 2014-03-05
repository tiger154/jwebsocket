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
public interface IService {

	/**
	 *
	 * @param aUser
	 * @return
	 */
	Boolean createUser(IUser aUser);

	/**
	 *
	 * @param aUser
	 * @return
	 */
	Boolean login(IUser aUser);

	/**
	 *
	 * @return
	 */
	List<IRecord> listRecords();

	/**
	 *
	 * @param aName
	 * @param aCant
	 * @param aUserLogin
	 * @return
	 */
	Boolean sell(String aName, String aCant, String aUserLogin);

	/**
	 *
	 * @param aName
	 * @param aCant
	 * @param aUserLogin
	 * @return
	 */
	Boolean buy(String aName, String aCant, String aUserLogin);

	/**
	 *
	 * @param aUser
	 * @return
	 */
	List<IPurchasing> readBuy(String aUser);

	/**
	 *
	 * @param aUser
	 * @return
	 */
	List<String> showComb(String aUser);

	/**
	 *
	 * @param aUserLogin
	 * @param aName
	 * @return
	 */
	Integer chart(String aUserLogin, String aName);
}
