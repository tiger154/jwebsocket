//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMS Provider interface (Community Edition, CE)
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

import org.jwebsocket.token.Token;

/**
 * Provides the base interface for the SMS providers.
 *
 * @author mayra
 */
public interface ISMSProvider {

	/**
	 * Allows to send a SMS through a defined provider.
	 *
	 * @param aToken the request token object that should contain the followings
	 * attributes:
	 * <p>
	 * <ul>
	 * <li>
	 * message: SMS message text
	 * </li>
	 * <li>
	 * to: Receiver of SMS
	 * </li>
	 * <li>
	 * from: Source identifier
	 * </li>
	 * </ul>
	 * </p>
	 * @return a map with the response code from the SMS provider
	 */
	public Token sendSms(Token aToken);

	/**
	 * Allows to send SMS longer than 160 characters. In this cases, the longer
	 * text is automatically divided up and sent in several parts. The receiver
	 * then should combines the parts of the interrelated text again in the
	 * terminal device.
	 *
	 * @param aToken the request token object
	 * @return a map with the response code from the SMS provider
	 */
	public Token longerSms(Token aToken);

	/**
	 *
	 *
	 * @param aToken the request token object
	 * @return a map with the response code from the SMS provider
	 */
	public Token gsmSms(Token aToken);

	/**
	 * Allows to send a large numbers of SMS messages to mobile phone terminals.
	 * Implements the bulk send setting up a separate HTTP call in a loop each
	 * time. In this way is possible to receive a response code and a message ID
	 * for each message. Sending can also be stopped here at any moment.
	 *
	 * @param aToken the request token object that should contain the followings
	 * attributes:
	 * <p>
	 * <ul>
	 * <li>
	 * message: SMS message text
	 * </li>
	 * <li>
	 * to: SMS receivers separated by ';'
	 * </li>
	 * <li>
	 * from: Source identifier
	 * </li>
	 * </ul>
	 * </p>
	 * @return a map with the response code from the SMS provider
	 */
	public Token bulkSms(Token aToken);
}
