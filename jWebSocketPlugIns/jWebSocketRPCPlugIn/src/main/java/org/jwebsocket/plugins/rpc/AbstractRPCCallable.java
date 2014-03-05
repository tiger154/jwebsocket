//	---------------------------------------------------------------------------
//	jWebSocket - AbstractRPCCallable (Community Edition, CE)
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

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 * Abstract RPCCallable class. Add a method to get an instance of the
 * rpcCallable class created.
 *
 * @author Quentin Ambard
 */
public abstract class AbstractRPCCallable {

	private static final Logger mLog = Logging.getLogger();

	/**
	 * Return an instance of the RpcCallableClass which extends this
	 * AbstractRPCCallable class using the default constructor. (call the method
	 * getInstanceOfRpcCallableClass(null, null))
	 *
	 * @return instance of the RPCCallable class
	 */
	public RPCCallable getInstanceOfRpcCallableClass() {
		return getInstanceOfRpcCallableClass(null, null);
	}

	/**
	 * return an instance of the RpcCallableClass which extends this
	 * AbstractRPCCallable class. Usually called with a WebSocketConnector as
	 * parameter (or null if no parameters)
	 *
	 * @param aListOfParameter
	 * @param aListOfClass
	 * @return instance of the RPCCallable class
	 */
	public RPCCallable getInstanceOfRpcCallableClass(Object[] aListOfParameter, Class[] aListOfClass) {
		RPCCallable lNewInstance;
		//We get the class of the instance
		Class lClass = this.getClass();
		//Get the constructor of this class
		try {
			if (aListOfClass == null) {
				lNewInstance = (RPCCallable) lClass.getConstructor().newInstance();
			} else {
				lNewInstance = (RPCCallable) lClass.getConstructor(aListOfClass).newInstance(aListOfParameter);
			}
		} catch (Exception lEx) {
			mLog.error("Can't build an instance of the RPCCallable class" + lClass.getName() + ". "
					+ "classes: " + aListOfClass + " - parameters: " + aListOfParameter + "."
					+ lEx.getMessage());
			return null;
		}
		return lNewInstance;
	}
}
