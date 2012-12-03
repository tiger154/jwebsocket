//  ---------------------------------------------------------------------------
//  jWebSocket - BaseSubscriberStore
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
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
 * @author aschulze
 */
public class MailStore {

	/**
	 * logger object
	 */
	private static Logger mLog = Logging.getLogger();
	private static BaseStorage mStorage = null;

	/**
	 * default constructor
	 */
	public MailStore(BaseStorage aStorage) {
		mStorage = aStorage;
	}

	public Token getMail(String aId) {
		Token lRes = null;
		try {
			String lStr = (String) mStorage.get(aId);
			lRes = JSONProcessor.JSONStringToToken(lStr);
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName()
					+ " parsing JSON data for mail id '"
					+ aId + "': " + lEx.getMessage();
			mLog.error(lMsg);
		}
		return lRes;
	}

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

	public void clearMails() {
		mStorage.clear();
	}

	public int getMailStoreSize() {
		return mStorage.size();
	}
}
