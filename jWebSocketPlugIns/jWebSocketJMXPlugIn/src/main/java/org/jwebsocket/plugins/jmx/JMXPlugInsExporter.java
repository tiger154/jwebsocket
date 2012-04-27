// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn v1.0
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.plugins.jmx;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.factory.JWebSocketJarClassLoader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.jmx.configdefinition.ConstuctorParameterDefinition;
import org.jwebsocket.plugins.jmx.configdefinition.JMXDefinition;
import org.jwebsocket.plugins.jmx.configdefinition.JMXDefinitionException;
import org.jwebsocket.plugins.jmx.configdefinition.JMXPluginDefinition;
import org.jwebsocket.plugins.jmx.mbeanspring.MBeanEnabledExporter;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * Main class of the mechanism that allows integrate plugins and classes to JMX 
 * infrastructure of the module.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXPlugInsExporter {

	private Logger mLog = Logging.getLogger();
	private String mConfigFilePath;
	private MBeanServer mServer;

	/**
	 * The class constructor.
	 * 
	 * @param aPath
	 * @param aMBeanServer
	 * @param aLog
	 */
	public JMXPlugInsExporter(String aPath, MBeanServer aMBeanServer) {
		this.mConfigFilePath = aPath;
		this.mServer = aMBeanServer;
	}

	/**
	 * Private method that allows to list the names of the configuration files 
	 * for the plugins and classes that will be exported.
	 * 
	 * @return List
	 */
	private List listConfigFilesNames() {
		List lAllConfigFiles = new FastList();
		try {
			File lPathOfConfig = new File(mConfigFilePath);
			if (false == lPathOfConfig.exists() || 
					false == lPathOfConfig.isDirectory()) {
				throw new IllegalArgumentException("The config file path is "
						+ "incorrect.");
			}
			for (String lfileName : lPathOfConfig.list()) {
				if (lfileName.toLowerCase().endsWith("beanconfig.xml")) {
					lAllConfigFiles.add(lfileName);
				}
			}
		} catch (Exception ex) {
			mLog.error("JMXPlugInExporter on listConfigFilesNames: " 
					+ ex.getMessage());
		}
		return lAllConfigFiles;
	}

	/**
	 * Main method of the class that is responsible for loading the configuration 
	 * files, create the objects of classes and plugins to export and register 
	 * it in JMX infrastructure created.
	 * 
	 * @throws Exception
	 */
	public void createMBeansToExport() throws Exception {
		List<String> lConfigFiles = listConfigFilesNames();
		MBeanEnabledExporter lExporter = new MBeanEnabledExporter();
		lExporter.setServer(mServer);
		Map lBean = new FastMap();
		for (String lConfigFile : lConfigFiles) {
			JWebSocketBeanFactory.load(mConfigFilePath + lConfigFile, getClass().getClassLoader());
			ApplicationContext lBeanFactory = JWebSocketBeanFactory.getInstance();
			
			if (lConfigFile.toLowerCase().endsWith("beanconfig.xml")) {
				String lBeanName = lConfigFile.substring(0, lConfigFile.length() - 14);
				registerMBean(lExporter, lBeanFactory, lBeanName, lBean);
			}
		}
		try {
			if (!lBean.isEmpty()) {
				lExporter.setBeans(lBean);
				lExporter.afterPropertiesSet();
			}
		} catch (Exception ex) {
			mLog.error("JMXPlugInExporter on createMBeansToExport: " 
					+ ex.getMessage());
		}
	}

	/**
	 * Private method that creates a new instance of a class given its name, the
	 * values of the parameters of the constructor and the .jar in which it is 
	 * contained.
	 * 
	 * @param aClassName
	 * @param aJarName
	 * @param aValues
	 * @return
	 * @throws Exception 
	 */
	private Object getNewClassInstance(String aClassName, String aJarName, 
			Object[] aValues) throws Exception {
		JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
		String lJarFilePath = JWebSocketConfig.getLibsFolder(aJarName);
		Class lClass = null;
		Object lObj = null;

		//lJarFilePath may be null if .jar is included in server bundle
		try {
			if (lJarFilePath != null) {
				lClassLoader.addFile(lJarFilePath);
				lClass = lClassLoader.loadClass(aClassName);
			}

			// if class found try to create an instance
			if (lClass != null) {
				if (aValues == null) {
					lObj = lClass.newInstance();
				} else {
					Class[] lTypes = new Class[aValues.length];
					for (int i = 0; i < aValues.length; i++) {
						lTypes[i] = aValues[i].getClass();
					}

					Constructor lClassConstructor = lClass.getConstructor(lTypes);

					if (lClassConstructor != null) {
						lClassConstructor.setAccessible(true);
						lObj = lClassConstructor.newInstance(aValues);
					}
				}
			}

		} catch (Exception ex) {
			throw new Exception(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		return lObj;
	}

	private void registerMBean(MBeanEnabledExporter aExporter, ApplicationContext aBeanFactory, String aBeanName, Map aBeanMap) throws Exception {
		try {
			JMXDefinition lDefinition = (JMXDefinition) 
					aBeanFactory.getBean(aBeanName);

			if (lDefinition instanceof JMXPluginDefinition) {
				JMXPluginDefinition lPluginDefinition = 
						(JMXPluginDefinition) lDefinition;
				//get the object of the server
				WebSocketServer lServer = 
						JWebSocketFactory.getServer(lPluginDefinition.getServerId());
				//get the plugin by id
				Object lMbeanObject = 
						lServer.getPlugInById(lPluginDefinition.getPluginId());

				aBeanMap.put("jWebSocketServer:name=" + aBeanName, lMbeanObject);
			} else {
				Object lClass = null;
				if (lDefinition.getConstructors().length > 0) {
					ConstuctorParameterDefinition[] lParameters = 
							lDefinition.getConstructors()[0].getParameters();

					if (lParameters.length == 0 || lParameters == null) {
						lClass = getNewClassInstance(lDefinition.getClassName(), 
								lDefinition.getJarName(), null);
					} else {
						Object[] lValues = new Object[lParameters.length];
						for (int i = 0; i < lParameters.length; i++) {
							lValues[i] = lParameters[i].getValue();
						}
						lClass = getNewClassInstance(lDefinition.getClassName(), 
								lDefinition.getJarName(), lValues);
					}
				}
				if (lClass != null) {
					aBeanMap.put("jWebSocketServer:name=" + aBeanName, lClass);
				} else {
					throw new Exception("The class " + aBeanName + " can't be "
							+ "instanciated");
				}
			}

			if (!aBeanName.equals("")) {
				aExporter.setDefinition("jWebSocketServer:name=" + aBeanName, 
						lDefinition);
			}
		} catch (Exception e) {
			mLog.error("JMXPlugInExporter on registerMBean: " + e.getMessage());

			JMXDefinitionException lDefinitionException = 
					new JMXDefinitionException("Unable to register the class: " 
					+ e.getMessage());
			
			if (!aBeanName.equals("")) {
				aBeanMap.put("jWebSocketServer:name=" + aBeanName + "Exception", 
						lDefinitionException);
			} else {
				aBeanMap.put("jWebSocketServer:name=MBeanException", 
						lDefinitionException);
			}
			aExporter.setDefinition("jWebSocketServer:name=" + aBeanName 
					+ "Exception", lDefinitionException);
		}
	}
}
