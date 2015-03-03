//	---------------------------------------------------------------------------
//	jWebSocket - Factory Singleton (Community Edition, CE)
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

import java.security.Security;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.jwebsocket.api.*;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.storage.ehcache.EhCacheManager;
import org.jwebsocket.util.Tools;
import org.springframework.beans.factory.BeanFactory;

/**
 * Factory to initialize and start the jWebSocket components
 *
 * @author Alexander Schulze
 * @version $Id:$
 */
public class JWebSocketFactory {

	// don't instantiate logger here! first read args!
	private static Logger mLog = null;
	private static Map<String, WebSocketEngine> mEngines = null;
	private static List<WebSocketServer> mServers = null;
	private static TokenServer mTokenServer = null;
	private static BeanFactory mBeanFactory;
	private static JWebSocketJarClassLoader mClassLoader = null;

	/**
	 *
	 * @return The class loader used to load the system resources like libraries, engines, plug-ins,
	 * ...
	 */
	public static JWebSocketJarClassLoader getClassLoader() {
		return mClassLoader;
	}

	/**
	 *
	 *
	 * @param aClassLoader
	 */
	public static void setClassLoader(JWebSocketJarClassLoader aClassLoader) {
		JWebSocketFactory.mClassLoader = aClassLoader;
	}

	/**
	 *
	 */
	public static void printCopyrightToConsole() {
		// the following 3 lines must not be removed due to Apache 2.0 license!
		System.out.println("jWebSocket Ver. "
				+ JWebSocketServerConstants.VERSION_STR
				+ " (Java " + System.getProperty("java.version") + " " + System.getProperty("sun.arch.data.model") + "bit)");
		System.out.println(JWebSocketCommonConstants.COPYRIGHT_CE);
		System.out.println(JWebSocketCommonConstants.LICENSE_CE);
	}

	/**
	 *
	 */
	public static void start() {
		start(null, null);
	}

	private static void securePackages() {
		// securing core packages 
		Security.setProperty("package.access",
				"org.jwebsocket.console,"
				+ "org.jwebsocket.security,"
				+ "org.jwebsocket.factory,"
				+ "org.jwebsocket.config,"
				+ "org.jwebsocket.spring,"
				+ "org.jwebsocket.instance,"
				+ "java.security");
	}

	/**
	 *
	 * @param aConfigPath
	 * @param aBootstrapPath
	 */
	public static void start(String aConfigPath, String aBootstrapPath) {
		securePackages();

		mLog = Logging.getLogger();
		if (null == aConfigPath) {
			aConfigPath = JWebSocketConfig.getConfigPath();
		}
		if (null == aBootstrapPath) {
			aBootstrapPath = JWebSocketConfig.getBootstrapPath();
		}

		boolean lDebug = true;
		if (lDebug) {
			Logger lLogger = Logger.getRootLogger();
			String lAppenderStr = "";
			Enumeration lAppenders = lLogger.getAllAppenders();
			while (lAppenders.hasMoreElements()) {
				Appender lAppender = (Appender) lAppenders.nextElement();
				lAppenderStr += " " + lAppender.getName();
			}
			if (lAppenderStr.length() <= 0) {
				lAppenderStr = " none detected";
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting instance, logger(s):"
						+ lAppenderStr
						+ ", ConfigPath: " + aConfigPath
						+ ", BootstrapPath: " + aBootstrapPath);
			}
		}

		JWebSocketInstance.setStatus(JWebSocketInstance.STARTING);

		// loading jwebsocket server policies if running out of a Web App
		if (!JWebSocketConfig.isWebApp()) {
			// registering shutdownHook
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
						JWebSocketFactory.stop();
					}
				}
			}));

			// loading policies
			String lPolicyFile = JWebSocketConfig.getConfigFolder("jWebSocket.policy");
			if (null != lPolicyFile) {
				try {
					System.setProperty("java.security.policy", lPolicyFile);
					System.setSecurityManager(new SecurityManager());

					if (mLog.isInfoEnabled()) {
						mLog.info("jWebSocket server security policies successfully loaded.");
					}
				} catch (Exception lEx) {
					mLog.error("Error loading jWebSocket server security policies...", lEx);
					return;
				}
			} else {
				if (mLog.isInfoEnabled()) {
					mLog.warn("jWebSocket server policy not located at $JWEBSOCKET_HOME/conf/jWebSocket.policy! "
							+ "Running without security restrictions...");
				}
			}
		}
		// start the shared utility timer
		Tools.startUtilityTimer();

		JWebSocketLoader lLoader = new JWebSocketLoader();

		// try to load bean from bootstrap
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Loading bootstrap '" + aBootstrapPath + "'...");
			}
			JWebSocketBeanFactory.load(aBootstrapPath, Thread.currentThread().getContextClassLoader());
			if (mLog.isDebugEnabled()) {
				mLog.debug("Bootstrap '" + aBootstrapPath + "' successfully loaded.");
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "loading bootstrap."));
		}

		// try to load configuration from .xml file
		try {
			WebSocketInitializer lInitializer
					= lLoader.initialize(aConfigPath);

			if (lInitializer == null) {
				JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
				return;
			}

			lInitializer.initializeLogging();

			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting jWebSocket Server Sub System...");
			}

			// load and init all external libraries
			ClassLoader lClassLoader = lInitializer.initializeLibraries();
			if (lClassLoader != null) {
				JWebSocketConfig.setClassLoader(lClassLoader);
			}
			mEngines = lInitializer.initializeEngines();

			if (null == mEngines || mEngines.size() <= 0) {
				// the loader already logs an error!
				JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
				return;
			}

			// initialize and start the server
			if (mLog.isDebugEnabled()) {
				mLog.debug("Initializing servers...");
			}
			mServers = lInitializer.initializeServers();

			if (mLog.isDebugEnabled()) {
				mLog.debug("Initializing plugins...");
			}
			Map<String, List<WebSocketPlugIn>> lPluginMap
					= lInitializer.initializePlugins();

			for (WebSocketServer lServer : mServers) {
				for (WebSocketEngine lEngine : mEngines.values()) {
					lServer.addEngine(lEngine);
				}
				List<WebSocketPlugIn> lPlugIns = lPluginMap.get(lServer.getId());
				for (WebSocketPlugIn lPlugIn : lPlugIns) {
					lServer.getPlugInChain().addPlugIn(lPlugIn);
				}
				if (mLog.isInfoEnabled()) {
					mLog.info(lPlugIns.size()
							+ " plugin(s) initialized for server '"
							+ lServer.getId() + "'.");
				}
			}
			Map<String, List<WebSocketFilter>> lFilterMap
					= lInitializer.initializeFilters();

			if (mLog.isDebugEnabled()) {
				mLog.debug("Initializing filters...");
			}

			for (WebSocketServer lServer : mServers) {
				// lServer.addEngine(mEngine);
				List<WebSocketFilter> lFilters = lFilterMap.get(lServer.getId());
				for (WebSocketFilter lFilter : lFilters) {
					lServer.getFilterChain().addFilter(lFilter);
				}
				if (mLog.isInfoEnabled()) {
					mLog.info(lFilters.size()
							+ " filter(s) initialized for server '"
							+ lServer.getId() + "'.");
				}
			}

			notifyStarting();

			boolean lEngineStarted = false;
			// first start the engines
			for (WebSocketEngine lEngine : mEngines.values()) {
				if (mLog.isDebugEnabled()) {
					String lEnginesStr = "";
					lEnginesStr += lEngine.getId() + ", ";
					if (lEnginesStr.length() > 0) {
						lEnginesStr = lEnginesStr.substring(0, lEnginesStr.length() - 2);
					}
					mLog.debug("Starting engine(s) '" + lEnginesStr + "'...");
				}

				try {
					lEngine.startEngine();
					lEngineStarted = true;
				} catch (WebSocketException lEx) {
					mLog.error("Starting engine '" + lEngine.getId()
							+ "' failed (" + lEx.getClass().getSimpleName() + ": "
							+ lEx.getMessage() + ").");
				}
			}

			// do not start any servers no engine could be started
			if (lEngineStarted) {
				// now start the servers
				if (mLog.isDebugEnabled()) {
					mLog.debug("Starting servers...");
				}
				for (WebSocketServer lServer : mServers) {
					try {
						lServer.startServer();
					} catch (WebSocketException lEx) {
						mLog.error("Starting server '" + lServer.getId()
								+ "' failed (" + lEx.getClass().getSimpleName()
								+ ": " + lEx.getMessage() + ").");
					}
				}

				if (mLog.isInfoEnabled()) {
					mLog.info("jWebSocket server startup complete");
				}

				// if everything went fine...
				JWebSocketInstance.setStatus(JWebSocketInstance.STARTED);

				notifyStarted();
			} else {
				notifyStopping();

				// if engine couldn't be started due to whatever reasons...
				JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);

				notifyStopped();

				throw new RuntimeException("None engine was able to start!");
			}
		} catch (WebSocketException lEx) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Exception during startup", lEx);
			}
			if (mLog != null && mLog.isInfoEnabled()) {
				mLog.info("jWebSocketServer failed to start.");
			}
			JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
			throw new RuntimeException(lEx);
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("SleepWhileInLoop")
	public static void run() {
		// remain here until shut down request
		while (JWebSocketInstance.getStatus() != JWebSocketInstance.SHUTTING_DOWN) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException lEx) {
				// no handling required here
			}
		}

	}

	/**
	 *
	 */
	public static void stop() {
		// set instance status to not accept any new incoming connections
		JWebSocketInstance.setStatus(JWebSocketInstance.STOPPING);
		notifyStopping();

		if (mLog != null && mLog.isDebugEnabled()) {
			mLog.debug("Stopping jWebSocket Sub System...");
		}

		if (null != mEngines) {
			for (WebSocketEngine lEngine : mEngines.values()) {
				// stop engine if previously started successfully
				if (lEngine != null) {
					if (mLog != null && mLog.isDebugEnabled()) {
						mLog.debug("Stopping engine...");
					}
					try {
						lEngine.stopEngine(CloseReason.SHUTDOWN);
						if (mLog != null && mLog.isInfoEnabled()) {
							mLog.info("jWebSocket engine '" + lEngine.getId() + "' stopped.");
						}
					} catch (WebSocketException lEx) {
						if (mLog != null) {
							mLog.error("Stopping engine '" + lEngine.getId() + "': " + lEx.getMessage());
						}
					}
				}
			}
		}

		if (null != mServers) {
			// now stop the servers
			if (mLog != null && mLog.isDebugEnabled()) {
				mLog.debug("Stopping servers...");
			}
			for (WebSocketServer lServer : mServers) {
				try {
					lServer.stopServer();
					if (mLog != null && mLog.isInfoEnabled()) {
						mLog.info("jWebSocket server '" + lServer.getId() + "' stopped.");
					}
				} catch (WebSocketException lEx) {
					if (mLog != null) {
						mLog.error("Stopping server: " + lEx.getMessage());
					}
				}
			}
		}

		// destroy (Spring) bean factories
		JWebSocketBeanFactory.destroy();
		if (null != mLog && mLog.isInfoEnabled()) {
			mLog.info("jWebSocket Server bean factories stopped.");
		}

		// stopping EhCache manager
		EhCacheManager.shutdown();
		if (null != mLog && mLog.isInfoEnabled()) {
			mLog.info("jWebSocket Server EhCacheManager stopped.");
		}

		// stop the shared utility timer
		Tools.stopUtilityTimer();
		if (null != mLog && mLog.isInfoEnabled()) {
			mLog.info("jWebSocket Server Timer instance stopped.");
		}

		// stop the shared utility thread pool
		Tools.stopUtilityThreadPool();
		if (null != mLog && mLog.isInfoEnabled()) {
			mLog.info("jWebSocket Server ThreadPool instance stopped.");
		}

		// set instance status
		JWebSocketInstance.setStatus(JWebSocketInstance.STOPPED);

		notifyStopped();
		if (null != mLog && mLog.isInfoEnabled()) {
			mLog.info("jWebSocket Server notified stopped event.");
		}

		try {
			// disconnecting opened derby databases (if exists)
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException lEx) {
		}

		if (null != mLog && mLog.isInfoEnabled()) {
			mLog.info("jWebSocket Server successfully stopped.");
		}

		Logging.exitLogs();
	}

	/**
	 *
	 * @return
	 */
	public static BeanFactory getBeans() {
		return mBeanFactory;
	}

	/**
	 *
	 * @param aCoreBeans
	 */
	public static void setBeans(BeanFactory aCoreBeans) {
		mBeanFactory = aCoreBeans;
	}

	/**
	 *
	 * @return
	 */
	public static Map<String, WebSocketEngine> getEngines() {
		return mEngines;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public static WebSocketEngine getEngine(String aId) {
		return mEngines.get(aId);
	}

	/**
	 *
	 * @return
	 */
	public static WebSocketEngine getEngine() {
		return mEngines.values().iterator().next();
	}

	/**
	 *
	 * @return
	 */
	public static List<WebSocketServer> getServers() {
		return mServers;
	}

	/**
	 * Returns the server identified by it's id or <tt>null</tt> if no server with that id could be
	 * found in the factory.
	 *
	 * @param aId id of the server to be returned.
	 * @return WebSocketServer with the given id or <tt>null</tt> if not found.
	 */
	public static WebSocketServer getServer(String aId) {
		if (aId != null && mServers != null) {
			for (WebSocketServer lServer : mServers) {
				if (lServer != null && aId.equals(lServer.getId())) {
					return lServer;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	public static TokenServer getTokenServer() {
		if (mTokenServer == null) {
			for (WebSocketServer lServer : mServers) {
				if (lServer instanceof TokenServer) {
					mTokenServer = (TokenServer) lServer;
					break;
				}
			}
		}

		return mTokenServer;
	}

	private static void notifyStarted() {
		for (WebSocketEngine lEngine : mEngines.values()) {
			try {
				lEngine.systemStarted();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStarted' event at '" + lEngine.getId()
						+ "' engine failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}

		for (WebSocketServer lServer : mServers) {
			try {
				lServer.systemStarted();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStarted' event at '" + lServer.getId()
						+ "' server failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}
	}

	private static void notifyStarting() {
		for (WebSocketEngine lEngine : mEngines.values()) {
			try {
				lEngine.systemStarting();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStarting' event at '" + lEngine.getId()
						+ "' engine failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}

		for (WebSocketServer lServer : mServers) {
			try {
				lServer.systemStarting();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStarting' event at '" + lServer.getId()
						+ "' server failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}
	}

	private static void notifyStopping() {
		for (WebSocketEngine lEngine : mEngines.values()) {
			try {
				lEngine.systemStopping();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStopping' event at '" + lEngine.getId()
						+ "' engine failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}

		for (WebSocketServer lServer : mServers) {
			try {
				lServer.systemStopping();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStopping' event at '" + lServer.getId()
						+ "' server failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}
	}

	private static void notifyStopped() {
		for (WebSocketEngine lEngine : mEngines.values()) {
			try {
				lEngine.systemStopped();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStopped' event at '" + lEngine.getId()
						+ "' engine failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}

		for (WebSocketServer lServer : mServers) {
			try {
				lServer.systemStopped();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStopped' event at '" + lServer.getId()
						+ "' server failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}
		}
	}
}
