//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketException (Community Edition, CE)
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
package org.jwebsocket.kit;

/**
 * Exception class to represent jWebSocketServer related exception, extended by unique IDs to
 * identify issue
 *
 * @author Puran Singh, Alexander Schulze
 *
 */
public class WebSocketException extends Exception {

	WebSocketExceptionType mExceptionType = WebSocketExceptionType.UNDEFINED;

	/**
	 * Creates the exception with a given exception message
	 *
	 * @param aMessage the error message
	 */
	public WebSocketException(String aMessage) {
		super(aMessage);
	}

	/**
	 * Creates the exception with a given cause
	 *
	 * @param aCause
	 */
	public WebSocketException(Throwable aCause) {
		super(aCause);
	}

	/**
	 *
	 * @param aMessage
	 * @param aExceptionType
	 */
	public WebSocketException(String aMessage,
			WebSocketExceptionType aExceptionType) {
		super(aMessage);
		mExceptionType = aExceptionType;
	}

	/**
	 * creates the exception with given message
	 *
	 * @param aMessage the error message
	 * @param aThrowable the cause
	 * @param aExceptionType
	 */
	public WebSocketException(String aMessage,
			WebSocketExceptionType aExceptionType, Throwable aThrowable) {
		super(aMessage, aThrowable);
		mExceptionType = aExceptionType;
	}

	/**
	 * creates the exception with given message
	 *
	 * @param aMessage the error message
	 * @param throwable the cause
	 */
	public WebSocketException(String aMessage, Throwable throwable) {
		super(aMessage, throwable);
	}
	private static final long serialVersionUID = 1L;
}
