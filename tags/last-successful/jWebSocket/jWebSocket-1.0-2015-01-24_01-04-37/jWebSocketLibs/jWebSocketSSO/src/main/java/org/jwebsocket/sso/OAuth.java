//	---------------------------------------------------------------------------
//	jWebSocket OAuth implementation for Java (Community Edition, CE)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Schulze
 */
public class OAuth extends OAuthBase {

	static final Logger mLog = Logger.getLogger(OAuth.class);

	/**
	 *
	 */
	public OAuth() {
	}

	/**
	 *
	 * @param aHost
	 */
	public OAuth(String aHost) {
		setOAuthHost(aHost);
	}

	/**
	 *
	 * @param aHost
	 * @param aAppId
	 * @param aAppSecret
	 */
	public OAuth(String aHost, String aAppId, String aAppSecret) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating OAuth instance for host: '"
					+ aHost + "', app-id: " + aAppId + ", app-secret: '******'");
		}
		mOAuthHost = aHost;
		mOAuthAppId = aAppId;
		mOAuthAppSecret = aAppSecret;
		if (mLog.isInfoEnabled()) {
			mLog.info("Initiated OAuth instance for host: '"
					+ aHost + "', app-id: " + aAppId + ", app-secret: '******'");
		}
	}

	/**
	 *
	 * @param aHost
	 * @param aAppId
	 * @param aAppSecret
	 * @param aDefaultTimeout
	 */
	public OAuth(String aHost, String aAppId, String aAppSecret, long aDefaultTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating OAuth instance for host: '"
					+ aHost + "', app-id: " + aAppId + ", app-secret: '******'"
					+ ", default-timeout: " + aDefaultTimeout);
		}
		mOAuthHost = aHost;
		mOAuthAppId = aAppId;
		mOAuthAppSecret = aAppSecret;
		mDefaultTimeout = aDefaultTimeout;
		if (mLog.isInfoEnabled()) {
			mLog.info("Initiated OAuth instance for host: '"
					+ aHost + "', app-id: " + aAppId + ", app-secret: '******'"
					+ ", default-timeout: " + aDefaultTimeout);
		}
	}

	/**
	 *
	 * @param aJSON
	 * @return
	 * @throws IOException
	 */
	public Map<String, Object> parseJSON(String aJSON) throws IOException {
		ObjectMapper lMapper = new ObjectMapper();
		Map<String, Object> lJSON;
		lJSON = lMapper.readValue(aJSON, Map.class);
		return lJSON;
	}

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @param aTimeout
	 * @return
	 */
	@Override
	public String getSSOSession(String aUsername, String aPassword, long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Requesting SSO session for user: '" + aUsername
					+ "', timeout: " + aTimeout + "ms...");
		}
		String lPostBody;
		String lJSONString;
		try {
			lPostBody = null;
			String lCredentials = Base64.encodeBase64String((aUsername + ":" + aPassword).getBytes("UTF-8"));
			Map lHeaders = new HashMap<String, String>();
			lHeaders.put("Authorization", "Basic " + lCredentials);
			lHeaders.put("Cache-Control", "no-cache");
			lHeaders.put("Content-Type", "application/x-www-form-urlencoded");
			lJSONString = HTTPSupport.request(mOAuthHost + mOAuthGetSessionURL, "GET",
					lHeaders, lPostBody, aTimeout);

			Map<String, Object> lJSON = parseJSON(lJSONString);
			mSessionId = (String) lJSON.get("smsession");
			if (mLog.isDebugEnabled()) {
				mLog.debug("SSO session obtained, response: '" + lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'");
			}
			return lJSONString;
		} catch (IOException lEx) {
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " authenticating directly against OAuth host.";
			lJSONString = "{\"code\":-1, \"msg\":\""
					+ lEx.getClass().getSimpleName() + ": " + lEx.getMessage() + "\"}";
			mLog.error("SSO session could not be obtained, response: '" + lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'");
			return lJSONString;
		}
	}

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @param aTimeout
	 * @return
	 */
	public String authDirect(String aUsername, String aPassword, long aTimeout) {
		String lPostBody;
		try {
			lPostBody
					= "client_id=ro_client"
					+ "&grant_type=password"
					+ "&username=" + URLEncoder.encode(aUsername, "UTF-8")
					+ "&password=" + URLEncoder.encode(aPassword, "UTF-8");
			Map lHeaders = new HashMap<String, String>();
			lHeaders.put("Content-Type", "application/x-www-form-urlencoded");
			String lJSONString = HTTPSupport.request(mOAuthHost, "POST",
					lHeaders, lPostBody, aTimeout);
			Map<String, Object> lJSON = parseJSON(lJSONString);
			mAccessToken = (String) lJSON.get("access_token");
			mRefreshToken = (String) lJSON.get("refresh_token");
			return lJSONString;
		} catch (IOException lEx) {
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " authenticating directly against OAuth host.";
			return "{\"code\":-1, \"msg\":\""
					+ lEx.getClass().getSimpleName() + "\"}";
		}
	}

	/**
	 *
	 * @param aSessionId
	 * @param aTimeout
	 * @return
	 */
	@Override
	public String authSession(String aSessionId, long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Authenticating session '" + aSessionId + "'"
					+ "', timeout: " + aTimeout + "ms...");
		}
		String lPostBody;
		String lJSONString;
		try {
			URLCodec lCodec = new URLCodec();
			lPostBody
					= "grant_type=password&"
					+ SSO_SESSION_COOKIE_NAME + "=" + lCodec.encode(aSessionId, "UTF-8");
			Map lHeaders = new HashMap<String, String>();
			lHeaders.put("Content-Type", "application/x-www-form-urlencoded");
			String lAuthStr = Base64.encodeBase64String(
					(getOAuthAppId() + ":" + getOAuthAppSecret()).getBytes("UTF-8"));
			lHeaders.put("Authorization", "Basic " + lAuthStr);
			lJSONString = HTTPSupport.request(mOAuthHost + OAUTH_AUTHSESSION_URL, "POST",
					lHeaders, lPostBody, aTimeout);
			Map<String, Object> lJSON = parseJSON(lJSONString);
			mAccessToken = (String) lJSON.get("access_token");
			mRefreshToken = (String) lJSON.get("refresh_token");
			if (mLog.isDebugEnabled()) {
				mLog.debug("Session authenticated, response: '" + lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'");
			}
			return lJSONString;
		} catch (IOException lEx) {
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " authenticating session against OAuth host.";
			lJSONString = "{\"code\":-1, \"msg\":\""
					+ lEx.getClass().getSimpleName() + ": " + lEx.getMessage() + "\"}";
			mLog.error("Session could not be authenticated, response: '" + lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'");
			return lJSONString;
		}
	}

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @return
	 */
	public String authDirect(String aUsername, String aPassword) {
		return authDirect(aUsername, aPassword, mDefaultTimeout);
	}

	/**
	 *
	 * @param aAppId
	 * @param aAppSecret
	 * @param aAccessToken
	 * @param aTimeout
	 * @return
	 */
	public String getUser(String aAppId, String aAppSecret, String aAccessToken,
			long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Requesting user for access-token: '" + aAccessToken
					+ "', app-id: '" + aAppId
					+ "', app-secret: '******'"
					+ ", timeout: " + aTimeout + "ms..."
			);
		}
		if (null == aAppSecret) {
			return "{\"code\":-1, \"msg\":\"No client secret passed\"}";
		}
		if (null == aAccessToken) {
			return "{\"code\":-1, \"msg\":\"No access token passed\"}";
		}
		if (aTimeout < 0) {
			return "{\"code\":-1, \"msg\":\"Invalid negative timeout passed\"}";
		}
		String lPostBody;
		mUsername = null;
		mFullname = null;
		mEmail = null;
		String lJSONString = null;
		try {
			lPostBody = "access_token=" + aAccessToken;

			Map lHeaders = new HashMap<String, String>();
			lHeaders.put("Content-Type", "application/x-www-form-urlencoded");

			lJSONString = HTTPSupport.request(mOAuthHost + OAUTH_GETUSER_URL, "POST",
					lHeaders, lPostBody, aTimeout);
			// to simulate a null or empty response or an invalid (e.g. non-JSON) response
			// lJSONString = lJSONString.replaceAll("\"aschulze\"", "null");
			// lJSONString = lJSONString.replaceAll("\"aschulze\"", "\"\"");
			// lJSONString = "<xml>" + lJSONString + "</xml>";
			// lJSONString = "<xml>" + lJSONString + "</xml>";
			// lJSONString = "{\"error\":\"invalid_request\", \"error_description\":\"Validation error\"}";
			// lJSONString = "{\"any other nonsense\":\"non sense\", \"bla bla\":\"radi radi radi\"}";
			Map<String, Object> lJSON = parseJSON(lJSONString);
			if (null != lJSON) {
				String lError = (String) lJSON.get("error");
				if (null != lError) {
					mReturnCode = -1;
					String lErrDescr = (String) lJSON.get("error_description");
					mReturnMsg = lError + " on validating access token: '"
							+ (null != lErrDescr ? lErrDescr : "[No error description from OAuth]") + "'.";
					mLog.error("Username could not be obtained, response: '" + mReturnMsg.replace("\n", "\\n").replace("\r", "\\r") + "'.");
					return "{\"code\":-1, \"msg\":\""
							+ mReturnMsg + "\"}";
				} else {
					mUsername = (String) lJSON.get("login_name");
					mFullname = (String) lJSON.get("full_user_name");
					mEmail = (String) lJSON.get("email");
					if (null == mUsername || mUsername.isEmpty()) {
						mReturnCode = -1;
						mReturnMsg
								= "OAuth did not deliver a username in field 'login_name' ("
								+ (null == mUsername ? "null" : "empty") + "), response: '" + lJSONString + "'.";
						mLog.error(mReturnMsg);
						return "{\"code\":-1, \"msg\":\""
								+ mReturnMsg + "\"}";
					}
				}
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("User obtained, response: '" + lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'");
			}
			return lJSONString;
		} catch (IOException lEx) {
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " validating access token to obtain user name from OAuth host: " + lEx.getMessage();
			lJSONString = "{\"code\":-1, \"msg\":\""
					+ lEx.getClass().getSimpleName()
					+ "\", \"response\":\""
					+ (lJSONString != null ? lJSONString.replace("\"", "\\\"") : "[null]") + "\"}";
			mLog.error("User could not be obtained, response '" + lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'.");
			return lJSONString;
		}
	}

	/**
	 *
	 * @param aAccessToken
	 * @param aTimeout
	 * @return
	 */
	public String getUser(String aAccessToken, long aTimeout) {
		return getUser(mOAuthAppId, mOAuthAppSecret, aAccessToken, aTimeout);
	}

	/**
	 *
	 * @param aAccessToken
	 * @return
	 */
	public String getUser(String aAccessToken) {
		return getUser(mOAuthAppId, mOAuthAppSecret, aAccessToken, mDefaultTimeout);
	}

	/**
	 *
	 * @param aTimeout
	 * @return
	 */
	public String getUser(long aTimeout) {
		return getUser(mOAuthAppId, mOAuthAppSecret, mAccessToken, aTimeout);
	}

	/**
	 *
	 * @return
	 */
	public String getUser() {
		return getUser(mOAuthAppId, mOAuthAppSecret, mAccessToken, mDefaultTimeout);
	}

	/**
	 *
	 * @param aRefreshToken
	 * @param aTimeout
	 * @return
	 */
	public String refreshAccessToken(String aRefreshToken, long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Refreshing access-token with refresh token: '"
					+ aRefreshToken
					+ "', app-id: '" + getOAuthAppId()
					+ "', app-secret: '******'"
					+ "', timeout: " + aTimeout + "ms..."
			);
		}
		String lPostBody;
		String lJSONString;
		try {
			lPostBody
					= "client_id=ro_client"
					+ "&grant_type=refresh_token"
					+ "&refresh_token=" + URLEncoder.encode(aRefreshToken, "UTF-8");
			Map lHeaders = new HashMap<String, String>();
			lHeaders.put("Content-Type", "application/x-www-form-urlencoded");
			String lAuthStr = Base64.encodeBase64String(
					(getOAuthAppId() + ":" + getOAuthAppSecret()).getBytes("UTF-8"));
			lHeaders.put("Authorization", "Basic " + lAuthStr);

			lJSONString = HTTPSupport.request(mOAuthHost + OAUTH_REFRESHTOKEN_URL, "POST",
					lHeaders, lPostBody, aTimeout);
			Map<String, Object> lJSON = parseJSON(lJSONString);
			mAccessToken = (String) lJSON.get("access_token");
			if (mLog.isDebugEnabled()) {
				mLog.debug("Access token refreshed, response: '"
						+ lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'");
			}
			return lJSONString;
		} catch (IOException lEx) {
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " refreshing acceess token from OAuth host.";
			lJSONString = "{\"code\":-1, \"msg\":\""
					+ lEx.getClass().getSimpleName() + ": " + lEx.getMessage() + "\"}";
			mLog.error("Token could not be refreshed, response: '"
					+ lJSONString.replace("\n", "\\n").replace("\r", "\\r") + "'");
			return lJSONString;
		}
	}

	/**
	 *
	 * @param aTimeout
	 * @return
	 */
	public String refreshAccessToken(long aTimeout) {
		return refreshAccessToken(mRefreshToken, aTimeout);
	}

	/**
	 *
	 * @return
	 */
	public String refreshAccessToken() {
		return refreshAccessToken(mDefaultTimeout);
	}

	/**
	 * @return the mDEF_TIMEOUT
	 */
	public long getDefaultTimeout() {
		return mDefaultTimeout;
	}

	/**
	 * @param aDefaultTimeout the mDEF_TIMEOUT to set
	 */
	public void setDefaultTimeout(long aDefaultTimeout) {
		this.mDefaultTimeout = aDefaultTimeout;
	}

	/**
	 * @return the mReturnCode
	 */
	public int getReturnCode() {
		return mReturnCode;
	}

	/**
	 * @return the mReturnMsg
	 */
	public String getReturnMsg() {
		return mReturnMsg;
	}

	/**
	 * @return the mOAuthAppId
	 */
	public String getOAuthAppId() {
		return mOAuthAppId;
	}

	/**
	 * @param mOAuthAppId the mOAuthAppId to set
	 */
	public void setOAuthAppId(String mOAuthAppId) {
		this.mOAuthAppId = mOAuthAppId;
	}

	/**
	 * @return the OAuthAppSecret
	 */
	public String getOAuthAppSecret() {
		return mOAuthAppSecret;
	}

	/**
	 * @param OAuthAppSecret the OAuthAppSecret to set
	 */
	public void setOAuthAppSecret(String OAuthAppSecret) {
		this.mOAuthAppSecret = OAuthAppSecret;
	}

	/**
	 *
	 * @param aURL
	 * @return
	 */
	public String testCall(String aURL) {
		Map lHeaders = new HashMap<String, String>();
		lHeaders.put("Content-Type", "application/x-www-form-urlencoded");
		String lRes = HTTPSupport.request(aURL, "GET", lHeaders, null, 5000);
		return lRes;
	}

	/**
	 * @return the mSessionId
	 */
	public String getSessionId() {
		return mSessionId;
	}

}
