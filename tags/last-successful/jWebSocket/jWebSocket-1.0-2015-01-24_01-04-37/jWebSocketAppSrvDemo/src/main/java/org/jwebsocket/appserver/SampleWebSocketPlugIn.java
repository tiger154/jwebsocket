//	---------------------------------------------------------------------------
//	jWebSocket - SampleWebSocketPlugIn (Community Edition, CE)
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
package org.jwebsocket.appserver;

import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.token.Token;

/**
 * Sample jWebSocket plug-in
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public class SampleWebSocketPlugIn extends ActionPlugIn {

	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SampleWebSocketPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket SampleWebSocketPlugIn - Community Edition";

	public SampleWebSocketPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
	}

	public void sayHelloAction(WebSocketConnector aConnector, Token aRequest) throws Exception {
		String lName = aRequest.getString("name");

		Token lResponse = createResponse(aRequest);
		lResponse.setString("data", "Hello '" + lName + "', from a jWebSocket plug-in ;)\n"
				+ "HOME: " + JWebSocketConfig.expandEnvVarsAndProps("${WEB_APP_HOME}"));

		sendToken(aConnector, lResponse);
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
}
