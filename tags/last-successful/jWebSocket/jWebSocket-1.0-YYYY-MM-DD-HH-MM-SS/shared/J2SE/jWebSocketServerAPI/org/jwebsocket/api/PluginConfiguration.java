//	---------------------------------------------------------------------------
//	jWebSocket - PluginConfiguration (Community Edition, CE)
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
import org.json.JSONObject;

/**
 * The Base interface for plug-in configuration
 *
 * @author puran
 * @version $Id: PluginConfiguration.java 1840 2011-11-28 13:41:15Z
 * fivefeetfurther $
 */
public interface PluginConfiguration extends Configuration {

	/**
	 * @return the package
	 */
	String getPackage();

	/**
	 * @return the jar
	 */
	String getJar();

	/**
	 * @return the namespace
	 */
	String getNamespace();

	/**
	 * @return the list of servers
	 */
	List<String> getServers();

	/**
	 * @return the list of required jars
	 */
	List<String> getJars();

	/**
	 * @return the settings
	 */
	Map<String, Object> getSettings();

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	String getString(String aKey, String aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	String getString(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	JSONObject getJSON(String aKey, JSONObject aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	JSONObject getJSON(String aKey);

	/**
	 * returns the enabled status of the plug-in.
	 *
	 * @return
	 */
	boolean getEnabled();
}
