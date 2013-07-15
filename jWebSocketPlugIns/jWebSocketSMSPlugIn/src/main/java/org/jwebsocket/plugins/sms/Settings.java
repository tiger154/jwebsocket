//	---------------------------------------------------------------------------
//	jWebSocket - Settings for SMS Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.sms;

/**
 * Define the plug-in settings.
 *
 * @author aschulze
 */
public class Settings {

	private ISMSProvider mProvider;

	/**
	 * Returns the SMS provider instance defined.
	 *
	 * @return the SMS provider
	 */
	public ISMSProvider getProvider() {
		return mProvider;
	}

	/**
	 * Sets the SMS provider instance to use.
	 *
	 * @param aProvider an instance of the SMS provider
	 */
	public void setProvider(ISMSProvider aProvider) {
		mProvider = aProvider;
	}
}
