//	---------------------------------------------------------------------------
//	jWebSocket - Settings for JDBC Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.jdbc;

import java.util.Map;
import javolution.util.FastMap;

/**
 *
 * @author Alexander Schulze, Rolando Betancourt Toucet
 */
public class Settings {

	private Map<String, NativeAccess> mConnections = new FastMap<String, NativeAccess>();
	private static final String DEFAULT = "default";

	/**
	 * @return the mConnections
	 */
	public Map<String, NativeAccess> getConnections() {
		return mConnections;
	}

	/**
	 * @param aConnections
	 */
	public void setConnections(Map<String, NativeAccess> aConnections) {
		mConnections = aConnections;
	}

	/**
	 *
	 * @param aAlias
	 * @return
	 */
	public NativeAccess getNativeAccess(String aAlias) {
		if (aAlias == null) {
			return mConnections.get(DEFAULT);
		} else {
			if (mConnections.containsKey(aAlias)) {
				return mConnections.get(aAlias);
			} else {
				return null;
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public NativeAccess getNativeAccess() {
		return getNativeAccess(null);
	}
}
