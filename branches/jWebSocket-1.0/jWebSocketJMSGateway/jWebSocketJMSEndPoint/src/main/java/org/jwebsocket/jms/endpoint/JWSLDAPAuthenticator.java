//	---------------------------------------------------------------------------
//	jWebSocket - JMS EndPoint LDAP Authenticator (Community Edition, CE)
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

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import org.apache.log4j.Logger;
import org.jwebsocket.ldap.ADTools;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class JWSLDAPAuthenticator implements IJWSAuthenticator {

	static final Logger mLog = Logger.getLogger(JWSLDAPAuthenticator.class);

	private ADTools mADTools = null;
	private String mBindUsername;
	private String mBindPassword;

	/**
	 *
	 */
	public JWSLDAPAuthenticator() {

	}

	/**
	 *
	 * @param aLDAPURL
	 * @param aBaseDNGroups
	 * @param aBaseDNUsers
	 * @param aBindUsername
	 * @param aBindPassword
	 */
	public JWSLDAPAuthenticator(String aLDAPURL, String aBaseDNGroups,
			String aBaseDNUsers, String aBindUsername, String aBindPassword) {
		init(aLDAPURL, aBaseDNGroups, aBaseDNUsers, aBindUsername, aBindPassword);
	}

	@Override
	public void initialize() throws JMSEndpointException {
	}

	@Override
	public void shutdown() throws JMSEndpointException {
		if (null != mADTools) {
			mADTools.logout();
		}
	}

	/**
	 *
	 * @param aLDAPURL
	 * @param aBaseDNGroups
	 * @param aBaseDNUsers
	 * @param aBindUsername
	 * @param aBindPassword
	 */
	public void init(String aLDAPURL, String aBaseDNGroups,
			String aBaseDNUsers, String aBindUsername, String aBindPassword) {
		mADTools = new ADTools(
				aLDAPURL, // URL to AD Server
				aBaseDNGroups, // DN to start search for groups
				aBaseDNUsers // DN to start search for users
		);
		// ldap binding is done in initialize method
		mBindUsername = aBindUsername;
		mBindPassword = aBindPassword;

		if (null != mADTools) {
			mLog.debug("Binding to LDAP...");
			if (null != mADTools.login(mBindUsername, mBindPassword)) {
				mLog.info(mBindUsername + " successfully bound to LDAP server.");
			} else {
				mLog.error(mBindUsername + " could not be bound to LDAP server.");
			}
		}
	}

	/**
	 *
	 * @return the username if authentication successful, otherwise null
	 */
	@Override
	public String authenticate(Token aToken) throws JMSEndpointException {
		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");
		if (null == lUsername && null == lPassword) {
			return null;
		}
		try {
			mADTools.getDirContext(lUsername, lPassword);
			return lUsername;
		} catch (NamingException lEx) {
			// "sometimes" spaces are returned as 0x00 characters,
			//  replace these to avoid subsequent JSON parse errors!
			throw new JMSEndpointException(lEx.getMessage().replace((char)0, (char)32));
		}
	}

}
