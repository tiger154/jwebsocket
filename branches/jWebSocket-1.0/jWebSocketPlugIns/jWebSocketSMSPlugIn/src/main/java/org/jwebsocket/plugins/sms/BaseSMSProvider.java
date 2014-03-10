//	---------------------------------------------------------------------------
//	jWebSocket SMS Plug-In, base features for all providers (Community Edition, CE)
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
package org.jwebsocket.plugins.sms;

/**
 * Provides the base class for the SMS providers. Contains common methods for
 * the providers.
 *
 * @author Alexander Schulze
 */
public class BaseSMSProvider {

	/**
	 * Allows to validate the format of a given phone number. Returns the phone
	 * number in a correct format.
	 *
	 * @param aNumber a phone number to validate
	 * @return a valid phone number
	 */
	public String trimPhoneNumber(String aNumber) {
		StringBuilder lRes = new StringBuilder(aNumber.length());
		for (int i = 0; i < aNumber.length(); i++) {
			char lC = aNumber.charAt(i);
			if (lC >= '0' && lC <= '9'
					| lC == '+') {
				lRes.append(lC);
			}
		}
		return lRes.toString();
	}
}
