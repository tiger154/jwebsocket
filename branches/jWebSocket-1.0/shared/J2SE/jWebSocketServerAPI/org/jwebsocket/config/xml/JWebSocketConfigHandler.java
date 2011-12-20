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
package org.jwebsocket.config.xml;

import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.config.ConfigHandler;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.kit.WebSocketRuntimeException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Handler class that handles the <tt>jWebSocket.xml</tt> configuration. This
 * class starts from the root and delegates the handler to specific config
 * handler, to read the whole config file.
 * 
 * @author puran
 * @version $Id: JWebSocketConfigHandler.java 596 2010-06-22 17:09:54Z
 *          fivefeetfurther $
 */
public class JWebSocketConfigHandler implements ConfigHandler {

	// We cannot use the logging subsystem here because its config needs to be
	// loaded first!
	private static final String ELEMENT_INSTALLATION = "installation";
	private static final String ELEMENT_PROTOCOL = "protocol";
	private static final String ELEMENT_NODE_ID = "node_id";
	private static final String ELEMENT_INITIALIZER_CLASS = "initializerClass";
	private static final String ELEMENT_JWEBSOCKET_HOME = "jWebSocketHome";
	private static final String ELEMENT_LIBRARY_FOLDER = "libraryFolder";
	private static final String ELEMENT_LIBRARIES = "libraries";
	private static final String ELEMENT_LIBRARY = "library";
	private static final String ELEMENT_ENGINES = "engines";
	private static final String ELEMENT_ENGINE = "engine";
	private static final String ELEMENT_SERVERS = "servers";
	private static final String ELEMENT_SERVER = "server";
	private static final String ELEMENT_PLUGINS = "plugins";
	private static final String ELEMENT_PLUGIN = "plugin";
	private static final String ELEMENT_FILTERS = "filters";
	private static final String ELEMENT_FILTER = "filter";
	private static final String ELEMENT_LOGGING = "logging";
	private static final String ELEMENT_LOG4J = "log4j";
	private static final String ELEMENT_RIGHTS = "rights";
	private static final String ELEMENT_RIGHT = "right";
	private static final String ELEMENT_ROLES = "roles";
	private static final String ELEMENT_ROLE = "role";
	private static final String ELEMENT_USERS = "users";
	private static final String ELEMENT_USER = "user";
	private static final String JWEBSOCKET = "jWebSocket";
	private static final String ELEMENT_THREAD_POOL = "threadPool";
	private static Map<String, ConfigHandler> handlerContext = new FastMap<String, ConfigHandler>();

	// initialize the different config handler implementations
	static {
		handlerContext.put("library", new LibraryConfigHandler());
		handlerContext.put("engine", new EngineConfigHandler());
		handlerContext.put("plugin", new PluginConfigHandler());
		handlerContext.put("server", new ServerConfigHandler());
		handlerContext.put("user", new UserConfigHandler());
		handlerContext.put("role", new RoleConfigHandler());
		handlerContext.put("right", new RightConfigHandler());
		handlerContext.put("filter", new FilterConfigHandler());
		handlerContext.put("log4j", new LoggingConfigHandler());
		handlerContext.put("threadPool", new ThreadPoolConfigHandler());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JWebSocketConfig processConfig(XMLStreamReader aStreamReader) {
		JWebSocketConfig.Builder lConfigBuilder = new JWebSocketConfig.Builder();

		try {
			while (aStreamReader.hasNext()) {
				aStreamReader.next();
				if (aStreamReader.isStartElement()) {
					String lElementName = aStreamReader.getLocalName();
					if (lElementName.equals(ELEMENT_INSTALLATION)) {
						aStreamReader.next();
						lConfigBuilder.setInstallation(aStreamReader.getText());
					} else if (lElementName.equals(ELEMENT_INITIALIZER_CLASS)) {
						aStreamReader.next();
						lConfigBuilder.setInitializer(aStreamReader.getText());
					} else if (lElementName.equals(ELEMENT_PROTOCOL)) {
						aStreamReader.next();
						lConfigBuilder.setProtocol(aStreamReader.getText());
					} else if (lElementName.equals(ELEMENT_NODE_ID)) {
						aStreamReader.next();
						lConfigBuilder.setNodeId(aStreamReader.getText());
					} else if (lElementName.equals(ELEMENT_JWEBSOCKET_HOME)) {
						aStreamReader.next();
						lConfigBuilder.setJWebSocketHome(aStreamReader.getText());
					} else if (lElementName.equals(ELEMENT_LIBRARY_FOLDER)) {
						aStreamReader.next();
						lConfigBuilder.setLibraryFolder(aStreamReader.getText());
					} else if (lElementName.equals(ELEMENT_LIBRARIES)) {
						List<LibraryConfig> lLibraries = handleLibraries(aStreamReader);
						lConfigBuilder = lConfigBuilder.setLibraries(lLibraries);
					} else if (lElementName.equals(ELEMENT_ENGINES)) {
						List<EngineConfig> lEngines = handleEngines(aStreamReader);
						lConfigBuilder = lConfigBuilder.setEngines(lEngines);
					} else if (lElementName.equals(ELEMENT_SERVERS)) {
						List<ServerConfig> lServers = handleServers(aStreamReader);
						lConfigBuilder = lConfigBuilder.setServers(lServers);
					} else if (lElementName.equals(ELEMENT_PLUGINS)) {
						List<PluginConfig> lPlugins = handlePlugins(aStreamReader);
						lConfigBuilder = lConfigBuilder.setPlugins(lPlugins);
					} else if (lElementName.equals(ELEMENT_FILTERS)) {
						List<FilterConfig> lFilters = handleFilters(aStreamReader);
						lConfigBuilder = lConfigBuilder.setFilters(lFilters);
					} else if (lElementName.equals(ELEMENT_LOGGING)) {
						List<LoggingConfig> loggingConfigs = handleLoggingConfigs(aStreamReader);
						lConfigBuilder = lConfigBuilder.setLoggingConfig(loggingConfigs);
					} else if (lElementName.equals(ELEMENT_RIGHTS)) {
						List<RightConfig> lGlobalRights = handleRights(aStreamReader);
						lConfigBuilder = lConfigBuilder.setGlobalRights(lGlobalRights);
					} else if (lElementName.equals(ELEMENT_ROLES)) {
						List<RoleConfig> lRoles = handleRoles(aStreamReader);
						lConfigBuilder = lConfigBuilder.setGlobalRoles(lRoles);
					} else if (lElementName.equals(ELEMENT_USERS)) {
						List<UserConfig> lUsers = handleUsers(aStreamReader);
						lConfigBuilder = lConfigBuilder.setUsers(lUsers);
					} else {
						// ignore
					}
				}
				if (aStreamReader.isEndElement()) {
					String lElementName = aStreamReader.getLocalName();
					if (lElementName.equals(JWEBSOCKET)) {
						break;
					}
				}
			}
		} catch (XMLStreamException lEx) {
			throw new WebSocketRuntimeException("Error parsing jWebSocket.xml configuration file", lEx);
		}

		// if no filters where given in the .xml file
		// initialize empty filter list here
		if (lConfigBuilder.getFilters() == null) {
			lConfigBuilder.setFilters(new FastList<FilterConfig>());
		}

		// now return the config object, this is the only one config object that
		// should exists
		// in the system
		return lConfigBuilder.buildConfig();
	}

	/**
	 * private method to handle the user config.
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of user config
	 * @throws XMLStreamException
	 *           if there's any exception reading configuration
	 */
	private List<UserConfig> handleUsers(XMLStreamReader aStreamReader)
			throws XMLStreamException {
		List<UserConfig> lUsers = new FastList<UserConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_USER)) {
					UserConfig lUser =
							(UserConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lUsers.add(lUser);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_USERS)) {
					break;
				}
			}
		}
		return lUsers;
	}

	/**
	 * method that reads the roles configuration
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of roles config
	 * @throws XMLStreamException
	 *           if there's any exception reading configuration
	 */
	private List<RoleConfig> handleRoles(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<RoleConfig> lRoles = new FastList<RoleConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ROLE)) {
					RoleConfig lRole =
							(RoleConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lRoles.add(lRole);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ROLES)) {
					break;
				}
			}
		}
		return lRoles;
	}

	/**
	 * private method to read the list of rights configuration
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of rights configuration
	 * @throws XMLStreamException
	 *           if there's any exception reading configuration
	 */
	private List<RightConfig> handleRights(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<RightConfig> lRights = new FastList<RightConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_RIGHT)) {
					RightConfig lRight =
							(RightConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lRights.add(lRight);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_RIGHTS)) {
					break;
				}
			}
		}
		return lRights;
	}

	/**
	 * private method that reads the config for plugins
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of plugin configs
	 * @throws XMLStreamException
	 *           if exception occurs while reading
	 */
	private List<PluginConfig> handlePlugins(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<PluginConfig> lPlugins = new FastList<PluginConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_PLUGIN)) {
					PluginConfig lPlugin =
							(PluginConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lPlugins.add(lPlugin);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_PLUGINS)) {
					break;
				}
			}
		}
		return lPlugins;
	}

	/**
	 * private method that reads the config for filters
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of filter configs
	 * @throws XMLStreamException
	 *           if exception occurs while reading
	 */
	private List<FilterConfig> handleFilters(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<FilterConfig> lFilters = new FastList<FilterConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_FILTER)) {
					FilterConfig lFilter =
							(FilterConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lFilters.add(lFilter);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_FILTERS)) {
					break;
				}
			}
		}
		return lFilters;
	}

	/**
	 * private method that reads the config for logging
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of logging configs
	 * @throws XMLStreamException
	 *           if exception occurs while reading
	 */
	private List<LoggingConfig> handleLoggingConfigs(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<LoggingConfig> loggingConfigs = new FastList<LoggingConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_LOG4J)) {
					LoggingConfig loggingConfig =
							(LoggingConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					loggingConfigs.add(loggingConfig);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_LOGGING)) {
					break;
				}
			}
		}
		return loggingConfigs;
	}

	/**
	 * private method that reads the list of server configs
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of server configs
	 * @throws XMLStreamException
	 *           if exception occurs reading xml
	 */
	private List<ServerConfig> handleServers(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<ServerConfig> lServers = new FastList<ServerConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_SERVER)) {
					ServerConfig lServer = (ServerConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lServers.add(lServer);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_SERVERS)) {
					break;
				}
			}
		}
		return lServers;
	}

	/**
	 * private method that reads the list of engines config from the xml file
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of engine configs
	 * @throws XMLStreamException
	 *           if exception occurs while reading
	 */
	private List<LibraryConfig> handleLibraries(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<LibraryConfig> lLibraries = new FastList<LibraryConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_LIBRARY)) {
					LibraryConfig lLibrary =
							(LibraryConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lLibraries.add(lLibrary);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_LIBRARIES)) {
					break;
				}
			}
		}
		return lLibraries;
	}

	/**
	 * private method that reads the list of engines config from the xml file
	 *
	 * @param aStreamReader
	 *          the stream reader object
	 * @return the list of engine configs
	 * @throws XMLStreamException
	 *           if exception occurs while reading
	 */
	private List<EngineConfig> handleEngines(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<EngineConfig> lEngines = new FastList<EngineConfig>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ENGINE)) {
					EngineConfig lEngine =
							(EngineConfig) handlerContext.get(lElementName).processConfig(aStreamReader);
					lEngines.add(lEngine);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ENGINES)) {
					break;
				}
			}
		}
		return lEngines;
	}

	private Document getDocument() throws Exception {
		SAXBuilder lBuilder = new SAXBuilder();
		File lFile = new File(JWebSocketConfig.getConfigurationPath());
		return (Document) lBuilder.build(lFile);
	}

	private void saveChange(Document aDoc) throws IOException {
		XMLOutputter lXmlOutput = new XMLOutputter();
		lXmlOutput.setFormat(Format.getPrettyFormat());
		lXmlOutput.output(aDoc, new FileWriter(JWebSocketConfig.getConfigurationPath()));
	}

	public void setEnabledPlugIn(String aId, Boolean aEnabled) throws Exception {
		Document lDoc = getDocument();
		Element lRootNode = lDoc.getRootElement();
		Element lPlugins = lRootNode.getChild(ELEMENT_PLUGINS);
		List<Element> lPluginsList = lPlugins.getChildren(ELEMENT_PLUGIN);

		for (Element lElement : lPluginsList) {
			if (aId.equals(lElement.getChildText("id"))) {
				if (lElement.getChildText("enabled") == null) {
					lElement.addContent(3, new Element("enabled").setText(aEnabled.toString()));
				} else {
					lElement.getChild("enabled").setText(aEnabled.toString());
				}
			}
		}

		saveChange(lDoc);
	}

	public void setEnabledFilter(String aId, Boolean aEnabled) throws Exception {
		Document lDoc = getDocument();
		Element lRootNode = lDoc.getRootElement();
		Element lFilters = lRootNode.getChild(ELEMENT_FILTERS);
		List<Element> lFiltersList = lFilters.getChildren(ELEMENT_FILTER);

		for (Element lElement : lFiltersList) {
			if (aId.equals(lElement.getChildText("id"))) {
				if (lElement.getChildText("enabled") == null) {
					lElement.addContent(3, new Element("enabled").setText(aEnabled.toString()));
				} else {
					lElement.getChild("enabled").setText(aEnabled.toString());
				}
			}
		}

		saveChange(lDoc);
	}

	public void removePlugInConfig(String aId) throws Exception {
		Document lDoc = getDocument();
		Element lRootNode = lDoc.getRootElement();
		Element lPlugins = lRootNode.getChild(ELEMENT_PLUGINS);
		List<Element> lPluginsList = lPlugins.getChildren(ELEMENT_PLUGIN);

		for (int i = 0; i < lPluginsList.size(); i++) {
			if (aId.equals(lPluginsList.get(i).getChildText("id"))) {
				lPluginsList.remove(i);
			}
		}

		saveChange(lDoc);
	}
	
	public void removeFilterConfig(String aId) throws Exception {
		Document lDoc = getDocument();
		Element lRootNode = lDoc.getRootElement();
		Element lFilters = lRootNode.getChild(ELEMENT_FILTERS);
		List<Element> lFiltersList = lFilters.getChildren(ELEMENT_FILTER);

		for (int i = 0; i < lFiltersList.size(); i++) {
			if (aId.equals(lFiltersList.get(i).getChildText("id"))) {
				lFiltersList.remove(i);
			}
		}

		saveChange(lDoc);
	}
}
