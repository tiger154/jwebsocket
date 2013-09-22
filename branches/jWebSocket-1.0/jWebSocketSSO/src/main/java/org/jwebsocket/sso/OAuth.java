//	---------------------------------------------------------------------------
//	jWebSocket OAuth implementation for Java (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author aschulze
 */
public class OAuth {

	private String mBaseURL = null;
	private String mClientSecret = null;
	private String mRefreshToken = null;
	private String mAccessToken = null;
	private String mUsername = null;

	public OAuth() {
	}

	public OAuth(String aURL) {
		mBaseURL = aURL;
	}

	public String call(String aURL, String aContentType, String aPostBody) {
		String lJSON;
		SSLContext lSSLContext;

		try {
			// TODO: Make acceptance of unsigned certificates optional!
			// This methodology is used to accept unsigned certficates
			// on the SSL server. Be careful with this in production environments!

			// Create a trust manager to accept unsigned certificates
			TrustManager[] lTrustManager = new TrustManager[]{
				new X509TrustManager() {
					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					@Override
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] aCerts, String aAuthType) {
					}

					@Override
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] aCerts, String aAuthType) {
					}
				}
			};

			// Use this trustmanager to not reject unsigned certificates
			lSSLContext = SSLContext.getInstance("TLS");
			lSSLContext.init(null, lTrustManager, new java.security.SecureRandom());
		} catch (Exception lEx) {
			return "{\"code\":-1; \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		}

		try {
			URL lURL = new URL(mBaseURL);

			HttpsURLConnection lConn = (HttpsURLConnection) lURL.openConnection();
			// in case basic authentication is required:
			// String encodedLoginCreds = new Base64().encodeAsString("username:password".getBytes());
			// lConn.setRequestProperty("Authorization", "Basic " + encodedLoginCreds);
			lConn.setSSLSocketFactory(lSSLContext.getSocketFactory());
			lConn.setDoInput(true);
			lConn.setDoOutput(true);
			lConn.setRequestMethod("POST");
			// lConn.setFollowRedirects(true);
			lConn.setRequestProperty("Content-Type", aContentType);

			// open up the output stream of the connection 
			DataOutputStream lOut = new DataOutputStream(lConn.getOutputStream());
			lOut.write(aPostBody.getBytes("UTF-8"));
			lOut.close();

			// System.out.println("Resp Code:" + lConn.getResponseCode());
			// System.out.println("Resp Message:" + lConn.getResponseMessage());

			// get ready to read the response from the cgi script 
			DataInputStream lIS = new DataInputStream(lConn.getInputStream());
			// read in each character until end-of-stream is detected 
			StringBuilder lSB = new StringBuilder();
			for (int lInt = lIS.read(); lInt != -1; lInt = lIS.read()) {
				lSB.append((char) lInt);
			}
			lJSON = lSB.toString();
			lIS.close();
		} catch (Exception lEx) {
			return "{\"code\":-1; \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		}
		return lJSON;
	}

	/**
	 * @return the BASE_URL
	 */
	public String getBaseURL() {
		return mBaseURL;
	}

	/**
	 * @param aBASE_URL the BASE_URL to set
	 */
	public void setBaseURL(String aBaseURL) {
		mBaseURL = aBaseURL;
	}

	/**
	 * @return the CLIENT_SECRET
	 */
	public String getClientSecret() {
		return mClientSecret;
	}

	/**
	 * @param aCLIENT_SECRET the CLIENT_SECRET to set
	 */
	public void setClientSecret(String aCLIENT_SECRET) {
		mClientSecret = aCLIENT_SECRET;
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

	public String authDirect(String aUsername, String aPassword) {
		String lPostBody;
		try {
			lPostBody =
					"client_id=ro_client"
					+ "&grant_type=password"
					+ "&username=" + URLEncoder.encode(aUsername, "UTF-8")
					+ "&password=" + URLEncoder.encode(aPassword, "UTF-8");
			String lJSONString = call(mBaseURL, "application/x-www-form-urlencoded;", lPostBody);
			ObjectMapper lMapper = new ObjectMapper();
			Map<String, Object> lJSON = lMapper.readValue(lJSONString, Map.class);
			mAccessToken = (String) lJSON.get("access_token");
			mRefreshToken = (String) lJSON.get("refresh_token");
			return lJSONString;
		} catch (Exception lEx) {
			return "{\"code\":-1; \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		}
	}

	public String getUser(String aClientSecret, String aAccessToken) {
		String lPostBody;
		try {
			lPostBody =
					"client_id=rs_client"
					+ "&client_secret=" + aClientSecret
					+ "&grant_type=" + "urn:pingidentity.com:oauth2:grant_type:validate_bearer"
					+ "&token=" + aAccessToken;
			String lJSONString = call(mBaseURL, "application/x-www-form-urlencoded;", lPostBody);
			ObjectMapper lMapper = new ObjectMapper();
			Map<String, Object> lJSON = lMapper.readValue(lJSONString, Map.class);
			Map<String, Object> lAccessToken = (Map) lJSON.get("access_token");
			mUsername = (String) lAccessToken.get("username");
			return lJSONString;
		} catch (Exception lEx) {
			return "{\"code\":-1; \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		}
	}

	public String getUser() {
		return getUser(mClientSecret, mAccessToken);
	}

	public String refreshAccessToken(String aRefreshToken) {
		String lPostBody;
		try {
			lPostBody =
					"client_id=ro_client"
					+ "&grant_type=" + "refresh_token"
					+ "&refresh_token=" + aRefreshToken;
			String lJSONString = call(mBaseURL, "application/x-www-form-urlencoded;", lPostBody);
			ObjectMapper lMapper = new ObjectMapper();
			Map<String, Object> lJSON = lMapper.readValue(lJSONString, Map.class);
			mAccessToken = (String) lJSON.get("access_token");
			return lJSONString;
		} catch (Exception lEx) {
			return "{\"code\":-1; \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		}
	}

	public String refreshAccessToken() {
		return refreshAccessToken(mRefreshToken);
	}
}
