//	---------------------------------------------------------------------------
//	Copyright (c) 2011 jWebSocket.org, Innotrade GmbH
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
package org.jwebsocket.plugins.system;

import java.util.Map;
import org.jwebsocket.api.WebSocketConnector;

/**
 *
 * @author kyberneees
 */
public class SecurityHelper {

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

	public static boolean userHasAuthority(Map<String, Object> aSessionStorage, String aAuthority) {
		if (!isUserAuthenticated(aSessionStorage)) {
			return false;
		}
		try {
			if (aSessionStorage.containsKey(SystemPlugIn.AUTHORITIES)) {
				String[] lAuthorities = ((String) aSessionStorage.get(SystemPlugIn.AUTHORITIES)).split(" ");
				int end = lAuthorities.length;

				for (int lIndex = 0; lIndex < end; lIndex++) {
					if (lAuthorities[lIndex].equals(aAuthority)) {
						return true;
					}
				}
			}

			return false;
		} catch (Exception ex) {
			//Not necessary to try this
			return false;
		}
	}

	public static boolean isUserAuthenticated(WebSocketConnector aConnector) {
		return isUserAuthenticated(aConnector.getSession().getStorage());
	}

	public static boolean userHasAuthority(WebSocketConnector aConnector, String aAuthority) {
		return userHasAuthority(aConnector.getSession().getStorage(), aAuthority);
	}
}
