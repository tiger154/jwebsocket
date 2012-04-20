//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Settings for Twitter Plug-in
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.twitter;

/**
 *
 * @author aschulze
 */
public class Settings {

	private String mConsumerKey;
	private String mConsumerSecret;
	private Integer mAppKey;
	private String mAccessKey;
	private String mAccessSecret;

	/**
	 * @return the mConsumerSecret
	 */
	public String getConsumerSecret() {
		return mConsumerSecret;
	}

	/**
	 * @param aConsumerSecret the mConsumerSecret to set
	 */
	public void setConsumerSecret(String aConsumerSecret) {
		mConsumerSecret = aConsumerSecret;
	}

	/**
	 * @return the mAppKey
	 */
	public Integer getAppKey() {
		return mAppKey;
	}

	/**
	 * @param aAppKey the mAppKey to set
	 */
	public void setAppKey(Integer aAppKey) {
		mAppKey = aAppKey;
	}

	/**
	 * @return the mAccessKey
	 */
	public String getAccessKey() {
		return mAccessKey;
	}

	/**
	 * @param aAccessKey the mAccessKey to set
	 */
	public void setAccessKey(String aAccessKey) {
		mAccessKey = aAccessKey;
	}

	/**
	 * @return the mAccessSecret
	 */
	public String getAccessSecret() {
		return mAccessSecret;
	}

	/**
	 * @param aAccessSecret the mAccessSecret to set
	 */
	public void setAccessSecret(String aAccessSecret) {
		mAccessSecret = aAccessSecret;
	}

	/**
	 * @return the mConsumerKey
	 */
	public String getConsumerKey() {
		return mConsumerKey;
	}

	/**
	 * @param mConsumerKey the mConsumerKey to set
	 */
	public void setConsumerKey(String mConsumerKey) {
		this.mConsumerKey = mConsumerKey;
	}
}
