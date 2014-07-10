/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.sso;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Map;
import javax.net.ssl.SSLContext;
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

/**
 *
 * @author aschulze
 */
public class HTTPSupport {

	/**
	 *
	 * @param aURL
	 * @param aMethod
	 * @param aHeaders
	 * @param aPostBody
	 * @param aTimeout
	 * @return
	 */
	public static String call2(String aURL, String aMethod, Map<String, String> aHeaders, String aPostBody, long aTimeout) {
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
					
//					if (lStatus >= 200 && lStatus < 300) {
//						HttpEntity entity = lResponse.getEntity();
//						return entity != null ? EntityUtils.toString(entity) : null;
//					} else {
//						throw new ClientProtocolException("Unexpected response status: " + lStatus);
//					}
					
				}

			};
			lResponse = lHTTPClient.execute(lRequest, lResponseHandler);
		} catch (Exception lEx) {
			System.out.println("HTTP Request " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		} finally {
		}
		return lResponse;
	}
}
