//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SharedCanvas Plug-in
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.sharedcanvas;

import java.util.Collection;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 * 
 * @author Daimi Mederos Llanes (dmederos, Artemisa, UCI), Victor Antonio Barzana Crespo (vbarzana, Artemisa, UCI)
 */
public class SharedCanvasPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(SharedCanvasPlugIn.class);
	private Collection<WebSocketConnector> mClients;

	public SharedCanvasPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating shared canvas plug-in...");
		}
		setNamespace(aConfiguration.getNamespace());
		mClients = new FastList<WebSocketConnector>().shared();
		if (mLog.isInfoEnabled()) {
			mLog.info("Shared canvas plug-in instantiated correctly.");
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// ensure that we do not keep any dead connectors in the list
		mClients.remove(aConnector);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Unregistered client " + aConnector.getId() + " from the shared canvas plug-in.");
		}
	}

	// TODO: implement unregister function to complete API
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if ("register".equals(aToken.getType())) {
				mClients.add(aConnector);
				if (mLog.isDebugEnabled()) {
					mLog.debug("Registered client " + aConnector.getId() + " at the shared canvas plug-in.");
				}
			} else {
				broadcast(aToken);
			}
		}
	}

	public void broadcast(Token aToken) {
		for (WebSocketConnector lConnector : mClients) {
			getServer().sendToken(lConnector, aToken);
		}
	}
}