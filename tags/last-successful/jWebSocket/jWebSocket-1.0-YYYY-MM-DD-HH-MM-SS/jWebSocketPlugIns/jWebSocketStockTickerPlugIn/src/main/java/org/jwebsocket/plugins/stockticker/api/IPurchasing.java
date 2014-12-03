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
