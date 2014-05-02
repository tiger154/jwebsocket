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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Schulze
 */
public class OAuthBase implements IOAuth {

	static final Logger mLog = Logger.getLogger(OAuthBase.class);
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
	String mOAuthAppId = null; // global (static) application id (not instance app_id)!
	String mOAuthAppSecret = null; // global (static) application secret (not instance app_secret)!

	static {
		System.setProperty("jsse.enableSNIExtension", "false");
	}

	/**
	 *
	 * @param aURL
	 * @param aMethod
	 * @param aHeaders
	 * @param aPostBody
	 * @param aTimeout
	 * @return
	 */
	public String call2(String aURL, String aMethod, Map<String, String> aHeaders, String aPostBody, long aTimeout) {
		String lResponse = "{\"code\": -1, \"msg\": \"undefined\"";
		try {
			KeyStore lTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			lTrustStore.load(null, null);
			// Trust own CA and all self-signed certs
			SSLContext lSSLContext = SSLContexts.custom()
					.loadTrustMaterial(lTrustStore, new TrustSelfSignedStrategy())
					.build();
			// Allow TLSv1 protocol only
			SSLConnectionSocketFactory lSSLFactory = new SSLConnectionSocketFactory(
					lSSLContext,
					new String[]{"TLSv1"},
					null,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			CloseableHttpClient lHTTPClient = HttpClients.custom()
					.setSSLSocketFactory(lSSLFactory)
					.build();
			HttpUriRequest lRequest;
			if ("POST".equals(aMethod)) {
				lRequest = new HttpPost(aURL);
				((HttpPost) lRequest).setEntity(new ByteArrayEntity(aPostBody.getBytes("UTF-8")));
			} else {
				lRequest = new HttpGet(aURL);
			}
			for (Map.Entry<String, String> lEntry : aHeaders.entrySet()) {
				lRequest.setHeader(lEntry.getKey(), lEntry.getValue());
			}

			// System.out.println("Executing request " + lRequest.getRequestLine());
			// Create a custom response handler
			ResponseHandler<String> lResponseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(
						final HttpResponse lResponse) throws ClientProtocolException, IOException {
					int lStatus = lResponse.getStatusLine().getStatusCode();
					HttpEntity lEntity = lResponse.getEntity();
					return lEntity != null ? EntityUtils.toString(lEntity) : null;
					/*
					 if (status >= 200 && status < 300) {
					 HttpEntity entity = response.getEntity();
					 return entity != null ? EntityUtils.toString(entity) : null;
					 } else {
					 throw new ClientProtocolException("Unexpected response status: " + status);
					 }
					 */
				}

			};
			lResponse = lHTTPClient.execute(lRequest, lResponseHandler);
		} catch (Exception lEx) {
			System.out.println("Request Exception: " + lEx.getMessage());
		} finally {
		}
		return lResponse;
	}

	/**
	 *
	 * @param aURL
	 * @param aReqType
	 * @param aHeaders
	 * @param aPostBody
	 * @param aTimeout
	 * @return
	 */
	public String call(String aURL, String aReqType, Map<String, String> aHeaders, String aPostBody, long aTimeout) {
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
			// lSSLContext = SSLContext.getInstance("SSL");
			lSSLContext.init(null, lTrustManager, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(lSSLContext.getSocketFactory());
		} catch (KeyManagementException lEx) {
			// exceptions could be processed differently
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " initializing SSL connection to OAuth host.";
			return "{\"code\":-1, \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		} catch (NoSuchAlgorithmException lEx) {
			// exceptions could be processed differently
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " initializing SSL connection to OAuth host.";
			return "{\"code\":-1, \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		}

		try {
			URL lURL = new URL(aURL);

			HttpsURLConnection lConn = (HttpsURLConnection) lURL.openConnection();
			// HttpURLConnection lConn = (HttpURLConnection) lURL.openConnection();
			// in case basic authentication is required:
			// String encodedLoginCreds = new Base64().encodeAsString("username:password".getBytes());
			// lConn.setRequestProperty("Authorization", "Basic " + encodedLoginCreds);

			lConn.setSSLSocketFactory(lSSLContext.getSocketFactory());
			lConn.setDoInput(true);
			lConn.setRequestMethod(aReqType);
			// lConn.setFollowRedirects(true);
			for (Map.Entry<String, String> lEntry : aHeaders.entrySet()) {
				lConn.setRequestProperty(lEntry.getKey(), lEntry.getValue());
			}

			lConn.setDoOutput(true);
			DataOutputStream lOut = new DataOutputStream(lConn.getOutputStream());
			// open up the output stream of the connection 
			if (null != aPostBody) {
				lOut.write(aPostBody.getBytes("UTF-8"));
			}
			lOut.close();

			// System.out.println("Resp Code:" + lConn.getResponseCode());
			// System.out.println("Resp Message:" + lConn.getResponseMessage());
			
//			Timer lTimer = new Timer();
//			lTimer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//				}
//			}, aTimeout);

			// get ready to read the response from the server 
			DataInputStream lIS = new DataInputStream(lConn.getInputStream());
			// read in each character until end-of-stream is detected 
			StringBuilder lSB = new StringBuilder();
			for (int lInt = lIS.read(); lInt != -1; lInt = lIS.read()) {
				lSB.append((char) lInt);
			}
			lJSON = lSB.toString();
			lIS.close();

		} catch (IOException lEx) {
			mReturnCode = -1;
			mReturnMsg = lEx.getClass().getSimpleName() + " requesting OAuth host.";
			return "{\"code\":-1, \"msg\":\"" + lEx.getClass().getSimpleName() + "\"}";
		}
		return lJSON;
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
