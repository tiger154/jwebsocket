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
package org.jwebsocket.config;

import org.jwebsocket.config.xml.LoggingConfig;
import static org.jwebsocket.config.JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
import static org.jwebsocket.config.JWebSocketServerConstants.CATALINA_HOME;
import static org.jwebsocket.config.JWebSocketServerConstants.DEFAULT_INSTALLATION;
import static org.jwebsocket.config.JWebSocketServerConstants.DEFAULT_NODE_ID;
import static org.jwebsocket.config.JWebSocketServerConstants.JWEBSOCKET_HOME;
import static org.jwebsocket.config.JWebSocketServerConstants.JWEBSOCKET_XML;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
// import org.jwebsocket.config.xml.ChannelConfig;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.config.xml.LibraryConfig;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.config.xml.RightConfig;
import org.jwebsocket.config.xml.RoleConfig;
import org.jwebsocket.config.xml.ServerConfig;
import org.jwebsocket.config.xml.UserConfig;
import org.jwebsocket.kit.WebSocketRuntimeException;
import org.jwebsocket.logging.Logging;

/**
 * Represents the jWebSocket configuration. This class is immutable and should
 * not be overridden.
 * 
 * @author puran
 * @version $Id: JWebSocketConfig.java 345 2010-04-10 20:03:48Z fivefeetfurther$
 */
public final class JWebSocketConfig implements Config {

	// DON'T SET LOGGER HERE! NEEDS TO BE INITIALIZED FIRST!
	private static Logger mLog = null;
	private final String mInstallation;
	private final String mNodeId;
	private final String mProtocol;
	private final String jWebSocketHome;
	private final String mLibraryFolder;
	private final String mInitializer;
	private final List<LibraryConfig> mLibraries;
	private final List<EngineConfig> mEngines;
	private final List<ServerConfig> mServers;
	private final List<UserConfig> mUsers;
	private final List<PluginConfig> mPlugins;
	private final List<FilterConfig> mFilters;
	private final LoggingConfig mLoggingConfig;
	private final List<RightConfig> mGlobalRights;
	private final List<RoleConfig> mGlobalRoles;
	// private final List<ChannelConfig> mChannels;
	private static JWebSocketConfig mConfig = null;
	private static ClassLoader mClassLoader = null;
	private static String mConfigOverrideRoot = null;

	/**
	 * @return the mClassLoader
	 */
	public static ClassLoader getClassLoader() {
		return mClassLoader;
	}

	/**
	 * @param amClassLoader the mClassLoader to set
	 */
	public static void setClassLoader(ClassLoader aClassLoader) {
		mClassLoader = aClassLoader;
	}

	/**
	 * @return the installation
	 */
	public String getInstallation() {
		if (mInstallation == null || mInstallation.length() == 0) {
			return DEFAULT_INSTALLATION;
		}
		return mInstallation;
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
	 * @return the jWebSocketHome
	 */
	public String getjWebSocketHome() {
		return jWebSocketHome;
	}

	/**
	 * @return the libraryFolder
	 */
	public String getLibraryFolder() {
		return mLibraryFolder;
	}

	/**
	 * @return the initializer
	 */
	public String getInitializer() {
		return mInitializer;
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
		mInstallation = aBuilder.mInstallation;
		mProtocol = aBuilder.mProtocol;
		mNodeId = aBuilder.mNodeId;
		jWebSocketHome = aBuilder.jWebSocketHome;
		mLibraryFolder = aBuilder.mLibraryFolder;
		mInitializer = aBuilder.mInitializer;
		mLibraries = aBuilder.mLibraries;
		mEngines = aBuilder.mEngines;
		mServers = aBuilder.mServers;
		mUsers = aBuilder.mUsers;
		mPlugins = aBuilder.mPlugins;
		mFilters = aBuilder.getFilters();
		mLoggingConfig = aBuilder.mLoggingConfig;
		mGlobalRights = aBuilder.mGlobalRights;
		mGlobalRoles = aBuilder.mGlobalRoles;
		// mChannels = aBuilder.mChannels;
		// validate the config
		validate();
	}

	/**
	 * Config builder class.
	 *
	 * @author puran
	 * @version $Id: JWebSocketConfig.java 596 2010-06-22 17:09:54Z
	 *          fivefeetfurther $
	 */
	public static class Builder {

		private String mInstallation;
		private String mProtocol;
		private String mNodeId;
		private String jWebSocketHome;
		private String mLibraryFolder;
		private String mInitializer;
		private List<LibraryConfig> mLibraries;
		private List<EngineConfig> mEngines;
		private List<ServerConfig> mServers;
		private List<UserConfig> mUsers;
		private List<PluginConfig> mPlugins;
		private List<FilterConfig> mFilters;
		private LoggingConfig mLoggingConfig;
		private List<RightConfig> mGlobalRights;
		private List<RoleConfig> mGlobalRoles;
		// private List<ChannelConfig> mChannels;

		/**
		 *
		 * @param aInstallation
		 * @return
		 */
		public Builder setInstallation(String aInstallation) {
			mInstallation = aInstallation;
			return this;
		}

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
		 * @param aJWebSocketHome
		 * @return
		 */
		public Builder setJWebSocketHome(String aJWebSocketHome) {
			jWebSocketHome = aJWebSocketHome;
			return this;
		}

		/**
		 *
		 * @param aInitializer
		 * @return
		 */
		public Builder setInitializer(String aInitializer) {
			mInitializer = aInitializer;
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
	 * @return the filters
	 */
	public List<FilterConfig> getFilters() {
		if (mFilters != null) {
			return Collections.unmodifiableList(mFilters);
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
				|| (mFilters == null) /* || mFilters.isEmpty() */
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
	 * @return the path to jWebSocket.xml
	 */
	public static String getConfigurationPath() {
		String lWebSocketXML = null;
		String lWebSocketHome = null;
		String lFileSep = System.getProperty("file.separator");
		File lFile;
		// try to obtain JWEBSOCKET_HOME environment variable
		lWebSocketHome = System.getenv(JWEBSOCKET_HOME);
		System.out.println("Looking for config file at %" + JWEBSOCKET_HOME + "%: " + lWebSocketHome + "...");
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jWebSocket.xml can be located in %JWEBSOCKET_HOME%/conf
			lWebSocketXML = lWebSocketHome + "conf" + lFileSep + JWEBSOCKET_XML;
			lFile = new File(lWebSocketXML);
			System.out.println("Checking config " + lWebSocketXML);
			if (lFile.exists()) {
				return lWebSocketXML;
			}
		}
		// try to obtain CATALINA_HOME environment variable
		lWebSocketHome = System.getenv(CATALINA_HOME);
		System.out.println("Looking for config file at %" + CATALINA_HOME + "%: " + lWebSocketHome + "...");
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jWebSocket.xml can be located in %CATALINA_HOME%/conf
			lWebSocketXML = lWebSocketHome + "conf" + lFileSep + JWEBSOCKET_XML + "...";
			lFile = new File(lWebSocketXML);
			System.out.println("Checking config " + lWebSocketXML);
			if (lFile.exists()) {
				return lWebSocketXML;
			}
		}
		// finally try to find config file at %CLASSPATH%/conf/
		URL lURL = Thread.currentThread().getContextClassLoader().getResource("conf/" + JWEBSOCKET_XML);
		System.out.println("Looking for config file in classpath " + lURL.toString());
		if (lURL != null) {
			try {
				URI lFilename = lURL.toURI();
				lFile = new File(lFilename);
				System.out.println("Checking config " + lFile.getPath());
				if (lFile.exists()) {
					lWebSocketXML = lFile.getPath();
					return lWebSocketXML;
				}
			} catch (Exception ex) {
			}
		}
		return null;
	}

	/**
	 * private method that checks the path of the jWebSocket.xml file
	 *
	 * @param aSubFolder 
	 * @param aFilename
	 * @return the path to jWebSocket.xml
	 */
	public static String getSubFolder(String aText, String aSubFolder,
			String aSubFolderTomcat, String aFilename) {

		String lPath = null;
		String lJWebSocketHome = null;
		String lFileSep = null;
		File lFile = null;

		checkLogs();

		// try to load lib from %JWEBSOCKET_HOME%/libs folder
		lJWebSocketHome = System.getenv(JWEBSOCKET_HOME);
		lFileSep = System.getProperty("file.separator");
		if (lJWebSocketHome != null) {
			// append trailing slash if needed
			if (!lJWebSocketHome.endsWith(lFileSep)) {
				lJWebSocketHome += lFileSep;
			}
			// jar can to be located in %JWEBSOCKET_HOME%/libs
			lPath = lJWebSocketHome + aSubFolder + lFileSep
					+ (null != aFilename ? aFilename : "");
			lFile = new File(lPath);
			if (lFile.exists()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Found " + aText + " at " + lPath + "...");
				}
				return lPath;
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug(aFilename + " not found at %"
							+ JWEBSOCKET_HOME + "%/" + aSubFolder + ".");
				}
			}
		}

		// try to load lib from %CATALINA_HOME%/libs folder
		lJWebSocketHome = System.getenv(CATALINA_HOME);
		lFileSep = System.getProperty("file.separator");
		if (lJWebSocketHome != null) {
			// append trailing slash if needed
			if (!lJWebSocketHome.endsWith(lFileSep)) {
				lJWebSocketHome += lFileSep;
			}
			// jars can to be located in %CATALINA_HOME%/lib
			lPath = lJWebSocketHome + aSubFolderTomcat + lFileSep
					+ (null != aFilename ? aFilename : "");
			lFile = new File(lPath);
			if (lFile.exists()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Found " + aText + " at " + lPath + "...");
				}
				return lPath;
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug(aFilename + " not found at %" + CATALINA_HOME
							+ "/" + aSubFolderTomcat + "%.");
				}
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
		return getSubFolder("log file", "logs", "logs", aFilename);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getTempFolder(String aFilename) {
		return getSubFolder("temporary file", "temp", "work", aFilename);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getBinFolder(String aFilename) {
		return getSubFolder("binary file", "bin", "bin", aFilename);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getConfigFolder(String aFilename) {
		return getSubFolder("config file", "conf", "conf", aFilename);
	}

	/**
	 *
	 * @param aFilename
	 * @return
	 */
	public static String getLibsFolder(String aFilename) {
		return getSubFolder("library", "libs", "lib", aFilename);
	}
}
