//	---------------------------------------------------------------------------
//	jWebSocket - BaseRPCCallable (Community Edition, CE)
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
package org.jwebsocket.plugins.rpc;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;

/**
 * Base RPCCallable class. Everytime a method from this class will be called by
 * a client, the same instance of the class will used Extends this class if you
 * want a unique object for all the rpc call.
 *
 * @author Quentin Ambard
 */
public class BaseRPCCallable extends AbstractRPCCallable implements RPCCallable {

	/**
	 *
	 */
	protected BaseRPCCallable() {
	}
	private static volatile BaseRPCCallable mInstance = null;

	/**
	 *
	 * @param aConnector
	 * @return
	 */
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

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
	}
}
