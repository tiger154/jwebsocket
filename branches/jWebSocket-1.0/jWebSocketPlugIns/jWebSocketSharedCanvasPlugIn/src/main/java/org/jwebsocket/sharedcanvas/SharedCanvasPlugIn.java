//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SharedCanvas Plug-in (Community Edition, CE)
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
package org.jwebsocket.sharedcanvas;

import java.util.Collection;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author Daimi Mederos Llanes (dmederos, Artemisa, UCI), Victor Antonio
 * Barzana Crespo (vbarzana, Artemisa, UCI)
 */
public class SharedCanvasPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_SHAREDCANVAS = JWebSocketServerConstants.NS_BASE + ".plugins.sharedcanvas";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket MonitoringPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket MonitoringPlugIn - Community Edition";
	private Collection<WebSocketConnector> mClients;

	/**
	 *
	 * @param aConfiguration
	 */
	public SharedCanvasPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating shared canvas plug-in...");
		}
		setNamespace(NS_SHAREDCANVAS);
		mClients = new FastList<WebSocketConnector>().shared();
		if (mLog.isInfoEnabled()) {
			mLog.info("Shared canvas plug-in instantiated correctly.");
		}
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
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// ensure that we do not keep any dead connectors in the list
		mClients.remove(aConnector);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Unregistered client '" + aConnector.getId() + "' from the shared canvas plug-in.");
		}
	}

	// TODO: implement unregister function to complete API
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if ("register".equals(aToken.getType())) {
				mClients.add(aConnector);
				if (mLog.isDebugEnabled()) {
					mLog.debug("Registered client '" + aConnector.getId() + "' at the shared canvas plug-in.");
				}
			} else {
				broadcast(aToken);
			}
		}
	}

	/**
	 *
	 * @param aToken
	 */
	public void broadcast(Token aToken) {
		for (WebSocketConnector lConnector : mClients) {
			getServer().sendToken(lConnector, aToken);
		}
	}
}