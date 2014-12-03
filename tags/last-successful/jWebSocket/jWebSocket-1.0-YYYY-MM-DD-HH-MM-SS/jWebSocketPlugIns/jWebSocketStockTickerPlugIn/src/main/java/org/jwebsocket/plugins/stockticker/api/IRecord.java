//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket StockTicker Plug-In (Community Edition, CE)
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
