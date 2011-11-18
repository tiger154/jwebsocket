//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.test;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class AutomTestPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(PerfTestPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_TEST_AUTOMATION =
			JWebSocketServerConstants.NS_BASE + ".plugins.test";
	// TODO: remove t. prefix here!
	private final static String BROADCAST = "t.broadcast";
	private final static String COMPLEX_VALIDATION = "t.complex_validation";
	private final static String DELAY = "delay";

	public AutomTestPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating automated test plug-in...");
		}
		// specify default name space for automated test plugin
		this.setNamespace(NS_TEST_AUTOMATION);
		// give a success message to the administrator
		if (mLog.isInfoEnabled()) {
			mLog.info("Automated test plug-in successfully loaded.");
		}
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
	 * Simply waits for a given amount of milliseconds.
	 * This is to test the timeout behaviour of the client.
	 * Default is 2000ms (=2sec).
	 * @param aConnector
	 * @param aToken 
	 */
	public void delay(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		int lDelay = aToken.getInteger( "delay", 2000 );
		try {
			Thread.sleep(lDelay);
		} catch (Exception lEx) {
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
