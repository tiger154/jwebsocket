//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Filesystem Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
		return JWebSocketConfig.expandEnvAndJWebSocketVars(mAliases.get(aAliasName));
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
	 * @param aDefaultValue
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
