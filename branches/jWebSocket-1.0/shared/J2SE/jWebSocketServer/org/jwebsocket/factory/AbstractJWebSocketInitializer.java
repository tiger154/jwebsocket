//	---------------------------------------------------------------------------
//	jWebSocket - AbstractJWebSocketInitializer (Community Edition, CE)
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
package org.jwebsocket.factory;

import org.apache.log4j.Logger;
import org.jwebsocket.api.*;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.LoggingConfig;
import org.jwebsocket.logging.Logging;

/**
 * Abstract initializer class that performs the initialization
 *
 * @author puran
 * @version $Id: AbstractJWebSocketInitializer.java 437 2010-05-03 22:10:20Z
 * mailtopuran $
 */
public abstract class AbstractJWebSocketInitializer implements WebSocketInitializer {

	private static Logger mLog = Logging.getLogger();
	/**
	 * the configuration object
	 */
	protected JWebSocketConfig jWebSocketConfig = null;

	/**
	 * @param aConfig the jwebsocket config object
	 */
	public AbstractJWebSocketInitializer(JWebSocketConfig aConfig) {
		this.jWebSocketConfig = aConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeLogging() {
		LoggingConfig lLoggingConfig = jWebSocketConfig.getLoggingConfig();
		// initialize log4j logging engine
		// BEFORE instantiating any jWebSocket classes
		Logging.initLogs(lLoggingConfig.getReloadDelay(), lLoggingConfig.getMaxLogTokenLength());
		mLog = Logging.getLogger(AbstractJWebSocketInitializer.class);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Logging settings: "
					+ "reload: " + lLoggingConfig.getReloadDelay());
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting jWebSocket Server Sub System...");
		}
	}

	/**
	 * Load the engine from the classpath
	 *
	 * @param aEngineName the name of the engine to load
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<WebSocketEngine> loadEngineFromClassPath(String aEngineName) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating engine...");
		}
		try {
			Class<WebSocketEngine> lEngineClass = (Class<WebSocketEngine>) Class.forName(aEngineName, true,
					JWebSocketFactory.getClassLoader().getClassLoader());

			if (mLog.isDebugEnabled()) {
				mLog.debug("Engine '" + aEngineName + "' loaded from classpath.");
			}
			return lEngineClass;
		} catch (ClassNotFoundException e) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Engine '" + aEngineName + "' not yet in classpath.");
			}
		}
		return null;
	}

	/**
	 *
	 * @param aServerName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<WebSocketServer> loadServerFromClasspath(String aServerName) {
		try {
			Class<WebSocketServer> lServerClass = (Class<WebSocketServer>) Class.forName(aServerName, true,
					JWebSocketFactory.getClassLoader().getClassLoader());

			if (mLog.isDebugEnabled()) {
				mLog.debug("Server '" + aServerName + "' loaded from classpath.");
			}
			return lServerClass;
		} catch (ClassNotFoundException ex) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Server '" + aServerName + "' not yet in classpath.");
			}
		}
		return null;
	}

	/**
	 *
	 * @param aClassName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<WebSocketPlugIn> loadPluginFromClasspath(String aClassName) {
		try {
			Class<WebSocketPlugIn> lPluginClass = (Class<WebSocketPlugIn>) Class.forName(aClassName, true,
					JWebSocketFactory.getClassLoader().getClassLoader());

			if (mLog.isDebugEnabled()) {
				mLog.debug("Class '" + aClassName + "' loaded from classpath.");
			}
			return lPluginClass;
		} catch (ClassNotFoundException ex) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Class '" + aClassName + "' not found in classpath.");
			}
		}
		return null;
	}

	/**
	 *
	 * @param aFilterName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<WebSocketFilter> loadFilterFromClasspath(String aFilterName) {
		try {
			Class<WebSocketFilter> lFilterClass = (Class<WebSocketFilter>) Class.forName(aFilterName, true,
					JWebSocketFactory.getClassLoader().getClassLoader());

			if (mLog.isDebugEnabled()) {
				mLog.debug("Filter '" + aFilterName + "' loaded from classpath.");
			}
			return lFilterClass;
		} catch (ClassNotFoundException ex) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Filter '" + aFilterName + "' not yet in classpath.");
			}
		}
		return null;
	}

	@Override
	public JWebSocketConfig getConfig() {
		return jWebSocketConfig;
	}
}
