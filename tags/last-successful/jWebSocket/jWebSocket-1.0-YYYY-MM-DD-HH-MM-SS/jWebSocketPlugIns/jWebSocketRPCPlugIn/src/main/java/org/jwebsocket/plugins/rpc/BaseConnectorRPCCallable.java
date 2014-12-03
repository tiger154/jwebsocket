//	---------------------------------------------------------------------------
//	jWebSocket - BaseConnectorRPCCallable (Community Edition, CE)
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

import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;

/**
 * Connector RPCCallable class. A unique instance of the object is stored for
 * each client. Extends this class if you want a unique object for all the rpc
 * call.
 *
 * @author Quentin Ambard
 */
public class BaseConnectorRPCCallable extends AbstractRPCCallable implements RPCCallable {

	private static final Map<String, BaseConnectorRPCCallable> mInstances
			= new FastMap<String, BaseConnectorRPCCallable>();
	private WebSocketConnector mConnector;

	/**
	 *
	 * @param aConnector
	 */
	public BaseConnectorRPCCallable(WebSocketConnector aConnector) {
		this.mConnector = aConnector;
	}

	/**
	 *
	 * @param aConnector
	 * @return
	 */
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

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	@Override
	public synchronized void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		mInstances.remove(aConnector.getId());
	}

	/**
	 *
	 * @return
	 */
	public WebSocketConnector getConnector() {
		return mConnector;
	}

	/**
	 *
	 * @param aConnector
	 */
	public void setConnector(WebSocketConnector aConnector) {
		this.mConnector = aConnector;
	}
}
