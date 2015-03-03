//	---------------------------------------------------------------------------
//	jWebSocket - http authentication against Proxy (Community Edition, CE)
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
package org.jwebsocket.plugins.sms;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Allows to define the parameters needed to set the http proxy authentication.
 *
 * @author mayra
 */
public class HttpAuthProxy extends Authenticator {

	private final String mUser, mPassword;

	/**
	 * Constructor with the http proxy authentication parameters.
	 *
	 * @param aUser the username to authenticate
	 * @param aPassword the password to authenticate
	 */
	public HttpAuthProxy(String aUser, String aPassword) {
		mUser = aUser;
		mPassword = aPassword;
	}

	/**
	 * Returns an object with the http proxy authentication parameters needed.
	 *
	 * @return an object with the parameters to authenticate
	 */
	protected PasswordAuthentication setAuthentication() {
		return new PasswordAuthentication(mUser, mPassword.toCharArray());
	}
}
