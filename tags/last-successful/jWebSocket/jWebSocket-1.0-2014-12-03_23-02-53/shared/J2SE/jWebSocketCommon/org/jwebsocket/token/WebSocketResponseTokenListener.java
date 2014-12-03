//	---------------------------------------------------------------------------
//	jWebSocket WebSocketResponseTokenListener (Community Edition, CE)
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
public interface WebSocketResponseTokenListener {

	/**
	 * Returns the timeout of the request.
	 *
	 * @return
	 */
	long getTimeout();

	/**
	 * Specifies the timeout of the request.
	 *
	 * @param aTimeout
	 */
	void setTimeout(long aTimeout);

	/**
	 * Is fired when the given response timeout is exceeded.
	 *
	 * @param aToken
	 */
	void OnTimeout(Token aToken);

	/**
	 * Is fired on any response to a send token.
	 *
	 * @param aToken
	 */
	void OnResponse(Token aToken);

	/**
	 * Is fired if token.code equals 0 (zero).
	 *
	 * @param aToken
	 */
	void OnSuccess(Token aToken);

	/**
	 * Is fired if token.code does not equal 0 (zero).
	 *
	 * @param aToken
	 */
	void OnFailure(Token aToken);
}
