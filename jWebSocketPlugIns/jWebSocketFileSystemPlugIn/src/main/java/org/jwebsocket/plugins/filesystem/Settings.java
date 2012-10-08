//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Settings for Filesystem Plug-in
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.filesystem;

import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.config.JWebSocketConfig;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class Settings {

	private Map<String, String> mAliases = new FastMap<String, String>();

	/**
	 * @return the aliases
	 */
	public Map getAliases() {
		return mAliases;
	}

	/**
	 * @param aAliases the aliases to set
	 */
	public void setAliases(Map aAliases) {
		mAliases = aAliases;
	}

	/**
	 *
	 * @param aAliasName
	 * @return
	 */
	public String getAliasPath(String aAliasName) {
		return mAliases.get(aAliasName);
	}

	/**
	 *
	 * @param aPath
	 * @return
	 */
	public String getAliasName(String aPath) {
		for (Map.Entry<String, String> lEntry : mAliases.entrySet()) {
			if (aPath.startsWith(lEntry.getValue())
					|| aPath.startsWith(JWebSocketConfig.expandEnvAndJWebSocketVars(lEntry.getValue()))) {
				return lEntry.getKey();
			}
		}

		return null;
	}

	/**
	 *
	 * @param aAliasName
	 * @return
	 */
	public String getAliasPath(String aAliasName, String aDefaultValue) {
		String lValue = mAliases.get(aAliasName);
		if (null == lValue) {
			return aDefaultValue;
		}
		return lValue;
	}
}
