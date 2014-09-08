//	---------------------------------------------------------------------------
//	jWebSocket OAuth interface for Java (Community Edition, CE)
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
//	this plug-in is based on: 
//	http://tools.ietf.org/html/rfc6749 - The OAuth 2.0 Authorization Framework
//	http://tools.ietf.org/html/rfc6750 - The OAuth 2.0 Authorization Framework: Bearer Token Usage
//	http://oauth.net/2/ - OAuth 2.0
//	---------------------------------------------------------------------------
package org.jwebsocket.sso;

/**
 *
 * @author Alexander Schulze
 */
public interface IOAuth {

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @param aTimeout
	 * @return
	 * @throws SSOException
	 */
	String getSSOSession(String aUsername, String aPassword, long aTimeout) throws SSOException;

	/**
	 *
	 * @param aSessionId
	 * @param aTimeout
	 * @return
	 * @throws SSOException
	 */
	String authSession(String aSessionId, long aTimeout) throws SSOException;

}
