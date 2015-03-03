//	---------------------------------------------------------------------------
//	jWebSocket - Spring Bean Factory (Community Edition, CE)
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
package org.jwebsocket.spring;

import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.Tools;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

/**
 * This is required to load the bootstrap.xml config file. It provides a shared
 * beanFactory for all plug-ins and this allows inter-dependencies between the
 * plug-ins and core components.
 *
 * @author alexanderschulze
 * @author Rolando Santamaria Maso
 */
public class JWebSocketBeanFactory {

	private static final Logger mLog = Logging.getLogger();
	private static GenericApplicationContext mGlobalContext = null;
	private static final Map<String, GenericApplicationContext> mContextMap = new FastMap<String, GenericApplicationContext>();

	/**
	 *
	 * @return
	 */
	public static GenericApplicationContext getInstance() {
		if (null == mGlobalContext) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Creating global bean factory...");
			}
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
		Assert.notNull(aNamespace, "The 'namespace' argument cannot be null!");

		if (!mContextMap.containsKey(aNamespace)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Creating namespaced bean factory '" + aNamespace + "'...");
			}
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
		if (mLog.isDebugEnabled()) {
			if (null != aNamespace) {
				mLog.debug("Loading '" + aPath + "' content beans on '" + aNamespace + "' bean factory...");
			} else {
				mLog.debug("Loading '" + aPath + "' content beans on global bean factory...");
			}
		}

		String lPath = JWebSocketConfig.expandEnvVarsAndProps(aPath);

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
		if (mLog.isDebugEnabled()) {
			mLog.debug("Destroying bean factories...");
		}

		// destroying namespaced application contexts
		for (GenericApplicationContext lContext : JWebSocketBeanFactory.mContextMap.values()) {
			try {
				lContext.destroy();
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "destroying " + lContext.getDisplayName() + " bean factory..."));
			}
		}

		try {
			mGlobalContext.refresh();
		} catch (Exception lEx) {
			// fails if running embedded in a Web app
		}
		try {
			// destroying global application context
			mGlobalContext.destroy();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "destroying global bean factory..."));
		}
	}
}
