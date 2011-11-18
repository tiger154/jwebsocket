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

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 * Abstract RPCCallable class.
 * Add a method to get an instance of the rpcCallable class created.
 * @author Quentin Ambard
 */
public abstract class AbstractRPCCallable {	
	private static Logger mLog = Logging.getLogger(AbstractRPCCallable.class);
		
	/**
	 * Return an instance of the RpcCallableClass which extends this AbstractRPCCallable class using the default constructor. 
	 * (call the method getInstanceOfRpcCallableClass(null, null))
	 * @return instance of the RPCCallable class
	 */
	public RPCCallable getInstanceOfRpcCallableClass() {
		return getInstanceOfRpcCallableClass(null, null);
	}
	
	/**
	 * return an instance of the RpcCallableClass which extends this AbstractRPCCallable class.
	 * Usually called with a WebSocketConnector as parameter (or null if no parameters)
	 * @param aListOfParameter
	 * @param aListOfClass
	 * @return instance of the RPCCallable class
	 */
	public RPCCallable getInstanceOfRpcCallableClass(Object[] aListOfParameter, Class[] aListOfClass) {
		RPCCallable lNewInstance = null ;
		//We get the class of the instance
		Class lClass = this.getClass();
		//Get the constructor of this class
		try {
			if (aListOfClass == null) {
			lNewInstance = (RPCCallable) lClass.getConstructor().newInstance() ;
			} else  {
				lNewInstance = (RPCCallable) lClass.getConstructor(aListOfClass).newInstance(aListOfParameter) ;
			}
		} catch (Exception e) {
			mLog.error("Can't build an instance of the RPCCallable class" + lClass.getName() +". " +
						"classes: " + aListOfClass +" - parameters: "+aListOfParameter+"." +
						e.getMessage());
			return null ;
		}
		return  lNewInstance ;
	}
}
