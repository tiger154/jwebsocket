//	---------------------------------------------------------------------------
//	jWebSocket - CloseReason Enumeration (Community Edition, CE)
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
 * Enumeration to handle reasons for connection close.
 *
 * @author Alexander Schulze
 */

/*

 See http://docs.oracle.com/javaee/7/api/javax/websocket/CloseReason.CloseCodes.html for details:
  
 1000  indicates a normal closure, meaning that the purpose for
 which the connection was established has been fulfilled.

 1001  indicates that an endpoint is "going away", such as a server
 going down or a browser having navigated away from a page.

 1002  indicates that an endpoint is terminating the connection due
 to a protocol error.

 1003  indicates that an endpoint is terminating the connection
 because it has received a type of data it cannot accept (e.g., an
 endpoint that understands only text data MAY send this if it
 receives a binary message).

 1004  Reserved.  The specific meaning might be defined in the future.

 1005  is a reserved value and MUST NOT be set as a status code in a
 Close control frame by an endpoint.  It is designated for use in
 applications expecting a status code to indicate that no status
 code was actually present.

 1006  is a reserved value and MUST NOT be set as a status code in a
 Close control frame by an endpoint.  It is designated for use in
 applications expecting a status code to indicate that the
 connection was closed abnormally, e.g., without sending or
 receiving a Close control frame.

 1007  indicates that an endpoint is terminating the connection
 because it has received data within a message that was not
 consistent with the type of the message (e.g., non-UTF-8 [RFC3629]
 data within a text message).

 1008  indicates that an endpoint is terminating the connection
 because it has received a message that violates its policy.  This
 is a generic status code that can be returned when there is no
 other more suitable status code (e.g., 1003 or 1009) or if there
 is a need to hide specific details about the policy.

 1009  indicates that an endpoint is terminating the connection
 because it has received a message that is too big for it to
 process.

 1010  indicates that an endpoint (client) is terminating the
 connection because it has expected the server to negotiate one or
 more extension, but the server didn't return them in the response
 message of the WebSocket handshake.  The list of extensions that
 are needed SHOULD appear in the /reason/ part of the Close frame.
 Note that this status code is not used by the server, because it
 can fail the WebSocket handshake instead.

 1011  indicates that a server is terminating the connection because
 it encountered an unexpected condition that prevented it from
 fulfilling the request.

 1015  is a reserved value and MUST NOT be set as a status code in a
 Close control frame by an endpoint.  It is designated for use in
 applications expecting a status code to indicate that the
 connection was closed due to a failure to perform a TLS handshake
 (e.g., the server certificate can't be verified).

 0000-0999
 Status codes in the range 0-999 are not used.

 1000-2999
 Status codes in the range 1000-2999 are reserved for definition by
 this protocol, its future revisions, and extensions specified in a
 permanent and readily available public specification.

 3000-3999
 Status codes in the range 3000-3999 are reserved for use by
 libraries, frameworks, and applications.  These status codes are
 registered directly with IANA.  The interpretation of these codes
 is undefined by this protocol.

 4000-4999
 Status codes in the range 4000-4999 are reserved for private use
 and thus can't be registered.  Such codes can be used by prior
 agreements between WebSocket applications.  The interpretation of
 these codes is undefined by this protocol.
  
 */
public enum CloseReason {

	/**
	 * The connection was broken.
	 */
	BROKEN(1011),
	/**
	 * The session timeout exceeded.
	 */
	TIMEOUT(1002),
	/**
	 * The server closed the connection.
	 */
	SERVER(1000),
	/**
	 * The client closed the connection.
	 */
	CLIENT(4001),
	/**
	 * The server was shut down.
	 */
	SHUTDOWN(1001),
	/**
	 * The server rejects the connection
	 */
	SERVER_REJECT_CONNECTION(4002),
	/**
	 * The server redirect new connections
	 */
	SERVER_REDIRECT_CONNECTION(4003);
	private final int mCode;

	CloseReason(int aCode) {
		mCode = aCode;
	}

	/**
	 *
	 * @return
	 */
	public int getCode() {
		return mCode;
	}
}
