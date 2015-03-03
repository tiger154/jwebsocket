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
package org.jwebsocket.plugins.stockticker.event;

import org.jwebsocket.plugins.stockticker.api.IUser;
import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 *
 * @author Roy
 */
public class CreateUser extends C2SEvent implements IUser {

	private String mUser;
	private String mPass;

	/**
	 *
	 * @param aUser
	 */
	@Override
	@ImportFromToken
	public void setUser(String aUser) {
		mUser = aUser;
	}

	/**
	 *
	 * @param aPass
	 */
	@Override
	@ImportFromToken
	public void setPass(String aPass) {
		mPass = aPass;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getUser() {
		return mUser;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getPass() {
		return mPass;
	}
}
