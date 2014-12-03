//	---------------------------------------------------------------------------
//	jWebSocket - JMS EndPoint AutoSelect Authenticator (Community Edition, CE)
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

import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class JWSAutoSelectAuthenticator implements IJWSAuthenticator {

	static final Logger mLog = Logger.getLogger(JWSAutoSelectAuthenticator.class);

	/**
	 *
	 */
	private final List<IJWSAuthenticator> mAuthenticators
			= new FastList<IJWSAuthenticator>();

	@Override
	public void initialize() throws JMSEndpointException {
		for (IJWSAuthenticator lAuthenticator : mAuthenticators) {
			lAuthenticator.initialize();
		}
	}

	@Override
	public void shutdown() throws JMSEndpointException {
		for (IJWSAuthenticator lAuthenticator : mAuthenticators) {
			lAuthenticator.shutdown();
		}
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String authenticate(Token aToken) throws JMSEndpointException {

		// iterate through list of authenticators and return first user
		// name with could successfully be authenticated.
		for (IJWSAuthenticator lAuthenticator : mAuthenticators) {
			String lAuthenticatedUsername = lAuthenticator.authenticate(aToken);
			if (null != lAuthenticatedUsername) {
				return lAuthenticatedUsername;
			}
		}
		return null;
	}

	@Override
	public String authToken(Token aToken) throws JMSEndpointException {

		// iterate through list of authenticators and return first user
		// name with could successfully be authenticated.
		for (IJWSAuthenticator lAuthenticator : mAuthenticators) {
			String lAuthenticatedUsername = null;
			if (lAuthenticator.acceptsToken(aToken)) {
				lAuthenticatedUsername = lAuthenticator.authToken(aToken);
			}
			if (null != lAuthenticatedUsername) {
				return lAuthenticatedUsername;
			}
		}
		throw new JMSEndpointException("No suitable authenticator installed for request.");
	}

	/**
	 *
	 * @param aAuthenticator
	 */
	public void addAuthenticator(IJWSAuthenticator aAuthenticator) {
		mAuthenticators.add(aAuthenticator);
	}

	/**
	 *
	 * @param aAuthenticator
	 */
	public void removeAuthenticator(IJWSAuthenticator aAuthenticator) {
		mAuthenticators.remove(aAuthenticator);
	}

	@Override
	public boolean acceptsToken(Token aToken) {
		return true;
	}
}
