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
public interface IRecord {

	/**
	 *
	 * @return
	 */
	Integer getId();

	/**
	 *
	 * @param mId
	 */
	void setId(Integer mId);

	/**
	 *
	 * @return
	 */
	String getName();

	/**
	 *
	 * @param aName
	 */
	void setName(String aName);

	/**
	 *
	 * @return
	 */
	Double getBid();

	/**
	 *
	 * @param aBid
	 */
	void setBid(Double aBid);

	/**
	 *
	 * @return
	 */
	Double getPrice();

	/**
	 *
	 * @param aPrice
	 */
	void setPrice(Double aPrice);

	/**
	 *
	 * @return
	 */
	Double getAsk();

	/**
	 *
	 * @param aAsk
	 */
	void setAsk(Double aAsk);

	/**
	 *
	 * @return
	 */
	Double getChng();

	/**
	 *
	 * @param aChng
	 */
	void setChng(Double aChng);

	/**
	 *
	 * @return
	 */
	Integer getTrend();

	/**
	 *
	 * @param aTrend
	 */
	void setTrend(Integer aTrend);

	/**
	 *
	 * @return
	 */
	List<Double> getHistoy();

	/**
	 *
	 * @param mHistoy
	 */
	void setHistoy(List<Double> mHistoy);
}
