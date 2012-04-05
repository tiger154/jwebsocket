//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Plug-In Template
//	Copyright (c) 2011 jWebSocket.org, Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.myplugin;

import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class MyPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private final static String NS_SAMPLE = "tld.domain.plugins.myplugin";
	private final static String SAMPLE_VAR = NS_SAMPLE + ".started";

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
