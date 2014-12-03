//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMS NullProvider (Community Edition, CE)
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

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * Null implementation of ISMSProvider interface for testing purposes
 *
 * @author Rolando Santamaria Maso
 */
public class NullProvider implements ISMSProvider {

	private static final Logger mLog = Logging.getLogger();

	@Override
	public Token sendSms(Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending SMS...");
		}

		return TokenFactory.createToken();
	}

	@Override
	public Token longerSms(Token aToken) {
		return TokenFactory.createToken();
	}

	@Override
	public Token gsmSms(Token aToken) {
		return TokenFactory.createToken();
	}

	@Override
	public Token bulkSms(Token aToken) {
		return TokenFactory.createToken();
	}
}
