//	---------------------------------------------------------------------------
//	jWebSocket - FileSystemPlugInListener (Community Edition, CE)
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
package org.jwebsocket.client.plugins.filesystem;

import org.jwebsocket.api.WebSocketClientTokenPlugInListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
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
