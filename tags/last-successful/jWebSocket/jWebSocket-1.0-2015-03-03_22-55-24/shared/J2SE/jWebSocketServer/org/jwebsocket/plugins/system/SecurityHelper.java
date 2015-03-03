//	---------------------------------------------------------------------------
//	jWebSocket - SecurityHelper (Community Edition, CE)
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
package org.jwebsocket.plugins.system;

import java.util.Map;
import org.jwebsocket.api.WebSocketConnector;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class SecurityHelper {

	/**
	 *
	 * @param aSessionStorage
	 * @return
	 */
	public static Boolean isUserAuthenticated(Map<String, Object> aSessionStorage) {
		try {
			if (aSessionStorage.containsKey(SystemPlugIn.IS_AUTHENTICATED)) {
				return (Boolean) aSessionStorage.get(SystemPlugIn.IS_AUTHENTICATED);
			}
			return false;
		} catch (Exception ex) {
			// Not necessary to try this
			return false;
		}
	}

	/**
	 *
	 * @param aSessionStorage
	 * @param aAuthority
	 * @return
	 */
	public static boolean userHasAuthority(Map<String, Object> aSessionStorage, String aAuthority) {
		if (!isUserAuthenticated(aSessionStorage)) {
			return false;
		}
		try {
			if (aSessionStorage.containsKey(SystemPlugIn.AUTHORITIES)) {
				return aSessionStorage.get(SystemPlugIn.AUTHORITIES).toString().contains(aAuthority + " ");
			}
			return false;
		} catch (Exception ex) {
			//Not necessary to try this
			return false;
		}
	}

	/**
	 *
	 * @param aConnector
	 * @return
	 */
	public static boolean isUserAuthenticated(WebSocketConnector aConnector) {
		return aConnector.getSession().isAuthenticated();
	}

	/**
	 *
	 * @param aConnector
	 * @param aAuthority
	 * @return
	 */
	public static boolean userHasAuthority(WebSocketConnector aConnector, String aAuthority) {
		return userHasAuthority(aConnector.getSession().getStorage(), aAuthority);
	}
}
