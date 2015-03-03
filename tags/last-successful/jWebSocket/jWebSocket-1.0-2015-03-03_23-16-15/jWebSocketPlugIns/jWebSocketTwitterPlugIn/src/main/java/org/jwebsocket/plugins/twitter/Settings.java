//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Twitter Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.twitter;

/**
 *
 * @author Alexander Schulze
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
	 * @param aConsumerKey the mConsumerKey to set
	 */
	public void setConsumerKey(String aConsumerKey) {
		mConsumerKey = aConsumerKey;
	}
}
