// ---------------------------------------------------------------------------
// jWebSocket - BaseSharedObject (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, softwareI
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.sharedobjects;

import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class BaseSharedObject implements ISharedObject {

	@Override
	public void init(String aUserId) {
	}

	@Override
	public String getClassName() {
		return null;
	}

	@Override
	public Token read(String aSubId) {
		return null;
	}

	@Override
	public void write(String aSubId, Token aToken) {
	}

	@Override
	public Token invoke(Token aToken) {
		return null;
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void lock(String aSubId, String aUserId) {
	}

	@Override
	public void unlock(String aSubId, String aUserId) {
	}

	@Override
	public void grant(String aSubId, String aUserId, int aRight) {
	}

	@Override
	public void revoke(String aSubId, String aUserId, int aRight) {
	}

	@Override
	public void registerClient(String aUserId, String aClientId) {
	}

	@Override
	public void unregisterClient(String aUserId, String aClientId) {
	}
}
