//	---------------------------------------------------------------------------
//	jWebSocket - FilterConfiguration (Community Edition, CE)
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
package org.jwebsocket.api;

import java.util.List;
import java.util.Map;

/**
 * Base interface for filter configuration
 *
 * @author Alexander Schulze, Puran Singh
 */
public interface FilterConfiguration extends Configuration {

	/**
	 *
	 * @return
	 */
	String getJar();

	/**
	 *
	 * @return
	 */
	String getPackageName();

	/**
	 *
	 * @return
	 */
	String getNamespace();

	/**
	 *
	 * @return
	 */
	List<String> getServers();

	/**
	 *
	 * @return
	 */
	Map<String, String> getSettings();

	/**
	 * returns the enabled status of the plug-in.
	 *
	 * @return
	 */
	boolean getEnabled();
}
