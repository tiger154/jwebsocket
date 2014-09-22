//	---------------------------------------------------------------------------
//	jWebSocket - Chat Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.sharedcanvas;

import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

public class SharedCanvasPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private static final FastMap<String, WebSocketConnector> mClients
			= new FastMap<String, WebSocketConnector>().shared();
	public static final String NS_CANVAS = "jws.canvas";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SharedCanvasPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket SharedCanvasPlugIn - Community Edition";
	private final static String TT_REGISTER = "register";
	private final static String TT_UNREGISTER = "unregister";
	private final static String TT_DATA = "data";

	/**
	 * This PlugIn shows how to easily set up a simple jWebSocket based chat
	 * system.
	 *
	 * @param aConfiguration
	 */
	public SharedCanvasPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating SharedCanvasPlugIn...");
		}
		// specify default name space for chat plugin
		this.setNamespace(NS_CANVAS);

		if (mLog.isInfoEnabled()) {
			mLog.info("SharedCanvasPlugIn successfully instantiated.");
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
	public String getNamespace() {
		return NS_CANVAS;
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		unregister(aConnector);
	}

	@Override
	public void processLogoff(WebSocketConnector aConnector) {
		unregister(aConnector);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if (TT_REGISTER.equals(aToken.getType())) {
				mClients.put(aConnector.getId(), aConnector);
			} else if (TT_UNREGISTER.equals(aToken.getType())) {
				unregister(aConnector);
			} else if (TT_DATA.equals(aToken.getType())) {
				broadcast(aConnector, aToken);
			}
		}
	}

	private void broadcast(WebSocketConnector aConnector, Token aToken) {
		// TODO: Maybe some condition here is needed
		aToken.setString("publisher", aConnector.getId());
		for (Map.Entry<String, WebSocketConnector> lEntry : mClients.entrySet()) {
			getServer().sendToken(lEntry.getValue(), aToken);
		}
	}

	private void unregister(WebSocketConnector aConnector) {
		if (!mClients.isEmpty() && mClients.containsKey(aConnector.getId())) {
			mClients.remove(aConnector.getId());
		}
	}
}
