//	---------------------------------------------------------------------------
//	jWebSocket - Automated Tests Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.test;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class AutomTestPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private static final String NS_TEST_AUTOMATION
			= JWebSocketServerConstants.NS_BASE + ".plugins.test";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket AutomTestPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION
			= "jWebSocket Automated Tests PlugIn - Community Edition";
	// TODO: remove t. prefix here!
	private final static String BROADCAST = "t.broadcast";
	private final static String COMPLEX_VALIDATION = "t.complex_validation";
	private final static String DELAY = "delay";

	/**
	 *
	 * @param aConfiguration
	 */
	public AutomTestPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating automated test plug-in...");
		}
		// specify default name space for automated test plugin
		this.setNamespace(NS_TEST_AUTOMATION);
		// give a success message to the administrator
		if (mLog.isInfoEnabled()) {
			mLog.info("Automated test plug-in successfully instantiated.");
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
		return NS_TEST_AUTOMATION;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		// if (lType != null && getNamespace().equals(lNS)) {
		if (BROADCAST.equals(lType)) {
			broadcast(aConnector, aToken);
		} else if (COMPLEX_VALIDATION.equals(lType)) {
			complexValidation(aConnector, aToken);
		} else if (DELAY.equals(lType)) {
			delay(aConnector, aToken);
		}
		// }
	}

	/**
	 * Broadcast a text to all the connectors
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void broadcast(WebSocketConnector aConnector, Token aToken) {
		Token lToken = TokenFactory.createToken();

		String lText = aToken.getString("text", "Missing text!");
		lToken.setString("text", lText);

		getServer().broadcastToken(aToken);
	}

	/**
	 * Simply waits for a given amount of milliseconds. This is to test the
	 * timeout behaviour of the client. Default is 2000ms (=2sec).
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void delay(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		int lDelay = aToken.getInteger("delay", 2000);
		try {
			Thread.sleep(lDelay);
		} catch (InterruptedException lEx) {
		}

		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Return a complex object to validate
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void complexValidation(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		//Return a complex object
		lResponse.setString("name", "JWebSocket");
		lResponse.setDouble("version", 1.1);

		List<String> lTeam = new FastList<String>();
		lTeam.add("one");
		lTeam.add("two");
		lTeam.add("three");
		lTeam.add("four");
		lTeam.add("five");
		lTeam.add("...");
		lResponse.setList("team", lTeam);

		Map<String, Boolean> lServices = new FastMap<String, Boolean>();
		lServices.put("chat", Boolean.TRUE);
		lServices.put("twitter", Boolean.TRUE);
		lServices.put("channels", Boolean.TRUE);
		lServices.put("mail", Boolean.TRUE);
		lServices.put("facebook", Boolean.FALSE);
		lResponse.setMap("services", lServices);

		sendToken(aConnector, aConnector, lResponse);
	}
}
