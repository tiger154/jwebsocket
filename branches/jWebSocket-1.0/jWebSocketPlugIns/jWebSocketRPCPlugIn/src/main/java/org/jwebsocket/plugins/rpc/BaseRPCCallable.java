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

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;

/**
 * Base RPCCallable class.
 * Everytime a method from this class will be called by a client, the same instance of the class will used
 * Extends this class if you want a unique object for all the rpc call.
 * @author Quentin Ambard
 */
public class BaseRPCCallable extends AbstractRPCCallable implements RPCCallable {

	protected BaseRPCCallable() {
	}
	private static volatile BaseRPCCallable mInstance = null;

	@Override
	public RPCCallable getInstance(WebSocketConnector aConnector) {
		if (mInstance == null) {
			synchronized (this) {
				if (mInstance == null) {
					mInstance = (BaseRPCCallable) getInstanceOfRpcCallableClass();
				}
			}
		}
		return mInstance;
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
	}
}
