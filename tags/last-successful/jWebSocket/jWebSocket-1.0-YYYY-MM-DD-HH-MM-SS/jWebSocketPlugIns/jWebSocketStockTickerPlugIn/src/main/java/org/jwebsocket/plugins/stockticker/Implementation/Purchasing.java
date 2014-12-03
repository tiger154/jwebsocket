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

import org.jwebsocket.plugins.stockticker.api.IPurchasing;

/**
 *
 * @author Roy
 */
public class Purchasing implements IPurchasing {

	private String mUser;
	private String mName;
	private Integer mCant;
	private Double mInversion;
	private Double mValue;

	/**
	 *
	 * @param aUser
	 * @param aName
	 * @param aCant
	 * @param aInversion
	 * @param aValue
	 */
	public Purchasing(String aUser, String aName, Integer aCant, Double aInversion, Double aValue) {
		this.mName = aName;
		this.mCant = aCant;
		this.mInversion = aInversion;
		this.mValue = aValue;
		this.mUser = aUser;
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
	 * @return the mCant
	 */
	@Override
	public Integer getCant() {
		return mCant;
	}

	/**
	 * @param mCant the mCant to set
	 */
	@Override
	public void setCant(Integer mCant) {
		this.mCant = mCant;
	}

	/**
	 * @return the mInversion
	 */
	@Override
	public Double getInversion() {
		return mInversion;
	}

	/**
	 * @param mInversion the mInversion to set
	 */
	@Override
	public void setInversion(Double mInversion) {
		this.mInversion = mInversion;
	}

	/**
	 * @return the mValue
	 */
	@Override
	public Double getValue() {
		return mValue;
	}

	/**
	 * @param mValue the mValue to set
	 */
	@Override
	public void setValue(Double mValue) {
		this.mValue = mValue;
	}

	/**
	 * @return the mUser
	 */
	@Override
	public String getUser() {
		return mUser;
	}

	/**
	 * @param mUser the mUser to set
	 */
	@Override
	public void setUser(String mUser) {
		this.mUser = mUser;
	}
}
