//	---------------------------------------------------------------------------
//	jWebSocket SampleRPCObject (Community Edition, CE)
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
package org.jwebsocket.plugins.rpc;

import org.jwebsocket.token.BaseTokenizable;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class SampleRPCObject extends BaseTokenizable implements ITokenizable {

	private String mFirstName = null;
	private String mLastName = null;
	private String mAddress = null;
	private String mZipcode = null;
	private String mCity = null;

	// we necessarily need to have a default constructor with no arguments
	// for tokenizable RPC objects
	/**
	 *
	 */
	public SampleRPCObject() {
		// don't miss this call to initialize classname
		super();
	}

	/**
	 *
	 * @param aFirstName
	 * @param aLastName
	 * @param aAddress
	 * @param aZipcode
	 * @param aCity
	 */
	public SampleRPCObject(String aFirstName, String aLastName,
			String aAddress, String aZipcode, String aCity) {

		// don't miss this call to initialize classname
		super();

		mFirstName = aFirstName;
		mLastName = aLastName;
		mAddress = aAddress;
		mZipcode = aZipcode;
		mCity = aCity;
	}

	@Override
	public void writeToToken(Token aToken) {
		if (aToken == null) {
			return;
		}
		aToken.setString("firstname", mFirstName);
		aToken.setString("lastname", mLastName);
		aToken.setString("address", mAddress);
		aToken.setString("zipcode", mZipcode);
		aToken.setString("city", mCity);

		// don't miss this call to write classname to Token
		super.writeToToken(aToken);
	}

	@Override
	public void readFromToken(Token aToken) {
		if (aToken == null) {
			return;
		}
		mFirstName = aToken.getString("firstname");
		mLastName = aToken.getString("lastname");
		mAddress = aToken.getString("address");
		mZipcode = aToken.getString("zipcode");
		mCity = aToken.getString("city");

		// don't miss this call to read classname to Token
		super.readFromToken(aToken);
	}
}
