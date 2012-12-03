//	---------------------------------------------------------------------------
//	jWebSocket - FileSystemPlugInListener
//	Copyright (c) 2012 jWebSocket.org, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.client.plugins.filesystem;

import org.jwebsocket.api.WebSocketClientTokenPlugInListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees
 */
public class FileSystemPlugInListener implements WebSocketClientTokenPlugInListener {

	@Override
	public void processToken(Token aToken) {
		if ("load".equals(aToken.getString("reqType"))) {
			if (new Integer(0).equals(aToken.getCode())) {
				if (Boolean.TRUE.equals(aToken.getBoolean("decode"))) {
					aToken.getMap().put("data", Tools.base64Decode(aToken.getString("data")));
				}
				OnFileLoaded(aToken);
			} else {
				OnFileError(aToken);
			}
		} else if ("send".equals(aToken.getString("reqType"))) {
			if (new Integer(0).equals(aToken.getCode())) {
				OnFileSent(aToken);
			} else {
				OnFileError(aToken);
			}
		} else if ("event".equals(aToken.getType())) {
			if ("filesaved".equals(aToken.getString("name"))) {
				OnFileSaved(aToken);
			} else if ("filereceived".equals(aToken.getString("name"))) {
				OnFileReceived(aToken);
			}
		}
	}

	/**
	 * Called when an error occur during the file-system life-cycle.
	 *
	 * @param aToken
	 */
	public void OnFileError(Token aToken) {
	}

	/**
	 * Called when a file has been loaded.
	 *
	 * @param aToken
	 */
	public void OnFileLoaded(Token aToken) {
	}

	/**
	 * Called when a file has been sent.
	 *
	 * @param aToken
	 */
	public void OnFileSent(Token aToken) {
	}

	/**
	 * Called when a file has been saved.
	 *
	 * @param aToken
	 */
	public void OnFileSaved(Token aToken) {
	}

	/**
	 * Called when a file has been received.
	 *
	 * @param aToken
	 */
	public void OnFileReceived(Token aToken) {
	}
}
