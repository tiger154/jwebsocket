//	---------------------------------------------------------------------------
//	jWebSocket Demo Plug-in for Support (Community Edition, CE)
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
package org.jwebsocket.plugins.myplugin;

import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class MyPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private final static String NS_SAMPLE = "tld.domain.plugins.myplugin";
	private final static String SAMPLE_VAR = NS_SAMPLE + ".started";
	
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket FileSystemPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket FileSystemPlugIn - Community Edition";

	/**
	 *
	 * @param aConfiguration
	 */
	public MyPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating my plug-in...");
		}
		// specify default name space for sample plugin
		this.setNamespace(NS_SAMPLE);
		
		if (mLog.isInfoEnabled()) {
			mLog.info("My plug-in successfully instantiated.");
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
	public void connectorStarted(WebSocketConnector aConnector) {
		// this method is called every time when a client
		// connected to the server
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// this method is called every time when a client
		// disconnected from the server
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		// this method is called when the engine has started
		super.engineStarted(aEngine);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		// this method is called when the engine has stopped
		super.engineStopped(aEngine);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		// get the type of the token
		// the type can be associated with a "command"
		String lType = aToken.getType();

		// get the namespace of the token
		// each plug-in should have its own unique namespace
		String lNS = aToken.getNS();

		// check if token has a type and a matching namespace
		if (lType != null && lNS != null && lNS.equals(getNamespace())) {

			// get the server time
			if ("mydemo_function".equals(lType)) {
				// create the response token
				// this includes the unique token-id
				Token lResponse = createResponse(aToken);

				// add the "time" and "started" field
				lResponse.setString("time", new Date().toString());
				lResponse.setString("started", (String) aConnector.getVar(SAMPLE_VAR));

				// lResponse.setString("vendor", "jwebsocket.org");
				// lResponse.setString("version", "2.3.0815");

				// send the response token back to the client
				sendToken(aConnector, aConnector, lResponse);
			}
		}
	}
}
