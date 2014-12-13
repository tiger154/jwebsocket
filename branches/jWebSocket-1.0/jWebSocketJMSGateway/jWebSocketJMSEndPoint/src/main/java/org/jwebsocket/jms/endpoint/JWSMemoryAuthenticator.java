//	---------------------------------------------------------------------------
//	jWebSocket - JMS EndPoint Memory Authenticator (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.token.Token;

/**
 * Memory authenticator for testing purposes (not recommended for production
 * systems). Stores the users credentials statically in RAM.
 *
 * @author Rolando Santamaria Maso
 */
public class JWSMemoryAuthenticator implements IJWSAuthenticator {

	private Map<String, String> mCredentials = new FastMap<String, String>();

	public JWSMemoryAuthenticator(Map<String, String> aCredentials) {
		mCredentials.putAll(mCredentials);
	}

	public void addCredentials(String aUsername, String aPassword) {
		mCredentials.put(aUsername, aPassword);
	}

	public void removeCredentials(String aUsername) {
		mCredentials.remove(aUsername);
	}

	@Override
	public void initialize() throws JMSEndpointException {

	}

	@Override
	public String authenticate(Token aToken) throws JMSEndpointException {
		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");

		if (null != lUsername && null != lPassword
				&& mCredentials.containsKey(lUsername) && mCredentials.get(lUsername).equals(lPassword)) {
			return lUsername;
		}

		return null;
	}

	@Override
	public String authToken(Token aToken) throws JMSEndpointException {
		String lUsername = aToken.getString("username");
		if (null == lUsername) {
			throw new JMSEndpointException("No user name for Memory authentication!");
		}
		String lPassword = aToken.getString("password");
		if (null == lPassword) {
			throw new JMSEndpointException("No password passed for Memory authentication!");
		}

		lUsername = authenticate(aToken);
		if (null != lUsername) {
			throw new JMSEndpointException("Memory authentication process failed. Invalid credentials!");
		}
		return lUsername;
	}

	@Override
	public boolean acceptsToken(Token aToken) {
		return (null != aToken.getString("username")
				&& null != aToken.getString("password"));
	}

	@Override
	public void shutdown() throws JMSEndpointException {
		mCredentials.clear();
	}
}
