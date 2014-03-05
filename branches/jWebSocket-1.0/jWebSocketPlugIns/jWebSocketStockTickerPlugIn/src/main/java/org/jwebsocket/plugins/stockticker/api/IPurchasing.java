/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.stockticker.api;

/**
 *
 * @author Roy
 */
public interface IPurchasing {

	/**
	 *
	 * @return
	 */
	String getUser();

	/**
	 *
	 * @param aUser
	 */
	void setUser(String aUser);

	/**
	 *
	 * @return
	 */
	String getName();

	/**
	 *
	 * @param mName
	 */
	void setName(String mName);

	/**
	 *
	 * @return
	 */
	Integer getCant();

	/**
	 *
	 * @param mCant
	 */
	void setCant(Integer mCant);

	/**
	 *
	 * @return
	 */
	Double getInversion();

	/**
	 *
	 * @param mInversion
	 */
	void setInversion(Double mInversion);

	/**
	 *
	 * @return
	 */
	Double getValue();

	/**
	 *
	 * @param mValue
	 */
	void setValue(Double mValue);
}
