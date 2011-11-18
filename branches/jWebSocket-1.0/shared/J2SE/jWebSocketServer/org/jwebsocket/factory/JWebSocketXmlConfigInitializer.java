//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.factory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.config.xml.LibraryConfig;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.config.xml.ServerConfig;

/**
 * Intialize the engine, servers and plugins based on jWebSocket.xml
 * configuration
 * @author puran
 * @version $Id: JWebSocketXmlConfigInitializer.java 424 2010-05-01 19:11:04Z mailtopuran $
 */
public class JWebSocketXmlConfigInitializer extends AbstractJWebSocketInitializer {

	private final JWebSocketJarClassLoader mClassLoader = new JWebSocketJarClassLoader();

	/**
	 * private constructor
	 */
	public JWebSocketXmlConfigInitializer(JWebSocketConfig aConfig) {
		super(aConfig);
	}

	/**
	 * Returns the initializer object
	 *
	 * @param aConfig the jWebSocket config
	 * @return the initializer object
	 */
	public static JWebSocketXmlConfigInitializer getInitializer(JWebSocketConfig aConfig) {
		return new JWebSocketXmlConfigInitializer(aConfig);
	}

	@Override
	public URLClassLoader initializeLibraries() {
		List<LibraryConfig> lLibs = jWebSocketConfig.getLibraries();
		JWebSocketJarClassLoader lJarLoader = null;
		if (lLibs != null) {
			// lJarLoader = new JWebSocketJarClassLoader();
			try {
				for (LibraryConfig lLibConf : lLibs) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Adding external library '" + lLibConf.getId()
								+ "' from '" + lLibConf.getURL() + "'...");
					}
					// lJarLoader.addFile(lLibConf.getURL());
					ClassPathUpdater.add(new File(lLibConf.getURL()));
				}
			} catch (Exception ex) {
				System.out.println(ex.toString());
			}
		} else {
			if (mLog.isDebugEnabled()) {
				mLog.debug("No external libraries referenced in config file.");
			}
		}
		return lJarLoader;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public WebSocketEngine initializeEngine() {
		WebSocketEngine lNewEngine = null;
		EngineConfig lEngineConfig = jWebSocketConfig.getEngines().get(0);
		String lJarFilePath = "-";
		try {
			// try to load engine from classpath first,could be located in server bundle
			Class<WebSocketEngine> lEngineClass = loadEngineFromClassPath(lEngineConfig.getName());
			// in case of a class not found exception we DO NOT want to show the
			// exception but subsequently load the class from the jar file
			if (lEngineClass == null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading from the jar file '" + lEngineConfig.getName() + "'  ");
				}
				lJarFilePath = JWebSocketConfig.getLibsFolder(lEngineConfig.getJar());
				// jarFilePath may be null if .jar is included in server bundle
				if (lJarFilePath != null) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Loading engine '" + lEngineConfig.getName() + "' from '" + lJarFilePath + "'...");
					}
					mClassLoader.addFile(lJarFilePath);
					lEngineClass = (Class<WebSocketEngine>) mClassLoader.loadClass(lEngineConfig.getName());
				}
			}
			// if class found try to create an instance
			if (lEngineClass != null) {
				Constructor<WebSocketEngine> lConstructor =
						lEngineClass.getDeclaredConstructor(EngineConfiguration.class);
				if (lConstructor != null) {
					lConstructor.setAccessible(true);
					lNewEngine = lConstructor.newInstance(new Object[]{lEngineConfig});
				} else {
					lNewEngine = lEngineClass.newInstance();
					lNewEngine.setEngineConfiguration(lEngineConfig);
				}
				if (mLog.isDebugEnabled()) {
					mLog.debug("Engine '" + lEngineConfig.getId()
							+ "' successfully instantiated.");
				}
			} else {
				mLog.error("jWebSocket engine class "
						+ lEngineConfig.getName() + " could not be loaded.");
			}
		} catch (Exception lEx) {
			mLog.error("Error initializing engine based on given configuration. "
					+ "Make sure that you are using correct jar file or "
					+ "engine class is in the classpath", lEx);
		}
		return lNewEngine;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<WebSocketServer> initializeServers() {
		List<WebSocketServer> lServers = new FastList<WebSocketServer>();
		List<ServerConfig> lServerConfigs = jWebSocketConfig.getServers();
		for (ServerConfig lServerConfig : lServerConfigs) {
			WebSocketServer lServer = null;
			String lJarFilePath = "-";
			try {
				Class<WebSocketServer> lServerClass = loadServerFromClasspath(lServerConfig.getName());
				// if not in classpath...try to load server from given .jar file
				if (lServerClass == null) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Load server from the jar file '" + lServerConfig.getName());
					}
					lJarFilePath = JWebSocketConfig.getLibsFolder(lServerConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (lJarFilePath != null) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Loading server '" + lServerConfig.getName() + "' from '" + lJarFilePath + "'...");
						}
						mClassLoader.addFile(lJarFilePath);
						lServerClass = (Class<WebSocketServer>) mClassLoader.loadClass(lServerConfig.getName());
					}
				}
				// if class found try to create an instance
				if (lServerClass != null) {
					Constructor<WebSocketServer> lConstructor =
							lServerClass.getDeclaredConstructor(ServerConfiguration.class);
					if (lConstructor != null) {
						lConstructor.setAccessible(true);
						lServer = lConstructor.newInstance(new Object[]{lServerConfig});
					} else {
						lServer = lServerClass.newInstance();
						lServer.setServerConfiguration(lServerConfig);
					}
					if (mLog.isDebugEnabled()) {
						mLog.debug("Server '" + lServerConfig.getId()
								+ "' successfully instantiated.");
					}
					// add the initialized server to the list
					lServers.add(lServer);
				} else {
					mLog.error("jWebSocket server class "
							+ lServerConfig.getName() + " could not be loaded.");
				}
			} catch (Exception lEx) {
				mLog.error("Error initializing server based on given configuration. Make sure that you are using correct jar file or "
						+ "server class is in the classpath", lEx);
			}
		}
		return lServers;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketPlugIn>> initializePlugins() {
		Map<String, List<WebSocketPlugIn>> lPlugInMap =
				new FastMap<String, List<WebSocketPlugIn>>();
		// populate the plugin FastMap with server id and empty list
		for (ServerConfig lServerConfig : jWebSocketConfig.getServers()) {
			lPlugInMap.put(lServerConfig.getId(), new FastList<WebSocketPlugIn>());
		}
		// now initialize the plugins
		for (PluginConfig lPlugInConfig : jWebSocketConfig.getPlugins()) {
			try {
				Class<WebSocketPlugIn> lPlugInClass =
						loadPluginFromClasspath(lPlugInConfig.getName());
				// if not in classpath..try to load plug-in from given .jar file
				if (lPlugInClass == null) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Plug-in '" + lPlugInConfig.getName()
								+ "' trying to load from file...");
					}
					String lJarFilePath = JWebSocketConfig.getLibsFolder(lPlugInConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (lJarFilePath != null) {
						mClassLoader.addFile(lJarFilePath);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Loading plug-in '"
									+ lPlugInConfig.getName()
									+ "' from '" + lJarFilePath + "'...");
						}
						lPlugInClass = (Class<WebSocketPlugIn>) mClassLoader.loadClass(lPlugInConfig.getName());
					}
				}
				// if class found try to create an instance
				if (lPlugInClass != null) {
					WebSocketPlugIn lPlugIn = null;

					Constructor<WebSocketPlugIn> lPlugInConstructor = null;
					lPlugInConstructor =
							lPlugInClass.getConstructor(PluginConfiguration.class);
					if (lPlugInConstructor != null) {
						lPlugInConstructor.setAccessible(true);
						lPlugIn = lPlugInConstructor.newInstance(lPlugInConfig);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Plug-in '" + lPlugInConfig.getId()
									+ "' successfully instantiated.");
						}
						// now add the plugin to plugin map based on server ids
						for (String lServerId : lPlugInConfig.getServers()) {
							List<WebSocketPlugIn> lPlugIns = lPlugInMap.get(lServerId);
							if (lPlugIns != null) {
								lPlugIns.add((WebSocketPlugIn) lPlugIn);
							}
						}
					} else {
						mLog.error("Plug-in '" + lPlugInConfig.getId()
								+ "' could not be instantiated due to invalid constructor.");
					}
				}

			} catch (Exception lEx) {
				mLog.error("Couldn't instantiate the plug-in.", lEx);
			}
		}
		return lPlugInMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketFilter>> initializeFilters() {
		Map<String, List<WebSocketFilter>> lFilterMap =
				new FastMap<String, List<WebSocketFilter>>();

		// populate the filter FastMap with server id and empty list
		for (ServerConfig lServerConfig : jWebSocketConfig.getServers()) {
			lFilterMap.put(lServerConfig.getId(), new FastList<WebSocketFilter>());
		}
		// now initialize the filter
		for (FilterConfig lFilterConfig : jWebSocketConfig.getFilters()) {
			try {
				Class<WebSocketFilter> lFilterClass =
						loadFilterFromClasspath(lFilterConfig.getName());
				// try to load filter from classpath first, could be located in server bundle
				lFilterClass = loadFilterFromClasspath(lFilterConfig.getName());
				if (lFilterClass == null) {
					String lJarFilePath =
							JWebSocketConfig.getLibsFolder(lFilterConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (lJarFilePath != null) {
						mClassLoader.addFile(lJarFilePath);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Loading filter '"
									+ lFilterConfig.getName()
									+ "' from '" + lJarFilePath + "'...");
						}
						lFilterClass = (Class<WebSocketFilter>) mClassLoader.loadClass(lFilterConfig.getName());
					}
				}
				if (lFilterClass != null) {
					Constructor<WebSocketFilter> lConstr =
							lFilterClass.getDeclaredConstructor(FilterConfiguration.class);
					lConstr.setAccessible(true);
					WebSocketFilter lFilter = lConstr.newInstance(new Object[]{lFilterConfig});
					if (mLog.isDebugEnabled()) {
						mLog.debug("Filter '" + lFilterConfig.getName()
								+ "' successfully instantiated.");
					}
					// now add the filter to filter FastMap based on server ids
					for (String lServerId : lFilterConfig.getServers()) {
						List<WebSocketFilter> lFilters = lFilterMap.get(lServerId);
						if (lFilters != null) {
							lFilters.add(lFilter);
						}
					}
				}

			} catch (Exception lEx) {
				mLog.error("Error instantiating filters", lEx);
			}
		}
		return lFilterMap;
	}
}
