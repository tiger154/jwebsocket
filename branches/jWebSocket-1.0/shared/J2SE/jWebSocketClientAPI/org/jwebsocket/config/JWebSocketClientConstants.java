//  ---------------------------------------------------------------------------
//  jWebSocket - JWebSocketClientConstants (Community Edition, CE)
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
package org.jwebsocket.config;

/**
 * Provides a global shared container for the jWebSocket configuration settings.
 *
 * @author aschulze
 * @version $Id: JWebSocketServerConstants.java 624 2010-07-06 12:28:44Z
 * fivefeetfurther $
 */
public final class JWebSocketClientConstants {

	/**
	 * Current version string of the jWebSocket package.
	 */
	// (RC0 build 30228)
	public static final String VERSION_STR = "1.0 RC0 (build 30405)";
	/**
	 * Name space base for tokens and plug-ins.
	 */
	public static final String NS_BASE = "org.jwebsocket";
	/**
	 * Name space for the system plug-in and core jWebSocket operations.
	 */
	public static final String NS_SYSTEM = NS_BASE + ".plugins.system";
	/**
	 * Name space for the channels plug-in.
	 */
	public static final String NS_CHANNELS = NS_BASE + ".plugins.channels";
	/**
	 * Name space for the file-system plug-in.
	 */
	public static final String NS_FILESYSTEM = NS_BASE + ".plugins.filesystem";
}
