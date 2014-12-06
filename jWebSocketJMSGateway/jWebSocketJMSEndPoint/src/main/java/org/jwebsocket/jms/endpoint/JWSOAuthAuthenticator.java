//	---------------------------------------------------------------------------
//	jWebSocket - JMS EndPoint OAuth Authenticator (Community Edition, CE)
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

import org.apache.log4j.Logger;
import org.jwebsocket.sso.OAuth;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class JWSOAuthAuthenticator implements IJWSAuthenticator {

	static final Logger mLog = Logger.getLogger(JWSOAuthAuthenticator.class);

	private OAuth mOAuth = null;
	private String mAccessToken = null;
	private String mRefreshToken = null;
	private String mOAuthSessionId = null;
	private long mDefautTimeout = 10000;

	/**
	 *
	 */
	public JWSOAuthAuthenticator() {
	}

	/**
	 *
	 * @param aOAuthHost
	 * @param aOAuthAppId
	 * @param aOAuthAppSecret
	 * @param aUsername
	 * @param aPassword
	 * @param aDefaultTimeout
	 */
	public JWSOAuthAuthenticator(String aOAuthHost, String aOAuthAppId,
			String aOAuthAppSecret, String aUsername, String aPassword,
			long aDefaultTimeout) {
		init(aOAuthHost, aOAuthAppId, aOAuthAppSecret, aUsername, aPassword,
				aDefaultTimeout);
	}

	@Override
	public void initialize() throws JMSEndpointException {
	}

	@Override
	public void shutdown() throws JMSEndpointException {
	}

	/**
	 *
	 * @param aOAuthHost
	 * @param aOAuthAppId
	 * @param aOAuthAppSecret
	 * @param aDefaultTimeout
	 */
	public void init(String aOAuthHost, String aOAuthAppId,
			String aOAuthAppSecret,
			long aDefaultTimeout) {
		mOAuth = new OAuth();
		mOAuth.setOAuthHost(aOAuthHost);
		mOAuth.setOAuthAppId(aOAuthAppId);
		mOAuth.setOAuthAppSecret(aOAuthAppSecret);
		mOAuth.setDefaultTimeout(aDefaultTimeout);
	}

	/**
	 *
	 * @param aOAuthHost
	 * @param aOAuthAppId
	 * @param aOAuthAppSecret
	 * @param aUsername
	 * @param aPassword
	 * @param aDefaultTimeout
	 * @deprecated
	 */
	@Deprecated
	public void init(String aOAuthHost, String aOAuthAppId,
			String aOAuthAppSecret, String aUsername, String aPassword,
			long aDefaultTimeout) {
		init(aOAuthHost, aOAuthAppId, aOAuthAppSecret, aDefaultTimeout);
	}

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @throws org.jwebsocket.jms.endpoint.JMSEndpointException
	 */
	public void authDirect(String aUsername, String aPassword) throws JMSEndpointException {
		mOAuthSessionId = mOAuth.getSSOSession(aUsername, aPassword, mDefautTimeout);
		mLog.debug("Getting Session Cookie: " + mOAuthSessionId);
		String lJSON = mOAuth.authSession(mOAuth.getSessionId(), mDefautTimeout);
		if (null != lJSON) {
			lJSON = lJSON.replace("\r\n", "\\r\\n");
		}
		mLog.debug("Authenticated Session: " + lJSON);
		mAccessToken = mOAuth.getAccessToken();
		mRefreshToken = mOAuth.getRefreshToken();
		mLog.debug("Access Token and Refresh Token: "
				+ mAccessToken + ", " + mRefreshToken);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String authenticate(Token aToken) throws JMSEndpointException {
		String lAccessToken = aToken.getString("accessToken");
		if (null == lAccessToken) {
			return null;
		}
		mOAuth.getUser(lAccessToken, mDefautTimeout);
		String lUsername = mOAuth.getUsername();
		return lUsername;
	}

	/**
	 *
	 * @return the username if authentication successful, otherwise null
	 */
	@Override
	public String authToken(Token aToken) throws JMSEndpointException {
		String lAccessToken = aToken.getString("accessToken");
		if (null == lAccessToken || lAccessToken.length() <= 0) {
			throw new JMSEndpointException(
					"No or empty access token passed for OAuth authentication.");
		}
		mOAuth.getUser(lAccessToken, mDefautTimeout);
		int lCode = mOAuth.getReturnCode();
		if (0 != lCode) {
			throw new JMSEndpointException("OAuth returned error code "
					+ lCode + ": " + mOAuth.getReturnMsg());
		}
		String lUsername = mOAuth.getUsername();
		if (null == lUsername) {
			throw new JMSEndpointException(
					"OAuth did not deliver a username for access token '"
					+ lAccessToken + "'.");
		}
		return lUsername;
	}

	/**
	 *
	 * @return the current access token of the OAuth object
	 */
	public String getAccessToken() {
		return mOAuth.getAccessToken();
	}

	@Override
	public boolean acceptsToken(Token aToken) {
		return (null != aToken.getString("accessToken"));
	}
}
