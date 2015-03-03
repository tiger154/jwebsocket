//	---------------------------------------------------------------------------
//	jWebSocket - MailStore (Community Edition, CE)
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
package org.jwebsocket.plugins.mail;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.storage.BaseStorage;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * Mail store based extension of EhCacheStorage.
 *
 * @author Alexander Schulze
 */
public class MailStore {

	/**
	 * logger object
	 */
	private static final Logger mLog = Logging.getLogger();
	private static BaseStorage<String, String> mStorage = null;

	/**
	 * default constructor
	 *
	 * @param aStorage
	 */
	public MailStore(BaseStorage<String, String> aStorage) {
		mStorage = aStorage;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public Token getMail(String aId) {
		Token lRes = null;
		try {
			String lStr = mStorage.get(aId);
			lRes = JSONProcessor.JSONStringToToken(lStr);
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName()
					+ " parsing JSON data for mail id '"
					+ aId + "': " + lEx.getMessage();
			mLog.error(lMsg);
		}
		return lRes;
	}

	/**
	 *
	 * @param aMail
	 * @return
	 */
	public Token storeMail(Token aMail) {
		Token lRes = TokenFactory.createToken();
		try {
			String lStr = JSONProcessor.tokenToPacket(aMail).getUTF8();
			mStorage.put(aMail.getString("id"), lStr);
			lRes.setInteger("code", 0);
			lRes.setString("msg", "ok");
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName()
					+ " creating JSON data for mail id '"
					+ aMail.getString("id") + "': " + lEx.getMessage();
			mLog.error(lMsg);
			lRes.setInteger("code", -1);
			lRes.setString("msg", lMsg);
		}
		return lRes;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public Token removeMail(String aId) {
		Token lRes = TokenFactory.createToken();
		if (mStorage.containsKey(aId)) {
			mStorage.remove(aId);
			lRes.setInteger("code", 0);
			lRes.setString("msg", "ok");
		} else {
			lRes.setInteger("code", -1);
			lRes.setString("msg", "No mail with ID '"
					+ aId + "' found in mail store .");
		}
		return lRes;
	}

	/**
	 *
	 */
	public void clearMails() {
		mStorage.clear();
	}

	/**
	 *
	 * @return
	 */
	public int getMailStoreSize() {
		return mStorage.size();
	}
}
