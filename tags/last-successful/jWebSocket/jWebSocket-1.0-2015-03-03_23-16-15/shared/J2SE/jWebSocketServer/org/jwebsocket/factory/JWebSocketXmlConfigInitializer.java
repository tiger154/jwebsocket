//	---------------------------------------------------------------------------
//	jWebSocket - JWebSocketXmlConfigInitializer (Community Edition, CE)
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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.*;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.*;
import org.jwebsocket.logging.Logging;

/**
 * Initialize the engine, servers and plug-ins based on jWebSocket.xml
 * configuration
 *
 * @author puran
 * @version $Id: JWebSocketXmlConfigInitializer.java 424 2010-05-01 19:11:04Z
 * mailtopuran $
 */
public class JWebSocketXmlConfigInitializer extends AbstractJWebSocketInitializer {

	private static final Logger mLog = Logging.getLogger();
//	private final static JWebSocketJarClassLoader mClassLoader = new JWebSocketJarClassLoader();
	private static JWebSocketJarClassLoader mClassLoader = new JWebSocketJarClassLoader();

	/**
	 * private constructor
	 *
	 * @param aConfig
	 */
	public JWebSocketXmlConfigInitializer(JWebSocketConfig aConfig) {
		super(aConfig);

		// Saving the initializer class loader reference
		if (null == JWebSocketFactory.getClassLoader()) {
			JWebSocketFactory.setClassLoader(mClassLoader);
		} else {
			mClassLoader = JWebSocketFactory.getClassLoader();
		}
	}

	/**
	 *
	 * @return
	 */
	public static JWebSocketJarClassLoader getClassLoader() {
		return mClassLoader;
	}

	/**
	 * Returns the initializer object
	 *
	 * @param aConfig the jWebSocket config
	 * @return the initializer object
	 */
	public static JWebSocketXmlConfigInitializer getInitializer(JWebSocketConfig aConfig) {
		JWebSocketXmlConfigInitializer lInitializer = new JWebSocketXmlConfigInitializer(aConfig);
		return lInitializer;
	}

	private void loadLibrary(String aPath) {
		try {
			String lPath = JWebSocketConfig.expandEnvVarsAndProps(aPath);
			mClassLoader.addFile(lPath);
			ClassPathUpdater.add(new File(lPath));

			// URLClassLoader lURLCL = (URLClassLoader) Thread.currentThread().getContextClassLoader();
			URLClassLoader lURLCL = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Method lMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
			lMethod.setAccessible(true);
			String lURLStr = "file:" + lPath;
			URL lURL = new URL(lURLStr);
			lMethod.invoke(lURLCL, new Object[]{lURL});
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "loading external library"));
		}
	}

	@Override
	public ClassLoader initializeLibraries() {
		List<LibraryConfig> lLibs = jWebSocketConfig.getLibraries();
		if (lLibs != null) {

			for (LibraryConfig lLibConf : lLibs) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Adding external library '" + lLibConf.getId()
							+ "' from '" + lLibConf.getURL() + "'...");
				}

				loadLibrary(lLibConf.getURL());

				if (mLog.isInfoEnabled()) {
					mLog.info("External library '" + lLibConf.getId()
							+ "' from '" + lLibConf.getURL()
							+ "' successfully added.");
				}
			}

		} else {
			if (mLog.isDebugEnabled()) {
				mLog.debug("No external libraries referenced in config file.");
			}
		}
		return mClassLoader.getClassLoader();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, WebSocketEngine> initializeEngines() {
		Map<String, WebSocketEngine> lEngines = new FastMap<String, WebSocketEngine>();
		for (EngineConfig lEngineConfig : jWebSocketConfig.getEngines()) {
			// EngineConfig lEngineConfig = jWebSocketConfig.getEngines().get(0);
			String lJarFilePath;
			try {
				// try to load engine from classpath first,could be located in server bundle
				Class<WebSocketEngine> lEngineClass = loadEngineFromClassPath(lEngineConfig.getName());
				// in case of a class not found exception we DO NOT want to show the
				// exception but subsequently load the class from the jar file
				if (lEngineClass == null) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Loading from the jar file '" + lEngineConfig.getName() + "'  ");
					}
					lJarFilePath = JWebSocketConfig.getLibsFolder(lEngineConfig.getJar(),
							Thread.currentThread().getContextClassLoader());
					// jarFilePath may be null if .jar is included in server bundle
					if (lJarFilePath != null) {
						mClassLoader.addFile(lJarFilePath);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Loading engine '" + lEngineConfig.getName() + "' from '" + lJarFilePath + "'...");
						}
						lEngineClass = (Class<WebSocketEngine>) mClassLoader.loadClass(lEngineConfig.getName());
					}
				}
				// if class found try to create an instance
				if (lEngineClass != null) {
					Constructor<WebSocketEngine> lConstructor
							= lEngineClass.getDeclaredConstructor(EngineConfiguration.class);
					WebSocketEngine lEngine;
					if (lConstructor != null) {
						lConstructor.setAccessible(true);
						lEngine = lConstructor.newInstance(new Object[]{lEngineConfig});
					} else {
						lEngine = lEngineClass.newInstance();
						lEngine.setEngineConfiguration(lEngineConfig);
					}
					lEngines.put(lEngine.getId(), lEngine);
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
		}
		return lEngines;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<WebSocketServer> initializeServers() {
		List<WebSocketServer> lServers = new FastList<WebSocketServer>();
		List<ServerConfig> lServerConfigs = jWebSocketConfig.getServers();
		for (ServerConfig lServerConfig : lServerConfigs) {
			WebSocketServer lServer;
			String lJarFilePath;
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
					Constructor<WebSocketServer> lConstructor
							= lServerClass.getDeclaredConstructor(ServerConfiguration.class);
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
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketPlugIn>> initializePlugins() {
		Map<String, List<WebSocketPlugIn>> lPlugInMap
				= new FastMap<String, List<WebSocketPlugIn>>();
		// populate the plugin FastMap with server id and empty list
		for (ServerConfig lServerConfig : jWebSocketConfig.getServers()) {
			lPlugInMap.put(lServerConfig.getId(), new FastList<WebSocketPlugIn>());
		}
		// now initialize the plugins
		for (PluginConfig lPlugInConfig : jWebSocketConfig.getPlugins()) {
			String lPlugInLogStr;
			try {
				lPlugInLogStr = lPlugInConfig.getNamespace();
				if (!lPlugInConfig.getJars().isEmpty()) {
					if (mLog.isDebugEnabled()) {
						mLog.info("Loading plug-in jars: " + lPlugInConfig.getJars());
					}
					for (String lPath : lPlugInConfig.getJars()) {
						loadLibrary(lPath);
					}
				}

				Class<WebSocketPlugIn> lPlugInClass
						= loadPluginFromClasspath(lPlugInConfig.getName());

				// if not in classpath..try to load plug-in from given .jar file
				if (lPlugInClass == null && null != lPlugInConfig.getJar() && lPlugInConfig.getJar().length() > 0) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Plug-in '" + lPlugInConfig.getName()
								+ "' trying to load from file...");
					}
					String lJarFilePath = JWebSocketConfig.getLibsFolder(
							lPlugInConfig.getJar(),
							Thread.currentThread().getContextClassLoader());
					// jarFilePath may be null if .jar is included in server bundle
					if (lJarFilePath != null) {
						mClassLoader.addFile(lJarFilePath);
						// ClassPathUpdater.add(new File(lJarFilePath));
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
					WebSocketPlugIn lPlugIn;

					Constructor<WebSocketPlugIn> lPlugInConstructor;
					lPlugInConstructor
							= lPlugInClass.getConstructor(PluginConfiguration.class);
					if (lPlugInConstructor != null) {
						lPlugInConstructor.setAccessible(true);
						lPlugIn = lPlugInConstructor.newInstance(lPlugInConfig);

						if (mLog.isInfoEnabled()) {
							mLog.info("Plug-in '"
									+ lPlugInConfig.getId()
									+ "' successfully instantiated, "
									+ lPlugIn.getLabel()
									+ " " + lPlugIn.getVersion()
									+ ", " + lPlugIn.getCopyright()
									+ ", " + lPlugIn.getLicense());
						}
						// now add the plugin to plugin map based on server ids
						for (String lServerId : lPlugInConfig.getServers()) {
							List<WebSocketPlugIn> lPlugIns = lPlugInMap.get(lServerId);
							if (lPlugIns != null
									&& lPlugIn.getPluginConfiguration().getServers().contains(lServerId)) {
								lPlugIns.add(lPlugIn);
							}
						}
					} else {
						mLog.error("Plug-in '" + lPlugInConfig.getId()
								+ "' could not be instantiated due to invalid constructor.");
					}
				} else {
					mLog.error("Plug-in '" + lPlugInConfig.getId()
							+ "' could not be instantiated"
							+ ", check class name '" 
							+ lPlugInConfig.getName() + "'.");
				}

			} catch (Exception lEx) {
				mLog.error("Could not instantiate '" + lPlugInConfig.getId() + "' plug-in: "
						+ "[" + lEx.getClass().getSimpleName() + "] " + lEx.getMessage());
			}
		}
		return lPlugInMap;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketFilter>> initializeFilters() {
		Map<String, List<WebSocketFilter>> lFilterMap
				= new FastMap<String, List<WebSocketFilter>>();

		// populate the filter FastMap with server id and empty list
		for (ServerConfig lServerConfig : jWebSocketConfig.getServers()) {
			lFilterMap.put(lServerConfig.getId(), new FastList<WebSocketFilter>());
		}
		// now initialize the filter
		for (FilterConfig lFilterConfig : jWebSocketConfig.getFilters()) {
			try {
				// try to load filter from classpath first, could be located in server bundle
				Class<WebSocketFilter> lFilterClass
						= loadFilterFromClasspath(lFilterConfig.getName());
				if (lFilterClass == null) {
					String lJarFilePath
							= JWebSocketConfig.getLibsFolder(lFilterConfig.getJar());
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
					Constructor<WebSocketFilter> lConstr
							= lFilterClass.getDeclaredConstructor(FilterConfiguration.class);
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
				mLog.error("Could not instantiate '" + lFilterConfig.getId() + "' filter: "
						+ "[" + lEx.getCause().getClass().getSimpleName() + "]" + lEx.getCause().getMessage());
			}
		}
		return lFilterMap;
	}
}
