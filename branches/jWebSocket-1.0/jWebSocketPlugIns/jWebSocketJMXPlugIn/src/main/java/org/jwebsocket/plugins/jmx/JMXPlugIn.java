// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn (Community Edition, CE)
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
package org.jwebsocket.plugins.jmx;

import javax.management.MBeanServer;
import javax.management.remote.rmi.RMIConnectorServer;
import mx4j.tools.adaptor.http.HttpAdaptor;
import mx4j.tools.adaptor.ssl.SSLAdaptorServerSocketFactory;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jmx.util.JMXPlugInAuthenticator;
import org.springframework.context.ApplicationContext;

/**
 * Main class of the module which takes care of creating the JMX infrastructure
 * to use. Initializes all other components within the module.
 *
 * @author Lisdey Perez Hernandez
 */
public class JMXPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private static JMXPlugIn mJmxPlugin = null;
	private HttpAdaptor mHttpAdaptor = null;
	private HttpAdaptor mHttpSSLAdaptor = null;
	private RMIConnectorServer mRmiConnector = null;
	private RMIConnectorServer mRmiSSLConnector = null;
	/**
	 *
	 */
	public static final String NS_JMX
			= JWebSocketServerConstants.NS_BASE + ".plugins.jmx";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket JMXPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket JMXPlugIn - Community Edition";

	/**
	 * The class constructor.
	 *
	 * @param aConfiguration
	 */
	public JMXPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);

		setNamespace(NS_JMX);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JMX PlugIn...");
		}
		this.mJmxPlugin = this;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_JMX;
	}

	/**
	 * Static method that returns an instance of this class, which is used in
	 * the module configuration file.
	 *
	 * @return JMXPlugIn
	 */
	public static JMXPlugIn getInstance() {
		return mJmxPlugin;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aEngine
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {

		ApplicationContext lFactory = getConfigBeanFactory();
		JMXPlugInAuthenticator.setConfigPath(getString("spring_config"));

		lFactory.getBean("exporter");

		SSLAdaptorServerSocketFactory lSSLSocket = new SSLAdaptorServerSocketFactory();
		lSSLSocket.setKeyStoreName(JWebSocketConfig.getConfigFolder("jWebSocket.ks"));
		String lKeyPass = getServer().getEngines().get("tcp0").getConfiguration().getKeyStorePassword();
		lSSLSocket.setKeyStorePassword(lKeyPass);
		mHttpSSLAdaptor.setSocketFactory(lSSLSocket);

		try {
			mRmiConnector = (RMIConnectorServer) lFactory.getBean("rmiConnector");
			mRmiSSLConnector = (RMIConnectorServer) lFactory.getBean("rmiSSLConnector");
			mHttpAdaptor = (HttpAdaptor) lFactory.getBean("HttpAdaptor");
			mHttpSSLAdaptor = (HttpAdaptor) lFactory.getBean("HttpSSLAdaptor");

			mRmiConnector.start();
			mRmiSSLConnector.start();

			mHttpAdaptor.addAuthorization(getString("http_user"), getString("http_password"));
			mHttpSSLAdaptor.addAuthorization(getString("http_user"), getString("http_password"));

			mHttpAdaptor.start();
			mHttpSSLAdaptor.start();

			MBeanServer lMBServer = (MBeanServer) lFactory.getBean("jWebSocketServer");

			String lBeanPath = JWebSocketConfig.getConfigFolder("")
					+ getString("beans_config");

			JMXPlugInsExporter lPluginsExporter
					= new JMXPlugInsExporter(lBeanPath, lMBServer);

			lPluginsExporter.createMBeansToExport();
		} catch (Exception ex) {
			mLog.error("JMX plug-in on engineStarted: " + ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aEngine
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		try {
			mHttpAdaptor.stop();
			mHttpSSLAdaptor.stop();
			mRmiConnector.stop();
			mRmiSSLConnector.stop();
		} catch (Exception ex) {
			mLog.error("JMX plug-in on engineStopped: " + ex.getMessage());
		}
	}
}
