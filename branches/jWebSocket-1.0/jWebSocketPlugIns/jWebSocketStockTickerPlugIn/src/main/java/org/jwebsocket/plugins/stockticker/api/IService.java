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
