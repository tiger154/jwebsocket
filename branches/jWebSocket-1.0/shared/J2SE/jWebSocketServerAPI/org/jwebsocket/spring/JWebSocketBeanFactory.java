//	---------------------------------------------------------------------------
//	jWebSocket - Spring Bean Factory (Community Edition, CE)
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
package org.jwebsocket.spring;

import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.util.Tools;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * This is required to load the bootstrap.xml config file. It provides a shared
 * beanFactory for all plug-ins and this allows inter-dependencies between the
 * plug-ins and core components.
 *
 * @author alexanderschulze
 * @author kyberneees
 */
public class JWebSocketBeanFactory {

	private static GenericApplicationContext mGlobalContext = null;
	private static Map<String, GenericApplicationContext> mContextMap = new FastMap<String, GenericApplicationContext>();

	/**
	 *
	 * @return
	 */
	public static GenericApplicationContext getInstance() {
		if (null == mGlobalContext) {
			mGlobalContext = new GenericApplicationContext(new DefaultListableBeanFactory());
		}
		return mGlobalContext;
	}

	/**
	 *
	 * @param aNamespace
	 * @return
	 */
	public static GenericApplicationContext getInstance(String aNamespace) {
		if (!mContextMap.containsKey(aNamespace)) {
			mContextMap.put(aNamespace, new GenericApplicationContext(new DefaultListableBeanFactory()));
			//Setting the default (core) application context as parent
			mContextMap.get(aNamespace).setParent(mGlobalContext);
		}
		return mContextMap.get(aNamespace);
	}

	/**
	 * Load beans from a configuration file into the global bean factory
	 *
	 * @param aPath
	 * @param aBeanClassLoader
	 */
	public static void load(String aPath, ClassLoader aBeanClassLoader) {
		load(null, aPath, aBeanClassLoader);
	}

	/**
	 * Load beans from a configuration file into a specific bean factory
	 *
	 * @param aNamespace
	 * @param aPath
	 * @param aClassLoader
	 */
	public static void load(String aNamespace, String aPath, ClassLoader aClassLoader) {
		String lPath = Tools.expandEnvVarsAndProps(aPath);

		XmlBeanDefinitionReader lXmlReader;
		if (null != aNamespace) {
			lXmlReader = new XmlBeanDefinitionReader(getInstance(aNamespace));
		} else {
			lXmlReader = new XmlBeanDefinitionReader(getInstance());
		}

		lXmlReader.setBeanClassLoader(aClassLoader);

		// if no JWEBSOCKET_HOME environment variable set 
		// then use the classpath resource, otherwise the file system resource
		// System.out.println("getJWebSocketHome: '" + JWebSocketConfig.getJWebSocketHome() + "'...");
		if (JWebSocketConfig.getJWebSocketHome().isEmpty()) {
			// System.out.println("Loading resource from classpath: " + aPath + "...");
			lXmlReader.loadBeanDefinitions(new ClassPathResource(lPath));
		} else {
			// System.out.println("Loading resource from filesystem: " + aPath + "...");
			lXmlReader.loadBeanDefinitions(new FileSystemResource(lPath));
		}
	}

	/**
	 * Destroy all GenericApplicationContext instances.
	 */
	public static void destroy() {
		// destroying namespaced application contexts
		for (GenericApplicationContext lContext : JWebSocketBeanFactory.mContextMap.values()) {
			lContext.destroy();
		}

		// destroying global application context
		JWebSocketBeanFactory.mGlobalContext.destroy();
	}
}
