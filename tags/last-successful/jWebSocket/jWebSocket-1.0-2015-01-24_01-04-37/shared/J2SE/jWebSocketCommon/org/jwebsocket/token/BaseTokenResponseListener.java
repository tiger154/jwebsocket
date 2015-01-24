//	---------------------------------------------------------------------------
//	jWebSocket BaseTokenResponseListener (Community Edition, CE)
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
package org.jwebsocket.token;

/**
 *
 * @author Alexander Schulze
 */
public class BaseTokenResponseListener implements WebSocketResponseTokenListener {

	private long mTimeout = 5000;

	/**
	 *
	 */
	public BaseTokenResponseListener() {
	}

	/**
	 *
	 * @param aTimeout
	 */
	public BaseTokenResponseListener(long aTimeout) {
		setTimeout(aTimeout);
	}

	/**
	 * Returns the timeout of the request.
	 *
	 * @return
	 */
	@Override
	public final long getTimeout() {
		return mTimeout;
	}

	/**
	 * Specifies the timeout of the request.
	 *
	 * @param aTimeout
	 */
	@Override
	public final void setTimeout(long aTimeout) {
		mTimeout = aTimeout;
	}

	/**
	 * Is fired when the given response timeout is exceeded.
	 *
	 * @param aToken
	 */
	@Override
	public void OnTimeout(Token aToken) {
	}

	/**
	 * Is fired on any response to a send token.
	 *
	 * @param aToken
	 */
	@Override
	public void OnResponse(Token aToken) {
	}

	/**
	 * Is fired if token.code equals 0 (zero).
	 *
	 * @param aToken
	 */
	@Override
	public void OnSuccess(Token aToken) {
	}

	/**
	 * Is fired if token.code does not equal 0 (zero).
	 *
	 * @param aToken
	 */
	@Override
	public void OnFailure(Token aToken) {
	}
}
