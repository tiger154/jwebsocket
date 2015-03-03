//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketRuntimeException (Community Edition, CE)
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
 * Exception class to represent JWebSocketServer related exception
 *
 * @author Puran Singh
 * @version $Id: WebSocketException.java 148 2010-03-07 05:24:10Z mailtopuran $
 *
 */
public class WebSocketRuntimeException extends RuntimeException {

	/**
	 * creates the exception with given message
	 *
	 * @param error the error messae
	 */
	public WebSocketRuntimeException(String error) {
		super(error);
	}

	/**
	 * creates the exception with given message
	 *
	 * @param error the error messae
	 * @param throwable the cause
	 */
	public WebSocketRuntimeException(String error, Throwable throwable) {
		super(error, throwable);
	}
	private static final long serialVersionUID = 1L;
}
