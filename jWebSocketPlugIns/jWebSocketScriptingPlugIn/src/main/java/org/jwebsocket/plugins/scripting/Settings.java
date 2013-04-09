//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting;

import java.util.Map;
import javolution.util.FastMap;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class Settings {

	private Map<String, String> mJavaScript = new FastMap<String, String>().shared();
	private Map<String, String> mApps = new FastMap<String, String>().shared();

	public Map<String, String> getApps() {
		return mApps;
	}

	public void setApps(Map<String, String> aMaps) {
		mApps.putAll(aMaps);
	}

	/**
	 * @return the aliases
	 */
	public Map getJavascript() {
		return mJavaScript;
	}

	/**
	 * @param aJavaScripts
	 */
	public void setJavascript(Map aJavaScripts) {
		mJavaScript.putAll(aJavaScripts);
	}
}
