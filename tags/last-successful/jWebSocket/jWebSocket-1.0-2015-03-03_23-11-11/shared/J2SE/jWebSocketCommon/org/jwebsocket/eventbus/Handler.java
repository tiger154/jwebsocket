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

/**
 *
 * @author Rolando Santamaria Maso
 */
public class Handler extends BaseTokenResponseListener implements IEventBus.IHandler {

	public static final Integer STATUS_OK = 0;
	private IEventBus mEB;
	private IEventListener mEventListener;

	public Handler() {
		this(new Long(0));
	}

	public Handler(IEventListener aListener, long aTimeout) {
		super(aTimeout);
		mEventListener = aListener;
	}

	public Handler(IEventListener aListener) {
		this(aListener, new Long(0));
	}

	public Handler(Long aTimeout) {
		this(null, aTimeout);
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
		reply(aResponse, null);
	}

	@Override
	public void reply(Token aResponse, IEventBus.IHandler aHandler) {
		mEB.send(aResponse, aHandler);
	}

	Token createErrorResponse(Token aInToken) {
		return getEventBus().createErrorResponse(aInToken);
	}

	Token createResponse(Token aInToken) {
		return getEventBus().createResponse(aInToken);
	}

	@Override
	public void OnMessage(Token aToken) {
		if (null != mEventListener) {
			mEventListener.OnMessage(aToken);
		}
	}

	@Override
	public void OnTimeout(Token aToken) {
		if (null != mEventListener) {
			mEventListener.OnTimeout(aToken);
		}
	}

	public interface IEventListener {

		public void OnMessage(Token aToken);

		public void OnTimeout(Token aToken);
	}
}
