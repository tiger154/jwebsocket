//	---------------------------------------------------------------------------
//	jWebSocket HttpCookie (Community Edition, CE)
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
package org.jwebsocket.util;

import java.net.URI;
import java.util.List;
import javolution.util.FastList;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class HttpCookie {

	private String mName;
	private String mValue;
	private String mDomain;
	private String mPath;
	private boolean mSecure;
	private boolean mHttpOnly;

	/**
	 *
	 * @param aName
	 * @param aValue
	 * @param aDomain
	 * @param aPath
	 * @param aSecure
	 * @param aHttpOnly
	 */
	public HttpCookie(String aName, String aValue, String aDomain, String aPath,
			boolean aSecure, boolean aHttpOnly) {
		this.mName = aName;
		this.mValue = aValue;
		this.mDomain = aDomain;
		this.mPath = aPath;
		this.mSecure = aSecure;
		this.mHttpOnly = aHttpOnly;
	}

	/**
	 *
	 * @return
	 */
	public String getDomain() {
		return mDomain;
	}

	/**
	 *
	 * @param domain
	 */
	public void setDomain(String domain) {
		this.mDomain = domain;
	}

	/**
	 *
	 * @return
	 */
	public boolean isHttpOnly() {
		return mHttpOnly;
	}

	/**
	 *
	 * @param httpOnly
	 */
	public void setHttpOnly(boolean httpOnly) {
		this.mHttpOnly = httpOnly;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return mName;
	}

	/**
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 *
	 * @return
	 */
	public String getPath() {
		return mPath;
	}

	/**
	 *
	 * @param path
	 */
	public void setPath(String path) {
		this.mPath = path;
	}

	/**
	 *
	 * @return
	 */
	public boolean isSecure() {
		return mSecure;
	}

	/**
	 *
	 * @param secure
	 */
	public void setSecure(boolean secure) {
		this.mSecure = secure;
	}

	/**
	 *
	 * @return
	 */
	public String getValue() {
		return mValue;
	}

	/**
	 *
	 * @param value
	 */
	public void setValue(String value) {
		this.mValue = value;
	}

	/**
	 *
	 * @param aUri
	 * @param aCookieList
	 * @return
	 * @throws Exception
	 */
	public static List<HttpCookie> parse(URI aUri, List<String> aCookieList) throws Exception {
		List<HttpCookie> lCookies = new FastList<HttpCookie>();

		for (String lCookie : aCookieList) {
			lCookies.add(HttpCookie.parse(aUri, lCookie));
		}

		return lCookies;
	}

	/**
	 *
	 * @param aUri
	 * @param aCookie
	 * @return
	 * @throws Exception
	 */
	public static HttpCookie parse(URI aUri, String aCookie) throws Exception {
		String[] lCookieParts = aCookie.split("; ");
		String[] lNameValue = lCookieParts[0].split("=", 2);

		String lName = lNameValue[0];
		String lValue = lNameValue[1];
		String lPath = aUri.getPath();
		String lDomain = aUri.getHost();

		if (lCookieParts.length > 1) {
			for (int lIndex = 1; lIndex < lCookieParts.length; lIndex++) {
				if (lCookieParts[lIndex].startsWith("Path=")) {
					String[] lPathParts = lCookieParts[lIndex].split("=", 2);
					lPath = lPathParts[1];
				} else if (lCookieParts[lIndex].startsWith("Domain=")) {
					String[] lDomainParts = lCookieParts[lIndex].split("=", 2);
					lDomain = lDomainParts[1];
				}
			}
		}

		Boolean lSecure = aCookie.contains("; Secure");
		Boolean lHttpOnly = aCookie.contains("; HttpOnly");

		return new HttpCookie(lName, lValue, lDomain, lPath, lSecure, lHttpOnly);
	}

	/**
	 *
	 * @param aUri
	 * @param aCookie
	 * @return
	 */
	public static boolean isValid(URI aUri, HttpCookie aCookie) {
		String lCookiePath = aCookie.getPath();
		String lDomain = aCookie.getDomain();
		if (!lCookiePath.endsWith("/")) {
			lCookiePath += "/";
		}
		if (!lDomain.startsWith(".")) {
			lDomain = "." + lDomain;
		}

		return ((("https".equals(aUri.getScheme()) || "wss".equals(aUri.getScheme())) && aCookie.isSecure())
				|| (("http".equals(aUri.getScheme()) || "ws".equals(aUri.getScheme())) && !aCookie.isSecure()))
				&& (aUri.getPath().equals(aCookie.getPath()) || aUri.getPath().startsWith(lCookiePath))
				&& (aUri.getHost().equals(aCookie.getDomain()) || aUri.getHost().endsWith(lDomain));
	}

	@Override
	public boolean equals(Object aObject) {
		if (aObject instanceof HttpCookie) {
			return aObject.hashCode() == hashCode();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int lHash = 3;
		lHash = 71 * lHash + (this.mName != null ? this.mName.hashCode() : 0);
		lHash = 71 * lHash + (this.mValue != null ? this.mValue.hashCode() : 0);
		lHash = 71 * lHash + (this.mDomain != null ? this.mDomain.hashCode() : 0);
		lHash = 71 * lHash + (this.mPath != null ? this.mPath.hashCode() : 0);
		lHash = 71 * lHash + (this.mSecure ? 1 : 0);
		lHash = 71 * lHash + (this.mHttpOnly ? 1 : 0);

		return lHash;
	}
}
