//	---------------------------------------------------------------------------
//	jWebSocket - RPC PlugIn
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.rpc;

import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;

/**
 * Connector RPCCallable class.
 * A unique instance of the object is stored for each client.
 * Extends this class if you want a unique object for all the rpc call.
 * @author Quentin Ambard
 */
public class BaseConnectorRPCCallable extends AbstractRPCCallable implements RPCCallable {

	private static Map<String, BaseConnectorRPCCallable> mInstances = new FastMap<String, BaseConnectorRPCCallable>();
	private WebSocketConnector mConnector;

	public BaseConnectorRPCCallable(WebSocketConnector aConnector) {
		this.mConnector = aConnector;
	}

	@Override
	public synchronized RPCCallable getInstance(WebSocketConnector aConnector) {
		if (mInstances.containsKey(aConnector.getId())) {
			return mInstances.get(aConnector.getId());
		}
		RPCCallable lNewInstance = getInstanceOfRpcCallableClass(
				new Object[]{aConnector},
				new Class[]{WebSocketConnector.class});
		mInstances.put(aConnector.getId(), (BaseConnectorRPCCallable) lNewInstance);
		return lNewInstance;
	}

	@Override
	public synchronized void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		mInstances.remove(aConnector.getId());
	}

	public WebSocketConnector getConnector() {
		return mConnector;
	}

	public void setConnector(WebSocketConnector aConnector) {
		this.mConnector = aConnector;
	}
}
