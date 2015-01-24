//	---------------------------------------------------------------------------
//	jWebSocket OAuth implementation for Java, OAuthBase (Community Edition, CE)
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
//	this plug-in is based on: 
//	http://tools.ietf.org/html/rfc6749 - The OAuth 2.0 Authorization Framework
//	http://tools.ietf.org/html/rfc6750 - The OAuth 2.0 Authorization Framework: Bearer Token Usage
//	http://oauth.net/2/ - OAuth 2.0
//	---------------------------------------------------------------------------
package org.jwebsocket.sso;

import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Schulze
 */
public class OAuthBase implements IOAuth {

	int mReturnCode = 0;
	String mReturnMsg = "Ok";

	String mOAuthHost = null;
	String mOAuthGetSessionURL = "/get-smsession";
	String mRefreshToken = null;
	String mAccessToken = null;
	String mUsername = null;
	String mFullname = null;
	String mEmail = null;
	String mSessionId = null;
	long mDefaultTimeout = 5000;

	String SSO_SESSION_COOKIE_NAME = "SMSESSION";
	String OAUTH_AUTHSESSION_URL = "/auth/oauth/v2/token";
	String OAUTH_GETUSER_URL = "/use-token";
	String OAUTH_REFRESHTOKEN_URL = "/auth/oauth/v2/token";
	String mOAuthAppId = null; // global (static) application id (not instance app_id)!
	String mOAuthAppSecret = null; // global (static) application secret (not instance app_secret)!

	static {
		System.setProperty("jsse.enableSNIExtension", "false");
	}

	/**
	 * @return the OAuthHost
	 */
	public String getOAuthHost() {
		return mOAuthHost;
	}

	/**
	 *
	 * @param aOAuthHost
	 */
	public void setOAuthHost(String aOAuthHost) {
		mOAuthHost = aOAuthHost;
	}

	/**
	 * @return the mUsername
	 */
	public String getUsername() {
		return mUsername;
	}

	/**
	 * @return the mRefreshToken
	 */
	public String getRefreshToken() {
		return mRefreshToken;
	}

	/**
	 * @return the mAccessToken
	 */
	public String getAccessToken() {
		return mAccessToken;
	}

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @param aTimeout
	 * @return
	 * @throws SSOException
	 */
	@Override
	public String getSSOSession(String aUsername, String aPassword, long aTimeout) throws SSOException {
		throw new SSOException("Not implemented.");
	}

	/**
	 *
	 * @param aSessionId
	 * @param aTimeout
	 * @return
	 * @throws SSOException
	 */
	@Override
	public String authSession(String aSessionId, long aTimeout) throws SSOException {
		throw new SSOException("Not implemented.");
	}
}
