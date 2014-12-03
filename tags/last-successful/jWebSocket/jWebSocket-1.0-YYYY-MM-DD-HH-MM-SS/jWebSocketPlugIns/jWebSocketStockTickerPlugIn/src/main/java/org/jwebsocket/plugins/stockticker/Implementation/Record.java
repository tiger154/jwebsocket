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
package org.jwebsocket.plugins.stockticker.Implementation;

import org.jwebsocket.plugins.stockticker.api.IRecord;
import java.util.List;

/**
 *
 * @author Roy
 */
public class Record implements IRecord {

	private Integer mId;
	private String mName;
	private Double mBid;
	private Double mPrice;
	private Double mAsk;
	private Double mChng;
	private Integer mTrend;
	private List<Double> mHistoy;

	/**
	 *
	 * @param aId
	 * @param aName
	 * @param aBid
	 * @param aPrice
	 * @param aAsk
	 * @param aChng
	 * @param aTrend
	 * @param aHistory
	 */
	public Record(Integer aId, String aName, Double aBid, Double aPrice, Double aAsk, Double aChng, Integer aTrend, List<Double> aHistory) {
		this.mId = aId;
		this.mName = aName;
		this.mBid = aBid;
		this.mPrice = aPrice;
		this.mAsk = aAsk;
		this.mChng = aChng;
		this.mTrend = aTrend;
		this.mHistoy = aHistory;
	}

	/**
	 * @return the mId
	 */
	@Override
	public Integer getId() {
		return mId;
	}

	/**
	 * @param mId the mId to set
	 */
	@Override
	public void setId(Integer mId) {
		this.mId = mId;
	}

	/**
	 * @return the mName
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * @param mName the mName to set
	 */
	@Override
	public void setName(String mName) {
		this.mName = mName;
	}

	/**
	 * @return the mBid
	 */
	@Override
	public Double getBid() {
		return mBid;
	}

	/**
	 * @param mBid the mBid to set
	 */
	@Override
	public void setBid(Double mBid) {
		this.mBid = mBid;
	}

	/**
	 * @return the mPrice
	 */
	@Override
	public Double getPrice() {
		return mPrice;
	}

	/**
	 * @param mPrice the mPrice to set
	 */
	@Override
	public void setPrice(Double mPrice) {
		this.mPrice = mPrice;
	}

	/**
	 * @return the mAsk
	 */
	@Override
	public Double getAsk() {
		return mAsk;
	}

	/**
	 * @param mAsk the mAsk to set
	 */
	@Override
	public void setAsk(Double mAsk) {
		this.mAsk = mAsk;
	}

	/**
	 * @return the mChng
	 */
	@Override
	public Double getChng() {
		return mChng;
	}

	/**
	 * @param mChng the mChng to set
	 */
	@Override
	public void setChng(Double mChng) {
		this.mChng = mChng;
	}

	/**
	 * @return the mTrend
	 */
	@Override
	public Integer getTrend() {
		return mTrend;
	}

	/**
	 * @param mTrend the mTrend to set
	 */
	@Override
	public void setTrend(Integer mTrend) {
		this.mTrend = mTrend;
	}

	/**
	 * @return the mHistoy
	 */
	@Override
	public List<Double> getHistoy() {
		return mHistoy;
	}

	/**
	 * @param mHistoy the mHistoy to set
	 */
	@Override
	public void setHistoy(List<Double> mHistoy) {
		this.mHistoy = mHistoy;
	}
}
