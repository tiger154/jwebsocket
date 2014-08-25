//	---------------------------------------------------------------------------
//	jWebSocket Handler (Community Edition, CE)
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
package org.jwebsocket.eventbus;

import org.jwebsocket.api.IEventBus;
import org.jwebsocket.token.BaseTokenResponseListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class Handler extends BaseTokenResponseListener implements IEventBus.IHandler {

	public static final Integer STATUS_OK = 0;
	private IEventBus mEB;

	public Handler() {
		setTimeout(0);
	}

	public Handler(Long aTimeout) {
		super(aTimeout);
	}

	@Override
	public void setEventBus(IEventBus aEB) {
		mEB = aEB;
	}

	@Override
	public IEventBus getEventBus() {
		return mEB;
	}

	@Override
	public void reply(Token aResponse) {
		mEB.send(aResponse);
	}

	@Override
	public void reply(Token aResponse, IEventBus.IHandler aHandler) {
		mEB.send(aResponse, aHandler);
	}

	Token createErrorResponse(Token aInToken) {
		Token lError = createResponse(aInToken);
		lError.setCode(-1);

		return lError;
	}

	Token createResponse(Token aInToken) {
		Token lResponse = TokenFactory.createToken(aInToken.getNS(), "response");
		lResponse.setCode(STATUS_OK);
		lResponse.setString(JMSEventBus.ATTR_TOKEN_BUS_UTID, aInToken.getString(JMSEventBus.ATTR_TOKEN_BUS_UTID));
		lResponse.setString("reqType", aInToken.getType());

		return lResponse;
	}

	@Override
	public void OnMessage(Token aToken) {
	}
}
