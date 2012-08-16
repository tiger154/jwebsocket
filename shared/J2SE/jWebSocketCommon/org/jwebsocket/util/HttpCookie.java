//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2012 Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.util;

import java.net.URI;
import java.util.List;
import javolution.util.FastList;

/**
 *
 * @author kyberneees
 */
public class HttpCookie {

	private String mName;
	private String mValue;
	private String mDomain;
	private String mPath;
	private boolean mSecure;
	private boolean mHttpOnly;

	public HttpCookie(String aName, String aValue, String aDomain, String aPath,
			boolean aSecure, boolean aHttpOnly) {
		this.mName = aName;
		this.mValue = aValue;
		this.mDomain = aDomain;
		this.mPath = aPath;
		this.mSecure = aSecure;
		this.mHttpOnly = aHttpOnly;
	}

	public String getDomain() {
		return mDomain;
	}

	public void setDomain(String domain) {
		this.mDomain = domain;
	}

	public boolean isHttpOnly() {
		return mHttpOnly;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.mHttpOnly = httpOnly;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
	}

	public boolean isSecure() {
		return mSecure;
	}

	public void setSecure(boolean secure) {
		this.mSecure = secure;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		this.mValue = value;
	}

	public static List<HttpCookie> parse(URI aUri, List<String> aCookieList) throws Exception {
		List<HttpCookie> lCookies = new FastList<HttpCookie>();

		for (String lCookie : aCookieList) {
			lCookies.add(HttpCookie.parse(aUri, lCookie));
		}

		return lCookies;
	}

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
