//	---------------------------------------------------------------------------
//	jWebSocket - JWebSocketConfig (Community Edition, CE)
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
package org.jwebsocket.config;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import static org.jwebsocket.config.JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
import static org.jwebsocket.config.JWebSocketServerConstants.DEFAULT_NODE_ID;
import org.jwebsocket.config.xml.*;
import org.jwebsocket.kit.WebSocketRuntimeException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * Represents the jWebSocket configuration. This class is immutable and should
 * not be overridden.
 *
 * @author Marcos Antonio Gonzalez Huerta
 * @author puran
 */
public class JWebSocketConfig implements Config {

	private static Logger mLog = null;
	private static String mWebAppHome;
	private final String mNodeId;
	private final String mProtocol;
	private final String mLibraryFolder;
	private final List<LibraryConfig> mLibraries;
	private final List<EngineConfig> mEngines;
	private final List<ServerConfig> mServers;
	private final List<UserConfig> mUsers;
	private final List<PluginConfig> mPlugins;
	private final List<FilterConfig> mFilters;
	private final LoggingConfig mLoggingConfig;
	private final List<RightConfig> mGlobalRights;
	private final List<RoleConfig> mGlobalRoles;
	private static JWebSocketConfig mConfig = null;
	private static ClassLoader mClassLoader = null;
	private static String mConfigPath = null;
	private static String mBootstrapPath = null;
	private static String mJWebSocketHome = null;
	private static boolean mIsWebApp = false;
	private static final Map<String, String> mProperties = new FastMap<String, String>().shared();

	/**
	 *
	 * @return
	 */
	public static Logger getLogger() {
		return mLog;
	}

	/**
	 * @return the mClassLoader
	 */
	public static ClassLoader getClassLoader() {
		return mClassLoader;
	}

	/**
	 *
	 * @param aClassLoader
	 */
	public static void setClassLoader(ClassLoader aClassLoader) {
		mClassLoader = aClassLoader;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		if (mProtocol == null || mProtocol.length() == 0) {
			return WS_SUBPROT_DEFAULT;
		}
		return mProtocol;
	}

	/**
	 * @return the node-id
	 */
	public String getNodeId() {
		if (mNodeId == null || mNodeId.length() == 0) {
			return DEFAULT_NODE_ID;
		}
		return mNodeId;
	}

	/**
	 * Get the system properties
	 *
	 * @return
	 */
	public static Map<String, String> getProperties() {
		return mProperties;
	}

	/**
	 *
	 * @return
	 */
	public static String getJWebSocketHome() {
		return mJWebSocketHome;
	}

	/**
	 *
	 * @return
	 */
	public static String getWebAppHome() {
		return mWebAppHome;
	}

	/**
	 * @return the jWebSocketHome (environment variable or command line option)
	 */
	public static String findJWebSocketHome() {
		// check Java property first
		// check if instance mJWebSocketHome is still null (not yet set)
		if (null == mJWebSocketHome) {
			mJWebSocketHome = System.getProperty(JWebSocketServerConstants.JWEBSOCKET_HOME);
			if (null != mJWebSocketHome) {
				System.out.println(JWebSocketServerConstants.JWEBSOCKET_HOME
						+ ": Using property "
						+ JWebSocketServerConstants.JWEBSOCKET_HOME + ": "
						+ mJWebSocketHome);
			}
		}

		// if no Java property check environment variable
		if (null == mJWebSocketHome) {
			mJWebSocketHome = System.getenv(JWebSocketServerConstants.JWEBSOCKET_HOME);
			if (null != mJWebSocketHome) {
				System.out.println(JWebSocketServerConstants.JWEBSOCKET_HOME
						+ ": Using environment variable "
						+ JWebSocketServerConstants.JWEBSOCKET_HOME + ": "
						+ mJWebSocketHome);
			}
		}

		// check current folders (only if not Web Application)
		if (!isWebApp() && null == mJWebSocketHome) {
			IOFileFilter lFileFilter = new WildcardFileFilter("jWebSocketServer*.jar");
			IOFileFilter lDirFilter;
			lDirFilter = FileFilterUtils.directoryFileFilter();
			Collection<File> lFiles;
			File lDir;

			// we are in development mode (NetBeans)?
			try {
				lDir = new File("../../../rte/jWebSocket-1.0/libs");
				lFiles = FileUtils.listFiles(lDir, lFileFilter, lDirFilter);
			} catch (Exception lEx) {
				lFiles = null;
			}
			if (null != lFiles && !lFiles.isEmpty()) {
				lDir = new File("../../../rte/jWebSocket-1.0");
				mJWebSocketHome = FilenameUtils.normalize(lDir.getAbsolutePath());
			} else {
				// we are in the /libs folder?
				try {
					lDir = new File(".");
					lFiles = FileUtils.listFiles(lDir, lFileFilter, lDirFilter);
				} catch (Exception lEx) {
					lFiles = null;
				}
				if (null != lFiles && !lFiles.isEmpty()) {
					lDir = new File("../");
					mJWebSocketHome = FilenameUtils.normalize(lDir.getAbsolutePath());
				} else {
					// we are in the /bin folder?
					try {
						lDir = new File("../libs");
						lFiles = FileUtils.listFiles(lDir, lFileFilter, lDirFilter);
					} catch (Exception lEx) {
						lFiles = null;
					}
					if (null != lFiles && !lFiles.isEmpty()) {
						lDir = new File("../");
						mJWebSocketHome = FilenameUtils.normalize(lDir.getAbsolutePath());
					} else {
						// we are in the base folder?
						try {
							lDir = new File("libs");
							lFiles = FileUtils.listFiles(lDir, lFileFilter, lDirFilter);
						} catch (Exception lEx) {
							lFiles = null;
						}
						if (lFiles != null && !lFiles.isEmpty()) {
							lDir = new File(".");
							mJWebSocketHome = FilenameUtils.normalize(lDir.getAbsolutePath());
						}
					}
				}
			}
		}

		// ensure that we get at least an empty string to avoid null pointer exceptions
		if (null == mJWebSocketHome) {
			mJWebSocketHome = "";
		}

		adjustJWebSocketHome();
		System.setProperty(JWebSocketServerConstants.JWEBSOCKET_HOME, mJWebSocketHome);

		return mJWebSocketHome;
	}

	/**
	 *
	 * @return
	 */
	public static String adjustJWebSocketHome() {
		if (!mJWebSocketHome.isEmpty()) {
			try {
				mJWebSocketHome = URLDecoder.decode(mJWebSocketHome, "utf-8");
				mJWebSocketHome = new File(mJWebSocketHome).getPath() + File.separator;
			} catch (UnsupportedEncodingException lEx) {
				throw new RuntimeException(lEx);
			}
		}

		return mJWebSocketHome;
	}

	/**
	 *
	 * @return
	 */
	public static String getConfigPath() {
		return mConfigPath;
	}

	/**
	 *
	 * @return
	 */
	public static String getBootstrapPath() {
		return mBootstrapPath;
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	private static String findConfigPath(String aFilename) {
		String lPath;
		if (null != mJWebSocketHome
				&& null == mConfigPath
				&& null != aFilename) {
			File lFile = new File(mJWebSocketHome + "conf" + File.separator + aFilename);
			lPath = FilenameUtils.normalize(lFile.getAbsolutePath());
			mConfigPath = lPath;
		}
		return mConfigPath;
	}

	/**
	 *
	 * @return
	 */
	private static String findConfigPath() {
		return findConfigPath(JWebSocketServerConstants.JWEBSOCKET_XML);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	private static String findBootstrapPath(String aFilename) {
		if (null == mBootstrapPath && null != aFilename) {
			mBootstrapPath = getJWebSocketHome()
					+ "conf" + File.separator + "Resources" + File.separator + aFilename;
		}
		return mBootstrapPath;
	}

	/**
	 *
	 * @return
	 */
	private static String findBootstrapPath() {
		return findBootstrapPath(JWebSocketServerConstants.BOOTSTRAP_XML);
	}

	/**
	 *
	 * @return
	 */
	public static boolean isWebApp() {
		return mIsWebApp;
	}

	/**
	 *
	 * @return
	 */
	public static boolean isLoadConfigFromResource() {
		// if not jWebSocket Home is given load from resource
		return (null == mJWebSocketHome) || (mJWebSocketHome.isEmpty());
	}

	/**
	 *
	 * @param aArgs
	 */
	public static void initForConsoleApp(String[] aArgs) {
		if (aArgs != null && aArgs.length > 0) {
			for (int lIdx = 0; lIdx < aArgs.length; lIdx++) {
				// is there one more argument beyond the current one?
				if (lIdx < aArgs.length - 1) {
					if ("-config".equals(aArgs[lIdx])) {
						mConfigPath = Tools.expandEnvVarsAndProps(aArgs[lIdx + 1]);
					} else if ("-bootstrap".equals(aArgs[lIdx])) {
						mBootstrapPath = Tools.expandEnvVarsAndProps(aArgs[lIdx + 1]);
					} else if ("-home".equals(aArgs[lIdx])) {
						mJWebSocketHome = Tools.expandEnvVarsAndProps(aArgs[lIdx + 1]);
						// check trailing backslash
						adjustJWebSocketHome();
						System.setProperty(
								JWebSocketServerConstants.JWEBSOCKET_HOME, mJWebSocketHome);
						System.out.println(
								"Using command-line argument -home "
								+ mJWebSocketHome);
					}
				}
			}
		}
		// init JWEBSOCKET_HOME
		if (null == mJWebSocketHome) {
			mJWebSocketHome = findJWebSocketHome();
		}
		// init path to jWebSocket.xml config file
		if (null == mConfigPath) {
			mConfigPath = findConfigPath();
		}
		// init path to bootstrap.xml config file
		if (null == mBootstrapPath) {
			mBootstrapPath = findBootstrapPath();
		}

		try {
			String lLog4JPath = mJWebSocketHome + "conf/log4j.xml";
			DOMConfigurator.configure(lLog4JPath);
			mLog = Logger.getLogger(JWebSocketConfig.class);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Console-Mode: Logs successfully configured by '" + lLog4JPath + "'.");
			}
		} catch (Exception lEx) {
			System.out.println(lEx.getClass().getSimpleName() + " configuring logs: " + lEx.getMessage());
		}

		// register global exception handler
		org.jwebsocket.exception.GlobalExceptionHandler.registerGlobalExceptionHandler();
	}

	/**
	 *
	 *
	 * @param aArgs
	 * @param lWebAppHome
	 */
	public static void initForWebApp(String[] aArgs, String lWebAppHome) {
		mIsWebApp = true;
		mWebAppHome = lWebAppHome;

		if (aArgs != null && aArgs.length > 0) {
			for (int lIdx = 0; lIdx < aArgs.length; lIdx++) {
				// is there one more argument beyond the current one?
				if (lIdx < aArgs.length - 1) {
					if ("-config".equals(aArgs[lIdx])) {
						mConfigPath = expandEnvVarsAndProps(aArgs[lIdx + 1]);
					} else if ("-bootstrap".equals(aArgs[lIdx])) {
						mBootstrapPath = expandEnvVarsAndProps(aArgs[lIdx + 1]);
					} else if ("-home".equals(aArgs[lIdx])) {
						mJWebSocketHome = expandEnvVarsAndProps(aArgs[lIdx + 1]);

						// check trailing backslash
						adjustJWebSocketHome();

						System.out.println(
								"Using JWEBSOCKET_HOME argument -home "
								+ mJWebSocketHome);
					}
				}
			}
		}

		// using env var if not '-home' param is provided
		if (null == mJWebSocketHome) {
			mJWebSocketHome = System.getenv(JWebSocketServerConstants.JWEBSOCKET_HOME);
		}
		Assert.notNull(mJWebSocketHome, "Missing required JWEBSOCKET_HOME path. "
				+ "Please be sure that you are setting "
				+ "properly the 'jws_home' context param "
				+ "or you have a JWEBSOCKET_HOME env variable defined!");

		// init path to jWebSocket.xml config file
		if (null == mConfigPath) {
			mConfigPath = findConfigPath();
		}
		// init path to bootstrap.xml config file
		if (null == mBootstrapPath) {
			mBootstrapPath = findBootstrapPath();
		}

		mLog = Logger.getLogger(JWebSocketConfig.class);

		// register global exception handler
		org.jwebsocket.exception.GlobalExceptionHandler.registerGlobalExceptionHandler();
	}

	/**
	 * Replaces all pattern ${name} in a string by the values of the
	 * corresponding environment variable or system property. The setting of a
	 * system property overrides the setting of the environment variable.
	 *
	 * @param aString
	 * @return
	 * @deprecated Use JWebSocketConfig.expandEnvVarsAndProps
	 */
	public static String expandEnvAndJWebSocketVars(String aString) {
		return expandEnvVarsAndProps(aString);
	}

	/**
	 * Replaces all pattern ${name} in a string by the values of the
	 * corresponding environment variable or system property. The setting of a
	 * system property overrides the setting of the environment variable.
	 *
	 * @param aString
	 * @return
	 */
	public static String expandEnvVarsAndProps(String aString) {
		Map lVars = new FastMap<String, String>();
		lVars.putAll(System.getenv());
		String lRes;

		if (isWebApp()) {
			lVars.put(JWebSocketServerConstants.JWEBSOCKET_HOME, getJWebSocketHome());
			lVars.put("WEB_APP_HOME", getWebAppHome());
			lRes = Tools.expandVars(aString, lVars, true);
		} else {
			lRes = Tools.expandEnvVarsAndProps(aString);
		}

		return lRes;
	}

	/**
	 * @return the libraryFolder
	 */
	public String getLibraryFolder() {
		return mLibraryFolder;
	}

	/**
	 * @return the config
	 */
	public static JWebSocketConfig getConfig() {
		return mConfig;
	}

	/**
	 * private constructor used by the builder
	 */
	private JWebSocketConfig(Builder aBuilder) {
		if (aBuilder.mEngines == null
				|| aBuilder.mServers == null
				|| aBuilder.mPlugins == null
				|| aBuilder.mUsers == null
				|| aBuilder.mGlobalRights == null
				|| aBuilder.mGlobalRoles == null
				|| aBuilder.getFilters() == null
				|| aBuilder.mLoggingConfig == null) {
			throw new WebSocketRuntimeException("Configuration is not loaded completely.");
		}
		mProtocol = aBuilder.mProtocol;
		mNodeId = aBuilder.mNodeId;
		mLibraryFolder = aBuilder.mLibraryFolder;
		mLibraries = aBuilder.mLibraries;
		mEngines = aBuilder.mEngines;
		mServers = aBuilder.mServers;
		mUsers = aBuilder.mUsers;
		mPlugins = aBuilder.mPlugins;
		mFilters = aBuilder.getFilters();
		mLoggingConfig = aBuilder.mLoggingConfig;
		mGlobalRights = aBuilder.mGlobalRights;
		mGlobalRoles = aBuilder.mGlobalRoles;

		// validate the config
		validate();
	}

	/**
	 * Config builder class.
	 *
	 * @author puran
	 */
	public static class Builder {

		private String mProtocol;
		private String mNodeId;
		private String mLibraryFolder;
		private List<LibraryConfig> mLibraries;
		private List<EngineConfig> mEngines;
		private List<ServerConfig> mServers;
		private List<UserConfig> mUsers;
		private List<PluginConfig> mPlugins;
		private List<FilterConfig> mFilters;
		private LoggingConfig mLoggingConfig;
		private List<RightConfig> mGlobalRights;
		private List<RoleConfig> mGlobalRoles;

		/**
		 *
		 * @param aProtocol
		 * @return
		 */
		public Builder setProtocol(String aProtocol) {
			mProtocol = aProtocol;
			return this;
		}

		/**
		 *
		 * @param aNodeId
		 * @return
		 */
		public Builder setNodeId(String aNodeId) {
			mNodeId = aNodeId;
			return this;
		}

		/**
		 *
		 * @param aLibraryFolder
		 * @return
		 */
		public Builder setLibraryFolder(String aLibraryFolder) {
			mLibraryFolder = aLibraryFolder;
			return this;
		}

		/**
		 *
		 * @param aLibraries
		 * @return
		 */
		public Builder setLibraries(List<LibraryConfig> aLibraries) {
			mLibraries = aLibraries;
			return this;
		}

		/**
		 *
		 * @param aEngines
		 * @return
		 */
		public Builder setEngines(List<EngineConfig> aEngines) {
			mEngines = aEngines;
			return this;
		}

		/**
		 *
		 * @param aServers
		 * @return
		 */
		public Builder setServers(List<ServerConfig> aServers) {
			mServers = aServers;
			return this;
		}

		/**
		 *
		 * @param aPlugins
		 * @return
		 */
		public Builder setPlugins(List<PluginConfig> aPlugins) {
			mPlugins = aPlugins;
			return this;
		}

		/**
		 *
		 * @param aFilters
		 * @return
		 */
		public Builder setFilters(List<FilterConfig> aFilters) {
			mFilters = aFilters;
			return this;
		}

		/**
		 *
		 * @param aLoggingConfigs
		 * @return
		 */
		public Builder setLoggingConfig(List<LoggingConfig> aLoggingConfigs) {
			mLoggingConfig = aLoggingConfigs.get(0);
			return this;
		}

		/**
		 *
		 * @param aRights
		 * @return
		 */
		public Builder setGlobalRights(List<RightConfig> aRights) {
			mGlobalRights = aRights;
			return this;
		}

		/**
		 *
		 * @param aRoles
		 * @return
		 */
		public Builder setGlobalRoles(List<RoleConfig> aRoles) {
			mGlobalRoles = aRoles;
			return this;
		}

		/**
		 *
		 * @param aUsers
		 * @return
		 */
		public Builder setUsers(List<UserConfig> aUsers) {
			mUsers = aUsers;
			return this;
		}

		/**
		 *
		 * @return
		 */
		public synchronized JWebSocketConfig buildConfig() {
//			if (mConfig == null) {
			mConfig = new JWebSocketConfig(this);
//			}
			return mConfig;
		}

		/**
		 * @return the filters
		 */
		public List<FilterConfig> getFilters() {
			return mFilters;
		}
	}

	/**
	 * @return the engines
	 */
	public List<LibraryConfig> getLibraries() {
		if (mLibraries != null) {
			return Collections.unmodifiableList(mLibraries);
		}
		return null;
	}

	/**
	 * @return the engines
	 */
	public List<EngineConfig> getEngines() {
		if (mEngines != null) {
			return Collections.unmodifiableList(mEngines);
		}
		return null;
	}

	/**
	 * @return the servers
	 */
	public List<ServerConfig> getServers() {
		if (mServers != null) {
			return Collections.unmodifiableList(mServers);
		}
		return null;
	}

	/**
	 * @return the users
	 */
	public List<UserConfig> getUsers() {
		if (mUsers != null) {
			return Collections.unmodifiableList(mUsers);
		}
		return null;
	}

	/**
	 * @return the plugins
	 */
	public List<PluginConfig> getPlugins() {
		if (mPlugins != null) {
			return Collections.unmodifiableList(mPlugins);
		}
		return null;
	}

	/**
	 *
	 * @param aIdPlugIn
	 * @return
	 */
	public PluginConfig getPlugin(String aIdPlugIn) {
		if (mPlugins != null) {
			for (int i = 0; i < mPlugins.size(); i++) {
				if (mPlugins.get(i).getId().equals(aIdPlugIn)) {
					return mPlugins.get(i);
				}
			}
		}
		return null;
	}

	/**
	 * @return the filters
	 */
	public List<FilterConfig> getFilters() {
		if (mFilters != null) {
			return Collections.unmodifiableList(mFilters);
		}
		return null;
	}

	/**
	 *
	 * @param aIdFilter
	 * @return
	 */
	public FilterConfig getFilter(String aIdFilter) {
		if (mFilters != null) {
			for (int i = 0; i < mFilters.size(); i++) {
				if (mFilters.get(i).getId().equals(aIdFilter)) {
					return mFilters.get(i);
				}
			}
		}
		return null;
	}

	/**
	 * @return the logging config object
	 */
	public LoggingConfig getLoggingConfig() {
		return mLoggingConfig;
	}

	/**
	 * @return the globalRights
	 */
	public List<RightConfig> getGlobalRights() {
		if (mGlobalRights != null) {
			return Collections.unmodifiableList(mGlobalRights);
		}
		return null;
	}

	/**
	 * @return the globalRoles
	 */
	public List<RoleConfig> getGlobalRoles() {
		if (mGlobalRoles != null) {
			return Collections.unmodifiableList(mGlobalRoles);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ( // we at least need one engine to process the connections
				(mEngines == null || mEngines.isEmpty())
				// we at least need one server to route the messages
				|| (mServers == null || mServers.isEmpty())
				|| (mUsers == null || mUsers.isEmpty())
				// we at least need the system plug-in
				|| (mPlugins == null || mPlugins.isEmpty())
				// the libraries section does not necessarily need to exist!
				// if not simply no external libraries are loaded.
				// || (mLibraries == null || mLibraries.isEmpty())

				// we do not want to force the users to use filters.
				// please leave this comment to prevent introducing the
				// following line again!
				|| (mFilters == null) /*
				 * || mFilters.isEmpty()
				 */
				|| (mLoggingConfig == null)
				|| (mGlobalRights == null || mGlobalRights.isEmpty())
				|| (mGlobalRoles == null || mGlobalRoles.isEmpty())) {
			throw new WebSocketRuntimeException("Missing one of the server configuration, please check your configuration file");
		}
	}

	private static void checkLogs() {
		if (mLog == null) {
			mLog = Logging.getLogger(JWebSocketConfig.class);
		}
	}

	/**
	 * private method that checks the path of the jWebSocket.xml file
	 *
	 * @param aText
	 * @param aSubFolder
	 * @param aFilename
	 * @param aClassLoader
	 * @return the path to jWebSocket.xml
	 */
	public static String getSubFolder(String aText, String aSubFolder,
			String aFilename, ClassLoader aClassLoader) {

		String lPath;
		String lJWebSocketHome;
		File lFile;

		checkLogs();

		// try to load resource from %JWEBSOCKET_HOME%sub folder
		lJWebSocketHome = getJWebSocketHome();
		if (lJWebSocketHome != null) {

			// if JWEBSOCKET_HOME not set and not given try to eval from resource
			// System.out.println("Loading from folder...");
			if (lJWebSocketHome.isEmpty() && null != aClassLoader) {
				URL lURL = aClassLoader.getResource("/");
				if (null != lURL) {
					lJWebSocketHome = lURL.getPath();
					// System.out.println("URL found: " + lJWebSocketHome);
				} else {
					// System.out.println("URL not found!");
				}
			}

			// file can to be located in %JWEBSOCKET_HOME%<folder>/
			lPath = lJWebSocketHome + aSubFolder + File.separator
					+ (null != aFilename ? aFilename : "");
			lFile = new File(lPath);
			if (lFile.exists()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Found " + aText + " at " + lPath + "...");
				}
				return lPath;
			} else {
				mLog.warn(aFilename + " not found at " + lPath + ".");
			}
		}
		return null;
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getLogsFolder(String aFilename) {
		return getSubFolder("log file", "logs", aFilename, null);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getTempFolder(String aFilename) {
		return getSubFolder("temporary file", "temp", aFilename, null);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getBinFolder(String aFilename) {
		return getSubFolder("binary file", "bin", aFilename, null);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getConfigFolder(String aFilename) {
		return getSubFolder(
				"config file",
				(isWebApp() ? "conf" : "conf"),
				aFilename,
				null);
	}

	/**
	 *
	 * @param aFilename
	 * @param aClassLoader
	 * @return
	 */
	public static String getConfigFolder(String aFilename, ClassLoader aClassLoader) {
		return getSubFolder(
				"config file",
				(isWebApp() ? "conf" : "conf"),
				aFilename,
				aClassLoader);
	}

	/**
	 *
	 * @param aFilename
	 * @param aClassLoader
	 * @return
	 */
	public static String getLibsFolder(String aFilename, ClassLoader aClassLoader) {
		return getSubFolder(
				"library",
				(isWebApp() && isLoadConfigFromResource() ? "lib" : "libs"),
				aFilename,
				aClassLoader);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getLibsFolder(String aFilename) {
		return getSubFolder(
				"library",
				(isWebApp() ? "lib" : "libs"),
				aFilename,
				null);
	}

	/**
	 *
	 * @param aPath
	 * @return
	 */
	public static URL getURLFromPath(String aPath) {
		// lURL = ClassLoader.getSystemClassLoader().getResource(aPath);
		URL lURL = getURLFromPath(aPath, Thread.currentThread().getContextClassLoader());
		return lURL;
	}

	/**
	 *
	 * @param aPath
	 * @param aClassLoader
	 * @return
	 */
	public static URL getURLFromPath(String aPath, ClassLoader aClassLoader) {
		URL lURL = null;
		try {
			if (isLoadConfigFromResource()) {
				lURL = aClassLoader.getResource(aPath);
			} else {
				lURL = new URL("file://" + aPath);
			}
		} catch (MalformedURLException lEx) {
			System.out.println(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		return lURL;
	}
}
