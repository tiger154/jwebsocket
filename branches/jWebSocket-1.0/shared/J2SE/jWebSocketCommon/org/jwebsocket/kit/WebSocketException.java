//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketException (Community Edition, CE)
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
package org.jwebsocket.kit;

/**
 * Exception class to represent jWebSocketServer related exception, extended by
 * unique IDs to identify issue
 *
 * @author Puran Singh, Alexander Schulze
 *
 */
public class WebSocketException extends Exception {

	WebSocketExceptionType mExceptionType = WebSocketExceptionType.UNDEFINED;

	/**
	 * creates the exception with given message
	 *
	 * @param aError the error message
	 */
	public WebSocketException(String aError) {
		super(aError);
	}

	/**
	 *
	 * @param aError
	 * @param aExceptionType
	 */
	public WebSocketException(String aError,
			WebSocketExceptionType aExceptionType) {
		super(aError);
		mExceptionType = aExceptionType;
	}

	/**
	 * creates the exception with given message
	 *
	 * @param aError the error message
	 * @param aThrowable the cause
	 * @param aExceptionType
	 */
	public WebSocketException(String aError,
			WebSocketExceptionType aExceptionType, Throwable aThrowable) {
		super(aError, aThrowable);
		mExceptionType = aExceptionType;
	}

	/**
	 * creates the exception with given message
	 *
	 * @param aError the error message
	 * @param throwable the cause
	 */
	public WebSocketException(String aError, Throwable throwable) {
		super(aError, throwable);
	}
	private static final long serialVersionUID = 1L;
}
